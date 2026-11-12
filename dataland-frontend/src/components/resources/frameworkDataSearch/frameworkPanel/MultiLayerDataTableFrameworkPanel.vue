<template>
  <div v-if="status == 'LoadingDatasets'" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading {{ frameworkDisplayName }} Data...</p>
    <DatalandProgressSpinner />
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
        frameworkIdentifier == DataTypeEnum.EutaxonomyFinancials202673 ||
        frameworkIdentifier == DataTypeEnum.EutaxonomyNonFinancials ||
        frameworkIdentifier == DataTypeEnum.EutaxonomyNonFinancials202673 ||
        frameworkIdentifier == DataTypeEnum.Sfdr ||
        frameworkIdentifier == DataTypeEnum.NuclearAndGas
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
      :hideEmptyFields="hideEmptyFields ?? false"
    />
  </div>
  <div v-if="status == 'InsufficientRights'">
    <h1>Sorry! You have insufficient rights to view the available datasets.</h1>
  </div>
  <div v-if="status == 'Error'">
    <h1>We are having issues loading the data.</h1>
  </div>
</template>

<script setup generic="FrameworkDataType" lang="ts">
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import MultiLayerDataTable from '@/components/resources/dataTable/MultiLayerDataTable.vue';
import ShowMultipleReportsBanner from '@/components/resources/frameworkDataSearch/ShowMultipleReportsBanner.vue';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { computed, inject } from 'vue';
import { type MLDTConfig } from '@/components/resources/dataTable/MultiLayerDataTableConfiguration';
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation';
import { sortDatasetsByReportingPeriod } from '@/utils/DataTableDisplay';
import {
  type CompanyReport,
  type DataMetaInformation,
  DataTypeEnum,
  type EutaxonomyFinancials202673Data,
  type EutaxonomyFinancialsData,
  type EutaxonomyNonFinancials202673Data,
  type EutaxonomyNonFinancialsData,
  type NuclearAndGasData,
  type SfdrData,
} from '@clients/backend';
import { editMultiLayerDataTableConfigForHighlightingHiddenFields } from '@/components/resources/frameworkDataSearch/frameworkPanel/MultiLayerDataTableQaHighlighter';
import { AxiosError } from 'axios';
import { useGetAllCompanyFrameworkDataQuery } from '@/api-queries/backend/framework-data/useGetAllCompanyFrameworkDataQuery';
import { useGetFrameworkDataQuery } from '@/api-queries/backend/framework-data/useGetFrameworkDataQuery';
import { useGetCompanyAssociatedDataByDimensionsQuery } from '@/api-queries/backend/framework-data/useGetCompanyAssociatedDataByDimensionsQuery';

type ViewPanelStates = 'LoadingDatasets' | 'DisplayingDatasets' | 'Error' | 'InsufficientRights';

const injectHideEmptyFields = inject<{ value: boolean }>('hideEmptyFields');

const props = defineProps<{
  companyId: string;
  singleDataMetaInfoToDisplay?: DataMetaInformation;
  frameworkIdentifier: string;
  displayConfiguration: MLDTConfig<FrameworkDataType>;
  inReviewMode: boolean;
}>();

