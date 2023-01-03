export class InputField {
    public readonly inputField: HTMLInputElement;
    public readonly inputErrorContainer: HTMLDivElement;
    public readonly inputErrorTextSpan: HTMLSpanElement;

    constructor(fieldId: string) {
        this.inputField = document.querySelector<HTMLInputElement>(`#${fieldId}`)!,
        this.inputErrorContainer = document.querySelector<HTMLDivElement>(`#input-error-${fieldId}`)!,
        this.inputErrorTextSpan = document.querySelector<HTMLSpanElement>(`#input-error-${fieldId} span.input-error`)!
    }
    overrideFieldErrorMessage(errorMessage?: string) {
        if (errorMessage) {
            this.inputErrorTextSpan.textContent = errorMessage
            this.inputField.classList.add("error")
            this.inputErrorContainer.classList.remove("hidden")
        } else {
            this.inputField.classList.remove("error")
            this.inputErrorContainer.classList.add("hidden")
        }
    }
}
