<template>
  <TheHeader :showUserProfileDropdown="!viewInPreviewMode" />
  <TheContent class="paper-section min-h-screen">
    <MarginWrapper class="text-left surface-0" style="margin-right: 0">
      <BackButton />
      <FrameworkDataSearchBar
        v-if="!viewInPreviewMode && !isReviewableByCurrentUser"
        :companyIdIfOnViewPage="companyID"
        class="mt-2"
        ref="frameworkDataSearchBar"
        @search-confirmed="handleSearchConfirm"
      />
    </MarginWrapper>
    <MarginWrapper class="surface-0" style="margin-right: 0">
      <div class="grid align-items-end">
        <div class="col-9">
          <CompanyInformationBanner
            :companyId="companyID"
            @fetchedCompanyInformation="handleFetchedCompanyInformation"
          />
        </div>
      </div>
    </MarginWrapper>
    <div v-if="isDataProcessedSuccesfully">
      <MarginWrapper
        class="text-left surface-0 dataland-toolbar"
        style="margin-right: 0"
        :class="[pageScrolled ? ['fixed w-100'] : '']"
      >
        <div class="flex justify-content-between align-items-center d-search-filters-panel">
          <div class="flex">
            <Dropdown
              v-if="!isReviewableByCurrentUser"
              id="chooseFrameworkDropdown"
              v-model="chosenDataTypeInDropdown"
              :options="dataTypesInDropdown"
              optionLabel="label"
              optionValue="value"
              :placeholder="humanizeStringOrNumber(dataType)"
              aria-label="Choose framework"
              class="fill-dropdown always-fill"
              dropdownIcon="pi pi-angle-down"
              @change="handleChangeFrameworkEvent"
            />
            <slot name="reportingPeriodDropdown" />
          </div>
          <div class="flex align-content-end align-items-center">
            <QualityAssuranceButtons
              v-if="isReviewableByCurrentUser"
              :meta-info="singleDataMetaInfoToDisplay"
              :company-name="fetchedCompanyInformation.companyName"
            />
            <PrimeButton
              v-if="isEditableByCurrentUser"
              class="uppercase p-button-outlined p-button p-button-sm d-letters ml-3"
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
            <router-link
              v-if="hasUserUploaderRights"
              :to="targetLinkForAddingNewDataset"
              class="no-underline ml-3"
              data-test="gotoNewDatasetButton"
            >
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
        <slot name="content" :inReviewMode="isReviewableByCurrentUser"></slot>
      </MarginWrapper>
    </div>
    <h1 v-else data-test="noDataCouldBeLoadedErrorIndicator">No data could be loaded.</h1>
  </TheContent>
  <TheFooter />
</template>

<script lang="ts">
import BackButton from "@/components/general/BackButton.vue";
import TheContent from "@/components/generics/TheContent.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import CompanyInformationBanner from "@/components/pages/CompanyInformation.vue";
import FrameworkDataSearchBar from "@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import type Keycloak from "keycloak-js";
import PrimeButton from "primevue/button";
import Dropdown, { type DropdownChangeEvent } from "primevue/dropdown";
import { defineComponent, inject, ref } from "vue";

import TheFooter from "@/components/generics/TheFooter.vue";
import { ARRAY_OF_FRAMEWORKS_WITH_UPLOAD_FORM, ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE } from "@/utils/Constants";
import { KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_UPLOADER, checkIfUserHasRole } from "@/utils/KeycloakUtils";
import { humanizeStringOrNumber } from "@/utils/StringHumanizer";
import { type DataMetaInformation, type CompanyInformation, type DataTypeEnum } from "@clients/backend";

import SelectReportingPeriodDialog from "@/components/general/SelectReportingPeriodDialog.vue";
import OverlayPanel from "primevue/overlaypanel";
import QualityAssuranceButtons from "@/components/resources/frameworkDataSearch/QualityAssuranceButtons.vue";

