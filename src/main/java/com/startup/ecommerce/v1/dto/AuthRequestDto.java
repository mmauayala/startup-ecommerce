package com.startup.ecommerce.v1.dto;

import lombok.Data;

@Data
public class AuthRequestDto {
    private String email;
    private String password;
}
