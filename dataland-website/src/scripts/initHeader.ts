export function initHeader(): void {
  const toggle = document.querySelector<HTMLButtonElement>('#mobile-menu-toggle');

  if (toggle) {
    toggle.addEventListener('click', (): void => {
      const event = new CustomEvent('toggle-mobile-nav');
      document.dispatchEvent(event);
    });
  }

  sessionStorage.removeItem('dataland_login_redirect_pending');
  sessionStorage.removeItem('dataland_register_redirect_pending');

  const isAuthenticated: boolean = localStorage.getItem('dataland_authenticated') === 'true';

  if (isAuthenticated) {
    const guestActions = document.querySelector<HTMLElement>('#header-guest-actions');
    const backToPlatform = document.querySelector<HTMLElement>('#header-back-to-platform');

    if (guestActions) {
      guestActions.style.display = 'none';
    }

    if (backToPlatform) {
      backToPlatform.style.display = 'inline-flex';
    }
  }
}

if (typeof window !== 'undefined') {
  if (document.readyState === 'loading') {
    window.addEventListener('DOMContentLoaded', () => {
      initHeader();
    });
  } else {
    initHeader();
  }
}
