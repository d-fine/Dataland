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
      @input="handlesContactsUpdate"
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
    <p class="gray-text font-italic" style="text-align: left">
      Let your contacts know what exactly your are looking for.
    </p>
    <div v-show="allowAccessDataRequesterMessage">
      <div class="mt-3 flex">
        <label class="tex-sm flex">
          <input
            type="checkbox"
            class="ml-2 mr-3 mt-1"
            style="min-width: 17px"
            v-model="consentToMessageDataUsageGiven"
            data-test="acceptConditionsCheckbox"
            @click="displayContactsNotValidError = false"
          />
          I hereby declare that the recipient(s) stated above consented to being contacted by Dataland with regard to
          this data request
        </label>
      </div>
      <p
        v-show="displayContactsNotValidError"
        class="text-danger text-xs mt-2"
        data-test="conditionsNotAcceptedErrorMessage"
      >
        You have to declare that the recipient(s) consented in order to add a message
      </p>
    </div>
  </FormKit>
</template>
<script lang="ts">
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";

export default defineComponent({
  name: "EmailDetails",
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
  emits: ["hasNewInput"],
  data() {
    return {
      displayContactsNotValidError: false,
      displayConditionsNoEmailError: false,
      allowAccessDataRequesterMessage: false,
      consentToMessageDataUsageGiven: false,
      dataRequesterMessage: "Please provide a valid email before entering a message",
      dataRequesterMessageNotAllowedText: "Please provide a valid email before entering a message",
      dataRequesterMessageAllowedText: "",
      contactsAsString: "",
    };
  },
  computed: {
    hasValidInput() {
      return this.consentToMessageDataUsageGiven && this.areValidEmails(this.contactsAsString);
    },
    selectedContacts(): string[] {
      return this.contactsAsString
        .split(",")
        .map((rawEmail) => rawEmail.trim())
        .filter((email) => email);
    },
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
      this.$emit("hasNewInput", this.hasValidInput, new Set<string>(this.selectedContacts), this.dataRequesterMessage);
    },
    /**
     * Enables the error messages
     */
    displayErrors() {
      this.displayContactsNotValidError = !this.consentToMessageDataUsageGiven;
      this.displayConditionsNoEmailError = !this.areValidEmails(this.contactsAsString);
    },
    /**
     * Checks if the first email in a string of comma separated emails is valid
     * @param emails string of comma separated emails
     * @returns true if valid, false otherwise
     */
    areValidEmails(emails: string): boolean {
      return this.isValidEmail(emails.split(",")[0]);
    },
    /**
     * Checks if an email string is a valid email by checking for _@_._
     * @param email the email string to check
     * @returns true if the email is valid, false otherwise
     */
    isValidEmail(email: string): boolean {
      if (email == "") return false;

      const splitByEt = email.split("@");

      if (splitByEt.length != 2) return false;
      if (splitByEt[0] == "") return false;
      if (splitByEt[1] == "") return false;

      const splitByEtAndDot = splitByEt[1].split(".");

      if (splitByEtAndDot.length < 2) return false;
      if (splitByEtAndDot[0] == "") return false;
      return splitByEtAndDot[splitByEtAndDot.length - 1] != "";
    },

    /**
     * Updates if the message block is active and if the accept terms and conditions checkmark below is visible
     * and required, based on whether valid emails have been provided
     * @param contactsAsString the emails string to check
     */
    handlesContactsUpdate(contactsAsString: string | undefined): void {
      if (this.areValidEmails(<string>contactsAsString)) {
        this.allowAccessDataRequesterMessage = true;
        if (this.dataRequesterMessage == this.dataRequesterMessageNotAllowedText) {
          this.dataRequesterMessage = this.dataRequesterMessageAllowedText;
        }
      } else {
        this.allowAccessDataRequesterMessage = false;
        if (this.dataRequesterMessage != this.dataRequesterMessageNotAllowedText) {
          this.dataRequesterMessageAllowedText = this.dataRequesterMessage;
          this.dataRequesterMessage = this.dataRequesterMessageNotAllowedText;
        }
      }
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
