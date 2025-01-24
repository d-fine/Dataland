<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="paper-section no-ui-message">
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
          <div class="px-8 py-4 justify-content-center uploadFormWrapper">
            <template v-if="submittingInProgress || postBulkDataRequestObjectProcessed">
              <template v-if="submittingInProgress">
                <div class="status-wrapper">
                  <i class="pi pi-spinner pi-spin text-primary text-6xl" aria-hidden="true" />
                </div>
              </template>
              <template v-else>
                <div>
                  <div class="status text-center">
                    <div class="status-wrapper">
                      <div v-if="submittingSucceeded" class="status-container">
                        <em class="col material-icons info-icon green-text">check_circle</em>
                        <h1 class="col status-text" data-test="requestStatusText">Success</h1>
                      </div>
                      <div v-else class="status-container">
                        <em class="material-icons info-icon red-text">error</em>
                        <h1 class="status-text" data-test="requestStatusText">Request Unsuccessful</h1>
                      </div>
                    </div>
                    <div class="col-4 col-offset-4">
                      {{ rejectedCompanyIdentifiers.length }} out of {{ identifiers.length }} provided company
                      identifiers could not be recognized and were rejected. {{ createdRequests.length }} data requests
                      were created and {{ notCreatedRequests.length }} skipped. More details can be found in the summary
                      below.
                    </div>

                    <p v-if="message" class="py-3">{{ message }}</p>

                    <PrimeButton
                      type="button"
                      @click="goToMyRequests()"
                      label="TO MY DATA REQUESTS"
                      class="uppercase p-button-outlined"
                    />
                  </div>
                </div>

                <div class="col-8 col-offset-2 bg-white mt-4">
                  <h1 class="middle-center-div">Data Request Summary</h1>
                  <div class="summary-section border-bottom py-4">
                    <div class="summary-wrapper">
                      <div class="grid col-6">
                        <div class="col">
                          <h6 class="middle-center-div summary-section-heading m-0">
                            {{ summarySectionReportingPeriodsHeading }}
                          </h6>
                          <p class="middle-center-div summary-section-data m-0 mt-3">{{ humanizedReportingPeriods }}</p>
                        </div>
                        <div class="col">
                          <h6 class="middle-center-div summary-section-heading m-0">
                            {{ summarySectionFrameworksHeading }}
                          </h6>
                          <p class="middle-center-div summary-section-data m-0 mt-3">
                            {{ humanizedSelectedFrameworks.join(', ') }}
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="summary-section border-bottom py-4">
                    <Accordion>
                      <AccordionTab>
                        <template #header>
                          <span class="flex align-items-center gap-2 w-full">
                            <em class="material-icons info-icon green-text">check_circle</em>
                            <span class="summary-section-heading">CREATED REQUESTS</span>
                            <Badge :value="createdRequests.length" class="ml-auto mr-2" />
                          </span>
                        </template>
                        <template v-for="entry in createdRequests" :key="entry">
                          <div class="grid-container align-items-center">
                            <div class="col bold-text middle-center-div">{{ entry.companyIdentifier }}</div>
                            <div class="col bold-text middle-center-div">{{ entry.companyName }}</div>
                            <div class="col bold-text middle-center-div">{{ entry.reportingPeriod }}</div>
                            <div class="col bold-text middle-center-div">{{ entry.framework }}</div>
                          </div>
                        </template>
                      </AccordionTab>
                    </Accordion>
                  </div>
                  <div class="summary-section border-bottom py-4">
                    <Accordion :active-index="0">
                      <AccordionTab>
                        <template #header>
                          <span class="flex align-items-center gap-2 w-full">
                            <em class="material-icons info-icon new-color">info</em>
                            <span class="summary-section-heading">SKIPPED REQUESTS (data already exists)</span>
                            <Badge :value="notCreatedRequests.length" class="ml-auto mr-2" />
                          </span>
                        </template>
                        <div class="text-center bg-gray-300 py-1 mt-1 mb-3">
                          If you believe that a dataset is incomplete or deprecated, you can still request it by submitting a single
                          data request on the corresponding dataset page.
                        </div>
                        <template v-for="entry in notCreatedRequests" :key="entry">
                          <div class="grid-container align-items-center">
                            <div class="col bold-text middle-center-div">{{ entry.companyIdentifier }}</div>
                            <div class="col bold-text middle-center-div">{{ entry.companyName }}</div>
                            <div class="col bold-text middle-center-div">{{ entry.reportingPeriod }}</div>
                            <div class="col bold-text middle-center-div">{{ entry.framework }}</div>
                            <a :href="entry.url" target="_blank" class="col bold-text new-color">View Data</a>
                          </div>
                        </template>
                      </AccordionTab>
                    </Accordion>
                  </div>
                  <div class="summary-section border-bottom py-4">
                    <Accordion>
                      <AccordionTab>
                        <template #header>
                          <span class="flex align-items-center gap-2 w-full">
                            <em class="material-icons info-icon red-text">error</em>
                            <span class="summary-section-heading">REJECTED IDENTIFIERS</span>
                            <Badge :value="rejectedCompanyIdentifiers.length" class="ml-auto mr-2" />
                          </span>
                        </template>
                        <template v-for="entry in rejectedCompanyIdentifiers" :key="entry">
                          <div class="grid-container align-items-center">
                            <div class="col bold-text middle-center-div">{{ entry }}</div>
                          </div>
                        </template>
                      </AccordionTab>
                    </Accordion>
                  </div>
                </div>
              </template>
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
                          @changed="selectedReportingPeriodsError = false"
                        />
                      </div>
                      <p
                        v-if="selectedReportingPeriodsError"
                        class="text-danger text-xs mt-2"
                        data-test="reportingPeriodErrorMessage"
                      >
                        Select at least one reporting period.
                      </p>
                    </BasicFormSection>

                    <BasicFormSection :data-test="selectFrameworkDiv" header="Select at least one framework">
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
                        :modelValue="selectedFrameworks"
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
                  <div class="col-12 flex align-items-end">
                    <PrimeButton
                      type="submit"
                      label="Submit"
                      class="p-button p-button-sm d-letters ml-auto"
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
    <TheFooter :is-light-version="true" :sections="footerContent" />
  </AuthenticationWrapper>
