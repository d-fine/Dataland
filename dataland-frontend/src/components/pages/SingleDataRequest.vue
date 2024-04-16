<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="paper-section no-ui-message">
      <CompanyInfoSheet
        :company-id="companyIdentifier"
        @fetched-company-information="handleFetchedCompanyInformation"
        :show-search-bar="false"
      />
      <div class="col-12 mb-2 bg-white">
        <div class="text-left company-details px-4">
          <h1 data-test="headerLabel">Single Data Request</h1>
        </div>
      </div>
      <div class="col-12">
        <FormKit
          v-if="!submitted"
          :actions="false"
          v-model="singleDataRequestModel"
          type="form"
          @submit="submitRequest"
          id="requestDataFormId"
          name="requestDataFormName"
        >
          <div class="col-12">
            <div class="grid px-8 py-4 justify-content-center uploadFormWrapper">
              <div class="col-12 md:col-8 xl:col-6">
                <div class="grid">
                  <div class="col-12">
                    <BasicFormSection :data-test="'reportingPeriods'" header="Select at least one reporting period">
                      <div class="flex flex-wrap mt-4 py-2">
                        <ToggleChipFormInputs
                          :name="'reportingPeriods'"
                          :options="reportingPeriodOptions"
                          @changed="selectedReportingPeriodsError = false"
                        />
                      </div>
                      <p
                        v-if="selectedReportingPeriodsError"
                        class="text-danger text-xs mt-2"
                        data-test="reportingPeriodErrorMessage"
                      >
                        Select at least one reporting period to submit your request.
                      </p>
                    </BasicFormSection>
                    <BasicFormSection :data-test="'selectFramework'" header="Select a framework">
                      <FormKit
                        type="select"
                        placeholder="Select framework"
                        v-model="frameworkName"
                        name="Framework"
                        :options="frameworkOptions"
                        validation="required"
                        :validation-messages="{
                          required: 'Select a framework to submit your request',
                        }"
                        outer-class="long"
                        :data-test="'datapoint-framework'"
                      />
                    </BasicFormSection>
                    <BasicFormSection header="Provide Contact Details">
                      <label for="Emails" class="label-with-optional">
                        <b>Emails</b><span class="optional-text">Optional</span>
                      </label>
                      <FormKit
                        v-model="contactsAsString"
                        type="text"
                        name="contactDetails"
                        data-test="contactEmail"
                        @input="updateMessageVisibility"
                      />
                      <p class="gray-text font-italic" style="text-align: left">
                        By specifying contacts your data request will be directed accordingly.<br />
                        You can specify multiple comma separated email addresses.<br />
                        This increases the chances of expediting the fulfillment of your request.
                      </p>
                      <br />
                      <p class="gray-text font-italic" style="text-align: left">
                        If you don't have a specific contact person, no worries.<br />
                        We are committed to fulfilling your request to the best of our ability.
                      </p>
                      <br />
                      <label for="Message" class="label-with-optional">
                        <b>Message</b><span class="optional-text">Optional</span>
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
                          <input
                            type="checkbox"
                            class="ml-1"
                            v-model="consentToMessageDataUsageGiven"
                            data-test="acceptConditionsCheckbox"
                          />
                          <label class="tex-sm ml-2"
                            >I hereby declare that the recipient(s) stated above consented to being contacted by
                            Dataland with regard to this data request
                          </label>
                        </div>
                        <p
                          v-show="displayConditionsNotAcceptedError"
                          class="text-danger text-xs mt-2"
                          data-test="conditionsNotAcceptedErrorMessage"
                        >
                          You have to accept the terms and conditions to add a message
                        </p>
                      </div>
                    </BasicFormSection>
                  </div>
                  <PrimeDialog
                    v-model:visible="maxRequestReachedModalIsVisible"
                    v-if="maxRequestReachedModalIsVisible"
                    id="successModal"
                    :dismissableMask="false"
                    :modal="true"
                    :closable="false"
                    style="border-radius: 0.75rem; text-align: center; max-width: 400px"
                    :show-header="false"
                    :draggable="false"
                  >
                    <template v-if="true">
                      <div class="text-center" style="display: flex; flex-direction: column">
                        <div style="margin: 10px">
                          <em class="material-icons info-icon red-text" style="font-size: 2.5em"> error </em>
                        </div>
                        <div style="margin: 10px">
                          <h2 class="m-0" data-test="successText">Quota exceeded</h2>
                        </div>
                      </div>
                    </template>
                    <div class="text-block" style="margin: 15px">
                      Your quota of {{ MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER() }} single data requests per
                      day is exceeded. The quota will reset automatically tomorrow.
                    </div>
                    <div class="text-block" style="margin: 15px">
                      To avoid quotas altogether, consider becoming a premium user.
                      <a href="#" @click="openBecomePremiumUserEmail">Contact Erik Breen</a> for more information on
                      premium membership.
                    </div>
                    <div style="margin: 10px">
                      <PrimeButton label="CLOSE" @click="closeMaxRequestsReachedModal()" class="p-button-outlined" />
                    </div>
                  </PrimeDialog>

                  <div class="col-12 flex align-items-end">
                    <PrimeButton
                      type="submit"
                      label="Submit"
                      class="p-button p-button-sm d-letters ml-auto"
                      name="submit_request_button"
                      @click="checkPreSubmitConditions()"
                    >
                      SUBMIT DATA REQUEST
                    </PrimeButton>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </FormKit>
        <div v-if="submitted" data-test="submittedDiv">
          <template v-if="submittingSucceeded">
            <em class="material-icons info-icon green-text">check_circle</em>
            <h1 class="status-text" data-test="requestStatusText">Submitting your data request was successful.</h1>
          </template>

          <template v-if="!submittingSucceeded">
            <em class="material-icons info-icon red-text">error</em>
            <h1 class="status-text" data-test="requestStatusText">
              The submission of your data request was unsuccessful.
            </h1>
            <p>{{ errorMessage }}</p>
          </template>

          <PrimeButton
            type="button"
            @click="goToCompanyPage()"
            label="BACK TO COMPANY PAGE"
            class="uppercase p-button-outlined"
            data-test="backToCompanyPageButton"
          />
        </div>
      </div>
    </TheContent>
    <TheFooter :is-light-version="true" :sections="footerContent" />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import TheContent from "@/components/generics/TheContent.vue";
