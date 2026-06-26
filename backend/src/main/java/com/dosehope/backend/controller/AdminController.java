package com.dosehope.backend.controller;

import com.dosehope.backend.dto.UserResponse;
import com.dosehope.backend.entity.DonationRequest;
import com.dosehope.backend.entity.Medicine;
import com.dosehope.backend.entity.MedicineStatus;
import com.dosehope.backend.entity.RequestStatus;
import com.dosehope.backend.entity.Role;
import com.dosehope.backend.repository.DonationRequestRepository;
import com.dosehope.backend.repository.MedicineRepository;
import com.dosehope.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

  private final UserRepository userRepository;
  private final MedicineRepository medicineRepository;
  private final DonationRequestRepository requestRepository;

  @GetMapping("/dashboard")
  @PreAuthorize("hasRole('ADMIN')")
  public Map<String, Long> dashboard() {
    long totalUsers = userRepository.count();
    long totalMedicines = medicineRepository.count();

    List<DonationRequest> allReqs = requestRepository.findAll();
    long pendingDonations = allReqs.stream().filter(r -> r.getStatus() == RequestStatus.PENDING).count();
    long completedDonations = allReqs.stream().filter(r -> r.getStatus() == RequestStatus.COMPLETED).count();

    long expiredMedicines = medicineRepository.findByStatus(MedicineStatus.EXPIRED).size();

    long activeVolunteers = userRepository.findByRole(Role.VOLUNTEER).size();
    long registeredNgos = userRepository.findByRole(Role.NGO).size();

    return Map.of(
        "totalUsers", totalUsers,
        "totalMedicines", totalMedicines,
        "pendingDonations", pendingDonations,
        "completedDonations", completedDonations,
        "expiredMedicines", expiredMedicines,
        "activeVolunteers", activeVolunteers,
        "registeredNgos", registeredNgos);
  }

  @GetMapping("/users")
  @PreAuthorize("hasRole('ADMIN')")
  public List<UserResponse> allUsers() {
    return userRepository.findAll().stream()
        .map(com.dosehope.backend.dto.UserResponse::fromEntity)
        .collect(Collectors.toList());
  }

  @GetMapping("/analytics")
  @PreAuthorize("hasRole('ADMIN')")
  public Map<String, Object> analytics() {
    // Minimal analytics: reuse dashboard metrics and add simple lists sizes
    Map<String, Long> db = dashboard();
    return Map.of(
        "summary", db);
  }
}
