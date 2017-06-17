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
package org.terasology.anatomy.ui;

import org.terasology.anatomy.event.AnatomyStatusGatheringEvent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.BaseInteractionScreen;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.utilities.Assets;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AnatomyScreenWindow extends BaseInteractionScreen {
    private static final String ANATOMY_PART_PREFIX = "Anatomy:";
    private EntityRef player = EntityRef.NULL;

    private final UISkin greenTextSkin = Assets.getSkin("Anatomy:greenText").get();
    private final UISkin redTextSkin = Assets.getSkin("Anatomy:redText").get();

    @Override
    public void initialise() {
    }

    @Override
    protected void initializeWithInteractionTarget(EntityRef interactionTarget) {
    }

    private void reInit() {
        player = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();

        // In case the player has been created yet, exit out early to prevent an error.
        if (player == EntityRef.NULL) {
            return;
        }
    }

    @Override
    public void onOpened() {
        EntityRef characterEntity = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
        CharacterComponent characterComponent = characterEntity.getComponent(CharacterComponent.class);

        // In case the player has been created yet, exit out early to prevent an error.
        if (characterComponent == null) {
            return;
        }

        // If the reference to the player entity hasn't been set yet, or it refers to a NULL entity, call the reInit()
        // method to set it. The getId() check is necessary for certain network entities whose ID is 0, but are
        // erroneously marked as existent.
        if (!player.exists() || (player.exists() && (player == EntityRef.NULL || player.getId() == 0 || player == null))) {
            reInit();
        }

        // As long as there's an interaction target, open this window.
        if (getInteractionTarget() != EntityRef.NULL) {
            initializeWithInteractionTarget(getInteractionTarget());
            super.onOpened();
        }

        // Every time the character screen window is opened, update the Anatomy part statuses.
        updateStatuses();
    }

    private void updateStatuses() {
        // Only update the statuses if the player character entity actually exists.
        if (player == null || player == EntityRef.NULL || player.getId() == 0) {
            return;
        }

        AnatomyStatusGatheringEvent event = new AnatomyStatusGatheringEvent();
        player.send(event);
        Map<String, List<String>> partEffectsMap = event.getEffectsMap();


        Collection<UILabel> labels = findAll(UILabel.class);
        for (UILabel label : labels) {
            if (label.getId().contains(ANATOMY_PART_PREFIX)) {
                String partID = label.getId().substring(ANATOMY_PART_PREFIX.length());
                List<String> partEffects = partEffectsMap.get(partID);
                if (partEffects == null) {
                    // No effects for this part
                    label.setSkin(greenTextSkin);
                    label.bindTooltipString(new ReadOnlyBinding<String>() {
                        @Override
                        public String get() {
                            return null;
                        }
                    });
                } else {
                    // This part has effects
                    label.setSkin(redTextSkin);
                    label.setTooltipDelay(0);
                    label.bindTooltipString(new ReadOnlyBinding<String>() {
                        @Override
                        public String get() {
                            return String.join(",", partEffects);
                        }
                    });
                }
            }
        }
    }
}
