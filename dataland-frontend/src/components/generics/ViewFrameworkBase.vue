<template>
  <TheContent class="min-h-screen">
    <CompanyInfoSheet
      :company-id="companyID"
      @fetched-company-information="handleFetchedCompanyInformation"
      :show-single-data-request-button="true"
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
              :available-data-dimensions="availableDataDimensions"
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

          <div class="button-container">
            <PrimeButton
              v-if="isReviewableByCurrentUser && !!singleDataMetaInfoToDisplay"
              label="REJECT"
              data-test="qaRejectButton"
              icon="pi pi-times"
              variant="outlined"
              @click="setQaStatusTo('Rejected')"
            />

            <PrimeButton
              v-if="isReviewableByCurrentUser && !!singleDataMetaInfoToDisplay"
              label="APPROVE"
              data-test="qaApproveButton"
              icon="pi pi-check"
              @click="setQaStatusTo('Accepted')"
            />

            <PrimeButton
              v-if="isJudgeableByCurrentUser && !!singleDataMetaInfoToDisplay"
              :disabled="!datasetJudgementId"
              label="REVIEW PAGE"
              data-test="qaReviewPageButton"
              icon="pi pi-angle-double-right"
              @click="visitJudgementPage"
            />

            <PrimeButton
              @click="downloadData()"
              data-test="downloadDataButton"
              label="DOWNLOAD DATA"
              icon="pi pi-download"
            />

            <PrimeButton
              v-if="dataPointsAreEditableForCurrentUser"
              @click="editModeIsOn = !editModeIsOn"
              data-test="editDataPointsButton"
              :label="!editModeIsOn ? 'ENTER EDIT MODE' : 'LEAVE EDIT MODE'"
              :icon="'pi pi-pencil'"
              title="Enter Edit Mode to modify data points inline"
            />
            <PrimeButton
              v-if="hasUserUploaderRights"
              icon="pi pi-plus"
              label="NEW DATASET"
              data-test="goToNewDatasetButton"
              @click="linkToNewDataset"
              title="Upload a new dataset for any framework"
            />
          </div>
        </div>
      </MarginWrapper>
      <MarginWrapper style="margin-right: 0">
        <slot name="content" :inReviewMode="isReviewableByCurrentUser"></slot>
      </MarginWrapper>
    </div>
    <h1 v-else data-test="noDataCouldBeLoadedErrorIndicator">No data could be loaded.</h1>
  </TheContent>
</template>

<script setup lang="ts">
import CompanyInfoSheet from '@/components/general/CompanyInfoSheet.vue';
import DownloadData from '@/components/general/DownloadData.vue';

import ChangeFrameworkDropdown from '@/components/generics/ChangeFrameworkDropdown.vue';
import TheContent from '@/components/generics/TheContent.vue';
import { pollExportJobStatus, prepareDownloadFile } from '@/utils/ExportUtils.ts';

