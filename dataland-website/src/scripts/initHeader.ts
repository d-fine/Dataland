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
    backToPlatform.style.display = isAuthenticated ? 'inline-flex' : 'none';
  }
}

export function initHeader(): void {
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
