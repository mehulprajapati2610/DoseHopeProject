package com.dosehope.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Maps to frontend's medicine.donorId — FK to the donor who listed it.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String manufacturer;

    @NotBlank
    @Column(name = "batch_number", nullable = false)
    private String batchNumber;

    @NotNull
    @Column(name = "manufacturing_date", nullable = false)
    private LocalDate manufacturingDate;

    @NotNull
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    @NotBlank
    @Column(nullable = false)
    private String category;

    @Column(length = 1000)
    private String description;

    private String image; // Cloudinary URL, populated later when upload is wired in

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MedicineStatus status = MedicineStatus.AVAILABLE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = MedicineStatus.AVAILABLE;
        }
    }
}
