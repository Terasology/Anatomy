/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.anatomy;

import org.terasology.network.Replicate;

/**
 * Note that later, this will be divided into more concrete components like LegsComponent, ArmsComponent,
 * HandsComponent, etc.
 */
public class AnatomyPart {
    /**
     * Used to determine if this limb needs to be "revived".
     */
    public boolean isAlive = true;

    @Replicate
    public String name = "Anatomy Part Name";

    @Replicate
    public int health = 100;

    @Replicate
    public int maxHealth = 100;

    @Replicate
    public int healthRegen = 1;

    @Replicate
    public long timeBetweenHealthRegenTick = 2;

    // Add other stats (like strengths/weaknesses) pertaining to this part here. Such as AnatomyPartStatsComponent.

    // Add list of equipment here. Specifically, all the possible slots in this part.

    // Add list of status buffs here.

    // Add list of status debuffs here.

    // Add perhaps stuff like leveling, skill leveling, or EXP?

    public boolean isHealthFull() {
        return (health == maxHealth) ? true : false;
    }
}
