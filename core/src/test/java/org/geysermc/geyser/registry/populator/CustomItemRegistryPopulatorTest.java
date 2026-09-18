/*
 * Copyright (c) 2019-2026 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.geyser.registry.populator;

import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;
import org.geysermc.geyser.api.item.custom.v2.CustomItemDefinition;
import org.geysermc.geyser.api.util.Identifier;
import org.geysermc.geyser.registry.populator.custom.CustomItemContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomItemRegistryPopulatorTest {

    @Test
    void legacyFurnaceMinecartUsesProtocol582Schema() {
        NbtMap root = legacyFurnaceMinecart(582);
        NbtMap components = root.getCompound("components");
        NbtMap icon = components.getCompound("item_properties").getCompound("minecraft:icon");

        assertEquals("minecart_furnace", icon.getString("texture"));
        assertFalse(icon.containsKey("textures"));
        assertLegacyFurnaceComponents(components);
    }

    @Test
    void legacyFurnaceMinecartUsesProtocol766Schema() {
        NbtMap root = legacyFurnaceMinecart(766);
        NbtMap components = root.getCompound("components");
        NbtMap icon = components.getCompound("item_properties").getCompound("minecraft:icon");

        assertEquals("minecart_furnace", icon.getCompound("textures").getString("default"));
        assertFalse(icon.containsKey("texture"));
        assertLegacyFurnaceComponents(components);
    }

    private static void assertLegacyFurnaceComponents(NbtMap components) {
        assertEquals(Set.of("minecraft:display_name", "minecraft:entity_placer", "item_properties"),
            components.keySet());

        NbtMap entityPlacer = components.getCompound("minecraft:entity_placer");
        List<NbtMap> dispenseOn = entityPlacer.getList("dispense_on", NbtType.COMPOUND);
        List<NbtMap> useOn = entityPlacer.getList("use_on", NbtType.COMPOUND);
        assertEquals(List.of(NbtMap.builder().putString("tags", "q.any_tag('rail')").build()), dispenseOn);
        assertEquals(dispenseOn, useOn);
    }

    private static NbtMap legacyFurnaceMinecart(int protocolVersion) {
        CustomItemDefinition definition = mock(CustomItemDefinition.class);
        Identifier identifier = mock(Identifier.class);
        when(identifier.toString()).thenReturn("geysermc:furnace_minecart");
        when(definition.bedrockIdentifier()).thenReturn(identifier);
        CustomItemContext context = new CustomItemContext(
            definition, null, List.of(), Optional.empty(), 2000, protocolVersion);
        return CustomItemRegistryPopulator.createLegacyFurnaceMinecartNbt(context).build();
    }
}
