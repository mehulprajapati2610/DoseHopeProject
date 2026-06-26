// ===== Mobile nav toggle =====
const navToggle = document.getElementById('navToggle');
const navLinks = document.getElementById('navLinks');

if (navToggle && navLinks) {
  navToggle.addEventListener('click', () => {
    navLinks.classList.toggle('open');
    navToggle.classList.toggle('active');
  });
}

// ===== Search form (placeholder behaviour for now — no backend yet) =====
const searchForm = document.querySelector('.search-form');
if (searchForm) {
  searchForm.addEventListener('submit', (e) => {
    e.preventDefault();
    const category = document.getElementById('category').value;
    const location = document.getElementById('location').value;
    const availability = document.getElementById('availability').value;
    console.log('Search requested:', { category, location, availability });
    alert('Search is a placeholder for now — this will query the medicines API once the backend is built.');
  });
}

// ===== Scroll-based navbar shadow =====
const navbar = document.getElementById('navbar');
window.addEventListener('scroll', () => {
  if (window.scrollY > 8) {
    navbar.style.boxShadow = '0 4px 20px rgba(15, 76, 58, 0.08)';
  } else {
    navbar.style.boxShadow = 'none';
  }
});
