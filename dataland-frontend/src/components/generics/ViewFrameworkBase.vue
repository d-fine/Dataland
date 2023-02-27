<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="paper-section min-h-screen">
      <MarginWrapper class="text-left surface-0" style="margin-right: 0">
        <BackButton />
        <FrameworkDataSearchBar class="mt-2" ref="frameworkDataSearchBar" @search-confirmed="handleSearchConfirm" />
      </MarginWrapper>
      <MarginWrapper class="surface-0" style="margin-right: 0">
        <div class="grid align-items-end">
          <div class="col-9">
            <CompanyInformation :companyID="companyID" />
          </div>
        </div>
      </MarginWrapper>
      <MarginWrapper v-if="isDataProcessedSuccesfully" class="text-left surface-0" style="margin-right: 0">
        <Dropdown
          id="chooseFrameworkDropdown"
          v-model="chosenDataTypeInDropdown"
          :options="dataTypesInDropdown"
          optionLabel="label"
          optionValue="value"
          :placeholder="humanizeString(dataType)"
          aria-label="Choose framework"
          class="fill-dropdown always-fill"
          dropdownIcon="pi pi-angle-down"
          @change="redirectToViewPageForChosenFramework"
        />
        <slot name="reportingPeriodDropdown"> </slot>
      </MarginWrapper>
      <MarginWrapper v-if="isDataProcessedSuccesfully" style="margin-right: 0">
        <slot name="content"> </slot>
      </MarginWrapper>
      <h1 v-else>No data could be loaded.</h1>
    </TheContent>
    <DatalandFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import FrameworkDataSearchBar from "@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import BackButton from "@/components/general/BackButton.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import { ApiClientProvider } from "@/services/ApiClients";
import { defineComponent, inject, ref } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import Dropdown from "primevue/dropdown";
import { humanizeString } from "@/utils/StringHumanizer";
import { ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS } from "@/utils/Constants";
import DatalandFooter from "@/components/general/DatalandFooter.vue";
import { DataMetaInformation } from "@clients/backend";

export default defineComponent({
  name: "ViewFrameworkBase",
  components: {
    TheContent,
    TheHeader,
    BackButton,
    MarginWrapper,
    FrameworkDataSearchBar,
    Dropdown,
    AuthenticationWrapper,
    CompanyInformation,
    DatalandFooter,
  },
  emits: ["updateAvailableReportingPeriodsForChosenFramework", "updateActiveDataMetaInfoForChosenFramework"],
  props: {
    companyID: {
      type: String,
    },
    dataType: {
      type: String,
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      frameworkDataSearchBar: ref<typeof FrameworkDataSearchBar>(),
    };
  },
  data() {
    return {
      chosenDataTypeInDropdown: "",
      dataTypesInDropdown: [] as { label: string; value: string }[],
      humanizeString: humanizeString,
      windowScrollHandler: (): void => {
        this.handleScroll();
      },
      isDataProcessedSuccesfully: true,
    };
  },
  created() {
    void this.getDropdownOptionsAndActiveDataMetaInfoAndDoEmits();
    window.addEventListener("scroll", this.windowScrollHandler);
  },
  methods: {
    /**
     * Hides the dropdown of the Autocomplete-component
     */
    handleScroll() {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
      this.frameworkDataSearchBar?.$refs.autocomplete.hide();
    },
    /**
     * Visits the framework view page for the framework which was chosen in the dropdown
     */
    redirectToViewPageForChosenFramework() {
      void this.$router.push(`/companies/${this.companyID as string}/frameworks/${this.chosenDataTypeInDropdown}`);
    },
    /**
     * Handles the "search-confirmed" event of the search bar by visiting the search page with the query param set to
     * the search term provided by the event.
     *
     * @param searchTerm The search term provided by the "search-confirmed" event of the search bar
     */
    async handleSearchConfirm(searchTerm: string) {
      await this.$router.push({
        name: "Search Companies for Framework Data",
        query: { input: searchTerm },
      });
    },

    /**
     * Uses a list of data meta info to derive all distinct frameworks that occur in that list. Only if those distinct
     * frameworks are also included in the frontend constant which contains all frameworks that have view-pages
     * implemented, the distinct frameworks are set as options for the framework-dropdown element.
     *
     * @param listOfDataMetaInfo a list of data meta info
     */
    getDistinctAvailableFrameworksAndPutThemIntoDropdown(listOfDataMetaInfo: DataMetaInformation[]) {
      const setOfAvailableFrameworksForCompany = [
        ...new Set(listOfDataMetaInfo.map((dataMetaInfo) => dataMetaInfo.dataType)),
      ];
      setOfAvailableFrameworksForCompany.forEach((dataType) => {
        if (ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS.includes(dataType)) {
          this.dataTypesInDropdown.push({ label: humanizeString(dataType), value: dataType });
        }
      });
    },

    /**
     * Uses a list of data meta info and filters out all elements whose data type (framework) do not equal the
     * dataType-prop set for this Vue-component during render.
     *
     * @param listOfDataMetaInfo a list of data meta info
     * @returns the filtered list of data meta info
     */
    filterListOfDataMetaInfoForChosenFrameworkAndReturnIt(
      listOfDataMetaInfo: DataMetaInformation[]
    ): DataMetaInformation[] {
      return listOfDataMetaInfo.filter((dataMetaInfo) => dataMetaInfo.dataType === this.dataType);
    },

    /**
     * Uses a list of data meta info to copy all distinct reporting periods that occur in that list to a new list
     * which is then returned.
     *
     * @param listOfDataMetaInfo a list of data meta info
     * @returns a list of strings which contains all distinct reporting periods
     */
    getDistinctReportingPeriodsInListOfDataMetaInfoAndReturnIt(listOfDataMetaInfo: DataMetaInformation[]): string[] {
      return [...new Set(listOfDataMetaInfo.map((dataMetaInfo) => dataMetaInfo.reportingPeriod))];
    },

    /**
     * Goes through all data meta info for the currently viewed company and does multiple things.
     * First it sets the distinct frameworks as options in the framework-dropdown.
     * Then it emits a list of all distinct reporting periods.
     * Finally it emits a list with data meta info elements for all active datasets for this company.
     */
    async getDropdownOptionsAndActiveDataMetaInfoAndDoEmits() {
      try {
        const metaDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getMetaDataControllerApi();
        const apiResponse = await metaDataControllerApi.getListOfDataMetaInfo(this.companyID);
        const listOfActiveDataMetaInfoPerFrameworkAndReportingPeriod = apiResponse.data;
        this.getDistinctAvailableFrameworksAndPutThemIntoDropdown(
          listOfActiveDataMetaInfoPerFrameworkAndReportingPeriod
        );

        const listOfActiveDataMetaInfoPerReportingPeriodForChosenFramework =
          this.filterListOfDataMetaInfoForChosenFrameworkAndReturnIt(
            listOfActiveDataMetaInfoPerFrameworkAndReportingPeriod
          );
        this.$emit(
          "updateAvailableReportingPeriodsForChosenFramework",
          this.getDistinctReportingPeriodsInListOfDataMetaInfoAndReturnIt(
            listOfActiveDataMetaInfoPerReportingPeriodForChosenFramework
          )
        );
        this.$emit(
          "updateActiveDataMetaInfoForChosenFramework",
          listOfActiveDataMetaInfoPerReportingPeriodForChosenFramework
        );
      } catch (error) {
        this.isDataProcessedSuccesfully = false;
        console.error(error);
      }
    },
  },
  watch: {
    companyID() {
      void this.getDropdownOptionsAndActiveDataMetaInfoAndDoEmits();
    },
  },
});
</script>
