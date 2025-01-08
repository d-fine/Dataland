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
                </span>
              </div>
            </th>
          </tr>
        </thead>
        <tbody class="p-datatable-tbody">
        <tr>
          <td class="headers-bg pl-4 vertical-align-top header-column-width">
            <span class="table-left-label">Publication date of the dataset on Dataland</span>
            <em
                class="material-icons info-icon"
                aria-hidden="true"
                v-tooltip.top="{
              value: 'Timestamp of data upload'
            }"
            >info</em
            >
          </td>
          <td
              v-for="(singleDataAndMetaInfo, idx) in dataAndMetaInfo"
              :key="idx"
              class="vertical-align-top">
             <span class="p-column-title" style="display: flex; align-items: center">
                  {{convertUnixTimeInMsToDateString(singleDataAndMetaInfo.metaInfo.uploadTime)}}
             </span>
          </td>
        </tr>
        <tr
        >
          <td
              class="headers-bg pl-4 vertical-align-top header-column-width"
              data-row-header="true"
          >
            <span class="table-left-label">Publication date of most recent report</span>
            <em
                class="material-icons info-icon"
                aria-hidden="true"
                v-tooltip.top="{
              value: 'Date when the latest version of the report was published',
            }"
            >info</em
            >
          </td>
          <td
              v-for="(singleDataAndMetaInfo, idx) in dataAndMetaInfo"
              :key="idx"
              class="vertical-align-top">
            <span class="p-column-title" style="display: flex; align-items: center">
                  {{latestDate(singleDataAndMetaInfo)}}
             </span>
          </td>
        </tr>
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
import {type MLDTConfig} from '@/components/resources/dataTable/MultiLayerDataTableConfiguration';
import MultiLayerDataTableBody from '@/components/resources/dataTable/MultiLayerDataTableBody.vue';
import {type DataAndMetaInformation} from '@/api-models/DataAndMetaInformation';
import Tooltip from 'primevue/tooltip';
import {convertUnixTimeInMsToDateString, dateStringFormatter} from '@/utils/DataFormatUtils';
import {
  type CompanyReport,
  DataTypeEnum,
  type EutaxonomyFinancialsData,
  type EutaxonomyNonFinancialsData,
  type SfdrData,
} from '@clients/backend';

const vTooltip = Tooltip;

/**
 * Extracts the publication date of the most recent report given DataAndMetaInformation.
 * @param singleDataAndMetaInfo the DataAndMetaInformation of a framework.
 * @returns publication date as string.
 */
function latestDate(singleDataAndMetaInfo: DataAndMetaInformation<T>): string {
  let latestDate = null;
  let referencedReports;
  switch (singleDataAndMetaInfo.metaInfo.dataType) {
    case DataTypeEnum.Sfdr:
      referencedReports = (singleDataAndMetaInfo.data as SfdrData).general?.general.referencedReports;
      break;
    case DataTypeEnum.EutaxonomyFinancials:
      referencedReports = (singleDataAndMetaInfo.data as EutaxonomyFinancialsData).general?.general?.referencedReports;
      break;
    case DataTypeEnum.EutaxonomyNonFinancials:
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
  return latestDate
      ? dateStringFormatter(latestDate)
      : '';
}
/* eslint-disable @typescript-eslint/no-unused-vars */
const props = defineProps<{
  config: MLDTConfig<T>;
  dataAndMetaInfo: Array<DataAndMetaInformation<T>>;
  ariaLabel: string;
  inReviewMode: boolean;
}>();
</script>
