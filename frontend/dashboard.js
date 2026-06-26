// ===== Guard: must be logged in =====
const currentUser = requireLogin('login.html', 'DONOR');
// If not logged in, requireLogin() already redirected — stop here so
// nothing below tries to read properties off a null user.
if (currentUser) {

  function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
  }

  document.getElementById('userPill').textContent = currentUser.name + ' · ' + currentUser.role;
  document.getElementById('greetingName').textContent = 'Welcome back, ' + currentUser.name.split(' ')[0];

  document.getElementById('logoutBtn').addEventListener('click', () => {
    logoutUser();
    window.location.href = 'index.html';
  });

  // ===== Stats =====
  async function renderStats() {
      const stats = await getDonorStats(currentUser.id);
    const statsRow = document.getElementById('statsRow');

    statsRow.innerHTML = `
      <div class="stat-card">
        <strong>${stats.totalDonated}</strong>
        <span>Medicines listed</span>
      </div>
      <div class="stat-card">
        <strong>${stats.pending}</strong>
        <span>Pending requests</span>
      </div>
      <div class="stat-card">
        <strong>${stats.completed}</strong>
        <span>Completed donations</span>
      </div>
      <div class="stat-card ${stats.expiringSoon > 0 ? 'alert' : ''}">
        <strong>${stats.expiringSoon}</strong>
        <span>Expiring within 30 days</span>
      </div>
    `;
  }

  // ===== Listings table =====
  async function renderListings() {
    const meds = await getMedicinesByDonor(currentUser.id);
    const tbody = document.getElementById('listingsTableBody');

    if (!meds || meds.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" class="empty-note">No listings yet. <a href="donate.html">Donate your first medicine</a>.</td></tr>';
      return;
    }

    tbody.innerHTML = meds.map(m => `
      <tr>
        <td>${escapeHtml(m.name)}</td>
        <td>${escapeHtml(m.category)}</td>
        <td>${m.quantity}</td>
        <td><span class="status-badge status-${m.status.replace(/\s/g, '')}">${m.status}</span></td>
        <td>${m.expiryDate}</td>
      </tr>
    `).join('');
  }

  // ===== Expiring soon list =====
  async function renderExpiring() {
    const meds = (await getMedicinesByDonor(currentUser.id))
      .filter(m => m.status === 'AVAILABLE' && daysUntil(m.expiryDate) <= 30 && daysUntil(m.expiryDate) >= 0)
      .sort((a, b) => daysUntil(a.expiryDate) - daysUntil(b.expiryDate));

    const container = document.getElementById('expiringList');

    if (!meds || meds.length === 0) {
      container.innerHTML = '<p class="empty-note">Nothing expiring soon. Good shape.</p>';
      return;
    }

    container.innerHTML = meds.map(m => `
      <div class="expiry-list-item">
        <span class="name">${escapeHtml(m.name)}</span>
        <span class="days">${daysUntil(m.expiryDate)} days left</span>
      </div>
    `).join('');
  }

  // ===== Requests table =====
  async function renderRequests() {
    const reqs = await getRequestsByDonor(currentUser.id);
    const tbody = document.getElementById('requestsTableBody');

    if (!reqs || reqs.length === 0) {
      tbody.innerHTML = '<tr><td colspan="4" class="empty-note">No requests yet. List a medicine to get started.</td></tr>';
      return;
    }

    tbody.innerHTML = reqs.map(r => `
      <tr>
        <td>${escapeHtml(r.medicineName)}</td>
        <td>${escapeHtml(r.ngoName)}</td>
        <td><span class="status-badge status-${r.status.replace(/\s/g, '')}">${r.status}</span></td>
        <td>${r.requestDate}</td>
      </tr>
    `).join('');
  }

  (async function init() {
    await renderStats();
    await renderListings();
    await renderExpiring();
    await renderRequests();
  })();
}
