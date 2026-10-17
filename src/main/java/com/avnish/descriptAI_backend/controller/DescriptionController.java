package com.avnish.descriptAI_backend.controller;

import com.avnish.descriptAI_backend.dto.ApprovalRequestDto;
import com.avnish.descriptAI_backend.model.ProductDescription;
import com.avnish.descriptAI_backend.service.DescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * @author Avnish
 * @date 11-10-2026
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product/description")
public class DescriptionController {

    private final DescriptionService descriptionService;

    @GetMapping("/status")
    public ResponseEntity<List<ProductDescription>> getAll() {
        return ResponseEntity.ok(descriptionService.findAll());
    }

    /**
     * Draft -> APPROVED. The previously approved version becomes SUPERSEDED.
     */
    @PostMapping("/approve")
    public ResponseEntity<Void> approve(@RequestBody ApprovalRequestDto request, Principal principal) {
        List<String> listOfIds;

            listOfIds = request.getListOfIds();
            if (listOfIds == null || listOfIds.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        descriptionService.approve(listOfIds, principal.getName());
        return ResponseEntity.ok().build();
    }
    @DeleteMapping ("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id){
        descriptionService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}