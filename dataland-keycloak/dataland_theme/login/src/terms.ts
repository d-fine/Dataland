const acceptButton = document.querySelector<HTMLButtonElement>('#accept_button')!
const cancelButton = document.querySelector<HTMLButtonElement>('#cancel_button')!

const acceptTermsCbx = document.querySelector<HTMLInputElement>('#accept_terms')!
const acceptTermsError = document.querySelector<HTMLDivElement>('#accept_terms_error')!

const acceptPrivacyCbx = document.querySelector<HTMLInputElement>('#accept_privacy')!
const acceptPrivacyError = document.querySelector<HTMLDivElement>('#accept_privacy_error')!

const responseInput = document.querySelector<HTMLInputElement>('#response')!
const tocForm = document.querySelector<HTMLFormElement>('#toc-form')!

function requireChecked(checkBox: HTMLInputElement, errorDiv: HTMLDivElement): boolean {
    if (checkBox.checked) {
        errorDiv.style.display = "none";
        return true;
    } else {
        errorDiv.style.display = "flex";
        return false;
    }
}

acceptButton.onclick = function () {
    const termsOkay = requireChecked(acceptTermsCbx, acceptTermsError);
    const privacyOkay = requireChecked(acceptPrivacyCbx, acceptPrivacyError);
    if (termsOkay && privacyOkay) {
        responseInput.name = "accept"
        tocForm.submit()
    }
}

cancelButton.onclick = function () {
    responseInput.name = "cancel"
    tocForm.submit()
}

export {}