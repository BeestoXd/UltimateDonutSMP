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

    @Test
    void clampNbtDataVersionHandlesNullAndInvalidInputs() {
        assertNull(ItemSerializationUtils.clampNbtDataVersion(null, 4000));
        byte[] shortBytes = new byte[]{0x01, 0x02};
        assertSame(shortBytes, ItemSerializationUtils.clampNbtDataVersion(shortBytes, 4000));
        byte[] normalBytes = new byte[20];
        assertSame(normalBytes, ItemSerializationUtils.clampNbtDataVersion(normalBytes, -1));
    }

    @Test
    void clampNbtDataVersionLeavesLowerOrEqualVersionIntact() throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        baos.write(new byte[]{0x0A, 0x00, 0x00}); // TAG_Compound
        baos.write(new byte[]{0x03, 0x00, 0x0B}); // TAG_Int "DataVersion"
        baos.write("DataVersion".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        baos.write(new byte[]{0x00, 0x00, 0x10, 0x00}); // 4096
        baos.write(0x00); // TAG_End

        byte[] original = baos.toByteArray();
        byte[] clamped = ItemSerializationUtils.clampNbtDataVersion(original, 5000);
        assertSame(original, clamped);
    }

    @Test
    void clampNbtDataVersionClampsHigherVersionUncompressed() throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        baos.write(new byte[]{0x0A, 0x00, 0x00});
        baos.write(new byte[]{0x03, 0x00, 0x0B});
        baos.write("DataVersion".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        baos.write(new byte[]{0x00, 0x00, 0x13, 0x27}); // 4903
        baos.write(0x00);

        byte[] original = baos.toByteArray();
        byte[] clamped = ItemSerializationUtils.clampNbtDataVersion(original, 4189);
        assertNotSame(original, clamped);

        int clampedVersion = ((clamped[17] & 0xFF) << 24)
                | ((clamped[18] & 0xFF) << 16)
                | ((clamped[19] & 0xFF) << 8)
                | (clamped[20] & 0xFF);
        assertEquals(4189, clampedVersion);
    }

    @Test
    void clampNbtDataVersionClampsHigherVersionGzipped() throws Exception {
        java.io.ByteArrayOutputStream rawBaos = new java.io.ByteArrayOutputStream();
        rawBaos.write(new byte[]{0x0A, 0x00, 0x00});
        rawBaos.write(new byte[]{0x03, 0x00, 0x0B});
        rawBaos.write("DataVersion".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        rawBaos.write(new byte[]{0x00, 0x00, 0x13, 0x27}); // 4903
        rawBaos.write(0x00);

        java.io.ByteArrayOutputStream gzipBaos = new java.io.ByteArrayOutputStream();
        try (java.util.zip.GZIPOutputStream gzos = new java.util.zip.GZIPOutputStream(gzipBaos)) {
            gzos.write(rawBaos.toByteArray());
        }

        byte[] gzippedOriginal = gzipBaos.toByteArray();
        byte[] clampedGzip = ItemSerializationUtils.clampNbtDataVersion(gzippedOriginal, 3953);
        assertNotSame(gzippedOriginal, clampedGzip);

        // Decompress and verify
        java.io.ByteArrayOutputStream decompressed = new java.io.ByteArrayOutputStream();
        try (java.util.zip.GZIPInputStream gzis = new java.util.zip.GZIPInputStream(new java.io.ByteArrayInputStream(clampedGzip))) {
            gzis.transferTo(decompressed);
        }
        byte[] uncompressedClamped = decompressed.toByteArray();
        int version = ((uncompressedClamped[17] & 0xFF) << 24)
                | ((uncompressedClamped[18] & 0xFF) << 16)
                | ((uncompressedClamped[19] & 0xFF) << 8)
                | (uncompressedClamped[20] & 0xFF);
        assertEquals(3953, version);
    }

    @Test
    void clampNbtDataVersionHandlesIssue474SerializedPayload() throws Exception {
        String base64Payload = "H4sIAAAAAAAA/52STU8CMRCGZ2EhsH4kYrz3otz8ARyNhpCoEDVcN6U7sM32Y9NOg/x7u4rAwQTiHJpO+s7M05nJANpw9siJz9F5aQ3A9bAHLVnAlZYGheNLGvmarw26NnSEDYYAIMkgE1bX1qAh34fLvVhZh1mUtPvQwU9yvHFaCXQlcSUFJJAFU6BTMaKIXrqwqoBekzuGQrpyfBM1drEMXnD61lx4crJCKp0NqzKKU4qpofuxqXHEYJ/892UwcdawsVWo2fsP/a5CZ11Kwp0U4q2xQ9zkZNzuBpWy6xOB72aKC2RkmXAYpcw65omLilEpPdv2+f6ALYObfWtF8GR1brjG/9GeK7kqKa+DqxWeyPxHK4/wFXGdMhjMwiIiPYSqkjTnKqBP4DYokropaE0gr+uR5oavsMi3f8/jaHTSO0FIcfaQTd6mr/l4+vz0ksLwaAzX2/1tLGmOL6Bg2DQBAwAA";
        byte[] bytes = java.util.Base64.getDecoder().decode(base64Payload);

        // Verify higher or equal limit leaves it untouched
        assertSame(bytes, ItemSerializationUtils.clampNbtDataVersion(bytes, 5000));

        // Clamp to server data version (e.g. 4189)
        byte[] clamped = ItemSerializationUtils.clampNbtDataVersion(bytes, 4189);
        assertNotSame(bytes, clamped);

        java.io.ByteArrayOutputStream decompressed = new java.io.ByteArrayOutputStream();
        try (java.util.zip.GZIPInputStream gzis = new java.util.zip.GZIPInputStream(new java.io.ByteArrayInputStream(clamped))) {
            gzis.transferTo(decompressed);
        }
        byte[] uncompressed = decompressed.toByteArray();
        int version = ((uncompressed[17] & 0xFF) << 24)
                | ((uncompressed[18] & 0xFF) << 16)
                | ((uncompressed[19] & 0xFF) << 8)
                | (uncompressed[20] & 0xFF);
        assertEquals(4189, version);
    }
}
