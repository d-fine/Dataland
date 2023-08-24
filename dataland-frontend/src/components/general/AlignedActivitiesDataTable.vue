<template>
  <DataTable scrollable :value="frozenColumnData" class="aligned-activities-data-table">
    <ColumnGroup type="header">
      <Row>
        <Column
          header=""
          :frozen="true"
          alignFrozen="left"
          :colspan="2"
          class="frozen-row-header"
          style="background-color: #fff"
        ></Column>
        <Column
          v-for="group of mainColumnGroups"
          :key="group.key"
          :header="group.label"
          :colspan="group.colspan"
          class="group-row-header"
        ></Column>
      </Row>
      <Row>
        <Column header="Activity" :frozen="true" alignFrozen="left" class="frozen-row-header"></Column>
        <Column header="Code(s)" :frozen="true" alignFrozen="left" class="frozen-row-header"></Column>
        <Column
          v-for="col of mainColumnDefinitions"
          :key="col.field"
          :header="col.header"
          :field="col.field"
          class="non-frozen-header"
        >
        </Column>
      </Row>
    </ColumnGroup>
    <Column
      v-for="col of frozenColumnDefinitions"
      :field="col.field"
      :key="col.field"
      :header="col.header"
      :frozen="col.frozen"
      :class="columnCss(col.field)"
      bodyClass="headers-bg"
      headerClass="horizontal-headers-size"
    >
      <template #body="{ data }">
        <template v-if="col.field === 'activity'">{{ camelCaseToWords(data.activity) }}</template>
        <template v-else>
          <ul class="unstyled-ul-list">
            <li v-for="code of data.naceCodes" :key="code">{{ code }}</li>
          </ul>
        </template>
      </template>
    </Column>
    <Column
      v-for="col of mainColumnDefinitions"
      :field="col.field"
      :key="col.field"
      :header="col.header"
      bodyClass="col-value"
      headerClass="horizontal-headers-size"
    >
      <template #body="{ data }">
        {{ findContentFromActivityGroupAndField(data.activity, col.group, col.field) }}
      </template>
    </Column>
  </DataTable>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import ColumnGroup from "primevue/columngroup";