import MarginWrapper from '@/components/wrapper/MarginWrapper.vue';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils.ts';
import { ApiClientProvider } from '@/services/ApiClients';
import { ExportFileTypeInformation } from '@/types/ExportFileTypeInformation.ts';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi.ts';
import { hasUserCompanyRoleForCompany } from '@/utils/CompanyRolesUtils';
import { type FrameworkData } from '@/utils/GenericFrameworkTypes.ts';
import {
  KEYCLOAK_ROLE_ADMIN,
  KEYCLOAK_ROLE_JUDGE,
  KEYCLOAK_ROLE_REVIEWER,
  KEYCLOAK_ROLE_UPLOADER,
} from '@/utils/KeycloakRoles';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils';
import { assertDefined } from '@/utils/TypeScriptUtils';
import {
  type BasicDataDimensions,
  type CompanyInformation,
  type DataMetaInformation,
  type DataTypeEnum,
  ExportFileType,
  type QaStatus,
} from '@clients/backend';
import { ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER } from '@/utils/Constants';
import { CompanyRole } from '@clients/communitymanager';
import { type AxiosError, type AxiosRequestConfig } from 'axios';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import ToggleSwitch from 'primevue/toggleswitch';
import { computed, inject, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { forceFileDownload, groupReportingPeriodsPerFrameworkForCompany } from '@/utils/FileDownloadUtils.ts';
import { useDialog } from 'primevue/usedialog';
import QaDatasetModal from '@/components/general/QaDatasetModal.vue';

const props = defineProps<{
  companyID: string;
  dataType: DataTypeEnum;
  singleDataMetaInfoToDisplay?: DataMetaInformation;
}>();

const emit = defineEmits(['updateActiveDataMetaInfoForChosenFramework']);
const router = useRouter();
const route = useRoute();
const dialog = useDialog();
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const fetchedCompanyInformation = ref<CompanyInformation>({} as CompanyInformation);
const availableDataDimensions = ref<BasicDataDimensions[]>([]);
const isDataProcessedSuccessfully = ref(false);
const hideEmptyFields = ref(true);
const hasUserUploaderRights = ref(false);
const hasUserReviewerRights = ref(false);
const dataId = ref(route.params.dataId);
const isDownloading = ref(false);
const downloadErrors = ref('');
const editModeIsOn = ref(false);
const hasUserAdminRights = ref(false);
const hasUserJudgeRights = ref(false);
const datasetJudgementId = ref<string | undefined>(undefined);

/**
 * Map of reporting period to the corresponding active BasicDataDimensions for this company and framework.
 * Built from searchViewableDimensions (DataAvailabilityController) — the single source of truth for what data is
 * actually available. This covers non-assembled datasets, standalone data points, and assembled/calculated dimensions.
 */
const mapOfReportingPeriodToActiveDataset = computed(() => {
  const map = new Map<string, BasicDataDimensions>();
  for (const d of availableDataDimensions.value) {
    if (d.dataType === props.dataType) {
      map.set(d.reportingPeriod, d);
    }
  }
  return map;
});

provide('hideEmptyFields', hideEmptyFields);
provide('editModeIsOn', editModeIsOn);

const isReviewableByCurrentUser = computed(
  () => hasUserReviewerRights.value && props.singleDataMetaInfoToDisplay?.qaStatus === 'Pending'
);

const isJudgeableByCurrentUser = computed(
  () => hasUserJudgeRights.value && props.singleDataMetaInfoToDisplay?.qaStatus === 'Pending'
);

const isEditableByCurrentUser = computed(
  () =>
    hasUserUploaderRights.value &&
    (!props.singleDataMetaInfoToDisplay ||
      props.singleDataMetaInfoToDisplay.currentlyActive ||
      props.singleDataMetaInfoToDisplay.qaStatus === 'Rejected')
);

const reportingPeriodsPerFramework = computed(() =>
  groupReportingPeriodsPerFrameworkForCompany(
    availableDataDimensions.value.map((meta) => ({
      metaInfo: { dataType: meta.dataType, reportingPeriod: meta.reportingPeriod },
    }))
  )
);

const dataPointsAreEditableForCurrentUser = computed(() => isEditableByCurrentUser.value && hasUserAdminRights.value);

watch(
  () => props.companyID,
  () => {
    void (async (): Promise<void> => {
      try {
        await getMetaData();
      } catch (error) {
        console.error('Error watching companyID:', error);
      }
    })();
  }
);

watch(mapOfReportingPeriodToActiveDataset, (val) => {
  emit('updateActiveDataMetaInfoForChosenFramework', val);
});

watch(isReviewableByCurrentUser, () => {
  hideEmptyFields.value = !hasUserReviewerRights.value;
});

onMounted(async () => {
  await getMetaData();
  if (dataId.value) {
    await getDatasetJudgementId();
  }
  await setViewPageAttributesForUser();
});

/**
 * Navigates to the new dataset creation page
 */
function linkToNewDataset(): void {
  void router.push(`/companies/${props.companyID}/frameworks/upload`);
}

/**
 * Sets dataset quality status to the given status
 * @param qaStatus the QA status to be assigned
 */
function setQaStatusTo(qaStatus: QaStatus): void {
  const { dataId, dataType, reportingPeriod } = props.singleDataMetaInfoToDisplay || {};
  const message = `${qaStatus} ${dataType} data for ${fetchedCompanyInformation.value.companyName} for the reporting period ${reportingPeriod}.`;

  dialog.open(QaDatasetModal, {
    props: {
      header: qaStatus,
      modal: true,
      dismissableMask: false,
    },
    data: {
      dataId,
      qaStatus,
      message,
    },
    onClose: () => {
      void router.push('/qualityassurance');
    },
  });
}

/**
 * Retrieves available data dimensions for the current company via the DataAvailabilityController.
 * This is the single source of truth for what data exists: it covers non-assembled datasets,
 * standalone data points, and assembled/calculated dimensions.
 * Populates availableDataDimensions (used for ChangeFrameworkDropdown, DownloadData reporting periods,
 * and the active-reporting-period map emitted to the parent).
 * Sets isDataProcessedSuccessfully once the availability call completes.
 */
async function getMetaData(): Promise<void> {
  try {
    const api = new ApiClientProvider(assertDefined(getKeycloakPromise)()).backendClients.dataAvailabilityController;
    const response = await api.searchViewableDimensions({
      companyIds: [props.companyID],
      frameworksOrDataPointTypes: ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER,
      reportingPeriods: [],
    });
    availableDataDimensions.value = response.data;
    isDataProcessedSuccessfully.value = true;
  } catch (err) {
    isDataProcessedSuccessfully.value = false;
    console.error(err);
  }
}

/**
 * Set if the user is allowed to upload data for the current company
 * @returns a promise that resolves to void, so the successful execution of the function can be awaited
 */
async function setViewPageAttributesForUser(): Promise<void> {
  hasUserReviewerRights.value = await checkIfUserHasRole(KEYCLOAK_ROLE_REVIEWER, getKeycloakPromise);
  hasUserJudgeRights.value = await checkIfUserHasRole(KEYCLOAK_ROLE_JUDGE, getKeycloakPromise);
  hasUserUploaderRights.value = await checkIfUserHasRole(KEYCLOAK_ROLE_UPLOADER, getKeycloakPromise);
  hasUserAdminRights.value = await checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, getKeycloakPromise);

  if (!hasUserUploaderRights.value) {
    hasUserUploaderRights.value = await hasUserCompanyRoleForCompany(
      CompanyRole.CompanyOwner,
      props.companyID,
      getKeycloakPromise
    );
  }

  hideEmptyFields.value = !hasUserReviewerRights.value;
}

