<template>
  <div class="p-datatable p-component">
    <div class="p-datatable-wrapper overflow-auto">
      <table class="p-datatable-table w-full" :aria-label="ariaLabel">
        <thead class="p-datatable-thead">
          <tr class="border-bottom-table">
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
          <tr
            v-show="
              !hideEmptyFields ||
              dataAndMetaInfo.some((singleDataAndMetaInfo) => singleDataAndMetaInfo.metaInfo.uploadTime != null)
            "
          >
            <td class="headers-bg pl-4 vertical-align-top header-column-width">
              <span class="table-left-label">Publication date of the dataset on Dataland</span>
              <em
                class="material-icons info-icon"
                aria-hidden="true"
                v-tooltip.top="{
                  value: 'Timestamp of data upload',
                }"
                >info</em
              >
            </td>
            <td v-for="(singleDataAndMetaInfo, idx) in dataAndMetaInfo" :key="idx" class="vertical-align-top">
              <span class="table-left-label" style="display: flex; align-items: center">
                {{ convertUnixTimeInMsToDateString(singleDataAndMetaInfo.metaInfo.uploadTime) }}
              </span>
            </td>
          </tr>
          <tr v-show="!hideEmptyFields || hasPublicationDate()">
            <td class="headers-bg pl-4 vertical-align-top header-column-width" data-row-header="true">
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
            <td v-for="(singleDataAndMetaInfo, idx) in dataAndMetaInfo" :key="idx" class="vertical-align-top">
              <i
                v-if="!hasPublicationDate() && inReviewMode"
                class="pi pi-eye-slash pr-1 text-red-500"
                aria-hidden="true"
                data-test="hidden-icon"
              />
              <span class="table-left-label" style="display: flex; align-items: center">
                {{ latestDate(singleDataAndMetaInfo) }}
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
import { type MLDTConfig } from '@/components/resources/dataTable/MultiLayerDataTableConfiguration';
import MultiLayerDataTableBody from '@/components/resources/dataTable/MultiLayerDataTableBody.vue';
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation';
import Tooltip from 'primevue/tooltip';
import { convertUnixTimeInMsToDateString, dateStringFormatter } from '@/utils/DataFormatUtils';
import {
  type CompanyReport,
  DataTypeEnum,
  type EutaxonomyFinancialsData,
  type EutaxonomyNonFinancialsData,
  type SfdrData,
  type NuclearAndGasData,
} from '@clients/backend';

const vTooltip = Tooltip;

/**
 *  Checks if at least one singleDataAndMetaInfo has a publication date.
 *  @returns boolean - true if at least one publicationDate exists, false otherwise.
 */
function hasPublicationDate(): boolean {
  return props.dataAndMetaInfo.some((singleDataAndMetaInfo) => {
    // Check for existing publication date
    let referencedReports;

    switch (singleDataAndMetaInfo.metaInfo.dataType) {
      case DataTypeEnum.Sfdr:
        referencedReports = (singleDataAndMetaInfo.data as SfdrData).general?.general.referencedReports;
        break;
      case DataTypeEnum.EutaxonomyFinancials:
        referencedReports = (singleDataAndMetaInfo.data as EutaxonomyFinancialsData).general?.general
          ?.referencedReports;
        break;
      case DataTypeEnum.EutaxonomyNonFinancials:
        referencedReports = (singleDataAndMetaInfo.data as EutaxonomyNonFinancialsData).general?.referencedReports;
        break;
      case DataTypeEnum.NuclearAndGas:
        referencedReports = (singleDataAndMetaInfo.data as NuclearAndGasData).general?.general?.referencedReports;
        break;
      default:
        referencedReports = null;
        break;
    }
    // Check if there is at least one valid publicationDate
    return (
      referencedReports &&
      Object.keys(referencedReports).length > 0 &&
      Object.values(referencedReports).some((companyReport) => companyReport?.publicationDate != null)
    );
  });
}

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
    case DataTypeEnum.NuclearAndGas:
      referencedReports = (singleDataAndMetaInfo.data as NuclearAndGasData).general?.general?.referencedReports;
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
  return latestDate ? dateStringFormatter(latestDate) : '';
}

const props = defineProps<{
  config: MLDTConfig<T>;
  dataAndMetaInfo: Array<DataAndMetaInformation<T>>;
  ariaLabel: string;
  inReviewMode: boolean;
  hideEmptyFields: boolean;
}>();
</script>
