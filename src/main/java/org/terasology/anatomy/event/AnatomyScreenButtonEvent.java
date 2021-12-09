// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.anatomy.event;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;

/**
 * This event is sent when the key V is pressed.
 */
@RegisterBindButton(id = "anatomyScreen", description = "Anatomy Screen", category = "interaction")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.V)
public class AnatomyScreenButtonEvent extends BindButtonEvent {
}
