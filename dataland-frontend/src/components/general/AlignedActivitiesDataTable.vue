<template>
  <DataTable scrollable :value="frozenColumnData">
    <ColumnGroup type="header">
      <Row>
        <Column header="" :rowspan="2" />
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
    <Column
      v-for="col of frozenColumnDefinitions"
      :field="col.field"
      :key="col.field"
      :header="col.header"
      :frozen="col.frozen"
      :style="columnCss(col.field)"
      bodyClass="headers-bg"
      headerClass="horizontal-headers-size"
    >
      <template #body="{ data }">
        <template v-if="col.field === 'activity'">{{ data.label }}</template>
        <template v-else>{{ data.content }}</template>
      </template>
    </Column>
    <Column
      v-for="col of mainColumnDefinitions"
      :field="col.field"
      :key="col.field"
      :header="col.header"
      headerClass="horizontal-headers-size"
    ></Column>
  </DataTable>

  <!-- <DataTable :value="frozenData">
    <Column
      v-for="col of frozenColumnDefinitions"
      :field="col.field"
      :key="col.field"
      :header="col.header"
      :frozen="col.frozen"
      bodyClass="headers-bg"
      headerClass="horizontal-headers-size"
    >
      <template #body="{ data }">
        <template v-if="col.field === 'activity'">{{ data.label }}</template>
        <template v-else>{{ data.content }}</template>
      </template>
    </Column>

    <Column
      v-for="col of dataColumnDefinitions"
      :field="col.field"
      :key="col.field"
      :header="col.header"
      headerClass="horizontal-headers-size"
    ></Column>
  </DataTable> -->
  <!-- <DataTable :value="listOfRowContents">
    <Column
      v-for="keyOfColumn of keysOfValuesForColumnDisplay"
      :field="keyOfColumn"
      :key="keyOfColumn"
      :header="kpiKeyOfTable"
      headerStyle="width: 15vw;"
    >
      <template #body="{ data }">
        <template v-if="data[keyOfColumn]">
          <ul v-if="Array.isArray(data[keyOfColumn])">
            <li :key="el" v-for="el in data[keyOfColumn]">{{ el }}</li>
          </ul>
          <div v-else-if="typeof data[keyOfColumn] === 'object'">
            <p :key="key" v-for="[key, value] in Object.entries(data[keyOfColumn])" style="margin: 0; padding: 0">
              {{ value }}
            </p>
          </div>
          <span v-else>{{ humanizeStringIfNecessary(keyOfColumn, data[keyOfColumn]) }}</span>
        </template>
      </template>
    </Column>
  </DataTable> -->
</template>

