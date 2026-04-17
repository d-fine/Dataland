function setupExternalLogoutModal(): void {
  const params: URLSearchParams = new URLSearchParams(globalThis.location.search);
  if (params.get('externalLogout') !== 'true') {
    return;
  }

  const modal: HTMLElement | null = document.getElementById('logout-modal');
  const closeBtn: HTMLButtonElement | null = document.getElementById('logout-modal-close') as HTMLButtonElement | null;

  if (modal) {
    modal.classList.remove('hidden');
  }

  if (closeBtn && modal) {
    closeBtn.addEventListener('click', (): void => {
      modal.classList.add('hidden');

      const url: URL = new URL(globalThis.location.href);
      url.searchParams.delete('externalLogout');
      globalThis.history.replaceState({}, '', url.toString());
    });
  }
}

export function initIndexPage(): void {
  setupExternalLogoutModal();
}

if (typeof document !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      initIndexPage();
    });
  } else {
    initIndexPage();
  }
}
