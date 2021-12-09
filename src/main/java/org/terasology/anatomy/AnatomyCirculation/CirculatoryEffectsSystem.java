// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.anatomy.AnatomyCirculation;

import org.terasology.anatomy.AnatomyCirculation.component.InjuredCirculatoryComponent;
import org.terasology.anatomy.AnatomyCirculation.event.BloodLevelChangedEvent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.health.DestroyEvent;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

@RegisterSystem
public class CirculatoryEffectsSystem extends BaseComponentSystem {

    @In
    private PrefabManager prefabManager;

    @ReceiveEvent
    public void onBloodLevelChanged(BloodLevelChangedEvent event, EntityRef entityRef,
                                    InjuredCirculatoryComponent injuredCirculatoryComponent) {
        if (injuredCirculatoryComponent.bloodLevel <= 0) {
            Prefab bloodLossDamage = prefabManager.getPrefab("Anatomy:bloodLoss");
            entityRef.send(new DestroyEvent(EntityRef.NULL, EntityRef.NULL, bloodLossDamage));
        }
    }
}
