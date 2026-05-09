package org.example.springboot2.tag.dto;

import com.alibaba.excel.annotation.ExcelProperty;

public class TagExcelDTO {

    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("内容")
    private String content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}