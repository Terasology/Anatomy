// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.anatomy.AnatomySkeleton.component;

import org.terasology.anatomy.component.PartHealthDetails;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This component contains injured skeletal system details.
 */
public class InjuredBoneComponent implements Component {
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
}
