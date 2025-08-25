package com.nageoffer.shortlink.project.toolkit;

import cn.hutool.core.lang.hash.MurmurHash;

/**
 * HASH 工具类
 */
public class HashUtil {

    private static final char[] CHARS = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };
    //是包含 62 个字符的编码字符集；
    private static final int SIZE = CHARS.length;

    private static String convertDecToBase62(long num) {
        //十进制转 base62 字符串方法
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            int i = (int) (num % SIZE);
            sb.append(CHARS[i]);
            num /= SIZE;
        }
        return sb.reverse().toString();
        //为什么要翻转，为了符合人类阅读数字时从高位到低位（如百十个）的阅读顺序
    }
    /*
    创建 StringBuilder 对象用于构建结果；使用循环，当数字大于 0 时，计算数字对 62 取模的结果，得到索引，根据索引从字符数组中获取对应字符并追加，
    将数字除以 62 继续下一次循环，最后反转字符串（因为余数是低位先得到的）并返回
     */

    public static String hashToBase62(String str) {
        int i = MurmurHash.hash32(str);
        long num = i < 0 ? Integer.MAX_VALUE - (long) i : i;
        return convertDecToBase62(num);
    }
    //这是主要的公共方法，功能是将输入字符串哈希后转为 base62字符串：
    //使用 MurmurHash 算法计算输入字符串的 32 位哈希值
    //处理负数情况：如果哈希值为负，则用 Integer.MAX_VALUE 减去该负数的绝对值，确保得到一个正数
    //调用 convertDecToBase62 方法将处理后的数字转为 base62 字符串，其最大长度为6位
}