package com.dosehope.backend.service;

import com.dosehope.backend.dto.DonationRequestResponse;
import com.dosehope.backend.entity.*;
import com.dosehope.backend.exception.InvalidOperationException;
import com.dosehope.backend.exception.ResourceNotFoundException;
import com.dosehope.backend.repository.DonationRequestRepository;
import com.dosehope.backend.repository.MedicineRepository;
import com.dosehope.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Mirrors frontend's data-store.js request/NGO/volunteer functions:
 * createRequest, getRequestsByDonor/Ngo/Volunteer, acceptRequest,
 * rejectRequest, assignVolunteer, markPickedUp, markDelivered,
 * getNgoStats, getVolunteerStats.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DonationRequestService {

    private final DonationRequestRepository requestRepository;
    private final MedicineRepository medicineRepository;
    private final UserRepository userRepository;

    public List<DonationRequestResponse> getByDonor(Long donorId) {
        return requestRepository.findByDonorId(donorId).stream()
                .map(DonationRequestResponse::fromEntity)
                .toList();
    }

    public List<DonationRequestResponse> getByNgo(Long ngoId) {
        return requestRepository.findByNgoId(ngoId).stream()
                .map(DonationRequestResponse::fromEntity)
                .toList();
    }

    public List<DonationRequestResponse> getByVolunteer(Long volunteerId) {
        return requestRepository.findByVolunteerId(volunteerId).stream()
                .map(DonationRequestResponse::fromEntity)
                .toList();
    }

    /**
     * Mirrors frontend's createRequest(): only an AVAILABLE medicine can be
     * requested; on success the medicine flips to REQUESTED and a new
     * request row is created with status PENDING.
     */
    public DonationRequestResponse createRequest(Long medicineId, Long ngoId) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + medicineId));

        if (medicine.getStatus() != MedicineStatus.AVAILABLE) {
            throw new InvalidOperationException("This medicine is no longer available to request.");
        }

        User ngo = userRepository.findById(ngoId)
                .orElseThrow(() -> new ResourceNotFoundException("NGO not found with id: " + ngoId));

        DonationRequest request = DonationRequest.builder()
                .medicine(medicine)
                .donor(medicine.getDonor())
                .ngo(ngo)
                .status(RequestStatus.PENDING)
                .requestDate(LocalDate.now())
                .build();

        DonationRequest saved = requestRepository.save(request);

        medicine.setStatus(MedicineStatus.REQUESTED);
        medicineRepository.save(medicine);

        return DonationRequestResponse.fromEntity(saved);
    }

    /**
     * Mirrors frontend's acceptRequest(): request -> ACCEPTED,
     * medicine -> ACCEPTED.
     */
    public DonationRequestResponse acceptRequest(Long requestId, Long ngoId) {
        DonationRequest request = getRequestOrThrow(requestId);
        if (!request.getNgo().getId().equals(ngoId)) {
            throw new org.springframework.security.access.AccessDeniedException("You do not own this request.");
        }
        request.setStatus(RequestStatus.ACCEPTED);
        DonationRequest saved = requestRepository.save(request);

        Medicine medicine = saved.getMedicine();
        medicine.setStatus(MedicineStatus.ACCEPTED);
        medicineRepository.save(medicine);

        return DonationRequestResponse.fromEntity(saved);
    }

    /**
     * Mirrors frontend's rejectRequest(): request -> CANCELLED,
     * medicine reverts back to AVAILABLE so it can be requested again.
     */
    public DonationRequestResponse rejectRequest(Long requestId, Long ngoId) {
        DonationRequest request = getRequestOrThrow(requestId);
        if (!request.getNgo().getId().equals(ngoId)) {
            throw new org.springframework.security.access.AccessDeniedException("You do not own this request.");
        }
        request.setStatus(RequestStatus.CANCELLED);
        DonationRequest saved = requestRepository.save(request);

        Medicine medicine = saved.getMedicine();
        medicine.setStatus(MedicineStatus.AVAILABLE);
        medicineRepository.save(medicine);

        return DonationRequestResponse.fromEntity(saved);
    }

    /**
     * Mirrors frontend's assignVolunteer(): validates the volunteer exists
     * and actually has the VOLUNTEER role, then request -> VOLUNTEER_ASSIGNED.
     */
    public DonationRequestResponse assignVolunteer(Long requestId, Long volunteerId, Long ngoId) {
        User volunteer = userRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer not found with id: " + volunteerId));

        if (volunteer.getRole() != Role.VOLUNTEER) {
            throw new InvalidOperationException("Selected user is not registered as a volunteer.");
        }

        DonationRequest request = getRequestOrThrow(requestId);
        if (!request.getNgo().getId().equals(ngoId)) {
            throw new org.springframework.security.access.AccessDeniedException("You do not own this request.");
        }
        request.setVolunteer(volunteer);
        request.setStatus(RequestStatus.VOLUNTEER_ASSIGNED);
        DonationRequest saved = requestRepository.save(request);

        Medicine medicine = saved.getMedicine();
        medicine.setStatus(MedicineStatus.ACCEPTED);
        medicineRepository.save(medicine);

        return DonationRequestResponse.fromEntity(saved);
    }

    /**
     * Mirrors frontend's markPickedUp(): request -> PICKED_UP with today's
     * date stamped, medicine -> PICKED.
     */
    public DonationRequestResponse markPickedUp(Long requestId, Long volunteerId) {
        DonationRequest request = getRequestOrThrow(requestId);
        if (request.getVolunteer() == null || !request.getVolunteer().getId().equals(volunteerId)) {
            throw new org.springframework.security.access.AccessDeniedException("This request is not assigned to you.");
        }
        request.setStatus(RequestStatus.PICKED_UP);
        request.setPickupDate(LocalDate.now());
        DonationRequest saved = requestRepository.save(request);

        Medicine medicine = saved.getMedicine();
        medicine.setStatus(MedicineStatus.PICKED);
        medicineRepository.save(medicine);

        return DonationRequestResponse.fromEntity(saved);
    }

    /**
     * Mirrors frontend's markDelivered(): request -> COMPLETED with today's
     * date stamped, medicine -> DELIVERED.
     */
    public DonationRequestResponse markDelivered(Long requestId, Long volunteerId) {
        DonationRequest request = getRequestOrThrow(requestId);
        if (request.getVolunteer() == null || !request.getVolunteer().getId().equals(volunteerId)) {
            throw new org.springframework.security.access.AccessDeniedException("This request is not assigned to you.");
        }
        request.setStatus(RequestStatus.COMPLETED);
        request.setDeliveryDate(LocalDate.now());
        DonationRequest saved = requestRepository.save(request);

        Medicine medicine = saved.getMedicine();
        medicine.setStatus(MedicineStatus.DELIVERED);
        medicineRepository.save(medicine);

        return DonationRequestResponse.fromEntity(saved);
    }

    /**
     * Mirrors frontend's getNgoStats(): totalRequests, pending, inProgress,
     * completed — used by ngo-dashboard.html.
     */
    public Map<String, Long> getNgoStats(Long ngoId) {
        List<DonationRequest> reqs = requestRepository.findByNgoId(ngoId);

        long total = reqs.size();
        long pending = reqs.stream().filter(r -> r.getStatus() == RequestStatus.PENDING).count();
        long inProgress = reqs.stream()
                .filter(r -> r.getStatus() == RequestStatus.ACCEPTED
                        || r.getStatus() == RequestStatus.VOLUNTEER_ASSIGNED
                        || r.getStatus() == RequestStatus.PICKED_UP)
                .count();
        long completed = reqs.stream().filter(r -> r.getStatus() == RequestStatus.COMPLETED).count();

        return Map.of(
                "totalRequests", total,
                "pending", pending,
                "inProgress", inProgress,
                "completed", completed
        );
    }

    /**
     * Mirrors frontend's getVolunteerStats(): pendingPickup, pendingDelivery,
     * completed — used by volunteer-dashboard.html.
     */
    public Map<String, Long> getVolunteerStats(Long volunteerId) {
        List<DonationRequest> reqs = requestRepository.findByVolunteerId(volunteerId);

        long pendingPickup = reqs.stream().filter(r -> r.getStatus() == RequestStatus.VOLUNTEER_ASSIGNED).count();
        long pendingDelivery = reqs.stream().filter(r -> r.getStatus() == RequestStatus.PICKED_UP).count();
        long completed = reqs.stream().filter(r -> r.getStatus() == RequestStatus.COMPLETED).count();

        return Map.of(
                "pendingPickup", pendingPickup,
                "pendingDelivery", pendingDelivery,
                "completed", completed
        );
    }

    /**
     * Mirrors frontend's getDonorStats() request-side counts (pending,
     * completed) — merged with MedicineService's medicine-side counts
     * by the controller to match the full frontend dashboard shape.
     */
    public Map<String, Long> getDonorRequestStats(Long donorId) {
        List<DonationRequest> reqs = requestRepository.findByDonorId(donorId);

        long pending = reqs.stream()
                .filter(r -> r.getStatus() == RequestStatus.PENDING
                        || r.getStatus() == RequestStatus.ACCEPTED
                        || r.getStatus() == RequestStatus.VOLUNTEER_ASSIGNED
                        || r.getStatus() == RequestStatus.PICKED_UP)
                .count();
        long completed = reqs.stream().filter(r -> r.getStatus() == RequestStatus.COMPLETED).count();

        return Map.of(
                "pending", pending,
                "completed", completed
        );
    }

    private DonationRequest getRequestOrThrow(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));
    }
}
