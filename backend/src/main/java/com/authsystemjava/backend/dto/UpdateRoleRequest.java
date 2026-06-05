package com.authsystemjava.backend.dto;

import com.authsystemjava.backend.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleRequest {
    @NotNull(message = "Role is required")
    private Role role;
}