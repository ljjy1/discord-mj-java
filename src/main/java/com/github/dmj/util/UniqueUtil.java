package com.github.dmj.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * @author ljjy1
 * @classname UniqueUtil
 * @description 生成一个纯数字的唯一ID工具
 * @date 2023/10/12 11:28
 */
public class UniqueUtil {

    public static Integer generateUniqueId() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(String.valueOf(Instant.now().toEpochMilli()).getBytes());

            int uniqueId = 0;
            for (int i = 0; i < Math.min(hashBytes.length, 8); i++) {
                uniqueId |= (hashBytes[i] & 0xFFL) << (8 * i);
            }

            return uniqueId;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
