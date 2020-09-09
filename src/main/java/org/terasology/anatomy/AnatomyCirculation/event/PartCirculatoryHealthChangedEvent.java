// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.anatomy.AnatomyCirculation.event;

import org.terasology.engine.entitySystem.event.Event;

public class PartCirculatoryHealthChangedEvent implements Event {
    public String partId;

    public PartCirculatoryHealthChangedEvent(String partId) {
        this.partId = partId;
    }
}
