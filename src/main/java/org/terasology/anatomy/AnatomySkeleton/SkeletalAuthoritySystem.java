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

import org.terasology.anatomy.AnatomySkeleton.component.BoneComponent;
import org.terasology.anatomy.AnatomySkeleton.event.BoneHealthChangedEvent;
import org.terasology.anatomy.component.AnatomyComponent;
import org.terasology.anatomy.component.PartSkeletalDetails;
import org.terasology.anatomy.event.AnatomyPartImpactedEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.math.TeraMath;
import org.terasology.registry.In;
import org.terasology.rendering.assets.skeletalmesh.Bone;

import java.util.Map;

/**
 * This authority system manages the Skeletal system health updates.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class SkeletalAuthoritySystem extends BaseComponentSystem {
    @In
    private org.terasology.engine.Time time;

    @In
    private EntityManager entityManager;

    @In
    private DelayManager delayManager;

    private float bluntDamageMultiplier = 1.5f;

    private String SKELETAL_REGEN_PREFIX = "Skeletal:Regen:";

    @Override
    public void initialise() {
        for (EntityRef entity : entityManager.getEntitiesWith(BoneComponent.class)) {
            for (Map.Entry<String, PartSkeletalDetails> partSkeletalDetailsEntry : entity.getComponent(BoneComponent.class).parts.entrySet()) {
                delayManager.addDelayedAction(entity, SKELETAL_REGEN_PREFIX + partSkeletalDetailsEntry.getKey(), (long) (1000 / partSkeletalDetailsEntry.getValue().regenRate));
            }
        }
    }

    @ReceiveEvent
    public void onRegen(DelayedActionTriggeredEvent event, EntityRef entityRef) {
        BoneComponent boneComponent = entityRef.getComponent(BoneComponent.class);
        String partID = event.getActionId().substring(SKELETAL_REGEN_PREFIX.length());
        PartSkeletalDetails partDetails = boneComponent.parts.get(partID);
        if (partDetails.health >= 0 && partDetails.health != partDetails.maxHealth && partDetails.regenRate != 0) {
            int healAmount = 0;
            healAmount = regenerateHealth(partDetails, healAmount);
            partDetails.health += healAmount;
            entityRef.saveComponent(boneComponent);
            entityRef.send(new BoneHealthChangedEvent(partID));
        }
        delayManager.addDelayedAction(entityRef, SKELETAL_REGEN_PREFIX + partID, (long) (1000 / partDetails.regenRate));
    }

    @ReceiveEvent
    public void onBoneDamage(AnatomyPartImpactedEvent event, EntityRef entityRef, AnatomyComponent anatomyComponent, BoneComponent boneComponent) {
        PartSkeletalDetails partDetails = boneComponent.parts.get(event.getTargetPart().id);
        // Check if the part contains bone.
        int damageAmount = event.getAmount();
        if (partDetails != null) {
            if (event.getDamageType().getName().equals("Equipment:bluntDamage")) {
                damageAmount *= bluntDamageMultiplier;
            }
            partDetails.health -= damageAmount;
            if (partDetails.health < 0) {
                partDetails.health = 0;
            }
            partDetails.nextRegenTick = time.getGameTimeInMs() + TeraMath.floorToInt(partDetails.waitBeforeRegen * 1000);
            entityRef.saveComponent(boneComponent);
            entityRef.send(new BoneHealthChangedEvent(event.getTargetPart().id));
        }
    }

    private int regenerateHealth(PartSkeletalDetails partDetails, int healAmount) {
        int newHeal = healAmount;
        while (time.getGameTimeInMs() >= partDetails.nextRegenTick) {
            newHeal++;
            partDetails.nextRegenTick = partDetails.nextRegenTick + (long) (1000 / partDetails.regenRate);
        }
        return newHeal;
    }
}
