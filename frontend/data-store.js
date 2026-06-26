/*
  API-backed DoseHope data-store.
  Replaces the previous localStorage mock with real REST calls to the
  backend while preserving the same function signatures used by the
  frontend pages. Responses are mapped to the original frontend shapes
  (string IDs, yyyy-mm-dd dates) so existing UI code needs minimal changes.

  Configure `API_BASE` if backend runs on a different host/port.
*/

const API_BASE = '' ; // e.g. 'http://localhost:8080'

const DB_KEYS = {
  CURRENT_USER: 'dosehope_current_user',
  TOKEN: 'dosehope_token'
};

/* ---------- helper: fetch wrapper with auth ---------- */
async function apiFetch(path, opts = {}) {
  const token = localStorage.getItem(DB_KEYS.TOKEN);
  const headers = { 'Content-Type': 'application/json', ...(opts.headers || {}) };
  if (token) headers['Authorization'] = 'Bearer ' + token;

  const res = await fetch(API_BASE + path, { ...opts, headers });
  const text = await res.text();
  const body = text ? JSON.parse(text) : null;
  if (!res.ok) {
    // normalize error shape
    const msg = (body && (body.message || body.error || body)) || res.statusText;
    return { ok: false, status: res.status, error: msg };
  }
  return { ok: true, data: body };
}

/* ---------- mapping helpers (server -> frontend shapes) ---------- */
function mapUser(u) {
  if (!u) return null;
  return {
    id: String(u.id),
    name: u.name,
    email: u.email,
    role: u.role,
    phone: u.phone || '',
    address: u.address || ''
  };
}

function toDateString(d) {
  if (!d) return null;
  // accept yyyy-MM-dd or ISO — return yyyy-MM-dd
  const dt = new Date(d);
  if (Number.isNaN(dt.getTime())) return d;
  return dt.toISOString().slice(0, 10);
}

function mapMedicine(m) {
  if (!m) return null;
  return {
    id: String(m.id),
    donorId: String(m.donorId),
    donorName: m.donorName || (m.donor && m.donor.name) || '',
    name: m.name,
    manufacturer: m.manufacturer,
    batchNumber: m.batchNumber,
    manufacturingDate: toDateString(m.manufacturingDate),
    expiryDate: toDateString(m.expiryDate),
    quantity: m.quantity,
    category: m.category,
    description: m.description,
    image: m.image || null,
    status: (m.status && String(m.status)) || 'Available',
    createdAt: m.createdAt ? toDateString(m.createdAt) : null
  };
}

function mapRequest(r) {
  if (!r) return null;
  return {
    id: String(r.id),
    medicineId: r.medicineId ? String(r.medicineId) : (r.medicine && String(r.medicine.id)),
    medicineName: r.medicineName || (r.medicine && r.medicine.name) || '',
    donorId: r.donorId ? String(r.donorId) : (r.donor && String(r.donor.id)),
    ngoId: r.ngoId ? String(r.ngoId) : (r.ngo && String(r.ngo.id)),
    ngoName: r.ngoName || (r.ngo && r.ngo.name) || '',
    volunteerId: r.volunteerId ? String(r.volunteerId) : null,
    status: r.status,
    requestDate: toDateString(r.requestDate),
    pickupDate: toDateString(r.pickupDate),
    deliveryDate: toDateString(r.deliveryDate)
  };
}

/* ---------- Auth ---------- */
async function registerUser({ name, email, password, role, phone, address }) {
  const payload = { name, email, password, role, phone, address };
  const res = await apiFetch('/api/auth/register', { method: 'POST', body: JSON.stringify(payload) });
  if (!res.ok) return { ok: false, error: res.error };
  const token = res.data.token;
  const user = mapUser(res.data.user);
  localStorage.setItem(DB_KEYS.TOKEN, token);
  localStorage.setItem(DB_KEYS.CURRENT_USER, JSON.stringify(user));
  return { ok: true, user };
}