<script lang="ts">
import { defineComponent } from "vue";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import ColumnGroup from "primevue/columngroup";
import Row from "primevue/row";
import { type DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import { humanizeString } from "@/utils/StringHumanizer";

export type ActivityObject = {
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

export default defineComponent({
  inject: ["dialogRef"],
  name: "AlignedActivitiesDataTable",
  components: { DataTable, Column, ColumnGroup, Row },
  data() {
    return {
      listOfRowContents: [] as Array<ActivityObject>,
      kpiKeyOfTable: "" as string,
      keysOfValuesForColumnDisplay: [] as string[],
      keysWithValuesToBeHumanized: ["isInHouseProductionOrIsContractProcessing", "sectors"] as string[],
      humanizeString,
      columnHeaders: {},
      frozenColumnDefinitions: [] as Array<{ field: string; header: string; frozen?: boolean }>,
      mainColumnGroups: [] as Array<{ key: string; label: string; colspan: number }>,
      mainColumnDefinitions: [] as Array<{ field: string; header: string; frozen?: boolean }>,
      frozenColumnData: [] as Array<{
        activity: string;
        label: string;
        content: string | string[] | number | number[];
      }>,
      mainColumnData: [] as Array<unknown>,
    };
  },
  mounted() {
    const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
    const dialogRefData = dialogRefToDisplay.data as {
      listOfRowContents: Array<object | string>;
      kpiKeyOfTable: string;
      columnHeaders: object;
    };
    this.kpiKeyOfTable = dialogRefData.kpiKeyOfTable;
    this.columnHeaders = dialogRefData.columnHeaders;
    if (typeof dialogRefData.listOfRowContents[0] === "string") {
      this.keysOfValuesForColumnDisplay.push(this.kpiKeyOfTable);
      this.listOfRowContents = dialogRefData.listOfRowContents.map((o) => ({ [this.kpiKeyOfTable]: o }));
    } else {
      this.listOfRowContents = dialogRefData.listOfRowContents;
      this.generateColsNames();
    }

    this.frozenColumnData = this.listOfRowContents.map((activity) => ({
      activity: activity.activityName,
      label: this.camelCaseToWords(activity.activityName),
      content: activity.naceCodes,
    }));
    console.log({ frozenColumnData: this.frozenColumnData, listOfRowContents: this.listOfRowContents });

    this.frozenColumnDefinitions = [
      { field: "activity", header: "Activity", frozen: true },
      { field: "naceCodes", header: "Code(s)", frozen: true },
    ];

    this.mainColumnDefinitions = [
      { field: "revenue", header: "Revenue" },
      { field: "revenuePercent", header: "Revenue (%)" },

      ...this.makeGroupColumns("substantialContributionCriteria"),
      ...this.makeGroupColumns("dnshCriteria"),

      { field: "minimumSafeguards", header: "Minimum Safeguards" },
    ];

    this.mainColumnData = this.frozenColumnData.map((col) => [
      ...mock_revenue_group(col.activity),
      ...mock_substantialContributionCriteria_group(col.activity),
      ...mock_dnshCriteria_group(col.activity),
      ...mock_minimumSafeguards_group(col.activity),
    ]);

    this.mainColumnGroups = [
      { key: "_revenue", label: "", colspan: 2 },
      { key: "substantialContributionCriteria", label: "Substantial Contribution Criteria", colspan: 6 },
      { key: "dnshCriteria", label: "DNSH Criteria", colspan: 6 },
      { key: "_minimumSafeguards", label: "", colspan: 1 },
    ];

    console.log("mainColumnData");
    console.log(this.mainColumnData);
  },
  methods: {
    /**
     * @param groupName the name of the group to which columns will be assigned
     * @returns column definitions for group
     */
    makeGroupColumns(groupName: string) {
      return [
        { field: `${groupName}_climateChangeMitigation`, header: "Climate change mitigation" },
        { field: `${groupName}_climateChangeAdaptation`, header: "Climate change adaptation" },
        { field: `${groupName}_waterAndMarineResources`, header: "Water and marine resources" },
        { field: `${groupName}_circularEconomy`, header: "Circular economy" },
        { field: `${groupName}_pollution`, header: "Pollution" },
        { field: `${groupName}_biodiversityAndEcosystems`, header: "Biodiversity And Ecosystems" },
      ];
    },
    /**
     * Gets the keys from a production site type to define the columns that the displayed table in this vue component
     * should have.
     */
    generateColsNames(): void {
      const presentKeys = this.listOfRowContents.reduce(function (keyList: string[], rowContent) {
        for (const key of Object.keys(rowContent)) {
          if (keyList.indexOf(key) === -1) keyList.push(key);
        }
        return keyList;
      }, []);
      for (const key of presentKeys) {
        this.keysOfValuesForColumnDisplay.push(key);
      }
    },
    /**
     * Humanizes a string if the corresponding key is listed as to be humanized
     * @param key decides if the value is to be humanized
     * @param value string to be possibly humanized
     * @returns a humanized input of the value parameter if the k
     */
    humanizeStringIfNecessary(key: string, value: string): string {
      if (this.keysWithValuesToBeHumanized.includes(key)) {
        return humanizeString(value);
      }
      return value;
    },
    /**
     *
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
          return "width: 20vw; min-width: 300px";
        case "naceCodes":
          return "width: 100px; max-width: 100px";
        default:
          return "";
      }
    },
  },
});

function randomValue(limit = 10): number {
  return Math.ceil(Math.random() * limit);
}

function mock_revenue_group(activity: string) {
  return [
    {
      activity,
      group: "_revenue",
      field: "revenue",
      content: randomValue(3000),
    },
    {
      activity,
      group: "_revenue",
      field: "revenuePercent",
      content: `${randomValue()}%`,
    },
  ];
}

function mock_substantialContributionCriteria_group(activity: string) {
  const fields = [
    "climateChangeMitigation",
    "climateChangeAdaptation",
    "waterAndMarineResources",
    "circularEconomy",
    "pollution",
    "biodiversityAndEcosystems",
  ];

  return fields.map((field) => ({
    activity,
    group: "substantialContributionCriteria",
    field,
    content: `${randomValue()}%`,
  }));
}

function mock_dnshCriteria_group(activity: string) {
  const fields = [
    "climateChangeMitigation",
    "climateChangeAdaptation",
    "waterAndMarineResources",
    "circularEconomy",
    "pollution",
    "biodiversityAndEcosystems",
  ];

  return fields.map((field) => ({
    activity,
    group: "dnshCriteria",
    field,
    content: randomValue() > 3 ? "Yes" : "No",
  }));
}

function mock_minimumSafeguards_group(activity: string) {
  return [
    {
      activity,
      group: "_minimumSafeguards",
      field: "minimumSafeguards",
      content: randomValue() > 3 ? "Yes" : "No",
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
