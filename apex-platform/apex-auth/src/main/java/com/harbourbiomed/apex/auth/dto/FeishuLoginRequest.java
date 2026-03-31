package com.harbourbiomed.apex.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FeishuLoginRequest {

    @NotBlank(message = "授权码不能为空")
    private String code;
}