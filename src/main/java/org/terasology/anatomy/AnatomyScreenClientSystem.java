// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.anatomy;

import org.terasology.anatomy.event.AnatomyScreenButtonEvent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.nui.input.ButtonState;

/**
 * This system listens for the AnatomyScreen button press and triggers the nuiManager.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class AnatomyScreenClientSystem extends BaseComponentSystem {
    private static final String ANATOMY_SCREEN = "Anatomy:AnatomyScreen";

    @In
    private NUIManager nuiManager;

    @ReceiveEvent
    public void showAnatomyScreen(AnatomyScreenButtonEvent event, EntityRef entityRef,
                                  ClientComponent clientComponent) {
        if (event.getState() == ButtonState.DOWN) {
            nuiManager.toggleScreen(ANATOMY_SCREEN);
        }
    }
}