async function loginUser(email, password) {
  const res = await apiFetch('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) });
  if (!res.ok) return { ok: false, error: res.error };
  const token = res.data.token;
  const user = mapUser(res.data.user);
  localStorage.setItem(DB_KEYS.TOKEN, token);
  localStorage.setItem(DB_KEYS.CURRENT_USER, JSON.stringify(user));
  return { ok: true, user };
}

function setCurrentUser(user) {
  localStorage.setItem(DB_KEYS.CURRENT_USER, JSON.stringify(mapUser(user)));
}

function getCurrentUser() {
  const raw = localStorage.getItem(DB_KEYS.CURRENT_USER);
  return raw ? JSON.parse(raw) : null;
}

function logoutUser() {
  localStorage.removeItem(DB_KEYS.CURRENT_USER);
  localStorage.removeItem(DB_KEYS.TOKEN);
}

function requireLogin(redirectTo, expectedRole) {
  const user = getCurrentUser();
  if (!user) {
    window.location.href = redirectTo || 'login.html';
    return null;
  }
  if (expectedRole && user.role !== expectedRole) {
    window.location.href = getDashboardForRole(user.role);
    return null;
  }
  return user;
}

/* ---------- Medicines ---------- */
async function getMedicines() {
  const res = await apiFetch('/api/medicines');
  if (!res.ok) return [];
  return Array.isArray(res.data) ? res.data.map(mapMedicine) : [];
}

async function getMedicineById(id) {
  const res = await apiFetch(`/api/medicines/${id}`);
  if (!res.ok) return null;
  return mapMedicine(res.data);
}

async function getMedicinesByDonor(donorId) {
  // server returns numeric ids; we map to string to keep UI consistent
  const res = await apiFetch('/api/medicines/my-listings');
  if (!res.ok) return [];
  return Array.isArray(res.data) ? res.data.map(mapMedicine) : [];
}

function isExpired(expiryDate) {
  return new Date(expiryDate) < new Date(new Date().toDateString());
}

function daysUntil(dateStr) {
  const diff = new Date(dateStr) - new Date(new Date().toDateString());
  return Math.ceil(diff / (1000 * 60 * 60 * 24));
}

async function addMedicine(med) {
  // med fields match MedicineRequest DTO keys
  const payload = {
    name: med.name,
    manufacturer: med.manufacturer,
    batchNumber: med.batchNumber,
    manufacturingDate: med.manufacturingDate,
    expiryDate: med.expiryDate,
    quantity: med.quantity,
    category: med.category,
    description: med.description,
    image: med.image || null
  };
  const res = await apiFetch('/api/medicines', { method: 'POST', body: JSON.stringify(payload) });
  if (!res.ok) return { ok: false, error: res.error };
  return { ok: true, medicine: mapMedicine(res.data) };
}

async function updateMedicineStatus(medicineId, status) {
  const res = await apiFetch(`/api/medicines/${medicineId}/status?status=${encodeURIComponent(status)}`, { method: 'PUT' });
  if (!res.ok) return { ok: false, error: res.error };
  return { ok: true, medicine: mapMedicine(res.data) };
}

/* ---------- Donation Requests ---------- */
async function getRequests() {
  const res = await apiFetch('/api/donations');
  if (!res.ok) return [];
  return Array.isArray(res.data) ? res.data.map(mapRequest) : [];
}

async function getRequestsByDonor(donorId) {
  const res = await apiFetch('/api/donations/my-donations');
  if (!res.ok) return [];
  return Array.isArray(res.data) ? res.data.map(mapRequest) : [];
}

async function createRequest({ medicineId }) {
  const res = await apiFetch(`/api/donations?medicineId=${encodeURIComponent(medicineId)}`, { method: 'POST' });
  if (!res.ok) return { ok: false, error: res.error };
  return { ok: true, request: mapRequest(res.data) };
}

/* ---------- Dashboard stats ---------- */
async function getDonorStats(donorId) {
  // merge medicine stats and request stats from two endpoints
  const medsRes = await apiFetch('/api/medicines/my-listings/stats');
  const reqRes = await apiFetch('/api/donations/my-donations/stats');
  const meds = medsRes.ok ? medsRes.data : {};
  const reqs = reqRes.ok ? reqRes.data : {};
  return {
    totalDonated: meds.totalDonated || 0,
    available: meds.available || 0,
    pending: reqs.pending || 0,
    completed: reqs.completed || 0,
    expiringSoon: meds.expiringSoon || 0,
    expired: meds.expired || 0
  };
}

/* ---------- NGO functions ---------- */
async function getAllVolunteers() {
  const res = await apiFetch('/api/users/volunteers');
  if (!res.ok) return [];
  return Array.isArray(res.data) ? res.data.map(mapUser) : [];
}

async function getRequestsByNgo(ngoId) {
  const res = await apiFetch('/api/donations/ngo-requests');
  if (!res.ok) return [];
  return Array.isArray(res.data) ? res.data.map(mapRequest) : [];
}

async function acceptRequest(requestId) {
  const res = await apiFetch(`/api/donations/${requestId}/accept`, { method: 'PUT' });
  if (!res.ok) return { ok: false, error: res.error };
  return { ok: true, request: mapRequest(res.data) };
}

async function rejectRequest(requestId) {
  const res = await apiFetch(`/api/donations/${requestId}/reject`, { method: 'PUT' });
  if (!res.ok) return { ok: false, error: res.error };
  return { ok: true, request: mapRequest(res.data) };
}

async function assignVolunteer(requestId, volunteerId) {
  const res = await apiFetch(`/api/donations/${requestId}/assign-volunteer?volunteerId=${encodeURIComponent(volunteerId)}`, { method: 'PUT' });
  if (!res.ok) return { ok: false, error: res.error };
  return { ok: true, request: mapRequest(res.data) };
}

async function getNgoStats(ngoId) {
  const res = await apiFetch('/api/donations/ngo-requests/stats');
  if (!res.ok) return { totalRequests: 0, pending: 0, inProgress: 0, completed: 0 };
  return res.data;
}

/* ---------- Volunteer functions ---------- */
async function getRequestsByVolunteer(volunteerId) {
  const res = await apiFetch('/api/donations/volunteer-assignments');
  if (!res.ok) return [];
  return Array.isArray(res.data) ? res.data.map(mapRequest) : [];
}

async function markPickedUp(requestId) {
  const res = await apiFetch(`/api/donations/${requestId}/pickup`, { method: 'PUT' });
  if (!res.ok) return { ok: false, error: res.error };
  return { ok: true, request: mapRequest(res.data) };
}

async function markDelivered(requestId) {
  const res = await apiFetch(`/api/donations/${requestId}/deliver`, { method: 'PUT' });
  if (!res.ok) return { ok: false, error: res.error };
  return { ok: true, request: mapRequest(res.data) };
}

async function getVolunteerStats(volunteerId) {
  const res = await apiFetch('/api/donations/volunteer-assignments/stats');
  if (!res.ok) return { pendingPickup: 0, pendingDelivery: 0, completed: 0 };
  return res.data;
}

/* ---------- Redirect helper by role ---------- */
function getDashboardForRole(role) {
  if (role === 'NGO') return 'ngo-dashboard.html';
  if (role === 'VOLUNTEER') return 'volunteer-dashboard.html';
  return 'dashboard.html'; // DONOR
}

