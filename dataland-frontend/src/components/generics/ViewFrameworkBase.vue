<template>
  <TheHeader :showUserProfileDropdown="!viewInPreviewMode" />
  <TheContent class="paper-section min-h-screen">
    <CompanyInfoSheet
      :company-id="companyId"
      @fetched-company-information="handleFetchedCompanyInformation"
      :show-single-data-request-button="true"
      :framework="dataType"
      :map-of-reporting-period-to-active-dataset="mapOfReportingPeriodToActiveDataset"
    />
    <div v-if="isDataProcessedSuccessfully">
      <MarginWrapper
        class="text-left surface-0 dataland-toolbar"
        style="box-shadow: 0 4px 4px 0 #00000005; margin-right: 0"
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
              data-test="chooseFrameworkDropdown"
            />
            <slot name="reportingPeriodDropdown" />
            <div class="flex align-content-start align-items-center pl-3">
              <InputSwitch
                class="form-field vertical-middle"
                data-test="hideEmptyDataToggleButton"
                inputId="hideEmptyDataToggleButton"
                v-model="hideEmptyFields"
              />
              <span data-test="hideEmptyDataToggleCaption" class="ml-2 font-semibold" style="font-size: 14px">
                Hide empty fields
              </span>
            </div>
          </div>

          <div class="flex align-content-end align-items-center">
            <QualityAssuranceButtons
              v-if="isReviewableByCurrentUser && !!singleDataMetaInfoToDisplay"
              :meta-info="singleDataMetaInfoToDisplay"
              :company-name="fetchedCompanyInformation.companyName"
            />

            <PrimeButton
              class="uppercase p-button p-button-sm d-letters ml-3"
              aria-label="DOWNLOAD DATA"
              @click="isDownloadModalOpen = true"
              data-test="downloadDataButton"
            >
              <span class="px-2 py-1">DOWNLOAD DATA</span>
            </PrimeButton>

            <DownloadDataSetModal
              v-model:isDownloadModalOpen.sync="isDownloadModalOpen"
              :handleDownload="getDatasetFromExportApi"
              :dataType="dataType"
              :mapOfReportingPeriodToActiveDataset="mapOfReportingPeriodToActiveDataset"
              @update:isDownloadModalOpen="isDownloadModalOpen = $event"
            ></DownloadDataSetModal>

            <PrimeButton
              v-if="isEditableByCurrentUser"
              class="uppercase p-button p-button-sm d-letters ml-3"
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
            <SelectReportingPeriodDialog
              :mapOfReportingPeriodToActiveDataset="mapOfReportingPeriodToActiveDataset"
              :action-on-click="ReportingPeriodTableActions.EditDataset"
              @selected-reporting-period="handleReportingPeriodSelection"
            />
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
import TheContent from '@/components/generics/TheContent.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import MarginWrapper from '@/components/wrapper/MarginWrapper.vue';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import Dropdown, { type DropdownChangeEvent } from 'primevue/dropdown';
import { computed, defineComponent, inject, type PropType, ref } from 'vue';

