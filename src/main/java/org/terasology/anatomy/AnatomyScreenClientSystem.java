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
import org.terasology.input.ButtonState;

/**
 * This system listens for the AnatomyScreen button press and triggers the nuiManager.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class AnatomyScreenClientSystem extends BaseComponentSystem {
    private static final String ANATOMY_SCREEN = "Anatomy:AnatomyScreen";

    @In
    private NUIManager nuiManager;

    @ReceiveEvent
    public void showAnatomyScreen(AnatomyScreenButtonEvent event, EntityRef entityRef, ClientComponent clientComponent) {
        if (event.getState() == ButtonState.DOWN) {
            nuiManager.toggleScreen(ANATOMY_SCREEN);
        }
    }
}
