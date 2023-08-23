<template>
  <div v-show="mapOfCategoryKeysToDataObjectArrays.size > 0">
    <DataTable tableClass="onlyHeaders">
      <Column
        headerStyle="width: 30vw;"
        headerClass="horizontal-headers-size first-horizontal-headers-size"
        header="KPIs"
      />
      <Column
        v-for="reportingPeriodWithDataId of arrayOfReportingPeriodWithDataId"
        headerClass="horizontal-headers-size"
        headerStyle="width: 30vw;"
        :header="reportingPeriodWithDataId.reportingPeriod"
        :key="reportingPeriodWithDataId.dataId"
      />
    </DataTable>
    <div
      v-for="(arrayOfKpiDataObjectsMapItem, index) in mapOfCategoryKeysToDataObjectArrays"
      :key="index"
      class="d-table-style"
    >
      <div v-if="shouldCategoryBeRendered(arrayOfKpiDataObjectsMapItem[0])">
        <div>
          <div class="pt-2 pl-2 pb-2 w-full d-cursor-pointer border-bottom-table p-2" @click="toggleExpansion(index)">
            <span
              :class="`p-badge badge-${colorOfCategory(arrayOfKpiDataObjectsMapItem[0])}`"
              :data-test="arrayOfKpiDataObjectsMapItem[0]"
              >{{ arrayOfKpiDataObjectsMapItem[0].toUpperCase() }}
            </span>
            <button v-if="!isExpanded(index)" class="pt-1 pr-3 d-cursor-pointer d-chevron-style">
              <span class="pr-1 pt-1 pi pi-chevron-right d-chevron-font"></span>
            </button>
            <button v-if="isExpanded(index)" class="pt-2 pr-3 d-cursor-pointer d-chevron-style">
              <span class="pr-1 pi pi-chevron-down d-chevron-font"></span>
            </button>
          </div>
        </div>
        <div v-show="isExpanded(index)">
          <TwoLayerDataTable
            data-test="TwoLayerTest"
            :arrayOfKpiDataObjects="arrayOfKpiDataObjectsMapItem[1]"
            :list-of-reporting-periods-with-data-id="arrayOfReportingPeriodWithDataId"
            headerInputStyle="display: none;"
            :modal-column-headers="modalColumnHeaders"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { type KpiDataObject, type KpiValue } from "@/components/resources/frameworkDataSearch/KpiDataObject";
import TwoLayerDataTable from "@/components/resources/frameworkDataSearch/TwoLayerDataTable.vue";
import { type ReportingPeriodOfDataSetWithId, sortReportingPeriodsToDisplayAsColumns } from "@/utils/DataTableDisplay";
import { type Category, type Field, type Subcategory } from "@/utils/GenericFrameworkTypes";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { defineComponent } from "vue";
import Column from "primevue/column";
import DataTable from "primevue/datatable";
import { type DataAndMetaInformationViewModel, type FrameworkViewModel } from "@/components/resources/ViewModel";

