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
              :availableReportingPeriods="availableReportingPeriods"
              @closeDownloadModal="onCloseDownloadModal"
              @downloadDataset="handleDatasetDownload"
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

<script setup lang="ts">
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
import { ApiClientProvider } from '@/services/ApiClients';
import { ExportFileTypeInformation } from '@/types/ExportFileTypeInformation.ts';
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
import InputSwitch from 'primevue/inputswitch';
import OverlayPanel from 'primevue/overlaypanel';
import { computed, inject, onMounted, provide, watch, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER } from '@/utils/Constants.ts';
import { forceFileDownload } from '@/utils/FileDownloadUtils.ts';
import type { PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi.ts';

// Props
const props = defineProps<{
  companyID: string;
  dataType: DataTypeEnum;
  singleDataMetaInfoToDisplay?: DataMetaInformation;
  viewInPreviewMode?: boolean;
}>();

const emit = defineEmits(['updateActiveDataMetaInfoForChosenFramework']);
const router = useRouter();
const route = useRoute();
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const reportingPeriodsOverlayPanel = ref();

const fetchedCompanyInformation = ref<CompanyInformation>({} as CompanyInformation);
const dataMetaInformation = ref<DataMetaInformation[]>([]);
const activeDataForCurrentCompanyAndFramework = ref<Array<DataAndMetaInformation<FrameworkData>>>([]);
const chosenDataTypeInDropdown = ref(props.dataType ?? '');
const isDownloadModalOpen = ref(false);
const isDataProcessedSuccessfully = ref(false);
const hideEmptyFields = ref(true);
const hasUserUploaderRights = ref(false);
const hasUserReviewerRights = ref(false);
const dataId = ref(route.params.dataId);

provide(
  'hideEmptyFields',
  computed(() => hideEmptyFields.value)
);
provide(
  'mapOfReportingPeriodToActiveDataset',
  computed(() => mapOfReportingPeriodToActiveDataset.value)
);

// Computed
const mapOfReportingPeriodToActiveDataset = computed(() => {
  const map = new Map<string, DataMetaInformation>();
  for (const d of activeDataForCurrentCompanyAndFramework.value) {
    map.set(d.metaInfo.reportingPeriod, d.metaInfo);
  }
  return map;
});

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

/**
 *
 */
const targetLinkForAddingNewDataset = computed(() => `/companies/${props.companyID}/frameworks/upload`);

// Watchers
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

// Lifecycle
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

// Functions
/**
 *
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
 *
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
 *
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
 *
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
 *
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
 *
 * @param dataId
 */
async function goToUpdateFormByDataId(dataId: string): Promise<void> {
  await router.push(`/companies/${props.companyID}/frameworks/${props.dataType}/upload?templateDataId=${dataId}`);
}

/**
 *
 * @param reportingPeriod
 */
async function goToUpdateFormByReportingPeriod(reportingPeriod: string): Promise<void> {
  await router.push(
    `/companies/${props.companyID}/frameworks/${props.dataType}/upload?reportingPeriod=${reportingPeriod}`
  );
}

/**
 *
 */
function onCloseDownloadModal(): void {
  isDownloadModalOpen.value = false;
}

/**
 *
 * @param selectedYears
 * @param selectedFileType
 * @param keepValuesOnly
 * @param includeAlias
 */
async function handleDatasetDownload(
  selectedYears: string[],
  selectedFileType: string,
  keepValuesOnly: boolean,
  includeAlias: boolean
): Promise<void> {
  try {
    const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
    const frameworkDataApi = getFrameworkDataApiForIdentifier(
      props.dataType,
      apiClientProvider
    ) as PublicFrameworkDataApi<FrameworkData>;

    const exportFileType = Object.values(ExportFileType).find((t) => t.toString() === selectedFileType);
    if (!exportFileType) throw new Error('ExportFileType undefined.');

    const fileExtension = ExportFileTypeInformation[exportFileType].fileExtension;
    const options: AxiosRequestConfig | undefined =
      fileExtension === 'EXCEL' ? { responseType: 'arraybuffer' } : undefined;

    const label = ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER.find((f) => f === props.dataType);
    const filename = `data-export-${label ?? props.dataType}-${getDateStringForDataExport(new Date())}.${fileExtension}`;

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
    console.error(err);
  }
}

/**
 *
 * @param info
 */
function handleFetchedCompanyInformation(info: CompanyInformation): void {
  fetchedCompanyInformation.value = info;
}
</script>
