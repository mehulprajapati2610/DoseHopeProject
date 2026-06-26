package com.dosehope.backend.repository;

import com.dosehope.backend.entity.Medicine;
import com.dosehope.backend.entity.MedicineStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findByDonorId(Long donorId);

    List<Medicine> findByStatus(MedicineStatus status);

    List<Medicine> findByStatusAndExpiryDateGreaterThanEqual(MedicineStatus status, LocalDate date);

    List<Medicine> findByCategoryAndStatus(String category, MedicineStatus status);

    List<Medicine> findByNameContainingIgnoreCaseAndStatus(String name, MedicineStatus status);
}
