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
                    <BasicFormSection :data-test="'selectFramework'" header="Select a framework">
                      <FormKit
                        type="select"
                        placeholder="Select framework"
                        v-model="frameworkName"
                        name="Framework"
                        :options="frameworkOptions"
                        validation="required"
                        :validation-messages="{
                          required: 'Select a framework',
                        }"
                        outer-class="long"
                        :data-test="'datapoint-framework'"
                      />
                    </BasicFormSection>
                    <BasicFormSection header="Provide Contact Details">
                      <label for="Email" class="label-with-optional">
                        <b>Email</b><span class="optional-text">Optional</span>
                      </label>
                      <FormKit v-model="contact" type="text" name="contactDetails" data-test="contactEmail" />
                      <p class="gray-text font-italic" style="text-align: left">
                        By specifying a contact person here, your data request will be directed accordingly.<br />
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
                      />
                      <p class="gray-text font-italic" style="text-align: left">
                        Let your contact know what exactly your are looking for.
                      </p>
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
import type { CompanyInformation, DataTypeEnum, ErrorResponse } from "@clients/backend";
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

export default defineComponent({
  name: "SingleDataRequest",
  components: {
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
    return {
      singleDataRequestModel: {},
      footerContent,
      fetchedCompanyInformation: {} as CompanyInformation,
      frameworkOptions: [] as { value: DataTypeEnum; label: string }[],
      frameworkName: this.$route.query.preSelectedFramework as DataTypeEnum,
      contact: "",
      dataRequesterMessage: "",
      errorMessage: "",
      selectedReportingPeriodsError: false,
      reportingPeriods: [
        { name: "2023", value: false },
        { name: "2022", value: false },
        { name: "2021", value: false },
        { name: "2020", value: false },
      ],
      submittingSucceeded: false,
      submitted: false,
    };
  },
  computed: {
    selectedReportingPeriods(): string[] {
      return this.reportingPeriods
        .filter((reportingPeriod) => reportingPeriod.value)
        .map((reportingPeriod) => reportingPeriod.name);
    },
    companyIdentifier(): string {
      return this.$route.params.companyId as string;
    },
  },
  methods: {
    /**
     * Check whether reporting periods have been selected
     */
    checkReportingPeriods(): void {
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
      const contactAsList = this.contact ? [this.contact] : undefined;
      return {
        companyIdentifier: this.companyIdentifier,
        frameworkName: this.frameworkName,
        reportingPeriods: this.selectedReportingPeriods,
        contacts: contactAsList,
        message: this.dataRequesterMessage,
      };
    },
    /**
     * Submits the data request to the request service
     */
    async submitRequest(): Promise<void> {
      if (!this.selectedReportingPeriodsError) {
        try {
          const singleDataRequestObject = this.collectDataToSend();
          const requestDataControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).apiClients
            .requestController;
          const response = await requestDataControllerApi.postSingleDataRequest(singleDataRequestObject);
          this.errorMessage = response.statusText;
          this.submittingSucceeded = true;
        } catch (error) {
          console.error(error);
          if (error instanceof AxiosError) {
            const responseMessages = (error.response?.data as ErrorResponse)?.errors;
            this.errorMessage = responseMessages ? responseMessages[0].message : error.message;
          } else {
            this.errorMessage =
              "An unexpected error occurred. Please try again or contact the support team if the issue persists.";
          }
        }
        this.submitted = true;
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
