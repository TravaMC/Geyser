package org.geysermc.geyser.util;

import org.cloudburstmc.protocol.bedrock.util.ChainValidationResult;
import org.geysermc.geyser.session.auth.BedrockClientData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class XboxIdentityTest {

    @Test
    void readsStringXuidFromExtraData() throws Exception {
        ChainValidationResult result = chain(true, extra("\"XUID\":\"2535412345678901\""));
        XboxIdentity.Parsed parsed = XboxIdentity.parse(result, null);
        assertEquals("2535412345678901", parsed.xuid());
        assertEquals("Fixture", parsed.displayName());
        assertTrue(XboxIdentity.isRealXuid(parsed.xuid()));
    }

    @Test
    void readsNumericXuidThatCloudburstIdentityClaimsRejects() throws Exception {
        ChainValidationResult result = chain(true, extra("\"XUID\":2535412345678901"));
        assertThrows(IllegalStateException.class, result::identityClaims);

        XboxIdentity.Parsed parsed = XboxIdentity.parse(result, null);
        assertEquals("2535412345678901", parsed.xuid());
        assertEquals("MHYw", XboxIdentity.identityPublicKey(result));
    }

    @Test
    void readsLowercaseXuidAndTokenXid() throws Exception {
        ChainValidationResult lowercase = chain(true, extra("\"xuid\":\"2535412345678901\""));
        assertEquals("2535412345678901", XboxIdentity.parse(lowercase, null).xuid());

        ChainValidationResult token = new ChainValidationResult(true,
            "{\"cpk\":\"MHYw\",\"xname\":\"TokenUser\",\"xid\":\"2535412345678901\",\"mid\":\"playfab\"}");
        XboxIdentity.Parsed parsed = XboxIdentity.parse(token, null);
        assertEquals("2535412345678901", parsed.xuid());
        assertEquals("TokenUser", parsed.displayName());
        assertEquals("playfab", parsed.minecraftId());
        assertEquals("MHYw", XboxIdentity.identityPublicKey(token));
    }

    @Test
    void fallsBackToPlatformOnlineIdWhenChainOmitsXuid() throws Exception {
        ChainValidationResult result = chain(false, extra(""));
        BedrockClientData clientData = mock(BedrockClientData.class);
        when(clientData.getPlatformOnlineId()).thenReturn("2535412345678901");
        when(clientData.getUsername()).thenReturn("FromClient");

        XboxIdentity.Parsed parsed = XboxIdentity.parse(result, clientData);
        assertEquals("2535412345678901", parsed.xuid());
        assertEquals("Fixture", parsed.displayName());
        assertTrue(XboxIdentity.isRealXuid(parsed.xuid()));
    }

    @Test
    void zeroAndBlankXuidAreNotXbox() {
        assertFalse(XboxIdentity.isRealXuid(null));
        assertFalse(XboxIdentity.isRealXuid(""));
        assertFalse(XboxIdentity.isRealXuid("0"));
        assertFalse(XboxIdentity.isRealXuid(" 0 "));
        assertFalse(XboxIdentity.isRealXuid("not-a-xuid"));
        assertTrue(XboxIdentity.isRealXuid("2535412345678901"));
    }

    private static ChainValidationResult chain(boolean signed, String extraFields) throws Exception {
        String extra = extraFields.isEmpty() ? extraFields : extraFields + ",";
        return new ChainValidationResult(signed, "{\"identityPublicKey\":\"MHYw\",\"extraData\":{"
            + extra
            + "\"displayName\":\"Fixture\",\"identity\":\"00000000-0000-0000-0000-000000000001\"}}");
    }

    private static String extra(String field) {
        return field;
    }
}
