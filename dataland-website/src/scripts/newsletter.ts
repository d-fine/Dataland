export function initNewsletterForm(): void {
  const form = document.querySelector<HTMLFormElement>('#newsletter-form');
  const submitButton = document.querySelector<HTMLButtonElement>('#submit-button');
  const messageEl = document.querySelector<HTMLElement>('#form-message');

  const firstNameInput = document.querySelector<HTMLInputElement>('#firstName');
  const lastNameInput = document.querySelector<HTMLInputElement>('#lastName');
  const companyInput = document.querySelector<HTMLInputElement>('#company');
  const emailInput = document.querySelector<HTMLInputElement>('#email');

  const requiredInputs = [firstNameInput, lastNameInput, companyInput, emailInput].filter(
    (input): input is HTMLInputElement => input !== null
  );

  function setMessage(text: string, type: string = ''): void {
    if (!messageEl) return;
    messageEl.textContent = text;
    messageEl.classList.remove('is-error', 'is-success');
    if (type) {
      messageEl.classList.add(type);
    }
  }

  function clearAllErrors(): void {
    requiredInputs.forEach((input: HTMLInputElement): void => input.classList.remove('is-error'));
  }

  function isEmailValid(value: string): boolean {
    return value.includes('@');
  }

  function areAllFieldsFilled(): boolean {
    return requiredInputs.every((input: HTMLInputElement): boolean => input.value.trim() !== '');
  }

  function isFormReady(): boolean {
    const emailValue: string = emailInput?.value.trim() ?? '';
    return areAllFieldsFilled() && isEmailValid(emailValue);
  }

  function updateSubmitState(): void {
    if (!submitButton) return;
    const enabled: boolean = isFormReady();
    submitButton.disabled = !enabled;
    submitButton.classList.toggle('is-enabled', enabled);
  }

  requiredInputs.forEach((input: HTMLInputElement): void => {
    input.addEventListener('input', updateSubmitState);

    input.addEventListener('focus', (): void => {
      input.classList.remove('is-error');
      if (messageEl?.classList.contains('is-error')) {
        setMessage('');
      }
    });
  });

  form?.addEventListener('submit', (event: SubmitEvent): void => {
    event.preventDefault();

    clearAllErrors();
    setMessage('');

    const emptyInputs: HTMLInputElement[] = requiredInputs.filter(
      (input: HTMLInputElement): boolean => input.value.trim() === ''
    );

    if (emptyInputs.length > 0) {
      emptyInputs.forEach((input: HTMLInputElement): void => input.classList.add('is-error'));
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

if (typeof window !== 'undefined') {
  if (document.readyState === 'loading') {
    window.addEventListener('DOMContentLoaded', () => {
      initNewsletterForm();
    });
  } else {
    initNewsletterForm();
  }
}
