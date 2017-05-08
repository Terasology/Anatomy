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
package org.terasology.anatomy.event;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;

/**
 * Send this event to an entity to revive its anatomy part(s).
 * Will be differentiated from DoHealEvent in the future.
 */
public class DoAnatomyReviveEvent implements Event {
    private int amount;
    private String targetPartName;
    private EntityRef instigator;

    public DoAnatomyReviveEvent(int amount) {
        this(amount, EntityRef.NULL);
    }

    public DoAnatomyReviveEvent(int amount, EntityRef instigator) {
        this.amount = amount;
        this.instigator = instigator;
    }

    public DoAnatomyReviveEvent(int amount, String targetPartName, EntityRef instigator) {
        this.amount = amount;
        this.targetPartName = targetPartName;
        this.instigator = instigator;
    }

    public int getAmount() {
        return amount;
    }

    public String getTargetPartName() {
        return targetPartName;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
