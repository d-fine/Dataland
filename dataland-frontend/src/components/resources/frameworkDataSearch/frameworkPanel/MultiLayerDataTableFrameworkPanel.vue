<template>
  <div v-if="status == 'LoadingDatasets'" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading {{ frameworkDisplayName }} Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-show="status == 'DisplayingDatasets'">
    <p v-if="inReviewMode">
      You are viewing this page in review mode. Therefore, <b>all</b> possible fields are displayed even if they are not
      going to be visible in the final view page. Normally hidden fields are highlighted (start with a
      <i class="pi pi-eye-slash pl-1 text-red-500" aria-hidden="true" />) and should be empty.
    </p>
    <ShowMultipleReportsBanner
      data-test="multipleReportsBanner"
      v-if="
        frameworkIdentifier == DataTypeEnum.EutaxonomyFinancials ||
        frameworkIdentifier == DataTypeEnum.EutaxonomyNonFinancials ||
        frameworkIdentifier == DataTypeEnum.Sfdr
      "
      :reporting-periods="sortedReportingPeriods"
      :reports="sortedReports"
    />
    <MultiLayerDataTable
      :dataAndMetaInfo="sortedDataAndMetaInfo"
      :inReviewMode="inReviewMode"
      :config="
        editMultiLayerDataTableConfigForHighlightingHiddenFields(
          displayConfiguration,
          inReviewMode,
          hideEmptyFields ?? false
        )
      "
      :ariaLabel="`Datasets of the ${frameworkDisplayName} framework`"
    />
  </div>
  <div v-if="status == 'InsufficientRights'">
    <h1>Sorry! You have insufficient rights to view this resource.</h1>
  </div>
  <div v-if="status == 'Error'">
    <h1>We are having issues loading the data.</h1>
  </div>
</template>

