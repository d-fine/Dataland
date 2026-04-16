// public/scripts/mobileNav.js

export function initMobileNav() {
  const nav = document.getElementById('mobile-nav');
  const overlay = document.getElementById('mobile-nav-overlay');
  const closeBtn = document.getElementById('mobile-nav-close');
  const toggle = document.getElementById('mobile-menu-toggle');

  if (!nav || !overlay || !closeBtn || !toggle) return;

  const isAuthenticated = localStorage.getItem('dataland_authenticated') === 'true';

  const backToPlatform = document.getElementById('mobile-nav-back-to-platform');
  const loginLink = nav.querySelector('[data-test="login-dataland-button"]');
  const signupLink = nav.querySelector('[data-test="signup-dataland-button"]');

  if (backToPlatform instanceof HTMLElement) {
    backToPlatform.style.display = isAuthenticated ? 'flex' : 'none';
  }
  if (loginLink instanceof HTMLElement) {
    loginLink.style.display = isAuthenticated ? 'none' : 'block';
  }
  if (signupLink instanceof HTMLElement) {
    signupLink.style.display = isAuthenticated ? 'none' : 'inline-flex';
  }

  function markActiveLinks() {
    const path = window.location.pathname;
    const links = nav.querySelectorAll('.mobile-nav__link');

    const navHrefs = ['/', '/product', '/about', '/community'];

    links.forEach((link) => {
      if (!(link instanceof HTMLAnchorElement)) return;
      const href = link.getAttribute('href');
      if (!href || !navHrefs.includes(href)) return;

      const isRoot = href === '/';
      const active = isRoot ? path === '/' : path.startsWith(href);

      link.classList.toggle('mobile-nav__link--active', active);
    });
  }

  markActiveLinks();

  let isOpen = false;

  function openNav() {
    if (isOpen) return;
    isOpen = true;
    nav.classList.remove('hidden');
    overlay.classList.remove('hidden');
    closeBtn.focus();
  }

  function closeNav() {
    if (!isOpen) return;
    isOpen = false;
    nav.classList.add('hidden');
    overlay.classList.add('hidden');
    toggle.focus();
  }

  function handleToggle() {
    if (isOpen) {
      closeNav();
    } else {
      openNav();
    }
  }

  function handleKeydown(event) {
    if (event.key === 'Escape' && isOpen) {
      event.preventDefault();
      closeNav();
    }
  }

  document.addEventListener('toggle-mobile-nav', handleToggle);
  document.addEventListener('keydown', handleKeydown);
  overlay.addEventListener('click', closeNav);
  closeBtn.addEventListener('click', closeNav);

  window.addEventListener('resize', () => {
    if (window.innerWidth >= 1024 && isOpen) {
      closeNav();
    }
  });
}

function setupMobileNavWithMedia() {
  const mql = window.matchMedia('(max-width: 1023px)');
  let initialized = false;

  function initIfMatches(e) {
    const matches = e ? e.matches : mql.matches;
    if (!matches || initialized) return;
    initMobileNav();
    initialized = true;
  }

  initIfMatches();

  if (!initialized) {
    const onChange = (e) => {
      initIfMatches(e);
      if (initialized) {
        mql.removeEventListener('change', onChange);
      }
    };
    mql.addEventListener('change', onChange);
  }
}

if (typeof window !== 'undefined') {
  if (document.readyState === 'loading') {
    window.addEventListener('DOMContentLoaded', setupMobileNavWithMedia);
  } else {
    setupMobileNavWithMedia();
  }
}
