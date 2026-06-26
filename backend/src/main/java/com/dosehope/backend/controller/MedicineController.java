package com.dosehope.backend.controller;

import com.dosehope.backend.dto.MedicineRequest;
import com.dosehope.backend.dto.MedicineResponse;
import com.dosehope.backend.entity.MedicineStatus;
import com.dosehope.backend.security.UserPrincipal;
import com.dosehope.backend.service.MedicineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    // Public browse — matches frontend's browse.html (no login wall on viewing)
    @GetMapping
    public List<MedicineResponse> getAvailable(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        return medicineService.getAllAvailable(category, search);
    }

    @GetMapping("/{id}")
    public MedicineResponse getById(@PathVariable Long id) {
        return medicineService.getById(id);
    }

    @GetMapping("/my-listings")
    @PreAuthorize("hasRole('DONOR')")
    public List<MedicineResponse> getMyListings(@AuthenticationPrincipal UserPrincipal principal) {
        return medicineService.getByDonor(principal.getId());
    }

    @GetMapping("/my-listings/stats")
    @PreAuthorize("hasRole('DONOR')")
    public Map<String, Long> getMyStats(@AuthenticationPrincipal UserPrincipal principal) {
        return medicineService.getDonorMedicineStats(principal.getId());
    }

    @PostMapping
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<MedicineResponse> addMedicine(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody MedicineRequest request) {
        MedicineResponse response = medicineService.addMedicine(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('NGO', 'VOLUNTEER', 'ADMIN')")
    public MedicineResponse updateStatus(@PathVariable Long id, @RequestParam MedicineStatus status) {
        return medicineService.updateStatus(id, status);
    }
}
