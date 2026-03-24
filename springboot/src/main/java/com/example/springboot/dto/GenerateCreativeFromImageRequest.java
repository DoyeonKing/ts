package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GenerateCreativeFromImageRequest {

    /**
     * poster 文创海报；merch 周边设计
     */
    @NotBlank(message = "type 不能为空")
    @Pattern(regexp = "poster|merch", message = "type 须为 poster 或 merch")
    private String type;

    /**
     * 用户上传后可公网访问的图片地址。
     */
    @NotBlank(message = "imageUrl 不能为空")
    private String imageUrl;

    private Long playId;

    /**
     * 用户想要的风格描述。
     */
    private String style;

    /**
     * 周边类型：如 T恤、帆布袋、明信片等。
     */
    private String productType;
}
