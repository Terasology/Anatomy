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

import com.google.common.collect.Lists;
import org.terasology.anatomy.AnatomySkeleton.component.InjuredBoneComponent;
import org.terasology.anatomy.AnatomySkeleton.event.BoneHealthChangedEvent;
import org.terasology.anatomy.component.AnatomyComponent;
import org.terasology.anatomy.component.PartHealthDetails;
import org.terasology.anatomy.event.AnatomyStatusGatheringEvent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.network.ClientComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A skeletal system which works with Anatomy. Provides a basic bone breaking effect with 3 levels of severity.
 */
@RegisterSystem
public class SkeletalSystem extends BaseComponentSystem {
    private Map<Integer, String> severityNameMap = new HashMap<>();

    private float DAMAGED_BONE_THRESHOLD = 0.6f;
    private float BROKEN_BONE_THRESHOLD = 0.4f;
    private float SHATTERED_BONE_THRESHOLD = 0.2f;

    @Override
    public void initialise() {
        severityNameMap.put(1, "Damaged bone");
        severityNameMap.put(2, "Broken bone");
        severityNameMap.put(3, "Shattered bone");
    }

    @ReceiveEvent
    public void onBoneHealthChanged(BoneHealthChangedEvent event, EntityRef entityRef, AnatomyComponent anatomyComponent, InjuredBoneComponent injuredBoneComponent) {
        int severity = getEffectSeverity(event.partId, injuredBoneComponent);
        if (severity == 0) {
            removeEffect(entityRef, event.partId);
        } else {
            applyEffect(entityRef, event.partId, severity);
        }
    }

    @ReceiveEvent
    public void onGather(AnatomyStatusGatheringEvent event, EntityRef entityRef, InjuredBoneComponent injuredBoneComponent) {
        if (event.getSystemFilter().equals("") || event.getSystemFilter().equals("Skeletal")) {
            for (Map.Entry<String, List<String>> injuredBoneEffect : injuredBoneComponent.parts.entrySet()) {
                for (String part : injuredBoneEffect.getValue()) {
                    event.addEffect(part, severityNameMap.get(Integer.parseInt(injuredBoneEffect.getKey())));
                }
            }
        }
    }

    private void applyEffect(EntityRef entityRef, String partId, int severity) {
        if (entityRef.getComponent(InjuredBoneComponent.class) == null) {
            entityRef.addComponent(new InjuredBoneComponent());
        }
        InjuredBoneComponent injuredBoneComponent = entityRef.getComponent(InjuredBoneComponent.class);
        for (Map.Entry<String, List<String>> partsOfSeverity : injuredBoneComponent.parts.entrySet()) {
            if (partsOfSeverity.getValue().contains(partId)) {
                if (Integer.parseInt(partsOfSeverity.getKey()) == severity) {
                    return;
                } else {
                    partsOfSeverity.getValue().remove(partId);
                }
            }
        }
        if (injuredBoneComponent.parts.containsKey(String.valueOf(severity))) {
            injuredBoneComponent.parts.get(String.valueOf(severity)).add(partId);
        } else {
            injuredBoneComponent.parts.put(String.valueOf(severity), Lists.newArrayList(partId));
        }
        entityRef.saveComponent(injuredBoneComponent);
    }

    private void removeEffect(EntityRef entityRef, String partId) {
        InjuredBoneComponent injuredBoneComponent = entityRef.getComponent(InjuredBoneComponent.class);
        if (injuredBoneComponent != null) {
            for (Map.Entry<String, List<String>> partsOfSeverity : injuredBoneComponent.parts.entrySet()) {
                partsOfSeverity.getValue().remove(partId);
            }
            entityRef.saveComponent(injuredBoneComponent);
        }
    }

    private int getEffectSeverity(String partId, InjuredBoneComponent injuredBoneComponent) {
        int maxHealth = injuredBoneComponent.partHealths.get(partId).maxHealth;
        int health = injuredBoneComponent.partHealths.get(partId).health;
        float healthPercent = (float) health / maxHealth;
        int severity = 0;
        if (healthPercent > BROKEN_BONE_THRESHOLD && healthPercent <= DAMAGED_BONE_THRESHOLD) {
            severity = 1;
        } else if (healthPercent > SHATTERED_BONE_THRESHOLD && healthPercent <= BROKEN_BONE_THRESHOLD) {
            severity = 2;
        } else if (healthPercent <= SHATTERED_BONE_THRESHOLD) {
            severity = 3;
        }
        return severity;
    }

    @Command(shortDescription = "Show bone healths of all injured parts")
    public String showBoneHealths(@Sender EntityRef client) {
        EntityRef character = client.getComponent(ClientComponent.class).character;
        InjuredBoneComponent injuredBoneComponent = character.getComponent(InjuredBoneComponent.class);
        String result = "Bone healths :\n";
        if (injuredBoneComponent != null) {
            for (Map.Entry<String, PartHealthDetails> partHealthDetailsEntry : injuredBoneComponent.partHealths.entrySet()) {
                result += partHealthDetailsEntry.getKey() + " :" + partHealthDetailsEntry.getValue().health + "/" + partHealthDetailsEntry.getValue().maxHealth + "\n";
            }
        }
        return result;
    }

    @Command(shortDescription = "Heal all bone parts to full health")
    public String healAllBones(@Sender EntityRef client) {
        EntityRef character = client.getComponent(ClientComponent.class).character;
        InjuredBoneComponent injuredBoneComponent = character.getComponent(InjuredBoneComponent.class);
        if (injuredBoneComponent != null) {
            for (Map.Entry<String, PartHealthDetails> partHealthDetailsEntry : injuredBoneComponent.partHealths.entrySet()) {
                partHealthDetailsEntry.getValue().health = partHealthDetailsEntry.getValue().maxHealth;
                character.send(new BoneHealthChangedEvent(partHealthDetailsEntry.getKey()));
            }
        }
        return "Healths fully restored.";
    }
}
