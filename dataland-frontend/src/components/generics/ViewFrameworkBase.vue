<template>
  <TheHeader :showUserProfileDropdown="!viewInPreviewMode" />
  <TheContent class="min-h-screen">
    <CompanyInfoSheet
      :company-id="companyID"
      @fetched-company-information="handleFetchedCompanyInformation"
      :show-single-data-request-button="true"
      :framework="dataType"
      :map-of-reporting-period-to-active-dataset="mapOfReportingPeriodToActiveDataset"
    />
    <div v-if="isDataProcessedSuccessfully">
      <MarginWrapper
        class="text-left"
        bg-class="dataland-toolbar sticky"
        style="box-shadow: 0 4px 4px 0 #00000005; margin-right: 0"
      >
        <div class="flex justify-content-between align-items-center d-search-filters-panel">
          <div class="flex">
            <ChangeFrameworkDropdown
              v-if="!isReviewableByCurrentUser"
              :data-meta-information="dataMetaInformation"
              :data-type="dataType"
              :company-id="companyID"
            />
            <slot name="reportingPeriodDropdown" />
            <div class="flex align-content-start align-items-center pl-3">
              <ToggleSwitch
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
          <Popover ref="reportingPeriodsPopover">
            <SimpleReportingPeriodSelectorDialog
              :reporting-periods="availableReportingPeriods"
              @selected-reporting-period="goToUpdateFormByReportingPeriod"
            />
          </Popover>
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
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation.ts';
import CompanyInfoSheet from '@/components/general/CompanyInfoSheet.vue';
import DownloadDatasetModal from '@/components/general/DownloadDatasetModal.vue';

import SimpleReportingPeriodSelectorDialog from '@/components/general/SimpleReportingPeriodSelectorDialog.vue';
import ChangeFrameworkDropdown from '@/components/generics/ChangeFrameworkDropdown.vue';
import TheContent from '@/components/generics/TheContent.vue';

import TheFooter from '@/components/generics/TheFooter.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import QualityAssuranceButtons from '@/components/resources/frameworkDataSearch/QualityAssuranceButtons.vue';
import MarginWrapper from '@/components/wrapper/MarginWrapper.vue';
import { getAllPrivateFrameworkIdentifiers } from '@/frameworks/BasePrivateFrameworkRegistry.ts';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils.ts';
import router from '@/router';
import { ApiClientProvider } from '@/services/ApiClients';
import { ExportFileTypeInformation } from '@/types/ExportFileTypeInformation.ts';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi.ts';
import { hasUserCompanyRoleForCompany } from '@/utils/CompanyRolesUtils';
import { getDateStringForDataExport } from '@/utils/DataFormatUtils.ts';
import { isFrameworkEditable } from '@/utils/Frameworks';
import { type FrameworkData } from '@/utils/GenericFrameworkTypes.ts';
import { KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { type CompanyInformation, type DataMetaInformation, DataTypeEnum, ExportFileType } from '@clients/backend';
import { CompanyRole } from '@clients/communitymanager';
import { AxiosError, type AxiosRequestConfig } from 'axios';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import ToggleSwitch from 'primevue/toggleswitch';
import Popover from 'primevue/popover';
import { computed, defineComponent, inject, type PropType, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER } from '@/utils/Constants.ts';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';

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
    Popover,
    SimpleReportingPeriodSelectorDialog,
    QualityAssuranceButtons,
    ToggleSwitch,
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
    };
  },
  data() {
    return {
      fetchedCompanyInformation: {} as CompanyInformation,
      chosenDataTypeInDropdown: '',
      dataMetaInformation: [] as Array<DataMetaInformation>,
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
      void this.getMetaData().then(() => {
        this.setActiveDataForCurrentCompanyAndFramework();
      });
    } else {
      void this.getMetaData();
      void this.getAllActiveDataForCurrentCompanyAndFramework();
    }
    void this.setViewPageAttributesForUser();
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
        const reportingPeriodsPopover = ref();
        if (reportingPeriodsPopover.value) {
          reportingPeriodsPopover.value.toggle(event);
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
     * Retrieves all data meta data available for current company
     */
    async getMetaData() {
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
     * Instead, the metaData endpoint is called and the activeDataForCurrentCompanyAndFramework property is manually
     * filled with retrieved metaData and empty data object.
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
          await this.getMetaData();
          this.setActiveDataForCurrentCompanyAndFramework();
        } else {
          this.isDataProcessedSuccessfully = false;
          console.error(error);
        }
      }
    },

    /**
     * Get available metaData in case of either insufficient rights.
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

        const formatted_timestamp = getDateStringForDataExport(new Date());
        const fileExtension = ExportFileTypeInformation[exportFileType].fileExtension;
        const options: AxiosRequestConfig | undefined =
          fileExtension === 'xlsx' ? { responseType: 'arraybuffer' } : undefined;

        const availableFrameworks = ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER.map((framework) => ({
          value: framework,
          label: humanizeStringOrNumber(framework),
        }));

        const frameworkLabel =
          availableFrameworks.find((framework) => framework.value === this.dataType)?.label || this.dataType;
        const filename = `data-export-${frameworkLabel}-${formatted_timestamp}.${fileExtension}`;

        const dataResponse = await frameworkDataApi.exportCompanyAssociatedDataByDimensions(
          [selectedYear],
          [this.companyID],
          exportFileType,
          true,
          options
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
     * @param filename name of the file to be downloaded
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
      void this.getMetaData();
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
<style scoped>
.d-letters {
  letter-spacing: 0.05em;
}

.vertical-middle {
  display: flex;
  align-items: center;
}

.p-button {
  white-space: nowrap;
  cursor: pointer;
  font-weight: var(--button-fw);
  text-decoration: none;
  min-width: 10em;
  width: fit-content;
  justify-content: center;
  display: inline-flex;
  align-items: center;
  vertical-align: bottom;
  flex-direction: row;
  letter-spacing: 0.05em;
  font-family: inherit;
  transition: all 0.2s;
  border-radius: 0;
  text-transform: uppercase;
  font-size: 0.875rem;

  &:enabled:hover {
    color: white;
    background: hsl(from var(--btn-primary-bg) h s calc(l - 20));
    border-color: hsl(from var(--btn-primary-bg) h s calc(l - 20));
  }

  &:enabled:active {
    background: hsl(from var(--btn-primary-bg) h s calc(l - 10));
    border-color: hsl(from var(--btn-primary-bg) h s calc(l - 10));
  }

  &:disabled {
    background-color: transparent;
    border: 0;
    color: var(--btn-disabled-color);
    cursor: not-allowed;
  }

  &:focus {
    outline: 0 none;
    outline-offset: 0;
    box-shadow: 0 0 0 0.2rem var(--btn-focus-border-color);
  }
}

.p-button {
  color: var(--btn-primary-color);
  background: var(--btn-primary-bg);
  border: 1px solid var(--btn-primary-bg);
  padding: var(--spacing-xs) var(--spacing-md);
  line-height: 1rem;
  margin: var(--spacing-xxs);

  &.p-button-sm {
    font-size: var(--font-size-sm);
    padding: var(--spacing-xs) var(--spacing-sm);
  }
}
</style>
