// src/scripts/mobileNav.ts

export function initMobileNav(): void {
  let nav = document.querySelector<HTMLElement>('#mobile-nav');
  let overlay = document.querySelector<HTMLElement>('#mobile-nav-overlay');

  if (!nav || !overlay) {
    const template = document.querySelector<HTMLTemplateElement>('#mobile-nav-template');
    if (!template) return;

    const fragment = template.content.cloneNode(true) as DocumentFragment;

    document.body.appendChild(fragment);

    nav = document.querySelector<HTMLElement>('#mobile-nav');
    overlay = document.querySelector<HTMLElement>('#mobile-nav-overlay');
  }

  const closeBtn = document.querySelector<HTMLButtonElement>('#mobile-nav-close');
  const toggle = document.querySelector<HTMLButtonElement>('#mobile-menu-toggle');

  if (!nav || !overlay || !closeBtn || !toggle) return;

  initMobileNavInternal(nav, overlay, closeBtn, toggle);
}

function initMobileNavInternal(
  nav: HTMLElement,
  overlay: HTMLElement,
  closeBtn: HTMLButtonElement,
  toggle: HTMLButtonElement
): void {
  const isAuthenticated: boolean = localStorage.getItem('dataland_authenticated') === 'true';

  const backToPlatform = document.querySelector<HTMLElement>('#mobile-nav-back-to-platform');
  const loginLink = nav.querySelector<HTMLElement>('[data-test="login-dataland-button"]');
  const signupLink = nav.querySelector<HTMLElement>('[data-test="signup-dataland-button"]');

  if (backToPlatform) {
    backToPlatform.style.display = isAuthenticated ? 'flex' : 'none';
  }
  if (loginLink) {
    loginLink.style.display = isAuthenticated ? 'none' : 'block';
  }
  if (signupLink) {
    signupLink.style.display = isAuthenticated ? 'none' : 'inline-flex';
  }

  function markActiveLinks(): void {
    const path: string = window.location.pathname;
    const links = nav.querySelectorAll<HTMLAnchorElement>('.mobile-nav__link');

    const navHrefs: readonly string[] = ['/', '/product', '/about', '/community'];

    links.forEach((link: HTMLAnchorElement): void => {
      const href: string | null = link.getAttribute('href');
      if (!href || !navHrefs.includes(href)) return;

      const isRoot: boolean = href === '/';
      const active: boolean = isRoot ? path === '/' : path.startsWith(href);

      link.classList.toggle('mobile-nav__link--active', active);
    });
  }

  markActiveLinks();

  let isOpen = false;

  function openNav(): void {
    if (isOpen) return;
    isOpen = true;
    nav.classList.remove('hidden');
    overlay.classList.remove('hidden');
    closeBtn.focus();
  }

  function closeNav(): void {
    if (!isOpen) return;
    isOpen = false;
    nav.classList.add('hidden');
    overlay.classList.add('hidden');
    toggle.focus();
  }

  function handleToggle(): void {
    if (isOpen) {
      closeNav();
    } else {
      openNav();
    }
  }

  function handleKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape' && isOpen) {
      event.preventDefault();
      closeNav();
    }
  }

  document.addEventListener('toggle-mobile-nav', handleToggle);
  document.addEventListener('keydown', handleKeydown);
  overlay.addEventListener('click', closeNav);
  closeBtn.addEventListener('click', closeNav);

  window.addEventListener('resize', (): void => {
    if (window.innerWidth >= 1024 && isOpen) {
      closeNav();
    }
  });
}

function setupMobileNavWithMedia(): void {
  const mql: MediaQueryList = window.matchMedia('(max-width: 1023px)');
  let initialized = false;

  function initIfMatches(e?: MediaQueryListEvent): void {
    const matches: boolean = e ? e.matches : mql.matches;
    if (!matches || initialized) return;

    initMobileNav();
    initialized = true;
  }

  initIfMatches();

  if (!initialized) {
    const onChange = (e: MediaQueryListEvent): void => {
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
    window.addEventListener('DOMContentLoaded', (): void => {
      setupMobileNavWithMedia();
    });
  } else {
    setupMobileNavWithMedia();
  }
}
