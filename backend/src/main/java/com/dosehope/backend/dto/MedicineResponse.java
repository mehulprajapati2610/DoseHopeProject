package com.dosehope.backend.dto;

import com.dosehope.backend.entity.Medicine;
import com.dosehope.backend.entity.MedicineStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response shape for medicines. Mirrors the medicine object fields used
 * throughout frontend's data-store.js / browse.js / dashboard.js:
 * id, donorId, donorName, name, manufacturer, batchNumber,
 * manufacturingDate, expiryDate, quantity, category, description,
 * status, createdAt.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineResponse {

    private Long id;
    private Long donorId;
    private String donorName;
    private String name;
    private String manufacturer;
    private String batchNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private Integer quantity;
    private String category;
    private String description;
    private String image;
    private MedicineStatus status;
    private LocalDateTime createdAt;

    public static MedicineResponse fromEntity(Medicine medicine) {
        return MedicineResponse.builder()
                .id(medicine.getId())
                .donorId(medicine.getDonor().getId())
                .donorName(medicine.getDonor().getName())
                .name(medicine.getName())
                .manufacturer(medicine.getManufacturer())
                .batchNumber(medicine.getBatchNumber())
                .manufacturingDate(medicine.getManufacturingDate())
                .expiryDate(medicine.getExpiryDate())
                .quantity(medicine.getQuantity())
                .category(medicine.getCategory())
                .description(medicine.getDescription())
                .image(medicine.getImage())
                .status(medicine.getStatus())
                .createdAt(medicine.getCreatedAt())
                .build();
    }
}
