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
 * This system manages the outcomes of the InjuredBone effect on various body parts.
 */
@RegisterSystem
public class SkeletalEffectsSystem extends BaseComponentSystem {
    /**
     * Stores the health thresholds at which different severities of the effect occur.
     */
    private Map<String, Float> severityPercentageEffectMap = new HashMap<>();

    private String MOBILITY_EFFECT = "mobility";

    @Override
    public void initialise() {
        severityPercentageEffectMap.put("1", 0.8f);
        severityPercentageEffectMap.put("2", 0.5f);
        severityPercentageEffectMap.put("3", 0.2f);
    }

    /**
     * Modifies the max speed based on skeletal effects.
     */
    @ReceiveEvent
    public void modifySpeed(GetMaxSpeedEvent event, EntityRef entityRef, AnatomyComponent anatomyComponent, InjuredBoneComponent injuredBoneComponent) {
        List<String> contributingParts = getContributingParts(anatomyComponent, MOBILITY_EFFECT);
        float multiplier = getMultiplier(injuredBoneComponent, contributingParts);
        event.multiply(multiplier);
    }

    /**
     * Modifies the jump speed/height based on skeletal effects.
     */
    @ReceiveEvent
    public void modifyJumpSpeed(AffectJumpForceEvent event, EntityRef entityRef, AnatomyComponent anatomyComponent, InjuredBoneComponent injuredBoneComponent) {
        List<String> contributingParts = getContributingParts(anatomyComponent, MOBILITY_EFFECT);
        float multiplier = getMultiplier(injuredBoneComponent, contributingParts);
        event.multiply(multiplier);
    }

    /**
     * Returns the list of anatomy parts contributing to the specific characteristic.
     *
     * @param effectID The characteristic to get contributing parts for.
     * @return List of anatomy parts
     */
    private List<String> getContributingParts(AnatomyComponent anatomyComponent, String effectID) {
        List<String> contributingParts = Lists.newArrayList();
        for (Map.Entry<String, AnatomyPartTag> anatomyPartTagEntry : anatomyComponent.parts.entrySet()) {
            if (anatomyPartTagEntry.getValue().abilities.contains(effectID)) {
                contributingParts.add(anatomyPartTagEntry.getKey());
            }
        }
        return contributingParts;
    }

    /**
     * Returns the multiplier to be applied based on the severity of effects on the contributing parts.
     *
     * @param contributingParts List of parts contributing to the ability for which the multiplier is to be calculated.
     * @return Multiplier for the ability.
     */
    private float getMultiplier(InjuredBoneComponent injuredBoneComponent, List<String> contributingParts) {
        int numContributingParts = contributingParts.size();
        int numAffectedContributingParts = 0;
        float multiplier = 0f;
        for (Map.Entry<String, List<String>> injuredBoneEntry : injuredBoneComponent.parts.entrySet()) {
            for (String injuredBonePart : injuredBoneEntry.getValue()) {
                if (contributingParts.contains(injuredBonePart)) {
                    numAffectedContributingParts += 1;
                    // Add contribution of injured parts based on severity
                    multiplier += severityPercentageEffectMap.get(injuredBoneEntry.getKey()) / numContributingParts;
                }
            }
        }
        // Add contributions for parts which aren't injured
        multiplier += 1 - ((float) numAffectedContributingParts / numContributingParts);
        return multiplier;
    }
}
