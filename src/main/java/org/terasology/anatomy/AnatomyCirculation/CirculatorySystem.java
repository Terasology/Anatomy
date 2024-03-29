// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.anatomy.AnatomyCirculation;

import com.google.common.collect.Lists;
import org.terasology.anatomy.AnatomyCirculation.component.InjuredCirculatoryComponent;
import org.terasology.anatomy.AnatomyCirculation.event.PartCirculatoryEffectChangedEvent;
import org.terasology.anatomy.AnatomyCirculation.event.PartCirculatoryHealthChangedEvent;
import org.terasology.anatomy.component.AnatomyComponent;
import org.terasology.anatomy.component.PartHealthDetails;
import org.terasology.anatomy.event.AnatomyStatusGatheringEvent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.players.event.OnPlayerRespawnedEvent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RegisterSystem
public class CirculatorySystem extends BaseComponentSystem {

    private static final float MINOR_BLEEDING_THRESHOLD = 0.8f;
    private static final float BLEEDING_THRESHOLD = 0.5f;
    private static final float SEVERE_BLEEDING_THRESHOLD = 0.2f;

    private final Map<Integer, String> severityNameMap = new HashMap<>();

    @Override
    public void initialise() {
        severityNameMap.put(1, "Minor bleeding");
        severityNameMap.put(2, "Bleeding");
        severityNameMap.put(3, "Severe bleeding");
    }

    @ReceiveEvent
    public void onPartCirculatoryHealthChanged(PartCirculatoryHealthChangedEvent event, EntityRef entityRef,
                                               AnatomyComponent anatomyComponent, InjuredCirculatoryComponent injuredCirculatoryComponent) {
        int severity = getEffectSeverity(event.partId, injuredCirculatoryComponent);
        if (severity == 0) {
            removeEffect(entityRef, event.partId);
        } else {
            applyEffect(entityRef, event.partId, severity);
        }
    }

    @ReceiveEvent
    public void onGather(AnatomyStatusGatheringEvent event, EntityRef entityRef, InjuredCirculatoryComponent injuredCirculatoryComponent) {
        if (event.getSystemFilter().equals("") || event.getSystemFilter().equals("Skeletal")) {
            for (Map.Entry<String, List<String>> injuredCirculatoryEffect : injuredCirculatoryComponent.parts.entrySet()) {
                for (String part : injuredCirculatoryEffect.getValue()) {
                    event.addEffect(part, severityNameMap.get(Integer.parseInt(injuredCirculatoryEffect.getKey())));
                }
            }
        }
    }

    private void applyEffect(EntityRef entityRef, String partId, int severity) {
        if (entityRef.getComponent(InjuredCirculatoryComponent.class) == null) {
            entityRef.addComponent(new InjuredCirculatoryComponent());
        }
        InjuredCirculatoryComponent injuredCirculatoryComponent = entityRef.getComponent(InjuredCirculatoryComponent.class);
        for (Map.Entry<String, List<String>> partsOfSeverity : injuredCirculatoryComponent.parts.entrySet()) {
            if (partsOfSeverity.getValue().contains(partId)) {
                if (Integer.parseInt(partsOfSeverity.getKey()) == severity) {
                    return;
                } else {
                    partsOfSeverity.getValue().remove(partId);
                }
            }
        }
        if (injuredCirculatoryComponent.parts.containsKey(String.valueOf(severity))) {
            injuredCirculatoryComponent.parts.get(String.valueOf(severity)).add(partId);
        } else {
            injuredCirculatoryComponent.parts.put(String.valueOf(severity), Lists.newArrayList(partId));
        }
        entityRef.saveComponent(injuredCirculatoryComponent);
        entityRef.send(new PartCirculatoryEffectChangedEvent());
    }

    private void removeEffect(EntityRef entityRef, String partId) {
        InjuredCirculatoryComponent injuredCirculatoryComponent = entityRef.getComponent(InjuredCirculatoryComponent.class);
        if (injuredCirculatoryComponent != null) {
            for (Map.Entry<String, List<String>> partsOfSeverity : injuredCirculatoryComponent.parts.entrySet()) {
                partsOfSeverity.getValue().remove(partId);
            }
            entityRef.saveComponent(injuredCirculatoryComponent);
            entityRef.send(new PartCirculatoryEffectChangedEvent());
        }
    }

    private int getEffectSeverity(String partId, InjuredCirculatoryComponent injuredCirculatoryComponent) {
        int maxHealth = injuredCirculatoryComponent.partHealths.get(partId).maxHealth;
        int health = injuredCirculatoryComponent.partHealths.get(partId).health;
        float healthPercent = (float) health / maxHealth;
        int severity = 0;
        if (healthPercent > BLEEDING_THRESHOLD && healthPercent <= MINOR_BLEEDING_THRESHOLD) {
            severity = 1;
        } else if (healthPercent > SEVERE_BLEEDING_THRESHOLD && healthPercent <= BLEEDING_THRESHOLD) {
            severity = 2;
        } else if (healthPercent <= SEVERE_BLEEDING_THRESHOLD) {
            severity = 3;
        }
        return severity;
    }

    @ReceiveEvent
    public void onPlayerRespawn(OnPlayerRespawnedEvent event, EntityRef entityRef,
                                InjuredCirculatoryComponent injuredCirculatoryComponent) {
        entityRef.removeComponent(InjuredCirculatoryComponent.class);
    }

    @Command(shortDescription = "Show circulatory healths of all injured parts")
    public String showCirculatoryHealths(@Sender EntityRef client) {
        EntityRef character = client.getComponent(ClientComponent.class).character;
        InjuredCirculatoryComponent injuredCirculatoryComponent = character.getComponent(InjuredCirculatoryComponent.class);
        String result = "";
        if (injuredCirculatoryComponent != null) {
            result += "Blood level : ";
            result += injuredCirculatoryComponent.bloodLevel + "/" +
                    injuredCirculatoryComponent.maxBloodLevel +
                    " Blood regen rate: " +
                    injuredCirculatoryComponent.bloodRegenRate + "\n";
            result += "Circulatory system healths :\n";
            for (Map.Entry<String, PartHealthDetails> partHealthDetailsEntry : injuredCirculatoryComponent.partHealths.entrySet()) {
                result +=
                        partHealthDetailsEntry.getKey()
                                + " :" + partHealthDetailsEntry.getValue().health
                                + "/" + partHealthDetailsEntry.getValue().maxHealth + "\n";
            }
        } else {
            result += "Circulatory system healthy.\n";
        }
        return result;
    }

    @Command(shortDescription = "Heal all circulatory system parts to full health")
    public String healAllCirculation(@Sender EntityRef client) {
        EntityRef character = client.getComponent(ClientComponent.class).character;
        InjuredCirculatoryComponent injuredCirculatoryComponent = character.getComponent(InjuredCirculatoryComponent.class);
        if (injuredCirculatoryComponent != null) {
            for (Map.Entry<String, PartHealthDetails> partHealthDetailsEntry : injuredCirculatoryComponent.partHealths.entrySet()) {
                partHealthDetailsEntry.getValue().health = partHealthDetailsEntry.getValue().maxHealth;
                character.send(new PartCirculatoryHealthChangedEvent(partHealthDetailsEntry.getKey()));
            }
        }
        return "Circulatory healths fully restored.";
    }
}
