<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="paper-section min-h-screen">
      <MarginWrapper class="text-left surface-0" style="margin-right: 0">
        <BackButton />
        <FrameworkDataSearchBar
          :companyIdIfOnViewPage="companyID"
          class="mt-2"
          ref="frameworkDataSearchBar"
          @search-confirmed="handleSearchConfirm"
        />
      </MarginWrapper>
      <MarginWrapper class="surface-0" style="margin-right: 0">
        <div class="grid align-items-end">
          <div class="col-9">
            <CompanyInformation :companyID="companyID" />
          </div>
        </div>
      </MarginWrapper>
      <MarginWrapper v-if="isDataProcessedSuccesfully" class="text-left surface-0" style="margin-right: 0">
        <div class="flex justify-content-between align-items-center d-search-filters-panel">
          <div class="flex">
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
              @change="handleChangeFrameworkEvent"
            />
            <slot name="reportingPeriodDropdown"> </slot>
          </div>
          <div v-if="hasUserUploaderRights" class="flex align-content-end align-items-center">
            <PrimeButton
              v-if="canEdit"
              class="uppercase p-button-outlined p-button p-button-sm d-letters mr-3"
              aria-label="EDIT"
              @click="editDataset"
            >
              <span class="material-icons-outlined px-2">mode</span>
              <span class="px-2">EDIT</span>
            </PrimeButton>
            <PrimeButton class="uppercase p-button-sm d-letters mr-3" aria-label="New Dataset" @click="gotoNewDataset">
              <span class="material-icons-outlined px-2">queue</span>
              <span class="px-2">NEW DATASET</span>
            </PrimeButton>
          </div>
        </div>
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
import PrimeButton from "primevue/button";
import { ApiClientProvider } from "@/services/ApiClients";
import { defineComponent, inject, ref } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import Dropdown, { DropdownChangeEvent } from "primevue/dropdown";
import { humanizeString } from "@/utils/StringHumanizer";
import { ARRAY_OF_FRAMEWORKS_WITH_UPLOAD_FORM, ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE } from "@/utils/Constants";
import DatalandFooter from "@/components/general/DatalandFooter.vue";
import { DataMetaInformation, DataTypeEnum } from "@clients/backend";
import { checkIfUserHasUploaderRights } from "@/utils/KeycloakUtils";
import SelectReportingPeriodDialog from "@/components/general/SelectReportingPeriodDialog.vue";

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
    PrimeButton,
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
      mapOfReportingPeriodToActiveDataset: {} as Map<string, DataMetaInformation>,
      isDataProcessedSuccesfully: true,
      hasUserUploaderRights: null as null | boolean,
    };
  },
  computed: {
    canEdit() {
      return ARRAY_OF_FRAMEWORKS_WITH_UPLOAD_FORM.includes(this.dataType as DataTypeEnum);
    },
  },
  created() {
    this.chosenDataTypeInDropdown = this.dataType ?? "";
    void this.getDropdownOptionsAndActiveDataMetaInfoAndDoEmits();
    checkIfUserHasUploaderRights(this.getKeycloakPromise)
      .then((hasUserUploaderRights) => {
        this.hasUserUploaderRights = hasUserUploaderRights;
      })
      .catch((error) => console.log(error));
    window.addEventListener("scroll", this.windowScrollHandler);
  },
  methods: {
    /**
     * Opens a modal for selecting a reporting period to edit data for
     */
    editDataset() {
      this.$dialog.open(SelectReportingPeriodDialog, {
        props: {
          header: "Select reporting period to edit data for",
          modal: true,
          dismissableMask: true,
        },
        data: {
          mapOfReportingPeriodToActiveDataset: this.mapOfReportingPeriodToActiveDataset,
        },
      });
    },
    /**
     * Navigates to the framework upload page for the current company (a framework is not pre-selected)
     */
    gotoNewDataset() {
      void this.$router.push(`/companies/${assertDefined(this.companyID)}/frameworks/upload`);
    },
    /**
     * Hides the dropdown of the Autocomplete-component
     */
    handleScroll() {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
      this.frameworkDataSearchBar?.$refs.autocomplete.hide();
    },
    /**
     * Visits the framework view page for the framework which was chosen in the dropdown
     *
     * @param dropDownChangeEvent the change event emitted by the dropdown component
     */
    handleChangeFrameworkEvent(dropDownChangeEvent: DropdownChangeEvent) {
      if (this.dataType != dropDownChangeEvent.value) {
        void this.$router.push(`/companies/${this.companyID as string}/frameworks/${this.chosenDataTypeInDropdown}`);
      }
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
    getDistinctAvailableFrameworksAndPutThemSortedIntoDropdown(listOfDataMetaInfo: DataMetaInformation[]) {
      this.dataTypesInDropdown = [];
      const setOfAvailableFrameworksForCompany = [
        ...new Set(listOfDataMetaInfo.map((dataMetaInfo) => dataMetaInfo.dataType)),
      ];
      const listOfDistinctAvailableAndViewableFrameworksForCompany: string[] = [];
      setOfAvailableFrameworksForCompany.forEach((dataType) => {
        if (ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE.includes(dataType)) {
          listOfDistinctAvailableAndViewableFrameworksForCompany.push(dataType);
        }
      });
      listOfDistinctAvailableAndViewableFrameworksForCompany.sort().forEach((dataType) => {
        this.dataTypesInDropdown.push({ label: humanizeString(dataType), value: dataType });
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
     * Goes through all data meta info for the currently viewed company and does multiple things.
     * First it sets the distinct frameworks as options in the framework-dropdown.
     * Then it emits a list of all distinct reporting periods. TODO adjust to changes
     * Finally it emits a list with data meta info elements for all active datasets for this company. TODO
     */
    async getDropdownOptionsAndActiveDataMetaInfoAndDoEmits() {
      try {
        const metaDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getMetaDataControllerApi();
        const apiResponse = await metaDataControllerApi.getListOfDataMetaInfo(this.companyID);
        const listOfActiveDataMetaInfoPerFrameworkAndReportingPeriod = apiResponse.data;

        this.getDistinctAvailableFrameworksAndPutThemSortedIntoDropdown(
          listOfActiveDataMetaInfoPerFrameworkAndReportingPeriod
        );

        // TODO modularize following code block
        this.mapOfReportingPeriodToActiveDataset = new Map<string, DataMetaInformation>();
        const listOfDistinctAvailableReportingPeriodsForFramework: string[] = [];
        listOfActiveDataMetaInfoPerFrameworkAndReportingPeriod.forEach((dataMetaInfo: DataMetaInformation) => {
          if (dataMetaInfo.dataType === this.dataType) {
            if (dataMetaInfo.currentlyActive) {
              this.mapOfReportingPeriodToActiveDataset.set(dataMetaInfo.reportingPeriod, dataMetaInfo); // TODO the fact that backend only sends one meta info per distinct reportingPeriod is assured implicitly by using reporting period as key here for the map.. is this ok??
              listOfDistinctAvailableReportingPeriodsForFramework.push(dataMetaInfo.reportingPeriod);
            } else {
              throw TypeError("Received inactive dataset meta info from Dataland Backend"); // TODO do we even need a check like this, or is this handled as "assured"
            }
          }
        });
        this.$emit("updateActiveDataMetaInfoForChosenFramework", this.mapOfReportingPeriodToActiveDataset);
        // TODO ________________

        // TODO modularize following code block
        listOfDistinctAvailableReportingPeriodsForFramework.sort((reportingPeriodA, reportingPeriodB) => {
          if (reportingPeriodA > reportingPeriodB) return -1;
          else return 0;
        });
        this.$emit(
          "updateAvailableReportingPeriodsForChosenFramework",
          listOfDistinctAvailableReportingPeriodsForFramework
        );
        // TODO _______
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
    dataType() {
      void this.getDropdownOptionsAndActiveDataMetaInfoAndDoEmits();
    },
  },
});
</script>
