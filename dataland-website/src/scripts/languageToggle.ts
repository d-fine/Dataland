// LanguageToggle.ts

export function initLanguageToggle(): void {
  const checkbox = document.querySelector<HTMLInputElement>('#languageToggle');
  const english = document.querySelector<HTMLElement>('#terms-en');
  const german = document.querySelector<HTMLElement>('#terms-de');

  if (!checkbox || !english || !german) {
    return;
  }

  english.classList.remove('hidden');
  german.classList.add('hidden');

  checkbox.addEventListener('change', () => {
    const isGerman = checkbox.checked;

    if (isGerman) {
      english.classList.add('hidden');
      german.classList.remove('hidden');
    } else {
      english.classList.remove('hidden');
      german.classList.add('hidden');
    }
  });
}

if (typeof document !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      initLanguageToggle();
    });
  } else {
    initLanguageToggle();
  }
}
