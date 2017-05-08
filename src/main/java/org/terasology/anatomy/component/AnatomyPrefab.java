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

import org.terasology.network.Replicate;
import org.terasology.reflection.MappedContainer;

@MappedContainer
public class AnatomyPrefab {
    @Replicate
    public String name = "Body part name reference";

    @Replicate
    public String displayName = "Body part name";

    @Replicate
    public String prefabName = "";
}
