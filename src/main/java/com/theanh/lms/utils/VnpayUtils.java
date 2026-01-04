package com.theanh.lms.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

public class VnpayUtils {

    public static String buildSignedUrl(String baseUrl, Map<String, String> params, String hashSecret) {
        try {
            TreeMap<String, String> sorted = new TreeMap<>(params);
            StringBuilder query = new StringBuilder();
            StringBuilder hashData = new StringBuilder();
            for (Map.Entry<String, String> entry : sorted.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value == null) continue;
                query.append(URLEncoder.encode(key, StandardCharsets.US_ASCII))
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII))
                        .append("&");
                hashData.append(key).append("=").append(value).append("&");
            }
            if (query.length() > 0) query.setLength(query.length() - 1);
            if (hashData.length() > 0) hashData.setLength(hashData.length() - 1);
            String secureHash = hmacSHA512(hashSecret, hashData.toString());
            return baseUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
        } catch (Exception e) {
            throw new RuntimeException("vnpay.sign.fail", e);
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
            TreeMap<String, String> sorted = new TreeMap<>(params);
            sorted.remove("vnp_SecureHash");
            sorted.remove("vnp_SecureHashType");
            StringBuilder hashData = new StringBuilder();
            for (Map.Entry<String, String> entry : sorted.entrySet()) {
                if (entry.getValue() == null) continue;
                hashData.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            if (hashData.length() > 0) hashData.setLength(hashData.length() - 1);
            String computed = hmacSHA512(hashSecret, hashData.toString());
            return computed.equalsIgnoreCase(secureHash);
        } catch (Exception e) {
            return false;
        }
    }
}
