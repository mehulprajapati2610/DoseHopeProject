package com.dosehope.backend.service;

import com.dosehope.backend.dto.MedicineRequest;
import com.dosehope.backend.dto.MedicineResponse;
import com.dosehope.backend.entity.Medicine;
import com.dosehope.backend.entity.MedicineStatus;
import com.dosehope.backend.entity.User;
import com.dosehope.backend.exception.InvalidOperationException;
import com.dosehope.backend.exception.ResourceNotFoundException;
import com.dosehope.backend.repository.MedicineRepository;
import com.dosehope.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Mirrors frontend's data-store.js medicine functions:
 * getMedicines, getMedicineById, getMedicinesByDonor, isExpired, daysUntil,
 * addMedicine, updateMedicineStatus, getDonorStats.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final UserRepository userRepository;

    public List<MedicineResponse> getAllAvailable() {
        return medicineRepository.findByStatusAndExpiryDateGreaterThanEqual(MedicineStatus.AVAILABLE, LocalDate.now())
                .stream()
                .map(MedicineResponse::fromEntity)
                .toList();
    }

    public List<MedicineResponse> getAllAvailable(String category, String searchName) {
        List<Medicine> medicines = medicineRepository.findByStatusAndExpiryDateGreaterThanEqual(MedicineStatus.AVAILABLE, LocalDate.now());

        return medicines.stream()
                .filter(m -> category == null || category.isBlank() || m.getCategory().equalsIgnoreCase(category))
                .filter(m -> searchName == null || searchName.isBlank() || m.getName().toLowerCase().contains(searchName.toLowerCase()))
                .map(MedicineResponse::fromEntity)
                .toList();
    }

    public MedicineResponse getById(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));
        return MedicineResponse.fromEntity(medicine);
    }

    public List<MedicineResponse> getByDonor(Long donorId) {
        return medicineRepository.findByDonorId(donorId)
                .stream()
                .map(MedicineResponse::fromEntity)
                .toList();
    }

    /**
     * Mirrors frontend's addMedicine() — rejects expired medicine before
     * it's ever persisted, exactly like the frontend's isExpired() guard.
     */
    public MedicineResponse addMedicine(Long donorId, MedicineRequest request) {
        if (request.getExpiryDate().isBefore(LocalDate.now())) {
            throw new InvalidOperationException("This medicine has already expired and cannot be listed.");
        }
        if (!request.getManufacturingDate().isBefore(request.getExpiryDate())) {
            throw new InvalidOperationException("Expiry date must be after the manufacturing date.");
        }

        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with id: " + donorId));

        Medicine medicine = Medicine.builder()
                .donor(donor)
                .name(request.getName())
                .manufacturer(request.getManufacturer())
                .batchNumber(request.getBatchNumber())
                .manufacturingDate(request.getManufacturingDate())
                .expiryDate(request.getExpiryDate())
                .quantity(request.getQuantity())
                .category(request.getCategory())
                .description(request.getDescription())
                .image(request.getImage())
                .status(MedicineStatus.AVAILABLE)
                .build();

        Medicine saved = medicineRepository.save(medicine);
        return MedicineResponse.fromEntity(saved);
    }

    public MedicineResponse updateStatus(Long medicineId, MedicineStatus status) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + medicineId));
        medicine.setStatus(status);
        Medicine saved = medicineRepository.save(medicine);
        return MedicineResponse.fromEntity(saved);
    }

    /**
     * Mirrors frontend's getDonorStats(): totalDonated, available, pending,
     * completed, expiringSoon, expired — used by dashboard.html.
     * Note: "pending"/"completed" counts that depend on DonationRequest
     * status are computed in DonationRequestService and merged by the
     * controller, since this service only owns Medicine data.
     */
    public Map<String, Long> getDonorMedicineStats(Long donorId) {
        List<Medicine> meds = medicineRepository.findByDonorId(donorId);
        LocalDate today = LocalDate.now();

        long totalDonated = meds.size();
        long available = meds.stream().filter(m -> m.getStatus() == MedicineStatus.AVAILABLE).count();
        long expiringSoon = meds.stream()
                .filter(m -> m.getStatus() == MedicineStatus.AVAILABLE)
                .filter(m -> {
                    long days = today.until(m.getExpiryDate()).getDays();
                    return days >= 0 && days <= 30;
                })
                .count();
        long expired = meds.stream()
                .filter(m -> m.getStatus() == MedicineStatus.EXPIRED || m.getExpiryDate().isBefore(today))
                .count();

        return Map.of(
                "totalDonated", totalDonated,
                "available", available,
                "expiringSoon", expiringSoon,
                "expired", expired
        );
    }

    public boolean isExpired(LocalDate expiryDate) {
        return expiryDate.isBefore(LocalDate.now());
    }

    public long daysUntil(LocalDate expiryDate) {
        return LocalDate.now().until(expiryDate).getDays();
    }
}
