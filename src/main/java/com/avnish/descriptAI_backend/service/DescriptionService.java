package com.avnish.descriptAI_backend.service;

/**
 * @author Avnish
 * @date 11-10-2026
 */

import com.avnish.descriptAI_backend.constants.DescriptionStatus;
import com.avnish.descriptAI_backend.dto.ProductDescriptionGeneratedResponse;
import com.avnish.descriptAI_backend.model.ProductDescription;
import com.avnish.descriptAI_backend.exceptions.DescriptionException;
import com.avnish.descriptAI_backend.repository.ProductDescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB version. No @Transactional on purpose: multi-document transactions need a
 * replica set, and a standalone local mongod rejects them. The operations below are
 * ordered so a failure in the middle never damages the live (APPROVED) description.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DescriptionService {

    private final ProductDescriptionRepository repo;

    /**
     * The ONE method the generation controller calls after the AI has produced text.
     * Saves ONLY results that are new (status GENERATED). Results that came back from the database
     * (APPROVED or DRAFT) are skipped, so asking again never creates duplicate versions.
     *
     * After saving, the result's status is changed to DRAFT so the client sees the real state.
     */
    public void saveGenerated(List<ProductDescriptionGeneratedResponse> results, String username) {
        for (ProductDescriptionGeneratedResponse item : results) {

            if (!GroqAIService.STATUS_NEW.equals(item.getStatus())) {
                continue;                                   // already saved earlier
            }
            if (item.getAiGeneratedDescription() == null || item.getAiGeneratedDescription().isBlank()) {
                log.warn("AI returned empty text for product {}, not saved", item.getProductId());
                continue;
            }

            Long productId;
            try {
                productId = Long.parseLong(item.getProductId().trim());
            } catch (NumberFormatException | NullPointerException e) {
                log.error("Product id '{}' is not numeric, draft not saved", item.getProductId());
                continue;
            }

            createDraft(productId, item, username);
            item.setStatus(DescriptionStatus.DRAFT.name());
        }
    }

    /**
     * Called for BOTH the first generate and every regenerate.
     * The text is already produced (the Groq call happens BEFORE this method).
     *
     * 1. next version = latest version + 1
     * 2. INSERT the new DRAFT first (unique index rejects a duplicate version -> 409)
     * 3. only then retire older unreviewed drafts
     * The APPROVED document is never touched here.
     */
    public ProductDescription createDraft(Long productId, ProductDescriptionGeneratedResponse source, String username) {

        int nextVersion = repo.findFirstByProductIdOrderByVersionNumberDesc(productId)
                .map(ProductDescription::getVersionNumber)
                .orElse(0) + 1;

        ProductDescription draft = new ProductDescription();
        draft.setProductId(productId);
        draft.setProductName(source.getProductName());
        draft.setCategory(source.getCategory());
        draft.setOriginalDescription(source.getOriginalDescription());
        draft.setAiGeneratedDescription(source.getAiGeneratedDescription().trim());
        draft.setImageURL(source.getImageURL());
        draft.setAiModel(source.getAiModel());
        draft.setVersionNumber(nextVersion);
        draft.setStatus(DescriptionStatus.DRAFT);
        draft.setCreatedBy(username);
        draft.setCreatedAt(Instant.now());

        // insert (not save): throws DuplicateKeyException if another request already took this version
        ProductDescription saved = repo.insert(draft);

        // retire older unreviewed drafts so the reviewer sees one pending draft
        repo.findAllByProductIdAndStatus(productId, DescriptionStatus.DRAFT).stream()
                .filter(d -> !d.getId().equals(saved.getId()))
                .forEach(d -> {
                   // d.setStatus(DescriptionStatus.SUPERSEDED);
                    repo.save(d);
                });

        log.info("Draft v{} created for product {} by {}", nextVersion, productId, username);
        return saved;
    }
    public List<ProductDescription> approve(List<String> descriptionId, String reviewer){
        List<ProductDescription> productDescriptionsList = new ArrayList<>();
        for(String description : descriptionId){
            ProductDescription productDescription = approve(description, reviewer);
            productDescriptionsList.add(productDescription);
        }
        return productDescriptionsList;
    }
    /** Draft -> APPROVED. The previously approved version becomes SUPERSEDED. */
    public ProductDescription approve(String descriptionId, String reviewer) {
        ProductDescription target = find(descriptionId);
        requireDraft(target, "approve");

        // Old approved first: the partial unique index allows only one APPROVED per product.
        // (If the next save fails, the product has no live version until you retry. Enable
        // transactions on a replica set if you need this step to be atomic.)
        repo.findByProductIdAndStatus(target.getProductId(), DescriptionStatus.APPROVED)
                .ifPresent(old -> {
                    old.setStatus(DescriptionStatus.SUPERSEDED);
                    repo.save(old);
                });

        target.setStatus(DescriptionStatus.APPROVED);
        target.setReviewedBy(reviewer);
        target.setReviewedAt(Instant.now());
        ProductDescription saved = repo.save(target);   // @Version check: fails if someone changed it meanwhile

        log.info("Description {} (product {}, v{}) approved by {}",
                saved.getId(), saved.getProductId(), saved.getVersionNumber(), reviewer);
        return saved;
    }

    /** Draft -> REJECTED. The live APPROVED version is not affected. */
    public ProductDescription reject(String descriptionId, String reviewer, String reason) {
        ProductDescription target = find(descriptionId);
        requireDraft(target, "reject");

        target.setStatus(DescriptionStatus.REJECTED);
        target.setReviewedBy(reviewer);
        target.setReviewedAt(Instant.now());
        target.setRejectionReason(reason);
        return repo.save(target);
    }

    /** Only drafts can be edited. Approved text is immutable: edit = new version. */
    public ProductDescription editDraft(String descriptionId, String newContent) {
        ProductDescription target = find(descriptionId);
        requireDraft(target, "edit");
        target.setAiGeneratedDescription(newContent);
        return repo.save(target);
    }

    public List<ProductDescription> history(Long productId) {
        return repo.findByProductIdOrderByVersionNumberDesc(productId);
    }

    // ---- helpers ----

    private ProductDescription find(String id) {
        return repo.findById(id)
                .orElseThrow(() -> new DescriptionException.NotFound("Description " + id + " not found"));
    }

    private void requireDraft(ProductDescription d, String action) {
        if (d.getStatus() != DescriptionStatus.DRAFT) {
            throw new DescriptionException.InvalidState(
                    "Cannot " + action + " a description that is " + d.getStatus()
                            + " (only DRAFT allowed)");
        }
    }
    public List<ProductDescription> findAll() {
        return repo.findAll();
    }

    public void deleteById(String id) {
        repo.deleteById(id);
    }
}