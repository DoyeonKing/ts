package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GenerateTextRequest {

    /**
     * review 观后感；plot 剧情分析；actor 演员评价
     */
    @NotBlank(message = "type 不能为空")
    @Pattern(regexp = "review|plot|actor", message = "type 须为 review、plot 或 actor")
    private String type;

    private Long playId;

    private String userInput;
}
