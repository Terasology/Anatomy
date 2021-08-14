// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.anatomy.component;

import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * This component defines the anatomical structure and abilities of a creature.
 */
public class AnatomyComponent implements Component<AnatomyComponent> {
    @Replicate
    public Map<String, AnatomyPartTag> parts = new HashMap<>();

    @Override
    public void copyFrom(AnatomyComponent other) {
        this.parts.clear();
        other.parts.forEach((k, v) -> this.parts.put(k, v.copy()));
    }
}
