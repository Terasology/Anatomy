// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.anatomy.event;

import com.google.common.collect.Lists;
import org.terasology.gestalt.entitysystem.event.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This event is sent to gather all anatomical part statuses from different systems.
 */
public class AnatomyStatusGatheringEvent implements Event {
    /**
     * Decides which systems will respond to the event. "" means all, or "Skeletal" for only skeletal system.
     */
    private String systemFilter = "";

    /**
     * The map which stores the parts mapped to list of effects.
     */
    private final Map<String, List<String>> effectsMap = new HashMap<>();

    /**
     * Create the event for all systems to respond to.
     */
    public AnatomyStatusGatheringEvent() {
    }

    /**
     * Create the event where only the system satisfying the filter will respond.
     *
     * @param systemFilter String for filtering which systems respond.
     */
    public AnatomyStatusGatheringEvent(String systemFilter) {
        this.systemFilter = systemFilter;
    }

    /**
     * Get the system filter string.
     *
     * @return The system filter string.
     */
    public String getSystemFilter() {
        return systemFilter;
    }

    /**
     * Get the effects map.
     *
     * @return The map from parts to its list of effects.
     */
    public Map<String, List<String>> getEffectsMap() {
        return effectsMap;
    }

    public void addEffect(String partId, String effect) {
        List<String> partEffects = effectsMap.get(partId);
        if (partEffects == null) {
            List<String> effects = Lists.newArrayList();
            effects.add(effect);
            effectsMap.put(partId, effects);
        } else {
            partEffects.add(effect);
        }
    }
}
