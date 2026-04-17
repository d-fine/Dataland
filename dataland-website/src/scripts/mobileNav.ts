// mobileNav.ts
const NAV_HREFS: ReadonlySet<string> = new Set(['/', '/product', '/about', '/community']);

function markActiveLinks(nav: HTMLElement): void {
  const path: string = globalThis.location.pathname;
  const links = nav.querySelectorAll<HTMLAnchorElement>('.mobile-nav__link');

  links.forEach((link: HTMLAnchorElement): void => {
    const href: string | null = link.getAttribute('href');
    if (!href || !NAV_HREFS.has(href)) return;

    const isRoot: boolean = href === '/';
    const active: boolean = isRoot ? path === '/' : path.startsWith(href);

    link.classList.toggle('mobile-nav__link--active', active);
  });
}

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

  if (nav.dataset.initialized === 'true') return;
  nav.dataset.initialized = 'true';

  initMobileNavInternal(nav, overlay, closeBtn, toggle);
}

function initMobileNavInternal(
  nav: HTMLElement,
  overlay: HTMLElement,
  closeBtn: HTMLButtonElement,
  toggle: HTMLButtonElement
): void {
  const isAuthenticated: boolean = globalThis.localStorage.getItem('dataland_authenticated') === 'true';

  const loginLink = nav.querySelector<HTMLElement>('[data-test="login-dataland-button"]');
  const signupLink = nav.querySelector<HTMLElement>('[data-test="signup-dataland-button"]');

  if (loginLink) {
    loginLink.style.display = isAuthenticated ? 'none' : 'block';
  }
  if (signupLink) {
    signupLink.style.display = isAuthenticated ? 'none' : 'inline-flex';
  }

  markActiveLinks(nav);

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

  toggle.addEventListener('click', handleToggle);
  document.addEventListener('keydown', handleKeydown);
  overlay.addEventListener('click', closeNav);
  closeBtn.addEventListener('click', closeNav);

  globalThis.addEventListener('resize', (): void => {
    if (globalThis.innerWidth >= 1024 && isOpen) {
      closeNav();
    }
  });
}

if (typeof document !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', (): void => {
      initMobileNav();
    });
  } else {
    initMobileNav();
  }
}
