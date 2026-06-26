package com.dosehope.backend.controller;

import com.dosehope.backend.dto.DonationRequestResponse;
import com.dosehope.backend.security.UserPrincipal;
import com.dosehope.backend.service.DonationRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationRequestController {

    private final DonationRequestService requestService;

    @GetMapping("/my-donations")
    @PreAuthorize("hasRole('DONOR')")
    public List<DonationRequestResponse> getMyDonations(@AuthenticationPrincipal UserPrincipal principal) {
        return requestService.getByDonor(principal.getId());
    }

    @GetMapping("/my-donations/stats")
    @PreAuthorize("hasRole('DONOR')")
    public Map<String, Long> getMyDonationStats(@AuthenticationPrincipal UserPrincipal principal) {
        return requestService.getDonorRequestStats(principal.getId());
    }

    @GetMapping("/ngo-requests")
    @PreAuthorize("hasRole('NGO')")
    public List<DonationRequestResponse> getNgoRequests(@AuthenticationPrincipal UserPrincipal principal) {
        return requestService.getByNgo(principal.getId());
    }

    @GetMapping("/ngo-requests/stats")
    @PreAuthorize("hasRole('NGO')")
    public Map<String, Long> getNgoStats(@AuthenticationPrincipal UserPrincipal principal) {
        return requestService.getNgoStats(principal.getId());
    }

    @GetMapping("/volunteer-assignments")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public List<DonationRequestResponse> getVolunteerAssignments(@AuthenticationPrincipal UserPrincipal principal) {
        return requestService.getByVolunteer(principal.getId());
    }

    @GetMapping("/volunteer-assignments/stats")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public Map<String, Long> getVolunteerStats(@AuthenticationPrincipal UserPrincipal principal) {
        return requestService.getVolunteerStats(principal.getId());
    }

    @PostMapping
    @PreAuthorize("hasRole('NGO')")
    public ResponseEntity<DonationRequestResponse> createRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam Long medicineId) {
        DonationRequestResponse response = requestService.createRequest(medicineId, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('NGO')")
    public DonationRequestResponse accept(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return requestService.acceptRequest(id, principal.getId());
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('NGO')")
    public DonationRequestResponse reject(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return requestService.rejectRequest(id, principal.getId());
    }

    @PutMapping("/{id}/assign-volunteer")
    @PreAuthorize("hasRole('NGO')")
    public DonationRequestResponse assignVolunteer(@PathVariable Long id, @RequestParam Long volunteerId, @AuthenticationPrincipal UserPrincipal principal) {
        return requestService.assignVolunteer(id, volunteerId, principal.getId());
    }

    @PutMapping("/{id}/pickup")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public DonationRequestResponse markPickedUp(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return requestService.markPickedUp(id, principal.getId());
    }

    @PutMapping("/{id}/deliver")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public DonationRequestResponse markDelivered(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return requestService.markDelivered(id, principal.getId());
    }
}
