// newsletterForm.ts

function setMessage(messageEl: HTMLElement | null, text: string, type: string = ''): void {
  if (!messageEl) return;
  messageEl.textContent = text;
  messageEl.classList.remove('is-error', 'is-success');
  if (type) {
    messageEl.classList.add(type);
  }
}

function clearAllErrors(requiredInputs: HTMLInputElement[]): void {
  requiredInputs.forEach((input: HTMLInputElement): void => input.classList.remove('is-error'));
}

function isEmailValid(value: string): boolean {
  return value.includes('@');
}

function areAllFieldsFilled(requiredInputs: HTMLInputElement[]): boolean {
  return requiredInputs.every((input: HTMLInputElement): boolean => input.value.trim() !== '');
}

function isFormReady(requiredInputs: HTMLInputElement[], emailInput: HTMLInputElement | null): boolean {
  const emailValue: string = emailInput?.value.trim() ?? '';
  return areAllFieldsFilled(requiredInputs) && isEmailValid(emailValue);
}

function updateSubmitState(
  submitButton: HTMLButtonElement | null,
  requiredInputs: HTMLInputElement[],
  emailInput: HTMLInputElement | null
): void {
  if (!submitButton) return;
  const enabled: boolean = isFormReady(requiredInputs, emailInput);
  submitButton.disabled = !enabled;
  submitButton.classList.toggle('is-enabled', enabled);
}

export function initNewsletterForm(): void {
  const form = document.querySelector<HTMLFormElement>('#newsletter-form');
  const submitButton = document.querySelector<HTMLButtonElement>('#submit-button');
  const messageEl = document.querySelector<HTMLElement>('#form-message');

  const firstNameInput = document.querySelector<HTMLInputElement>('#firstName');
  const lastNameInput = document.querySelector<HTMLInputElement>('#lastName');
  const companyInput = document.querySelector<HTMLInputElement>('#company');
  const emailInput = document.querySelector<HTMLInputElement>('#email');

  const requiredInputs: HTMLInputElement[] = [firstNameInput, lastNameInput, companyInput, emailInput].filter(
    (input): input is HTMLInputElement => input !== null
  );

  const handleUpdateSubmitState = (): void => {
    updateSubmitState(submitButton, requiredInputs, emailInput ?? null);
  };

  requiredInputs.forEach((input: HTMLInputElement): void => {
    input.addEventListener('input', handleUpdateSubmitState);

    input.addEventListener('focus', (): void => {
      input.classList.remove('is-error');
      if (messageEl?.classList.contains('is-error')) {
        setMessage(messageEl, '');
      }
    });
  });

  form?.addEventListener('submit', (event: SubmitEvent): void => {
    event.preventDefault();

    clearAllErrors(requiredInputs);
    setMessage(messageEl, '');

    const emptyInputs: HTMLInputElement[] = requiredInputs.filter(
      (input: HTMLInputElement): boolean => input.value.trim() === ''
    );

    if (emptyInputs.length > 0) {
      emptyInputs.forEach((input: HTMLInputElement): void => input.classList.add('is-error'));
      setMessage(messageEl, 'Fields should not be empty.', 'is-error');
      handleUpdateSubmitState();
      return;
    }

    if (emailInput && !isEmailValid(emailInput.value.trim())) {
      emailInput.classList.add('is-error');
      setMessage(messageEl, 'The email address is not valid.', 'is-error');
      handleUpdateSubmitState();
      return;
    }

    setMessage(messageEl, 'Form submission is currently disabled.', 'is-error');
  });

  handleUpdateSubmitState();
}

if (typeof document !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      initNewsletterForm();
    });
  } else {
    initNewsletterForm();
  }
}
