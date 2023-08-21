<template>
  <!-- <div class="p-datatable">
    <div class="aligned-activities-container">
      <div class="aligned-activities-column">
        <div v-for="col of frozenColumnDefinitions" class="aligned-activities-row">{{ col.field }}</div>
        <div class="aligned-activities-row">1</div>
        <div class="aligned-activities-row">1</div>
      </div>
      <div class="aligned-activities-column">2</div>
      <div class="aligned-activities-column aligned-activities-scrolled-column">3</div>
    </div>
  </div> -->

  <DataTable scrollable :value="frozenColumnData">
    <ColumnGroup type="header">
      <Row>
        <Column header="" :rowspan="2" />
      </Row>
      <Row>
        <Column header="" :colspan="2"></Column>
        <Column header="Test"></Column>
      </Row>
    </ColumnGroup>
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

export default defineComponent({
  inject: ["dialogRef"],
  name: "AlignedActivitiesDataTable",
  components: { DataTable, Column, ColumnGroup, Row },
  data() {
    return {
      listOfRowContents: [] as Array<object | string>,
      kpiKeyOfTable: "" as string,
      keysOfValuesForColumnDisplay: [] as string[],
      keysWithValuesToBeHumanized: ["isInHouseProductionOrIsContractProcessing", "sectors"] as string[],
      humanizeString,
      columnHeaders: {},
      frozenColumnDefinitions: [] as Array<{ field: string; header: string; frozen?: boolean }>,
      mainColumnDefinitions: [] as Array<{ field: string; header: string; frozen?: boolean }>,
      frozenColumnData: [] as Array<{
        activity: string;
        label: string;
        content: string | string[] | number | number[];
      }>,
      mainColumnData: [] as Array<unknown>
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

    console.log(this.listOfRowContents);

    this.frozenColumnDefinitions = [
      { field: "activity", header: "Activity", frozen: true },
      { field: "content", header: "Code(s)", frozen: true },
    ];

    this.mainColumnDefinitions = [
      { field: "revenue", header: "Revenue" },
      { field: "revenuePercent", header: "Revenue (%)" },
      { field: "climateChangeMitigation", header: "Climate change mitigation" },
      { field: "climateChangeAdaptation", header: "Climate change adaptation" },
      { field: "waterAndMarineResources", header: "Water and marine resources" },
      { field: "circularEconomy", header: "Circular economy" },
      { field: "pollution", header: "Pollution" },
    ];

    this.frozenColumnData = [
      {
        activity: "UrbanAndSuburbanTransportRoadPassengerTransport",
        label: "Urban And Suburban Transport Road Passenger Transport",
        content: ["E"],
      },
      {
        activity: "ElectricityGenerationFromRenewableNonFossilGaseousAndLiquidFuels",
        label: "Electricity Generation From Renewable Non Fossil Gaseous And Liquid Fuels",
        content: [],
      },
      {
        activity: "InfrastructureEnablingLowCarbonRoadTransportAndPublicTransport",
        label: "Infrastructure Enabling Low Carbon Road Transport And Public Transport",
        content: ["F", "I"],
      },
      { activity: "ManufactureOfCement", label: "Manufacture Of Cement", content: ["I"] },
      { activity: "InfrastructureForRailTransport", label: "Infrastructure For Rail Transport", content: [] },
    ];

    this.mainColumnData = [
      { activity: "", group: "", field: "" }
    ];
  },
  methods: {
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
  },
});
</script>

<style lang="scss" scoped>
.aligned-activities-container {
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  justify-content: flex-start;
  align-items: stretch;
  align-content: stretch;
  width: calc(80vw - 3rem); // p-dialog element has CSS to take 80% of vieport width and p-content has 1.5rem margins
  min-height: 400px;
}

.aligned-activities-column {
  display: block;
  flex-grow: 0;
  flex-shrink: 1;
  flex-basis: auto;
  align-self: auto;
  order: 0;
  min-width: 100px;
  max-width: 300px;
  border-right: 1px solid red;

  flex-direction: column;
  flex-wrap: nowrap;
  justify-content: flex-start;
  align-items: stretch;
  align-content: stretch;
}

.aligned-activities-column.aligned-activities-scrolled-column {
  flex-grow: 1;
  border-right: 0 none;
}

.aligned-activities-rows {
  display: flex;
  flex-direction: column;
  flex-wrap: nowrap;
  justify-content: flex-start;
  align-items: stretch;
  align-content: stretch;
}

.aligned-activities-row {
  display: block;
  flex-grow: 0;
  flex-shrink: 1;
  flex-basis: auto;
  align-self: auto;
  order: 0;
  height: 60px;
  border: 1px solid green;
}
</style>
