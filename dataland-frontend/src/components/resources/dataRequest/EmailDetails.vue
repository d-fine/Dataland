<template>
  <FormKit type="form" :actions="false" class="formkit-wrapper">
    <label for="Emails" class="label-with-optional">
      <b>Emails</b><span v-if="isOptional" class="optional-text">Optional</span>
    </label>
    <FormKit
      v-model="contactsAsString"
      type="text"
      name="contactDetails"
      data-test="contactEmail"
      @input="handleContactsUpdate"
    />
    <p v-show="displayContactsNotValidError" class="text-danger" data-test="contactsNotValidErrorMessage">
      You have to provide valid contacts to add a message to the request
    </p>
    <p class="gray-text font-italic" style="text-align: left">
      By specifying contacts your data request will be directed accordingly.<br />
      You can specify multiple comma separated email addresses.<br />
      This increases the chances of expediting the fulfillment of your request.
    </p>
    <br />
    <p v-if="isOptional" class="gray-text font-italic" style="text-align: left">
      If you don't have a specific contact person, no worries.<br />
      We are committed to fulfilling your request to the best of our ability.
    </p>
    <br />
    <label for="Message" class="label-with-optional">
      <b>Message</b><span v-if="isOptional" class="optional-text">Optional</span>
    </label>
    <FormKit
      v-model="dataRequesterMessage"
      type="textarea"
      name="dataRequesterMessage"
      data-test="dataRequesterMessage"
      v-bind:disabled="!allowAccessDataRequesterMessage"
    />
    <p
      v-show="displayNoMessageError && allowAccessDataRequesterMessage"
      class="text-danger"
      data-test="noMessageErrorMessage"
    >
      You have not provided a message yet.
    </p>
    <p class="gray-text font-italic" style="text-align: left">
      Let your contacts know what exactly your are looking for.
    </p>
    <div v-show="allowAccessDataRequesterMessage">
      <div class="mt-3 flex">
        <label class="tex-sm flex uploadFormWrapper">
          <input
            type="checkbox"
            class="ml-2 mr-3 mt-1"
            style="min-width: 17px"
            v-model="consentToMessageDataUsageGiven"
            data-test="acceptConditionsCheckbox"
            @click="displayContactsNotValidError = false"
          />
          I hereby declare that the recipient(s) stated above consented to <br />
          being contacted by Dataland with regard to this data request
        </label>
      </div>
      <p
        v-show="displayConsentToMessageDateUsageNotGiven"
        class="text-danger mt-2"
        data-test="conditionsNotAcceptedErrorMessage"
      >
        You have to declare that the recipient(s) consented in order to add a message
      </p>
    </div>
  </FormKit>
</template>
<script lang="ts">
import { defineComponent } from 'vue';
import { FormKit } from '@formkit/vue';
import { isEmailAddressValid } from '@/utils/ValidationUtils';
const dataRequesterMessageAccessDisabledText = 'Please provide a valid email before entering a message';

