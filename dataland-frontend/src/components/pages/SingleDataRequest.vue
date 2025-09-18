<template>
  <TheContent>
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
                  <BasicFormSection
                    :data-test="'reportingPeriods'"
                    header="Select at least one reporting period"
                    class="header-styling"
                  >
                    <div class="flex flex-wrap mt-4 py-2">
                      <ToggleChipFormInputs
                        :name="'reportingPeriods'"
                        :selectedOptions="reportingPeriodOptions"
                        :available-options="reportingPeriodOptions"
                        @changed="selectedReportingPeriodsError = false"
                      />
                    </div>
                    <Message
                      v-if="selectedReportingPeriodsError"
                      severity="error"
                      variant="simple"
                      size="small"
                      data-test="reportingPeriodErrorMessage"
                      style="margin-top: var(--spacing-xs)"
                    >
                      Select at least one reporting period to submit your request
                    </Message>
                  </BasicFormSection>
                  <BasicFormSection data-test="'selectFramework'" header="Select a framework" class="header-styling">
                    <PrimeSelect
                      placeholder="Select framework"
                      v-model="frameworkName"
                      name="Framework"
                      :options="frameworkOptions"
                      option-label="label"
                      option-value="value"
                      data-test="datapoint-framework"
                      :highlightOnSelect="false"
                      @change="selectedFrameworkError = false"
                      fluid
                    />
                    <Message
                      v-if="selectedFrameworkError"
                      severity="error"
                      variant="simple"
                      size="small"
                      data-test="frameworkErrorMessage"
                      style="margin-top: var(--spacing-xs)"
                    >
                      Select a framework to submit your request
                    </Message>
                  </BasicFormSection>
                  <BasicFormSection
                    :data-test="'notifyMeImmediately'"
                    header="Notify Me Immediately"
                    class="header-styling"
                  >
                    <div class="dataland-info-text normal">Receive emails directly or via summary</div>
                    <ToggleSwitch
                      style="display: block; margin: var(--spacing-md) 0"
                      data-test="notifyMeImmediatelyInput"
                      inputId="notifyMeImmediatelyInput"
                      v-model="notifyMeImmediately"
                    />
                    <label for="notifyMeImmediatelyInput" data-test="notifyMeImmediatelyText">
                      <strong v-if="notifyMeImmediately">immediate update</strong>
                      <span v-else>weekly summary</span>
                    </label>
                  </BasicFormSection>
                  <BasicFormSection
                    :data-test="'informationCompanyOwnership'"
                    header="Information about company ownership"
                    class="header-styling"
                  >
                    <p v-if="hasCompanyAtLeastOneOwner" class="dataland-info-text normal">
                      This company has at least one company owner. <br />
                      The company company owner(s) will be informed about your data request.
                    </p>
                    <p v-else class="dataland-info-text normal">This company does not have a company owner yet.</p>
                  </BasicFormSection>
                  <BasicFormSection header="Provide Contact Details" class="header-styling">
                    <label for="Emails" class="label-with-optional">
                      <b>Emails</b><span class="optional-text">Optional</span>
                    </label>
                    <FormKit
                      v-model="contactsAsString"
                      type="text"
                      name="contactDetails"
                      data-test="contactEmail"
                      @input="handleContactsUpdate"
                    />
                    <p
                      v-show="displayContactsNotValidError"
                      class="text-danger"
                      data-test="contactsNotValidErrorMessage"
                    >
                      You have to provide valid contacts to add a message to the request
                    </p>
                    <p class="dataland-info-text normal">
                      By specifying contacts your data request will be directed accordingly.<br />
                      You can specify multiple comma separated email addresses.<br />
                      This increases the chances of expediting the fulfillment of your request.
                    </p>
                    <br />
                    <p class="dataland-info-text normal">
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
                    <p class="dataland-info-text normal">Let your contacts know what exactly your are looking for.</p>
                    <div v-show="allowAccessDataRequesterMessage">
                      <div class="mt-3 flex">
                        <label class="tex-sm flex">
                          <input
                            type="checkbox"
                            class="ml-2 mr-3 mt-1"
                            style="min-width: 17px"
                            v-model="consentToMessageDataUsageGiven"
                            data-test="acceptConditionsCheckbox"
                            @click="displayConditionsNotAcceptedError = false"
                          />
                          I hereby declare that the recipient(s) stated above consented to being contacted by Dataland
                          with regard to this data request
                        </label>
                      </div>
                      <p
                        v-show="displayConditionsNotAcceptedError"
                        class="text-danger mt-2"
                        data-test="conditionsNotAcceptedErrorMessage"
                      >
                        You have to declare that the recipient(s) consented in order to add a message
                      </p>
                    </div>
                  </BasicFormSection>
                </div>
                <PrimeDialog
                  v-model:visible="maxRequestReachedModalIsVisible"
                  id="successModal"
                  :dismissableMask="false"
                  :modal="true"
                  :closable="false"
                  style="border-radius: var(--spacing-sm); text-align: center; max-width: 400px"
                  :show-header="false"
                  :draggable="false"
                  data-test="quotaReachedModal"
                >
                  <em class="material-icons info-icon red-text" style="font-size: 3em">error</em>
                  <div class="text-block" style="margin: 15px">
                    Your quota of {{ MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER }} single data requests per day
                    is exceeded. The quota will reset automatically tomorrow.
                  </div>
                  <div class="text-block" style="margin: 15px">
                    To avoid quotas altogether, consider becoming a premium user.
                    <a href="#" @click="openBecomePremiumUserEmail">Contact Erik Breen</a> for more information on
                    premium membership.
                  </div>
                  <div style="margin: 10px">
                    <PrimeButton
                      label="CLOSE"
                      @click="closeMaxRequestsReachedModal()"
                      class="p-button-outlined"
                      data-test="closeMaxRequestsReachedModalButton"
                    />
                  </div>
                </PrimeDialog>
              </div>
              <PrimeButton
                type="submit"
                label="SUBMIT DATA REQUEST"
                @click="checkPreSubmitConditions"
                class="submit-button"
              />
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
</template>

