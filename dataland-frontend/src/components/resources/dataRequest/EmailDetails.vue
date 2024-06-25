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
    <p v-show="displayContactsNotValidError" class="text-danger text-xs" data-test="contactsNotValidErrorMessage">
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
      class="text-danger text-xs"
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
        class="text-danger text-xs mt-2"
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
      const contacts = ((): Set<string> | undefined => {
        if (this.areContactsValid()) {
          return new Set<string>(this.selectedContacts);
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
     * Checks if an email string is a valid email using regex
     * @param email the email string to check
     * @returns true if the email is valid, false otherwise
     */
    isValidEmail(email: string): boolean {
      // This RegEx should be kept consistent with the validation rules used by the community service in the backend
      const regex = /^[a-zA-Z0-9_.!-]+@([a-zA-Z0-9-]+\.){1,2}[a-z]{2,}$/;
      return regex.test(email.toLowerCase());
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
        this.selectedContacts.every((selectedContact) => this.isValidEmail(selectedContact))
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
  color: #e67f3f;
  margin-left: 8px;
}
</style>
