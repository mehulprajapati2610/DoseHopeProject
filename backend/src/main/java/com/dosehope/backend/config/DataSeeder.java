package com.dosehope.backend.config;

import com.dosehope.backend.entity.*;
import com.dosehope.backend.repository.DonationRequestRepository;
import com.dosehope.backend.repository.MedicineRepository;
import com.dosehope.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

  private final UserRepository userRepository;
  private final MedicineRepository medicineRepository;
  private final DonationRequestRepository requestRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) throws Exception {
    if (userRepository.count() > 0)
      return; // assume already seeded

    // Users
    User donor = User.builder()
        .name("Demo Donor")
        .email("donor@example.com")
        .password(passwordEncoder.encode("demo123"))
        .role(Role.DONOR)
        .phone("+910000000001")
        .address("Demo City")
        .build();

    User ngo = User.builder()
        .name("Demo NGO")
        .email("ngo@example.com")
        .password(passwordEncoder.encode("demo123"))
        .role(Role.NGO)
        .phone("+910000000002")
        .address("Demo City")
        .build();

    User vol = User.builder()
        .name("Demo Volunteer")
        .email("volunteer@example.com")
        .password(passwordEncoder.encode("demo123"))
        .role(Role.VOLUNTEER)
        .phone("+910000000003")
        .address("Demo City")
        .build();

    User admin = User.builder()
        .name("Admin")
        .email("admin@example.com")
        .password(passwordEncoder.encode("admin123"))
        .role(Role.ADMIN)
        .phone("+910000000004")
        .address("Demo City")
        .build();

    donor = userRepository.save(donor);
    ngo = userRepository.save(ngo);
    vol = userRepository.save(vol);
    admin = userRepository.save(admin);

    // Medicines
    Medicine med1 = Medicine.builder()
        .donor(donor)
        .name("Paracetamol 500mg")
        .manufacturer("Cipla")
        .batchNumber("PCM-1001")
        .manufacturingDate(LocalDate.now().minusDays(200))
        .expiryDate(LocalDate.now().plusDays(300))
        .quantity(20)
        .category("First Aid")
        .description("Demo paracetamol")
        .build();

    Medicine med2 = Medicine.builder()
        .donor(donor)
        .name("Cetirizine 10mg")
        .manufacturer("Mankind")
        .batchNumber("CTZ-2002")
        .manufacturingDate(LocalDate.now().minusDays(100))
        .expiryDate(LocalDate.now().plusDays(10))
        .quantity(10)
        .category("Allergy")
        .description("Expiring soon")
        .build();

    med1 = medicineRepository.save(med1);
    med2 = medicineRepository.save(med2);

    // Requests
    DonationRequest req = DonationRequest.builder()
        .medicine(med2)
        .donor(donor)
        .ngo(ngo)
        .status(RequestStatus.PENDING)
        .requestDate(LocalDate.now().minusDays(2))
        .build();

    requestRepository.save(req);

    System.out.println("[DataSeeder] Seeded demo users, medicines and a request.");
  }
}
