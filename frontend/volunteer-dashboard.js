// ===== Guard: must be logged in =====
const currentUser = requireLogin('login.html', 'VOLUNTEER');
if (currentUser) {

  document.getElementById('userPill').textContent = currentUser.name + ' · ' + currentUser.role;
  document.getElementById('greetingName').textContent = 'Welcome, ' + currentUser.name;

  document.getElementById('logoutBtn').addEventListener('click', () => {
    logoutUser();
    window.location.href = 'index.html';
  });

  function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
  }

  async function renderStats() {
    const stats = await getVolunteerStats(currentUser.id);
    document.getElementById('statsRow').innerHTML = `
      <div class="stat-card">
        <strong>${stats.pendingPickup}</strong>
        <span>Pending pickup</span>
      </div>
      <div class="stat-card">
        <strong>${stats.pendingDelivery}</strong>
        <span>Pending delivery</span>
      </div>
      <div class="stat-card">
        <strong>${stats.completed}</strong>
        <span>Completed deliveries</span>
      </div>
      <div class="stat-card">
        <strong>${stats.pendingPickup + stats.pendingDelivery + stats.completed}</strong>
        <span>Total assignments</span>
      </div>
    `;
  }

  function actionButtonFor(req) {
    if (req.status === 'VOLUNTEER_ASSIGNED') {
      return `<button class="btn btn-primary" data-pickup="${req.id}">Mark Picked Up</button>`;
    }
    if (req.status === 'PICKED_UP') {
      return `<button class="btn btn-primary" data-deliver="${req.id}">Mark Delivered</button>`;
    }
    return '<span class="empty-note">—</span>';
  }

  async function renderAssignments() {
    const reqs = await getRequestsByVolunteer(currentUser.id);
    const tbody = document.getElementById('assignmentsTableBody');

    if (!reqs || reqs.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" class="empty-note">No assignments yet. NGOs will assign you to pickups as they come in.</td></tr>';
      return;
    }

    tbody.innerHTML = reqs.map(r => `
      <tr>
        <td>${escapeHtml(r.medicineName)}</td>
        <td>${escapeHtml(r.ngoName)}</td>
        <td><span class="status-badge status-${r.status.replace(/\s/g, '')}">${r.status}</span></td>
        <td>${r.pickupDate || '<span class="empty-note">Not yet</span>'}</td>
        <td>${actionButtonFor(r)}</td>
      </tr>
    `).join('');
  }

  async function refresh() {
    await renderStats();
    await renderAssignments();
  }

  refresh();

  document.getElementById('assignmentsTableBody').addEventListener('click', async (e) => {
      const pickupBtn = e.target.closest('[data-pickup]');
      const deliverBtn = e.target.closest('[data-deliver]');

      if (pickupBtn) {
        await markPickedUp(pickupBtn.dataset.pickup);
        refresh();
      }
      if (deliverBtn) {
        await markDelivered(deliverBtn.dataset.deliver);
        refresh();
      }
    });

}
