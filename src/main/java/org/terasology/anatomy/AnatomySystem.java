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
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.health.*;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.registry.In;

/**
 * Created by Xt on 5/20/16.
 */

/**
 * Provides a basic system for managing an entity's anatomy.
 */
@RegisterSystem
public class AnatomySystem extends BaseComponentSystem implements UpdateSubscriberSystem
{
    private static final Logger logger = LoggerFactory.getLogger(AnatomySystem.class);

    @In
    private EntityManager entityManager;

    @In
    private Time time;

    @Override
    public void update(float delta)
    {
        long gameTime = time.getGameTimeInMs();

        for (EntityRef entity : entityManager.getEntitiesWith(AnatomyPartComponent.class))
        {
            AnatomyPartComponent part = entity.getComponent(AnatomyPartComponent.class);

            // Check to see if health should be regenerated.
            if (!part.isHealthFull())
            {
                if (gameTime >= part.nextHealthRegenTick)
                {
                    part.heal(part.healthRegen); // Temporary. Replace with event.
                    part.nextHealthRegenTick = gameTime + part.timeBetweenHealthRegenTick; // 25500 is temporary. Replace with variable in Component.
                }
            }

            // Check to see if energy should be regenerated.
            if (!part.isEnergyFull())
            {
                if (gameTime >= part.nextHealthRegenTick)
                {
                    part.recover(part.energyRegen); // Temporary. Replace with event. Replace with variable in Component.
                    part.nextEnergyRegenTick = gameTime + part.timeBetweenEnergyRegenTick;
                }
            }
        }
    }

    @ReceiveEvent(components = {HealthComponent.class, AnatomyPartComponent.class})
    public void onDamage(DoAnatomyDamageEvent event, EntityRef entity, AnatomyPartComponent part)
    {
        part.damage(event.getAmount());

        logger.info(part.name + " has taken " + event.getAmount() + " points of damage!\n");
        // Do something
    }

    @ReceiveEvent(components = {HealthComponent.class, AnatomyPartComponent.class})
    public void onHeal(DoAnatomyHealEvent event, EntityRef entity, AnatomyPartComponent part)
    {
        part.heal(event.getAmount());

        logger.info(part.name + " has recovered " + event.getAmount() + " points of health!\n");
        // Do something
    }

    @Command(shortDescription = "Damage Anatomy component for amount", runOnServer = true)
    public void dmgAnatomyPart(@CommandParam("amount") int amount)
    {
        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyPartComponent.class))
        {
            clientEntity.send(new DoAnatomyDamageEvent(amount));
        }
    }

    @Command(shortDescription = "Heal Anatomy component for amount", runOnServer = true)
    public void healAnatomyPart(@CommandParam("amount") int amount)
    {
        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyPartComponent.class))
        {
            clientEntity.send(new DoAnatomyHealEvent(amount));
        }
    }

    @Command(shortDescription = "Shows Anatomy component health", runOnServer = true)
    public void getAnatomyPartHealth()
    {
        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyPartComponent.class))
        {
            AnatomyPartComponent part = clientEntity.getComponent(AnatomyPartComponent.class);
            logger.info(part.name + " has " + part.health + " points of health!\n");
        }
    }
}
