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

          <div class="button-container">
            <QualityAssuranceButtons
              v-if="isReviewableByCurrentUser && !!singleDataMetaInfoToDisplay"
              :meta-info="singleDataMetaInfoToDisplay"
              :company-name="fetchedCompanyInformation.companyName"
            />

            <PrimeButton
              aria-label="Download data"
              v-if="!getAllPrivateFrameworkIdentifiers().includes(dataType)"
              @click="downloadData()"
              data-test="downloadDataButton"
              label="Download Data"
              icon="pi pi-download"
            />

            <PrimeButton
              v-if="isEditableByCurrentUser"
              aria-label="Edit data"
              @click="editDataset"
              data-test="editDatasetButton"
              label="Edit Data"
              :icon="
                availableReportingPeriods.length > 1 && !singleDataMetaInfoToDisplay
                  ? 'pi pi-chevron-down'
                  : 'pi pi-pencil'
              "
              :icon-pos="availableReportingPeriods.length > 1 && !singleDataMetaInfoToDisplay ? 'right' : 'left'"
            />
            <router-link
              v-if="hasUserUploaderRights"
              :to="targetLinkForAddingNewDataset"
              data-test="gotoNewDatasetButton"
            >
              <PrimeButton aria-label="New Dataset" icon="pi pi-plus" label="New Dataset" />
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

<script setup lang="ts">
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation.ts';
import CompanyInfoSheet from '@/components/general/CompanyInfoSheet.vue';
import DownloadData from '@/components/general/DownloadData.vue';

import SimpleReportingPeriodSelectorDialog from '@/components/general/SimpleReportingPeriodSelectorDialog.vue';
import ChangeFrameworkDropdown from '@/components/generics/ChangeFrameworkDropdown.vue';
import TheContent from '@/components/generics/TheContent.vue';

import TheFooter from '@/components/generics/TheFooter.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import QualityAssuranceButtons from '@/components/resources/frameworkDataSearch/QualityAssuranceButtons.vue';
import MarginWrapper from '@/components/wrapper/MarginWrapper.vue';
import { getAllPrivateFrameworkIdentifiers } from '@/frameworks/BasePrivateFrameworkRegistry.ts';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils.ts';
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
import { type CompanyInformation, type DataMetaInformation, type DataTypeEnum, ExportFileType } from '@clients/backend';
import { CompanyRole } from '@clients/communitymanager';
import { AxiosError, type AxiosRequestConfig } from 'axios';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import ToggleSwitch from 'primevue/toggleswitch';
import OverlayPanel from 'primevue/overlaypanel';
import { computed, inject, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER } from '@/utils/Constants.ts';
import { forceFileDownload, groupReportingPeriodsPerFrameworkForCompany } from '@/utils/FileDownloadUtils.ts';
import { useDialog } from 'primevue/usedialog';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';

const props = defineProps<{
  companyID: string;
  dataType: DataTypeEnum;
  singleDataMetaInfoToDisplay?: DataMetaInformation;
  viewInPreviewMode?: boolean;
}>();

const emit = defineEmits(['updateActiveDataMetaInfoForChosenFramework']);
const router = useRouter();
const route = useRoute();
const dialog = useDialog();
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const fetchedCompanyInformation = ref<CompanyInformation>({} as CompanyInformation);
const dataMetaInformation = ref<DataMetaInformation[]>([]);
const activeDataForCurrentCompanyAndFramework = ref<Array<DataAndMetaInformation<FrameworkData>>>([]);
const chosenDataTypeInDropdown = ref(props.dataType ?? '');
const isDataProcessedSuccessfully = ref(false);
const hideEmptyFields = ref(true);
const hasUserUploaderRights = ref(false);
const hasUserReviewerRights = ref(false);
const dataId = ref(route.params.dataId);
const reportingPeriodsOverlayPanel = ref();
const isDownloading = ref(false);
const downloadErrors = ref('');

const mapOfReportingPeriodToActiveDataset = computed(() => {
  const map = new Map<string, DataMetaInformation>();
  for (const d of activeDataForCurrentCompanyAndFramework.value) {
    map.set(d.metaInfo.reportingPeriod, d.metaInfo);
  }
  return map;
});

