// initHeader.js

export function initHeader() {
  const toggle = document.querySelector('#mobile-menu-toggle');

  if (toggle) {
    toggle.addEventListener('click', () => {
      const event = new CustomEvent('toggle-mobile-nav');
      document.dispatchEvent(event);
    });
  }

  sessionStorage.removeItem('dataland_login_redirect_pending');
  sessionStorage.removeItem('dataland_register_redirect_pending');

  const isAuthenticated = localStorage.getItem('dataland_authenticated') === 'true';

  if (isAuthenticated) {
    const guestActions = document.querySelector('#header-guest-actions');
    const backToPlatform = document.querySelector('#header-back-to-platform');

    if (guestActions) {
      guestActions.style.display = 'none';
    }

    if (backToPlatform) {
      backToPlatform.style.display = 'inline-flex';
    }
  }
}

// Auto‑initialize when loaded in the browser
if (typeof window !== 'undefined') {
  if (document.readyState === 'loading') {
    window.addEventListener('DOMContentLoaded', () => {
      initHeader();
    });
  } else {
    initHeader();
  }
}
