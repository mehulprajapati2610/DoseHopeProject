// ===== Tab switching =====
const tabs = document.querySelectorAll('.auth-tab');
const forms = document.querySelectorAll('.auth-form');

tabs.forEach(tab => {
  tab.addEventListener('click', () => {
    tabs.forEach(t => t.classList.remove('active'));
    forms.forEach(f => f.classList.remove('active'));
    tab.classList.add('active');
    document.getElementById(tab.dataset.tab + 'Form').classList.add('active');
  });
});

// ===== Redirect destination after login =====
function getRedirectForRole(role) {
  return getDashboardForRole(role);
}

// ===== Login form =====
const loginForm = document.getElementById('loginForm');
const loginError = document.getElementById('loginError');

loginForm.addEventListener('submit', async (e) => {  e.preventDefault();
  const email = document.getElementById('loginEmail').value.trim();
  const password = document.getElementById('loginPassword').value;

  const result = await loginUser(email, password);
  if (!result.ok) {
    loginError.textContent = result.error;
    loginError.hidden = false;
    return;
  }
  loginError.hidden = true;
  window.location.href = getRedirectForRole(result.user.role);
});

// ===== Demo account quick-fill =====
document.querySelectorAll('.demo-chip').forEach(chip => {
  chip.addEventListener('click', () => {
    document.getElementById('loginEmail').value = chip.dataset.email;
    document.getElementById('loginPassword').value = 'demo123';
  });
});

// ===== Register form =====
const registerForm = document.getElementById('registerForm');
const registerError = document.getElementById('registerError');

registerForm.addEventListener('submit', async (e) => {  e.preventDefault();
  const name = document.getElementById('regName').value.trim();
  const email = document.getElementById('regEmail').value.trim();
  const password = document.getElementById('regPassword').value;
  const role = document.getElementById('regRole').value;
  const phone = document.getElementById('regPhone').value.trim();

  const result = await registerUser({ name, email, password, role, phone, address: '' });
  if (!result.ok) {
    registerError.textContent = result.error;
    registerError.hidden = false;
    return;
  }
  registerError.hidden = true;
  window.location.href = getRedirectForRole(result.user.role);
});

// ===== If already logged in, skip straight to dashboard =====
(function redirectIfLoggedIn() {
  const user = getCurrentUser();
  if (user) {
    window.location.href = getRedirectForRole(user.role);
  }
})();
