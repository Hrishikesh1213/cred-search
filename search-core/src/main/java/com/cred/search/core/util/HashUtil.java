package com.cred.search.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@UtilityClass
public class HashUtil {

    // Configure to sort keys alphabetically.
    // This ensures that map key ordering does not affect the resulting hash.
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    public static String generateHash(List<Map<String, Object>> legsList) {
        try {
            // 1. Convert List -> Deterministic JSON String
            String jsonString = objectMapper.writeValueAsString(legsList);

            // 2. Create SHA-256 Digest
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(jsonString.getBytes(StandardCharsets.UTF_8));

            // 3. Convert Bytes -> Hex String
            return bytesToHex(encodedHash);

        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate route hash", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}