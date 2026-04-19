function updateEqualButtonGroup(group: HTMLElement): void {
  const buttons = Array.from(group.querySelectorAll<HTMLElement>('.btn'));
  if (buttons.length === 0) {
    return;
  }

  buttons.forEach((button) => {
    button.style.inlineSize = '';
  });

  const maxWidth = Math.ceil(
    buttons.reduce((currentMax, button) => Math.max(currentMax, button.getBoundingClientRect().width), 0),
  );

  buttons.forEach((button) => {
    button.style.inlineSize = `${maxWidth}px`;
  });
}

function initEqualButtons(): void {
  const groups = Array.from(document.querySelectorAll<HTMLElement>('[data-equal-buttons]'));
  if (groups.length === 0) {
    return;
  }

  const updateAllGroups = (): void => {
    groups.forEach(updateEqualButtonGroup);
  };

  updateAllGroups();
  globalThis.addEventListener('resize', updateAllGroups);
  globalThis.addEventListener('load', updateAllGroups);

  if ('fonts' in document) {
    document.fonts.ready.then(updateAllGroups);
  }
}

if (typeof document !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initEqualButtons, { once: true });
  } else {
    initEqualButtons();
  }
}
