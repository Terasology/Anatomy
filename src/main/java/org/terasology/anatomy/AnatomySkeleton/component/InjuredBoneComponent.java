// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.anatomy.AnatomySkeleton.component;

import com.google.common.collect.Lists;
import org.terasology.anatomy.component.PartHealthDetails;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This component contains injured skeletal system details.
 */
public class InjuredBoneComponent implements Component<InjuredBoneComponent> {
    /**
     * Maps each part to its health details.
     */
    @Replicate
    public Map<String, PartHealthDetails> partHealths = new HashMap<>();

    /**
     * Maps severity to the list of parts affected.
     */
    @Replicate
    public Map<String, List<String>> parts = new HashMap<>();

    @Override
    public void copyFrom(InjuredBoneComponent other) {
        this.partHealths.clear();
        other.partHealths.forEach((k, v) -> this.partHealths.put(k, v.copy()));
        this.parts.clear();
        other.parts.forEach((k, v) -> this.parts.put(k, Lists.newArrayList(v)));
    }
}
