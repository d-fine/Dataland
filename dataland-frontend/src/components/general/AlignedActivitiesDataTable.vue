<template>
  <DataTable data-test="dataTableTest" scrollable :value="frozenColumnData" class="activities-data-table">
    <ColumnGroup data-test="columnGroupTest" type="header">
      <Row>
        <Column
          header=""
          :frozen="true"
          alignFrozen="left"
          :colspan="2"
          class="frozen-row-header border-right"
          style="background-color: #fff"
        ></Column>
        <Column
          data-test="mainColumnTest"
          v-for="group of mainColumnGroups"
          :key="group.key"
          :header="group.label"
          :colspan="group.colspan"
          class="group-row-header"
        ></Column>
      </Row>
      <Row>
        <Column
          data-test="headerActivity"
          header="Activity"
          :frozen="true"
          alignFrozen="left"
          class="frozen-row-header"
        ></Column>
        <Column header="NACE Code(s)" :frozen="true" alignFrozen="left" class="frozen-row-header border-right"></Column>
        <Column
          v-for="col of mainColumnDefinitions"
          :key="col.field"
          :header="col.header"
          :field="col.field"
          :class="groupColumnCssClasses(col, 'non-frozen-header')"
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
      :class="groupColumnCssClasses(col)"
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
import {
  type YesNo,
  type Activity,
  type EuTaxonomyAlignedActivity,
} from "@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model";
import { formatAmountWithCurrency, formatPercentageNumber } from "@/utils/ValuesConversionUtils";

type ActivityFieldValueObject = {
  activity: string;
  group: string;
  field: string;
  content: string;
};

type MainColumnDefinition = {
  field: string;
  header: string;
  frozen?: boolean;
  group: string;
  groupIndex: number;
};

const substantialContributionFields = [
  "substantialContributionToClimateChangeMitigation",
  "substantialContributionToClimateChangeAdaption",
  "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResources",
  "substantialContributionToTransitionToACircularEconomy",
  "substantialContributionToPollutionPreventionAndControl",
  "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystems",
] as const;

const dnshCriteriaFields = [
  "dnshToClimateChangeMitigation",
  "dnshToClimateChangeAdaption",
  "dnshToSustainableUseAndProtectionOfWaterAndMarineResources",
  "dnshToTransitionToACircularEconomy",
  "dnshToPollutionPreventionAndControl",
  "dnshToProtectionAndRestorationOfBiodiversityAndEcosystems",
] as const;

