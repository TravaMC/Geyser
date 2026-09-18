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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Downgrades Bedrock 1.20.0 (589) identifiers/states to the 1.19.80/1.19.83 (582) palette.
 * Reverse of Geyser 2.1.1's 1.19.80 {@code legacyMapper}: carpets/corals were still meta,
 * pumpkins used {@code direction}, both sculk sensors used {@code powered_bit},
 * sniffer egg / pitcher plants did not exist as standalone blocks.
 */
public final class Conversion589_582 extends ConversionHelper {

    private static final Set<String> PUMPKIN_DIRECTION_BLOCKS = Set.of(
        "minecraft:pumpkin",
        "minecraft:carved_pumpkin",
        "minecraft:lit_pumpkin"
    );

    private static final Map<String, Integer> CARDINAL_TO_PUMPKIN_DIRECTION = Map.of(
        "south", 0,
        "west", 1,
        "north", 2,
        "east", 3
    );

    private static final Map<String, String> CORAL_TO_COLOR = Map.of(
        "tube", "blue",
        "brain", "pink",
        "bubble", "purple",
        "fire", "red",
        "horn", "yellow"
    );

    private static final Map<String, Integer> CORAL_TO_DATA = Map.of(
        "tube", 0,
        "brain", 1,
        "bubble", 2,
        "fire", 3,
        "horn", 4
    );

    private static final Map<String, Integer> COLOR_TO_DATA = new LinkedHashMap<>();
    private static final Map<String, String> COLOR_TO_LEGACY = new LinkedHashMap<>();

    static {
        String[] colors = {
            "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
            "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
        };
        for (int i = 0; i < colors.length; i++) {
            String color = colors[i];
            COLOR_TO_DATA.put(color, i);
            COLOR_TO_LEGACY.put(color, color.equals("light_gray") ? "silver" : color);
        }
    }

    private Conversion589_582() {
    }

    private static boolean isTrialPottery(String identifier) {
        if (identifier == null) {
            return false;
        }
        return identifier.endsWith("flow_pottery_sherd") || identifier.endsWith("flow_pottery_shard")
            || identifier.endsWith("guster_pottery_sherd") || identifier.endsWith("guster_pottery_shard")
            || identifier.endsWith("scrape_pottery_sherd") || identifier.endsWith("scrape_pottery_shard");
    }

    public static GeyserMappingItem remapItem(Item item, GeyserMappingItem mapping) {
        String javaId = item.javaIdentifier();
        // 1.21 trial sherds are not in the 1.19.80 dump. Do not rebuild the Bedrock id from the
        // Java name (that turns a forPre685 angler fallback back into flow_pottery_shard).
        if (isTrialPottery(javaId) || isTrialPottery(mapping.getBedrockIdentifier())) {
            return mapping.withBedrockIdentifier("minecraft:angler_pottery_shard");
        }

        String identifier = mapping.getBedrockIdentifier();
        if (identifier != null && identifier.endsWith("pottery_sherd")) {
            mapping = mapping.withBedrockIdentifier(identifier.replace("pottery_sherd", "pottery_shard"));
            identifier = mapping.getBedrockIdentifier();
        } else if (javaId.endsWith("pottery_sherd")) {
            mapping = mapping.withBedrockIdentifier(javaId.replace("pottery_sherd", "pottery_shard"));
            identifier = mapping.getBedrockIdentifier();
        }

        if (identifier == null || !identifier.startsWith("minecraft:")) {
            return mapping;
        }
        String path = identifier.substring("minecraft:".length());

        if (path.endsWith("_carpet") && !identifier.equals("minecraft:moss_carpet")) {
            String color = path.substring(0, path.length() - "_carpet".length());
            Integer data = COLOR_TO_DATA.get(color);
            if (data != null) {
                return mapping.withBedrockIdentifier("minecraft:carpet").withBedrockData(data);
            }
        }

        boolean deadCoral = path.startsWith("dead_") && path.endsWith("_coral") && !path.contains("fan") && !path.contains("block");
        boolean liveCoral = !path.startsWith("dead_") && path.endsWith("_coral") && !path.contains("fan") && !path.contains("block")
            && !identifier.equals("minecraft:coral");
        if (deadCoral || liveCoral) {
            String species = deadCoral ? path.substring("dead_".length(), path.length() - "_coral".length())
                : path.substring(0, path.length() - "_coral".length());
            Integer data = CORAL_TO_DATA.get(species);
            if (data != null) {
                int value = deadCoral ? data + 8 : data;
                return mapping.withBedrockIdentifier("minecraft:coral").withBedrockData(value);
            }
        }

        return mapping;
    }

    public static NbtMap remapBlock(NbtMap tag) {
        tag = unflattenCarpet(tag);
        tag = unflattenCoral(tag);
        tag = remapPumpkinDirection(tag);
        tag = remapSculkSensorPhase(tag);
        tag = remapSnifferEgg(tag);
        tag = remapPitcherPlant(tag);
        return remapPitcherCrop(tag);
    }

