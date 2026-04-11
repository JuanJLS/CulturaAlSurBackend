package com.culturaalsur.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Returned by POST /api/auth/login.
 * Angular stores the token and sends it as: Authorization: Bearer <token>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
}
