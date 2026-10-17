package com.avnish.descriptAI_backend.model;
import com.avnish.descriptAI_backend.constants.DescriptionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * One document = one generated version of a product's description (MongoDB).
 *
 * Two unique indexes protect the data at database level:
 *  1. (productId, versionNumber)       -> a version number can exist only once per product
 *  2. productId WHERE status=APPROVED  -> at most ONE approved description per product
 *
 * Needs: spring.data.mongodb.auto-index-creation=true  (off by default)
 */
@Document(collection = "product_descriptions")
@CompoundIndexes({
        @CompoundIndex(
                name = "uniq_product_version",
                def = "{'productId': 1, 'versionNumber': 1}",
                unique = true),
        @CompoundIndex(
                name = "uniq_product_approved",
                def = "{'productId': 1}",
                unique = true,
                partialFilter = "{'status': 'APPROVED'}")
})
@Getter
@Setter
@NoArgsConstructor
public class ProductDescription {

    @Id
    private String id;                    // MongoDB ObjectId as String

    private Long productId;
    private String productName;
    private String category;
    private String originalDescription;
    private int versionNumber;
    private DescriptionStatus status;     // stored as a string, e.g. "DRAFT"
    private String aiGeneratedDescription;
    private String imageURL;
    // --- audit trail ---
    private String aiModel;                 // model that produced it
    private String createdBy;             // user who clicked Generate / Regenerate
    private Instant createdAt;

    private String reviewedBy;            // who approved or rejected
    private Instant reviewedAt;
    private String rejectionReason;

    @Version
    private Long lockVersion;             // optimistic locking: two reviewers cannot act on the same document
}