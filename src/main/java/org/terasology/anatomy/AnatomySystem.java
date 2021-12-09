// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.anatomy;

import org.terasology.anatomy.component.AnatomyComponent;
import org.terasology.anatomy.component.AnatomyPartTag;
import org.terasology.anatomy.event.AnatomyPartImpactedEvent;
import org.terasology.anatomy.event.AnatomyStatusGatheringEvent;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.module.health.events.OnDamagedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides a basic system for managing an entity's anatomy.
 */
@RegisterSystem
public class AnatomySystem extends BaseComponentSystem {
    @In
    private EntityManager entityManager;

    private final Random random = new FastRandom();

    /**
     * Receives an {@link OnDamagedEvent} and allocates the damage to an anatomy part.
     */
    @ReceiveEvent
    public void onDamage(OnDamagedEvent event, EntityRef entity, AnatomyComponent comp) {
        if (comp != null) {
            List<String> keys = new ArrayList<>(comp.parts.keySet());
            // Randomly assign damage to a part, until positional damage is introduced.
            AnatomyPartTag partTag = comp.parts.get(keys.get(random.nextInt(0, keys.size() - 1)));
            entity.send(new AnatomyPartImpactedEvent(event.getDamageAmount(), partTag, event.getType(), event.getInstigator()));
        }
    }

    /**
     * Console command - Damages a particular anatomy part for a given amount.
     */
    @Command(shortDescription = "Damage Anatomy part for amount")
    public String dmgAnatomyPart(@Sender EntityRef entityRef, @CommandParam("name") String partName, @CommandParam("amount") int amount) {
        EntityRef clientEntity = entityRef.getComponent(ClientComponent.class).character;
        AnatomyComponent anatomyComponent = clientEntity.getComponent(AnatomyComponent.class);
        AnatomyPartTag partTag = anatomyComponent.parts.get(partName);
        if (partTag != null) {
            clientEntity.send(new AnatomyPartImpactedEvent(amount, partTag));
            return "Inflicted " + amount + " damage to " + getAnatomyNameFromID(partTag.id, anatomyComponent);
        } else {
            return "No such part found.";
        }
    }

    /**
     * Console command - Damages all anatomy parts for a given amount.
     */
    @Command(shortDescription = "Damage ALL Anatomy parts for amount")
    public String dmgAnatomyPartAll(@Sender EntityRef entityRef, @CommandParam("amount") int amount) {
        EntityRef clientEntity = entityRef.getComponent(ClientComponent.class).character;
        AnatomyComponent anatomyComponent = clientEntity.getComponent(AnatomyComponent.class);
        List<String> keys = new ArrayList<>(anatomyComponent.parts.keySet());
        String result = "";
        for (String key : keys) {
            result += "Inflicted " + amount + " damage to " + getAnatomyNameFromID(key, anatomyComponent) + "\n";
            AnatomyPartTag partTag = anatomyComponent.parts.get(key);
            clientEntity.send(new AnatomyPartImpactedEvent(amount, partTag));
        }
        return result;
    }

    /**
     * Console command - Shows a list of anatomy effects on each part.
     */
    @Command(shortDescription = "Lists anatomy effects on all parts")
    public String showAnatomyEffects(@Sender EntityRef client) {
        EntityRef character = client.getComponent(ClientComponent.class).character;
        String result = "Anatomy effects:\n";
        AnatomyStatusGatheringEvent event = new AnatomyStatusGatheringEvent();
        character.send(event);
        Map<String, List<String>> partEffects = event.getEffectsMap();
        for (Map.Entry<String, List<String>> partEntry : partEffects.entrySet()) {
            result += getAnatomyNameFromID(partEntry.getKey(), character.getComponent(AnatomyComponent.class)) + ": ";
            for (String partEffect : partEntry.getValue()) {
                result += partEffect + ", ";
            }
            result += "\n";
        }
        return result;
    }

    /**
     * Returns the anatomy part name using it's ID.
     *
     * @return Name of the part
     */
    public String getAnatomyNameFromID(String partID, AnatomyComponent component) {
        return component.parts.get(partID).name;
    }
}
