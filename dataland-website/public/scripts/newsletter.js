// newsletter.js

export function initNewsletterForm() {
  const form = document.querySelector('#newsletter-form');
  const submitButton = document.querySelector('#submit-button');
  const messageEl = document.querySelector('#form-message');

  const firstNameInput = document.querySelector('#firstName');
  const lastNameInput = document.querySelector('#lastName');
  const companyInput = document.querySelector('#company');
  const emailInput = document.querySelector('#email');

  const requiredInputs = [firstNameInput, lastNameInput, companyInput, emailInput].filter((input) => input !== null);

  function setMessage(text, type = '') {
    if (!messageEl) return;
    messageEl.textContent = text;
    messageEl.classList.remove('is-error', 'is-success');
    if (type) {
      messageEl.classList.add(type);
    }
  }

  function clearAllErrors() {
    requiredInputs.forEach((input) => input.classList.remove('is-error'));
  }

  function isEmailValid(value) {
    return value.includes('@');
  }

  function areAllFieldsFilled() {
    return requiredInputs.every((input) => input.value.trim() !== '');
  }

  function isFormReady() {
    const emailValue = emailInput?.value.trim() ?? '';
    return areAllFieldsFilled() && isEmailValid(emailValue);
  }

  function updateSubmitState() {
    if (!submitButton) return;
    const enabled = isFormReady();
    submitButton.disabled = !enabled;
    submitButton.classList.toggle('is-enabled', enabled);
  }

  requiredInputs.forEach((input) => {
    input.addEventListener('input', updateSubmitState);

    input.addEventListener('focus', () => {
      input.classList.remove('is-error');
      if (messageEl?.classList.contains('is-error')) {
        setMessage('');
      }
    });
  });

  form?.addEventListener('submit', (event) => {
    event.preventDefault();

    clearAllErrors();
    setMessage('');

    const emptyInputs = requiredInputs.filter((input) => input.value.trim() === '');

    if (emptyInputs.length > 0) {
      emptyInputs.forEach((input) => input.classList.add('is-error'));
      setMessage('Fields should not be empty.', 'is-error');
      updateSubmitState();
      return;
    }

    if (emailInput && !isEmailValid(emailInput.value.trim())) {
      emailInput.classList.add('is-error');
      setMessage('The email address is not valid.', 'is-error');
      updateSubmitState();
      return;
    }

    setMessage('Form submission is currently disabled.', 'is-error');
  });

  updateSubmitState();
}

// Auto‑initialize when loaded in the browser
if (typeof window !== 'undefined') {
  if (document.readyState === 'loading') {
    window.addEventListener('DOMContentLoaded', () => {
      initNewsletterForm();
    });
  } else {
    initNewsletterForm();
  }
}
