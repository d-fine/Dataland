export class InputField {
  public readonly inputField: HTMLInputElement;
  public readonly inputErrorContainer: HTMLDivElement;
  public readonly inputErrorTextSpan: HTMLSpanElement;

  constructor(public readonly inputFieldContainer: HTMLDivElement) {
    this.inputField =
      inputFieldContainer.querySelector<HTMLInputElement>(`input`)!;
    this.inputErrorContainer =
      inputFieldContainer.querySelector<HTMLDivElement>(
        `div.input-error-container`
      )!;
    this.inputErrorTextSpan =
      inputFieldContainer.querySelector<HTMLSpanElement>(`span.input-error`)!;
  }
  overrideFieldErrorMessage(errorMessage?: string) {
    if (errorMessage) {
      this.inputErrorTextSpan.textContent = errorMessage;
      this.inputField.classList.add("error");
      this.inputErrorContainer.classList.remove("hidden");
    } else {
      this.inputField.classList.remove("error");
      this.inputErrorContainer.classList.add("hidden");
    }
  }
}
