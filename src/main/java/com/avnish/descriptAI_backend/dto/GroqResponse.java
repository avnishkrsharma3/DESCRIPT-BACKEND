package com.avnish.descriptAI_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class GroqResponse {
    private String id;
    private String object;
    private String status;

    @JsonProperty("created_at")
    private Long createdAt;

    private List<Output> output;
    private Usage usage;
}