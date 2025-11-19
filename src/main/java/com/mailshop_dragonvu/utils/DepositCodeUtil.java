package com.mailshop_dragonvu.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DepositCodeUtil {
    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String generateDepositKey(Long userId, String secret) {
        String encoded = encodeBase62(userId);
        String check = checksum(encoded, secret);
        return "DIO-" + encoded + "-" + check;
    }

    public static String encodeBase62(long number) {
        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            sb.append(BASE62.charAt((int)(number % 62)));
            number /= 62;
        }
        return sb.reverse().toString();
    }

    public static long decodeBase62(String base62) {
        long result = 0;
        for (char c : base62.toCharArray()) {
            result = result * 62 + BASE62.indexOf(c);
        }
        return result;
    }
    public static String checksum(String input, String secretKey) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(key);
            byte[] macData = sha256Hmac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(macData).substring(0, 1);
        } catch (Exception e) {
            throw new RuntimeException("Checksum error", e);
        }
    }

}
