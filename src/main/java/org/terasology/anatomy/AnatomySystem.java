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
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.health.*;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.registry.In;

/**
 * Provides a basic system for managing an entity's anatomy.
 */
@RegisterSystem
public class AnatomySystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(AnatomySystem.class);

    @In
    private EntityManager entityManager;

    @In
    DelayManager delayManager;

    @Override
    public void initialise() {
        for (EntityRef entity : entityManager.getEntitiesWith(AnatomyPartComponent.class)) {
            AnatomyPartComponent part = entity.getComponent(AnatomyPartComponent.class);
            delayManager.addPeriodicAction(entity, "Anatomy:RegenHealth", part.timeBetweenHealthRegenTick, part.timeBetweenHealthRegenTick);
        }
    }

    @ReceiveEvent
    public void onHealthRegenDelayCompletion(PeriodicActionTriggeredEvent event, EntityRef entityRef, AnatomyPartComponent part) {
        if (event.getActionId().equals("Anatomy:RegenHealth")) {
            heal(part, part.healthRegen);
        }
    }

    @ReceiveEvent(components = {HealthComponent.class, AnatomyPartComponent.class})
    public void onDamage(DoAnatomyDamageEvent event, EntityRef entity, AnatomyPartComponent part) {
        if (part.isAlive) {
            part.health -= event.getAmount();

            logger.info(part.name + " has taken " + event.getAmount() + " points of damage!\n");

            if (part.health <= 0) {
                part.health = 0;
                part.isAlive = false;
                logger.info(part.name + " has been destroyed!\n");
            }
        }
    }

    @ReceiveEvent(components = {HealthComponent.class, AnatomyPartComponent.class})
    public void onHeal(DoAnatomyHealEvent event, EntityRef entity, AnatomyPartComponent part) {
        heal(part, event.getAmount());
    }

    @ReceiveEvent(components = {HealthComponent.class, AnatomyPartComponent.class})
    public void onRevive(DoAnatomyReviveEvent event, EntityRef entity, AnatomyPartComponent part) {
        if (!part.isAlive) {
            part.isAlive = true;

            heal(part, event.getAmount());
            logger.info(part.name + " has been revived with " + part.health + " health!\n");
        }
    }

    private void heal(AnatomyPartComponent part, int amount) {
        if (part.isAlive & !part.isHealthFull()) {
            part.health += amount;

            logger.info(part.name + " has recovered " + amount + " points of health!\n");

            if (part.health >= part.maxHealth) {
                part.health = part.maxHealth;

                logger.info(part.name + " is at max health!\n");
            }
        }
    }

    @Command(shortDescription = "Damage Anatomy component for amount", runOnServer = true)
    public void dmgAnatomyPart(@CommandParam("amount") int amount) {
        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyPartComponent.class)) {
            clientEntity.send(new DoAnatomyDamageEvent(amount));
        }
    }

    @Command(shortDescription = "Heal Anatomy component for amount", runOnServer = true)
    public void healAnatomyPart(@CommandParam("amount") int amount) {
        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyPartComponent.class)) {
            clientEntity.send(new DoAnatomyHealEvent(amount));
        }
    }

    @Command(shortDescription = "Revive Anatomy component for amount", runOnServer = true)
    public void reviveAnatomyPart(@CommandParam("amount") int amount) {
        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyPartComponent.class)) {
            clientEntity.send(new DoAnatomyReviveEvent(amount));
        }
    }

    @Command(shortDescription = "Shows Anatomy component health", runOnServer = true)
    public void getAnatomyPartHealth() {
        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyPartComponent.class)) {
            AnatomyPartComponent part = clientEntity.getComponent(AnatomyPartComponent.class);
            logger.info(part.name + " has health: " + part.health + "/" + part.maxHealth + "\n");
        }
    }
}
