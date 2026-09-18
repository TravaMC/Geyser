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

import org.cloudburstmc.protocol.bedrock.util.ChainValidationResult;
import org.geysermc.geyser.session.auth.BedrockClientData;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * Reads Xbox identity out of a Bedrock login without depending on Cloudburst's
 * {@code extraData.XUID} being a JSON string.
 *
 * <p>Pre-1.21.90 clients send a certificate chain. Cloudburst's
 * {@code identityClaims()} requires {@code extraData.XUID} to be a {@link String};
 * 1.19.80–1.21.80 dumps often store it as a number, as {@code xuid}/{@code xid},
 * or only on the client JWT as {@code PlatformOnlineId}. That throws
 * {@code XUID node is missing} (generic disconnect) or never fills AuthData,
 * after which Geyser treats the player as unsigned Xbox and kicks with
 * {@code geyser.network.remote.invalid_xbox_account}.
 *
 * <p>Token logins (protocol 818+) put {@code xid}/{@code xname}/{@code cpk}/{@code mid}
 * at the JWT root instead of nested {@code extraData}.
 */
public final class XboxIdentity {
    private XboxIdentity() {
    }

    public record Parsed(String displayName, UUID identity, String xuid, String minecraftId) {
    }

    public static Parsed parse(ChainValidationResult result, BedrockClientData clientData) {
        Map<String, Object> raw = result.rawIdentityClaims();
        Map<String, Object> extra = extraMap(raw);

        String displayName = firstString(extra, "displayName", "xname");
        if (isBlank(displayName)) {
            displayName = firstString(raw, "displayName", "xname");
        }
        if (isBlank(displayName) && clientData != null) {
            displayName = clientData.getUsername();
        }
        if (isBlank(displayName)) {
            displayName = "BedrockPlayer";
        }

        String xuid = firstXuid(extra, "XUID", "xuid", "xid");
        if (!isRealXuid(xuid)) {
            xuid = firstXuid(raw, "XUID", "xuid", "xid");
        }
        if (!isRealXuid(xuid) && clientData != null) {
            String platform = clientData.getPlatformOnlineId();
            if (isRealXuid(platform)) {
                xuid = platform.trim();
            }
        }
        if (xuid == null) {
            xuid = "";
        }

        UUID identity = parseUuid(firstString(extra, "identity"));
        if (identity == null) {
            identity = parseUuid(firstString(raw, "identity"));
        }
        if (identity == null && isRealXuid(xuid)) {
            identity = UUID.nameUUIDFromBytes(("pocket-auth-1-xuid:" + xuid).getBytes(StandardCharsets.UTF_8));
        }
        if (identity == null) {
            identity = new UUID(0L, 0L);
        }

        String minecraftId = firstString(extra, "minecraftId", "mid");
        if (isBlank(minecraftId)) {
            minecraftId = firstString(raw, "minecraftId", "mid");
        }

        return new Parsed(displayName, identity, xuid, isBlank(minecraftId) ? null : minecraftId);
    }

    public static String identityPublicKey(ChainValidationResult result) {
        Map<String, Object> raw = result.rawIdentityClaims();
        String key = firstString(raw, "identityPublicKey", "cpk");
        if (isBlank(key)) {
            throw new IllegalStateException("identityPublicKey node is missing");
        }
        return key;
    }

    public static boolean isRealXuid(String xuid) {
        if (xuid == null) {
            return false;
        }
        String trimmed = xuid.trim();
        if (trimmed.isEmpty() || "0".equals(trimmed)) {
            return false;
        }
        for (int i = 0; i < trimmed.length(); i++) {
            if (!Character.isDigit(trimmed.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> extraMap(Map<String, Object> raw) {
        Object extra = raw.get("extraData");
        if (extra instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return raw;
    }

    private static String firstXuid(Map<String, Object> data, String... keys) {
        if (data == null) {
            return null;
        }
        for (String key : keys) {
            String value = coerceString(data.get(key));
            if (isRealXuid(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static String firstString(Map<String, Object> data, String... keys) {
        if (data == null) {
            return null;
        }
        for (String key : keys) {
            String value = coerceString(data.get(key));
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private static String coerceString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String string) {
            return string;
        }
        if (value instanceof Number number) {
            return Long.toString(number.longValue());
        }
        return value.toString();
    }

    private static UUID parseUuid(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
