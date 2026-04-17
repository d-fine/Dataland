// indexPage.ts

function getStep(slider: HTMLElement): number {
  const firstCard = slider.querySelector<HTMLElement>('.testimonial-card');
  if (!firstCard) {
    return slider.clientWidth;
  }

  const styles: CSSStyleDeclaration = globalThis.getComputedStyle(slider);
  const gap: number = Number.parseFloat(styles.columnGap || styles.gap || '0');

  const cardWidth: number = firstCard.getBoundingClientRect().width;
  return cardWidth + gap;
}

function updateSliderButtons(slider: HTMLElement, prevButton: HTMLButtonElement, nextButton: HTMLButtonElement): void {
  const maxScrollLeft: number = slider.scrollWidth - slider.clientWidth;
  prevButton.disabled = slider.scrollLeft <= 4;
  nextButton.disabled = slider.scrollLeft >= maxScrollLeft - 4;
}

function scrollSlider(slider: HTMLElement, direction: number): void {
  slider.scrollBy({
    left: direction * getStep(slider),
    behavior: 'smooth',
  });
}

function setupSlider(): void {
  const slider = document.querySelector<HTMLElement>('[data-testimonial-slider]');
  const prevButton = document.querySelector<HTMLButtonElement>('[data-direction="prev"]');
  const nextButton = document.querySelector<HTMLButtonElement>('[data-direction="next"]');

  if (!slider || !prevButton || !nextButton) {
    return;
  }

  const handleUpdateButtons = (): void => {
    updateSliderButtons(slider, prevButton, nextButton);
  };

  prevButton.addEventListener('click', (): void => scrollSlider(slider, -1));
  nextButton.addEventListener('click', (): void => scrollSlider(slider, 1));
  slider.addEventListener('scroll', handleUpdateButtons, { passive: true });
  globalThis.addEventListener('resize', handleUpdateButtons);

  handleUpdateButtons();
}

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
  setupSlider();
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
