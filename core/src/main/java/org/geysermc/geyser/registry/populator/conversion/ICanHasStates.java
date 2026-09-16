/*
 * Copyright (c) 2026 GeyserMC. http://geysermc.org
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.geyser.registry.populator.conversion;

import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.registry.type.GeyserMappingItem;

/**
 * Downgrades 26.50 palettes: that version added server-controlled stair/fence states
 * and coloured wool/concrete stairs and slabs that do not exist below 26.50.
 */
public class ICanHasStates extends ConversionHelper {
    public static NbtMap convertBlock(NbtMap tag) {
        String name = tag.getString("name");
        String remapped = remapName(name);
        if (!remapped.equals(name)) {
            tag = withId(tag, remapped);
            name = remapped;
        }
        if (name.contains("_stairs")) {
            return removeStates(tag, "minecraft:corner");
        }
        if ((name.contains("_fence") && !name.contains("_fence_gate"))
                || name.contains("glass_pane")
                || name.contains("_bars")
                || name.equals("minecraft:trip_wire")
                || name.equals("minecraft:tripwire")) {
            return removeStates(tag, "minecraft:connection_north", "minecraft:connection_east",
                "minecraft:connection_south", "minecraft:connection_west");
        }
        return tag;
    }

    public static GeyserMappingItem remapItem(Item item, GeyserMappingItem mapping) {
        String identifier = mapping.getBedrockIdentifier();
        if (identifier == null || identifier.isEmpty()) {
            return mapping;
        }
        String namespaced = identifier.contains(":") ? identifier : "minecraft:" + identifier;
        String remapped = remapName(namespaced);
        if (remapped.equals(namespaced)) {
            return mapping;
        }
        return mapping.withBedrockIdentifier(remapped);
    }

    static String remapName(String name) {
        if (name.endsWith("_wool_stairs")) {
            return "minecraft:oak_stairs";
        }
        if (name.endsWith("_concrete_stairs")) {
            return "minecraft:stone_stairs";
        }
        if (name.endsWith("_wool_double_slab")) {
            return "minecraft:oak_double_slab";
        }
        if (name.endsWith("_wool_slab")) {
            return "minecraft:oak_slab";
        }
        if (name.endsWith("_concrete_double_slab")) {
            return "minecraft:smooth_stone_double_slab";
        }
        if (name.endsWith("_concrete_slab")) {
            return "minecraft:smooth_stone_slab";
        }
        if (name.equals("minecraft:red_shrub")) {
            return "minecraft:deadbush";
        }
        if (name.equals("minecraft:shelf_mushroom")) {
            return "minecraft:brown_mushroom";
        }
        return name;
    }
}
