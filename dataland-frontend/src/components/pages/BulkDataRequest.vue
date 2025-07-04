<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="no-ui-message">
      <div class="col-12 mb-2 bg-white">
        <div class="text-left company-details px-4">
          <h1 data-test="headerLabel">Bulk Data Request</h1>
        </div>
      </div>

      <div class="col-12">
        <FormKit
          :actions="false"
          v-model="bulkDataRequestModel"
          type="form"
          @submit="submitRequest"
          id="requestDataFormId"
          name="requestDataFormName"
        >
          <div class="grid px-8 py-4 justify-content-center uploadFormWrapper align-items-center">
            <template v-if="submittingInProgress || postBulkDataRequestObjectProcessed">
              <div class="col-12">
                <div class="status text-center">
                  <template v-if="submittingInProgress">
                    <div class="status-wrapper col-8 col-offset-2">
                      <i class="pi pi-spinner pi-spin text-primary text-6xl" aria-hidden="true" />
                    </div>
                  </template>
                  <template v-else>
                    <div class="status text-center col-8 col-offset-2">
                      <div class="status-wrapper">
                        <div v-if="requestSuccessStatus == 'Success'" class="status-container">
                          <em class="material-icons info-icon green-text mr-2">check_circle</em>
                          <h1 class="status-text" data-test="requestStatusText">Success</h1>
                        </div>
                        <div v-if="requestSuccessStatus == 'Partial Success'" class="status-container">
                          <em class="material-icons info-icon info-color-text mr-2">info</em>
                          <h1 class="status-text" data-test="requestStatusText">Partial Success</h1>
                        </div>
                        <div v-if="!isSuccessful || requestSuccessStatus == 'No Success'" class="status-container">
                          <em class="material-icons info-icon red-text mr-2">error</em>
                          <h1 class="status-text" data-test="requestStatusText">Request Unsuccessful</h1>
                        </div>
                      </div>
                      <p class="col-6 col-offset-3 mb-4">{{ message }}</p>

                      <PrimeButton
                        type="button"
                        @click="goToMyRequests()"
                        label="TO MY DATA REQUESTS"
                        class="uppercase p-button-outlined"
                      />
                    </div>
                    <BulkDataRequestSummary
                      class="col-8 col-offset-2"
                      v-if="isSuccessful"
                      :bulk-data-request-response="bulkDataRequestResponse"
                      :humanized-reporting-periods="humanizedReportingPeriods"
                      :summary-section-reporting-periods-heading="summarySectionReportingPeriodsHeading"
                      :humanized-selected-frameworks="humanizedSelectedFrameworks"
                      :summary-section-frameworks-heading="summarySectionFrameworksHeading"
                    />
                  </template>
                </div>
              </div>
            </template>

            <template v-else>
              <div class="col-12 md:col-8 xl:col-6">
                <div class="grid">
                  <div class="col-12">
                    <BasicFormSection :data-test="'reportingPeriodsDiv'" header="Select at least one reporting period">
                      <div class="flex flex-wrap mt-4 py-2">
                        <ToggleChipFormInputs
                          :name="'listOfReportingPeriods'"
                          :options="reportingPeriods"
                          :available-options="reportingPeriods"
                          @changed="selectedReportingPeriodsError = false"
                        />
                      </div>
                      <p
                        v-if="selectedReportingPeriodsError"
                        class="text-danger mt-2"
                        data-test="reportingPeriodErrorMessage"
                      >
                        Select at least one reporting period.
                      </p>
                    </BasicFormSection>

                    <BasicFormSection :data-test="'selectFrameworkDiv'" header="Select at least one framework">
                      <MultiSelectFormFieldBindData
                        name="FrameworkSelection"
                        data-test="selectFrameworkSelect"
                        placeholder="Select framework"
                        :options="availableFrameworks"
                        optionValue="value"
                        optionLabel="label"
                        v-model:selectedItemsBindInternal="selectedFrameworks"
                        innerClass="long"
                      />
                      <FormKit
                        :modelValue="selectedFrameworks.toString()"
                        type="text"
                        name="listOfFrameworkNames"
                        validation="required"
                        validation-label="List of framework names"
                        :validation-messages="{
                          required: 'Select at least one framework',
                        }"
                        :outer-class="{ 'hidden-input': true }"
                      />
                      <div data-test="addedFrameworks" class="radius-1 w-full">
                        <span v-if="!selectedFrameworks.length" class="gray-text no-framework"
                          >No Frameworks added yet</span
                        >
                        <span class="form-list-item" :key="it" v-for="it in selectedFrameworks">
                          {{ humanizeStringOrNumber(it) }}
                          <em @click="removeItem(it)" class="material-icons">close</em>
                        </span>
                      </div>
                    </BasicFormSection>

                    <BasicFormSection :data-test="'notifyMeImmediately'" header="Notify Me Immediately">
                      Receive emails directly or via summary
                      <ToggleSwitch
                        style="display: block; margin: 1rem 0"
                        data-test="notifyMeImmediatelyInput"
                        inputId="notifyMeImmediatelyInput"
                        v-model="notifyMeImmediately"
                      />
                      <label for="notifyMeImmediatelyInput">
                        <strong v-if="notifyMeImmediately">immediate update</strong>
                        <span v-else>weekly summary</span>
                      </label>
                    </BasicFormSection>

                    <BasicFormSection :data-test="'selectIdentifiersDiv'" header="Provide Company Identifiers">
                      <FormKit
                        v-model="identifiersInString"
                        type="textarea"
                        name="listOfCompanyIdentifiers"
                        validation="required"
                        validation-label="List of company identifiers"
                        :validation-messages="{
                          required: 'Provide at least one identifier',
                        }"
                        placeholder="E.g.: DE-000402625-0, SWE402626, DE-000402627-2, SWE402626,DE-0004026244"
                      />
                      <span class="gray-text font-italic">
                        Accepted identifiers: DUNS Number, LEI, ISIN & permID. Expected in comma separated format.
                      </span>
                    </BasicFormSection>
                  </div>
                  <div class="col-12 flex justify-content-end">
                    <PrimeButton
                      type="submit"
                      label="Submit"
                      class="primary-button align-self-end"
                      name="submit_request_button"
                      @click="checkReportingPeriods()"
                    >
                      NEXT
                    </PrimeButton>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </FormKit>
      </div>
    </TheContent>
    <TheFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import MultiSelectFormFieldBindData from '@/components/forms/parts/fields/MultiSelectFormFieldBindData.vue';
