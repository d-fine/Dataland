<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="paper-section no-ui-message">
      <CompanyInfoSheet
        :company-id="companyId"
        @fetched-company-information="handleFetchedCompanyInformation"
        :show-search-bar="false"
      />

      <div class="col-6 mx-auto next-to-each-other">
        <h2>Request Data</h2>
        <PrimeButton
          type="submit"
          label="Submit"
          class="p-button p-button-sm d-letters place-self-center ml-auto"
          name="submit_request_button"
        >
          Submit Data Request
        </PrimeButton>
      </div>

      <div class="col-6 mx-auto">
        <div data-test="selectFramework" class="bg-white radius-1 pt-2 pb-2 pl-5" style="text-align: left">
          <h4 class="p-0">Select the framework for which you want to request data</h4>
          <FormKit
            type="select"
            placeholder="Select framework"
            v-model:selectedItemsBindInternal="selectedFramework"
            name="Framework"
            :options="frameworkOptions"
            outer-class="long"
            data-test="datapoint-framework"
          />
        </div>
      </div>

      <div class="col-6 mx-auto">
        <div data-test="selectReportingPeriod" class="bg-white radius-1 pt-2 pb-2 pl-5" style="text-align: left">
          <h4 class="p-0">Select at least one reporting period</h4>
          <div class="years-container">
            <button
              class="years"
              v-for="year in years"
              :key="year"
              @click="toggleSelection(year)"
              :class="{ selected: selectedYears.includes(year) }"
            >
              {{ year }}
            </button>
          </div>
        </div>
      </div>

      <div class="col-6 mx-auto">
        <div data-test="provideContactDetails" class="bg-white radius-1 pt-2 pb-2 pl-5" style="text-align: left">
          <h4 class="p-0">Provide Contact Details</h4>
          <label for="Email" class="label-with-optional">
            <b>Email</b> <span class="optional-text">Optional</span>
          </label>
          <FormKit type="text" v-model="email" name="Email" />
          <p class="gray-text font-italic" style="text-align: left">
            By specifying a contact person here, your data request will be directed accordingly.<br />
            this increases the chances of expediting the fulfillment of your request.
          </p>
          <p class="gray-text font-italic" style="text-align: left">
            If you don't have a specific contact person, no worries.<br />
            We are committed to fulfilling your request to the best of our ability.
          </p>
          <br />
          <label for="Message" class="label-with-optional">
            <b>Message</b> <span class="optional-text">Optional</span>
          </label>
          <FormKit type="textarea" v-model="message" name="Message" />
          <p class="gray-text font-italic" style="text-align: left">
            Let your contact know what exactly your are looking for.
          </p>
        </div>
      </div>
    </TheContent>
    <TheFooter :is-light-version="true" :sections="footerContent" />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import TheContent from "@/components/generics/TheContent.vue";
import { defineComponent, inject } from "vue";
import TheFooter from "@/components/generics/TheNewFooter.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import { type Content, type Page } from "@/types/ContentTypes";
import contentData from "@/assets/content.json";
import CompanyInfoSheet from "@/components/general/CompanyInfoSheet.vue";
import { type CompanyInformation, DataTypeEnum } from "@clients/backend";
import { type SingleDataRequest } from "@clients/communitymanager";
import PrimeButton from "primevue/button";
import type Keycloak from "keycloak-js";

export default defineComponent({
  name: "SingleDataRequest",
  components: {
    CompanyInfoSheet,
    AuthenticationWrapper,
    TheHeader,
    TheContent,
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
    const years: number[] = [2024, 2023, 2022, 2021, 2020];
    const frameworkOptions: DataTypeEnum[] = Object.values(DataTypeEnum).sort();
    return {
      years,
      selectedYears: [] as number[],
      footerContent,
      fetchedCompanyInformation: {} as CompanyInformation,
      frameworkOptions,
      selectedFramework: DataTypeEnum,
      email: "",
      message: "",
    };
  },
  //TODO: default to be removed
  props: {
    companyId: {
      type: String,
      required: true,
      default: "d9923b5c-8a67-4aad-8112-640af606bccb",
    },
  },
  methods: {
    /**
     * Toggle on the button for the selected year and add it to the list of selected years
     * @param year - the year to be toggled on in the year selection
     */
    toggleSelection(year: number): void {
      const index = this.selectedYears.indexOf(year);

      if (index === -1) {
        this.selectedYears.push(year);
      } else {
        this.selectedYears.splice(index, 1);
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
        companyId: this.companyId,
        selectedFramework: this.selectedFramework,
        yearsRequested: this.selectedYears,
        email: this.email,
        message: this.message,
      };
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
  color: var(--primary-orange);
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
