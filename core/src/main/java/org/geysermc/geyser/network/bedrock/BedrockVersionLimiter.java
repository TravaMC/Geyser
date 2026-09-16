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

package org.geysermc.geyser.network.bedrock;

import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.api.util.MinecraftVersion;
import org.geysermc.geyser.configuration.GeyserConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Via-style Bedrock version allowlist. Specs are version strings, protocol numbers,
 * comparisons ({@code >=1.21.0}), or inclusive ranges ({@code 1.20.0-1.20.80}).
 */
public final class BedrockVersionLimiter {
    private BedrockVersionLimiter() {
    }

    public static boolean isEnabled(GeyserConfig config) {
        return config.bedrock().allowedVersions().enabled();
    }

    public static boolean isProtocolAllowed(GeyserConfig config, int protocolVersion) {
        if (!isEnabled(config)) {
            return true;
        }
        Set<Integer> allowed = resolveAllowedProtocols(
            config.bedrock().allowedVersions().list(), GameProtocol.SUPPORTED_BEDROCK_VERSIONS);
        for (int allowedProtocol : allowed) {
            if (sameProtocol(allowedProtocol, protocolVersion)) {
                return true;
            }
        }
        return false;
    }

    public static String describeAllowed(GeyserConfig config) {
        if (!isEnabled(config)) {
            return GameProtocol.getAllSupportedBedrockVersions();
        }
        Set<Integer> allowed = resolveAllowedProtocols(config.bedrock().allowedVersions().list(), GameProtocol.SUPPORTED_BEDROCK_VERSIONS);
        List<String> names = new ArrayList<>();
        for (MinecraftVersion version : GameProtocol.SUPPORTED_BEDROCK_VERSIONS) {
            if (allowed.contains(version.protocolVersion()) && !names.contains(version.versionString())) {
                names.add(version.versionString());
            }
        }
        return names.isEmpty() ? "(none)" : String.join(", ", names);
    }

    public static Set<Integer> resolveAllowedProtocols(Collection<String> specs, List<? extends MinecraftVersion> supported) {
        return resolveAllowedProtocols(specs, supported, BedrockVersionLimiter::warn);
    }

    public static Set<Integer> resolveAllowedProtocols(Collection<String> specs, List<? extends MinecraftVersion> supported,
                                                       Consumer<String> warnings) {
        Set<Integer> protocols = new TreeSet<>();
        if (specs == null) {
            return protocols;
        }
        for (String raw : specs) {
            if (raw == null) {
                continue;
            }
            String spec = raw.trim();
            if (spec.isEmpty()) {
                continue;
            }
            Set<Integer> matched = matchSpec(spec, supported);
            if (matched.isEmpty()) {
                warnings.accept("Ignoring unknown Bedrock version spec in allowed-versions.list: \"" + spec + "\"");
                continue;
            }
            protocols.addAll(matched);
        }
        return protocols;
    }

