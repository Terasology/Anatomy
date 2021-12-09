// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.anatomy.event;

import org.terasology.anatomy.component.AnatomyPartTag;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.logic.health.EngineDamageTypes;
import org.terasology.engine.network.ServerEvent;
import org.terasology.gestalt.entitysystem.event.Event;

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
     * @param amount The amount of damage being caused.
     * @param targetPart The target anatomy part.
     * @param damageType The type of damage being dealt.
     * @param instigator The entity which caused the damage.
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
