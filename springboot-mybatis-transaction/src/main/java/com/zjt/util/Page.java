package com.zjt.util;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 分页工具类
 */
@Data
public class Page<T> implements Serializable {

    private int page;

    private int rows;

    private static final long serialVersionUID = 1L;

    private List<T> records = Collections.emptyList();

    private Map<String, Object> condition;
}