<script lang="ts">
import contentData from '@/assets/content.json';
import BasicFormSection from '@/components/general/BasicFormSection.vue';
import CompanyInfoSheet from '@/components/general/CompanyInfoSheet.vue';
import ToggleChipFormInputs from '@/components/general/ToggleChipFormInputs.vue';
import TheContent from '@/components/generics/TheContent.vue';
import { MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER } from '@/DatalandSettings';
import router from '@/router';
import { ApiClientProvider } from '@/services/ApiClients';
import type { Content } from '@/types/ContentTypes.ts';
import { hasCompanyAtLeastOneCompanyOwner } from '@/utils/CompanyRolesUtils';
import { FRAMEWORKS_WITH_VIEW_PAGE, FRONTEND_CREATABLE_REQUESTS_REPORTING_PERIODS } from '@/utils/Constants';
import { openEmailClient } from '@/utils/Email';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { isEmailAddressValid } from '@/utils/ValidationUtils';
import { type CompanyInformation, type DataTypeEnum, type ErrorResponse } from '@clients/backend';
import { type SingleDataRequest, type SingleDataRequestDataTypeEnum } from '@clients/communitymanager';
import { FormKit } from '@formkit/vue';
import { AxiosError } from 'axios';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import PrimeDialog from 'primevue/dialog';
import ToggleSwitch from 'primevue/toggleswitch';
import { defineComponent, inject } from 'vue';
import PrimeSelect from 'primevue/select';
import Message from 'primevue/message';