import BasicFormSection from '@/components/general/BasicFormSection.vue';
import ToggleChipFormInputs from '@/components/general/ToggleChipFormInputs.vue';
import TheContent from '@/components/generics/TheContent.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import BulkDataRequestSummary from '@/components/pages/BulkDataRequestSummary.vue';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import router from '@/router';
import { ApiClientProvider } from '@/services/ApiClients';
import { SuccessStatus } from '@/types/SuccessStatus.ts';
import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { type DataTypeEnum, type ErrorResponse } from '@clients/backend';
import type { BulkDataRequest, BulkDataRequestDataTypesEnum, BulkDataRequestResponse } from '@clients/communitymanager';
import { FormKit } from '@formkit/vue';
import { AxiosError } from 'axios';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import ToggleSwitch from 'primevue/toggleswitch';
import { defineComponent, inject } from 'vue';

export default defineComponent({
  name: 'BulkDataRequest',
  components: {
    BulkDataRequestSummary,
    MultiSelectFormFieldBindData,
    AuthenticationWrapper,
    ToggleSwitch,
    TheHeader,
    TheContent,
    TheFooter,
    PrimeButton,
    FormKit,
    BasicFormSection,
    ToggleChipFormInputs,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },

  data() {
    return {
      bulkDataRequestModel: {},
      availableFrameworks: [] as { value: DataTypeEnum; label: string }[],
      selectedFrameworks: [] as Array<DataTypeEnum>,
      identifiersInString: '',
      notifyMeImmediately: false,
      identifiers: [] as Array<string>,
      bulkDataRequestResponse: undefined as BulkDataRequestResponse | undefined,
      requestSuccessStatus: {},
      isSuccessful: false,
      submittingInProgress: false,
      postBulkDataRequestObjectProcessed: false,
      message: '',
      selectedReportingPeriodsError: false,
      reportingPeriods: [
        { name: '2024', value: false },
        { name: '2023', value: false },
        { name: '2022', value: false },
        { name: '2021', value: false },
        { name: '2020', value: false },
      ],
    };
  },

  computed: {
    humanizedSelectedFrameworks(): string[] {
      return this.selectedFrameworks.map((it) => humanizeStringOrNumber(it));
    },
    selectedReportingPeriods(): string[] {
      return this.reportingPeriods
        .filter((reportingPeriod) => reportingPeriod.value)
        .map((reportingPeriod) => reportingPeriod.name);
    },
    humanizedReportingPeriods(): string {
      return this.selectedReportingPeriods.join(', ');
    },
    summarySectionReportingPeriodsHeading(): string {
      const len = this.reportingPeriods.filter((reportingPeriod) => reportingPeriod.value).length;
      return `${len} REPORTING PERIOD${len > 1 ? 'S' : ''}`;
    },
    summarySectionFrameworksHeading(): string {
      const len = this.selectedFrameworks.length;
      return `${len} FRAMEWORK${len > 1 ? 'S' : ''}`;
    },
  },

  methods: {
    humanizeStringOrNumber,
    /**
     * Check whether reporting periods have been selected
     */
    checkReportingPeriods(): void {
      if (!this.selectedReportingPeriods.length) {
        this.selectedReportingPeriodsError = true;
      }
    },
    /**
     * Remove framework from selected frameworks from array
     * @param it - framework to remove
     */
    removeItem(it: string) {
      this.selectedFrameworks = this.selectedFrameworks.filter((el) => el !== it);
    },
    /**
     * Builds a DataRequest object using the currently entered inputs and returns it
     * @returns the DataRequest object
     */
    collectDataToSend(): BulkDataRequest {
      return {
        // as unknown as Set<string> cast required to ensure proper json is created
        reportingPeriods: this.selectedReportingPeriods as unknown as Set<string>,
        companyIdentifiers: this.identifiers as unknown as Set<string>,
        dataTypes: this.selectedFrameworks as unknown as Set<BulkDataRequestDataTypesEnum>,
        notifyMeImmediately: this.notifyMeImmediately,
      };
    },
    /**
     * Converts the string inside the identifiers field into a list of identifiers
     */
    processInput() {
      const uniqueIdentifiers = new Set(this.identifiersInString.replace(/(\r\n|\n|\r|;| )/gm, ',').split(','));
      uniqueIdentifiers.delete('');
      this.identifiers = [...uniqueIdentifiers];
      this.identifiersInString = this.identifiers.join(', ');
    },

    /**
     * Submits the data request to the request service
     */
    async submitRequest(): Promise<void> {
      this.processInput();
      this.submittingInProgress = true;

      try {
        const bulkDataRequestObject = this.collectDataToSend();
        const requestDataControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).apiClients
          .requestController;
        const response = await requestDataControllerApi.postBulkDataRequest(bulkDataRequestObject);
        this.bulkDataRequestResponse = response.data;
        this.calculateRequestSuccessStatus(this.bulkDataRequestResponse);
        this.composeSummaryMessage();
        this.isSuccessful = true;
      } catch (error) {
        console.error(error);
        if (error instanceof AxiosError) {
          const responseMessages = (error.response?.data as ErrorResponse)?.errors;
          this.message = responseMessages ? responseMessages[0].message : error.message;
        } else {
          this.message =
            'An unexpected error occurred. Please try again or contact the support team if the issue persists.';
        }
      } finally {
        this.submittingInProgress = false;
        this.postBulkDataRequestObjectProcessed = true;
      }
    },

    /**
     * Composes the summary message in case the bulkDataRequestResponse contains meaningful data.
     */
    composeSummaryMessage() {
      if (!this.bulkDataRequestResponse) {
        return;
      }
      const numberOfAccepted = this.bulkDataRequestResponse.acceptedDataRequests.length;
      const numberOfExisting =
        this.bulkDataRequestResponse.alreadyExistingDatasets.length +
        this.bulkDataRequestResponse.alreadyExistingNonFinalRequests.length;
      const numberOfRejected = this.bulkDataRequestResponse.rejectedCompanyIdentifiers.length;

      this.message =
        `${numberOfAccepted} data requests were created. ${numberOfExisting} data requests were skipped. ` +
        `${numberOfRejected} out of ${this.identifiers.length} provided company identifiers could not be recognized and were rejected. ` +
        'More details can be found in the summary below.';
    },

    /**
     * Calculate the SuccessStatus of the BulkDataRequest.
     * If no requests rejected -> Success
     * If some but not all rejected -> Partial Success
     * Else -> No Success
     */
    calculateRequestSuccessStatus(bulkDataRequestResponse: BulkDataRequestResponse) {
      const sumOfAllRequestedData =
        bulkDataRequestResponse.acceptedDataRequests.length +
        bulkDataRequestResponse.alreadyExistingNonFinalRequests.length +
        bulkDataRequestResponse.alreadyExistingDatasets.length +
        bulkDataRequestResponse.rejectedCompanyIdentifiers.length;

      if (bulkDataRequestResponse.rejectedCompanyIdentifiers.length === 0) {
        this.requestSuccessStatus = SuccessStatus.Success;
      } else if (bulkDataRequestResponse.rejectedCompanyIdentifiers.length < sumOfAllRequestedData) {
        this.requestSuccessStatus = SuccessStatus.PartialSuccess;
      } else {
        this.requestSuccessStatus = SuccessStatus.NoSuccess;
      }
    },

    /**
     * Populates the availableFrameworks property in the format expected by the dropdown filter
     */
    retrieveAvailableFrameworks() {
      this.availableFrameworks = FRAMEWORKS_WITH_VIEW_PAGE.map((dataTypeEnum: DataTypeEnum) => {
        return {
          value: dataTypeEnum,
          label: humanizeStringOrNumber(dataTypeEnum),
        };
      });
    },

    /**
     * Go to RequestedDatasetsPage
     */
    goToMyRequests() {
      void router.push('/requests');
    },
  },
  mounted() {
    this.retrieveAvailableFrameworks();
  },
});
</script>

