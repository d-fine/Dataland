<template>
  <div class="p-datatable p-component">
    <div class="p-datatable-wrapper overflow-auto">
      <table class="p-datatable-table w-full" :aria-label="ariaLabel">
        <thead class="p-datatable-thead">
          <tr>
            <th class="horizontal-headers-size">
              <div class="p-column-header-content">
                <span class="p-column-title">KPIs</span>
              </div>
            </th>
            <th
              v-for="(singleDataAndMetaInfo, idx) in dataAndMetaInfo"
              :key="idx"
              class="horizontal-headers-size"
              :data-dataset-index="idx"
            >
              <div class="p-column-header-content">
                <span class="p-column-title" style="display: flex; align-items: center">
                  {{ singleDataAndMetaInfo.metaInfo.reportingPeriod }}
                  <i
                    class="material-icons info-icon pl-2"
                    aria-hidden="true"
                    :title="singleDataAndMetaInfo.metaInfo.reportingPeriod"
                    data-pd-tooltip="true"
                    v-tooltip.top="{
                      value: reportingYearToolTip(singleDataAndMetaInfo),
                      class: 'd-tooltip',
                    }"
                    >info</i
                  >
                </span>
              </div>
            </th>
          </tr>
        </thead>
        <tbody class="p-datatable-tbody">
          <MultiLayerDataTableBody
            :dataAndMetaInfo="dataAndMetaInfo"
            :inReviewMode="inReviewMode"
            :config="config"
            :isTopLevel="true"
            :isVisible="true"
          />
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.p-datatable-table {
  border-spacing: 0;
  border-collapse: collapse;
}
</style>

<script setup lang="ts" generic="T">
import { type MLDTConfig } from '@/components/resources/dataTable/MultiLayerDataTableConfiguration';
import MultiLayerDataTableBody from '@/components/resources/dataTable/MultiLayerDataTableBody.vue';
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation';
import Tooltip from 'primevue/tooltip';
import { convertUnixTimeInMsToDateString, dateStringFormatter } from '@/utils/DataFormatUtils';
import {
  type CompanyReport,
  type EuTaxonomyFinancialsData,
  type EutaxonomyNonFinancialsData,
  type SfdrData,
} from '@clients/backend';

const vTooltip = Tooltip;

/**
 * Generates the toolTip for reportingYear given DataAndMetaInformation.
 * @param singleDataAndMetaInfo the DataAndMetaInformation of a framework.
 * @returns string the toolTip.
 */
function reportingYearToolTip(singleDataAndMetaInfo: DataAndMetaInformation<T>): string {
  let latestDate = null;
  let referencedReports;
  switch (singleDataAndMetaInfo.metaInfo.dataType) {
    case 'sfdr':
      referencedReports = (singleDataAndMetaInfo.data as SfdrData).general?.general.referencedReports;
      break;
    case 'eu-taxonomy-financials':
      referencedReports = (singleDataAndMetaInfo.data as EuTaxonomyFinancialsData).referencedReports;
      break;
    case 'eutaxonomy-non-financials':
      referencedReports = (singleDataAndMetaInfo.data as EutaxonomyNonFinancialsData).general?.referencedReports;
      break;
    default:
      referencedReports = null;
      break;
  }
  if (referencedReports) {
    for (const key in referencedReports) {
      const companyReport: CompanyReport | undefined = referencedReports[key];
      const publicationDate = companyReport?.publicationDate;
      if (publicationDate && (!latestDate || publicationDate > latestDate)) {
        latestDate = publicationDate;
      }
    }
  }
  const mostRecentSourceToolTip = latestDate
    ? `Publication date of most recent report:\n ${dateStringFormatter(latestDate)}\n\n`
    : '';
  const datasetPublishedToolTip =
    'Publication date of the dataset on Dataland:\n ' +
    convertUnixTimeInMsToDateString(singleDataAndMetaInfo.metaInfo.uploadTime);
  return mostRecentSourceToolTip + datasetPublishedToolTip;
}
/* eslint-disable @typescript-eslint/no-unused-vars */
const props = defineProps<{
  config: MLDTConfig<T>;
  dataAndMetaInfo: Array<DataAndMetaInformation<T>>;
  ariaLabel: string;
  inReviewMode: boolean;
}>();
</script>
