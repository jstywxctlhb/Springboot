package com.zjt.model;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.util.List;

@Data
public class PageRusult<T> extends PageInfo<T> {

    private Integer code;

    public PageRusult() {

    }

    public PageRusult(List<T> list) {
        super(list, 8);
    }
}