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
package org.terasology.anatomy.component;

import com.google.common.collect.Lists;
import org.terasology.network.Replicate;
import org.terasology.reflection.MappedContainer;

import java.util.List;

@MappedContainer
public class AnatomyPartTag {
    /**
     * Id of the anatomy part. Useful for multi-word names.
     */
    public String id = "";

    /**
     * Name of the anatomy part.
     */
    @Replicate
    public String name = "";

    /**
     * List of characteristics of a part like bone, blood etc.
     */
    @Replicate
    public List<String> characteristics = Lists.newArrayList();

    /**
     * List of ability that this part grants/contributes to.
     */
    @Replicate
    public List<String> abilities = Lists.newArrayList();
}
