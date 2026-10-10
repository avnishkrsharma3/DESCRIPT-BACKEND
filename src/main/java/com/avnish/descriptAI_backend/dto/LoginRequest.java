package com.avnish.descriptAI_backend.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;


@Data
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be 3 to 30 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be 8 to 64 characters")
    @ToString.Exclude
    private String password;

    // Collects every JSON field that is not declared above (e.g. "role")
    @JsonIgnore
    private Map<String, Object> unknownFields = new HashMap<>();

    @JsonAnySetter
    public void addUnknownField(String name, Object value) {
        unknownFields.put(name, value);
    }

    // Fails validation if any unknown field was sent
    @JsonIgnore
    @AssertTrue(message = "Unknown field(s) not allowed in request")
    public boolean isNoUnknownFields() {
        return unknownFields.isEmpty();
    }
}