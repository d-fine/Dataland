<template>
  <TheContent>
    <CompanyInfoSheet
      :company-id="companyIdentifier"
      @fetched-company-information="handleFetchedCompanyInformation"
      :show-search-bar="false"
    />
    <h1 data-test="headerLabel" style="text-align: left; padding-left: 1.5rem">Single Data Request</h1>
    <div v-if="!submitted" class="single-request-form">
      <Card data-test="reportingPeriods">
        <template #title>
          Select at least one reporting period
          <Divider />
        </template>
        <template #content>
          <div class="flex flex-wrap py-2">
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
        </template>
      </Card>
      <Card data-test="selectFramework">
        <template #title>
          Select a framework
          <Divider />
        </template>
        <template #content>
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
        </template>
      </Card>
      <Card data-test="enterComment">
        <template #title>
          Enter a comment (optional)
          <Divider />
        </template>
        <template #content>
          <InputText type="text" v-model="enteredComment" />
        </template>
      </Card>

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
          Your quota of {{ MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER }} single data requests per day is
          exceeded. The quota will reset automatically tomorrow.
        </div>
        <div class="text-block" style="margin: 15px">
          To avoid quotas altogether, consider becoming a premium user.
          <a href="#" @click="openBecomePremiumUserEmail">Contact Erik Breen</a> for more information on premium
          membership.
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
      <PrimeButton type="submit" label="SUBMIT DATA REQUEST" @click="handleSubmission" class="submit-button" />
    </div>
    <div v-else data-test="submittedDiv">
      <template v-if="submittingSucceeded">
        <em class="material-icons info-icon green-text">check_circle</em>
        <h1 class="status-text" data-test="requestStatusText">Submitting your data request was successful.</h1>
      </template>

      <template v-else>
        <em class="material-icons info-icon red-text">error</em>
        <h1 class="status-text" data-test="requestStatusText">The submission of your data request was unsuccessful.</h1>
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
  </TheContent>
</template>

<script lang="ts">
import contentData from '@/assets/content.json';
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
import { type CompanyInformation, type DataTypeEnum, type ErrorResponse } from '@clients/backend';
import { type SingleDataRequestDataTypeEnum } from '@clients/communitymanager';
import { AxiosError } from 'axios';
import type Keycloak from 'keycloak-js';
import Card from 'primevue/card';
import Divider from 'primevue/divider';
import InputText from 'primevue/inputtext';
import PrimeButton from 'primevue/button';
import PrimeDialog from 'primevue/dialog';
import { defineComponent, inject } from 'vue';
import PrimeSelect from 'primevue/select';
import Message from 'primevue/message';
import { type SingleRequest } from '@clients/datasourcingservice';

export default defineComponent({
  name: 'SingleDataRequest',
  components: {
    Card,
    Divider,
    InputText,
    Message,
    PrimeSelect,
    PrimeDialog,
    ToggleChipFormInputs,
    CompanyInfoSheet,
    TheContent,
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

    return {
      fetchedCompanyInformation: {} as CompanyInformation,
      frameworkOptions: [] as { value: DataTypeEnum; label: string }[],
      frameworkName: router.currentRoute.value.query.preSelectedFramework as SingleDataRequestDataTypeEnum,
      errorMessage: '',
      selectedReportingPeriodsError: false,
      selectedFrameworkError: false,
      reportingPeriodOptions: FRONTEND_CREATABLE_REQUESTS_REPORTING_PERIODS.map((period) => {
        return {
          name: period,
          value: false,
        };
      }),
      enteredComment: '',
      submittingSucceeded: false,
      submitted: false,
      maxRequestReachedModalIsVisible: false,
      becomePremiumUserEmailTemplate,
      MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER,
      hasCompanyAtLeastOneOwner: false,
    };
  },
  computed: {
    selectedReportingPeriods(): string[] {
      return this.reportingPeriodOptions
        .filter((reportingPeriodOption) => reportingPeriodOption.value)
        .map((reportingPeriodOption) => reportingPeriodOption.name);
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
     * Check whether reporting periods have been selected
     */
    checkIfAtLeastOneReportingPeriodSelected(): boolean {
      if (!this.selectedReportingPeriods.length) {
        this.selectedReportingPeriodsError = true;
        return false;
      }
      return true;
    },

    /**
     * Checks whether at least one framework has been selected
     */
    checkIfAtLeastOneFrameworkSelected(): boolean {
      if (!this.selectedFrameworks.length) {
        this.selectedFrameworkError = true;
        return false;
      }
      return true;
    },

    /**
     * checks if the forms are filled out correctly and updates the displayed warnings accordingly
     */
    async handleSubmission(): Promise<void> {
      const reportingPeriodIsSelected = this.checkIfAtLeastOneReportingPeriodSelected();
      const frameworkIsSelected = this.checkIfAtLeastOneFrameworkSelected();
      if (reportingPeriodIsSelected && frameworkIsSelected) {
        await this.submitRequest();
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
     * Builds an array of SingleRequest objects using the currently entered inputs and returns it
     * @returns the array of SingleRequest objects
     */
    collectDataToSend(): SingleRequest[] {
      return this.selectedReportingPeriods.map((reportingPeriod) => {
        return {
          companyIdentifier: this.companyIdentifier,
          dataType: this.frameworkName,
          reportingPeriod: reportingPeriod,
          memberComment: this.enteredComment || undefined,
        };
      });
    },
    /**
     * Sets state variables
     * @param errorMessage sets error message
     * @param submitted sets submitted state
     * @param submittingSucceeded sets succeeded submit state
     */
    editStateVariables(errorMessage: string, submitted: boolean, submittingSucceeded: boolean): void {
      this.errorMessage = errorMessage;
      this.submitted = submitted;
      this.submittingSucceeded = submittingSucceeded;
    },
    /**
     * Submits the data request to the request service
     */
    async submitRequest(): Promise<void> {
      try {
        const singleRequests = this.collectDataToSend();
        const requestControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).apiClients
          .requestController;
        for (const singleRequest of singleRequests) {
          await requestControllerApi.createRequest(singleRequest);
        }
        this.editStateVariables('', true, true);
      } catch (error) {
        console.error(error);
        if (error instanceof AxiosError) {
          if (error.response?.status == 403) {
            this.openMaxRequestsReachedModal();
          } else {
            const responseMessages = (error.response?.data as ErrorResponse)?.errors;
            this.editStateVariables(responseMessages?.[0]?.message ?? error.message, true, false);
          }
        } else {
          this.editStateVariables(
            'An unexpected error occurred. Please try again or contact the support team if the issue persists.',
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
.single-request-form {
  padding-left: 2rem;
  display: grid;
  place-items: center;
  background-color: var(--input-text-bg);

  > [data-pc-name='card'] {
    width: 50%;
    --p-card-title-font-size: var(--font-size-base);
  }

  > [data-pc-name='card']:first-of-type {
    margin: 2rem 0 1rem;
  }

  > [data-pc-name='card']:last-of-type {
    margin: 1rem 0 2rem;
  }

  .p-card-content > [data-pc-name='inputtext'] {
    width: 100%;
  }
}

.submit-button {
  display: block;
  margin-left: auto;
  margin-right: 25%;
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