provide('hideEmptyFields', hideEmptyFields);
provide('mapOfReportingPeriodToActiveDataset', mapOfReportingPeriodToActiveDataset);

const availableReportingPeriods = computed(() => {
  const set = new Set<string>();
  activeDataForCurrentCompanyAndFramework.value.forEach((item) => {
    if (item.metaInfo.dataType === chosenDataTypeInDropdown.value) {
      set.add(item.metaInfo.reportingPeriod);
    }
  });
  return Array.from(set).sort();
});

const isReviewableByCurrentUser = computed(
  () => hasUserReviewerRights.value && props.singleDataMetaInfoToDisplay?.qaStatus === 'Pending'
);

const isEditableByCurrentUser = computed(
  () =>
    hasUserUploaderRights.value &&
    isFrameworkEditable(props.dataType) &&
    (!props.singleDataMetaInfoToDisplay ||
      props.singleDataMetaInfoToDisplay.currentlyActive ||
      props.singleDataMetaInfoToDisplay.qaStatus === 'Rejected')
);

const targetLinkForAddingNewDataset = computed(() => `/companies/${props.companyID}/frameworks/upload`);
const reportingPeriodsPerFramework = computed(() =>
  groupReportingPeriodsPerFrameworkForCompany(
    dataMetaInformation.value.map((meta) => ({
      metaInfo: { dataType: meta.dataType, reportingPeriod: meta.reportingPeriod },
    }))
  )
);

watch(
  () => props.companyID,
  () => {
    void (async (): Promise<void> => {
      try {
        await getMetaData();
        await getAllActiveDataForCurrentCompanyAndFramework();
      } catch (error) {
        console.error('Error watching companyID:', error);
      }
    })();
  }
);

