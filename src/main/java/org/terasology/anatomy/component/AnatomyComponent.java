// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.anatomy.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

import java.util.HashMap;
import java.util.Map;

/**
 * This component defines the anatomical structure and abilities of a creature.
 */
public class AnatomyComponent implements Component {
    @Replicate
    public Map<String, AnatomyPartTag> parts = new HashMap<>();
}
