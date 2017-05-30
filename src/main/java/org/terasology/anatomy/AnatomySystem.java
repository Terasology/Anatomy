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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.anatomy.component.AnatomyComponent;
import org.terasology.anatomy.component.AnatomyPartTag;
import org.terasology.anatomy.event.AnatomyEffectAddedEvent;
import org.terasology.anatomy.event.AnatomyEffectRemovedEvent;
import org.terasology.anatomy.event.AnatomyPartImpactedEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.health.OnDamagedEvent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides a basic system for managing an entity's anatomy.
 * Much of this system is still under development.
 */
@RegisterSystem
public class AnatomySystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(AnatomySystem.class);

    @In
    private EntityManager entityManager;

    @In
    private DelayManager delayManager;

    // USED only for TESTING, with the assumption that ONLY the local player has the AnatomyComponent attached.
    @In
    private EntityRef playerRef;

    private Random random = new FastRandom();

    @ReceiveEvent
    public void onDamage(OnDamagedEvent event, EntityRef entity, AnatomyComponent comp) {
        if (comp != null) {
            List<String> keys = new ArrayList<>(comp.parts.keySet());
            // Randomly assign damage to a part, until positional damage is introduced.
            AnatomyPartTag partTag = comp.parts.get(keys.get(random.nextInt(0, keys.size() - 1)));
            entity.send(new AnatomyPartImpactedEvent(event.getDamageAmount(), partTag, event.getType(), event.getInstigator()));
        }
    }

    @ReceiveEvent
    public void onAnatomyEffectAdded(AnatomyEffectAddedEvent event, EntityRef entityRef, AnatomyComponent component) {
        List<String> partEffects = component.parts.get(event.partId).effects;
        if (!partEffects.contains(event.effectName)) {
            partEffects.add(event.effectName);
        }
        entityRef.saveComponent(component);
    }

    @ReceiveEvent
    public void onAnatomyEffectRemoved(AnatomyEffectRemovedEvent event, EntityRef entityRef, AnatomyComponent component) {
        List<String> partEffects = component.parts.get(event.partId).effects;
        partEffects.remove(event.effectName);
        entityRef.saveComponent(component);
    }

    @Command(shortDescription = "Damage Anatomy part for amount")
    public void dmgAnatomyPart(@CommandParam("name") String partName, @CommandParam("amount") int amount) {
        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyComponent.class)) {
            AnatomyComponent anatomyComponent = clientEntity.getComponent(AnatomyComponent.class);
            AnatomyPartTag partTag = anatomyComponent.parts.get(partName);
            if (partTag != null) {
                clientEntity.send(new AnatomyPartImpactedEvent(amount, partTag));
            }
        }
    }

    @Command(shortDescription = "Damage ALL Anatomy parts for amount")
    public void dmgAnatomyPartAll(@CommandParam("amount") int amount) {
        for (EntityRef clientEntity : entityManager.getEntitiesWith(AnatomyComponent.class)) {
            AnatomyComponent anatomyComponent = clientEntity.getComponent(AnatomyComponent.class);
            List<String> keys = new ArrayList<>(anatomyComponent.parts.keySet());
            for (String key : keys) {
                AnatomyPartTag partTag = anatomyComponent.parts.get(key);
                clientEntity.send(new AnatomyPartImpactedEvent(amount, partTag));
            }
        }
    }

    @Command(shortDescription = "Lists anatomy effects on all parts")
    public String showAnatomyEffects(@Sender EntityRef client) {
        EntityRef character = client.getComponent(ClientComponent.class).character;
        String result = "Anatomy effects:\n";
        for (Map.Entry<String, AnatomyPartTag> partEntry : character.getComponent(AnatomyComponent.class).parts.entrySet()) {
            result += partEntry.getValue().name + ": ";
            for (String partEffect : partEntry.getValue().effects) {
                result += partEffect + ", ";
            }
            result += "\n";
        }
        return result;
    }
}
