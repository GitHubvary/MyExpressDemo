package com.example.demo.common;

import java.util.List;

/**
 * Description:
 * date: 2021/1/16 16:30
 */
public class CollectionUtils {

    /**
     * Description:返回集合第一个元素，没有就null
     * @param:
     * @return:
    */
    public static <T> T getListFirst(List<T> list) {
        return isListEmpty(list) ? null : list.get(0);
    }

    /**
     * Description:判断集合是否为空
     * @param:
     * @return:
    */
    public static <T> boolean isListEmpty(List<T> list) {
        return list == null || list.size() == 0;
    }
}