    private static NbtMap unflattenCarpet(NbtMap tag) {
        String name = tag.getString("name");
        if (!name.startsWith("minecraft:") || !name.endsWith("_carpet") || name.equals("minecraft:moss_carpet")) {
            return tag;
        }
        String color = name.substring("minecraft:".length(), name.length() - "_carpet".length());
        String legacyColor = COLOR_TO_LEGACY.get(color);
        if (legacyColor == null) {
            return tag;
        }
        NbtMapBuilder states = tag.getCompound("states").toBuilder();
        states.putString("color", legacyColor);
        return tag.toBuilder()
            .putString("name", "minecraft:carpet")
            .putCompound("states", states.build())
            .build();
    }

    private static NbtMap unflattenCoral(NbtMap tag) {
        String name = tag.getString("name");
        if (!name.startsWith("minecraft:") || name.equals("minecraft:coral")
            || name.contains("fan") || name.contains("block") || !name.endsWith("_coral")) {
            return tag;
        }
        String path = name.substring("minecraft:".length());
        boolean dead = path.startsWith("dead_");
        String species = dead ? path.substring("dead_".length(), path.length() - "_coral".length())
            : path.substring(0, path.length() - "_coral".length());
        String coralColor = CORAL_TO_COLOR.get(species);
        if (coralColor == null) {
            return tag;
        }
        NbtMapBuilder states = tag.getCompound("states").toBuilder();
        states.putString("coral_color", coralColor);
        states.putBoolean("dead_bit", dead);
        return tag.toBuilder()
            .putString("name", "minecraft:coral")
            .putCompound("states", states.build())
            .build();
    }

    private static NbtMap remapPumpkinDirection(NbtMap tag) {
        String name = tag.getString("name");
        if (!PUMPKIN_DIRECTION_BLOCKS.contains(name)) {
            return tag;
        }
        NbtMap states = tag.getCompound("states");
        if (!states.containsKey("minecraft:cardinal_direction")) {
            return tag;
        }
        Integer direction = CARDINAL_TO_PUMPKIN_DIRECTION.get(states.getString("minecraft:cardinal_direction"));
        if (direction == null) {
            return tag;
        }
        NbtMapBuilder statesBuilder = states.toBuilder();
        statesBuilder.remove("minecraft:cardinal_direction");
        statesBuilder.putInt("direction", direction);
        return tag.toBuilder().putCompound("states", statesBuilder.build()).build();
    }

    private static NbtMap remapSculkSensorPhase(NbtMap tag) {
        // 1.19.80 uses powered_bit for both sculk_sensor and calibrated_sculk_sensor.
        // Geyser 2.1.1 matched endsWith("sculk_sensor"); a name-equals miss leaves hundreds
        // of Java states unmapped (4 facing × 16 power × 3 phase × 2 waterlogged).
        String name = tag.getString("name");
        if (!name.endsWith("sculk_sensor")) {
            return tag;
        }
        NbtMap states = tag.getCompound("states");
        if (!states.containsKey("sculk_sensor_phase")) {
            return tag;
        }
        Object phaseObj = states.get("sculk_sensor_phase");
        int phase = phaseObj instanceof Number number ? number.intValue() : 0;
        NbtMapBuilder statesBuilder = states.toBuilder();
        statesBuilder.remove("sculk_sensor_phase");
        statesBuilder.putBoolean("powered_bit", phase != 0);
        return tag.toBuilder().putCompound("states", statesBuilder.build()).build();
    }

    private static NbtMap remapSnifferEgg(NbtMap tag) {
        if (!"minecraft:sniffer_egg".equals(tag.getString("name"))) {
            return tag;
        }
        return withoutStates("minecraft:dragon_egg");
    }

    private static NbtMap remapPitcherPlant(NbtMap tag) {
        if (!"minecraft:pitcher_plant".equals(tag.getString("name"))) {
            return tag;
        }
        NbtMapBuilder states = NbtMap.builder();
        states.putString("double_plant_type", "sunflower");
        Object upper = tag.getCompound("states").get("upper_block_bit");
        if (upper instanceof Boolean bool) {
            states.putBoolean("upper_block_bit", bool);
        } else if (upper instanceof Byte b) {
            states.putBoolean("upper_block_bit", b != 0);
        } else {
            states.putBoolean("upper_block_bit", false);
        }
        return NbtMap.builder()
            .putString("name", "minecraft:double_plant")
            .putCompound("states", states.build())
            .build();
    }

    private static NbtMap remapPitcherCrop(NbtMap tag) {
        if (!"minecraft:pitcher_crop".equals(tag.getString("name"))) {
            return tag;
        }
        NbtMap states = tag.getCompound("states");
        Object upper = states.get("upper_block_bit");
        boolean upperBit = upper instanceof Byte b ? b != 0
            : upper instanceof Boolean bool && bool;
        if (upperBit) {
            NbtMapBuilder flower = NbtMap.builder();
            flower.putString("flower_type", "orchid");
            return NbtMap.builder()
                .putString("name", "minecraft:red_flower")
                .putCompound("states", flower.build())
                .build();
        }
        NbtMapBuilder pot = NbtMap.builder();
        pot.putBoolean("update_bit", false);
        return NbtMap.builder()
            .putString("name", "minecraft:flower_pot")
            .putCompound("states", pot.build())
            .build();
    }
}
