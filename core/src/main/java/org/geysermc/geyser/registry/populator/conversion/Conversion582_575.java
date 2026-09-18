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
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.geysermc.geyser.item.type.Item;
import org.geysermc.geyser.registry.type.GeyserMappingItem;

import java.util.Map;
import java.util.Set;

/**
 * Downgrades Bedrock 1.19.80/1.19.83 (582) identifiers/states to the 1.19.70/1.19.73 (575) palette.
 * Reverse of Geyser 2.1.0's 1.19.80 mapper: overworld logs/fences were still meta, cherry did not
 * exist, calibrated sculk / pink petals / extra pottery / armor trims were 1.19.80+.
 */
public final class Conversion582_575 extends ConversionHelper {

    private static final Set<String> KEEP_POTTERY_SHARDS = Set.of(
        "minecraft:archer_pottery_shard",
        "minecraft:arms_up_pottery_shard",
        "minecraft:prize_pottery_shard",
        "minecraft:skull_pottery_shard"
    );

    private static final Set<String> ALREADY_SEPARATE_FENCES = Set.of(
        "minecraft:nether_brick_fence",
        "minecraft:crimson_fence",
        "minecraft:warped_fence",
        "minecraft:mangrove_fence",
        "minecraft:bamboo_fence"
    );

    private static final Map<String, Integer> WOOD_DATA = Map.of(
        "oak", 0,
        "spruce", 1,
        "birch", 2,
        "jungle", 3,
        "acacia", 4,
        "dark_oak", 5
    );

    private Conversion582_575() {
    }

