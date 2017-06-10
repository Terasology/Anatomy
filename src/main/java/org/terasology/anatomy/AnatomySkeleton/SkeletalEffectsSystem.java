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

import com.google.common.collect.Lists;
import org.terasology.anatomy.AnatomySkeleton.component.InjuredBoneComponent;
import org.terasology.anatomy.component.AnatomyComponent;
import org.terasology.anatomy.component.AnatomyPartTag;
import org.terasology.anatomy.component.PartEffectOutcome;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.GetMaxSpeedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This system manages the outcomes of the BrokenBone effect on various body parts.
 */
@RegisterSystem
public class SkeletalEffectsSystem extends BaseComponentSystem {
    private Map<String, Float> severityPercentageEffectMap = new HashMap<>();

    private String MOBILITY_EFFECT = "mobility";

    @Override
    public void initialise() {
        severityPercentageEffectMap.put("1", 0.9f);
        severityPercentageEffectMap.put("2", 0.7f);
        severityPercentageEffectMap.put("3", 0.5f);
    }

    @ReceiveEvent
    public void modifySpeed(GetMaxSpeedEvent event, EntityRef entityRef, AnatomyComponent anatomyComponent, InjuredBoneComponent injuredBoneComponent) {
        // Loop over each severity of the InjuredBone effect.
        for (Map.Entry<String, List<String>> injuredBoneEntry : injuredBoneComponent.parts.entrySet()) {
            // Loop over each part corresponding to a particular severity.
            for (String injuredBonePart : injuredBoneEntry.getValue()) {
                //Get the outcome corresponding to the part and its effect severity.
                //TODO: Temporary for now (since only leg effects are defined), until effects is sorted out.
                if (injuredBonePart.contains("Leg")) {
                    if (anatomyComponent.parts.get(injuredBonePart).abilities.contains(MOBILITY_EFFECT)) {
                        event.multiply(severityPercentageEffectMap.get(injuredBoneEntry.getKey()));
                    }
                }
            }
        }
    }
}
