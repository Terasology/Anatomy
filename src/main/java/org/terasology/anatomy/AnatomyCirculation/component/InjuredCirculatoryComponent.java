// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.anatomy.AnatomyCirculation.component;

import com.google.common.collect.Lists;
import org.terasology.anatomy.component.PartHealthDetails;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InjuredCirculatoryComponent implements Component<InjuredCirculatoryComponent> {
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

    @Replicate
    public int bloodLevel = 100;

    @Replicate
    public int maxBloodLevel = 100;

    @Replicate
    public float baseBloodRegenRate = 1.0f;

    @Replicate
    public float bloodRegenRate = 1.0f;

    public long nextRegenTick;

    @Override
    public void copy(InjuredCirculatoryComponent other) {
        this.partHealths.clear();
        other.partHealths.forEach((k, v) -> this.partHealths.put(k, v.copy()));
        this.parts.clear();
        other.parts.forEach((k, v) -> this.parts.put(k, Lists.newArrayList(v)));
        this.bloodLevel = other.bloodLevel;
        this.maxBloodLevel = other.maxBloodLevel;
        this.baseBloodRegenRate = other.baseBloodRegenRate;
        this.bloodRegenRate = other.bloodRegenRate;
        this.nextRegenTick = other.nextRegenTick;

    }
}