export default defineComponent({
  name: "ThreeLayerTable",

  components: { TwoLayerDataTable, DataTable, Column },
  data() {
    return {
      expandedGroup: [0],
      resultKpiData: null as KpiDataObject,
      arrayOfReportingPeriodWithDataId: [] as Array<ReportingPeriodOfDataSetWithId>,
      mapOfKpiKeysToDataObjects: new Map() as Map<string, KpiDataObject>,
      mapOfCategoryKeysToDataObjectArrays: new Map() as Map<string, Array<KpiDataObject>>,
      importantCategoryKeys: ["general"],
      importantSubcategoryKeys: ["general", "basicInformation", "masterData"],
    };
  },
  props: {
    dataModel: {
      type: Array as () => Array<Category>,
      required: true,
    },
    dataAndMetaInfo: {
      type: Array as () => Array<DataAndMetaInformationViewModel<FrameworkViewModel>>,
      required: true,
    },
    formatValueForDisplay: {
      type: Function as () => (field: Field, value: KpiValue) => KpiValue,
      default: (field: Field, value: KpiValue): KpiValue => value,
    },
    modalColumnHeaders: {
      type: Object,
      default: () => ({}),
    },
  },
  mounted() {
      this.triggerConversionOfDataForDisplay()
  },
  watch: {
    dataAndMetaInfo() {
        this.triggerConversionOfDataForDisplay()
      }
  },
  emits: ["dataConverted"],
  methods: {
    /**
     * Creates kpi data objects to pass them to the data table.
     * @param kpiKey The field name of a kpi
     * @param kpiValue The corresponding value to the kpiKey
     * @param subcategory The sub category to which the kpi belongs
     * @param category category to which the kpi belongs to
     * @param dataId The value of the date kpi of an LkSG dataset
     */
    createKpiDataObjects(
      kpiKey: string,
      kpiValue: KpiValue,
      subcategory: Subcategory,
      category: Category,
      dataId: string,
    ): void {
      const kpiField = assertDefined(subcategory.fields.find((field) => field.name === kpiKey));
      const kpiData: KpiDataObject = {
        categoryKey: this.importantCategoryKeys.includes(category.name) ? `_${category.name}` : category.name,
        categoryLabel: category.label ? category.label : category.name,
        subcategoryKey: this.importantSubcategoryKeys.includes(subcategory.name)
          ? `_${subcategory.name}`
          : subcategory.name,
        subcategoryLabel: subcategory.label ? subcategory.label : subcategory.name,
        kpiKey: kpiKey,
        kpiLabel: kpiField?.label ? kpiField.label : kpiKey,
        kpiDescription: kpiField?.description ? kpiField.description : "",
        kpiFormFieldComponent: kpiField?.component ?? "",
        content: { [dataId]: this.formatValueForDisplay(kpiField, kpiValue) ?? "" },
      };
      const uniqueIdentiferOfKpi = `${kpiKey}+${subcategory.name}+${category.name}`;
      if (this.mapOfKpiKeysToDataObjects.has(uniqueIdentiferOfKpi)) {
        Object.assign(kpiData.content, this.mapOfKpiKeysToDataObjects.get(uniqueIdentiferOfKpi)?.content);
      }
      this.mapOfKpiKeysToDataObjects.set(uniqueIdentiferOfKpi, kpiData);
      this.resultKpiData = kpiData;
    },
    /**
     * Retrieves and converts the stored array of datasets in order to make it displayable in the frontend.
     */
    convertDataToFrontendFormat(): void {
      this.arrayOfReportingPeriodWithDataId = [];
      if (this.dataAndMetaInfo.length) {
        this.dataAndMetaInfo.forEach((currentDataset) => {
          const dataId = currentDataset.metaInfo?.dataId ?? "";
          const reportingPeriod = currentDataset.metaInfo?.reportingPeriod ?? "";
          this.arrayOfReportingPeriodWithDataId.push({
            dataId: dataId,
            reportingPeriod: reportingPeriod,
          });
          for (const [categoryKey, categoryObject] of Object.entries(currentDataset.data)) {
            if (categoryKey == "toApiModel") continue; // ignore toApiModel() Function as it is not a KPI
            if (categoryObject == null) continue;
            const listOfDataObjects: Array<KpiDataObject> = [];
            const frameworkCategoryData = assertDefined(
              this.dataModel.find((category) => category.name === categoryKey),
            );
            this.iterateThroughSubcategories(
              categoryObject,
              categoryKey,
              frameworkCategoryData,
              dataId,
              listOfDataObjects,
              currentDataset.data,
            );

            this.mapOfCategoryKeysToDataObjectArrays.set(frameworkCategoryData.label, listOfDataObjects);
          }
        });
      }
      this.arrayOfReportingPeriodWithDataId = sortReportingPeriodsToDisplayAsColumns(
        this.arrayOfReportingPeriodWithDataId as ReportingPeriodOfDataSetWithId[],
      );
      this.$emit("dataConverted");
    },
    /**
     * Iterates through all subcategories of a category
     * @param categoryObject the data object of the framework's category
     * @param categoryKey the key of the corresponding framework's category
     * @param frameworkCategoryData  the category object of the framework's category
     * @param dataId  the ID of the dataset
     * @param listOfDataObjects a map containing the category and it's corresponding Kpis
     * @param currentDataset dataset for which the show if conditions should be checked
     */
    iterateThroughSubcategories(
      categoryObject,
      categoryKey,
      frameworkCategoryData: Category,
      dataId: string,
      listOfDataObjects: Array<KpiDataObject>,
      currentDataset: FrameworkViewModel,
    ) {
      for (const [subCategoryKey, subCategoryObject] of Object.entries(categoryObject)) {
        if (subCategoryObject == null) continue;
        this.iterateThroughSubcategoryKpis(
          subCategoryObject,
          categoryKey,
          subCategoryKey,
          frameworkCategoryData,
          dataId,
          listOfDataObjects,
          currentDataset,
        );
      }
    },
    /**
     * Builds the result Kpi Data Object and adds it to the result list
     * @param subCategoryObject the data object of the framework's subcategory
     * @param categoryKey the key of the corresponding framework's category
     * @param subCategoryKey the key of the corresponding framework's subcategory
     * @param frameworkCategoryData the category object of the framework's category
     * @param dataId the ID of the dataset
     * @param listOfDataObjects a map containing the category and it's corresponding Kpis
     * @param currentDataset dataset for which the show if conditions should be checked
     */
    iterateThroughSubcategoryKpis(
      subCategoryObject,
      categoryKey,
      subCategoryKey: string,
      frameworkCategoryData: Category,
      dataId: string,
      listOfDataObjects: Array<KpiDataObject>,
      currentDataset: FrameworkViewModel,
    ) {
      for (const [kpiKey, kpiValue] of Object.entries(subCategoryObject)) {
        const subcategory = assertDefined(
          frameworkCategoryData.subcategories.find((subCategory) => subCategory.name === subCategoryKey),
        );
        const field = assertDefined(subcategory.fields.find((field) => field.name == kpiKey));

        if (field.showIf(currentDataset.toApiModel())) {
          this.createKpiDataObjects(kpiKey, kpiValue as KpiValue, subcategory, frameworkCategoryData, dataId);
          listOfDataObjects.push(this.resultKpiData);
        }
      }
    },
    /**
     * Checks whether a given category shall be displayed for at least one of the datasets to display
     * @param categoryName The name of the category to check
     * @returns true if category shall be displayed, else false
     */
    shouldCategoryBeRendered(categoryName: string): boolean {
      const category = assertDefined(this.dataModel.find((category) => category.label === categoryName));
      return this.dataAndMetaInfo.some((dataAndMetaInfo) => category.showIf(dataAndMetaInfo.data.toApiModel()));
    },
    /**
     * Retrieves the color for a given category from Data Model
     * @param categoryName The name of the category whose color is searched
     * @returns color as string
     */
    colorOfCategory(categoryName: string): string {
      return assertDefined(this.dataModel.find((category) => category.label === categoryName)).color;
    },
    /**
     * Checks whether an element is expanded or not
     * @param key element for which the check should be run
     * @returns if the element is expanded or not
     */
    isExpanded(key: number) {
      return this.expandedGroup.indexOf(key) !== -1;
    },
    /**
     * Expands and collapses an item
     * @param key element for which the check should be run
     */
    toggleExpansion(key: number) {
      if (this.isExpanded(key)) this.expandedGroup.splice(this.expandedGroup.indexOf(key), 1);
      else this.expandedGroup.push(key);
    },

      triggerConversionOfDataForDisplay() {
          if (this.dataAndMetaInfo.length > 0) {
              this.convertDataToFrontendFormat();
          }
      }
  },
});
</script>
<style scoped lang="scss">
.d-table-style {
  font-size: 16px;
  text-align: left;
  background-color: #f6f5ef;
}

.d-chevron-style {
  float: right;
  border: none;
  background-color: #f6f5ef;
}

.d-chevron-font {
  color: #e67f3f;
  font-size: 14px;
}
</style>
