package com.github.dmj.util;

import cn.hutool.core.util.RandomUtil;

import java.util.Random;

/**
 * @author ljjy1
 * @classname UniqueUtil
 * @description 生成一个纯数字的唯一ID工具
 * @date 2023/10/12 11:28
 */
public class UniqueUtil {

    public static Integer generateUniqueId() {
        int digits = 11; // 指定生成的位数
        // 生成11位数的随机纯数字字符串
        String randomNumber = RandomUtil.randomNumbers(digits);
        if(randomNumber.charAt(0) == '0'){
            //如果第一位是0 随机切换到1-9
            Random random = new Random();
            int randomOne = random.nextInt(9) + 1;
            randomNumber = randomOne +randomNumber.substring(1);
        }
        return Integer.parseInt(randomNumber);
    }
}