/**
 * Download the dataset from the selected reporting period as a file in the selected format
 * @param selectedYears selected reporting year
 * @param selectedFileType selected export file type
 * @param selectedFramework selected data type
 * @param keepValuesOnly selected export of values only
 * @param includeAlias selected type of field names
 * @param latestOnly whether to export only the latest reporting period per company
 */
async function handleDatasetDownload(
  selectedYears: string[],
  selectedFileType: string,
  selectedFramework: DataTypeEnum,
  keepValuesOnly: boolean,
  includeAlias: boolean,
  latestOnly: boolean
): Promise<void> {
  isDownloading.value = true;
  downloadErrors.value = '';
  try {
    const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
    // Cast is safe because all registered frameworks extend PublicFrameworkDataApi
    const frameworkDataApi: PublicFrameworkDataApi<FrameworkData> | null = getFrameworkDataApiForIdentifier(
      selectedFramework,
      apiClientProvider
    ) as PublicFrameworkDataApi<FrameworkData>;

    const exportFileType = Object.values(ExportFileType).find((t) => t.toString() === selectedFileType);
    if (!exportFileType) throw new Error('ExportFileType undefined.');

    let exportJobId: string;
    if (latestOnly) {
      exportJobId = (
        await frameworkDataApi.postExportLatestJobCompanyAssociatedDataByDimensions(
          [props.companyID],
          exportFileType,
          keepValuesOnly,
          includeAlias
        )
      ).data.id;
    } else {
      exportJobId = (
        await frameworkDataApi.postExportJobCompanyAssociatedDataByDimensions(
          selectedYears,
          [props.companyID],
          exportFileType,
          keepValuesOnly,
          includeAlias
        )
      ).data.id;
    }

    await pollExportJobStatus(exportJobId, apiClientProvider.apiClients.dataExportController);

    const fileExtension = ExportFileTypeInformation[exportFileType].fileExtension;
    const options: AxiosRequestConfig | undefined =
      fileExtension === 'xlsx' ? { responseType: 'arraybuffer' } : undefined;

    const response = await apiClientProvider.apiClients.dataExportController.exportCompanyAssociatedDataById(
      exportJobId,
      options
    );
    const { filename, content } = prepareDownloadFile(exportFileType, selectedFramework, response.data);

    forceFileDownload(content, filename);
  } catch (err) {
    downloadErrors.value = `${(err as AxiosError).message}`;
    console.error(err);
  } finally {
    isDownloading.value = false;
  }
}

/**
 * Saves the company information emitted by the CompanyInformation vue components event.
 * @param info the company information for the current companyID
 */
function handleFetchedCompanyInformation(info: CompanyInformation): void {
  fetchedCompanyInformation.value = info;
}

/**
 * Retrieves the dataset judgement id for the dataset in review and saves it in the datasetJudgementId ref.
 * This is needed to navigate to the review page for the dataset in review, which requires the dataset judgement id in the url.
 */
async function getDatasetJudgementId(): Promise<void> {
  try {
    const routeDataId = route.params.dataId as string | undefined;
    if (routeDataId) {
      const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
      const response =
        await apiClientProvider.apiClients.datasetJudgementController.getDatasetJudgementsByDatasetId(routeDataId);
      datasetJudgementId.value = response.data[0]?.dataSetJudgementId;
    }
  } catch (error) {
    console.error('Error getting dataset judgement id:', error);
    return;
  }
}

/**
 * Navigates to the judgement page for the dataset in judgement.
 */
async function visitJudgementPage(): Promise<void> {
  try {
    await router.push(`/qualityassurance/review/${datasetJudgementId.value}`);
  } catch (error) {
    console.error('Error navigating to judgement page:', error);
  }
}

/**
 * Opens the PortfolioDownload with the current portfolio's data for downloading.
 * Once the dialog is closed, it reloads the portfolio data and shows the portfolio overview again.
 */
function downloadData(): void {
  downloadErrors.value = '';
  const fullName = 'Download Data';

  dialog.open(DownloadData, {
    props: {
      modal: true,
      header: fullName,
      pt: {
        title: {
          style: {
            maxWidth: '15em',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
          },
        },
      },
    },
    data: {
      reportingPeriodsPerFramework: reportingPeriodsPerFramework,
      isDownloading: isDownloading,
      downloadErrors: downloadErrors,
    },
    emits: {
      onDownloadDataset: handleDatasetDownload,
    },
  });
}
</script>
<style scoped>
.button-container {
  display: flex;
  gap: var(--spacing-sm);
}

.vertical-middle {
  display: flex;
  align-items: center;
}
</style>
