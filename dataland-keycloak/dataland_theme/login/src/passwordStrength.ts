import { zxcvbn, zxcvbnOptions, debounce } from '@zxcvbn-ts/core'
import zxcvbnCommonPackage from '@zxcvbn-ts/language-common'
import zxcvbnEnPackage from '@zxcvbn-ts/language-en'

zxcvbnOptions.setOptions({
    translations: zxcvbnEnPackage.translations,
    graphs: zxcvbnCommonPackage.adjacencyGraphs,
    dictionary: {
        ...zxcvbnCommonPackage.dictionary,
        ...zxcvbnEnPackage.dictionary,
    },
})

const passwordFieldId = document.querySelector<HTMLInputElement>('#password-new') ? "password-new" : "password"

const passwordField = document.querySelector<HTMLInputElement>(`#${passwordFieldId}`)!
const confirmPasswordField = document.querySelector<HTMLInputElement>('#password-confirm')!
const form = document.querySelector<HTMLInputElement>('form')!
const passwordStrengthIndicatorInner = document.querySelector<HTMLInputElement>('#password-strength-indicator > div')!

function overrideFieldErrorMessage(fieldId: string, errorMessage?: string) {
    const fieldInput = document.querySelector<HTMLInputElement>(`#${fieldId}`)!
    const fieldInputError = document.querySelector<HTMLDivElement>(`#input-error-${fieldId}`)!
    const fieldInputErrorText = document.querySelector<HTMLSpanElement>(`#input-error-${fieldId} span.input-error`)!

    if (errorMessage) {
        fieldInputErrorText.textContent = errorMessage
        fieldInput.classList.add("error")
        fieldInputError.classList.remove("hidden")
    } else {
        fieldInput.classList.remove("error")
        fieldInputError.classList.add("hidden")
    }
}

function updatePasswordStrengthIndicator(levelCss: string) {
    passwordStrengthIndicatorInner.classList.forEach((cls) => {
        if (cls.startsWith("d-password-strength")) {
            passwordStrengthIndicatorInner.classList.remove(cls)
        }
    })
    passwordStrengthIndicatorInner.classList.add(levelCss)
}

function evaluatePasswordSecurity(): boolean {
    checkIfPasswordAndConfirmMatch()

    const passwordLength = passwordField.value.length;
    if (passwordLength < 12) {
        overrideFieldErrorMessage(passwordFieldId, 'Please choose a password with at least 12 characters')
        updatePasswordStrengthIndicator("d-password-strength-too-short")
        return false;
    }

    if (passwordLength > 128) {
        overrideFieldErrorMessage(passwordFieldId, 'Please choose a password with at most 128 characters')
        updatePasswordStrengthIndicator("d-password-strength-too-short")
        return false;
    }

    const zxvbnEvaluation = zxcvbn(passwordField.value);
    updatePasswordStrengthIndicator(`d-password-strength-${zxvbnEvaluation.score}`)
    if (zxvbnEvaluation.score <= 1) {
        let response = 'This password is too insecure. ';
        if (zxvbnEvaluation.feedback.warning) response += `${zxvbnEvaluation.feedback.warning} `
        if (zxvbnEvaluation.feedback.suggestions.length > 0) response += zxvbnEvaluation.feedback.suggestions[0]
        overrideFieldErrorMessage(passwordFieldId, response)
        return false;
    }

    overrideFieldErrorMessage(passwordFieldId, undefined)
    return true;
}
const debouncedEvaluatePasswordSecurity = debounce(evaluatePasswordSecurity, 200, true)

function checkIfPasswordAndConfirmMatch(): boolean {
    if (passwordField.value === confirmPasswordField.value) {
        overrideFieldErrorMessage('password-confirm', undefined)
        return true
    } else {
        if (confirmPasswordField.value.length > 0) {
            overrideFieldErrorMessage('password-confirm', 'Password confirmation doesn\'t match.')
        }
        return false;
    }
}
passwordField.addEventListener('change', function () {
    evaluatePasswordSecurity();
});
passwordField.addEventListener('input', function () {
    debouncedEvaluatePasswordSecurity()
});
confirmPasswordField.addEventListener('input', function () {
    checkIfPasswordAndConfirmMatch();
});

form.onsubmit = function () {
    return evaluatePasswordSecurity() && checkIfPasswordAndConfirmMatch()
}