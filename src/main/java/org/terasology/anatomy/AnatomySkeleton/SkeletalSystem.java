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
package org.terasology.anatomy.AnatomySkeleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.anatomy.AnatomySkeleton.component.BoneComponent;
import org.terasology.anatomy.component.AnatomyComponent;
import org.terasology.anatomy.component.PartSkeletalDetails;
import org.terasology.anatomy.event.AnatomyPartImpactedEvent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

/**
 * A skeletal system which works with Anatomy.
 * Provides basic effects.
 */
@RegisterSystem
public class SkeletalSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(SkeletalSystem.class);

    private Random random = new FastRandom();

    @ReceiveEvent
    public void onBoneDamage(AnatomyPartImpactedEvent event, EntityRef entityRef, AnatomyComponent anatomyComponent, BoneComponent boneComponent) {
        PartSkeletalDetails partDetails = boneComponent.parts.get(event.getTargetPart().name);
        // Check if the part contains bone.
        if (partDetails != null) {

        }
    }
}