    public static GeyserMappingItem remapItem(@SuppressWarnings("unused") Item item, GeyserMappingItem mapping) {
        String identifier = mapping.getBedrockIdentifier();
        if (identifier == null || !identifier.startsWith("minecraft:")) {
            return mapping;
        }

        if (identifier.endsWith("_armor_trim_smithing_template")
            || identifier.equals("minecraft:netherite_upgrade_smithing_template")) {
            return mapping.withBedrockIdentifier("minecraft:netherite_ingot").withBedrockData(0);
        }

        if (identifier.endsWith("pottery_sherd")) {
            identifier = identifier.replace("pottery_sherd", "pottery_shard");
            mapping = mapping.withBedrockIdentifier(identifier);
        }
        if (identifier.endsWith("pottery_shard") && !KEEP_POTTERY_SHARDS.contains(identifier)) {
            return mapping.withBedrockIdentifier("minecraft:archer_pottery_shard").withBedrockData(0);
        }

        return switch (identifier) {
            case "minecraft:oak_log" -> mapping.withBedrockIdentifier("minecraft:log").withBedrockData(0);
            case "minecraft:spruce_log" -> mapping.withBedrockIdentifier("minecraft:log").withBedrockData(1);
            case "minecraft:birch_log" -> mapping.withBedrockIdentifier("minecraft:log").withBedrockData(2);
            case "minecraft:jungle_log" -> mapping.withBedrockIdentifier("minecraft:log").withBedrockData(3);
            case "minecraft:acacia_log" -> mapping.withBedrockIdentifier("minecraft:log2").withBedrockData(0);
            case "minecraft:dark_oak_log" -> mapping.withBedrockIdentifier("minecraft:log2").withBedrockData(1);
            case "minecraft:oak_fence" -> mapping.withBedrockIdentifier("minecraft:fence").withBedrockData(0);
            case "minecraft:spruce_fence" -> mapping.withBedrockIdentifier("minecraft:fence").withBedrockData(1);
            case "minecraft:birch_fence" -> mapping.withBedrockIdentifier("minecraft:fence").withBedrockData(2);
            case "minecraft:jungle_fence" -> mapping.withBedrockIdentifier("minecraft:fence").withBedrockData(3);
            case "minecraft:acacia_fence" -> mapping.withBedrockIdentifier("minecraft:fence").withBedrockData(4);
            case "minecraft:dark_oak_fence" -> mapping.withBedrockIdentifier("minecraft:fence").withBedrockData(5);
            case "minecraft:cherry_log" -> mapping.withBedrockIdentifier("minecraft:log").withBedrockData(0);
            case "minecraft:cherry_wood" -> mapping.withBedrockIdentifier("minecraft:wood").withBedrockData(0);
            case "minecraft:stripped_cherry_log" -> mapping.withBedrockIdentifier("minecraft:stripped_oak_log");
            case "minecraft:stripped_cherry_wood" -> mapping.withBedrockIdentifier("minecraft:wood").withBedrockData(8);
            case "minecraft:cherry_planks" -> mapping.withBedrockIdentifier("minecraft:planks").withBedrockData(0);
            case "minecraft:cherry_leaves" -> mapping.withBedrockIdentifier("minecraft:leaves").withBedrockData(0);
            case "minecraft:cherry_sapling" -> mapping.withBedrockIdentifier("minecraft:sapling").withBedrockData(0);
            case "minecraft:cherry_slab" -> mapping.withBedrockIdentifier("minecraft:wooden_slab").withBedrockData(0);
            case "minecraft:cherry_double_slab" -> mapping.withBedrockIdentifier("minecraft:double_wooden_slab").withBedrockData(0);
            case "minecraft:cherry_fence" -> mapping.withBedrockIdentifier("minecraft:fence").withBedrockData(0);
            case "minecraft:cherry_fence_gate" -> mapping.withBedrockIdentifier("minecraft:fence_gate");
            case "minecraft:cherry_stairs" -> mapping.withBedrockIdentifier("minecraft:oak_stairs");
            case "minecraft:cherry_button" -> mapping.withBedrockIdentifier("minecraft:wooden_button");
            case "minecraft:cherry_pressure_plate" -> mapping.withBedrockIdentifier("minecraft:wooden_pressure_plate");
            case "minecraft:cherry_door" -> mapping.withBedrockIdentifier("minecraft:wooden_door");
            case "minecraft:cherry_trapdoor" -> mapping.withBedrockIdentifier("minecraft:trapdoor");
            case "minecraft:cherry_sign", "minecraft:cherry_standing_sign" ->
                mapping.withBedrockIdentifier("minecraft:oak_sign");
            case "minecraft:cherry_hanging_sign" -> mapping.withBedrockIdentifier("minecraft:oak_hanging_sign");
            case "minecraft:cherry_boat" -> mapping.withBedrockIdentifier("minecraft:oak_boat");
            case "minecraft:cherry_chest_boat" -> mapping.withBedrockIdentifier("minecraft:oak_chest_boat");
            case "minecraft:pink_petals" -> mapping.withBedrockIdentifier("minecraft:red_flower").withBedrockData(7);
            case "minecraft:calibrated_sculk_sensor" -> mapping.withBedrockIdentifier("minecraft:sculk_sensor");
            case "minecraft:suspicious_gravel" -> mapping.withBedrockIdentifier("minecraft:suspicious_sand");
            default -> mapping;
        };
    }

    public static NbtMap remapBlock(NbtMap tag) {
        tag = unflattenLog(tag);
        tag = unflattenFence(tag);
        tag = remapCherryFamily(tag);
        tag = remapCalibratedSculk(tag);
        tag = remapPinkPetals(tag);
        return remapSuspiciousGravel(tag);
    }

    private static NbtMap unflattenLog(NbtMap tag) {
        String name = tag.getString("name");
        String woodType = switch (name) {
            case "minecraft:oak_log", "minecraft:cherry_log" -> "oak";
            case "minecraft:spruce_log" -> "spruce";
            case "minecraft:birch_log" -> "birch";
            case "minecraft:jungle_log" -> "jungle";
            case "minecraft:acacia_log" -> "acacia";
            case "minecraft:dark_oak_log" -> "dark_oak";
            default -> null;
        };
        if (woodType == null) {
            return tag;
        }
        boolean log2 = "acacia".equals(woodType) || "dark_oak".equals(woodType);
        NbtMapBuilder states = tag.getCompound("states").toBuilder();
        if (log2) {
            states.putString("new_log_type", woodType);
            return tag.toBuilder()
                .putString("name", "minecraft:log2")
                .putCompound("states", states.build())
                .build();
        }
        states.putString("old_log_type", woodType);
        return tag.toBuilder()
            .putString("name", "minecraft:log")
            .putCompound("states", states.build())
            .build();
    }