watch(
  () => props.dataType,
  (newVal) => {
    chosenDataTypeInDropdown.value = newVal;
    void (async (): Promise<void> => {
      try {
        await getAllActiveDataForCurrentCompanyAndFramework();
      } catch (error) {
        console.error('Error watching dataType:', error);
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
  if (dataId.value) {
    await getMetaData();
    setActiveDataForCurrentCompanyAndFramework();
  } else {
    await getMetaData();
    await getAllActiveDataForCurrentCompanyAndFramework();
  }
  await setViewPageAttributesForUser();
});

/**
 * Retrieves all data meta data available for current company
 */
async function getMetaData(): Promise<void> {
  try {
    const api = new ApiClientProvider(assertDefined(getKeycloakPromise)()).backendClients.metaDataController;
    const response = await api.getListOfDataMetaInfo(props.companyID);
    dataMetaInformation.value = response.data;
  } catch (err) {
    isDataProcessedSuccessfully.value = false;
    console.error(err);
  }
}

/**
 * For public datasets, retrieves all active DataAndMetaInformation for current datatype and companyID. Then, the
 * mapOfReportingPeriodToActiveDataset is populated with this information (computed property).
 * For private datasets, the call to getAllCompanyData may lead to 403 if user doesn't have sufficient rights.
 * Instead, the metaData endpoint is called and the activeDataForCurrentCompanyAndFramework property is manually
 * filled with retrieved metaData and empty data object.
 */
async function getAllActiveDataForCurrentCompanyAndFramework(): Promise<void> {
  try {
    const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
    const frameworkDataApi = getFrameworkDataApiForIdentifier(props.dataType, apiClientProvider);

    const response = await frameworkDataApi?.getAllCompanyData(props.companyID, true);
    activeDataForCurrentCompanyAndFramework.value = Array.from(response!.data);
    isDataProcessedSuccessfully.value = true;
    emit('updateActiveDataMetaInfoForChosenFramework', mapOfReportingPeriodToActiveDataset.value);
  } catch (error) {
    if (error instanceof AxiosError && error?.status === 403 && props.dataType === 'vsme') {
      await getMetaData();
      setActiveDataForCurrentCompanyAndFramework();
    } else {
      isDataProcessedSuccessfully.value = false;
      console.error(error);
    }
  }
}

/**
 * Get available metaData in case of either insufficient rights.
 */
function setActiveDataForCurrentCompanyAndFramework(): void {
  if (dataMetaInformation.value) {
    activeDataForCurrentCompanyAndFramework.value = dataMetaInformation.value.map((meta) => ({
      metaInfo: meta,
      data: {},
    }));
    isDataProcessedSuccessfully.value = true;
  } else {
    isDataProcessedSuccessfully.value = false;
  }
}

/**
 * Set if the user is allowed to upload data for the current company
 * @returns a promise that resolves to void, so the successful execution of the function can be awaited
 */
async function setViewPageAttributesForUser(): Promise<void> {
  hasUserReviewerRights.value = await checkIfUserHasRole(KEYCLOAK_ROLE_REVIEWER, getKeycloakPromise);
  hasUserUploaderRights.value = await checkIfUserHasRole(KEYCLOAK_ROLE_UPLOADER, getKeycloakPromise);

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
 * Triggered on click on Edit button. In singleDatasetView, it triggers call to upload page with templateDataId. In
 * datasetOverview with only one dataset available, it triggers call to upload page with reportingPeriod.
 * In datasetOverview with multiple datasets available, a modal is opened to choose reportingPeriod to edit.
 * @param event event
 */
async function editDataset(event: Event): Promise<void> {
  if (props.singleDataMetaInfoToDisplay) {
    await goToUpdateFormByDataId(props.singleDataMetaInfoToDisplay.dataId);
  } else if (availableReportingPeriods.value.length > 1) {
    reportingPeriodsOverlayPanel.value?.toggle(event);
  } else if (availableReportingPeriods.value.length === 1) {
    await goToUpdateFormByReportingPeriod(availableReportingPeriods.value[0]);
  }
}

/**
 * Navigates to the data update form by using templateDataId
 * @param dataId dataId
 */
async function goToUpdateFormByDataId(dataId: string): Promise<void> {
  await router.push(`/companies/${props.companyID}/frameworks/${props.dataType}/upload?templateDataId=${dataId}`);
}

/**
 * Navigates to the data update form by using reportingPeriod
 * @param reportingPeriod reporting period
 */
async function goToUpdateFormByReportingPeriod(reportingPeriod: string): Promise<void> {
  await router.push(
    `/companies/${props.companyID}/frameworks/${props.dataType}/upload?reportingPeriod=${reportingPeriod}`
  );
}

/**
 * Download the dataset from the selected reporting period as a file in the selected format
 * @param selectedYears selected reporting year
 * @param selectedFileType selected export file type
 * @param selectedFramework selected data type
 * @param keepValuesOnly selected export of values only
 * @param includeAlias selected type of field names
 */
async function handleDatasetDownload(
  selectedYears: string[],
  selectedFileType: string,
  selectedFramework: DataTypeEnum,
  keepValuesOnly: boolean,
  includeAlias: boolean
): Promise<void> {
  isDownloading.value = true;
  try {
    const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
    // DataExport Button does not exist for private frameworks, so cast is safe
    const frameworkDataApi: PublicFrameworkDataApi<FrameworkData> | null = getFrameworkDataApiForIdentifier(
      selectedFramework,
      apiClientProvider
    ) as PublicFrameworkDataApi<FrameworkData>;

    const exportFileType = Object.values(ExportFileType).find((t) => t.toString() === selectedFileType);
    if (!exportFileType) throw new Error('ExportFileType undefined.');

    const fileExtension = ExportFileTypeInformation[exportFileType].fileExtension;
    const options: AxiosRequestConfig | undefined =
      fileExtension === 'xlsx' ? { responseType: 'arraybuffer' } : undefined;

    const label = ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER.find((f) => f === humanizeStringOrNumber(selectedFramework));
    const filename = `data-export-${label ?? humanizeStringOrNumber(selectedFramework)}-${getDateStringForDataExport(new Date())}.${fileExtension}`;

    const response = await frameworkDataApi.exportCompanyAssociatedDataByDimensions(
      selectedYears,
      [props.companyID],
      exportFileType,
      keepValuesOnly,
      includeAlias,
      options
    );

    const content = exportFileType === 'JSON' ? JSON.stringify(response.data) : response.data;
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
 * Opens the PortfolioDownload with the current portfolio's data for downloading.
 * Once the dialog is closed, it reloads the portfolio data and shows the portfolio overview again.
 */
function downloadData(): void {
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

.d-letters {
  letter-spacing: 0.05em;
}

.vertical-middle {
  display: flex;
  align-items: center;
}
</style>
