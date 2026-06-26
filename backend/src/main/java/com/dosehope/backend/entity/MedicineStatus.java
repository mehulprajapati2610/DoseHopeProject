package com.dosehope.backend.entity;

/**
 * Mirrors the medicine.status string values used in the frontend's
 * data-store.js (Available, Requested, Accepted, Picked, Delivered, Expired).
 * Keep these names in sync if the frontend changes.
 */
public enum MedicineStatus {
    AVAILABLE,
    REQUESTED,
    ACCEPTED,
    PICKED,
    DELIVERED,
    EXPIRED
}
