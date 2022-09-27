package com.next.beans;

import com.google.common.collect.Lists;
import lombok.*;

import java.util.List;

/**
 * @Title: PageResult
 * @Description:
 * @author: tjx
 * @date :2022/9/26 23:02
 */
@Getter
@Setter
@ToString
public class PageResult<T> {

    private List<T> data = Lists.newArrayList();

    private Integer total = 0;
}