import { defineComponent, inject } from "vue";
import TheFooter from "@/components/generics/TheNewFooter.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import { type Content, type Page } from "@/types/ContentTypes";
import contentData from "@/assets/content.json";
import CompanyInfoSheet from "@/components/general/CompanyInfoSheet.vue";
import { type CompanyInformation, type DataTypeEnum, type ErrorResponse } from "@clients/backend";
import { type SingleDataRequest } from "@clients/communitymanager";
import PrimeButton from "primevue/button";
import type Keycloak from "keycloak-js";
import { AxiosError } from "axios";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import ToggleChipFormInputs from "@/components/general/ToggleChipFormInputs.vue";
import BasicFormSection from "@/components/general/BasicFormSection.vue";
import { humanizeStringOrNumber } from "@/utils/StringFormatter";
import { ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE } from "@/utils/Constants";
import PrimeDialog from "primevue/dialog";
import { openEmailClient } from "@/utils/Email";
import { MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER } from "@/DatalandSettings";

export default defineComponent({
  name: "SingleDataRequest",
  components: {
    PrimeDialog,
    BasicFormSection,
    ToggleChipFormInputs,
    CompanyInfoSheet,
    AuthenticationWrapper,
    TheHeader,
    TheContent,
    FormKit,
    TheFooter,
    PrimeButton,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    const content: Content = contentData;
    const footerPage: Page | undefined = content.pages.find((page) => page.url === "/");
    const footerContent = footerPage?.sections;

    const becomePremiumUserEmailTemplate = content.pages
      .find((page) => page.url === "/companies")
      .sections.find((section) => section.title === "Single Data Request")
      .cards.find((card) => card.title === "Interested in becoming a premium user");
    return {
      singleDataRequestModel: {},
      footerContent,
      fetchedCompanyInformation: {} as CompanyInformation,
      frameworkOptions: [] as { value: DataTypeEnum; label: string }[],
      frameworkName: this.$route.query.preSelectedFramework as DataTypeEnum,
      contactsAsString: "",
      allowAccessDataRequesterMessage: false,
      dataRequesterMessage: "Please provide a valid email before entering a message",
      dataRequesterMessageNotAllowedText: "Please provide a valid email before entering a message",
      dataRequesterMessageAllowedText: "",
      consentToMessageDataUsageGiven: false,
      errorMessage: "",
      selectedReportingPeriodsError: false,
      displayConditionsNotAcceptedError: false,
      reportingPeriodOptions: [
        { name: "2023", value: false },
        { name: "2022", value: false },
        { name: "2021", value: false },
        { name: "2020", value: false },
      ],
      submittingSucceeded: false,
      submitted: false,
      maxRequestReachedModalIsVisible: false,
      becomePremiumUserEmailTemplate,
    };
  },
  computed: {
    selectedReportingPeriods(): string[] {
      return this.reportingPeriodOptions
        .filter((reportingPeriodOption) => reportingPeriodOption.value)
        .map((reportingPeriodOption) => reportingPeriodOption.name);
    },
    selectedContacts(): string[] {
      return this.contactsAsString
        .split(",")
        .map((rawEmail) => rawEmail.trim())
        .filter((email) => email);
    },
    companyIdentifier(): string {
      return this.$route.params.companyId as string;
    },
  },
  methods: {
    /**
     * Returns the constant
     * @class
     */
    MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER() {
      return MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER;
    },
    /**
     * Opens an Email regarding becoming a premium user
     */
    openBecomePremiumUserEmail() {
      openEmailClient(this.becomePremiumUserEmailTemplate);
    },
    /**
     * Opens the Max Requests Reached Modal
     */
    openMaxRequestsReachedModal() {
      this.maxRequestReachedModalIsVisible = true;
    },
    /**
     * Closes the Max Requests Reached Modal
     */
    closeMaxRequestsReachedModal() {
      this.maxRequestReachedModalIsVisible = false;
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
    updateMessageVisibility(contactsAsString: string | undefined): void {
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

    /**
     * Updates if the message terms and conditions not being accepted should stop the user from submitting the request.
     * Based on if they are accepted or not and on if the user wants to submit a message
     */
    updateConditionsNotAcceptedError(): void {
      this.displayConditionsNotAcceptedError =
        !this.consentToMessageDataUsageGiven && this.allowAccessDataRequesterMessage;
    },

    /**
     * checks if the forms are filled out correctly and updates the displayed warnings accordingly
     */
    checkPreSubmitConditions(): void {
      this.checkIfAtLeastOneReportingPeriodSelected();
      this.updateConditionsNotAcceptedError();
    },

    /**
     * Returns if the forms are filled out correctly
     * @returns true if they are filled out correctly, false otherwise
     */
    preSubmitConditionsFulfilled(): boolean {
      return !this.displayConditionsNotAcceptedError && !this.selectedReportingPeriodsError;
    },

    /**
     * Check whether reporting periods have been selected
     */
    checkIfAtLeastOneReportingPeriodSelected(): void {
      if (!this.selectedReportingPeriods.length) {
        this.selectedReportingPeriodsError = true;
      }
    },
    /**
     * Saves the company information emitted by the CompanyInformation vue components event.
     * @param fetchedCompanyInformation the company information for the current company Id
     */
    handleFetchedCompanyInformation(fetchedCompanyInformation: CompanyInformation) {
      this.fetchedCompanyInformation = fetchedCompanyInformation;
    },
    /**
     * Builds a SingleDataRequest object using the currently entered inputs and returns it
     * @returns the SingleDataRequest object
     */
    collectDataToSend(): SingleDataRequest {
      return {
        companyIdentifier: this.companyIdentifier,
        dataType: this.frameworkName,
        reportingPeriods: this.selectedReportingPeriods as Set<string>,
        contacts: this.selectedContacts as Set<string>,
        message: this.dataRequesterMessage,
      };
    },
    /**
     * Submits the data request to the request service
     */
    async submitRequest(): Promise<void> {
      if (this.preSubmitConditionsFulfilled()) {
        try {
          const singleDataRequestObject = this.collectDataToSend();
          const requestDataControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).apiClients
            .requestController;
          const response = await requestDataControllerApi.postSingleDataRequest(singleDataRequestObject);
          this.errorMessage = response.statusText;
          this.submitted = true;
          this.submittingSucceeded = true;
        } catch (error) {
          console.error(error);
          if (error instanceof AxiosError) {
            const errorJSON = error.toJSON();
            if (errorJSON.status == 403) {
              this.openMaxRequestsReachedModal();
            } else {
              const responseMessages = (error.response?.data as ErrorResponse)?.errors;
              this.errorMessage = responseMessages ? responseMessages[0].message : error.message;
              this.submitted = true;
              this.submittingSucceeded = false;
            }
            console.log("Error:", error.toJSON());
          } else {
            this.submitted = true;
            this.submittingSucceeded = false;
            this.errorMessage =
              "An unexpected error occurred. Please try again or contact the support team if the issue persists.";
          }
        }
      }
    },
    /**
     * Populates the availableFrameworks property in the format expected by the dropdown filter
     */
    retrieveFrameworkOptions() {
      this.frameworkOptions = ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE.map((dataTypeEnum) => {
        return {
          value: dataTypeEnum,
          label: humanizeStringOrNumber(dataTypeEnum),
        };
      });
    },
    /**
     * Go to company cockpit page
     */
    goToCompanyPage() {
      const thisCompanyId = this.companyIdentifier;
      void this.$router.push({
        path: `/companies/${thisCompanyId}`,
      });
    },
  },
  mounted() {
    this.retrieveFrameworkOptions();
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

.years-container {
  display: flex;
  margin-top: 10px;
}

.years {
  border: 2px solid black;
  border-radius: 20px;
  padding: 8px 16px;
  margin-right: 10px;
  background-color: white;
  color: black;
  cursor: pointer;
}

.years.selected {
  background-color: #e67f3f;
  color: black;
  border-color: black;
  font-weight: bold;
}
</style>
