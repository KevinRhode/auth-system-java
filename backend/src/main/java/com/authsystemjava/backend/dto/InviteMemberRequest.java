package com.authsystemjava.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class InviteMemberRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    private String role = "MEMBER";
}