export default defineComponent({
  inject: ["dialogRef"],
  name: "AlignedActivitiesDataTable",
  components: { DataTable, Column, ColumnGroup, Row },
  data() {
    return {
      listOfRowContents: [] as Array<EuTaxonomyAlignedActivity>,
      kpiKeyOfTable: "" as string,
      columnHeaders: {} as { [kpiKeyOfTable: string]: { [columnName: string]: string } },
      frozenColumnDefinitions: [] as Array<{ field: string; header: string; frozen?: boolean; group: string }>,
      mainColumnGroups: [] as Array<{ key: string; label: string; colspan: number }>,
      mainColumnDefinitions: [] as Array<MainColumnDefinition>,
      frozenColumnData: [] as Array<{
        activity: Activity | undefined;
        naceCodes: string[] | undefined;
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
      this.listOfRowContents = dialogRefData.listOfRowContents as Array<EuTaxonomyAlignedActivity>;
    }

    this.frozenColumnDefinitions = [
      { field: "activity", header: this.humanizeHeaderName("activity"), frozen: true, group: "_frozen" },
      { field: "naceCodes", header: this.humanizeHeaderName("naceCodes"), frozen: true, group: "_frozen" },
    ];

    this.mainColumnDefinitions = [
      { field: "revenue", header: this.humanizeHeaderName("revenue"), group: "_revenue", groupIndex: 0 },
      { field: "revenuePercent", header: this.humanizeHeaderName("revenuePercent"), group: "_revenue", groupIndex: 1 },

      ...this.createGroupColumnDefinitions([...substantialContributionFields], "substantialContributionCriteria"),
      ...this.createGroupColumnDefinitions([...dnshCriteriaFields], "dnshCriteria"),

      {
        field: "minimumSafeguards",
        header: this.humanizeHeaderName("minimumSafeguards"),
        group: "_minimumSafeguards",
        groupIndex: 0,
      },
    ];

    this.frozenColumnData = this.listOfRowContents.map((activity) => ({
      activity: activity.activityName,
      naceCodes: activity.naceCodes,
    }));

    this.mainColumnData = this.listOfRowContents
      .map((col) => [
        ...createRevenueGroupData(col),

        ...substantialContributionFields.map((field) =>
          createActivityGroupDataItem(
            col.activityName as string,
            "substantialContributionCriteria",
            field,
            formatPercentageNumber(col[field]),
          ),
        ),

        ...dnshCriteriaFields.map((field) =>
          createActivityGroupDataItem(
            col.activityName as string,
            "dnshCriteria",
            field,
            this.valueFormatterForYesNo(col[field] as YesNo),
          ),
        ),

        ...createMinimumSafeguardsGroupData(col),
      ])
      .flat();

    this.mainColumnGroups = [
      { key: "_revenue", label: "", colspan: this.findMaxColspanForGroup("_revenue") },
      {
        key: "substantialContributionCriteria",
        label: this.humanizeHeaderName("substantialContributionCriteria"),
        colspan: this.findMaxColspanForGroup("substantialContributionCriteria"),
      },
      {
        key: "dnshCriteria",
        label: this.humanizeHeaderName("dnshCriteria"),
        colspan: this.findMaxColspanForGroup("dnshCriteria"),
      },
      { key: "_minimumSafeguards", label: "", colspan: this.findMaxColspanForGroup("_minimumSafeguards") },
    ];
  },
  methods: {
    /**
     * @param value the YesNo value to format as Yes, No or blank
     * @returns the new formatted string
     */
    valueFormatterForYesNo(value: YesNo): string {
      return typeof value !== "undefined" ? value : "";
    },
    /**
     * @param groupName name of the group to count number of fields
     * @returns the maximum value of fields per activity and group
     */
    findMaxColspanForGroup(groupName: string): number {
      const environmentalObjectivesLength = Object.keys(EnvironmentalObjective).length;
      const colspans: { [groupName: string]: number } = {
        _revenue: 2,
        substantialContributionCriteria: environmentalObjectivesLength,
        dnshCriteria: environmentalObjectivesLength,
        _minimumSafeguards: 1,
      };
      return colspans[groupName];
    },
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
     * @param columnDefinition column definition we check against
     * @param additionalClasses (optional) any additional classes to be added
     * @returns classes for specific columns
     */
    groupColumnCssClasses(columnDefinition: MainColumnDefinition, additionalClasses = ""): string {
      if (columnDefinition.groupIndex === 0) return `first-group-column ${additionalClasses}`;
      return additionalClasses;
    },
    /**
     * @param key the item to lookup
     * @returns the display version of the column header
     */
    humanizeHeaderName(key: string) {
      return this.columnHeaders[this.kpiKeyOfTable][key];
    },

    /**
     *
     * @param groupFields array of field names within the group
     * @param groupKey the group key or name
     * @returns column definitions
     */
    createGroupColumnDefinitions(groupFields: string[], groupKey: string) {
      return groupFields.map((field, index) => ({
        field,
        header: this.humanizeHeaderName(field),
        group: groupKey,
        groupIndex: index,
      }));
    },
  },
});

/**
 * @param activity targeted activity object
 * @returns list of revenue data items
 */
function createRevenueGroupData(activity: EuTaxonomyAlignedActivity): ActivityFieldValueObject[] {
  return [
    {
      activity: activity.activityName as Activity,
      group: "_revenue",
      field: "revenue",
      content: formatAmountWithCurrency(activity.share?.absoluteShare),
    },
    {
      activity: activity.activityName as Activity,
      group: "_revenue",
      field: "revenuePercent",
      content: formatPercentageNumber(activity.share?.relativeShareInPercent),
    },
  ];
}

/**
 * @param activityName name of the activity
 * @param groupName the name of the group to which the fields will be assigned to
 * @param field key of field where content will be rendered
 * @param content formatted value which will be displayed
 * @returns data item for the field in the group
 */
function createActivityGroupDataItem(
  activityName: string,
  groupName: string,
  field: string,
  content: string,
): ActivityFieldValueObject {
  return {
    activity: activityName,
    group: groupName,
    field,
    content,
  };
}

/**
 * @param activity targeted activity object
 * @returns list of minimum safeguards data items
 */
function createMinimumSafeguardsGroupData(activity: EuTaxonomyAlignedActivity): ActivityFieldValueObject[] {
  return [
    {
      activity: activity.activityName as string,
      group: "_minimumSafeguards",
      field: "minimumSafeguards",
      content: activity.minimumSafeguards ?? "",
    },
  ];
}
</script>
