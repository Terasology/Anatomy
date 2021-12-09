// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.anatomy.AnatomySkeleton.event;

import org.terasology.gestalt.entitysystem.event.Event;

/**
 * This event is sent when a part's skeletal system health changes.
 */
public class BoneHealthChangedEvent implements Event {
    /**
     * The ID of the part.
     */
    public String partId;

    /**
     * @param partId ID of the part whose health changed.
     */
    public BoneHealthChangedEvent(String partId) {
        this.partId = partId;
    }
}
