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
      <div v-if="isDataProcessedSuccesfully">
        <MarginWrapper class="text-left surface-0" style="margin-right: 0">
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
              <slot name="reportingPeriodDropdown"></slot>
            </div>
            <div v-if="hasUserUploaderRights" class="flex align-content-end align-items-center">
              <PrimeButton
                v-if="canEdit"
                class="uppercase p-button-outlined p-button p-button-sm d-letters mr-3"
                aria-label="EDIT DATA"
                @click="editDataset"
                data-test="editDatasetButton"
              >
                <span class="px-2">EDIT DATA</span>
                <span
                  v-if="mapOfReportingPeriodToActiveDataset.size > 1 && !singleDataMetaInfoToDisplay"
                  class="material-icons-outlined"
                  >arrow_drop_down</span
                >
              </PrimeButton>
              <router-link :to="addNewDatasetLinkTarget" class="no-underline" data-test="gotoNewDatasetButton">
                <PrimeButton class="uppercase p-button-sm d-letters" aria-label="New Dataset">
                  <span class="material-icons-outlined px-2">queue</span>
                  <span class="px-2">NEW DATASET</span>
                </PrimeButton>
              </router-link>
            </div>
            <OverlayPanel ref="reportingPeriodsOverlayPanel">
              <SelectReportingPeriodDialog :mapOfReportingPeriodToActiveDataset="mapOfReportingPeriodToActiveDataset" />
            </OverlayPanel>
          </div>
        </MarginWrapper>
        <MarginWrapper style="margin-right: 0">
          <slot name="content"></slot>
        </MarginWrapper>
      </div>
      <h1 v-else data-test="noDataCouldBeLoadedErrorIndicator">No data could be loaded.</h1>
    </TheContent>
    <TheFooter />
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
import TheFooter from "@/components/general/TheFooter.vue";
import { DataMetaInformation, DataTypeEnum } from "@clients/backend";
import { checkIfUserHasUploaderRights } from "@/utils/KeycloakUtils";

