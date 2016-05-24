/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.anatomy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.anatomy.events.DoAnatomyDamageEvent;
import org.terasology.anatomy.events.DoAnatomyDeadEvent;
import org.terasology.anatomy.events.DoAnatomyHealEvent;
import org.terasology.anatomy.events.DoAnatomyReviveEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.registry.In;

/**
 * Provides a basic system for managing an entity's anatomy.
 * Much of this system is still under development.
 */
@RegisterSystem
public class AnatomySystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(AnatomySystem.class);

    @In
    private EntityManager entityManager;

    @In
    private DelayManager delayManager;

    // USED only for TESTING, with the assumption that ONLY the local player has the AnatomyComponent attached.
    @In
    private EntityRef playerRef;

    // TODO: Determine why OnPlayerSpawnedEvent is not being detected.
    @ReceiveEvent
    public void onPlayerCreationEvent(OnPlayerSpawnedEvent event, EntityRef player) {
        /*
        EntityRef head = entityManager.create("anatomy:humanHead");
        EntityRef body = entityManager.create("anatomy:humanBody");
        EntityRef arms = entityManager.create("anatomy:humanArms");
        EntityRef legs = entityManager.create("anatomy:humanLegs");

        AnatomyComponent anatomy = player.getComponent(AnatomyComponent.class);
        anatomy.aParts.add(head);
        anatomy.aParts.add(body);
        anatomy.aParts.add(legs);
        anatomy.aParts.add(arms);
        */
    }

    @ReceiveEvent
    public void onHealthRegenDelayCompletion(PeriodicActionTriggeredEvent event, EntityRef entityRef, AnatomyPartComponent part) {
        if (event.getActionId().equals("Anatomy:RegenHealth")) {
            //heal(part, part.healthRegen);
        }
    }

    @ReceiveEvent(components = {AnatomyPartComponent.class})
    public void onDamage(DoAnatomyDamageEvent event, EntityRef entity, AnatomyPartComponent comp) {
        if (comp.isAlive) {
            comp.health -= event.getAmount();

            logger.info(comp.name + " has taken " + event.getAmount() + " points of damage!");

            if (comp.health <= 0) {
                // Send event indicating that this anatomy part is dead.
                entity.send(new DoAnatomyDeadEvent(comp, event.getInstigator()));
            }
        }
    }

    @ReceiveEvent(components = {AnatomyPartComponent.class})
    public void onDead(DoAnatomyDeadEvent event, EntityRef entity) {
        event.getPart().health = 0;
        event.getPart().isAlive = false;
        logger.info(event.getPart().name + " has been destroyed!");
    }

    @ReceiveEvent(components = {AnatomyPartComponent.class})
    public void onHeal(DoAnatomyHealEvent event, EntityRef entity, AnatomyPartComponent comp) {
        heal(comp, event.getAmount());
    }

    @ReceiveEvent(components = {AnatomyPartComponent.class})
    public void onRevive(DoAnatomyReviveEvent event, EntityRef entity, AnatomyPartComponent comp) {
        if (!comp.isAlive) {
            comp.isAlive = true;

            heal(comp, event.getAmount());
            logger.info(comp.name + " has been revived with " + comp.health + " health!");
        }
        else
        {
            logger.info(comp.name + " is already alive.");
        }
    }

    private void heal(AnatomyPartComponent part, int amount) {
        if (part.isAlive && !part.isHealthFull()) {
            part.health += amount;

            logger.info(part.name + " has recovered " + amount + " points of health!");

            if (part.health >= part.maxHealth) {
                part.health = part.maxHealth;

                logger.info(part.name + " is at max health!");
            }
        }
        else if (part.isHealthFull()) {
            logger.info(part.name + " is already at max health!");
        }
        else {
            logger.info(part.name + " can't be healed as it's dead!");
        }
    }

    // The following commands are used for debugging.

    // For this current revision, this command needs to be run before the Anatomy system is up and running properly.
    // This is temporary until I figure out why OnPlayerSpawnedEvent is not being detected.
    @Command(shortDescription = "Create all the Anatomy parts", runOnServer = true)
    public void createAnatomyParts() {

        EntityRef player = EntityRef.NULL;
        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyComponent.class)) {
            player = clientEntity;
            break;
        }

        // Create anatomy parts for a human character.
        EntityRef head = entityManager.create("anatomy:humanHead");
        EntityRef body = entityManager.create("anatomy:humanBody");
        EntityRef arms = entityManager.create("anatomy:humanArms");
        EntityRef legs = entityManager.create("anatomy:humanLegs");

        AnatomyComponent anatomy = player.getComponent(AnatomyComponent.class);
        anatomy.aParts.add(head);
        anatomy.aParts.add(body);
        anatomy.aParts.add(legs);
        anatomy.aParts.add(arms);
    }

    @Command(shortDescription = "Damage Anatomy part for amount", runOnServer = true)
    public void dmgAnatomyPart(@CommandParam("name") String partName, @CommandParam("amount") int amount) {
        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyComponent.class)) {
            for (EntityRef partEntityRef : clientEntity.getComponent(AnatomyComponent.class).aParts) {
                if (partEntityRef.getComponent(AnatomyPartComponent.class).name.equalsIgnoreCase(partName)) {
                    partEntityRef.send(new DoAnatomyDamageEvent(amount, partName, null, null, null));
                }
            }
        }
    }

    @Command(shortDescription = "Damage ALL Anatomy parts for amount", runOnServer = true)
    public void dmgAnatomyPartAll(@CommandParam("amount") int amount) {
        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyComponent.class)) {
            for (EntityRef partEntityRef : clientEntity.getComponent(AnatomyComponent.class).aParts) {
                partEntityRef.send(new DoAnatomyDamageEvent(amount));
            }
        }
    }

    @Command(shortDescription = "Heal Anatomy part for amount", runOnServer = true)
    public void healAnatomyPart(@CommandParam("name") String partName, @CommandParam("amount") int amount) {
        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyComponent.class)) {
            for (EntityRef partEntityRef : clientEntity.getComponent(AnatomyComponent.class).aParts) {
                if (partEntityRef.getComponent(AnatomyPartComponent.class).name.equalsIgnoreCase(partName)) {
                    partEntityRef.send(new DoAnatomyHealEvent(amount, partName, null));
                }
            }
        }
    }

    @Command(shortDescription = "Heal ALL Anatomy parts for amount", runOnServer = true)
    public void healAnatomyPartAll(@CommandParam("amount") int amount) {
        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyComponent.class)) {
            for (EntityRef partEntityRef : clientEntity.getComponent(AnatomyComponent.class).aParts) {
                partEntityRef.send(new DoAnatomyHealEvent(amount));
            }
        }
    }

    @Command(shortDescription = "Revive Anatomy component for amount", runOnServer = true)
    public void reviveAnatomyPart(@CommandParam("name") String partName, @CommandParam("amount") int amount) {
        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyComponent.class)) {
            for (EntityRef partEntityRef : clientEntity.getComponent(AnatomyComponent.class).aParts) {
                if (partEntityRef.getComponent(AnatomyPartComponent.class).name.equalsIgnoreCase(partName)) {
                    partEntityRef.send(new DoAnatomyReviveEvent(amount, partName, null));
                }
            }
        }
    }

    @Command(shortDescription = "Show all Anatomy parts' health", runOnServer = true)
    public void getAnatomyPartHealth() {
        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyComponent.class)) {
            logger.info(clientEntity.toString() + " has the following anatomy health values:");

            for (EntityRef partEntityRef : clientEntity.getComponent(AnatomyComponent.class).aParts) {
                AnatomyPartComponent part = partEntityRef.getComponent(AnatomyPartComponent.class);
                logger.info(part.name + " has health: " + part.health + "/" + part.maxHealth);
            }
        }
    }
}
