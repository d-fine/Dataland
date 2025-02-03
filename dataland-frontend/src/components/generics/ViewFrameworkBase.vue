<template>
  <TheHeader :showUserProfileDropdown="!viewInPreviewMode" />
  <TheContent class="paper-section min-h-screen">
    <CompanyInfoSheet
      :company-id="companyID"
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
              v-if="!getAllPrivateFrameworkIdentifiers().includes(dataType)"
              @click="isDownloadModalOpen = true"
              data-test="downloadDataButton"
            >
              <span class="px-2 py-1">DOWNLOAD DATA</span>
            </PrimeButton>

            <DownloadDatasetModal
              v-if="!getAllPrivateFrameworkIdentifiers().includes(dataType)"
              :isDownloadModalOpen="isDownloadModalOpen"
              :mapOfReportingPeriodToActiveDataset="mapOfReportingPeriodToActiveDataset"
              :singleDataMetaInfoToDisplay="singleDataMetaInfoToDisplay"
              @close-download-modal="onCloseDownloadModal"
              @download-dataset="handleDatasetDownload"
              data-test="downloadModal"
            >
            </DownloadDatasetModal>

            <PrimeButton
              v-if="isEditableByCurrentUser"
              class="uppercase p-button p-button-sm d-letters ml-3"
              aria-label="EDIT DATA"
              @click="editDataset"
              data-test="editDatasetButton"
            >
              <span class="px-2 py-1">EDIT DATA</span>
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
import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { type CompanyInformation, type DataMetaInformation, type DataTypeEnum } from '@clients/backend';

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
import DownloadDatasetModal from '@/components/general/DownloadDatasetModal.vue';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi.ts';
import { type FrameworkData } from '@/utils/GenericFrameworkTypes.ts';
import { ExportFileTypes } from '@/types/ExportFileTypes.ts';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils.ts';
import { getAllPrivateFrameworkIdentifiers } from '@/frameworks/BasePrivateFrameworkRegistry.ts';
import { isFrameworkEditable } from '@/utils/Frameworks.ts';
import { KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles.ts';

export default defineComponent({
  name: 'ViewFrameworkBase',
  components: {
    DownloadDatasetModal,
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
    companyID: {
      type: String,
      required: true,
    },
    dataType: {
      type: String as PropType<DataTypeEnum>,
      required: true,
    },
    /**
     * This object is filled if ViewFrameworkBase displays a single dataset.
     * If ViewFrameworkBase is used to display multiple datasets, mapOfReportingPeriodToActiveDataset is populated instead.
     */
    singleDataMetaInfoToDisplay: {
      type: Object as PropType<DataMetaInformation>,
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
      /**
       * This object is filled if ViewFrameworkBase displays multiple datasets.
       * If ViewFrameworkBase is used to display a single dataset, singleDataMetaInfoToDisplay is populated instead.
       */
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
        isFrameworkEditable(this.dataType) &&
        (!this.singleDataMetaInfoToDisplay ||
          this.singleDataMetaInfoToDisplay.currentlyActive ||
          this.singleDataMetaInfoToDisplay.qaStatus === 'Rejected')
      );
    },
    targetLinkForAddingNewDataset() {
      return `/companies/${this.companyID ?? ''}/frameworks/upload`;
    },
  },
  created() {
    this.chosenDataTypeInDropdown = this.dataType ?? '';
    void this.getFrameworkDropdownOptionsAndActiveDataMetaInfoForEmit();

    void this.setViewPageAttributesForUser();

    window.addEventListener('scroll', this.windowScrollHandler);
  },
  methods: {
    getAllPrivateFrameworkIdentifiers,
    /**
     * Triggered by event "closeDownloadModal" emitted by the DownloadDatasetModal component
     */
    onCloseDownloadModal() {
      this.isDownloadModalOpen = false;
    },
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
          this.singleDataMetaInfoToDisplay.dataId
        );
      } else if (this.mapOfReportingPeriodToActiveDataset.size > 1 && !this.singleDataMetaInfoToDisplay) {
        const panel = this.$refs.reportingPeriodsOverlayPanel as OverlayPanel;
        if (panel) {
          panel.toggle(event);
        }
      } else if (this.mapOfReportingPeriodToActiveDataset.size == 1 && !this.singleDataMetaInfoToDisplay) {
        this.gotoUpdateForm(
          assertDefined(this.companyID),
          this.dataType,
          Array.from(this.mapOfReportingPeriodToActiveDataset.values())[0].dataId
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
      void router.push(
        `/companies/${assertDefined(companyID)}/frameworks/${assertDefined(dataType)}/upload?templateDataId=${dataId}`
      );
    },
    /**
     * Hides the dropdown of the Autocomplete-component
     */
    handleScroll() {
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
        void router.push(`/companies/${this.companyID}/frameworks/${this.chosenDataTypeInDropdown}`);
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
        const apiResponse = await metaDataControllerApi.getListOfDataMetaInfo(this.companyID);
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
        .then(() => {
          return checkIfUserHasRole(KEYCLOAK_ROLE_UPLOADER, this.getKeycloakPromise).then((hasUserUploaderRights) => {
            this.hasUserUploaderRights = hasUserUploaderRights;
          });
        })
        .then(() => {
          if (!this.hasUserUploaderRights) {
            return hasUserCompanyRoleForCompany(CompanyRole.CompanyOwner, this.companyID, this.getKeycloakPromise).then(
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
     * Download the dataset from the selected reporting period as a file in the selected format
     * @param selectedYear selected reporting year
     * @param selectedFileTypeIdentifier selected export file type
     */
    async handleDatasetDownload(selectedYear: string, selectedFileTypeIdentifier: string) {
      let dataId;
      if (this.singleDataMetaInfoToDisplay) {
        dataId = this.singleDataMetaInfoToDisplay.dataId;
      } else {
        dataId = this.mapOfReportingPeriodToActiveDataset.get(selectedYear)?.dataId;
      }

      if (!dataId) {
        throw new ReferenceError(`DataId does not exist.`);
      }

      try {
        const apiClientProvider = new ApiClientProvider(assertDefined(this.getKeycloakPromise)());
        // DataExport Button does not exist for private frameworks, so cast is safe
        const frameworkDataApi: PublicFrameworkDataApi<FrameworkData> | null = getFrameworkDataApiForIdentifier(
          this.dataType,
          apiClientProvider
        ) as PublicFrameworkDataApi<FrameworkData>;

        if (!frameworkDataApi) {
          throw new ReferenceError('Retrieving dataApi for framework failed.');
        }

        let dataResponse;
        let dataContent;

        const exportFileType = Object.values(ExportFileTypes).find(
          (fileType) => fileType.identifier === selectedFileTypeIdentifier
        );

        if (!exportFileType) {
          throw new ReferenceError('ExportFileType undefined.');
        }

        const fileExtension = exportFileType.fileExtension;
        const filename = `${dataId}.${fileExtension}`;

        switch (exportFileType.identifier) {
          case 'csv':
            dataResponse = await frameworkDataApi.exportCompanyAssociatedDataToCsv(dataId);
            dataContent = dataResponse.data;
            break;
          case 'excel':
            dataResponse = await frameworkDataApi.exportCompanyAssociatedDataToExcel(dataId);
            dataContent = dataResponse.data;
            break;
          case 'json':
            dataResponse = await frameworkDataApi.exportCompanyAssociatedDataToJson(dataId);
            dataContent = JSON.stringify(dataResponse.data);
            break;
        }

        if (!dataResponse) {
          throw new Error(`Retrieving frameworkData for dataId ${dataId} failed.`);
        }

        this.forceFileDownload(dataContent, filename);
      } catch (error) {
        console.error(error);
      }
    },

    /**
     * In order to download a file via frontend, it is necessary to create a link, attach the file to it, and click
     * the link to trigger the file download. Afterward, the created element is deleted from the DOM.
     * @param content dataContent string to be downloaded to file
     * @param filename name of file to be downloaded
     */
    forceFileDownload(content: string, filename: string) {
      const url = window.URL.createObjectURL(new Blob([content]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
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
