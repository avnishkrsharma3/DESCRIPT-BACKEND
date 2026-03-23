package com.avnish.descriptAI_backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class GroqAIRequest  {
    private String model;
    private List<Input> input;

    @Data
    @Builder
    public static class Input {
        private String role;
        private String content;
    }
}
