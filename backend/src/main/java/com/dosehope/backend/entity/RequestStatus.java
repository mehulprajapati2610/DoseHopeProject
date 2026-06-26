package com.dosehope.backend.entity;

/**
 * Mirrors the donationRequest.status string values used in the frontend's
 * data-store.js (Pending, Accepted, Volunteer Assigned, Picked Up, Completed,
 * Cancelled). Keep these names in sync if the frontend changes.
 */
public enum RequestStatus {
    PENDING,
    ACCEPTED,
    VOLUNTEER_ASSIGNED,
    PICKED_UP,
    COMPLETED,
    CANCELLED
}
