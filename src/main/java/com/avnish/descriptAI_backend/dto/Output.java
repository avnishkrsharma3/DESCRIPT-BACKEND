package com.avnish.descriptAI_backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class Output {
    private String type;
    private String id;
    private String status;
    private String role;
    private List<Content> content;
}