    private static NbtMap unflattenFence(NbtMap tag) {
        String name = tag.getString("name");
        if (!name.startsWith("minecraft:") || !name.endsWith("_fence") || name.contains("gate")
            || ALREADY_SEPARATE_FENCES.contains(name) || name.equals("minecraft:fence")) {
            return tag;
        }
        String woodType = name.equals("minecraft:cherry_fence")
            ? "oak"
            : name.substring("minecraft:".length(), name.length() - "_fence".length());
        if (!WOOD_DATA.containsKey(woodType)) {
            return tag;
        }
        NbtMapBuilder states = tag.getCompound("states").toBuilder();
        states.putString("wood_type", woodType);
        return tag.toBuilder()
            .putString("name", "minecraft:fence")
            .putCompound("states", states.build())
            .build();
    }

    private static NbtMap remapCherryFamily(NbtMap tag) {
        String name = tag.getString("name");
        return switch (name) {
            case "minecraft:cherry_planks" -> withStringState(tag, "minecraft:planks", "wood_type", "oak");
            case "minecraft:cherry_slab" -> withStringState(tag, "minecraft:wooden_slab", "wood_type", "oak");
            case "minecraft:cherry_double_slab" -> withStringState(tag, "minecraft:double_wooden_slab", "wood_type", "oak");
            case "minecraft:cherry_leaves" -> withStringState(tag, "minecraft:leaves", "old_leaf_type", "oak");
            case "minecraft:cherry_sapling" -> withStringState(tag, "minecraft:sapling", "sapling_type", "oak");
            case "minecraft:cherry_wood" -> withStringState(tag, "minecraft:wood", "wood_type", "oak");
            case "minecraft:stripped_cherry_wood" -> withStrippedOakWood(tag);
            case "minecraft:stripped_cherry_log" -> withName(tag, "stripped_oak_log");
            case "minecraft:cherry_stairs" -> withName(tag, "oak_stairs");
            case "minecraft:cherry_button" -> withName(tag, "wooden_button");
            case "minecraft:cherry_pressure_plate" -> withName(tag, "wooden_pressure_plate");
            case "minecraft:cherry_door" -> withName(tag, "wooden_door");
            case "minecraft:cherry_trapdoor" -> withName(tag, "trapdoor");
            case "minecraft:cherry_fence_gate" -> withName(tag, "fence_gate");
            case "minecraft:cherry_standing_sign" -> withName(tag, "standing_sign");
            case "minecraft:cherry_wall_sign" -> withName(tag, "wall_sign");
            case "minecraft:cherry_hanging_sign", "minecraft:cherry_wall_hanging_sign" -> withName(tag, "oak_hanging_sign");
            default -> tag;
        };
    }

    private static NbtMap withStrippedOakWood(NbtMap tag) {
        NbtMapBuilder states = tag.getCompound("states").toBuilder();
        states.putString("wood_type", "oak");
        states.putBoolean("stripped_bit", true);
        return tag.toBuilder()
            .putString("name", "minecraft:wood")
            .putCompound("states", states.build())
            .build();
    }

    private static NbtMap withStringState(NbtMap tag, String name, String key, String value) {
        NbtMapBuilder states = tag.getCompound("states").toBuilder();
        states.putString(key, value);
        return tag.toBuilder()
            .putString("name", name)
            .putCompound("states", states.build())
            .build();
    }

    private static NbtMap remapCalibratedSculk(NbtMap tag) {
        if (!"minecraft:calibrated_sculk_sensor".equals(tag.getString("name"))) {
            return tag;
        }
        return withName(tag, "sculk_sensor");
    }

    private static NbtMap remapPinkPetals(NbtMap tag) {
        if (!"minecraft:pink_petals".equals(tag.getString("name"))) {
            return tag;
        }
        NbtMapBuilder states = NbtMap.builder();
        states.putString("flower_type", "tulip_pink");
        return NbtMap.builder()
            .putString("name", "minecraft:red_flower")
            .putCompound("states", states.build())
            .build();
    }

    private static NbtMap remapSuspiciousGravel(NbtMap tag) {
        if (!"minecraft:suspicious_gravel".equals(tag.getString("name"))) {
            return tag;
        }
        return withName(tag, "suspicious_sand");
    }
}
