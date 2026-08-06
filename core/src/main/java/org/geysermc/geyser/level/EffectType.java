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

package org.geysermc.geyser.level;

import lombok.Getter;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;

@Getter
public enum EffectType {
    NONE(0, null, 0x000000, Integer.MAX_VALUE),

    SPEED(1, Effect.SPEED, 0x33EBFF, 0),
    SLOWNESS(2, Effect.SLOWNESS, 0x8BAFE0, 0),
    HASTE(3, Effect.HASTE, 0xD9C043, 0),
    MINING_FATIGUE(4, Effect.MINING_FATIGUE, 0x4A4217, 0),
    STRENGTH(5, Effect.STRENGTH, 0xFFC700, 0),
    INSTANT_HEALTH(6, Effect.INSTANT_HEALTH, 0xF82423, 0),
    INSTANT_DAMAGE(7, Effect.INSTANT_DAMAGE, 0xA9656A, 0),
    JUMP_BOOST(8, Effect.JUMP_BOOST, 0xFDFF84, 0),
    NAUSEA(9, Effect.NAUSEA, 0x551D4A, 0),
    REGENERATION(10, Effect.REGENERATION, 0xCD5CAB, 0),
    RESISTANCE(11, Effect.RESISTANCE, 0x9146F0, 0),
    FIRE_RESISTANCE(12, Effect.FIRE_RESISTANCE, 0xFF9900, 0),
    WATER_BREATHING(13, Effect.WATER_BREATHING, 0x98DAC0, 0),
    INVISIBILITY(14, Effect.INVISIBILITY, 0xF6F6F6, 0),
    BLINDNESS(15, Effect.BLINDNESS, 0x1F1F23, 0),
    NIGHT_VISION(16, Effect.NIGHT_VISION, 0xC2FF66, 0),
    HUNGER(17, Effect.HUNGER, 0x587653, 0),
    WEAKNESS(18, Effect.WEAKNESS, 0x484D48, 0),
    POISON(19, Effect.POISON, 0x87A363, 0),
    WITHER(20, Effect.WITHER, 0x736156, 0),
    HEALTH_BOOST(21, Effect.HEALTH_BOOST, 0xF87D23, 0),
    ABSORPTION(22, Effect.ABSORPTION, 0x2552A5, 0),
    SATURATION(23, Effect.SATURATION, 0xF82423, 0),
    LEVITATION(24, Effect.LEVITATION, 0xCEFFFF, 0),
    FATAL_POISON(25, null, 0x4E9331, 0), // Bedrock-exclusive effect, maybe useful later if we map to closest color
    CONDUIT_POWER(26, Effect.CONDUIT_POWER, 0x1DC2D1, 0),
    SLOW_FALLING(27, Effect.SLOW_FALLING, 0xF3CFB9, 0),
    BAD_OMEN(28, Effect.BAD_OMEN, 0x0B6138, 0),
    HERO_OF_THE_VILLAGE(29, Effect.HERO_OF_THE_VILLAGE, 0x44FF44, 0),
    DARKNESS(30, Effect.DARKNESS, 0x292721, 0),
    // Bedrock 1.21.0 (685) — Tricky Trials
    TRIAL_OMEN(31, Effect.TRIAL_OMEN, 0x16A6A6, 685),
    WIND_CHARGED(32, Effect.WIND_CHARGED, 0xBDC9FF, 685),
    WEAVING(33, Effect.WEAVING, 0x78695A, 685),
    OOZING(34, Effect.OOZING, 0x99FFA3, 685),
    INFESTED(35, Effect.INFESTED, 0x8C9B8C, 685),
    RAID_OMEN(36, Effect.RAID_OMEN, 0xDE4058, 685),
    // Bedrock 1.21.130 (898) — Mounts of Mayhem
    BREATH_OF_THE_NAUTILUS(37, Effect.BREATH_OF_THE_NAUTILUS, 0x00FFEE, 898),

    // Java-exclusive effects — no Bedrock ID; never send MobEffectPacket
    GLOWING(0, Effect.GLOWING, 0x94A061, Integer.MAX_VALUE),
    LUCK(0, Effect.LUCK, 0x59C106, Integer.MAX_VALUE),
    BAD_LUCK(0, Effect.UNLUCK, 0xC0A44D, Integer.MAX_VALUE),
    DOLPHINS_GRACE(0, Effect.DOLPHINS_GRACE, 0x88A3BE, Integer.MAX_VALUE);

    private final int bedrockId;
    private final Effect javaEffect;
    private final int color;
    /**
     * Minimum Bedrock protocol that understands {@link #bedrockId}.
     * Unknown effect IDs disconnect older clients ("broken packet").
     */
    private final int minProtocolVersion;

    EffectType(int bedrockId, Effect javaEffect, int color, int minProtocolVersion) {
        this.bedrockId = bedrockId;
        this.javaEffect = javaEffect;
        this.color = color;
        this.minProtocolVersion = minProtocolVersion;
    }

    /**
     * Whether this effect can be sent in {@code MobEffectPacket} / visible-effect metadata
     * to a client on {@code protocolVersion}.
     */
    public boolean isSupportedOn(int protocolVersion) {
        return bedrockId > 0 && protocolVersion >= minProtocolVersion;
    }

    public static EffectType fromJavaEffect(Effect effect) {
        for (EffectType type : values()) {
            if (type.getJavaEffect() == effect) {
                return type;
            }
        }
        return EffectType.NONE;
    }

    public static EffectType fromColor(int color) {
        color = color & 0xFFFFFF; // Ignore alpha channel
        for (EffectType type : values()) {
            if (type.getColor() == color) {
                return type;
            }
        }
        return EffectType.NONE;
    }
}
