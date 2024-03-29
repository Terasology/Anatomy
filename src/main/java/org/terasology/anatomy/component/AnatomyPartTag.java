// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.anatomy.component;

import com.google.common.collect.Lists;
import org.terasology.engine.network.Replicate;
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

    public AnatomyPartTag copy() {
        AnatomyPartTag newAnatomyPartTag = new AnatomyPartTag();
        newAnatomyPartTag.id = this.id;
        newAnatomyPartTag.name = this.name;
        newAnatomyPartTag.characteristics = Lists.newArrayList(this.characteristics);
        newAnatomyPartTag.abilities = Lists.newArrayList(this.abilities);
        return newAnatomyPartTag;
    }
}