<script setup generic="FrameworkDataType" lang="ts">
import MultiLayerDataTable from '@/components/resources/dataTable/MultiLayerDataTable.vue';
import ShowMultipleReportsBanner from '@/components/resources/frameworkDataSearch/ShowMultipleReportsBanner.vue';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { computed, inject, ref, shallowRef, watch } from 'vue';
import { type MLDTConfig } from '@/components/resources/dataTable/MultiLayerDataTableConfiguration';
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation';
import { sortDatasetsByReportingPeriod } from '@/utils/DataTableDisplay';
import {
  type CompanyReport,
  type DataMetaInformation,
  DataTypeEnum,
  type EuTaxonomyDataForFinancials,
  type EutaxonomyNonFinancialsData,
  type SfdrData,
} from '@clients/backend';
import type Keycloak from 'keycloak-js';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { editMultiLayerDataTableConfigForHighlightingHiddenFields } from '@/components/resources/frameworkDataSearch/frameworkPanel/MultiLayerDataTableQaHighlighter';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils';
import { type BaseFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import { AxiosError } from 'axios';

type ViewPanelStates = 'LoadingDatasets' | 'DisplayingDatasets' | 'Error' | 'InsufficientRights';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const props = defineProps<{
  companyId: string;
  singleDataMetaInfoToDisplay?: DataMetaInformation;
  frameworkIdentifier: string;
  displayConfiguration: MLDTConfig<FrameworkDataType>;
  inReviewMode: boolean;
}>();
const injecHideEmptyFields = inject<{ value: boolean }>('hideEmptyFields');
const hideEmptyFields = computed<boolean | undefined>(() => injecHideEmptyFields?.value);

const frameworkDisplayName = computed(() => humanizeStringOrNumber(props.frameworkIdentifier));

const sortedDataAndMetaInfo = computed(() => {
  return sortDatasetsByReportingPeriod(rawDataAndMetaInfoForDisplay.value);
});

const sortedReportingPeriods = computed(() => {
  return sortedDataAndMetaInfo.value.map((singleDataAndMetaInfo) => singleDataAndMetaInfo.metaInfo.reportingPeriod);
});

const sortedReports = computed(() => {
  switch (props.frameworkIdentifier) {
    case DataTypeEnum.EutaxonomyNonFinancials: {
      return sortedDataAndMetaInfo.value
        .map(
          (singleDataAndMetaInfo) =>
            (singleDataAndMetaInfo.data as EutaxonomyNonFinancialsData).general?.referencedReports
        )
        .filter((reports): reports is { [key: string]: CompanyReport } => reports !== null && reports !== undefined);
    }
    case DataTypeEnum.EutaxonomyFinancials: {
      return sortedDataAndMetaInfo.value
        .map((singleDataAndMetaInfo) => (singleDataAndMetaInfo.data as EuTaxonomyDataForFinancials).referencedReports)
        .filter((reports): reports is { [key: string]: CompanyReport } => reports !== null && reports !== undefined);
    }
    case DataTypeEnum.Sfdr: {
      return sortedDataAndMetaInfo.value
        .map((singleDataAndMetaInfo) => (singleDataAndMetaInfo.data as SfdrData).general?.general.referencedReports)
        .filter((reports): reports is { [key: string]: CompanyReport } => reports !== null && reports !== undefined);
    }
    default: {
      return []; //Since other frameworks don't have referenced reports and therefore banners, reports don't need
      // to be added and the banner will never receive "null" as an input
    }
  }
});

const updateCounter = ref(0);
const status = ref<ViewPanelStates>('LoadingDatasets');
const rawDataAndMetaInfoForDisplay = shallowRef<DataAndMetaInformation<FrameworkDataType>[]>([]);

watch(
  [
    (): string => props.companyId,
    (): string => props.frameworkIdentifier,
    (): DataMetaInformation | undefined => props.singleDataMetaInfoToDisplay,
  ],

  async () => reloadDisplayData(++updateCounter.value),
  { immediate: true }
);

/**
 * Triggers a reload of the information to be displayed with concurrency conflict handling.
 * @param currentCounter the value of the request counter at call-time.
 */
async function reloadDisplayData(currentCounter: number): Promise<void> {
  status.value = 'LoadingDatasets';
  try {
    const fetchedDataAndMetaInfo = await loadDataForDisplay(props.companyId, props.singleDataMetaInfoToDisplay);
    if (updateCounter.value == currentCounter) {
      rawDataAndMetaInfoForDisplay.value = fetchedDataAndMetaInfo;
      status.value = 'DisplayingDatasets';
    }
  } catch (err) {
    console.error(err);
    if (updateCounter.value == currentCounter) {
      if (err instanceof AxiosError && err.response?.status == 403) {
        status.value = 'InsufficientRights';
      } else {
        status.value = 'Error';
      }
    }
  }
}

/**
 * Fetches all datasets that should be displayed
 * @param companyId the id of the company to retrieve data for
 * @param singleDataMetaInfoToDisplay If set, only display the dataset belonging to this single entry
 * @returns the datasets that should be displayed
 */
async function loadDataForDisplay(
  companyId: string,
  singleDataMetaInfoToDisplay?: DataMetaInformation
): Promise<DataAndMetaInformation<FrameworkDataType>[]> {
  const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
  const dataControllerApi = getFrameworkDataApiForIdentifier(props.frameworkIdentifier, apiClientProvider) as
    | BaseFrameworkDataApi<FrameworkDataType>
    | undefined;
  if (dataControllerApi) {
    if (singleDataMetaInfoToDisplay) {
      const singleDataset = (await dataControllerApi.getFrameworkData(singleDataMetaInfoToDisplay.dataId)).data.data;
      return [{ metaInfo: singleDataMetaInfoToDisplay, data: singleDataset }];
    } else {
      return (await dataControllerApi.getAllCompanyData(assertDefined(companyId))).data;
    }
  } else {
    throw new Error(`No data controller found for framework ${props.frameworkIdentifier}`);
  }
}
</script>
