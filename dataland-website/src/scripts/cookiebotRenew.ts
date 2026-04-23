document.querySelectorAll<HTMLButtonElement>('[data-cookiebot-renew]').forEach((btn) => {
  btn.addEventListener('click', () => {
    (window as unknown as { Cookiebot?: { renew: () => void } }).Cookiebot?.renew();
  });
});

