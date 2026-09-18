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
import org.geysermc.geyser.registry.type.GeyserMappingItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Conversion582_575Test {

    @Test
    void unflattensOakLog() {
        NbtMap tag = NbtMap.builder()
            .putString("name", "minecraft:oak_log")
            .putCompound("states", NbtMap.builder().putString("pillar_axis", "y").build())
            .build();
        NbtMap remapped = Conversion582_575.remapBlock(tag);
        assertEquals("minecraft:log", remapped.getString("name"));
        assertEquals("oak", remapped.getCompound("states").getString("old_log_type"));
        assertEquals("y", remapped.getCompound("states").getString("pillar_axis"));
    }

    @Test
    void remapsCherryPlanksAndCalibratedSculk() {
        NbtMap planks = Conversion582_575.remapBlock(NbtMap.builder()
            .putString("name", "minecraft:cherry_planks")
            .putCompound("states", NbtMap.EMPTY)
            .build());
        assertEquals("minecraft:planks", planks.getString("name"));
        assertEquals("oak", planks.getCompound("states").getString("wood_type"));

        NbtMap sculk = Conversion582_575.remapBlock(NbtMap.builder()
            .putString("name", "minecraft:calibrated_sculk_sensor")
            .putCompound("states", NbtMap.builder().putBoolean("powered_bit", true).build())
            .build());
        assertEquals("minecraft:sculk_sensor", sculk.getString("name"));
        assertEquals(true, sculk.getCompound("states").getBoolean("powered_bit"));
    }

    @Test
    void extraPotteryBecomesArcherShard() {
        GeyserMappingItem mapping = new GeyserMappingItem()
            .withBedrockIdentifier("minecraft:angler_pottery_shard");
        GeyserMappingItem remapped = Conversion582_575.remapItem(null, mapping);
        assertEquals("minecraft:archer_pottery_shard", remapped.getBedrockIdentifier());
    }
}