import TheFooter from '@/components/generics/TheFooter.vue';
import { FRAMEWORKS_WITH_EDIT_FUNCTIONALITY, FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import { KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_UPLOADER, checkIfUserHasRole } from '@/utils/KeycloakUtils';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { type DataMetaInformation, type CompanyInformation, type DataTypeEnum } from '@clients/backend';

import SelectReportingPeriodDialog from '@/components/general/SelectReportingPeriodDialog.vue';
import OverlayPanel from 'primevue/overlaypanel';
import QualityAssuranceButtons from '@/components/resources/frameworkDataSearch/QualityAssuranceButtons.vue';
import CompanyInfoSheet from '@/components/general/CompanyInfoSheet.vue';
import type FrameworkDataSearchBar from '@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue';
import InputSwitch from 'primevue/inputswitch';
import { hasUserCompanyRoleForCompany } from '@/utils/CompanyRolesUtils';
import { ReportingPeriodTableActions, type ReportingPeriodTableEntry } from '@/utils/PremadeDropdownDatasets';
import { CompanyRole } from '@clients/communitymanager';
import router from '@/router';
import DownloadDataSetModal from '@/components/general/DownloadDataSetModal.vue';

export default defineComponent({
  name: 'ViewFrameworkBase',
  components: {
    DownloadDataSetModal,
    CompanyInfoSheet,
    TheContent,
    TheHeader,
    MarginWrapper,
    Dropdown,
    TheFooter,
    PrimeButton,
    OverlayPanel,
    SelectReportingPeriodDialog,
    QualityAssuranceButtons,
    InputSwitch,
  },
  emits: ['updateActiveDataMetaInfoForChosenFramework'],
  props: {
    companyId: {
      type: String,
      required: true,
    },
    dataType: {
      type: String as PropType<DataTypeEnum>,
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
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
      frameworkDataSearchBar: ref<typeof FrameworkDataSearchBar>(),
    };
  },
  data() {
    return {
      fetchedCompanyInformation: {} as CompanyInformation,
      chosenDataTypeInDropdown: '',
      dataTypesInDropdown: [] as { label: string; value: string }[],
      humanizeStringOrNumber,
      windowScrollHandler: (): void => {
        this.handleScroll();
      },
      pageScrolled: false,
      scrollEmittedByToolbar: false,
      latestScrollPosition: 0,
      mapOfReportingPeriodToActiveDataset: new Map<string, DataMetaInformation>(),
      isDataProcessedSuccessfully: true,
      hasUserUploaderRights: false,
      hasUserReviewerRights: false,
      hideEmptyFields: !this.hasUserReviewerRights,
      isDownloadModalOpen: false,
    };
  },
  provide() {
    return {
      hideEmptyFields: computed(() => {
        return this.hideEmptyFields;
      }),
    };
  },
  computed: {
    ReportingPeriodTableActions() {
      return ReportingPeriodTableActions;
    },
    isReviewableByCurrentUser() {
      return this.hasUserReviewerRights && this.singleDataMetaInfoToDisplay?.qaStatus === 'Pending';
    },
    isEditableByCurrentUser() {
      return (
        this.hasUserUploaderRights &&
        FRAMEWORKS_WITH_EDIT_FUNCTIONALITY.includes(this.dataType) &&
        (!this.singleDataMetaInfoToDisplay ||
          this.singleDataMetaInfoToDisplay.currentlyActive ||
          this.singleDataMetaInfoToDisplay.qaStatus === 'Rejected')
      );
    },
    targetLinkForAddingNewDataset() {
      return `/companies/${this.companyId ?? ''}/frameworks/upload`;
    },
  },
  created() {
    this.chosenDataTypeInDropdown = this.dataType ?? '';
    void this.getFrameworkDropdownOptionsAndActiveDataMetaInfoForEmit();

    void this.setViewPageAttributesForUser();

    window.addEventListener('scroll', this.windowScrollHandler);
  },
  methods: {
    /**
     * Saves the company information emitted by the CompanyInformation vue components event.
     * @param fetchedCompanyInformation the company information for the current companyId
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
          this.singleDataMetaInfoToDisplay.dataId
        );
      } else if (this.mapOfReportingPeriodToActiveDataset.size > 1 && !this.singleDataMetaInfoToDisplay) {
        const panel = this.$refs.reportingPeriodsOverlayPanel as OverlayPanel;
        if (panel) {
          panel.toggle(event);
        }
      } else if (this.mapOfReportingPeriodToActiveDataset.size == 1 && !this.singleDataMetaInfoToDisplay) {
        this.gotoUpdateForm(
          assertDefined(this.companyId),
          this.dataType,
          Array.from(this.mapOfReportingPeriodToActiveDataset.values())[0].dataId
        );
      }
    },
    /**
     * Navigates to the data update form
     * @param companyId company Id
     * @param dataType data type
     * @param dataId data Id
     */
    gotoUpdateForm(companyId: string, dataType: DataTypeEnum, dataId: string) {
      void router.push(
        `/companies/${assertDefined(companyId)}/frameworks/${assertDefined(dataType)}/upload?templateDataId=${dataId}`
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
        void router.push(`/companies/${this.companyId}/frameworks/${this.chosenDataTypeInDropdown}`);
      }
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
        if (FRAMEWORKS_WITH_VIEW_PAGE.includes(dataType)) {
          listOfDistinctAvailableAndViewableFrameworksForCompany.push(dataType);
        }
      });
      listOfDistinctAvailableAndViewableFrameworksForCompany.sort((a, b) => a.localeCompare(b));
      listOfDistinctAvailableAndViewableFrameworksForCompany.forEach((dataType) => {
        this.dataTypesInDropdown.push({ label: humanizeStringOrNumber(dataType), value: dataType });
      });
    },

    /**
     * Uses a list of data meta info to set a map which has the distinct reporting periods as keys, and the respective
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
            throw TypeError('Received inactive dataset meta info from Dataland Backend');
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
        const backendClients = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).backendClients;
        const metaDataControllerApi = backendClients.metaDataController;
        const apiResponse = await metaDataControllerApi.getListOfDataMetaInfo(this.companyId);
        const listOfActiveDataMetaInfoPerFrameworkAndReportingPeriod = apiResponse.data;
        this.getDistinctAvailableFrameworksAndPutThemSortedIntoDropdown(
          listOfActiveDataMetaInfoPerFrameworkAndReportingPeriod
        );
        this.setMapOfReportingPeriodToActiveDatasetFromListOfActiveMetaDataInfo(
          listOfActiveDataMetaInfoPerFrameworkAndReportingPeriod
        );
        this.$emit('updateActiveDataMetaInfoForChosenFramework', this.mapOfReportingPeriodToActiveDataset);
        this.isDataProcessedSuccessfully = true;
      } catch (error) {
        this.isDataProcessedSuccessfully = false;
        console.error(error);
      }
    },
    /**
     * Set if the user is allowed to upload data for the current company
     * @returns a promise that resolves to void, so the successful execution of the function can be awaited
     */
    async setViewPageAttributesForUser(): Promise<void> {
      return checkIfUserHasRole(KEYCLOAK_ROLE_REVIEWER, this.getKeycloakPromise)
        .then((hasUserReviewerRights) => {
          this.hasUserReviewerRights = hasUserReviewerRights;
        })
        .then(async () => {
          this.hasUserUploaderRights = await checkIfUserHasRole(KEYCLOAK_ROLE_UPLOADER, this.getKeycloakPromise);
        })
        .then(() => {
          if (!this.hasUserUploaderRights) {
            return hasUserCompanyRoleForCompany(CompanyRole.CompanyOwner, this.companyId, this.getKeycloakPromise).then(
              (hasUserUploaderRights) => {
                this.hasUserUploaderRights = hasUserUploaderRights;
              }
            );
          }
        });
    },
    /**
     * Handles the selection of the reporting period in th dropdown panel
     * @param reportingPeriodTableEntry object, which was chosen
     * @returns a router push to the edit url of the chosen dataset
     */
    handleReportingPeriodSelection(reportingPeriodTableEntry: ReportingPeriodTableEntry) {
      return router.push(reportingPeriodTableEntry.editUrl);
    },
    /**
     * Downloads the dataset from the selected reporting period as a file in the selected format
     * @param reportingYear selected reporting year
     * @param fileFormat selected file format
     */
    getDatasetFromExportApi(reportingYear: String, fileFormat: String) {
      console.log(this.dataType, this.companyId, reportingYear, fileFormat);
      return;
    },
  },
  watch: {
    companyID() {
      void this.getFrameworkDropdownOptionsAndActiveDataMetaInfoForEmit();
    },
    isReviewableByCurrentUser() {
      this.hideEmptyFields = !this.hasUserReviewerRights;
    },
    dataType(newDataType: string) {
      this.chosenDataTypeInDropdown = newDataType;
      void this.getFrameworkDropdownOptionsAndActiveDataMetaInfoForEmit();
    },
  },
});
</script>
