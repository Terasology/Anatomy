// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.anatomy.AnatomyCirculation;

import org.terasology.anatomy.AnatomyCirculation.component.InjuredCirculatoryComponent;
import org.terasology.anatomy.AnatomyCirculation.event.BloodLevelChangedEvent;
import org.terasology.anatomy.AnatomyCirculation.event.PartCirculatoryEffectChangedEvent;
import org.terasology.anatomy.AnatomyCirculation.event.PartCirculatoryHealthChangedEvent;
import org.terasology.anatomy.component.AnatomyComponent;
import org.terasology.anatomy.component.PartHealthDetails;
import org.terasology.anatomy.event.AnatomyPartImpactedEvent;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.engine.registry.In;
import org.terasology.math.TeraMath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class CirculatoryHealthSystem extends BaseComponentSystem {
    private final Map<Integer, Float> severityBleedingRateMap = new HashMap<>();
    private final float bluntDamageMultiplier = 0.5f;
    private final float pierceDamageMultiplier = 1.5f;
    private final String CIRCULATORY_REGEN_PREFIX = "Circulatory:Regen:";
    private final String CIRCULATORY_BLOOD_REGEN_PREFIX = "Circulatory:BloodRegen:";
    private final String CIRCULATORY_CHARACTERISTIC = "blood";
    @In
    private EntityManager entityManager;
    @In
    private DelayManager delayManager;
    @In
    private org.terasology.engine.core.Time time;

    @Override
    public void initialise() {
        severityBleedingRateMap.put(1, -0.5f);
        severityBleedingRateMap.put(2, -1.0f);
        severityBleedingRateMap.put(3, -2.0f);
    }

    @ReceiveEvent
    public void onBloodLevelRegen(DelayedActionTriggeredEvent event, EntityRef entityRef,
                                  InjuredCirculatoryComponent injuredCirculatoryComponent) {
        if (event.getActionId().startsWith(CIRCULATORY_BLOOD_REGEN_PREFIX)) {
            if (injuredCirculatoryComponent.bloodLevel >= 0 && injuredCirculatoryComponent.bloodLevel <= injuredCirculatoryComponent.maxBloodLevel && injuredCirculatoryComponent.bloodRegenRate != 0) {
                int healAmount = 0;
                healAmount += injuredCirculatoryComponent.bloodRegenRate / TeraMath.fastAbs(injuredCirculatoryComponent.bloodRegenRate);
                injuredCirculatoryComponent.nextRegenTick =
                        time.getGameTimeInMs() + (long) (1000 / TeraMath.fastAbs(injuredCirculatoryComponent.bloodRegenRate));
                injuredCirculatoryComponent.bloodLevel += healAmount;
                injuredCirculatoryComponent.bloodLevel = TeraMath.clamp(injuredCirculatoryComponent.bloodLevel, 0,
                        injuredCirculatoryComponent.maxBloodLevel);
                entityRef.saveComponent(injuredCirculatoryComponent);
                entityRef.send(new BloodLevelChangedEvent());
            }
            delayManager.addDelayedAction(entityRef, CIRCULATORY_BLOOD_REGEN_PREFIX,
                    (long) (1000 / TeraMath.fastAbs(injuredCirculatoryComponent.bloodRegenRate)));
        }
    }

    @ReceiveEvent
    public void onPartHealthRegen(DelayedActionTriggeredEvent event, EntityRef entityRef,
                                  InjuredCirculatoryComponent injuredCirculatoryComponent) {
        if (event.getActionId().startsWith(CIRCULATORY_REGEN_PREFIX)) {
            String partID = event.getActionId().substring(CIRCULATORY_REGEN_PREFIX.length());
            PartHealthDetails partDetails = injuredCirculatoryComponent.partHealths.get(partID);
            if (partDetails.health >= 0 && partDetails.health != partDetails.maxHealth && partDetails.regenRate != 0) {
                int healAmount = 0;
                healAmount = regenerateHealth(partDetails, healAmount);
                partDetails.health += healAmount;
                partDetails.health = TeraMath.clamp(partDetails.health, 0, partDetails.maxHealth);
                entityRef.saveComponent(injuredCirculatoryComponent);
                entityRef.send(new PartCirculatoryHealthChangedEvent(partID));
            }
            delayManager.addDelayedAction(entityRef, CIRCULATORY_REGEN_PREFIX + partID,
                    (long) (1000 / partDetails.regenRate));
        }
    }

    @ReceiveEvent
    public void onBleedingRateChanged(PartCirculatoryEffectChangedEvent event, EntityRef entityRef,
                                      InjuredCirculatoryComponent injuredCirculatoryComponent) {
        float bloodRegenRate = injuredCirculatoryComponent.baseBloodRegenRate;
        for (Map.Entry<String, List<String>> severityPartsEntry : injuredCirculatoryComponent.parts.entrySet()) {
            bloodRegenRate += severityPartsEntry.getValue().size() * severityBleedingRateMap.get(Integer.parseInt(severityPartsEntry.getKey()));
        }
        injuredCirculatoryComponent.bloodRegenRate = bloodRegenRate;
        entityRef.saveComponent(injuredCirculatoryComponent);
    }

    @ReceiveEvent
    public void onCirculatoryDamage(AnatomyPartImpactedEvent event, EntityRef entityRef,
                                    AnatomyComponent anatomyComponent) {
        if (anatomyComponent.parts.get(event.getTargetPart().id).characteristics.contains(CIRCULATORY_CHARACTERISTIC)) {
            InjuredCirculatoryComponent injuredCirculatoryComponent =
                    entityRef.getComponent(InjuredCirculatoryComponent.class);
            if (injuredCirculatoryComponent == null) {
                injuredCirculatoryComponent = new InjuredCirculatoryComponent();
                entityRef.addComponent(injuredCirculatoryComponent);
            }
            PartHealthDetails partHealthDetails = injuredCirculatoryComponent.partHealths.get(event.getTargetPart().id);
            if (partHealthDetails == null) {
                partHealthDetails = new PartHealthDetails();
                injuredCirculatoryComponent.partHealths.put(event.getTargetPart().id, partHealthDetails);
                // Part has been injured for the first time, so add delayed part health regen event and blood level
                // regen event.
                delayManager.addDelayedAction(entityRef, CIRCULATORY_REGEN_PREFIX + event.getTargetPart().id,
                        (long) (1000 / partHealthDetails.regenRate));
                delayManager.addDelayedAction(entityRef, CIRCULATORY_BLOOD_REGEN_PREFIX, 1000);
            }
            int damageAmount = event.getAmount();
            if (event.getDamageType().getName().equals("Equipment:pierceDamage")) {
                damageAmount *= pierceDamageMultiplier;
            }
            if (event.getDamageType().getName().equals("Equipment:bluntDamage")) {
                damageAmount *= bluntDamageMultiplier;
            }
            partHealthDetails.health -= damageAmount;
            partHealthDetails.health = TeraMath.clamp(partHealthDetails.health, 0, partHealthDetails.maxHealth);
            partHealthDetails.nextRegenTick =
                    time.getGameTimeInMs() + TeraMath.floorToInt(partHealthDetails.waitBeforeRegen * 1000);
            entityRef.saveComponent(injuredCirculatoryComponent);
            entityRef.send(new PartCirculatoryHealthChangedEvent(event.getTargetPart().id));
        }
    }

    private int regenerateHealth(PartHealthDetails partDetails, int healAmount) {
        int newHeal = healAmount;
        while (time.getGameTimeInMs() >= partDetails.nextRegenTick) {
            newHeal++;
            partDetails.nextRegenTick = partDetails.nextRegenTick + (long) (1000 / partDetails.regenRate);
        }
        return newHeal;
    }
}