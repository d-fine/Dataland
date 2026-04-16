export function initIndexPage(): void {
  const slider = document.querySelector<HTMLElement>('[data-testimonial-slider]');
  const prevButton = document.querySelector<HTMLButtonElement>('[data-direction="prev"]');
  const nextButton = document.querySelector<HTMLButtonElement>('[data-direction="next"]');

  if (slider && prevButton && nextButton) {
    const getStep = (): number => {
      const firstCard = slider.querySelector<HTMLElement>('.testimonial-card');
      if (!firstCard) {
        return slider.clientWidth;
      }

      const styles: CSSStyleDeclaration = window.getComputedStyle(slider);
      const gap: number = Number.parseFloat(styles.columnGap || styles.gap || '0');

      const cardWidth: number = firstCard.getBoundingClientRect().width;
      return cardWidth + gap;
    };

    const updateButtons = (): void => {
      const maxScrollLeft: number = slider.scrollWidth - slider.clientWidth;
      prevButton.disabled = slider.scrollLeft <= 4;
      nextButton.disabled = slider.scrollLeft >= maxScrollLeft - 4;
    };

    const scrollSlider = (direction: number): void => {
      slider.scrollBy({
        left: direction * getStep(),
        behavior: 'smooth',
      });
    };

    prevButton.addEventListener('click', (): void => scrollSlider(-1));
    nextButton.addEventListener('click', (): void => scrollSlider(1));
    slider.addEventListener('scroll', updateButtons, { passive: true });
    window.addEventListener('resize', updateButtons);

    updateButtons();
  }

  const params: URLSearchParams = new URLSearchParams(window.location.search);
  if (params.get('externalLogout') === 'true') {
    const modal: HTMLElement | null = document.getElementById('logout-modal');
    const closeBtn: HTMLButtonElement | null = document.getElementById(
      'logout-modal-close'
    ) as HTMLButtonElement | null;

    if (modal) {
      modal.classList.remove('hidden');
    }

    if (closeBtn && modal) {
      closeBtn.addEventListener('click', (): void => {
        modal.classList.add('hidden');

        const url: URL = new URL(window.location.href);
        url.searchParams.delete('externalLogout');
        window.history.replaceState({}, '', url.toString());
      });
    }
  }
}

if (typeof window !== 'undefined') {
  if (document.readyState === 'loading') {
    window.addEventListener('DOMContentLoaded', () => {
      initIndexPage();
    });
  } else {
    initIndexPage();
  }
}
