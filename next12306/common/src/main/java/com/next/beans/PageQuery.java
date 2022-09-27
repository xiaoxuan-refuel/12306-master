package com.next.beans;

import javax.validation.constraints.Min;

/**
 * @Title: PageQuery
 * @Description: 分页参数类
 * @author: tjx
 * @date :2022/9/26 23:00
 */
public class PageQuery {
    @Min(value = 1,message = "页码不合法")
    private Integer pageNo = 1;

    @Min(value = 1,message = "每页展示的页数不合法")
    private Integer pageSize = 10;

    private Integer offSet;

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getOffSet() {
        return (pageNo - 1) * pageSize;
    }

    public void setOffSet(Integer offSet) {
        this.offSet = offSet;
    }
}
