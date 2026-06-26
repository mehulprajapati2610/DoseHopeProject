// ===== Browse page is public — no login wall, but adapt nav if logged in =====
const currentUser = getCurrentUser();

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str == null ? '' : str;
  return div.innerHTML;
}

const userPill = document.getElementById('userPill');
const logoutBtn = document.getElementById('logoutBtn');
const donateLink = document.getElementById('donateLink');

if (currentUser) {
  userPill.textContent = currentUser.name + ' · ' + currentUser.role;
  logoutBtn.addEventListener('click', () => {
    logoutUser();
    window.location.href = 'index.html';
  });

  // Only donors list medicines — hide the Donate link for other roles.
  if (donateLink && currentUser.role !== 'DONOR') {
    donateLink.style.display = 'none';
  }
} else {
  // Not logged in — relabel the action button to send people to login instead.
  userPill.style.display = 'none';
  logoutBtn.textContent = 'Log in';
  logoutBtn.addEventListener('click', () => {
    window.location.href = 'login.html';
  });
}

/* ---------- Expiry helper (mirrors dashboard.js logic) ---------- */
function isExpiringSoon(expiryDate) {
  const d = daysUntil(expiryDate);
  return d >= 0 && d <= 30;
}

/* ---------- State ---------- */
let allMedicines = [];
let activeRequestMedicineId = null;

const grid = document.getElementById('medicineGrid');
const filterCategory = document.getElementById('filterCategory');
const filterSearch = document.getElementById('filterSearch');
const filterSort = document.getElementById('filterSort');

/* ---------- Render ---------- */
function renderGrid() {
  const category = filterCategory.value;
  const search = filterSearch.value.trim().toLowerCase();
  const sort = filterSort.value;

  let meds = allMedicines.filter(m => {
    const matchesCategory = !category || m.category === category;
    const matchesSearch = !search || m.name.toLowerCase().includes(search);
    return matchesCategory && matchesSearch;
  });

  if (sort === 'expiry') {
    meds = meds.slice().sort((a, b) => daysUntil(a.expiryDate) - daysUntil(b.expiryDate));
  } else {
    meds = meds.slice().sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  }

  if (meds.length === 0) {
    grid.innerHTML = '<p class="no-results">No medicines match your search right now. Try a different category or check back soon.</p>';
    return;
  }

  grid.innerHTML = meds.map(m => {
    const soon = isExpiringSoon(m.expiryDate);
    const canRequest = currentUser && currentUser.role === 'NGO';

    return `
      <div class="medicine-card">
        <div class="medicine-card-top">
          <h3>${escapeHtml(m.name)}</h3>
          <span class="expiry-tag ${soon ? 'soon' : 'safe'}">${soon ? daysUntil(m.expiryDate) + 'd left' : 'Available'}</span>
        </div>
        <div class="medicine-meta">
          <span><strong>Manufacturer:</strong> ${escapeHtml(m.manufacturer)}</span>
          <span><strong>Category:</strong> ${escapeHtml(m.category)}</span>
          <span><strong>Quantity:</strong> ${m.quantity}</span>
          <span><strong>Expiry:</strong> ${m.expiryDate}</span>
          <span><strong>Donor:</strong> ${escapeHtml(m.donorName || 'DoseHope donor')}</span>
        </div>
        <div class="medicine-card-footer">
          ${canRequest
            ? `<button class="btn btn-primary" data-request="${m.id}" data-name="${escapeHtml(m.name)}">Request</button>`
            : `<span class="medicine-meta">${currentUser ? 'Only NGOs can request medicines' : 'Log in as an NGO to request'}</span>`}
        </div>
      </div>
    `;
  }).join('');
}

async function loadMedicines() {
  grid.innerHTML = '<p class="no-results">Loading medicines…</p>';
  allMedicines = await getMedicines();
  renderGrid();
}

/* ---------- Filters ---------- */
filterCategory.addEventListener('change', renderGrid);
filterSort.addEventListener('change', renderGrid);
filterSearch.addEventListener('input', renderGrid);

/* ---------- Request modal ---------- */
const requestModal = document.getElementById('requestModal');
const modalMedName = document.getElementById('modalMedName');
const modalCancel = document.getElementById('modalCancel');
const modalConfirm = document.getElementById('modalConfirm');

grid.addEventListener('click', (e) => {
  const btn = e.target.closest('[data-request]');
  if (!btn) return;
  activeRequestMedicineId = btn.dataset.request;
  modalMedName.textContent = `Request "${btn.dataset.name}"?`;
  requestModal.hidden = false;
});

modalCancel.addEventListener('click', () => {
  requestModal.hidden = true;
  activeRequestMedicineId = null;
});

requestModal.addEventListener('click', (e) => {
  if (e.target === requestModal) {
    requestModal.hidden = true;
    activeRequestMedicineId = null;
  }
});

modalConfirm.addEventListener('click', async () => {
  if (!activeRequestMedicineId) return;
  const result = await createRequest({ medicineId: activeRequestMedicineId });
  requestModal.hidden = true;
  activeRequestMedicineId = null;

  if (!result.ok) {
    alert(result.error || 'Could not send the request. Please try again.');
    return;
  }
  await loadMedicines();
});

/* ---------- Init ---------- */
loadMedicines();

