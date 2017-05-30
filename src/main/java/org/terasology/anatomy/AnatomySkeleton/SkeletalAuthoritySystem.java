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
import org.terasology.math.TeraMath;
import org.terasology.registry.In;

import java.util.Map;

/**
 * This authority system manages the Skeletal system health updates.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class SkeletalAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    @In
    private org.terasology.engine.Time time;

    @In
    private EntityManager entityManager;

    private float bluntDamageMultiplier = 1.5f;

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(BoneComponent.class)) {
            BoneComponent boneComponent = entity.getComponent(BoneComponent.class);
            for (Map.Entry<String, PartSkeletalDetails> partEntry : boneComponent.parts.entrySet()) {
                PartSkeletalDetails partDetails = partEntry.getValue();
                if (partDetails.health < 0) {
                    continue;
                }

                if (partDetails.health == partDetails.maxHealth || partDetails.regenRate == 0) {
                    continue;
                }

                int healAmount = 0;
                healAmount = regenerateHealth(partDetails, healAmount);
                partDetails.health += healAmount;
                entity.saveComponent(boneComponent);
                entity.send(new BoneHealthChangedEvent(partEntry.getKey()));
            }
        }
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
