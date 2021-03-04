package com.example.demo.common.util;

import java.util.Random;
import java.util.UUID;

/**
 * Description:随机工具类
 * date: 2021/2/25 9:20
 */
public final class RandomUtils {
    private static final String ALLCHAR
            = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LETTERCHAR
            = "abcdefghijkllmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERCHAR = "0123456789";

    /**
     * 生成指定范围内的随机数
     */
    public static int integer(int scopeMin, int scoeMax) {
        Random random = new Random();
        return (random.nextInt(scoeMax) % (scoeMax - scopeMin + 1) + scopeMin);
    }

    /**
     * 返回固定长度的数字
     */
    public static String number(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(NUMBERCHAR.charAt(random.nextInt(NUMBERCHAR.length())));
        }
        return sb.toString();
    }

    /**
     * 返回一个定长的随机字符串(包含大小写字母、数字)
     */
    public static String stringWithNumber(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(ALLCHAR.charAt(random.nextInt(ALLCHAR.length())));
        }
        return sb.toString();
    }

    /**
     * 返回一个定长的随机纯字母字符串(只包含大小写字母)
     */
    public static String string(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(ALLCHAR.charAt(random.nextInt(LETTERCHAR.length())));
        }
        return sb.toString();
    }


    /**
     * 32位UUID
     */
    public static String uuid() {
        UUID uuid = UUID.randomUUID();
        String s = uuid.toString();
        return s.substring(0, 8) + s.substring(9, 13) +
                s.substring(14, 18) + s.substring(19, 23) + s.substring(24);
    }

    /**
     * 根据时间生成随机数15位
     */
    public static String time() {
        long millis = System.currentTimeMillis();
        Random random = new Random();
        int end2 = random.nextInt(99);
        return millis + String.format("%02d", end2);
    }

}
