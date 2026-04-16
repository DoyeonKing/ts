package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GenerateCreativeRequest {

    /**
     * poster 文创海报；merch 周边设计
     */
    @NotBlank(message = "type 不能为空")
    @Pattern(regexp = "poster|merch", message = "type 须为 poster 或 merch")
    private String type;

    private Long playId;

    /**
     * 用户给出的风格描述，如“梦幻蓝紫”“复古海报风”等。
     */
    private String style;
}
