// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.anatomy.component;

import org.terasology.engine.network.Replicate;
import org.terasology.reflection.MappedContainer;

/**
 * Container for all the health details corresponding to a part in a system.
 */
@MappedContainer
public class PartHealthDetails {
    @Replicate
    public float regenRate = 1.0f;

    @Replicate
    public int health = 100;

    @Replicate
    public int maxHealth = 100;

    public long nextRegenTick;

    @Replicate
    public float waitBeforeRegen = 10.0f;

    public PartHealthDetails() {
    }

    public PartHealthDetails copy() {
        PartHealthDetails newHealth = new PartHealthDetails();
        newHealth.regenRate = this.regenRate;
        newHealth.health = this.health;
        newHealth.maxHealth = this.maxHealth;
        newHealth.nextRegenTick = this.nextRegenTick;
        newHealth.waitBeforeRegen = this.waitBeforeRegen;
        return newHealth;
    }
}