export default defineComponent({
  name: 'EmailDetails',
  components: { FormKit },
  props: {
    isOptional: {
      type: Boolean,
      default: true,
    },
    showErrors: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['hasNewInput'],
  data() {
    return {
      displayNoMessageError: false,
      displayContactsNotValidError: false,
      displayConsentToMessageDateUsageNotGiven: false,
      allowAccessDataRequesterMessage: false,
      consentToMessageDataUsageGiven: false,
      dataRequesterMessage: 'Please provide a valid email before entering a message',
      contactsAsString: '',
    };
  },
  computed: {
    hasValidInput() {
      return this.areAllFieldsFilledCorrectly() || (this.isOptional && this.contactsAsString.length == 0);
    },
    selectedContacts(): string[] {
      return this.contactsAsString
        .split(',')
        .map((rawEmail) => rawEmail.trim())
        .filter((email) => email);
    },
  },
  mounted() {
    this.emitInput();
  },
  watch: {
    hasValidInput() {
      this.emitInput();
    },
    selectedContacts() {
      this.emitInput();
    },
    dataRequesterMessage() {
      this.emitInput();
    },
    showErrors() {
      this.displayErrors();
    },
  },
  methods: {
    /**
     * Method to emit the provided Input
     */
    emitInput() {
      const contacts = ((): string[] | undefined => {
        if (this.areContactsValid()) {
          return this.selectedContacts;
        } else {
          return undefined;
        }
      })();
      const message = ((): string | undefined => {
        if (contacts) {
          return this.dataRequesterMessage;
        } else {
          return undefined;
        }
      })();
      this.$emit('hasNewInput', this.hasValidInput, contacts, message);
    },
    /**
     * Enables the error messages
     */
    displayErrors() {
      this.updateContactsNotValidError();
      this.displayConsentToMessageDateUsageNotGiven = !this.consentToMessageDataUsageGiven;
      this.displayNoMessageError = this.dataRequesterMessage.length == 0;
    },

    /**
     * updates the messagebox visibility and stops displaying the contacts not valid error
     */
    handleContactsUpdate(): void {
      this.displayContactsNotValidError = false;
      this.$nextTick(() => this.updateMessageVisibility()).catch((error) => console.error(error));
    },

    /**
     * Updates if the message block is active and if the accept terms and conditions checkmark below is visible
     * and required, based on whether valid contacts have been provided
     */
    updateMessageVisibility(): void {
      if (this.areContactsFilledAndValid()) {
        this.allowAccessDataRequesterMessage = true;
        if (this.dataRequesterMessage == dataRequesterMessageAccessDisabledText) {
          this.dataRequesterMessage = '';
        }
      } else {
        this.allowAccessDataRequesterMessage = false;
        this.consentToMessageDataUsageGiven = false;
        this.displayConsentToMessageDateUsageNotGiven = false;
        if (this.contactsAsString == '' && this.dataRequesterMessage == '') {
          this.dataRequesterMessage = dataRequesterMessageAccessDisabledText;
        }
      }
    },
    /**
     * Checks if the provided contacts are accepted
     * @returns true if all the provided emails are valid and at least one has been provided, false otherwise
     */
    areContactsFilledAndValid(): boolean {
      if (this.selectedContacts.length == 0) return false;
      return this.areContactsValid();
    },
    /**
     * Updates if an error should be displayed and submitting should be disabled because the provided contacts are not valid
     */
    updateContactsNotValidError(): void {
      this.displayContactsNotValidError = !this.areContactsValid();
    },
    /**
     * Checks if each of the provided contacts is a valid email
     * @returns true if the provided emails are all valid (therefor also if there are none), false otherwise
     */
    areContactsValid(): boolean {
      return (
        this.contactsAsString.length > 0 &&
        this.selectedContacts.every((selectedContact) => isEmailAddressValid(selectedContact))
      );
    },
    /**
     * Checks if all fields are filled correctly
     * @returns true if the provided emails are all valid (therefor also if there are none), false otherwise
     */
    areAllFieldsFilledCorrectly(): boolean {
      return this.consentToMessageDataUsageGiven && this.areContactsValid() && this.dataRequesterMessage.length > 0;
    },
  },
});
</script>

<style scoped lang="scss">
.label-with-optional {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.optional-text {
  font-style: italic;
  color: var(--p-primary-color);
  margin-left: 8px;
}

.text-danger {
  color: var(--fk-color-error);
  font-size: var(--font-size-xs);
}

.gray-text {
  color: var(--gray);
}

.uploadFormWrapper {
  input[type='checkbox'],
  input[type='radio'] {
    display: grid;
    place-content: center;
    height: 18px;
    width: 18px;
    cursor: pointer;
    margin: 0 10px 0 0;
  }
  input[type='checkbox'] {
    background-color: var(--input-text-bg);
    border: 2px solid var(--input-checked-color);
    border-radius: 2px;
  }
  input[type='checkbox']:not(.p-radiobutton):checked {
    background-color: var(--input-checked-color);
  }
  input[type='radio'],
  input.p-radiobutton {
    background-color: white;
    border: 2px solid var(--input-label-color);
    border-radius: 15px;
    cursor: pointer;
    margin-right: 10px;
  }
  input.p-radiobutton:hover,
  input.p-radiobutton:active,
  input.p-radiobutton:checked {
    border: 3px solid var(--input-label-color);
  }
  input[type='checkbox']::before,
  input[type='radio']::before {
    content: '';
    width: 5px;
    height: 7px;
    border-width: 0 2px 2px 0;
    transform: rotate(45deg);
    margin-top: -2px;
    display: none;
  }
  input[type='checkbox']::before {
    border-style: solid;
    border-color: var(--input-text-bg);
  }
  input[type='radio']::before,
  input.p-radiobutton::before {
    border-style: solid;
    border-color: var(--input-label-color);
  }
  input[type='checkbox']:checked::before,
  input[type='radio']:checked::before {
    display: block;
  }
  label[data-checked='true'] {
    input[type='radio']::before,
    input.p-radiobutton::before {
      display: block;
    }
  }
  .p-multiselect {
    font-size: var(--font-size-base);
    border-radius: 0;
    background: var(--input-text-bg);
    border-style: solid;
    border-width: 0 0 1px 0;
    border-color: var(--input-text-border);
    box-shadow: none;
    width: 100%;

    &:focus-within {
      outline: none;
      border-bottom: 1px solid var(--input-text-border-hover);
    }
    .p-multiselect-label {
      padding: 0.75rem;
      &.p-placeholder {
        color: var(--text-color-secondary);
      }
    }
    .p-multiselect-trigger {
      width: 2.5rem;
      .p-multiselect-trigger-icon {
        color: var(--text-color-secondary);
        font-size: 0.75rem;
      }
    }
  }
  .w-100 {
    width: 100px;
  }
  .short {
    width: 33%;
  }
  .medium {
    width: 66%;
  }
  .long {
    width: 100%;
  }
  .shortish-hard {
    width: 45%;
    min-width: 45%;
    max-width: 45%;
  }
  .normal-line-height {
    line-height: normal;
  }
  .no-selection .p-dropdown-label {
    color: #767676;
  }
  .yes-no-radio {
    fieldset.formkit-fieldset {
      border: 0;
      padding: 0;
    }
    .formkit-options {
      display: flex;
      padding: 0.5rem 0 0 0;
      .formkit-option:nth-child(n + 2) {
        margin-left: 10%;
      }
    }
  }
  .formkit-wrapper,
  .formkit-fieldset {
    max-width: 100%;
  }
  .title {
    margin: 0.25rem 0;
  }
  .subtitle {
    display: block;
    padding-bottom: 1rem;
    .form-field-label {
      h5 {
        font-size: var(--font-size-base);
      }
    }
  }
  .form-field-label {
    display: flex;
    align-self: center;
    .info-icon {
      margin-left: 0.5rem;
    }
    button {
      margin-left: auto;
    }
    h5 {
      margin: 0.5rem 0;
    }
    .asterisk {
      color: #ee1a1a;
    }
    .asterisk:before {
      content: ' ';
      display: inline-block;
      width: 0.25rem;
    }
  }
  .middle-next-to-field {
    display: flex;
    align-self: center;
    padding-bottom: 1rem;
  }
  p {
    margin: 0.25rem;
  }
  .p-datepicker {
    width: 100%;
    .p-button {
      margin: 0;
    }
  }
  .formFields {
    background: var(--upload-form-bg);
    padding: var(--spacing-lg);
    margin-left: auto;
    margin-bottom: 1rem;
  }
  .formkit-message {
    &[data-message-type='ui'] {
      text-align: center;
      width: 100%;
      padding: 2rem;
      border: 1px solid var(--input-error);
    }
  }
  .hidden-input {
    .formkit-wrapper {
      visibility: hidden;
      height: 0;
    }
  }
  .form-list-item {
    color: var(--input-label-color);
    background: var(--el-list-item-bg);
    font-size: var(--font-size-sm);
    font-weight: var(--font-weight-semibold);
    margin: var(--spacing-xxs);
    text-align: center;
    min-width: 1.5rem;
    border-radius: 1rem;
    padding: 0.25rem 1rem;
    border: 1px solid var(--input-label-color);
    display: inline-flex;
    align-items: center;
    em {
      margin-left: 0.5rem;
      cursor: pointer;
      &:hover {
        color: hsl(from var(--input-label-color) h s calc(l - 20));
      }
    }
  }
  .uploadFormSection {
    margin-bottom: 1.5rem;
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    .form-field:not(:last-child) {
      margin: 0 0 1rem 0;
      padding: 0 0 1rem 0;
      border-bottom: 1px solid var(--input-separator);
    }
  }
  .uploaded-files {
    .p-progressbar {
      display: none;
    }
    .p-fileupload {
      .p-fileupload-content {
        border: none;
        padding: 1rem;
        align-items: center;
        .file-upload-item {
          margin: 1rem 0;
        }
        button {
          margin-left: auto;
          width: 25px;
          height: 25px;
          padding: 0;
        }
      }
    }
  }
}

.info-icon {
  cursor: help;
}

.p-dropdown {
  background: var(--input-text-bg);
  .p-dropdown-trigger {
    background: transparent;
    color: var(--p-primary-contrast-color);
    width: 2.357rem;
  }
}
</style>
