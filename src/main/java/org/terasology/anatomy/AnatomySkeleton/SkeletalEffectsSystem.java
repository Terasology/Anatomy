// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.anatomy.AnatomySkeleton;

import com.google.common.collect.Lists;
import org.terasology.anatomy.AnatomySkeleton.component.InjuredBoneComponent;
import org.terasology.anatomy.component.AnatomyComponent;
import org.terasology.anatomy.component.AnatomyPartTag;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.AffectJumpForceEvent;
import org.terasology.engine.logic.characters.GetMaxSpeedEvent;

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
    private final Map<String, Float> severityPercentageEffectMap = new HashMap<>();

    private final String MOBILITY_EFFECT = "mobility";

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
    public void modifySpeed(GetMaxSpeedEvent event, EntityRef entityRef, AnatomyComponent anatomyComponent,
                            InjuredBoneComponent injuredBoneComponent) {
        List<String> contributingParts = getContributingParts(anatomyComponent, MOBILITY_EFFECT);
        float multiplier = getMultiplier(injuredBoneComponent, contributingParts);
        event.multiply(multiplier);
    }

    /**
     * Modifies the jump speed/height based on skeletal effects.
     */
    @ReceiveEvent
    public void modifyJumpSpeed(AffectJumpForceEvent event, EntityRef entityRef, AnatomyComponent anatomyComponent,
                                InjuredBoneComponent injuredBoneComponent) {
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
     * @param contributingParts List of parts contributing to the ability for which the multiplier is to be
     *         calculated.
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
