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
                <span class="p-column-title" style="display: flex; align-items: center"
                  >{{ singleDataAndMetaInfo.metaInfo.reportingPeriod }}
                  <i
                    class="material-icons pl-2"
                    aria-hidden="true"
                    style="cursor: pointer"
                    :title="singleDataAndMetaInfo.metaInfo.reportingPeriod"
                    data-pd-tooltip="true"
                    v-tooltip.top="{
                      value: reportingYearToolTip(singleDataAndMetaInfo),
                      class: 'd-tooltip',
                    }"
                    >info</i
                  ></span
                >
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

<script lang="ts" generic="T">
import { defineComponent } from "vue";
import { type MLDTConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import MultiLayerDataTableBody from "@/components/resources/dataTable/MultiLayerDataTableBody.vue";
import { type DataAndMetaInformation } from "@/api-models/DataAndMetaInformation";
import Tooltip from "primevue/tooltip";
import { convertUnixTimeInMsToDateString } from "@/utils/DataFormatUtils";
import {
  type CompanyReport,
  type EuTaxonomyDataForFinancials,
  type EutaxonomyNonFinancialsData,
  type SfdrData,
} from "@clients/backend";

export default defineComponent({
  components: {
    MultiLayerDataTableBody,
  },
  directives: {
    tooltip: Tooltip,
  },
  methods: {
    /**
     * Generates the toolTip for reportingYear given DataAndMetaInformation.
     * @param singleDataAndMetaInfo DataAndMetaInformation of a framework.
     * @returns string the toolTip
     */
    reportingYearToolTip(singleDataAndMetaInfo: DataAndMetaInformation<T>): string {
      let latestDate = null;
      let referencedReports = null;
      switch (singleDataAndMetaInfo.metaInfo.dataType) {
        case "sfdr":
          referencedReports = (singleDataAndMetaInfo.data as SfdrData).general?.general.referencedReports;
          break;
        case "eutaxonomy-financials":
          referencedReports = (singleDataAndMetaInfo.data as EuTaxonomyDataForFinancials).referencedReports;
          break;
        case "eutaxonomy-non-financials":
          referencedReports = (singleDataAndMetaInfo.data as EutaxonomyNonFinancialsData).general?.referencedReports;
          break;
        default:
          referencedReports = null;
          break;
      }
      if (referencedReports) {
        for (const key in referencedReports) {
          const companyReport: CompanyReport | undefined = referencedReports[key];
          const reportDate = companyReport?.reportDate;
          if (reportDate && (!latestDate || reportDate > latestDate)) {
            latestDate = reportDate;
          }
        }
      }
      const mostRecentSourceToolTip = latestDate ? `Publication date of most recent source:\n ${latestDate}\n\n` : "";
      const datasetPublishedToolTip =
        "Dataset published on Dataland:\n " +
        convertUnixTimeInMsToDateString(singleDataAndMetaInfo.metaInfo.uploadTime);
      return mostRecentSourceToolTip + datasetPublishedToolTip;
    },
  },
  props: {
    config: {
      type: Object as () => MLDTConfig<T>,
      required: true,
    },
    dataAndMetaInfo: {
      type: Array as () => Array<DataAndMetaInformation<T>>,
      required: true,
    },
    ariaLabel: {
      type: String,
      required: false,
    },
    inReviewMode: {
      type: Boolean,
      required: true,
    },
  },
});
</script>
