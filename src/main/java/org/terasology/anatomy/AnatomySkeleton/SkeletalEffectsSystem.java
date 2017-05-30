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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.anatomy.AnatomySkeleton.component.BoneComponent;
import org.terasology.anatomy.AnatomySkeleton.component.BrokenBoneComponent;
import org.terasology.anatomy.component.PartEffectOutcome;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.GetMaxSpeedEvent;

import java.util.List;
import java.util.Map;

/**
 * This system manages the outcomes of the BrokenBone effect on various body parts.
 */
@RegisterSystem
public class SkeletalEffectsSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(SkeletalEffectsSystem.class);

    @ReceiveEvent
    public void modifySpeed(GetMaxSpeedEvent event, EntityRef entityRef, BoneComponent boneComponent, BrokenBoneComponent brokenBoneComponent) {
        // Loop over each severity of the BrokenBone effect.
        for (Map.Entry<String, List<String>> brokenBoneEntry : brokenBoneComponent.parts.entrySet()) {
            // Loop over each part corresponding to a particular severity.
            for (String brokenBonePart : brokenBoneEntry.getValue()) {
                PartEffectOutcome partEffectOutcome = boneComponent.partEffectOutcomes.get(brokenBonePart + ":" + brokenBoneEntry.getKey());
                if (partEffectOutcome.outcome.equals("modifySpeed")) {
                    event.multiply(partEffectOutcome.magnitude);
                }
            }
        }
    }
}