import Row from "primevue/row";
import { type DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import { EnvironmentalObjective } from "@/api-models/EnvironmentalObjective";

type ActivityObject = {
  activityName: string;
  naceCodes: string[];
  share: {
    relativeShareInPercent: number;
    absoluteShare: {
      amount: number;
      currency: string;
    };
  };
  substantialContributionCriteria: {
    ClimateMitigation: number;
    ClimateAdaptation: number;
    Water: number;
    CircularEconomy: number;
    PollutionPrevention: number;
    Biodiversity: number;
  };
  dnshCriteria: {
    ClimateMitigation: "Yes" | "No";
    ClimateAdaptation: "Yes" | "No";
    Water: "Yes" | "No";
    CircularEconomy: "Yes" | "No";
    PollutionPrevention: "Yes" | "No";
    Biodiversity: "Yes" | "No";
  };
  minimumSafeguards: "Yes" | "No";
};

type ActivityFieldValueObject = {
  activity: string;
  group: string;
  field: string;
  content: string | string[] | number | number[];
};

export default defineComponent({
  inject: ["dialogRef"],
  name: "AlignedActivitiesDataTable",
  components: { DataTable, Column, ColumnGroup, Row },
  data() {
    return {
      listOfRowContents: [] as Array<ActivityObject>,
      kpiKeyOfTable: "" as string,
      columnHeaders: {} as { [kpiKeyOfTable: string]: { [columnName: string]: string } },
      frozenColumnDefinitions: [] as Array<{ field: string; header: string; frozen?: boolean; group: string }>,
      mainColumnGroups: [] as Array<{ key: string; label: string; colspan: number }>,
      mainColumnDefinitions: [] as Array<{ field: string; header: string; frozen?: boolean; group: string }>,
      frozenColumnData: [] as Array<{
        activity: string;
        naceCodes: string | string[] | number | number[];
      }>,
      mainColumnData: [] as Array<ActivityFieldValueObject>,
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
      this.listOfRowContents = dialogRefData.listOfRowContents;
    }

    this.frozenColumnDefinitions = [
      { field: "activity", header: this.humanizeHeaderName("activity"), frozen: true, group: "_frozen" },
      { field: "naceCodes", header: this.humanizeHeaderName("naceCodes"), frozen: true, group: "_frozen" },
    ];

    this.mainColumnDefinitions = [
      { field: "revenue", header: this.humanizeHeaderName("revenue"), group: "_revenue" },
      { field: "revenuePercent", header: this.humanizeHeaderName("revenuePercent"), group: "_revenue" },

      ...this.makeGroupColumns("substantialContributionCriteria"),
      ...this.makeGroupColumns("dnshCriteria"),

      { field: "minimumSafeguards", header: this.humanizeHeaderName("minimumSafeguards"), group: "_minimumSafeguards" },
    ];

    this.frozenColumnData = this.listOfRowContents.map((activity) => ({
      activity: activity.activityName,
      naceCodes: activity.naceCodes,
    }));

    this.mainColumnData = this.listOfRowContents
      .map((col) => [
        ...createRevenueGroupData(col),
        ...createSubstantialContributionCriteriaGroupData(col),
        ...createDnshCriteriaGroupData(col),
        ...createMinimumSafeguardsGroupData(col),
      ])
      .flat();

    this.mainColumnGroups = [
      { key: "_revenue", label: "", colspan: 2 },
      {
        key: "substantialContributionCriteria",
        label: this.humanizeHeaderName("substantialContributionCriteria"),
        colspan: 6,
      },
      { key: "dnshCriteria", label: this.humanizeHeaderName("dnshCriteria"), colspan: 6 },
      { key: "_minimumSafeguards", label: "", colspan: 1 },
    ];
  },
  methods: {
    /**
     * Search mainColumnData for specific item and return it's value
     * @param activityName name of the targeted activity
     * @param groupName name of the targeted group
     * @param fieldName name of the targeted field
     * @returns string value from main data
     */
    findContentFromActivityGroupAndField(activityName: string, groupName: string, fieldName: string) {
      const value = this.mainColumnData.find(
        (item) => item.activity === activityName && item.group === groupName && item.field === fieldName,
      );
      return value ? value.content : "";
    },
    /**
     * @param groupName the name of the group to which columns will be assigned
     * @returns column definitions for group
     */
    makeGroupColumns(groupName: string) {
      const EnvironmentalObjectiveKeys = Object.keys(EnvironmentalObjective).filter((v) => isNaN(Number(v)));
      return EnvironmentalObjectiveKeys.map((enviromentalObjectiveKey: string) => ({
        field: enviromentalObjectiveKey,
        header: this.humanizeHeaderName(enviromentalObjectiveKey),
        group: groupName,
      }));
    },
    /**
     * @param target the camel case string we want to format
     * @returns a human readable version
     */
    camelCaseToWords(target: string): string {
      return target.replace(/([A-Z]+)/g, " $1").replace(/([A-Z][a-z])/g, " $1");
    },
    /**
     * @param key Define the column's CSS class
     * @returns CSS class name
     */
    columnCss(key: string): string {
      switch (key) {
        case "activity":
          return "col-activity";
        case "naceCodes":
          return "col-nace-codes";
        default:
          return "";
      }
    },
    /**
     * @param key the item to lookup
     * @returns the display version of the column header
     */
    humanizeHeaderName(key: string) {
      return this.columnHeaders[this.kpiKeyOfTable][key];
    },
  },
});

/**
 * @param activity targeted activity object
 * @returns list of revenue data items
 */
function createRevenueGroupData(activity: ActivityObject): ActivityFieldValueObject[] {
  return [
    {
      activity: activity.activityName,
      group: "_revenue",
      field: "revenue",
      content: `${activity.share?.absoluteShare?.amount ?? ""} ${activity.share?.absoluteShare?.currency ?? ""}`,
    },
    {
      activity: activity.activityName,
      group: "_revenue",
      field: "revenuePercent",
      content: `${activity.share?.relativeShareInPercent ?? ""}%`,
    },
  ];
}

/**
 * @param activity targeted activity object
 * @returns list of substantial contribution criteria data items
 */
function createSubstantialContributionCriteriaGroupData(activity: ActivityObject): ActivityFieldValueObject[] {
  const fields = Object.entries(activity.substantialContributionCriteria);
  return fields.map(([field, value]) => {
    const content = value ? `${value}%` : "";
    return {
      activity: activity.activityName,
      group: "substantialContributionCriteria",
      field,
      content,
    };
  });
}

/**
 * @param activity targeted activity object
 * @returns list of DNSH criteria data items
 */
function createDnshCriteriaGroupData(activity: ActivityObject): ActivityFieldValueObject[] {
  const fields = Object.entries(activity.dnshCriteria);
  return fields.map(([field, value]) => {
    const content = value ? `${value}` : "";
    return {
      activity: activity.activityName,
      group: "dnshCriteria",
      field,
      content,
    };
  });
}

/**
 * @param activity targeted activity object
 * @returns list of minimum safeguards data items
 */
function createMinimumSafeguardsGroupData(activity: ActivityObject): ActivityFieldValueObject[] {
  return [
    {
      activity: activity.activityName,
      group: "_minimumSafeguards",
      field: "minimumSafeguards",
      content: activity.minimumSafeguards ?? "",
    },
  ];
}
</script>

<style lang="scss" scoped>
.col-lg {
  $w: 300px;
  min-width: $w;
  max-width: $w;
  width: $w;
}
.col-sm {
  $w: 50px;
  min-width: $w;
  max-width: $w;
  width: $w;
}
</style>
