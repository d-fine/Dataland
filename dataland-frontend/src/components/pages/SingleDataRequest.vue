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
                <BasicFormSection :data-test="'selectFrameworkDiv'" header="Select a framework">
                  <FormKit
                    type="select"
                    placeholder="Select framework"
                    v-model:selectedItemsBindInternal="frameworkName"
                    name="Framework"
                    :options="frameworkOptions"
                    validation="required"
                    :validation-messages="{
                      required: 'Select a framework',
                    }"
                    outer-class="long"
                    data-test="datapoint-framework"
                  />
              </BasicFormSection>
              <BasicFormSection :data-test="'provideContactDetails'" header="Provide Contact Details">
                <label for="Email" class="label-with-optional">
                <b>Email</b><span class="optional-text">Optional</span>
                </label>
                <FormKit v-model="contactList" type="text" name="contactDetails" />
                <p class="gray-text font-italic" style="text-align: left">
                  By specifying a contact person here, your data request will be directed accordingly.<br />
                  this increases the chances of expediting the fulfillment of your request.
                </p>
                <br />
                <p class="gray-text font-italic" style="text-align: left">
                  If you don't have a specific contact person, no worries.<br />
                  We are committed to fulfilling your request to the best of our ability.
                </p>
                <br/>
                <label for="Message" class="label-with-optional">
                  <b>Message</b><span class="optional-text">Optional</span>
                </label>
                <FormKit v-model="message" type="textarea" name="message" />
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
import { type CompanyInformation, DataTypeEnum, type ErrorResponse } from "@clients/backend";
import { type SingleDataRequest } from "@clients/communitymanager";
import PrimeButton from "primevue/button";
import type Keycloak from "keycloak-js";
import { AxiosError } from "axios";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import ToggleChipFormInputs from "@/components/general/ToggleChipFormInputs.vue";
import BasicFormSection from "@/components/general/BasicFormSection.vue";

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
    const frameworkOptions: DataTypeEnum[] = Object.values(DataTypeEnum).sort();
    return {
      singleDataRequestModel: {},
      footerContent,
      fetchedCompanyInformation: {} as CompanyInformation,
      frameworkOptions,
      frameworkName: DataTypeEnum,
      contactList: "",
      message: "",
      selectedReportingPeriodsError: false,
      reportingPeriods: [
        { name: "2023", value: false },
        { name: "2022", value: false },
        { name: "2021", value: false },
        { name: "2020", value: false },
      ],
    };
  },
  computed: {
    selectedReportingPeriods(): string[] {
      return this.reportingPeriods
        .filter((reportingPeriod) => reportingPeriod.value)
        .map((reportingPeriod) => reportingPeriod.name);
    },
  },
  //TODO: default to be removed
  props: {
    companyIdentifier: {
      type: String,
      required: true,
      default: "d9923b5c-8a67-4aad-8112-640af606bccb",
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
     * Toggle on the button for the selected year and add it to the list of selected years
     * @param year - the year to be toggled on in the year selection
     */
    toggleSelection(year: string): void {
      const index = this.listOfReportingPeriods.indexOf(year);

      if (index === -1) {
        this.listOfReportingPeriods.push(year);
      } else {
        this.listOfReportingPeriods.splice(index, 1);
      }
    },
    /**
     * Saves the company information emitted by the CompanyInformation vue components event.
     * @param fetchedCompanyInformation the company information for the current company Id
     */
    handleFetchedCompanyInformation(fetchedCompanyInformation: CompanyInformation) {
      this.fetchedCompanyInformation = fetchedCompanyInformation;
    },
    //TODO: add reporting period validation
    /**
     * Builds a SingleDataRequest object using the currently entered inputs and returns it
     * @returns the SingleDataRequest object
     */
    collectDataToSend(): SingleDataRequest {
      return {
        companyIdentifier: this.companyIdentifier,
        frameworkName: this.frameworkName,
        listOfReportingPeriods: this.selectedReportingPeriods,
        contactList: this.contactList,
        message: this.message,
      };
    },
    /**
     * Submits the data request to the request service
     */
    async submitRequest(): Promise<void> {
      try {
        const singleDataRequestObject = this.collectDataToSend();
        const requestDataControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).apiClients
          .requestController;
        const response = await requestDataControllerApi.postSingleDataRequest(singleDataRequestObject);
        this.message = response.data.message;
      } catch (error) {
        console.error(error);
        if (error instanceof AxiosError) {
          const responseMessages = (error.response?.data as ErrorResponse)?.errors;
          this.message = responseMessages ? responseMessages[0].message : error.message;
        } else {
          this.message =
            "An unexpected error occurred. Please try again or contact the support team if the issue persists.";
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
