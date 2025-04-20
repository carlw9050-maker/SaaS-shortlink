package com.nageoffer.shortlink.admin.toolkit;

import java.util.Random;

/**
 * gid随机生成器
 */
public class RandomGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random RANDOM = new Random();
    public static String generateRandom() {
        return generateRandom(6);
    }

    /**
     * 生成随机分组ID
     * @param length gid位数
     * @return gid
     */
    public static String generateRandom(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            builder.append(CHARACTERS.charAt(randomIndex));
        }
        return builder.toString();
    }
}

