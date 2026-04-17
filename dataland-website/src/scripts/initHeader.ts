// header.ts

function dispatchToggleMobileNavEvent(): void {
  const event = new CustomEvent('toggle-mobile-nav');
  document.dispatchEvent(event);
}

function clearAuthRedirectFlags(): void {
  globalThis.sessionStorage.removeItem('dataland_login_redirect_pending');
  globalThis.sessionStorage.removeItem('dataland_register_redirect_pending');
}

function updateHeaderForAuthState(): void {
  const isAuthenticated = globalThis.localStorage.getItem('dataland_authenticated') === 'true';

  const guestActions = document.querySelector<HTMLElement>('#header-guest-actions');
  const backToPlatform = document.querySelector<HTMLElement>('#header-back-to-platform');

  if (guestActions) {
    guestActions.style.display = isAuthenticated ? 'none' : 'flex';
  }

  if (backToPlatform) {
    backToPlatform.classList.toggle('hidden', !isAuthenticated);
    backToPlatform.style.removeProperty('display');
  }
}

export function initHeader(): void {
  const toggle = document.querySelector<HTMLButtonElement>('#mobile-menu-toggle');

  if (toggle) {
    toggle.addEventListener('click', dispatchToggleMobileNavEvent);
  }

  clearAuthRedirectFlags();
  updateHeaderForAuthState();
}

if (typeof document !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      initHeader();
    });
  } else {
    initHeader();
  }
}
