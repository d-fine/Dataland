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
        frameworkIdentifier == DataTypeEnum.EutaxonomyNonFinancials
      "
      :reporting-periods="sortedReportingPeriods"
      :reports="sortedReports"
    />
    <MultiLayerDataTable
      :mldtDatasets="mldtDatasets"
      :inReviewMode="inReviewMode"
      :config="
        editMultiLayerDataTableConfigForHighlightingHiddenFields(
          displayConfiguration,
          inReviewMode,
          <boolean>hideEmptyFields,
        )
      "
      :ariaLabel="`Datasets of the ${frameworkDisplayName} framework`"
    />
  </div>
  <div v-if="status == 'Error'">
    <h1>We are having issues loading the data.</h1>
  </div>
</template>

<script setup generic="FrameworkDataType" lang="ts">
import MultiLayerDataTable from "@/components/resources/dataTable/MultiLayerDataTable.vue";
import ShowMultipleReportsBanner from "@/components/resources/frameworkDataSearch/ShowMultipleReportsBanner.vue";
import { humanizeStringOrNumber } from "@/utils/StringHumanizer";
import { computed, inject, ref, shallowRef, watch } from "vue";
import { type MLDTConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import { type DataAndMetaInformation } from "@/api-models/DataAndMetaInformation";
import { sortDatasetsByReportingPeriod } from "@/utils/DataTableDisplay";
import {
  type DataMetaInformation,
  DataTypeEnum,
  type EuTaxonomyDataForFinancials,
  type EuTaxonomyDataForNonFinancials,
} from "@clients/backend";
import type Keycloak from "keycloak-js";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { editMultiLayerDataTableConfigForHighlightingHiddenFields } from "@/components/resources/frameworkDataSearch/frameworkPanel/MultiLayerDataTableQaHighlighter";
import { getFrontendFrameworkDefinition } from "@/frameworks/FrontendFrameworkRegistry";
import { type FrameworkDataApi } from "@/utils/api/UnifiedFrameworkDataApi";
import { type FrontendFrameworkDefinition } from "@/frameworks/FrameworkDefinition";

type ViewPanelStates = "LoadingDatasets" | "DisplayingDatasets" | "Error";

const getKeycloakPromise = inject<() => Promise<Keycloak>>("getKeycloakPromise");

const props = defineProps<{
  companyId: string;
  singleDataMetaInfoToDisplay?: DataMetaInformation;
  frameworkIdentifier: string;
  displayConfiguration: MLDTConfig<FrameworkDataType>;
  inReviewMode: boolean;
}>();
const injecHideEmptyFields = inject<{ value: boolean }>("hideEmptyFields");
const hideEmptyFields = computed<boolean | undefined>(() => injecHideEmptyFields?.value);

const frameworkDisplayName = computed(() => humanizeStringOrNumber(props.frameworkIdentifier));

const mldtDatasets = computed(() => {
  const sortedDataAndMetaInformation = sortDatasetsByReportingPeriod(dataAndMetaInformationForDisplay.value);
  return sortedDataAndMetaInformation.map((singleDataSet) => ({
    headerLabel: singleDataSet.metaInfo.reportingPeriod,
    dataset: singleDataSet.data,
  }));
});

const sortedReportingPeriods = computed(() => {
  return mldtDatasets.value.map((mldtDataset) => mldtDataset.headerLabel);
});

const sortedReports = computed(() => {
  switch (props.frameworkIdentifier) {
    case DataTypeEnum.EutaxonomyNonFinancials: {
      return mldtDatasets.value.map(
        (mldtDataset) => (mldtDataset.dataset as EuTaxonomyDataForNonFinancials).general?.referencedReports,
      );
    }
    case DataTypeEnum.EutaxonomyFinancials: {
      return mldtDatasets.value.map(
        (mldtDataset) => (mldtDataset.dataset as EuTaxonomyDataForFinancials).referencedReports,
      );
    }
    default: {
      return null; //Since other frameworks don't have referenced reports and therefore banners, reports don't need
      // to be added and the banner will never receive "null" as an input
    }
  }
});

const updateCounter = ref(0);
const status = ref<ViewPanelStates>("LoadingDatasets");
const dataAndMetaInformationForDisplay = shallowRef<DataAndMetaInformation<FrameworkDataType>[]>([]);

watch(
  [(): string => props.companyId, (): DataMetaInformation | undefined => props.singleDataMetaInfoToDisplay],
  async () => reloadDisplayData(++updateCounter.value),
  { immediate: true },
);

/**
 * Triggers a reload of the information to be displayed with concurrency conflict handling.
 * @param currentCounter the value of the request counter at call-time.
 */
async function reloadDisplayData(currentCounter: number): Promise<void> {
  status.value = "LoadingDatasets";
  try {
    const datasetsForDisplay = await loadDataForDisplay(props.companyId, props.singleDataMetaInfoToDisplay);
    if (updateCounter.value == currentCounter) {
      dataAndMetaInformationForDisplay.value = datasetsForDisplay;
      status.value = "DisplayingDatasets";
    }
  } catch (err) {
    console.error(err);
    if (updateCounter.value == currentCounter) {
      status.value = "Error";
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
  singleDataMetaInfoToDisplay?: DataMetaInformation,
): Promise<DataAndMetaInformation<FrameworkDataType>[]> {
  const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

  const frameworkDefinition = getFrontendFrameworkDefinition(
    props.frameworkIdentifier,
  ) as FrontendFrameworkDefinition<FrameworkDataType>;
  let dataControllerApi: FrameworkDataApi<FrameworkDataType>;
  if (frameworkDefinition) {
    dataControllerApi = frameworkDefinition.getFrameworkApiClient(undefined, apiClientProvider.axiosInstance);
  } else {
    dataControllerApi = apiClientProvider.getUnifiedFrameworkDataController(props.frameworkIdentifier);
  }

  if (singleDataMetaInfoToDisplay) {
    const singleDataset = (await dataControllerApi.getFrameworkData(singleDataMetaInfoToDisplay.dataId)).data.data;

    return [{ metaInfo: singleDataMetaInfoToDisplay, data: singleDataset }];
  } else {
    return (await dataControllerApi.getAllCompanyData(assertDefined(companyId))).data;
  }
}
</script>
