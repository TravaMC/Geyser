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

package org.geysermc.geyser.util;

import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer;
import org.cloudburstmc.protocol.bedrock.data.GameRuleData;
import org.cloudburstmc.protocol.bedrock.data.TrimMaterial;
import org.cloudburstmc.protocol.bedrock.data.TrimPattern;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataMap;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityProperty;
import org.cloudburstmc.protocol.bedrock.data.entity.FloatEntityProperty;
import org.cloudburstmc.protocol.bedrock.data.entity.IntEntityProperty;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.packet.AddEntityPacket;
import org.cloudburstmc.protocol.bedrock.packet.AddItemEntityPacket;
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket;
import org.cloudburstmc.protocol.bedrock.packet.AvailableCommandsPacket;
import org.cloudburstmc.protocol.bedrock.packet.AvailableEntityIdentifiersPacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.BiomeDefinitionListPacket;
import org.cloudburstmc.protocol.bedrock.packet.BlockEntityDataPacket;
import org.cloudburstmc.protocol.bedrock.packet.CameraPresetsPacket;
import org.cloudburstmc.protocol.bedrock.packet.ChunkRadiusUpdatedPacket;
import org.cloudburstmc.protocol.bedrock.packet.ClientboundMapItemDataPacket;
import org.cloudburstmc.protocol.bedrock.packet.CraftingDataPacket;
import org.cloudburstmc.protocol.bedrock.packet.CreativeContentPacket;
import org.cloudburstmc.protocol.bedrock.packet.DimensionDataPacket;
import org.cloudburstmc.protocol.bedrock.packet.GameRulesChangedPacket;
import org.cloudburstmc.protocol.bedrock.packet.InventoryContentPacket;
import org.cloudburstmc.protocol.bedrock.packet.InventorySlotPacket;
import org.cloudburstmc.protocol.bedrock.packet.ItemComponentPacket;
import org.cloudburstmc.protocol.bedrock.packet.LevelChunkPacket;
import org.cloudburstmc.protocol.bedrock.packet.LevelSoundEventPacket;
import org.cloudburstmc.protocol.bedrock.packet.LoginPacket;
import org.cloudburstmc.protocol.bedrock.packet.MobEquipmentPacket;
import org.cloudburstmc.protocol.bedrock.packet.MoveEntityAbsolutePacket;
import org.cloudburstmc.protocol.bedrock.packet.MoveEntityDeltaPacket;
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket;
import org.cloudburstmc.protocol.bedrock.packet.NetworkChunkPublisherUpdatePacket;
import org.cloudburstmc.protocol.bedrock.packet.NetworkSettingsPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayStatusPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket;
import org.cloudburstmc.protocol.bedrock.packet.RemoveEntityPacket;
import org.cloudburstmc.protocol.bedrock.packet.RequestChunkRadiusPacket;
import org.cloudburstmc.protocol.bedrock.packet.RequestNetworkSettingsPacket;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePackClientResponsePacket;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePackStackPacket;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePacksInfoPacket;
import org.cloudburstmc.protocol.bedrock.packet.RespawnPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetCommandsEnabledPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetEntityDataPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetSpawnPositionPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetTimePacket;
import org.cloudburstmc.protocol.bedrock.packet.SetTitlePacket;
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket;
import org.cloudburstmc.protocol.bedrock.packet.SyncEntityPropertyPacket;
import org.cloudburstmc.protocol.bedrock.packet.TextPacket;
import org.cloudburstmc.protocol.bedrock.packet.TickSyncPacket;
import org.cloudburstmc.protocol.bedrock.packet.TrimDataPacket;
import org.cloudburstmc.protocol.bedrock.packet.UnlockedRecipesPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateAdventureSettingsPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateAttributesPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateBlockPacket;
import org.cloudburstmc.protocol.bedrock.packet.VoxelShapesPacket;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.StringJoiner;

/**
 * Formats Bedrock packets for join-sequence crash debugging.
 * Keeps a short ring buffer so disconnect can print the last packets sent.
 */
public final class JoinOutboundPacketDump {
    private static final int RECENT_LIMIT = 48;
    private static final int DETAIL_LIMIT = 1800;

    private final ArrayDeque<String> recent = new ArrayDeque<>(RECENT_LIMIT);
    private int outboundCount;

    public String describe(BedrockPacket packet) {
        return describe(packet, true);
    }

    public String describeInbound(BedrockPacket packet) {
        return describe(packet, false);
    }

