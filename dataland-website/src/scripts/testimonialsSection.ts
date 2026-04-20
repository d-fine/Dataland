function getSliderStep(slider: HTMLElement): number {
  const firstCard = slider.querySelector<HTMLElement>('.testimonial-card');
  if (!firstCard) {
    return slider.clientWidth;
  }

  const styles = globalThis.getComputedStyle(slider);
  const gap = Number.parseFloat(styles.columnGap || styles.gap || '0');
  const cardWidth = firstCard.getBoundingClientRect().width;

  return cardWidth + gap;
}

function updateSliderButtons(
  slider: HTMLElement,
  prevButtons: HTMLButtonElement[],
  nextButtons: HTMLButtonElement[]
): void {
  const maxScrollLeft = slider.scrollWidth - slider.clientWidth;
  const isAtStart = slider.scrollLeft <= 4;
  const isAtEnd = slider.scrollLeft >= maxScrollLeft - 4;

  prevButtons.forEach((button) => {
    button.disabled = isAtStart;
  });

  nextButtons.forEach((button) => {
    button.disabled = isAtEnd;
  });
}

function bindSliderControls(section: HTMLElement): void {
  const slider = section.querySelector<HTMLElement>('[data-testimonial-slider]');
  const prevButtons = Array.from(section.querySelectorAll<HTMLButtonElement>('[data-direction="prev"]') ?? []);
  const nextButtons = Array.from(section.querySelectorAll<HTMLButtonElement>('[data-direction="next"]') ?? []);

  if (!slider || prevButtons.length === 0 || nextButtons.length === 0) {
    return;
  }

  const scrollSlider = (direction: number) => {
    slider.scrollBy({
      left: direction * getSliderStep(slider),
      behavior: 'smooth',
    });
  };

  const handleUpdateButtons = () => {
    updateSliderButtons(slider, prevButtons, nextButtons);
  };

  prevButtons.forEach((button) => {
    button.addEventListener('click', () => scrollSlider(-1));
  });

  nextButtons.forEach((button) => {
    button.addEventListener('click', () => scrollSlider(1));
  });

  slider.addEventListener('scroll', handleUpdateButtons, { passive: true });
  globalThis.addEventListener('resize', handleUpdateButtons);
  handleUpdateButtons();
}

export function initTestimonialsSliders(): void {
  if (typeof document === 'undefined') return;

  document.querySelectorAll<HTMLElement>('[data-testimonials-section]').forEach((section) => {
    bindSliderControls(section);
  });
}

if (typeof document !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener(
      'DOMContentLoaded',
      () => {
        initTestimonialsSliders();
      },
      { once: true }
    );
  } else {
    initTestimonialsSliders();
  }
}
