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

import org.terasology.anatomy.component.AnatomyPartTag;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.engine.network.ServerEvent;

/**
 * This event is sent when an anatomy part is damaged.
 */
@ServerEvent
public class AnatomyPartImpactedEvent implements Event {
    private int amount;
    private Prefab damageType;
    private EntityRef instigator;
    private EntityRef directCause;
    private AnatomyPartTag targetPart;

    public AnatomyPartImpactedEvent() {
    }

    public AnatomyPartImpactedEvent(int amount, AnatomyPartTag targetPart) {
        this(amount, targetPart, EngineDamageTypes.DIRECT.get());
    }

    public AnatomyPartImpactedEvent(int amount, AnatomyPartTag targetPart, Prefab damageType) {
        this(amount, targetPart, damageType, EntityRef.NULL);
    }

    public AnatomyPartImpactedEvent(int amount, AnatomyPartTag targetPart, Prefab damageType, EntityRef instigator) {
        this(amount, targetPart, damageType, instigator, EntityRef.NULL);
    }

    /**
     * @param amount      The amount of damage being caused.
     * @param targetPart  The target anatomy part.
     * @param damageType  The type of damage being dealt.
     * @param instigator  The entity which caused the damage.
     * @param directCause The tool used for causing the damage.
     */
    public AnatomyPartImpactedEvent(int amount, AnatomyPartTag targetPart, Prefab damageType, EntityRef instigator, EntityRef directCause) {
        this.amount = amount;
        this.damageType = damageType;
        this.instigator = instigator;
        this.directCause = directCause;
        this.targetPart = targetPart;
    }

    public int getAmount() {
        return amount;
    }

    public Prefab getDamageType() {
        return damageType;
    }

    public AnatomyPartTag getTargetPart() {
        return targetPart;
    }
}