    private static Set<Integer> matchSpec(String spec, List<? extends MinecraftVersion> supported) {
        Set<Integer> matched = new LinkedHashSet<>();
        if (spec.startsWith(">=") || spec.startsWith("<=") || spec.startsWith(">") || spec.startsWith("<")) {
            int opLen = spec.startsWith(">=") || spec.startsWith("<=") ? 2 : 1;
            String op = spec.substring(0, opLen);
            String rhs = spec.substring(opLen).trim();
            Integer protocol = tryParseProtocol(rhs);
            int[] version = protocol == null ? tryParseVersion(rhs) : null;
            if (protocol == null && version == null) {
                return matched;
            }
            for (MinecraftVersion candidate : supported) {
                int cmp = protocol != null
                    ? Integer.compare(canonicalProtocol(candidate.protocolVersion()), canonicalProtocol(protocol))
                    : compareVersions(tryParseVersion(candidate.versionString()), version);
                if (cmp == Integer.MIN_VALUE) {
                    continue;
                }
                boolean ok = switch (op) {
                    case ">=" -> cmp >= 0;
                    case "<=" -> cmp <= 0;
                    case ">" -> cmp > 0;
                    case "<" -> cmp < 0;
                    default -> false;
                };
                if (ok) {
                    matched.add(candidate.protocolVersion());
                }
            }
            return matched;
        }

        int dash = spec.indexOf('-');
        if (dash > 0 && dash < spec.length() - 1) {
            String left = spec.substring(0, dash).trim();
            String right = spec.substring(dash + 1).trim();
            Integer leftProtocol = tryParseProtocol(left);
            Integer rightProtocol = tryParseProtocol(right);
            if (leftProtocol != null && rightProtocol != null) {
                int min = canonicalProtocol(Math.min(leftProtocol, rightProtocol));
                int max = canonicalProtocol(Math.max(leftProtocol, rightProtocol));
                for (MinecraftVersion candidate : supported) {
                    int protocol = candidate.protocolVersion();
                    if (canonicalProtocol(protocol) >= min && canonicalProtocol(protocol) <= max) {
                        matched.add(protocol);
                    }
                }
                return matched;
            }
            int[] leftVersion = tryParseVersion(left);
            int[] rightVersion = tryParseVersion(right);
            if (leftVersion != null && rightVersion != null) {
                if (compareVersions(leftVersion, rightVersion) > 0) {
                    int[] swap = leftVersion;
                    leftVersion = rightVersion;
                    rightVersion = swap;
                }
                for (MinecraftVersion candidate : supported) {
                    int[] current = tryParseVersion(candidate.versionString());
                    if (current == null) {
                        continue;
                    }
                    if (compareVersions(current, leftVersion) >= 0 && compareVersions(current, rightVersion) <= 0) {
                        matched.add(candidate.protocolVersion());
                    }
                }
                return matched;
            }
        }

        Integer protocol = tryParseProtocol(spec);
        if (protocol != null) {
            for (MinecraftVersion candidate : supported) {
                if (sameProtocol(candidate.protocolVersion(), protocol)) {
                    matched.add(candidate.protocolVersion());
                }
            }
            return matched;
        }

        int[] exact = tryParseVersion(spec);
        if (exact != null) {
            for (MinecraftVersion candidate : supported) {
                int[] current = tryParseVersion(candidate.versionString());
                if (current != null && compareVersions(current, exact) == 0) {
                    matched.add(candidate.protocolVersion());
                }
            }
            if (!matched.isEmpty()) {
                return matched;
            }
        }

        for (MinecraftVersion candidate : supported) {
            if (candidate.versionString().equalsIgnoreCase(spec)) {
                matched.add(candidate.protocolVersion());
            }
        }
        return matched;
    }

    static Integer tryParseProtocol(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return null;
            }
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    /**
     * Retail 26.50/26.51 report 2193; Cloudburst still numbers the codec 2192.
     */
    static boolean sameProtocol(int left, int right) {
        if (left == right) {
            return true;
        }
        return GameProtocol.isSame26_50Protocol(left) && GameProtocol.isSame26_50Protocol(right);
    }

    static int canonicalProtocol(int protocol) {
        return GameProtocol.isSame26_50Protocol(protocol) ? GameProtocol.BEDROCK_1_26_50_PROTOCOL : protocol;
    }

    static int[] tryParseVersion(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        // GameProtocol lists 26.x as "26.50"; clients/Via often write "1.26.50" / "1.26.51".
        if (value.startsWith("1.26.")) {
            value = value.substring(2);
        }
        String[] parts = value.split("\\.");
        if (parts.length == 0 || parts.length > 4) {
            return null;
        }
        int[] parsed = new int[4];
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                return null;
            }
            for (int c = 0; c < parts[i].length(); c++) {
                if (!Character.isDigit(parts[i].charAt(c))) {
                    return null;
                }
            }
            try {
                parsed[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return parsed;
    }

    static int compareVersions(int[] left, int[] right) {
        if (left == null || right == null) {
            return Integer.MIN_VALUE;
        }
        int length = Math.max(left.length, right.length);
        for (int i = 0; i < length; i++) {
            int l = i < left.length ? left[i] : 0;
            int r = i < right.length ? right[i] : 0;
            int cmp = Integer.compare(l, r);
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }

    private static void warn(String message) {
        GeyserImpl geyser = GeyserImpl.getInstance();
        if (geyser != null && geyser.getLogger() != null) {
            geyser.getLogger().warning(message);
        }
    }

    public static String describeProtocols(Set<Integer> protocols, List<? extends MinecraftVersion> supported) {
        return supported.stream()
            .filter(version -> protocols.contains(version.protocolVersion()))
            .map(MinecraftVersion::versionString)
            .distinct()
            .collect(Collectors.joining(", "));
    }
}