    private String describe(BedrockPacket packet, boolean outbound) {
        String name = packet.getClass().getSimpleName();
        String detail = detail(packet);
        String line = detail.isEmpty() ? name : name + " " + detail;
        remember(line);
        if (outbound) {
            outboundCount++;
        }
        return line;
    }

    public int outboundCount() {
        return outboundCount;
    }

    public String recentSummary() {
        if (recent.isEmpty()) {
            return "(none)";
        }
        StringJoiner joiner = new StringJoiner(" | ");
        for (String entry : recent) {
            joiner.add(entry);
        }
        return joiner.toString();
    }

    private void remember(String line) {
        if (recent.size() >= RECENT_LIMIT) {
            recent.removeFirst();
        }
        recent.addLast(line);
    }

    private static String detail(BedrockPacket packet) {
        return switch (packet) {
            case AddEntityPacket p -> {
                StringBuilder sb = new StringBuilder();
                sb.append("id=").append(p.getIdentifier())
                    .append(" runtime=").append(p.getRuntimeEntityId())
                    .append(" unique=").append(p.getUniqueEntityId())
                    .append(" pos=").append(fmt(p.getPosition()))
                    .append(" rot=").append(p.getRotation())
                    .append(" meta=").append(metaSummary(p.getMetadata()));
                appendProperties(sb, p.getProperties().getIntProperties(), p.getProperties().getFloatProperties());
                yield sb.toString();
            }
            case AddItemEntityPacket p -> "runtime=" + p.getRuntimeEntityId()
                + " item=" + itemSummary(p.getItemInHand())
                + " pos=" + fmt(p.getPosition());
            case AddPlayerPacket p -> "name=" + p.getUsername()
                + " uuid=" + p.getUuid()
                + " runtime=" + p.getRuntimeEntityId()
                + " pos=" + fmt(p.getPosition())
                + " meta=" + metaSummary(p.getMetadata());
            case SetEntityDataPacket p -> {
                StringBuilder sb = new StringBuilder();
                sb.append("runtime=").append(p.getRuntimeEntityId())
                    .append(" tick=").append(p.getTick())
                    .append(" meta=").append(metaSummary(p.getMetadata()));
                appendProperties(sb, p.getProperties().getIntProperties(), p.getProperties().getFloatProperties());
                yield sb.toString();
            }
            case SetEntityMotionPacket p -> "runtime=" + p.getRuntimeEntityId()
                + " motion=" + fmt(p.getMotion())
                + " tick=" + p.getTick();
            case LevelChunkPacket p -> "chunk=" + p.getChunkX() + "," + p.getChunkZ()
                + " sub=" + p.getSubChunksLength()
                + " cache=" + p.isCachingEnabled()
                + " bytes=" + (p.getData() == null ? 0 : p.getData().readableBytes());
            case CraftingDataPacket p -> "recipes=" + p.getCraftingData().size()
                + " potions=" + p.getPotionMixData().size()
                + " containers=" + p.getContainerMixData().size()
                + " clear=" + p.isCleanRecipes()
                + " recipeTypes=" + recipeTypes(p);
            case UnlockedRecipesPacket p -> "action=" + p.getAction()
                + " count=" + p.getUnlockedRecipes().size()
                + sample(p.getUnlockedRecipes(), 8);
            case InventoryContentPacket p -> "container=" + p.getContainerId()
                + " slots=" + p.getContents().size()
                + " sample=" + itemListSample(p.getContents(), 6);
            case InventorySlotPacket p -> "container=" + p.getContainerId()
                + " slot=" + p.getSlot()
                + " item=" + itemSummary(p.getItem());
            case LevelSoundEventPacket p -> "sound=" + p.getSound()
                + " id=" + p.getIdentifier()
                + " extra=" + p.getExtraData()
                + " pos=" + fmt(p.getPosition());
            case MobEquipmentPacket p -> "runtime=" + p.getRuntimeEntityId()
                + " slot=" + p.getInventorySlot()
                + " hotbar=" + p.getHotbarSlot()
                + " item=" + itemSummary(p.getItem());
            case MoveEntityAbsolutePacket p -> "runtime=" + p.getRuntimeEntityId()
                + " pos=" + fmt(p.getPosition())
                + " onGround=" + p.isOnGround();
            case MoveEntityDeltaPacket p -> "runtime=" + p.getRuntimeEntityId()
                + " flags=" + p.getFlags();
            case MovePlayerPacket p -> "runtime=" + p.getRuntimeEntityId()
                + " mode=" + p.getMode()
                + " pos=" + fmt(p.getPosition())
                + " rot=" + p.getRotation()
                + " onGround=" + p.isOnGround()
                + " riding=" + p.getRidingRuntimeEntityId()
                + " tick=" + p.getTick();
            case UpdateAttributesPacket p -> "runtime=" + p.getRuntimeEntityId()
                + " attrs=" + p.getAttributes().size()
                + " names=" + attrNames(p);
            case RemoveEntityPacket p -> "unique=" + p.getUniqueEntityId();
            case PlayerListPacket p -> "action=" + p.getAction()
                + " entries=" + p.getEntries().size()
                + " names=" + playerListNames(p);
            case AvailableCommandsPacket p -> "commands=" + p.getCommands().size();
            case TextPacket p -> "type=" + p.getType()
                + " needsTrans=" + p.isNeedsTranslation()
                + " source=" + p.getSourceName()
                + " msg=" + truncate(p.getMessage(), 200)
                + " xuid=" + p.getXuid();
            case RespawnPacket p -> "state=" + p.getState()
                + " runtime=" + p.getRuntimeEntityId()
                + " pos=" + fmt(p.getPosition());
            case PlayStatusPacket p -> "status=" + p.getStatus();
            case NetworkSettingsPacket p -> "algo=" + p.getCompressionAlgorithm()
                + " threshold=" + p.getCompressionThreshold()
                + " clientThrottle=" + p.isClientThrottleEnabled();
            case ResourcePacksInfoPacket p -> "packs=" + p.getResourcePackInfos().size()
                + " force=" + p.isForcedToAccept()
                + " template=" + p.getWorldTemplateId()
                + " ids=" + packIds(p);
            case ResourcePackStackPacket p -> "packs=" + p.getResourcePacks().size()
                + " experiments=" + p.getExperiments().size()
                + " gameVersion=" + p.getGameVersion()
                + " experimentsPreviouslyToggled=" + p.isExperimentsPreviouslyToggled();
            case ResourcePackClientResponsePacket p -> "status=" + p.getStatus()
                + " ids=" + p.getPackIds();
            case StartGamePacket p -> "dim=" + p.getDimensionId()
                + " gamemode=" + p.getPlayerGameType()
                + " movement=" + p.getAuthoritativeMovementMode()
                + " items=" + p.getItemDefinitions().size()
                + " blocks=" + p.getBlockProperties().size()
                + " vanilla=" + p.getVanillaVersion()
                + " experiments=" + p.getExperiments().size()
                + " template=" + p.getWorldTemplateId()
                + " hashed=" + p.isBlockNetworkIdsHashed()
                + " authBB=" + p.isServerAuthoritativeBlockBreaking()
                + " unique=" + p.getUniqueEntityId()
                + " runtime=" + p.getRuntimeEntityId()
                + " seed=" + p.getSeed()
                + " pos=" + fmt(p.getPlayerPosition())
                + " inventoriesAuth=" + p.isInventoriesServerAuthoritative();
            case ItemComponentPacket p -> "items=" + p.getItems().size()
                + " sample=" + sampleObjects(p.getItems(), 12);
            case BiomeDefinitionListPacket p -> "hasBiomes=" + (p.getBiomes() != null)
                + " hasNbt=" + (p.getDefinitions() != null)
                + " nbtKeys=" + (p.getDefinitions() == null ? 0 : p.getDefinitions().size());
            case CreativeContentPacket p -> "contents=" + p.getContents().size()
                + " groups=" + p.getGroups().size()
                + " sample=" + creativeSample(p, 10);
            case AvailableEntityIdentifiersPacket p -> "nbtKeys=" + (p.getIdentifiers() == null ? 0 : p.getIdentifiers().size())
                + " nbt=" + nbtBrief(p.getIdentifiers());
            case CameraPresetsPacket p -> "presets=" + p.getPresets().size();
            case DimensionDataPacket p -> "defs=" + p.getDefinitions().size();
            case VoxelShapesPacket p -> "shapes=" + (p.getShapes() == null ? 0 : p.getShapes().size());
            case LoginPacket p -> "protocol=" + p.getProtocolVersion()
                + " auth=" + (p.getAuthPayload() == null ? "null" : p.getAuthPayload().getClass().getSimpleName());
            case RequestNetworkSettingsPacket p -> "protocol=" + p.getProtocolVersion();
            case RequestChunkRadiusPacket p -> "radius=" + p.getRadius()
                + " max=" + p.getMaxRadius();
            case ChunkRadiusUpdatedPacket p -> "radius=" + p.getRadius();
            case TickSyncPacket p -> "req=" + p.getRequestTimestamp()
                + " resp=" + p.getResponseTimestamp();
            case TrimDataPacket p -> "patterns=" + p.getPatterns().size()
                + " materials=" + p.getMaterials().size()
                + " patternIds=" + trimPatternIds(p)
                + " materialIds=" + trimMaterialIds(p);
            case SyncEntityPropertyPacket p -> nbtBrief(p.getData());
            case GameRulesChangedPacket p -> "rules=" + gameRules(p);
            case UpdateAbilitiesPacket p -> "unique=" + p.getUniqueEntityId()
                + " playerPerm=" + p.getPlayerPermission()
                + " commandPerm=" + p.getCommandPermission()
                + " layers=" + abilityLayers(p);
            case UpdateAdventureSettingsPacket p -> "noPvM=" + p.isNoPvM()
                + " noMvP=" + p.isNoMvP()
                + " immutable=" + p.isImmutableWorld()
                + " showNameTags=" + p.isShowNameTags()
                + " autoJump=" + p.isAutoJump();
            case NetworkChunkPublisherUpdatePacket p -> "pos=" + fmt(p.getPosition())
                + " radius=" + p.getRadius();
            case ClientboundMapItemDataPacket p -> "mapId=" + p.getUniqueMapId()
                + " scale=" + p.getScale()
                + " locked=" + p.isLocked()
                + " origin=" + p.getOrigin()
                + " pixels=" + (p.getColors() == null ? 0 : p.getColors().length);
            case BlockEntityDataPacket p -> "pos=" + fmt(p.getBlockPosition())
                + " nbt=" + nbtBrief(p.getData());
            case UpdateBlockPacket p -> "pos=" + fmt(p.getBlockPosition())
                + " def=" + p.getDefinition()
                + " flags=" + p.getFlags()
                + " layer=" + p.getDataLayer();
            case SetTitlePacket p -> "type=" + p.getType()
                + " text=" + truncate(p.getText(), 160)
                + " fadeIn=" + p.getFadeInTime()
                + " stay=" + p.getStayTime()
                + " fadeOut=" + p.getFadeOutTime()
                + " xuid=" + p.getXuid();
            case SetSpawnPositionPacket p -> "type=" + p.getSpawnType()
                + " pos=" + fmt(p.getBlockPosition())
                + " dim=" + p.getDimensionId()
                + " causing=" + fmt(p.getSpawnPosition());
            case SetCommandsEnabledPacket p -> "enabled=" + p.isCommandsEnabled();
            case SetTimePacket p -> "time=" + p.getTime();
            default -> compact(packet);
        };
    }

