// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.anatomy.ui;

import org.terasology.anatomy.event.AnatomyStatusGatheringEvent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.nui.BaseInteractionScreen;
import org.terasology.engine.utilities.Assets;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.skin.UISkin;
import org.terasology.nui.widgets.UILabel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This represents the Anatomy Screen, which contains information about the effects on the player.
 */
public class AnatomyScreenWindow extends BaseInteractionScreen {
    private static final String ANATOMY_PART_PREFIX = "Anatomy:";
    private final UISkin greenTextSkin = Assets.getSkin("Anatomy:greenText").get();
    private final UISkin redTextSkin = Assets.getSkin("Anatomy:redText").get();
    private EntityRef player = EntityRef.NULL;

    @Override
    public void initialise() {
    }

    @Override
    protected void initializeWithInteractionTarget(EntityRef interactionTarget) {
    }

    /**
     * Gets the player entity from the Core Registry.
     */
    private void reInit() {
        player = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
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
