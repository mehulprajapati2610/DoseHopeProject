// ===== Guard: must be logged in =====
const currentUser = requireLogin('login.html', 'NGO');
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

  let activeAssignRequestId = null;

  async function renderStats() {
    const stats = await getNgoStats(currentUser.id);
    document.getElementById('statsRow').innerHTML = `
      <div class="stat-card">
        <strong>${stats.totalRequests}</strong>
        <span>Total requests</span>
      </div>
      <div class="stat-card">
        <strong>${stats.pending}</strong>
        <span>Pending review</span>
      </div>
      <div class="stat-card">
        <strong>${stats.inProgress}</strong>
        <span>In progress</span>
      </div>
      <div class="stat-card">
        <strong>${stats.completed}</strong>
        <span>Completed</span>
      </div>
    `;
  }

  function actionButtonsFor(req) {
      if (req.status === 'PENDING') {
        return `
          <button class="btn btn-primary" data-accept="${req.id}">Accept</button>
          <button class="btn btn-outline" data-reject="${req.id}">Reject</button>
        `;
      }
      if (req.status === 'ACCEPTED') {
        return `<button class="btn btn-primary" data-assign="${req.id}">Assign Volunteer</button>`;
      }
      return '<span class="empty-note">—</span>';
    }

  async function renderRequests() {
    const reqs = await getRequestsByNgo(currentUser.id);
    const tbody = document.getElementById('requestsTableBody');

    if (!reqs || reqs.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" class="empty-note">No requests yet. Browse available medicines to request one.</td></tr>';
      return;
    }

    const volunteers = await getAllVolunteers();

    tbody.innerHTML = reqs.map(r => {
      const vol = volunteers.find(v => v.id === r.volunteerId);
      return `
        <tr>
          <td>${escapeHtml(r.medicineName)}</td>
          <td><span class="status-badge status-${r.status.replace(/\s/g, '')}">${r.status}</span></td>
          <td>${r.requestDate}</td>
          <td>${vol ? escapeHtml(vol.name) : '<span class="empty-note">Unassigned</span>'}</td>
          <td>${actionButtonsFor(r)}</td>
        </tr>
      `;
    }).join('');
  }

  async function refresh() {
    await renderStats();
    await renderRequests();
  }

  refresh();

  // ===== Accept / Reject =====
document.getElementById('requestsTableBody').addEventListener('click', async (e) => {    const acceptBtn = e.target.closest('[data-accept]');
    const rejectBtn = e.target.closest('[data-reject]');
    const assignBtn = e.target.closest('[data-assign]');

    if (acceptBtn) {
          await acceptRequest(acceptBtn.dataset.accept);
          refresh();
        }
        if (rejectBtn) {
          await rejectRequest(rejectBtn.dataset.reject);
          refresh();
        }
    if (assignBtn) {
          activeAssignRequestId = assignBtn.dataset.assign;
          const volunteers = await getAllVolunteers();
          const select = document.getElementById('volunteerSelect');
          select.innerHTML = volunteers.map(v => `<option value="${v.id}">${escapeHtml(v.name)}</option>`).join('');
          document.getElementById('assignModal').hidden = false;
        }
  });

  // ===== Assign modal =====
  const assignModal = document.getElementById('assignModal');
  document.getElementById('assignCancel').addEventListener('click', () => { assignModal.hidden = true; });
  assignModal.addEventListener('click', (e) => { if (e.target === assignModal) assignModal.hidden = true; });
document.getElementById('assignConfirm').addEventListener('click', async () => {
    if (!activeAssignRequestId) return;
    const volunteerId = document.getElementById('volunteerSelect').value;
    await assignVolunteer(activeAssignRequestId, volunteerId);
    assignModal.hidden = true;
    refresh();
  });

}
