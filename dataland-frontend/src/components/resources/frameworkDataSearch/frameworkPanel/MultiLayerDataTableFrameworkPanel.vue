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

    <MultiLayerDataTable
      :datasets="mldtDatasets"
      :config="
        inReviewMode
          ? convertMultiLayerDataTableConfigForHighlightingHiddenFields(displayConfiguration)
          : displayConfiguration
      "
      :ariaLabel="`Datasets of the ${frameworkDisplayName} framework`"
    />
  </div>
  <div v-if="status == 'Error'">
    <h1>We are having issues loading the data.</h1>
  </div>
</template>

<script setup generic="Framework extends keyof FrameworkDataTypes" lang="ts">
import { type FrameworkDataTypes } from "@/utils/api/FrameworkDataTypes";
import MultiLayerDataTable from "@/components/resources/dataTable/MultiLayerDataTable.vue";
import { humanizeStringOrNumber } from "@/utils/StringHumanizer";
import { computed, inject, ref, shallowRef, watch } from "vue";
import { type MLDTConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import { type DataAndMetaInformation } from "@/api-models/DataAndMetaInformation";
import { sortDatasetsByReportingPeriod } from "@/utils/DataTableDisplay";
import { type DataMetaInformation } from "@clients/backend";
import type Keycloak from "keycloak-js";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { convertMultiLayerDataTableConfigForHighlightingHiddenFields } from "@/components/resources/frameworkDataSearch/frameworkPanel/MultiLayerDataTableQaHighlighter";

type ViewPanelStates = "LoadingDatasets" | "DisplayingDatasets" | "Error";

const getKeycloakPromise = inject<() => Promise<Keycloak>>("getKeycloakPromise");

const props = defineProps<{
  companyId: string;
  singleDataMetaInfoToDisplay?: DataMetaInformation;
  frameworkIdentifier: Framework;
  displayConfiguration: MLDTConfig<FrameworkDataTypes[Framework]["data"]>;
  inReviewMode: boolean;
}>();

const frameworkDisplayName = computed(() => humanizeStringOrNumber(props.frameworkIdentifier));

const mldtDatasets = computed(() => {
  const sortedDataAndMetaInformation = sortDatasetsByReportingPeriod(dataAndMetaInformationForDisplay.value);
  return sortedDataAndMetaInformation.map((it) => ({
    headerLabel: it.metaInfo.reportingPeriod,
    dataset: it.data,
  }));
});

const updateCounter = ref(0);
const status = ref<ViewPanelStates>("LoadingDatasets");
const dataAndMetaInformationForDisplay = shallowRef<DataAndMetaInformation<FrameworkDataTypes[Framework]["data"]>[]>(
  [],
);

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
): Promise<DataAndMetaInformation<FrameworkDataTypes[Framework]["data"]>[]> {
  const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
  const dataControllerApi = await apiClientProvider.getUnifiedFrameworkDataController<Framework>(
    props.frameworkIdentifier,
  );

  if (singleDataMetaInfoToDisplay) {
    const singleDataset = (await dataControllerApi.getFrameworkData(singleDataMetaInfoToDisplay.dataId)).data.data;

    return [{ metaInfo: singleDataMetaInfoToDisplay, data: singleDataset }];
  } else {
    return (await dataControllerApi.getAllCompanyData(assertDefined(companyId))).data;
  }
}
</script>
