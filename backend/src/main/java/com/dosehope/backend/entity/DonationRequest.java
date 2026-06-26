package com.dosehope.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "donation_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Maps to frontend's request.medicineId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    // Maps to frontend's request.donorId (denormalized for quick lookup,
    // also derivable via medicine.donor — kept explicit to match frontend shape)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    // Maps to frontend's request.ngoId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ngo_id", nullable = false)
    private User ngo;

    // Maps to frontend's request.volunteerId — nullable until assigned
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id")
    private User volunteer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "pickup_date")
    private LocalDate pickupDate;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.requestDate == null) {
            this.requestDate = LocalDate.now();
        }
        if (this.status == null) {
            this.status = RequestStatus.PENDING;
        }
    }
}
