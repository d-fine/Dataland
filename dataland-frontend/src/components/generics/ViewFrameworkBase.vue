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
            <ChangeFrameworkDropdown
              v-if="!isReviewableByCurrentUser"
              :data-meta-information="dataMetaInformation"
              :data-type="dataType"
              :company-i-d="companyID"
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
              :reportingPeriods="availableReportingPeriods"
              @close-download-modal="onCloseDownloadModal"
              @download-dataset="handleDatasetDownload"
              data-test="downloadModal"
            />

            <PrimeButton
              v-if="isEditableByCurrentUser"
              class="uppercase p-button p-button-sm d-letters ml-3"
              aria-label="EDIT DATA"
              @click="editDataset"
              data-test="editDatasetButton"
            >
              <span class="px-2 py-1">EDIT DATA</span>
              <span
                v-if="availableReportingPeriods.length > 1 && !singleDataMetaInfoToDisplay"
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
            <SimpleReportingPeriodSelectorDialog
              :reporting-periods="availableReportingPeriods"
              @selected-reporting-period="goToUpdateFormByReportingPeriod"
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
import { computed, defineComponent, inject, type PropType, ref } from 'vue';

import TheFooter from '@/components/generics/TheFooter.vue';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils';
import { type CompanyInformation, type DataMetaInformation, DataTypeEnum, ExportFileType } from '@clients/backend';

