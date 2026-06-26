package com.dosehope.backend.repository;

import com.dosehope.backend.entity.DonationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationRequestRepository extends JpaRepository<DonationRequest, Long> {

    List<DonationRequest> findByDonorId(Long donorId);

    List<DonationRequest> findByNgoId(Long ngoId);

    List<DonationRequest> findByVolunteerId(Long volunteerId);

    List<DonationRequest> findByMedicineId(Long medicineId);
}
