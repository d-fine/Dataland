<template>
  <DataTable scrollable :value="frozenColumnData" class="aligned-activities-data-table">
    <ColumnGroup type="header">
      <Row>
        <Column header="" :rowspan="2" :colspan="2" />
      </Row>
      <Row>
        <Column
          v-for="group of mainColumnGroups"
          :key="group.key"
          :header="group.label"
          :colspan="group.colspan"
        ></Column>
      </Row>
      <Row>
        <Column header="" :colspan="2"></Column>
        <Column v-for="col of mainColumnDefinitions" :key="col.field" :header="col.header" :field="col.field"> </Column>
      </Row>
    </ColumnGroup>
    <div class="table-group-row-cover"></div>
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
        <template v-else>{{ data.naceCodes }}</template>
      </template>
    </Column>
    <Column
      v-for="col of mainColumnDefinitions"
      :field="col.field"
      :key="col.field"
      :header="col.header"
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
  };
  dnshCriteria: {
    ClimateMitigation: "Yes" | "No";
    ClimateAdaptation: "Yes" | "No";
    Water: "Yes" | "No";
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
      frozenColumnDefinitions: [] as Array<{ field: string; header: string; frozen: boolean }>,
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
    };
    this.kpiKeyOfTable = dialogRefData.kpiKeyOfTable;
    if (typeof dialogRefData.listOfRowContents[0] === "string") {
      this.listOfRowContents = dialogRefData.listOfRowContents.map((o) => ({ [this.kpiKeyOfTable]: o }));
    } else {
      this.listOfRowContents = dialogRefData.listOfRowContents;
    }

    this.frozenColumnDefinitions = [
      { field: "activity", header: "Activity", frozen: true },
      { field: "naceCodes", header: "Code(s)", frozen: true },
    ];

    this.mainColumnDefinitions = [
      { field: "revenue", header: "Revenue", group: "_revenue" },
      { field: "revenuePercent", header: "Revenue (%)", group: "_revenue" },

      ...this.makeGroupColumns("substantialContributionCriteria"),
      ...this.makeGroupColumns("dnshCriteria"),

      { field: "minimumSafeguards", header: "Minimum Safeguards", group: "_minimumSafeguards" },
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
      { key: "substantialContributionCriteria", label: "Substantial Contribution Criteria", colspan: 6 },
      { key: "dnshCriteria", label: "DNSH Criteria", colspan: 6 },
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
      return [
        { field: `ChangeMitigation`, header: "Climate change mitigation", group: groupName },
        { field: `ClimateAdaptation`, header: "Climate change adaptation", group: groupName },
        { field: `Water`, header: "Water and marine resources", group: groupName },
        { field: `CircularEconomy`, header: "Circular economy", group: groupName },
        { field: `Pollution`, header: "Pollution", group: groupName },
        { field: `BiodiversityAndEcosystems`, header: "Biodiversity And Ecosystems", group: groupName },
      ];
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
      group: "",
      field: "revenue",
      content: `${activity.share.absoluteShare.amount} ${activity.share.absoluteShare.currency}`,
    },
    {
      activity: activity.activityName,
      group: "",
      field: "revenuePercent",
      content: `${activity.share.relativeShareInPercent}%`,
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
  console.log("create", { activity });
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
