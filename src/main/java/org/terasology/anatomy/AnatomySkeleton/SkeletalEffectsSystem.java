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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.anatomy.AnatomySkeleton.component.InjuredBoneComponent;
import org.terasology.anatomy.component.AnatomyComponent;
import org.terasology.anatomy.component.AnatomyPartTag;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.AffectJumpForceEvent;
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
        severityPercentageEffectMap.put("1", 0.8f);
        severityPercentageEffectMap.put("2", 0.5f);
        severityPercentageEffectMap.put("3", 0.2f);
    }

    @ReceiveEvent
    public void modifySpeed(GetMaxSpeedEvent event, EntityRef entityRef, AnatomyComponent anatomyComponent, InjuredBoneComponent injuredBoneComponent) {
        List<String> contributingParts = getContributingParts(anatomyComponent, MOBILITY_EFFECT);
        float multiplier = getMultiplier(injuredBoneComponent, contributingParts);
        event.multiply(multiplier);
    }

    @ReceiveEvent
    public void modifyJumpSpeed(AffectJumpForceEvent event, EntityRef entityRef, AnatomyComponent anatomyComponent, InjuredBoneComponent injuredBoneComponent) {
        List<String> contributingParts = getContributingParts(anatomyComponent, MOBILITY_EFFECT);
        float multiplier = getMultiplier(injuredBoneComponent, contributingParts);
        event.multiply(multiplier);
    }

    private List<String> getContributingParts(AnatomyComponent anatomyComponent, String effectID) {
        List<String> contributingParts = Lists.newArrayList();
        for (Map.Entry<String, AnatomyPartTag> anatomyPartTagEntry : anatomyComponent.parts.entrySet()) {
            if (anatomyPartTagEntry.getValue().abilities.contains(effectID)) {
                contributingParts.add(anatomyPartTagEntry.getKey());
            }
        }
        return contributingParts;
    }

    private float getMultiplier(InjuredBoneComponent injuredBoneComponent, List<String> contributingParts) {
        int numContributingParts = contributingParts.size();
        int numAffectedContributingParts = 0;
        float multiplier = 0f;
        for (Map.Entry<String, List<String>> injuredBoneEntry : injuredBoneComponent.parts.entrySet()) {
            for (String injuredBonePart : injuredBoneEntry.getValue()) {
                if (contributingParts.contains(injuredBonePart)) {
                    numAffectedContributingParts += 1;
                    multiplier += severityPercentageEffectMap.get(injuredBoneEntry.getKey()) / numContributingParts;
                }
            }
        }
        multiplier += 1 - ((float)numAffectedContributingParts / numContributingParts);
        return multiplier;
    }
}
