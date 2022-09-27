package com.next.util;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Title: StrUtil
 * @Description:
 * @author: tjx
 * @date :2022/9/27 15:16
 */
public class StrUtil {

    public static List<Long> splitToListLong(String str){
        List<String> strings = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(str);
        return strings.stream().map(s -> Long.valueOf(s)).collect(Collectors.toList());
    }

    public static List<Integer> splitToListInteger(String str){
        List<String> strings = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(str);
        return strings.stream().map(s -> Integer.valueOf(s)).collect(Collectors.toList());
    }
}
