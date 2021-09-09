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
package org.terasology.anatomy.AnatomySkeleton;

import org.terasology.anatomy.AnatomySkeleton.component.InjuredBoneComponent;
import org.terasology.anatomy.AnatomySkeleton.event.BoneHealthChangedEvent;
import org.terasology.anatomy.component.AnatomyComponent;
import org.terasology.anatomy.component.PartHealthDetails;
import org.terasology.anatomy.event.AnatomyPartImpactedEvent;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.math.TeraMath;

/**
 * This authority system manages the Skeletal system health updates.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class SkeletalHealthSystem extends BaseComponentSystem {
    @In
    private Time time;

    @In
    private EntityManager entityManager;

    @In
    private DelayManager delayManager;

    private float bluntDamageMultiplier = 1.5f;

    private String SKELETAL_REGEN_PREFIX = "Skeletal:Regen:";
    private String BONE_CHARACTERISTIC = "bone";

    @ReceiveEvent
    public void onRegen(DelayedActionTriggeredEvent event, EntityRef entityRef, InjuredBoneComponent injuredBoneComponent) {
        if (event.getActionId().startsWith(SKELETAL_REGEN_PREFIX)) {
            String partID = event.getActionId().substring(SKELETAL_REGEN_PREFIX.length());
            PartHealthDetails partDetails = injuredBoneComponent.partHealths.get(partID);
            if (partDetails.health >= 0 && partDetails.health != partDetails.maxHealth && partDetails.regenRate != 0) {
                int healAmount = 0;
                healAmount = regenerateHealth(partDetails, healAmount);
                partDetails.health += healAmount;
                partDetails.health = TeraMath.clamp(partDetails.health, 0, partDetails.maxHealth);
                entityRef.saveComponent(injuredBoneComponent);
                entityRef.send(new BoneHealthChangedEvent(partID));
            }
            delayManager.addDelayedAction(entityRef, SKELETAL_REGEN_PREFIX + partID, (long) (1000 / partDetails.regenRate));
        }
    }

    @ReceiveEvent
    public void onBoneDamage(AnatomyPartImpactedEvent event, EntityRef entityRef, AnatomyComponent anatomyComponent) {
        if (anatomyComponent.parts.get(event.getTargetPart().id).characteristics.contains(BONE_CHARACTERISTIC)) {
            InjuredBoneComponent injuredBoneComponent = entityRef.getComponent(InjuredBoneComponent.class);
            if (injuredBoneComponent == null) {
                injuredBoneComponent = new InjuredBoneComponent();
                entityRef.addComponent(injuredBoneComponent);
            }
            PartHealthDetails partHealthDetails = injuredBoneComponent.partHealths.get(event.getTargetPart().id);
            if (partHealthDetails == null) {
                partHealthDetails = new PartHealthDetails();
                injuredBoneComponent.partHealths.put(event.getTargetPart().id, partHealthDetails);
                // Part has been injured for the first time, so add delayed regen event.
                delayManager.addDelayedAction(entityRef, SKELETAL_REGEN_PREFIX + event.getTargetPart().id, (long) (1000 / partHealthDetails.regenRate));
            }
            int damageAmount = event.getAmount();
            if (event.getDamageType().getName().equals("Equipment:bluntDamage")) {
                damageAmount *= bluntDamageMultiplier;
            }
            partHealthDetails.health -= damageAmount;
            partHealthDetails.health = TeraMath.clamp(partHealthDetails.health, 0, partHealthDetails.maxHealth);
            partHealthDetails.nextRegenTick = time.getGameTimeInMs() + TeraMath.floorToInt(partHealthDetails.waitBeforeRegen * 1000);
            entityRef.saveComponent(injuredBoneComponent);
            entityRef.send(new BoneHealthChangedEvent(event.getTargetPart().id));
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