export default defineComponent({
  name: 'SingleDataRequest',
  components: {
    Message,
    PrimeSelect,
    PrimeDialog,
    ToggleSwitch,
    BasicFormSection,
    ToggleChipFormInputs,
    CompanyInfoSheet,
    TheContent,
    FormKit,
    PrimeButton,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  data() {
    const content: Content = contentData;
    const companiesPage = content.pages.find((page) => page.url === '/companies');
    const singleDatRequestSection = companiesPage
      ? companiesPage.sections.find((section) => section.title === 'Single Data Request')
      : undefined;
    const becomePremiumUserEmailTemplate = singleDatRequestSection
      ? singleDatRequestSection.cards?.find((card) => card.title === 'Interested in becoming a premium user')
      : undefined;

    const dataRequesterMessageAccessDisabledText = 'Please provide a valid email before entering a message';

    return {
      singleDataRequestModel: {},
      fetchedCompanyInformation: {} as CompanyInformation,
      frameworkOptions: [] as { value: DataTypeEnum; label: string }[],
      frameworkName: router.currentRoute.value.query.preSelectedFramework as SingleDataRequestDataTypeEnum,
      contactsAsString: '',
      allowAccessDataRequesterMessage: false,
      dataRequesterMessage: dataRequesterMessageAccessDisabledText,
      dataRequesterMessageAccessDisabledText,
      consentToMessageDataUsageGiven: false,
      errorMessage: '',
      selectedReportingPeriodsError: false,
      selectedFrameworkError: false,
      displayConditionsNotAcceptedError: false,
      displayContactsNotValidError: false,
      reportingPeriodOptions: FRONTEND_CREATABLE_REQUESTS_REPORTING_PERIODS.map((period) => {
        return {
          name: period,
          value: false,
        };
      }),
      submittingSucceeded: false,
      submitted: false,
      maxRequestReachedModalIsVisible: false,
      becomePremiumUserEmailTemplate,
      MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER,
      hasCompanyAtLeastOneOwner: false,
      notifyMeImmediately: false,
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
        .split(',')
        .map((rawEmail) => rawEmail.trim())
        .filter((email) => email);
    },
    companyIdentifier(): string {
      return router.currentRoute.value.params.companyId as string;
    },
    selectedFrameworks(): DataTypeEnum[] {
      return this.frameworkName ? [this.frameworkName] : [];
    },
  },
  methods: {
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
     * Checks if the provided contacts are accepted
     * @returns true if all the provided emails are valid and at least one has been provided, false otherwise
     */
    areContactsFilledAndValid(): boolean {
      if (this.selectedContacts.length == 0) return false;
      return this.areContactsValid();
    },
    /**
     * Checks if each of the provided contacts is a valid email
     * @returns true if the provided emails are all valid (therefor also if there are none), false otherwise
     */
    areContactsValid(): boolean {
      return this.selectedContacts.every((selectedContact) => isEmailAddressValid(selectedContact));
    },

    /**
     * updates the messagebox visibility and stops displaying the contacts not valid error
     */
    handleContactsUpdate(): void {
      this.displayContactsNotValidError = false;
      void this.$nextTick(() => this.updateMessageVisibility());
    },

    /**
     * Updates if the message block is active and if the accept terms and conditions checkmark below is visible
     * and required, based on whether valid contacts have been provided
     */
    updateMessageVisibility(): void {
      if (this.areContactsFilledAndValid()) {
        this.allowAccessDataRequesterMessage = true;
        if (this.dataRequesterMessage == this.dataRequesterMessageAccessDisabledText) {
          this.dataRequesterMessage = '';
        }
      } else {
        this.allowAccessDataRequesterMessage = false;
        if (this.contactsAsString == '' && this.dataRequesterMessage == '') {
          this.dataRequesterMessage = this.dataRequesterMessageAccessDisabledText;
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
     * Updates if an error should be displayed and submitting should be disabled because the provided contacts are not valid
     */
    updateContactsNotValidError(): void {
      this.displayContactsNotValidError = !this.areContactsValid();
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
     * Checks whether at least one framework has been selected
     */
    checkIfAtLeastOneFrameworkSelected(): void {
      if (!this.selectedFrameworks.length) {
        this.selectedFrameworkError = true;
      }
    },

    /**
     * checks if the forms are filled out correctly and updates the displayed warnings accordingly
     */
    checkPreSubmitConditions(): void {
      this.checkIfAtLeastOneReportingPeriodSelected();
      this.updateConditionsNotAcceptedError();
      this.updateContactsNotValidError();
      this.checkIfAtLeastOneFrameworkSelected();
    },

    /**
     * Returns if the forms are filled out correctly
     * @returns true if they are filled out correctly, false otherwise
     */
    preSubmitConditionsFulfilled(): boolean {
      return (
        !this.displayConditionsNotAcceptedError &&
        !this.selectedReportingPeriodsError &&
        !this.displayContactsNotValidError
      );
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
        // as unknown as Set<string> cast required to ensure proper json is created
        reportingPeriods: this.selectedReportingPeriods as unknown as Set<string>,
        contacts: this.selectedContacts as unknown as Set<string>,
        message: this.allowAccessDataRequesterMessage ? this.dataRequesterMessage : '',
        notifyMeImmediately: this.notifyMeImmediately,
      };
    },
    /**
     * Sets state variables
     * @param errorMessage sets error message
     * @param submitted sets submitted state
     * @param submittingSucceded sets succeded submit state
     */
    editStateVariables(errorMessage: string, submitted: boolean, submittingSucceded: boolean): void {
      this.errorMessage = errorMessage;
      this.submitted = submitted;
      this.submittingSucceeded = submittingSucceded;
    },
    /**
     * Submits the data request to the request service
     */
    async submitRequest(): Promise<void> {
      if (!this.preSubmitConditionsFulfilled()) {
        return;
      }
      try {
        const singleDataRequestObject = this.collectDataToSend();
        const requestDataControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).apiClients
          .requestController;
        const response = await requestDataControllerApi.postSingleDataRequest(singleDataRequestObject);
        this.editStateVariables(response.statusText, true, true);
      } catch (error) {
        console.error(error);
        if (error instanceof AxiosError) {
          if (error.response?.status == 403) {
            this.openMaxRequestsReachedModal();
          } else {
            const responseMessages = (error.response?.data as ErrorResponse)?.errors;
            this.editStateVariables(responseMessages ? responseMessages[0].message : error.message, true, false);
          }
        } else {
          this.editStateVariables(
            'An unexpected error occurred.' + ' Please try again or contact the support team if the issue persists.',
            true,
            false
          );
        }
      }
    },
    /**
     * Populates the availableFrameworks property in the format expected by the dropdown filter
     */
    retrieveFrameworkOptions() {
      this.frameworkOptions = FRAMEWORKS_WITH_VIEW_PAGE.map((dataTypeEnum) => {
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
      void router.push({
        path: `/companies/${thisCompanyId}`,
      });
    },
    /**
     * Updates the hasCompanyAtLeastOneOwner in an async way
     */
    async updateHasCompanyAtLeastOneOwner() {
      this.hasCompanyAtLeastOneOwner = await hasCompanyAtLeastOneCompanyOwner(
        this.companyIdentifier,
        this.getKeycloakPromise
      );
    },
  },
  mounted() {
    this.retrieveFrameworkOptions();
    this.updateHasCompanyAtLeastOneOwner().catch((error) => console.error(error));
  },
});
</script>

<style scoped lang="scss">
.submit-button {
  display: block;
  margin-left: auto;
}

.header-styling {
  text-align: left;
}

.label-with-optional {
  display: flex;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.optional-text {
  font-style: italic;
  color: var(--p-primary-color);
  margin-left: 8px;
}

.green-text {
  color: var(--green);
}

.red-text {
  color: var(--red);
}

.uploadFormWrapper {
  background-color: var(--p-surface-50);
}
</style>
