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
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
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

    // USED only for TESTING, with the assumption that ONLY the local player has the Anatomy component attached.
    @In
    private EntityRef playerRef;

    @Override
    public void initialise() {
        // The following won't work as the entities have not been initialized yet.
        for (EntityRef entity : entityManager.getEntitiesWith(AnatomyComponent.class)) {
            AnatomyComponent comp = entity.getComponent(AnatomyComponent.class);

            for (int i = 0; i < comp.partNames.size(); i++) {
                AnatomyPart part = new AnatomyPart();
                part.name = comp.partNames.get(i);
                part.maxHealth = comp.maxHealths.get(i);
                part.health = part.maxHealth;
                part.healthRegen = comp.healthRegens.get(i);

                comp.parts.add(part);
            }

            playerRef = entity;
            //delayManager.addPeriodicAction(entity, "Anatomy:RegenHealth", part.timeBetweenHealthRegenTick, part.timeBetweenHealthRegenTick);
        }
    }

    @ReceiveEvent
    public void onHealthRegenDelayCompletion(PeriodicActionTriggeredEvent event, EntityRef entityRef, AnatomyPart part) {
        if (event.getActionId().equals("Anatomy:RegenHealth")) {
            heal(part, part.healthRegen);
        }
    }

    @ReceiveEvent(components = {HealthComponent.class, AnatomyComponent.class})
    public void onDamage(DoAnatomyDamageEvent event, EntityRef entity, AnatomyComponent comp) {
        for (AnatomyPart part : comp.parts) {
            if (part.isAlive && part.name.equalsIgnoreCase(event.getTargetPartName())) {
                part.health -= event.getAmount();

                logger.info(part.name + " has taken " + event.getAmount() + " points of damage!");

                if (part.health <= 0) {
                    // Send event indicating that this anatomy part is dead.
                    entity.send(new DoAnatomyDeadEvent(part, event.getInstigator()));
                }
            }
        }
    }

    @ReceiveEvent(components = {HealthComponent.class, AnatomyComponent.class})
    public void onHeal(DoAnatomyHealEvent event, EntityRef entity, AnatomyComponent comp) {
        heal(comp, event.getAmount(), event.getTargetPartName());
    }

    @ReceiveEvent
    public void onDead(DoAnatomyDeadEvent event, EntityRef entity) {
        event.getPart().health = 0;
        event.getPart().isAlive = false;
        logger.info(event.getPart().name + " has been destroyed!");
    }

    @ReceiveEvent(components = {HealthComponent.class, AnatomyComponent.class})
    public void onRevive(DoAnatomyReviveEvent event, EntityRef entity, AnatomyComponent comp) {
        for (AnatomyPart part : comp.parts) {
            if (!part.isAlive && part.name.equalsIgnoreCase(event.getTargetPartName())) {
                part.isAlive = true;

                heal(part, event.getAmount());
                logger.info(part.name + " has been revived with " + part.health + " health!");
            }
        }
    }

    private void heal(AnatomyComponent comp, int amount, String partName) {
        for (AnatomyPart part : comp.parts) {
            if (part.isAlive && part.name.equalsIgnoreCase(partName) && !part.isHealthFull()) {
                part.health += amount;

                logger.info(part.name + " has recovered " + amount + " points of health!");

                if (part.health >= part.maxHealth) {
                    part.health = part.maxHealth;

                    logger.info(part.name + " is at max health!");
                }
            }
        }
    }

    private void heal(AnatomyPart part, int amount) {
        if (part.isAlive && !part.isHealthFull()) {
            part.health += amount;

            logger.info(part.name + " has recovered " + amount + " points of health!");

            if (part.health >= part.maxHealth) {
                part.health = part.maxHealth;

                logger.info(part.name + " is at max health!");
            }
        }
    }

    // The following commands are used for debugging.

    // For this current revision, this command needs to be run before the Anatomy system is up and running properly.
    @Command(shortDescription = "Create all the Anatomy parts", runOnServer = true)
    public void createAnatomyParts() {
        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyComponent.class)) {

            AnatomyComponent comp = clientEntity.getComponent(AnatomyComponent.class);

            // Only create these parts if they haven't already been created.
            if (comp.parts.size() > 0) {
                return;
            }

            for (int i = 0; i < comp.partNames.size(); i++) {
                logger.info("Creating part #" + i);

                AnatomyPart part = new AnatomyPart();
                part.name = comp.partNames.get(i);
                part.maxHealth = comp.maxHealths.get(i);
                part.health = part.maxHealth;
                part.healthRegen = comp.healthRegens.get(i);

                comp.parts.add(part);
            }

            playerRef = clientEntity;
            logger.info("The parts of entity: " + clientEntity.toString() + " have been created.");
        }
    }

    @Command(shortDescription = "Damage Anatomy part for amount", runOnServer = true)
    public void dmgAnatomyPart(@CommandParam("name") String partName, @CommandParam("amount") int amount) {
        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyComponent.class)) {
            clientEntity.send(new DoAnatomyDamageEvent(amount, partName, null, null, null));
        }
    }

    @Command(shortDescription = "Damage ALL Anatomy parts for amount", runOnServer = true)
    public void dmgAnatomyPartAll(@CommandParam("amount") int amount) {
        AnatomyComponent comp = playerRef.getComponent(AnatomyComponent.class);

        for (AnatomyPart part : comp.parts) {
            if (part.isAlive) {
                part.health -= amount;

                logger.info(part.name + " has taken " + amount + " points of damage!");

                if (part.health <= 0) {
                    part.health = 0;
                    part.isAlive = false;
                    logger.info(part.name + " has been destroyed!");
                }
            }
        }
    }

    @Command(shortDescription = "Heal Anatomy part for amount", runOnServer = true)
    public void healAnatomyPart(@CommandParam("name") String partName, @CommandParam("amount") int amount) {

        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyComponent.class)) {
            clientEntity.send(new DoAnatomyHealEvent(amount, partName, null));
        }
    }

    @Command(shortDescription = "Heal ALL Anatomy parts for amount", runOnServer = true)
    public void healAnatomyPartAll(@CommandParam("amount") int amount) {
        AnatomyComponent comp = playerRef.getComponent(AnatomyComponent.class);

        for (AnatomyPart part : comp.parts) {
            if (part.isAlive && !part.isHealthFull()) {
                part.health += amount;

                logger.info(part.name + " has recovered " + amount + " points of health!");

                if (part.health >= part.maxHealth) {
                    part.health = part.maxHealth;

                    logger.info(part.name + " is at max health!");
                }
            }
        }
    }

    @Command(shortDescription = "Revive Anatomy component for amount", runOnServer = true)
    public void reviveAnatomyPart(@CommandParam("name") String partName, @CommandParam("amount") int amount) {
        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyComponent.class)) {
            clientEntity.send(new DoAnatomyReviveEvent(amount, partName, null));
        }
    }

    @Command(shortDescription = "Shows all Anatomy parts' health", runOnServer = true)
    public void getAnatomyPartHealth() {
        //AnatomyComponent comp = playerRef.getComponent(AnatomyComponent.class);

        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyComponent.class)) {
            logger.info(clientEntity.toString() + " has the following anatomy health values:");
            AnatomyComponent comp = clientEntity.getComponent(AnatomyComponent.class);

            for (AnatomyPart part : comp.parts) {
                logger.info(part.name + " has health: " + part.health + "/" + part.maxHealth);
            }
        }
    }
}
