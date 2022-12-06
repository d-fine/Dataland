import { zxcvbn, zxcvbnOptions } from '@zxcvbn-ts/core'
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

const passwordField = document.querySelector<HTMLInputElement>('#password')!
const hintSpan = document.querySelector<HTMLSpanElement>('#password-security-hint')!

let passwordScoreCalculationTimeout: number | null = null;
passwordField.addEventListener('change', function () {
    evaluatePasswordSecurity();
});
passwordField.addEventListener('input', function () {
    if (passwordScoreCalculationTimeout != null) {
        clearTimeout(passwordScoreCalculationTimeout);
        passwordScoreCalculationTimeout = null;
    }
    passwordScoreCalculationTimeout = setTimeout(evaluatePasswordSecurity, 250)
});

function evaluatePasswordSecurity() {
    if (passwordScoreCalculationTimeout != null) {
        clearTimeout(passwordScoreCalculationTimeout);
        passwordScoreCalculationTimeout = null;
    }

    const evaluation = zxcvbn(passwordField.value);
    hintSpan.innerText = `Score: ${evaluation.score}/4, Warning: ${evaluation.feedback.warning}, Hint: ${evaluation.feedback.suggestions}`
}
