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
                        Select at least one reporting period to submit your request
                      </p>
                    </BasicFormSection>
                    <BasicFormSection :data-test="'selectFramework'" header="Select a framework">
                      <SingleSelectFormElement
                        placeholder="Select framework"
                        v-model="frameworkName"
                        name="Framework"
                        :options="frameworkOptions"
                        validation="required"
                        :validation-messages="{
                          required: 'Select a framework to submit your request',
                        }"
                        required
                        data-test="datapoint-framework"
                      />
                    </BasicFormSection>
                    <BasicFormSection :data-test="'emailOnUpdate'" header="Receive emails on update">
                      <InputSwitch
                        class="p-inputswitch p-inputswitch-slider"
                        style="display: block; margin: 1rem 0"
                        data-test="emailOnUpdateInput"
                        inputId="emailOnUpdateInput"
                        v-model="emailOnUpdate"
                      />
                      <label for="emailOnUpdateInput" v-if="emailOnUpdate" data-test="emailOnUpdateText">
                        You receive an email immediately after the next status change, i.e. if the data is available or
                        the data provider says there is no data.
                      </label>
                      <label for="emailOnUpdateInput" v-else data-test="emailOnUpdateText">
                        You receive updates in your weekly summary letter.
                      </label>
                    </BasicFormSection>
                    <BasicFormSection
                      :data-test="'informationCompanyOwnership'"
                      header="Information about company ownership"
                    >
                      <p v-if="hasCompanyAtLeastOneOwner">
                        This company has at least one company owner. <br />
                        The company company owner(s) will be informed about your data request.
                      </p>
                      <p v-else>This company does not have a company owner yet.</p>
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
                        @input="handleContactsUpdate"
                      />
                      <p
                        v-show="displayContactsNotValidError"
                        class="text-danger text-xs"
                        data-test="contectsNotValidErrorMessage"
                      >
                        You have to provide valid contacts to add a message to the request
                      </p>
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
                          class="text-danger text-xs mt-2"
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
                    style="border-radius: 0.75rem; text-align: center; max-width: 400px"
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

                  <div class="col-12 flex align-items-end">
                    <PrimeButton
                      type="submit"
                      label="Submit"
                      class="p-button p-button-sm d-letters ml-auto"
                      name="submit_request_button"
                      @click="checkPreSubmitConditions"
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
import { FormKit } from '@formkit/vue';
import TheContent from '@/components/generics/TheContent.vue';
import { defineComponent, inject } from 'vue';
import TheFooter from '@/components/generics/TheNewFooter.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import { type Content, type Page } from '@/types/ContentTypes';
import contentData from '@/assets/content.json';
import CompanyInfoSheet from '@/components/general/CompanyInfoSheet.vue';
import { type CompanyInformation, type DataTypeEnum, type ErrorResponse } from '@clients/backend';
import { type SingleDataRequest, type SingleDataRequestDataTypeEnum } from '@clients/communitymanager';
import PrimeButton from 'primevue/button';
import type Keycloak from 'keycloak-js';
import { AxiosError } from 'axios';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import ToggleChipFormInputs from '@/components/general/ToggleChipFormInputs.vue';
import BasicFormSection from '@/components/general/BasicFormSection.vue';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import PrimeDialog from 'primevue/dialog';
import InputSwitch from 'primevue/inputswitch';
import { openEmailClient } from '@/utils/Email';
import { MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER } from '@/DatalandSettings';
import { hasCompanyAtLeastOneCompanyOwner } from '@/utils/CompanyRolesUtils';
import SingleSelectFormElement from '@/components/forms/parts/elements/basic/SingleSelectFormElement.vue';
import router from '@/router';
import { isEmailAddressValid } from '@/utils/ValidationUtils';

export default defineComponent({
  name: 'SingleDataRequest',
  components: {
    SingleSelectFormElement,
    PrimeDialog,
    InputSwitch,
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
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  data() {
    const content: Content = contentData;
    const footerPage: Page | undefined = content.pages.find((page) => page.url === '/');
    const footerContent = footerPage?.sections;

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
      footerContent,
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
      displayConditionsNotAcceptedError: false,
      displayContactsNotValidError: false,
      reportingPeriodOptions: [
        { name: '2024', value: false },
        { name: '2023', value: false },
        { name: '2022', value: false },
        { name: '2021', value: false },
        { name: '2020', value: false },
      ],
      submittingSucceeded: false,
      submitted: false,
      maxRequestReachedModalIsVisible: false,
      becomePremiumUserEmailTemplate,
      MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER,
      hasCompanyAtLeastOneOwner: false,
      emailOnUpdate: false,
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
     * checks if the forms are filled out correctly and updates the displayed warnings accordingly
     */
    checkPreSubmitConditions(): void {
      this.checkIfAtLeastOneReportingPeriodSelected();
      this.updateConditionsNotAcceptedError();
      this.updateContactsNotValidError();
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