export default defineComponent({
  name: "ViewFrameworkBase",
  components: {
    TheContent,
    TheHeader,
    BackButton,
    MarginWrapper,
    FrameworkDataSearchBar,
    Dropdown,
    CompanyInformationBanner,
    TheFooter,
    PrimeButton,
    OverlayPanel,
    SelectReportingPeriodDialog,
    QualityAssuranceButtons,
  },
  emits: ["updateActiveDataMetaInfoForChosenFramework"],
  props: {
    companyID: {
      type: String,
      required: true,
    },
    dataType: {
      type: String,
      required: true,
    },
    singleDataMetaInfoToDisplay: {
      type: Object as () => DataMetaInformation,
    },
    viewInPreviewMode: {
      type: Boolean,
      default: false,
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
      fetchedCompanyInformation: {} as CompanyInformation,

      chosenDataTypeInDropdown: "",
      dataTypesInDropdown: [] as { label: string; value: string }[],
      humanizeStringOrNumber,
      windowScrollHandler: (): void => {
        this.handleScroll();
      },
      pageScrolled: false,
      scrollEmittedByToolbar: false,
      latestScrollPosition: 0,
      mapOfReportingPeriodToActiveDataset: new Map<string, DataMetaInformation>(),
      isDataProcessedSuccesfully: true,
      hasUserUploaderRights: false,
      hasUserReviewerRights: false,
    };
  },
  computed: {
    isReviewableByCurrentUser() {
      return this.hasUserReviewerRights && this.singleDataMetaInfoToDisplay?.qaStatus === "Pending";
    },
    isEditableByCurrentUser() {
      return (
        this.hasUserUploaderRights &&
        ARRAY_OF_FRAMEWORKS_WITH_UPLOAD_FORM.includes(this.dataType as DataTypeEnum) &&
        (!this.singleDataMetaInfoToDisplay ||
          this.singleDataMetaInfoToDisplay.currentlyActive ||
          this.singleDataMetaInfoToDisplay.qaStatus === "Rejected")
      );
    },
    targetLinkForAddingNewDataset() {
      return `/companies/${this.companyID ?? ""}/frameworks/upload`;
    },
  },
  created() {
    this.chosenDataTypeInDropdown = this.dataType ?? "";
    void this.getFrameworkDropdownOptionsAndActiveDataMetaInfoForEmit();

    checkIfUserHasRole(KEYCLOAK_ROLE_UPLOADER, this.getKeycloakPromise)
      .then((hasUserUploaderRights) => {
        this.hasUserUploaderRights = hasUserUploaderRights;
      })
      .catch((error) => console.log(error));
    checkIfUserHasRole(KEYCLOAK_ROLE_REVIEWER, this.getKeycloakPromise)
      .then((hasUserReviewerRights) => {
        this.hasUserReviewerRights = hasUserReviewerRights;
      })
      .catch((error) => console.log(error));

    window.addEventListener("scroll", this.windowScrollHandler);
  },
  methods: {
    /**
     * Saves the company information emitted by the CompanyInformation vue components event.
     * @param fetchedCompanyInformation the company information for the current company Id
     */
    handleFetchedCompanyInformation(fetchedCompanyInformation: CompanyInformation) {
      this.fetchedCompanyInformation = fetchedCompanyInformation;
    },

    /**
     * Opens Overlay Panel for selecting a reporting period to edit data for
     * @param event event
     */
    editDataset(event: Event) {
      if (this.singleDataMetaInfoToDisplay) {
        this.gotoUpdateForm(
          this.singleDataMetaInfoToDisplay.companyId,
          this.singleDataMetaInfoToDisplay.dataType,
          this.singleDataMetaInfoToDisplay.dataId,
        );
      } else if (this.mapOfReportingPeriodToActiveDataset.size > 1 && !this.singleDataMetaInfoToDisplay) {
        const panel = this.$refs.reportingPeriodsOverlayPanel as OverlayPanel;
        if (panel) {
          panel.toggle(event);
        }
      } else if (this.mapOfReportingPeriodToActiveDataset.size == 1 && !this.singleDataMetaInfoToDisplay) {
        this.gotoUpdateForm(
          assertDefined(this.companyID),
          this.dataType as DataTypeEnum,
          Array.from(this.mapOfReportingPeriodToActiveDataset.values())[0].dataId,
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
        `/companies/${assertDefined(companyID)}/frameworks/${assertDefined(dataType)}/upload?templateDataId=${dataId}`,
      );
    },
    /**
     * Hides the dropdown of the Autocomplete-component
     */
    handleScroll() {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
      this.frameworkDataSearchBar?.$refs.autocomplete.hide();
      const windowScrollY = window.scrollY;
      if (this.scrollEmittedByToolbar) {
        this.scrollEmittedByToolbar = false;
      } else if (this.latestScrollPosition > windowScrollY) {
        //ScrollUP event
        this.latestScrollPosition = windowScrollY;
        this.pageScrolled = document.documentElement.scrollTop >= 195;
      } else {
        //ScrollDOWN event
        this.latestScrollPosition = windowScrollY;
        this.pageScrolled = document.documentElement.scrollTop > 195;
      }
    },
    /**
     * Visits the framework view page for the framework which was chosen in the dropdown
     * @param dropDownChangeEvent the change event emitted by the dropdown component
     */
    handleChangeFrameworkEvent(dropDownChangeEvent: DropdownChangeEvent) {
      if (this.dataType != dropDownChangeEvent.value) {
        void this.$router.push(`/companies/${this.companyID}/frameworks/${this.chosenDataTypeInDropdown}`);
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
      listOfDistinctAvailableAndViewableFrameworksForCompany.sort((a, b) => a.localeCompare(b));
      listOfDistinctAvailableAndViewableFrameworksForCompany.forEach((dataType) => {
        this.dataTypesInDropdown.push({ label: humanizeStringOrNumber(dataType), value: dataType });
      });
    },

    /**
     * Uses a list of data meta info to set a map which has the distinct repoting periods as keys, and the respective
     * active data meta info as value.
     * It only takes into account data meta info whose dataType equals the current dataType prop value.
     * @param listOfActiveDataMetaInfo The list to be used as input for the map.
     */
    setMapOfReportingPeriodToActiveDatasetFromListOfActiveMetaDataInfo(
      listOfActiveDataMetaInfo: DataMetaInformation[],
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
          assertDefined(this.getKeycloakPromise)(),
        ).getMetaDataControllerApi();
        const apiResponse = await metaDataControllerApi.getListOfDataMetaInfo(this.companyID);
        const listOfActiveDataMetaInfoPerFrameworkAndReportingPeriod = apiResponse.data;
        this.getDistinctAvailableFrameworksAndPutThemSortedIntoDropdown(
          listOfActiveDataMetaInfoPerFrameworkAndReportingPeriod,
        );
        this.setMapOfReportingPeriodToActiveDatasetFromListOfActiveMetaDataInfo(
          listOfActiveDataMetaInfoPerFrameworkAndReportingPeriod,
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
