package com.dosehope.backend.controller;

import com.dosehope.backend.dto.DonationRequestResponse;
import com.dosehope.backend.dto.UserResponse;
import com.dosehope.backend.security.UserPrincipal;
import com.dosehope.backend.service.DonationRequestService;
import com.dosehope.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/volunteers")
@RequiredArgsConstructor
public class VolunteerController {

  private final UserService userService;
  private final DonationRequestService requestService;

  @GetMapping
  public List<UserResponse> getAllVolunteers() {
    return userService.getAllVolunteers();
  }

  @GetMapping("/assignments")
  @PreAuthorize("hasRole('VOLUNTEER')")
  public List<DonationRequestResponse> getAssignments(@AuthenticationPrincipal UserPrincipal principal) {
    return requestService.getByVolunteer(principal.getId());
  }

  @GetMapping("/assignments/stats")
  @PreAuthorize("hasRole('VOLUNTEER')")
  public java.util.Map<String, Long> getAssignmentStats(@AuthenticationPrincipal UserPrincipal principal) {
    return requestService.getVolunteerStats(principal.getId());
  }
}
