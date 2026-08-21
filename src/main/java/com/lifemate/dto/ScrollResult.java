package com.lifemate.dto;

import lombok.Data;

import java.util.List;

/** 滚动分页结果：list + minTime + offset（下次请求携带 minTime/offset 继续翻页）。 */
@Data
public class ScrollResult {
    private List<?> list;
    private Long minTime;
    private Integer offset;
}
