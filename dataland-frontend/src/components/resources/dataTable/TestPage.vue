<!-- INFO: This file exists to help you understand the new data table and experiment. TODO: Remove -->

<template>
  <MultiLayerDataTable :config="viewConfiguration" :datasets="datasets" />
</template>

<style scoped></style>

<script lang="ts">
import { defineComponent } from "vue";
import MultiLayerDataTable from "@/components/resources/dataTable/MultiLayerDataTable.vue";
import { type MLDTConfig, type MLDTDataset } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import { MLDTDisplayComponents } from "@/components/resources/dataTable/MultiLayerDataTableCells";

interface TestDataset {
  companyName: string;
  companyOwner: string;
  turnover?: string;
  turnoverCurrency?: string;
}

const viewConfiguration: MLDTConfig<TestDataset> = [
  {
    type: "cell",
    label: "Company Name",
    explanation: "The name of the company",
    shouldDisplay: (dataset) => true,
    valueGetter: (dataset) => ({
      displayComponent: MLDTDisplayComponents.StringDisplayComponent,
      displayValue: dataset.companyName,
    }),
  },
  {
    type: "section",
    label: "General",
    expandOnPageLoad: true,
    shouldDisplay: (dataset) => true,
    children: [
      {
        type: "cell",
        label: "Company Owner",
        shouldDisplay: (dataset) => true,
        valueGetter: (dataset) => ({
          displayComponent: MLDTDisplayComponents.StringDisplayComponent,
          displayValue: dataset.companyOwner,
        }),
      },
      {
        type: "section",
        label: "Financial information",
        expandOnPageLoad: false,
        shouldDisplay: (dataset) => true,
        children: [
          {
            type: "cell",
            label: "Total Turnover",
            shouldDisplay: (dataset) => !!dataset.turnover || !!dataset.turnoverCurrency,
            valueGetter: (dataset) => ({
              displayComponent: MLDTDisplayComponents.StringDisplayComponent,
              displayValue: (dataset.turnover || "") + " " + (dataset.turnoverCurrency || ""),
            }),
          },
        ],
      },
    ],
  },
];

const demoDataset: MLDTDataset<TestDataset> = {
  headerLabel: "Testing",
  dataset: {
    companyName: "Marc Inc.",
    companyOwner: "Marc",
    turnover: "100000",
    turnoverCurrency: "EUR",
  },
};

export default defineComponent({
  name: "TestPage",
  components: { MultiLayerDataTable },
  data() {
    return {
      viewConfiguration,
      datasets: [demoDataset],
    };
  },
});
</script>
