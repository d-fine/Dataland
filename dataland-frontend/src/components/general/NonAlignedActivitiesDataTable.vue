<template>
  <DataTable data-test="dataTableTest" :value="mainColumnData" class="activities-data-table">
    <Column
      v-for="col of mainColumnDefinitions"
      :field="col.field"
      :key="col.field"
      :header="col.header"
      :headerClass="cellClass(col)"
      :bodyClass="cellClass(col)"
    >
      <template #body="{ data }">
        <template v-if="col.field === 'activity'">{{ activityApiNameToHumanizedName(data.activity) }}</template>
        <template v-else-if="col.field === 'naceCodes'">
          <ul class="unstyled-ul-list">
            <li v-for="code of data.naceCodes" :key="code">{{ code }}</li>
          </ul>
        </template>
        <template v-else>
          {{ data[col.field] }}
        </template>
      </template>
    </Column>
  </DataTable>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import { type DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import {
  type AmountWithCurrency,
  type EuTaxonomyAlignedActivity,
} from "@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model";
import {
  activityApiNameToHumanizedName,
} from "../resources/frameworkDataSearch/euTaxonomy/ActivityName";

type NonAlignedActivityFieldValueObject = {
  activity: string;
  naceCodes: string[];
  revenue: string;
  revenuePercent: string;
};

type MainColumnDefinition = {
  field: string;
  header: string;
};

export default defineComponent({
  inject: ["dialogRef"],
  name: "NonAlignedActivitiesDataTable",
  components: { DataTable, Column },
  data() {
    return {
      listOfRowContents: [] as Array<EuTaxonomyAlignedActivity>,
      kpiKeyOfTable: "" as string,
      columnHeaders: {} as { [kpiKeyOfTable: string]: { [columnName: string]: string } },
      mainColumnDefinitions: [] as Array<MainColumnDefinition>,
      mainColumnData: [] as Array<NonAlignedActivityFieldValueObject>,
    };
  },
  mounted() {
    const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
    const dialogRefData = dialogRefToDisplay.data as {
      listOfRowContents: Array<object | string>;
      kpiKeyOfTable: string;
      columnHeaders: { [kpiKeyOfTable: string]: { [columnName: string]: string } };
    };
    this.kpiKeyOfTable = dialogRefData.kpiKeyOfTable;
    this.columnHeaders = dialogRefData.columnHeaders;

    if (typeof dialogRefData.listOfRowContents[0] === "string") {
      this.listOfRowContents = dialogRefData.listOfRowContents.map((o) => ({ [this.kpiKeyOfTable]: o }));
    } else {
      this.listOfRowContents = dialogRefData.listOfRowContents as Array<EuTaxonomyAlignedActivity>;
    }

    this.mainColumnDefinitions = [
      { field: "activity", header: this.humanizeHeaderName("activityName") },
      { field: "naceCodes", header: this.humanizeHeaderName("naceCodes") },
      { field: "revenue", header: this.humanizeHeaderName("revenue") },
      { field: "revenuePercent", header: this.humanizeHeaderName("revenuePercent") },
    ];

    this.mainColumnData = this.listOfRowContents.map((activity) => ({
      activity: activity.activityName as string,
      naceCodes: activity.naceCodes as string[],
      revenue: this.formatAbsoluteShare(activity.share?.absoluteShare),
      revenuePercent: this.fromatRelativeShareInPercent(activity.share?.relativeShareInPercent),
    }));
  },
  methods: {
    activityApiNameToHumanizedName,
    /**
     * @param key the item to lookup
     * @returns the display version of the column header
     */
    humanizeHeaderName(key: string) {
      return this.columnHeaders[this.kpiKeyOfTable][key];
    },
    /**
     * @param absoluteShare object containing amount and currency properties
     * @returns combined amount and currency
     */
    formatAbsoluteShare(absoluteShare: AmountWithCurrency | undefined): string {
      if (!absoluteShare) return "";
      const amount = absoluteShare.amount ?? "";
      const currency = absoluteShare.currency ?? "";
      return `${amount} ${currency}`;
    },
    /**
     *
     * @param relativeShareInPercent number value of percent
     * @returns formatted value
     */
    fromatRelativeShareInPercent(relativeShareInPercent: number | undefined): string {
      if (!relativeShareInPercent) return "";
      return `${relativeShareInPercent} %`;
    },
    /**
     *
     * @param col definition of column
     * @returns class names determined by column
     */
    cellClass(col: MainColumnDefinition): string {
      if (col.field === "activity") {
        return "col-activity headers-bg border-bottom";
      } else if (col.field === "naceCodes") {
        return "col-nace-codes headers-bg border-bottom";
      }
      return "horizontal-headers-size border-bottom";
    },
  },
});
</script>
