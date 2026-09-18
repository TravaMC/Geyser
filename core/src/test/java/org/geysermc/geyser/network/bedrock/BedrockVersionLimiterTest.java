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

import org.geysermc.geyser.api.util.MinecraftVersion;
import org.geysermc.geyser.impl.MinecraftVersionImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BedrockVersionLimiterTest {
    private static final List<MinecraftVersion> SUPPORTED = List.of(
        new MinecraftVersionImpl("1.20.0", 589),
        new MinecraftVersionImpl("1.20.10", 594),
        new MinecraftVersionImpl("1.21.0", 685),
        new MinecraftVersionImpl("1.21.40", 748),
        new MinecraftVersionImpl("26.0", 1000),
        new MinecraftVersionImpl("26.45", 2169),
        new MinecraftVersionImpl("26.50", 2192),
        new MinecraftVersionImpl("26.50", 2193),
        new MinecraftVersionImpl("26.51", 2192),
        new MinecraftVersionImpl("26.51", 2193)
    );

    @Test
    void exactVersionAndProtocol() {
        assertEquals(Set.of(748), resolve("1.21.40"));
        assertEquals(Set.of(748), resolve("748"));
        assertEquals(Set.of(2192, 2193), resolve("26.50"));
        assertEquals(Set.of(2192, 2193), resolve("1.26.50"));
        assertEquals(Set.of(2192, 2193), resolve("26.51"));
        assertEquals(Set.of(2192, 2193), resolve("1.26.51"));
        assertEquals(Set.of(2192, 2193), resolve("2193"));
        assertEquals(Set.of(2192, 2193), resolve("2192"));
    }

    @Test
    void exactOneTwentyOneDoesNotMatchLaterPatches() {
        assertEquals(Set.of(685), resolve("1.21"));
        assertEquals(Set.of(685), resolve("1.21.0"));
        assertFalse(resolve("1.21").contains(748));
    }

    @Test
    void prefixDoesNotMatchLongerVersion() {
        assertTrue(resolve("1.2").isEmpty());
        assertFalse(resolve("1.2").contains(589));
    }

    @Test
    void comparisons() {
        assertEquals(Set.of(2169, 2192, 2193), resolve(">=26.45"));
        assertEquals(Set.of(2169, 2192, 2193), resolve(">=1.26.45"));
        assertEquals(Set.of(2192, 2193), resolve(">=26.51"));
        assertEquals(Set.of(2192, 2193), resolve(">=1.26.51"));
        assertEquals(Set.of(589, 594, 685, 748), resolve("<26.0"));
        assertEquals(Set.of(2192, 2193), resolve(">2169"));
        assertEquals(Set.of(589, 594, 685, 748, 1000, 2169), resolve("<=2169"));
        assertEquals(Set.of(2192, 2193), resolve(">=2193"));
        assertEquals(Set.of(589, 594, 685, 748, 1000, 2169, 2192, 2193), resolve("<=2192"));
        assertTrue(resolve(">2192").isEmpty());
        assertEquals(Set.of(589, 594, 685, 748, 1000, 2169), resolve("<2193"));
    }

    @Test
    void inclusiveRanges() {
        assertEquals(Set.of(589, 594), resolve("1.20.0-1.20.10"));
        assertEquals(Set.of(748, 1000), resolve("748-1000"));
        assertEquals(Set.of(589, 594), resolve("1.20.10-1.20.0"));
        assertEquals(Set.of(2192, 2193), resolve("2192-2192"));
        assertEquals(Set.of(2192, 2193), resolve("2193-2193"));
    }

    @Test
    void unknownSpecsAreIgnored() {
        List<String> warnings = new ArrayList<>();
        Set<Integer> allowed = BedrockVersionLimiter.resolveAllowedProtocols(
            List.of("not-a-version", "1.21.40"), SUPPORTED, warnings::add);
        assertEquals(Set.of(748), allowed);
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("not-a-version"));
    }

    @Test
    void emptyListMatchesNothing() {
        assertTrue(BedrockVersionLimiter.resolveAllowedProtocols(List.of(), SUPPORTED).isEmpty());
    }

    private static Set<Integer> resolve(String spec) {
        return BedrockVersionLimiter.resolveAllowedProtocols(List.of(spec), SUPPORTED);
    }
}