import SimpleReportingPeriodSelectorDialog from '@/components/general/SimpleReportingPeriodSelectorDialog.vue';
import OverlayPanel from 'primevue/overlaypanel';
import QualityAssuranceButtons from '@/components/resources/frameworkDataSearch/QualityAssuranceButtons.vue';
import CompanyInfoSheet from '@/components/general/CompanyInfoSheet.vue';
import type FrameworkDataSearchBar from '@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue';
import InputSwitch from 'primevue/inputswitch';
import { hasUserCompanyRoleForCompany } from '@/utils/CompanyRolesUtils';
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation.ts';
import { CompanyRole } from '@clients/communitymanager';
import router from '@/router';
import DownloadDatasetModal from '@/components/general/DownloadDatasetModal.vue';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi.ts';
import { type FrameworkData } from '@/utils/GenericFrameworkTypes.ts';
import { ExportFileTypeInformation } from '@/types/ExportFileTypeInformation.ts';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils.ts';
import { getAllPrivateFrameworkIdentifiers } from '@/frameworks/BasePrivateFrameworkRegistry.ts';
import { isFrameworkEditable } from '@/utils/Frameworks';
import { KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles';
import ChangeFrameworkDropdown from '@/components/generics/ChangeFrameworkDropdown.vue';
import { AxiosError } from 'axios';
import { useRoute } from 'vue-router';

export default defineComponent({
  name: 'ViewFrameworkBase',
  components: {
    ChangeFrameworkDropdown,
    DownloadDatasetModal,
    CompanyInfoSheet,
    TheContent,
    TheHeader,
    MarginWrapper,
    TheFooter,
    PrimeButton,
    OverlayPanel,
    SimpleReportingPeriodSelectorDialog,
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
      dataMetaInformation: [] as Array<DataMetaInformation>,
      windowScrollHandler: (): void => {
        this.handleScroll();
      },
      pageScrolled: false,
      scrollEmittedByToolbar: false,
      latestScrollPosition: 0,
      activeDataForCurrentCompanyAndFramework: [] as Array<DataAndMetaInformation<FrameworkData>>,
      isDataProcessedSuccessfully: false,
      hasUserUploaderRights: false,
      hasUserReviewerRights: false,
      hideEmptyFields: !this.hasUserReviewerRights,
      isDownloadModalOpen: false,
      route: useRoute(),
      dataId: null as null | string | string[],
    };
  },
  provide() {
    return {
      hideEmptyFields: computed(() => this.hideEmptyFields),
      mapOfReportingPeriodToActiveDataset: computed(() => this.mapOfReportingPeriodToActiveDataset),
    };
  },
  computed: {
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

    availableReportingPeriods(): string[] {
      const reportingPeriods = new Set<string>();
      this.activeDataForCurrentCompanyAndFramework.forEach((dataAndMetaInformation) => {
        if (dataAndMetaInformation.metaInfo.dataType == this.chosenDataTypeInDropdown)
          reportingPeriods.add(dataAndMetaInformation.metaInfo.reportingPeriod);
      });
      return Array.from(reportingPeriods).sort();
    },

    /**
     * This object is filled if ViewFrameworkBase displays multiple datasets.
     * If ViewFrameworkBase is used to display a single dataset, singleDataMetaInfoToDisplay is populated instead.
     */
    mapOfReportingPeriodToActiveDataset(): Map<string, DataMetaInformation> {
      const map: Map<string, DataMetaInformation> = new Map();
      this.activeDataForCurrentCompanyAndFramework.forEach((dataAndMetaInformation) => {
        map.set(dataAndMetaInformation.metaInfo.reportingPeriod, dataAndMetaInformation.metaInfo);
      });
      return map;
    },
  },

  created() {
    this.chosenDataTypeInDropdown = this.dataType ?? '';
    this.dataId = this.route.params.dataId;
    if (this.dataId) {
      void this.getDataMetaData().then(() => {
        this.setActiveDataForCurrentCompanyAndFramework();
      });
    } else {
      void this.getDataMetaData();
      void this.getAllActiveDataForCurrentCompanyAndFramework();
    }
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
     * @param fetchedCompanyInformation the company information for the current companyID
     */
    handleFetchedCompanyInformation(fetchedCompanyInformation: CompanyInformation) {
      this.fetchedCompanyInformation = fetchedCompanyInformation;
    },
    /**
     * Triggered on click on Edit button. In singleDatasetView, it triggers call to upload page with templateDataId. In
     * datasetOverview with only one dataset available, it triggers call to upload page with reportingPeriod.
     * In datasetOverview with multiple datasets available, a modal is opened to choose reportingPeriod to edit.
     * @param event event
     */
    editDataset(event: Event) {
      if (this.singleDataMetaInfoToDisplay) {
        this.goToUpdateFormByDataId(this.singleDataMetaInfoToDisplay.dataId);
      } else if (this.availableReportingPeriods.length > 1 && !this.singleDataMetaInfoToDisplay) {
        const panel = this.$refs.reportingPeriodsOverlayPanel as OverlayPanel;
        if (panel) {
          panel.toggle(event);
        }
      } else if (this.availableReportingPeriods.length == 1 && !this.singleDataMetaInfoToDisplay) {
        this.goToUpdateFormByReportingPeriod(this.availableReportingPeriods[0]);
      }
    },
    /**
     * Navigates to the data update form with templateDataId
     * @param dataId dataId
     */
    goToUpdateFormByDataId(dataId: string) {
      void router.push(
        `/companies/${assertDefined(this.companyID)}/frameworks/${assertDefined(this.dataType)}/upload?templateDataId=${dataId}`
      );
    },
    /**
     * Navigates to the data update form by using reportingPeriod
     * @param reportingPeriod reporting period
     */
    goToUpdateFormByReportingPeriod(reportingPeriod: string) {
      void router.push(
        `/companies/${assertDefined(this.companyID)}/frameworks/${assertDefined(this.dataType)}/upload?reportingPeriod=${reportingPeriod}`
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
     * Retrieves all data meta data available for current company
     */
    async getDataMetaData() {
      try {
        const apiClientProvider = new ApiClientProvider(assertDefined(this.getKeycloakPromise)());
        const metaDataControllerApi = apiClientProvider.backendClients.metaDataController;
        const apiResponse = await metaDataControllerApi.getListOfDataMetaInfo(this.companyID);
        this.dataMetaInformation = apiResponse.data;
      } catch (error) {
        this.isDataProcessedSuccessfully = false;
        console.log(error);
      }
    },

    /**
     * For public datasets, retrieves all active DataAndMetaInformation for current datatype and companyID. Then, the
     * mapOfReportingPeriodToActiveDataset is populated with this information (computed property).
     * For private datasets, the call to getAllCompanyData may lead to 403 if user doesn't have sufficient rights.
     * Instead, the metadata endpoint is called and the activeDataForCurrentCompanyAndFramework property is manually
     * filled with retrieved metadata and empty data object.
     */
    async getAllActiveDataForCurrentCompanyAndFramework() {
      try {
        const apiClientProvider = new ApiClientProvider(assertDefined(this.getKeycloakPromise)());
        const frameworkDataApi: PublicFrameworkDataApi<FrameworkData> | null = getFrameworkDataApiForIdentifier(
          this.dataType,
          apiClientProvider
        ) as PublicFrameworkDataApi<FrameworkData>;
        const apiResponse = await frameworkDataApi.getAllCompanyData(this.companyID, true);
        this.$emit('updateActiveDataMetaInfoForChosenFramework', this.mapOfReportingPeriodToActiveDataset);
        this.activeDataForCurrentCompanyAndFramework = Array.from(apiResponse.data);
        this.isDataProcessedSuccessfully = true;
      } catch (error) {
        if (error instanceof AxiosError && error?.status == 403 && this.dataType == DataTypeEnum.Vsme) {
          await this.getDataMetaData();
          this.setActiveDataForCurrentCompanyAndFramework();
        } else {
          this.isDataProcessedSuccessfully = false;
          console.error(error);
        }
      }
    },

    /**
     * Get available metadata in case that data cannot be received due to insufficient access rights for private data.
     */
    setActiveDataForCurrentCompanyAndFramework() {
      if (this.dataMetaInformation) {
        this.activeDataForCurrentCompanyAndFramework = this.dataMetaInformation.map((metaInfo) => {
          return { metaInfo: metaInfo, data: {} };
        });
        this.isDataProcessedSuccessfully = true;
      } else {
        this.isDataProcessedSuccessfully = false;
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
     * Download the dataset from the selected reporting period as a file in the selected format
     * @param selectedYear selected reporting year
     * @param selectedFileType selected export file type
     */
    async handleDatasetDownload(selectedYear: string, selectedFileType: string) {
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

        const exportFileType = Object.values(ExportFileType).find(
          (fileType) => fileType.toString() == selectedFileType
        );

        if (!exportFileType) {
          throw new ReferenceError('ExportFileType undefined.');
        }

        const fileExtension = ExportFileTypeInformation[exportFileType].fileExtension;
        const filename = `${selectedYear}-${this.dataType}-${this.companyID}.${fileExtension}`;

        const dataResponse = await frameworkDataApi.exportCompanyAssociatedDataByDimensions(
          selectedYear,
          this.companyID,
          exportFileType
        );
        const dataContent =
          exportFileType == ExportFileType.Json ? JSON.stringify(dataResponse.data) : dataResponse.data;

        if (!dataResponse) {
          throw new Error(`Retrieving ${this.dataType} data for company with companyID ${this.companyID}
           and reporting period ${selectedFileType} failed.`);
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
      void this.getDataMetaData();
      void this.getAllActiveDataForCurrentCompanyAndFramework();
    },
    isReviewableByCurrentUser() {
      this.hideEmptyFields = !this.hasUserReviewerRights;
    },
    dataType(newDataType: string) {
      this.chosenDataTypeInDropdown = newDataType;
      void this.getAllActiveDataForCurrentCompanyAndFramework();
    },
    mapOfReportingPeriodToActiveDataset() {
      this.$emit('updateActiveDataMetaInfoForChosenFramework', this.mapOfReportingPeriodToActiveDataset);
    },
  },
});
</script>
