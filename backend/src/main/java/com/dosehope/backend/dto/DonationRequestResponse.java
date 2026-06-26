package com.dosehope.backend.dto;

import com.dosehope.backend.entity.DonationRequest;
import com.dosehope.backend.entity.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response shape for donation requests. Mirrors the request object fields
 * used in frontend's data-store.js / browse.js / dashboard.js / ngo-dashboard.js
 * / volunteer-dashboard.js:
 * id, medicineId, medicineName, donorId, ngoId, ngoName, volunteerId,
 * status, requestDate, pickupDate, deliveryDate.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationRequestResponse {

    private Long id;
    private Long medicineId;
    private String medicineName;
    private Long donorId;
    private Long ngoId;
    private String ngoName;
    private Long volunteerId;
    private String volunteerName;
    private RequestStatus status;
    private LocalDate requestDate;
    private LocalDate pickupDate;
    private LocalDate deliveryDate;
    private LocalDateTime createdAt;

    public static DonationRequestResponse fromEntity(DonationRequest request) {
        return DonationRequestResponse.builder()
                .id(request.getId())
                .medicineId(request.getMedicine().getId())
                .medicineName(request.getMedicine().getName())
                .donorId(request.getDonor().getId())
                .ngoId(request.getNgo().getId())
                .ngoName(request.getNgo().getName())
                .volunteerId(request.getVolunteer() != null ? request.getVolunteer().getId() : null)
                .volunteerName(request.getVolunteer() != null ? request.getVolunteer().getName() : null)
                .status(request.getStatus())
                .requestDate(request.getRequestDate())
                .pickupDate(request.getPickupDate())
                .deliveryDate(request.getDeliveryDate())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
