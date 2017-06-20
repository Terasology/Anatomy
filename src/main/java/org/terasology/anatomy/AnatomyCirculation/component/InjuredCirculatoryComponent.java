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
package org.terasology.anatomy.AnatomyCirculation.component;

import org.terasology.anatomy.component.PartHealthDetails;
import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InjuredCirculatoryComponent implements Component {
    /**
     * Maps each part to its health details.
     */
    @Replicate
    public Map<String, PartHealthDetails> partHealths = new HashMap<>();

    /**
     * Maps severity to the list of parts affected.
     */
    @Replicate
    public Map<String, List<String>> parts = new HashMap<>();

    @Replicate
    public int bloodLevel = 100;

    @Replicate
    public int maxBloodLevel = 100;

    @Replicate
    public float baseBloodRegenRate = 1.0f;

    @Replicate
    public float bloodRegenRate = 1.0f;

    public long nextRegenTick;
}
