package com.authsystemjava.backend.dto;

import com.authsystemjava.backend.model.CompanyRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMemberRoleRequest {

    @NotNull(message = "Role is required")
    private CompanyRole role;
}