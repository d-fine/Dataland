// indexPage.js

export function initIndexPage() {
  const slider = document.querySelector('[data-testimonial-slider]');
  const prevButton = document.querySelector('[data-direction="prev"]');
  const nextButton = document.querySelector('[data-direction="next"]');

  if (slider && prevButton && nextButton) {
    const getStep = () => {
      const firstCard = slider.querySelector('.testimonial-card');
      if (!firstCard) {
        return slider.clientWidth;
      }

      const styles = window.getComputedStyle(slider);
      const gap = Number.parseFloat(styles.columnGap || styles.gap || '0');

      const cardWidth = firstCard.getBoundingClientRect().width;
      return cardWidth + gap;
    };

    const updateButtons = () => {
      const maxScrollLeft = slider.scrollWidth - slider.clientWidth;
      prevButton.disabled = slider.scrollLeft <= 4;
      nextButton.disabled = slider.scrollLeft >= maxScrollLeft - 4;
    };

    const scrollSlider = (direction) => {
      slider.scrollBy({
        left: direction * getStep(),
        behavior: 'smooth',
      });
    };

    prevButton.addEventListener('click', () => scrollSlider(-1));
    nextButton.addEventListener('click', () => scrollSlider(1));
    slider.addEventListener('scroll', updateButtons, { passive: true });
    window.addEventListener('resize', updateButtons);

    updateButtons();
  }

  const params = new URLSearchParams(window.location.search);
  if (params.get('externalLogout') === 'true') {
    const modal = document.getElementById('logout-modal');
    const closeBtn = document.getElementById('logout-modal-close');

    if (modal) {
      modal.classList.remove('hidden');
    }

    if (closeBtn && modal) {
      closeBtn.addEventListener('click', () => {
        modal.classList.add('hidden');

        const url = new URL(window.location.href);
        url.searchParams.delete('externalLogout');
        window.history.replaceState({}, '', url.toString());
      });
    }
  }
}

// Auto‑initialize when loaded in the browser
if (typeof window !== 'undefined') {
  if (document.readyState === 'loading') {
    window.addEventListener('DOMContentLoaded', () => {
      initIndexPage();
    });
  } else {
    initIndexPage();
  }
}