    private static String compact(BedrockPacket packet) {
        String text = String.valueOf(packet);
        String simple = packet.getClass().getSimpleName();
        String fqcn = packet.getClass().getName();
        if (text.equals(simple) || text.equals(fqcn) || text.startsWith(fqcn + "@")) {
            return "";
        }
        return truncate(text.replace('\n', ' '), DETAIL_LIMIT);
    }

    private static void appendProperties(StringBuilder sb, Collection<IntEntityProperty> ints,
                                         Collection<FloatEntityProperty> floats) {
        if ((ints == null || ints.isEmpty()) && (floats == null || floats.isEmpty())) {
            return;
        }
        sb.append(" propsInt=").append(formatProps(ints));
        sb.append(" propsFloat=").append(formatProps(floats));
    }

    private static String formatProps(Collection<? extends EntityProperty> props) {
        if (props == null || props.isEmpty()) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(props.size()).append('{');
        boolean first = true;
        for (EntityProperty prop : props) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append(prop.getIndex()).append('=');
            if (prop instanceof IntEntityProperty intProp) {
                sb.append(intProp.getValue());
            } else if (prop instanceof FloatEntityProperty floatProp) {
                sb.append(floatProp.getValue());
            } else {
                sb.append('?');
            }
        }
        sb.append('}');
        return sb.toString();
    }

    private static String metaSummary(EntityDataMap metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "0";
        }
        StringJoiner joiner = new StringJoiner(",", metadata.size() + "{", "}");
        int n = 0;
        for (var entry : metadata.entrySet()) {
            if (n++ >= 20) {
                joiner.add("...+" + (metadata.size() - 20));
                break;
            }
            joiner.add(String.valueOf(entry.getKey()) + "=" + truncate(String.valueOf(entry.getValue()), 48));
        }
        return joiner.toString();
    }

    private static String itemSummary(ItemData item) {
        if (item == null || item == ItemData.AIR) {
            return "air";
        }
        String def = item.getDefinition() == null ? "?" : item.getDefinition().getIdentifier();
        return def + "*" + item.getCount() + (item.getDamage() != 0 ? "#" + item.getDamage() : "");
    }

    private static String itemListSample(Collection<ItemData> items, int limit) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        int n = 0;
        for (ItemData item : items) {
            if (n++ >= limit) {
                joiner.add("...+" + (items.size() - limit));
                break;
            }
            joiner.add(itemSummary(item));
        }
        return joiner.toString();
    }

    private static String packIds(ResourcePacksInfoPacket packet) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (var info : packet.getResourcePackInfos()) {
            joiner.add(String.valueOf(info));
        }
        return truncate(joiner.toString(), 400);
    }

    private static String sampleObjects(Iterable<?> values, int limit) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        int n = 0;
        int sizeHint = 0;
        for (Object value : values) {
            sizeHint++;
            if (n++ >= limit) {
                continue;
            }
            if (value instanceof ItemDefinition definition) {
                joiner.add(definition.getIdentifier() + "(" + definition.getRuntimeId() + ")");
            } else {
                joiner.add(truncate(String.valueOf(value), 80));
            }
        }
        if (sizeHint > limit) {
            joiner.add("...+" + (sizeHint - limit));
        }
        return joiner.toString();
    }

    private static String creativeSample(CreativeContentPacket packet, int limit) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        int n = 0;
        for (var entry : packet.getContents()) {
            if (n++ >= limit) {
                joiner.add("...+" + (packet.getContents().size() - limit));
                break;
            }
            joiner.add(itemSummary(entry.getItem()));
        }
        return joiner.toString();
    }

    private static String recipeTypes(CraftingDataPacket packet) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        int n = 0;
        for (var recipe : packet.getCraftingData()) {
            if (n++ >= 12) {
                joiner.add("...");
                break;
            }
            joiner.add(String.valueOf(recipe.getType()));
        }
        return joiner.toString();
    }

    private static String attrNames(UpdateAttributesPacket packet) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (var attr : packet.getAttributes()) {
            joiner.add(attr.getName() + "=" + attr.getValue());
        }
        return joiner.toString();
    }

    private static String playerListNames(PlayerListPacket packet) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        int n = 0;
        for (var entry : packet.getEntries()) {
            if (n++ >= 8) {
                joiner.add("...");
                break;
            }
            joiner.add(entry.getName() + "/" + entry.getUuid());
        }
        return joiner.toString();
    }

    private static String trimPatternIds(TrimDataPacket packet) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (TrimPattern pattern : packet.getPatterns()) {
            joiner.add(pattern.getItemName() + "=" + pattern.getPatternId());
        }
        return truncate(joiner.toString(), 400);
    }

    private static String trimMaterialIds(TrimDataPacket packet) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (TrimMaterial material : packet.getMaterials()) {
            joiner.add(material.getItemName() + "=" + material.getMaterialId());
        }
        return truncate(joiner.toString(), 400);
    }

    private static String gameRules(GameRulesChangedPacket packet) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (GameRuleData<?> rule : packet.getGameRules()) {
            joiner.add(rule.getName() + "=" + rule.getValue());
        }
        return joiner.toString();
    }

    private static String abilityLayers(UpdateAbilitiesPacket packet) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (AbilityLayer layer : packet.getAbilityLayers()) {
            joiner.add(layer.getLayerType() + " walk=" + layer.getWalkSpeed() + " fly=" + layer.getFlySpeed()
                + " abilities=" + layer.getAbilitiesSet());
        }
        return joiner.toString();
    }

    private static String nbtBrief(NbtMap nbt) {
        if (nbt == null) {
            return "null";
        }
        String id = nbt.getString("id", nbt.getString("type", ""));
        return "keys=" + nbt.size() + (id.isEmpty() ? "" : " id=" + id) + " " + truncate(nbt.toString(), 500);
    }

    private static String fmt(Vector3f v) {
        if (v == null) {
            return "null";
        }
        return String.format("%.2f,%.2f,%.2f", v.getX(), v.getY(), v.getZ());
    }

    private static String fmt(Vector3i v) {
        if (v == null) {
            return "null";
        }
        return v.getX() + "," + v.getY() + "," + v.getZ();
    }

    private static String sample(Iterable<String> values, int limit) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        int n = 0;
        for (String value : values) {
            if (n++ >= limit) {
                joiner.add("...");
                break;
            }
            joiner.add(value);
        }
        return joiner.toString();
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "null";
        }
        String flattened = value.replace('\n', ' ');
        if (flattened.length() <= max) {
            return flattened;
        }
        return flattened.substring(0, max) + "…";
    }
}
