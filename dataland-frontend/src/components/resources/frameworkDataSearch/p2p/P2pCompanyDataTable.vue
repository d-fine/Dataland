<template>
  <div class="card">
    <DataTable
      :value="arrayOfKpiDataObjects"
      rowGroupMode="subheader"
      groupRowsBy="categoryKey"
      dataKey="categoryKey"
      sortField="categoryKey"
      :sortOrder="1"
      sortMode="single"
      :expandableRowGroups="true"
      v-model:expandedRowGroups="expandedRowGroups"
    >
      <Column
        bodyClass="headers-bg"
        headerStyle="width: 30vw;"
        headerClass="horizontal-headers-size"
        field="kpiKey"
        header="KPIs"
      >
        <template #body="slotProps">
          <span class="table-left-label" :data-test="slotProps.data.kpiKey">{{ slotProps.data.kpiLabel }}</span>
          <em
            class="material-icons info-icon"
            aria-hidden="true"
            :title="slotProps.data.kpiLabel ? slotProps.data.kpiLabel : ''"
            v-tooltip.top="{
              value: slotProps.data.kpiDescription ? slotProps.data.kpiDescription : '',
            }"
            >info</em
          >
        </template>
      </Column>
      <Column
        v-for="reportingPeriodWithDataId of listOfReportingPeriodsWithDataId"
        headerClass="horizontal-headers-size"
        headerStyle="width: 30vw;"
        :field="reportingPeriodWithDataId.dataId"
        :header="reportingPeriodWithDataId.reportingPeriod"
        :key="reportingPeriodWithDataId.dataId"
      >
        <template #body="slotProps">
          <template
            v-if="
              slotProps.data.content[reportingPeriodWithDataId.dataId] !== undefined &&
              slotProps.data.content[reportingPeriodWithDataId.dataId] !== null
            "
          >
            <template v-if="Array.isArray(slotProps.data.content[reportingPeriodWithDataId.dataId])">
              <a
                v-if="
                  slotProps.data.content[reportingPeriodWithDataId.dataId].length > 1 ||
                  slotProps.data.content[reportingPeriodWithDataId.dataId].some((el) => typeof el === 'object')
                "
                @click="
                  openModalAndDisplayValuesInSubTable(
                    slotProps.data.content[reportingPeriodWithDataId.dataId],
                    slotProps.data.kpiLabel,
                    slotProps.data.kpiKey
                  )
                "
                class="link"
                >Show "{{ slotProps.data.kpiLabel }}"
                <em class="material-icons" aria-hidden="true" title=""> dataset </em>
              </a>
              <span v-else> {{ slotProps.data.content[reportingPeriodWithDataId.dataId][0] }} </span>
            </template>
            <span v-else-if="slotProps.data.kpiFormFieldComponent === 'PercentageFormField'">
              {{ slotProps.data.content[reportingPeriodWithDataId.dataId] }} %</span
            >
            <span
              v-else-if="
                typeof slotProps.data.content[reportingPeriodWithDataId.dataId] === 'object' &&
                slotProps.data.content[reportingPeriodWithDataId.dataId]?.value !== undefined
              "
            >
              <span
                v-if="
                  isYesNo(slotProps.data.content[reportingPeriodWithDataId.dataId].value) &&
                  hasDocument(slotProps.data.content[reportingPeriodWithDataId.dataId])
                "
              >
                <DocumentLink
                  :label="yesLabelMap.get(isCertificate(slotProps.data.kpiLabel))"
                  :download-name="slotProps.data.content[reportingPeriodWithDataId.dataId].dataSource.name"
                  :reference="slotProps.data.content[reportingPeriodWithDataId.dataId].dataSource.reference"
                  show-icon
                />
              </span>
              <span
                v-else-if="
                  isYesNo(slotProps.data.content[reportingPeriodWithDataId.dataId].value) &&
                  isCertificate(slotProps.data.kpiLabel)
                "
              >
                {{
                  slotProps.data.content[reportingPeriodWithDataId.dataId].value === YesNo.Yes
                    ? yesLabelMap.get(true)
                    : noLabelMap.get(true)
                }}
              </span>
              <span v-else>{{ slotProps.data.content[reportingPeriodWithDataId.dataId].value }}</span>
            </span>
            <span v-else>{{ slotProps.data.content[reportingPeriodWithDataId.dataId] }}</span>
          </template>
        </template>
      </Column>

      <Column field="subcategoryKey">
          <DataTable>
              <Column>Test</Column>
          </DataTable>
      </Column>
      <template #groupheader="slotProps">
        <span :data-test="slotProps.data.categoryKey" :id="slotProps.data.categoryKey" style="cursor: pointer">
          {{ slotProps.data.categoryLabel ? slotProps.data.categoryLabel : slotProps.data.categoryKey }}
        </span>
      </template>
    </DataTable>
  </div>