import OverlayPanel from "primevue/overlaypanel";
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
    TheFooter,
    PrimeButton,
    OverlayPanel,
    SelectReportingPeriodDialog,
  },
  emits: ["updateActiveDataMetaInfoForChosenFramework"],
  props: {
    companyID: {
      type: String,
    },
    dataType: {
      type: String,
    },
    singleDataMetaInfoToDisplay: {
      type: Object as () => DataMetaInformation,
      required: false,
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
      mapOfReportingPeriodToActiveDataset: new Map<string, DataMetaInformation>(),
      isDataProcessedSuccesfully: true,
      hasUserUploaderRights: null as null | boolean,
    };
  },
  computed: {
    canEdit() {
      return (
        ARRAY_OF_FRAMEWORKS_WITH_UPLOAD_FORM.includes(this.dataType as DataTypeEnum) &&
        (!this.singleDataMetaInfoToDisplay || this.singleDataMetaInfoToDisplay.currentlyActive)
      );
    },
    addNewDatasetLinkTarget() {
      return `/companies/${this.companyID ?? ""}/frameworks/upload`;
    },
  },
  created() {
    this.chosenDataTypeInDropdown = this.dataType ?? "";
    void this.getFrameworkDropdownOptionsAndActiveDataMetaInfoForEmit();
    checkIfUserHasUploaderRights(this.getKeycloakPromise)
      .then((hasUserUploaderRights) => {
        this.hasUserUploaderRights = hasUserUploaderRights;
      })
      .catch((error) => console.log(error));
    window.addEventListener("scroll", this.windowScrollHandler);
  },
  methods: {
    /**
     * Opens Overlay Panel for selecting a reporting period to edit data for
     * @param event event
     */
    editDataset(event: Event) {
      if (this.singleDataMetaInfoToDisplay) {
        this.gotoUpdateForm(
          this.singleDataMetaInfoToDisplay.companyId,
          this.singleDataMetaInfoToDisplay.dataType,
          this.singleDataMetaInfoToDisplay.dataId
        );
      } else if (this.mapOfReportingPeriodToActiveDataset.size > 1 && !this.singleDataMetaInfoToDisplay) {
        // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-explicit-any,@typescript-eslint/no-unsafe-member-access
        (this.$refs.reportingPeriodsOverlayPanel as any)?.toggle(event);
      } else if (this.mapOfReportingPeriodToActiveDataset.size == 1 && !this.singleDataMetaInfoToDisplay) {
        this.gotoUpdateForm(
          assertDefined(this.companyID),
          this.dataType as DataTypeEnum,
          (this.mapOfReportingPeriodToActiveDataset.entries().next().value as [string, DataMetaInformation])[1].dataId
        );
      }
    },
    /**
     * Navigates to the data update form
     * @param companyID company ID
     * @param dataType data type
     * @param dataId data Id
     */
    gotoUpdateForm(companyID: string, dataType: DataTypeEnum, dataId: string) {
      void this.$router.push(
        `/companies/${assertDefined(companyID)}/frameworks/${assertDefined(dataType)}/upload?templateDataId=${dataId}`
      );
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
      listOfDistinctAvailableAndViewableFrameworksForCompany.sort();
      listOfDistinctAvailableAndViewableFrameworksForCompany.forEach((dataType) => {
        this.dataTypesInDropdown.push({ label: humanizeString(dataType), value: dataType });
      });
    },

    /**
     * Uses a list of data meta info and filters out all elements whose data type (framework) do not equal the
     * dataType-prop set for this Vue-component during render.
     * @param listOfDataMetaInfo a list of data meta info
     * @returns the filtered list of data meta info
     */
    filterListOfDataMetaInfoForChosenFrameworkAndReturnIt(
      listOfDataMetaInfo: DataMetaInformation[]
    ): DataMetaInformation[] {
      return listOfDataMetaInfo.filter((dataMetaInfo) => dataMetaInfo.dataType === this.dataType);
    },

    /**
     * Uses a list of data meta info to set a map which has the distinct repoting periods as keys, and the respective
     * active data meta info as value.
     * It only takes into account data meta info whose dataType equals the current dataType prop value.
     * @param listOfActiveDataMetaInfo The list to be used as input for the map.
     */
    setMapOfReportingPeriodToActiveDatasetFromListOfActiveMetaDataInfo(
      listOfActiveDataMetaInfo: DataMetaInformation[]
    ) {
      this.mapOfReportingPeriodToActiveDataset = new Map<string, DataMetaInformation>();
      listOfActiveDataMetaInfo.forEach((dataMetaInfo: DataMetaInformation) => {
        if (dataMetaInfo.dataType === this.dataType) {
          if (dataMetaInfo.currentlyActive) {
            this.mapOfReportingPeriodToActiveDataset.set(dataMetaInfo.reportingPeriod, dataMetaInfo);
          } else {
            throw TypeError("Received inactive dataset meta info from Dataland Backend");
          }
        }
      });
    },

    /**
     * Goes through all data meta info for the currently viewed company and does two things.
     * First it sets the distinct frameworks as options in the framework-dropdown.
     * Then it builds a map which - for the currently chosen framework - maps all reporting periods to the data meta
     * info of the currently active dataset.
     */
    async getFrameworkDropdownOptionsAndActiveDataMetaInfoForEmit() {
      try {
        const metaDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getMetaDataControllerApi();
        const apiResponse = await metaDataControllerApi.getListOfDataMetaInfo(this.companyID);
        const listOfActiveDataMetaInfoPerFrameworkAndReportingPeriod = apiResponse.data;
        this.getDistinctAvailableFrameworksAndPutThemSortedIntoDropdown(
          listOfActiveDataMetaInfoPerFrameworkAndReportingPeriod
        );
        this.setMapOfReportingPeriodToActiveDatasetFromListOfActiveMetaDataInfo(
          listOfActiveDataMetaInfoPerFrameworkAndReportingPeriod
        );
        this.$emit("updateActiveDataMetaInfoForChosenFramework", this.mapOfReportingPeriodToActiveDataset);
        this.isDataProcessedSuccesfully = true;
      } catch (error) {
        this.isDataProcessedSuccesfully = false;
        console.error(error);
      }
    },
  },
  watch: {
    companyID() {
      void this.getFrameworkDropdownOptionsAndActiveDataMetaInfoForEmit();
    },
    dataType(newDataType: string) {
      this.chosenDataTypeInDropdown = newDataType;
      void this.getFrameworkDropdownOptionsAndActiveDataMetaInfoForEmit();
    },
  },
});
</script>
