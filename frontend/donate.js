// ===== Guard: must be logged in =====
const currentUser = requireLogin('login.html');

// If not logged in, requireLogin() already redirected — stop here so
// nothing below tries to read properties off a null user.
if (currentUser) {

  document.getElementById('userPill').textContent = currentUser.name + ' · ' + currentUser.role;
  document.getElementById('logoutBtn').addEventListener('click', () => {
    logoutUser();
    window.location.href = 'index.html';
  });

  // ===== Expiry live preview =====
  const expDateInput = document.getElementById('medExpDate');
  const expiryPreview = document.getElementById('expiryPreview');

  expDateInput.addEventListener('change', () => {
    const val = expDateInput.value;
    if (!val) { expiryPreview.hidden = true; return; }

    const days = daysUntil(val);
    expiryPreview.hidden = false;
    expiryPreview.className = 'expiry-preview';

    if (days < 0) {
      expiryPreview.classList.add('bad');
      expiryPreview.textContent = 'This medicine expired ' + Math.abs(days) + ' day(s) ago. It cannot be listed.';
    } else if (days <= 30) {
      expiryPreview.classList.add('warn');
      expiryPreview.textContent = 'Expires in ' + days + ' day(s) — still listable, but mark it as urgent in the description.';
    } else {
      expiryPreview.classList.add('ok');
      expiryPreview.textContent = 'Expires in ' + days + ' day(s). Good to list.';
    }
  });

  function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
  }

  // ===== Render recent listings =====
  async function renderRecentListings() {
    const container = document.getElementById('recentListings');
    const mine = await getMedicinesByDonor(currentUser.id);

    if (!mine || mine.length === 0) {
      container.innerHTML = '<p class="empty-note">You haven\'t listed anything yet — your first listing will appear here.</p>';
      return;
    }

    container.innerHTML = mine.slice(0,5).map(m => `
      <div class="recent-item">
        <span class="recent-item-name">${escapeHtml(m.name)}</span>
        <span class="status-badge status-${m.status.replace(/\s/g, '')}">${m.status}</span>
      </div>
    `).join('');
  }

  renderRecentListings();

  // ===== Form submit =====
  const donateForm = document.getElementById('donateForm');
  const formError = document.getElementById('formError');
  const formSuccess = document.getElementById('formSuccess');

donateForm.addEventListener('submit', async (e) => {    e.preventDefault();
    formError.hidden = true;
    formSuccess.hidden = true;

    const name = document.getElementById('medName').value.trim();
    const manufacturer = document.getElementById('medManufacturer').value.trim();
    const batchNumber = document.getElementById('medBatch').value.trim();
    const category = document.getElementById('medCategory').value;
    const manufacturingDate = document.getElementById('medMfgDate').value;
    const expiryDate = document.getElementById('medExpDate').value;
    const quantity = parseInt(document.getElementById('medQuantity').value, 10);
    const description = document.getElementById('medDescription').value.trim();

    if (!name || !manufacturer || !batchNumber || !category || !manufacturingDate || !expiryDate || !quantity) {
      formError.textContent = 'Please fill in all required fields marked with *.';
      formError.hidden = false;
      return;
    }

    if (new Date(manufacturingDate) >= new Date(expiryDate)) {
      formError.textContent = 'Expiry date must be after the manufacturing date.';
      formError.hidden = false;
      return;
    }

    const result = await addMedicine({
      donorId: currentUser.id,
      donorName: currentUser.name,
      name, manufacturer, batchNumber, category,
      manufacturingDate, expiryDate, quantity, description
    });

    if (!result.ok) {
      formError.textContent = result.error;
      formError.hidden = false;
      return;
    }

    formSuccess.textContent = '"' + name + '" has been listed and is now visible to verified NGOs.';
    formSuccess.hidden = false;
    donateForm.reset();
    expiryPreview.hidden = true;
    await renderRecentListings();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  });

}
