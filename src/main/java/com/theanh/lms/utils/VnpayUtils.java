package com.theanh.lms.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class VnpayUtils {

    /**
     * Build signed URL for VNPay v2.1.0.
     * Hash is computed on raw (non-encoded) values. URL encoding is applied only when building the query string.
     */
    public static String buildSignedUrl(String baseUrl, Map<String, String> params, String hashSecret) {
        try {
            TreeMap<String, String> sorted = new TreeMap<>(params);
            StringBuilder query = new StringBuilder();
            StringBuilder hashData = new StringBuilder();
            for (Map.Entry<String, String> entry : sorted.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value == null) {
                    continue;
                }
                String encodedKey = URLEncoder.encode(key, StandardCharsets.US_ASCII);
                String encodedValue = URLEncoder.encode(value, StandardCharsets.US_ASCII);
                // Hash and query use the same encoded values (per VNPay sample)
                hashData.append(encodedKey).append("=").append(encodedValue).append("&");
                query.append(encodedKey).append("=").append(encodedValue).append("&");
            }
            if (query.length() > 0) query.setLength(query.length() - 1);
            if (hashData.length() > 0) hashData.setLength(hashData.length() - 1);
            String secureHash = hmacSHA512(hashSecret, hashData.toString());
            return baseUrl + "?" + query + "&vnp_SecureHash=" + secureHash.toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("vnpay.sign.fail", e);
        }
    }

    // Helpers for logging/debug (do not include in signature)
    public static String buildHashDataPreview(Map<String, String> params) {
        TreeMap<String, String> sorted = new TreeMap<>(params);
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (entry.getValue() == null) continue;
            String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII);
            String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII);
            hashData.append(encodedKey).append("=").append(encodedValue).append("&");
        }
        if (hashData.length() > 0) hashData.setLength(hashData.length() - 1);
        return hashData.toString();
    }

    public static String computeSecureHashPreview(String hashSecret, Map<String, String> params) {
        return computeSecureHashPreview(hashSecret, params, false);
    }

    public static String computeSecureHashPreview(String hashSecret, Map<String, String> params, boolean suffix) {
        try {
            String data = buildHashDataPreview(params);
            String full = hmacSHA512(hashSecret, data);
            if (full == null || full.length() < 8) return full;
            return suffix ? full.substring(full.length() - 4) : full.substring(0, 4);
        } catch (Exception e) {
            return "err";
        }
    }

    public static String hmacSHA512(String key, String data) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA512");
        mac.init(new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static boolean verifySignature(Map<String, String> params, String secureHash, String hashSecret) {
        try {
            // 1) Sort by key (A-Z)
            TreeMap<String, String> sorted = new TreeMap<>(params);

            // 2) Remove hash fields (must NOT be included in signing data)
            sorted.remove("vnp_SecureHash");
            sorted.remove("vnp_SecureHashType");

            // 3) Build signing data: key=value&key=value... (URL-encode UTF-8)
            StringBuilder hashData = new StringBuilder();
            for (Map.Entry<String, String> entry : sorted.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value == null || value.isBlank()) continue;

                // VNPay typically signs on URL-encoded values where space becomes '+'
                String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
                String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);

                hashData.append(encodedKey).append("=").append(encodedValue).append("&");
            }
            if (hashData.length() > 0) hashData.setLength(hashData.length() - 1);

            // 4) HMAC SHA512 and compare (case-insensitive hex)
            String computed = hmacSHA512(hashSecret, hashData.toString());
            return computed != null && computed.equalsIgnoreCase(secureHash);
        } catch (Exception e) {
            return false;
        }
    }


}
