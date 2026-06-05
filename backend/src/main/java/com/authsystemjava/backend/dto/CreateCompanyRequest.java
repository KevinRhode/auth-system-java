package com.authsystemjava.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateCompanyRequest {

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
}