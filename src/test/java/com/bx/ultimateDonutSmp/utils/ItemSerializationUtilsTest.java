package com.bx.ultimateDonutSmp.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemSerializationUtilsTest {

    @Test
    void isLegacyBytesCorrectlyIdentifiesJavaSerializationHeader() {
        byte[] legacyBytes = new byte[]{(byte) 0xAC, (byte) 0xED, 0x00, 0x05};
        byte[] otherBytes = new byte[]{0x00, 0x01, 0x02, 0x03};

        assertTrue(ItemSerializationUtils.isLegacyBytes(legacyBytes));
        assertFalse(ItemSerializationUtils.isLegacyBytes(otherBytes));
        assertFalse(ItemSerializationUtils.isLegacyBytes(null));
        assertFalse(ItemSerializationUtils.isLegacyBytes(new byte[]{(byte) 0xAC}));
    }
}
