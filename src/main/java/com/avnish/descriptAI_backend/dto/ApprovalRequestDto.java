package com.avnish.descriptAI_backend.dto;

import lombok.Data;

import java.util.List;

/**
 * @author Avnish
 * @date 11-10-2026
 */
@Data
public class ApprovalRequestDto {
    private List<String> listOfIds;
}