<style scoped lang="scss">
.uploadFormWrapper {
  min-height: calc(100vh - 200px);

  .status {
    .status-text {
      font-weight: 700;
      font-size: 48px;
      line-height: 48px;
      letter-spacing: 0.25px;
    }

    .info-icon {
      font-size: 48px;
    }
  }

  div.summary-section {
    &.border-bottom {
      border-bottom: 1px solid #dadada;
    }

    .summary-section-heading {
      font-weight: 500;
      font-size: 16px;
      line-height: 20px;

      .info-icon {
        margin-bottom: -2px;
        vertical-align: bottom;
      }
    }

    .summary-section-data {
      font-weight: 700;

      .identifier {
        font-weight: 400;
        font-size: 16px;
        line-height: 26px;
        letter-spacing: 0.44px;
      }
    }
  }
}

.status-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
}

.status-container {
  display: flex;
  align-items: center;
}

.info-color-text {
  color: var(--p-primary-color);
}

.no-framework {
  display: flex;
  justify-content: center;
  align-items: center;
}

.paper-section {
  background-color: var(--paper-white);
}

.radius-1 {
  border-radius: var(--p-border-radius-xs);
}

.text-primary {
  color: var(--main-color);
}

.text-danger {
  color: var(--fk-color-error);
  font-size: var(--font-size-xs);
}

.bg-white {
  background-color: var(--default-neutral-white);
}

.gray-text {
  color: var(--gray);
}

.red-text {
  color: var(--red);
}

.green-text {
  color: var(--green);
}
</style>