</template>

<script lang="ts">
import DetailsCompanyDataTable from "@/components/general/DetailsCompanyDataTable.vue";
import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";
import { KpiDataObject } from "@/components/resources/frameworkDataSearch/KpiDataObject";
import { ReportingPeriodOfDataSetWithId } from "@/utils/DataTableDisplay";
import { BaseDataPointYesNo, YesNo } from "@clients/backend";
import Column from "primevue/column";
import DataTable from "primevue/datatable";
import Tooltip from "primevue/tooltip";
import { defineComponent, PropType } from "vue";

export default defineComponent({
  name: "LksgCompanyDataTable",
  components: { DataTable, Column, DocumentLink },
  directives: {
    tooltip: Tooltip,
  },
  data() {
    return {
      expandedRowGroups: ["_masterData"],
      yesLabelMap: new Map<boolean, string>([
        [true, "Certified"],
        [false, "Yes"],
      ]),
      noLabelMap: new Map<boolean, string>([
        [true, "Uncertified"],
        [false, "No"],
      ]),
      YesNo,
    };
  },
  props: {
    arrayOfKpiDataObjects: {
      type: Array as PropType<Array<KpiDataObject>>,
      default: () => [],
    },
    listOfReportingPeriodsWithDataId: {
      type: Array as PropType<Array<ReportingPeriodOfDataSetWithId>>,
      default: () => [],
    },
  },
  mounted() {
    document.addEventListener("click", (e) => this.expandRowGroupOnHeaderClick(e));
  },
  methods: {
    /**
     * Checks if the BaseDataPoint holds a document reference
     * @param dataPoint the object to check for a reference
     * @returns true if the data point contains a document reference and has the appropriate value
     */
    hasDocument(dataPoint: BaseDataPointYesNo): boolean {
      return (
        dataPoint?.value === YesNo.Yes &&
        dataPoint?.dataSource?.reference != undefined &&
        dataPoint.dataSource.reference.length > 0
      );
    },
    /**
     * Checks if a label belongs to a certificate
     * @param label the label to check
     * @returns true if the label belongs to a certificate
     */
    isCertificate(label: string): boolean {
      const lowerCaseLabel = label.toLowerCase();
      return lowerCaseLabel.includes("certificate") || lowerCaseLabel.includes("certification");
    },
    /**
     * Checks if a string is 'Yes' or 'No'
     * @param value the string to check
     * @returns true if the string is 'Yes' or 'No'
     */
    isYesNo(value: string) {
      return (Object.values(YesNo) as string[]).includes(value);
    },
    /**
     * Opens a modal to display a table with the provided list of production sites
     * @param listOfValues An array consisting of production sites
     * @param modalTitle The title for the modal, which is derived from the key of the KPI
     * @param kpiKey the key of the KPI used to determine the type of Subtable that needs to be displayed
     */
    openModalAndDisplayValuesInSubTable(listOfValues: [], modalTitle: string, kpiKey: string) {
      this.$dialog.open(DetailsCompanyDataTable, {
        props: {
          header: modalTitle,
          modal: true,
          dismissableMask: true,
        },
        data: {
          listOfRowContents: listOfValues,
          kpiKeyOfTable: kpiKey,
        },
      });
    },
    /**
     * Enables groupRowExpansion (and collapse) when clicking on the whole header row
     * @param event a click event
     */
    expandRowGroupOnHeaderClick(event: Event) {
      const id = (event.target as Element).id;

      const matchingChild = Array.from((event.target as Element).children).filter((child: Element) =>
        this.arrayOfKpiDataObjects.some((dataObject) => dataObject.subcategoryKey === child.id)
      )[0];

      if (matchingChild || this.arrayOfKpiDataObjects.some((dataObject) => dataObject.subcategoryKey === id)) {
        const index = this.expandedRowGroups.indexOf(matchingChild?.id ?? id);
        if (index === -1) this.expandedRowGroups.push(matchingChild?.id ?? id);
        else this.expandedRowGroups.splice(index, 1);
      }
    },
  },
});
</script>

<style lang="scss" scoped>
.p-rowgroup-footer td {
  font-weight: 500;
}

::v-deep(.p-rowgroup-header) {
  span {
    font-weight: 500;
  }

  .p-row-toggler {
    vertical-align: middle;
    margin-right: 0.25rem;
    float: right;
    cursor: pointer;
  }
}
</style>
