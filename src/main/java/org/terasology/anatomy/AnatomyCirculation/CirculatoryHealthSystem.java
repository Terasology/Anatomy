/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.anatomy.AnatomyCirculation;

import org.terasology.anatomy.AnatomyCirculation.component.InjuredCirculatoryComponent;
import org.terasology.anatomy.AnatomyCirculation.event.PartCirculatoryHealthChangedEvent;
import org.terasology.anatomy.component.AnatomyComponent;
import org.terasology.anatomy.component.PartHealthDetails;
import org.terasology.anatomy.event.AnatomyPartImpactedEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.math.TeraMath;
import org.terasology.registry.In;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class CirculatoryHealthSystem extends BaseComponentSystem {
    @In
    private org.terasology.engine.Time time;

    @In
    private EntityManager entityManager;

    @In
    private DelayManager delayManager;

    private float bluntDamageMultiplier = 0.5f;
    private float pierceDamageMultiplier = 1.5f;

    private String CIRCULATORY_REGEN_PREFIX = "Circulatory:Regen:";
    private String CIRCULATORY_CHARACTERISTIC = "blood";

    @ReceiveEvent
    public void onRegen(DelayedActionTriggeredEvent event, EntityRef entityRef, InjuredCirculatoryComponent injuredCirculatoryComponent) {
        if (event.getActionId().startsWith(CIRCULATORY_REGEN_PREFIX)) {
            String partID = event.getActionId().substring(CIRCULATORY_REGEN_PREFIX.length());
            PartHealthDetails partDetails = injuredCirculatoryComponent.partHealths.get(partID);
            if (partDetails.health >= 0 && partDetails.health != partDetails.maxHealth && partDetails.regenRate != 0) {
                int healAmount = 0;
                healAmount = regenerateHealth(partDetails, healAmount);
                partDetails.health += healAmount;
                entityRef.saveComponent(injuredCirculatoryComponent);
                entityRef.send(new PartCirculatoryHealthChangedEvent(partID));
            }
            delayManager.addDelayedAction(entityRef, CIRCULATORY_REGEN_PREFIX + partID, (long) (1000 / partDetails.regenRate));
        }
    }

    @ReceiveEvent
    public void onCirculatoryDamage(AnatomyPartImpactedEvent event, EntityRef entityRef, AnatomyComponent anatomyComponent) {
        if (anatomyComponent.parts.get(event.getTargetPart().id).characteristics.contains(CIRCULATORY_CHARACTERISTIC)) {
            InjuredCirculatoryComponent injuredCirculatoryComponent = entityRef.getComponent(InjuredCirculatoryComponent.class);
            if (injuredCirculatoryComponent == null) {
                injuredCirculatoryComponent = new InjuredCirculatoryComponent();
                entityRef.addComponent(injuredCirculatoryComponent);
            }
            PartHealthDetails partHealthDetails = injuredCirculatoryComponent.partHealths.get(event.getTargetPart().id);
            if (partHealthDetails == null) {
                partHealthDetails = new PartHealthDetails();
                injuredCirculatoryComponent.partHealths.put(event.getTargetPart().id, partHealthDetails);
                // Part has been injured for the first time, so add delayed regen event.
                delayManager.addDelayedAction(entityRef, CIRCULATORY_REGEN_PREFIX + event.getTargetPart().id, (long) (1000 / partHealthDetails.regenRate));
            }
            int damageAmount = event.getAmount();
            if (event.getDamageType().getName().equals("Equipment:pierceDamage")) {
                damageAmount *= pierceDamageMultiplier;
            }
            if (event.getDamageType().getName().equals("Equipment:bluntDamage")) {
                damageAmount *= bluntDamageMultiplier;
            }
            partHealthDetails.health -= damageAmount;
            if (partHealthDetails.health < 0) {
                partHealthDetails.health = 0;
            }
            partHealthDetails.nextRegenTick = time.getGameTimeInMs() + TeraMath.floorToInt(partHealthDetails.waitBeforeRegen * 1000);
            entityRef.saveComponent(injuredCirculatoryComponent);
            entityRef.send(new PartCirculatoryHealthChangedEvent(event.getTargetPart().id));
        }
    }

    private int regenerateHealth(PartHealthDetails partDetails, int healAmount) {
        int newHeal = healAmount;
        while (time.getGameTimeInMs() >= partDetails.nextRegenTick) {
            newHeal++;
            partDetails.nextRegenTick = partDetails.nextRegenTick + (long) (1000 / partDetails.regenRate);
        }
        return newHeal;
    }
}