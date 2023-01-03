import { zxcvbn, zxcvbnOptions, debounce } from '@zxcvbn-ts/core'
import zxcvbnCommonPackage from '@zxcvbn-ts/language-common'
import zxcvbnEnPackage from '@zxcvbn-ts/language-en'
import { InputField } from "./utils/InputField";

zxcvbnOptions.setOptions({
    translations: zxcvbnEnPackage.translations,
    graphs: zxcvbnCommonPackage.adjacencyGraphs,
    dictionary: {
        ...zxcvbnCommonPackage.dictionary,
        ...zxcvbnEnPackage.dictionary,
    },
})

const passwordFieldId = document.querySelector<HTMLInputElement>('#password-new') ? "password-new" : "password"

const passwordField = new InputField(passwordFieldId)
const confirmPasswordField = new InputField('password-confirm')
const form = document.querySelector<HTMLInputElement>('form')!
const passwordStrengthIndicatorInner = document.querySelector<HTMLInputElement>('#password-strength-indicator > div')!

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

    const passwordLength = passwordField.inputField.value.length;
    if (passwordLength < 12) {
        passwordField.overrideFieldErrorMessage('Please choose a password with at least 12 characters')
        updatePasswordStrengthIndicator("d-password-strength-too-short")
        return false;
    }

    if (passwordLength > 128) {
        passwordField.overrideFieldErrorMessage('Please choose a password with at most 128 characters')
        updatePasswordStrengthIndicator("d-password-strength-too-short")
        return false;
    }

    const zxvbnEvaluation = zxcvbn(passwordField.inputField.value);
    updatePasswordStrengthIndicator(`d-password-strength-${zxvbnEvaluation.score}`)
    if (zxvbnEvaluation.score <= 1) {
        let response = 'This password is too insecure. ';
        if (zxvbnEvaluation.feedback.warning) response += `${zxvbnEvaluation.feedback.warning} `
        if (zxvbnEvaluation.feedback.suggestions.length > 0) response += zxvbnEvaluation.feedback.suggestions[0]
        passwordField.overrideFieldErrorMessage(response)
        return false;
    }

    passwordField.overrideFieldErrorMessage(undefined)
    return true;
}
const debouncedEvaluatePasswordSecurity = debounce(evaluatePasswordSecurity, 200, true)

function checkIfPasswordAndConfirmMatch(): boolean {
    if (passwordField.inputField.value === confirmPasswordField.inputField.value) {
        confirmPasswordField.overrideFieldErrorMessage(undefined)
        return true
    } else {
        if (confirmPasswordField.inputField.value.length > 0) {
            confirmPasswordField.overrideFieldErrorMessage('Password confirmation doesn\'t match.')
        }
        return false;
    }
}
passwordField.inputField.addEventListener('change', function () {
    evaluatePasswordSecurity();
});
passwordField.inputField.addEventListener('input', function () {
    debouncedEvaluatePasswordSecurity()
});
confirmPasswordField.inputField.addEventListener('input', function () {
    checkIfPasswordAndConfirmMatch();
});

form.onsubmit = function () {
    return evaluatePasswordSecurity() && checkIfPasswordAndConfirmMatch()
}