</template>

<script lang="ts">
// @ts-nocheck
import { FormKit } from '@formkit/vue';
import PrimeButton from 'primevue/button';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Badge from 'primevue/badge';
import { defineComponent, inject } from 'vue';
import type Keycloak from 'keycloak-js';
import { type DataTypeEnum, type ErrorResponse } from '@clients/backend';
import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import TheContent from '@/components/generics/TheContent.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import TheFooter from '@/components/generics/TheNewFooter.vue';
import contentData from '@/assets/content.json';
import type { Content, Page } from '@/types/ContentTypes';
import MultiSelectFormFieldBindData from '@/components/forms/parts/fields/MultiSelectFormFieldBindData.vue';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { ApiClientProvider } from '@/services/ApiClients';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { AxiosError } from 'axios';
import BasicFormSection from '@/components/general/BasicFormSection.vue';
import ToggleChipFormInputs from '@/components/general/ToggleChipFormInputs.vue';
import { type BulkDataRequest, type BulkDataRequestDataTypesEnum } from '@clients/communitymanager';
import router from '@/router';
import { type ExistingDataResponse } from '@/utils/RequestUtils.ts';

export default defineComponent({
  name: 'BulkDataRequest',
  components: {
    Accordion,
    AccordionTab,
    Badge,
    MultiSelectFormFieldBindData,
    AuthenticationWrapper,
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
    const content: Content = contentData;
    const footerPage: Page | undefined = content.pages.find((page) => page.url === '/');
    const footerContent = footerPage?.sections;
    return {
      bulkDataRequestModel: {},
      availableFrameworks: [] as { value: DataTypeEnum; label: string }[],
      selectedFrameworks: ['lksg', 'sfdr', 'eutaxonomy-non-financials'] as Array<DataTypeEnum>,
      identifiersInString: '',
      identifiers: [] as Array<string>,
      acceptedCompanyIdentifiers: ['Test123', 'AnotherTest'] as Array<string>,
      rejectedCompanyIdentifiers: ['Rejected123', 'AnotherReject'] as Array<string>,
      createdRequests: [
        {
          companyIdentifier: '1',
          companyName: 'VW',
          reportingPeriod: '2023',
          framework: 'sfdr',
          url: 'https://dataland.com',
        },
        {
          companyIdentifier: '2',
          companyName: 'Siemens',
          reportingPeriod: '2025',
          framework: 'lksg',
          url: 'https://dataland.com',
        },
        {
          companyIdentifier: '3',
          companyName: 'BASF',
          reportingPeriod: '2019',
          framework: 'eu-taxonomy',
          url: 'https://dataland.com',
        },
        {
          companyIdentifier: '4',
          companyName: 'Bayer',
          reportingPeriod: '2018',
          framework: 'eu-taxonomy',
          url: 'https://dataland.com',
        },
      ] as Array<ExistingDataResponse>,
      notCreatedRequests: [
        {
          companyIdentifier: '123',
          companyName: 'Adidas',
          reportingPeriod: '2024',
          framework: 'sfdr',
          url: 'https://dataland.com',
        },
        {
          companyIdentifier: '456',
          companyName: 'Daimler',
          reportingPeriod: '2022',
          framework: 'lksg',
          url: 'https://dataland.com',
        },
        {
          companyIdentifier: '789',
          companyName: 'Deutsche Bank',
          reportingPeriod: '2021',
          framework: 'eu-taxonomy',
          url: 'https://dataland.com',
        },
      ] as Array<ExistingDataResponse>,
      submittingSucceeded: true,
      submittingInProgress: false,
      postBulkDataRequestObjectProcessed: true,
      message: '',
      footerContent,
      selectedReportingPeriodsError: false,
      reportingPeriods: [
        { name: '2024', value: true },
        { name: '2023', value: false },
        { name: '2022', value: false },
        { name: '2021', value: true },
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
     * Creates section title for identifiers
     * @param items string array to calculate size and proper grammar
     * @param statusText optional text identifying the status of the heading
     * @returns a formatted heading
     */
    summarySectionIdentifiersHeading(items: string[], statusText = ''): string {
      const numberOfItems = items.length;
      const messageSegments = [items.length, statusText, `IDENTIFIER${numberOfItems > 1 ? 'S' : ''}`];
      return messageSegments.filter((segment) => !!segment).join(' ');
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

        this.message = response.data.message;
        this.rejectedCompanyIdentifiers = response.data.rejectedCompanyIdentifiers;
        this.acceptedCompanyIdentifiers = response.data.acceptedCompanyIdentifiers;
        this.submittingSucceeded = this.acceptedCompanyIdentifiers.length > 0;
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

.summary-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px; /* Adjust the gap as needed */
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

.new-color {
  color: $orange-prime;
}

.bold-text {
  font-weight: bold;
}

.no-framework {
  display: flex;
  justify-content: center;
  align-items: center;
}

.grid-container {
  display: grid;
  grid-template-columns: 2fr 4fr 1fr 2fr 1fr;
  gap: 1px;
}
</style>
