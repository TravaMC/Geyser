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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.geyser.api.util.MinecraftVersion;
import org.geysermc.geyser.network.bedrock.GameProtocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Parses {@code advanced.dump-join-packets}: empty = off, {@code *} = every protocol,
 * otherwise protocol numbers and/or Bedrock version strings.
 */
public final class JoinDumpFilter {
    private JoinDumpFilter() {
    }

    public static boolean matches(@Nullable Collection<String> selectors, int protocolVersion,
                                 @Nullable String gameVersion) {
        if (selectors == null || selectors.isEmpty() || protocolVersion <= 0) {
            return false;
        }
        for (String selector : selectors) {
            for (String token : tokens(selector)) {
                if (isAll(token)) {
                    return true;
                }
                if (token.equals(Integer.toString(protocolVersion))) {
                    return true;
                }
                if (gameVersion != null && token.equalsIgnoreCase(gameVersion)) {
                    return true;
                }
                if (matchesRegisteredVersion(token, protocolVersion)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<String> tokens(@Nullable String spec) {
        List<String> out = new ArrayList<>();
        if (spec == null) {
            return out;
        }
        for (String part : spec.split("[,\\s]+")) {
            String token = part.trim();
            if (!token.isEmpty()) {
                out.add(token);
            }
        }
        return out;
    }

    private static boolean isAll(String token) {
        String lower = token.toLowerCase(Locale.ROOT);
        return "*".equals(token) || "all".equals(lower) || "true".equals(lower);
    }

    private static boolean matchesRegisteredVersion(String token, int protocolVersion) {
        for (MinecraftVersion version : GameProtocol.SUPPORTED_BEDROCK_VERSIONS) {
            if (token.equalsIgnoreCase(version.versionString())
                && version.protocolVersion() == protocolVersion) {
                return true;
            }
        }
        return false;
    }
}