const hideEmptyFields = computed<boolean | undefined>(() => injectHideEmptyFields?.value);
const frameworkDisplayName = computed(() => humanizeStringOrNumber(props.frameworkIdentifier));
const sortedDataAndMetaInfo = computed(() => {
  return sortDatasetsByReportingPeriod([...rawDataAndMetaInfoForDisplay.value]);
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
    case DataTypeEnum.EutaxonomyNonFinancials202673: {
      return sortedDataAndMetaInfo.value
        .map(
          (singleDataAndMetaInfo) =>
            (singleDataAndMetaInfo.data as EutaxonomyNonFinancials202673Data).general?.referencedReports
        )
        .filter((reports): reports is { [key: string]: CompanyReport } => reports !== null && reports !== undefined);
    }
    case DataTypeEnum.EutaxonomyFinancials: {
      return sortedDataAndMetaInfo.value
        .map(
          (singleDataAndMetaInfo) =>
            (singleDataAndMetaInfo.data as EutaxonomyFinancialsData).general?.general?.referencedReports
        )
        .filter((reports): reports is { [key: string]: CompanyReport } => reports !== null && reports !== undefined);
    }
    case DataTypeEnum.EutaxonomyFinancials202673: {
      return sortedDataAndMetaInfo.value
        .map(
          (singleDataAndMetaInfo) =>
            (singleDataAndMetaInfo.data as EutaxonomyFinancials202673Data).general?.general?.referencedReports
        )
        .filter((reports): reports is { [key: string]: CompanyReport } => reports !== null && reports !== undefined);
    }
    case DataTypeEnum.Sfdr: {
      return sortedDataAndMetaInfo.value
        .map((singleDataAndMetaInfo) => (singleDataAndMetaInfo.data as SfdrData).general?.general?.referencedReports)
        .filter((reports): reports is { [key: string]: CompanyReport } => reports !== null && reports !== undefined);
    }
    case DataTypeEnum.NuclearAndGas: {
      return sortedDataAndMetaInfo.value
        .map(
          (singleDataAndMetaInfo) =>
            (singleDataAndMetaInfo.data as NuclearAndGasData).general?.general?.referencedReports
        )
        .filter((reports): reports is { [key: string]: CompanyReport } => reports !== null && reports !== undefined);
    }
    default: {
      return []; //Since other frameworks don't have referenced reports and therefore banners, reports don't need
      // to be added and the banner will never receive "null" as an input
    }
  }
});

const singleMetaInfo = computed(() => props.singleDataMetaInfoToDisplay);
const isSingleMode = computed(() => !!singleMetaInfo.value);

const allDataQuery = useGetAllCompanyFrameworkDataQuery(
  { framework: () => props.frameworkIdentifier, companyId: () => props.companyId },
  { enabled: computed(() => !isSingleMode.value && !!props.companyId) }
);

const singleByIdQuery = useGetFrameworkDataQuery(
  {
    framework: computed(() => props.frameworkIdentifier as DataTypeEnum),
    dataId: computed(() => (isSingleMode.value ? singleMetaInfo.value?.dataId : undefined)),
  },
  { retry: false }
);

// Falls back to the reporting period if the dataset cannot be fetched via its id.
const singleByDimensionsQuery = useGetCompanyAssociatedDataByDimensionsQuery(
  {
    framework: () => props.frameworkIdentifier,
    companyId: () => singleMetaInfo.value?.companyId,
    reportingPeriod: () => singleMetaInfo.value?.reportingPeriod,
  },
  {
    enabled: computed(() => isSingleMode.value && (!singleMetaInfo.value?.dataId || singleByIdQuery.isError.value)),
  }
);

const activeSingleQuery = computed(() =>
  !singleMetaInfo.value?.dataId || singleByIdQuery.isError.value ? singleByDimensionsQuery : singleByIdQuery
);

const rawDataAndMetaInfoForDisplay = computed<DataAndMetaInformation<FrameworkDataType>[]>(() => {
  if (isSingleMode.value) {
    const data = activeSingleQuery.value.data.value?.data;
    return data && singleMetaInfo.value ? [{ metaInfo: singleMetaInfo.value, data: data as FrameworkDataType }] : [];
  }
  return (allDataQuery.data.value ?? []) as DataAndMetaInformation<FrameworkDataType>[];
});

const status = computed<ViewPanelStates>(() => {
  const query = isSingleMode.value ? activeSingleQuery.value : allDataQuery;
  if (query.data.value !== undefined) return 'DisplayingDatasets';
  if (query.isError.value && !(isSingleMode.value && query === singleByIdQuery)) {
    const error = query.error.value;
    return error instanceof AxiosError && error.response?.status == 403 ? 'InsufficientRights' : 'Error';
  }
  return 'LoadingDatasets';
});
</script>
<style scoped>
.d-center-div {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: white;
}
</style>
