<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading {{ humanizeString(dataTypeEnum.P2p) }} Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="mapOfCategoryKeysToDataObjectArrays.size > 0 && !waitingForData">
    <DataTable tableClass="onlyHeaders">
      <Column headerStyle="width: 30vw;" headerClass="horizontal-headers-size" header="KPIs"> </Column>
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
          <DisplayFrameworkDataTable
            :arrayOfKpiDataObjects="arrayOfKpiDataObjectsMapItem[1]"
            :list-of-reporting-periods-with-data-id="arrayOfReportingPeriodWithDataId"
            headerInputStyle="display: none;"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { KpiDataObject, KpiValue } from "@/components/resources/frameworkDataSearch/KpiDataObject";
import { PanelProps } from "@/components/resources/frameworkDataSearch/PanelComponentOptions";
import DisplayFrameworkDataTable from "@/components/resources/frameworkDataSearch/DisplayFrameworkDataTable.vue";
import { p2pDataModel } from "@/components/resources/frameworkDataSearch/p2p/P2pDataModel";
import { ApiClientProvider } from "@/services/ApiClients";
import { ReportingPeriodOfDataSetWithId, sortReportingPeriodsToDisplayAsColumns } from "@/utils/DataTableDisplay";
import { Category, Subcategory } from "@/utils/GenericFrameworkTypes";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { DataAndMetaInformationPathwaysToParisData, DataTypeEnum, PathwaysToParisData } from "@clients/backend";
import Keycloak from "keycloak-js";
import { defineComponent, inject } from "vue";
import Column from "primevue/column";
import DataTable from "primevue/datatable";
import { humanizeString } from "@/utils/StringHumanizer";

export default defineComponent({
  name: "P2pPanel",

  components: { DisplayFrameworkDataTable, DataTable, Column },
  data() {
    return {
      expandedGroup: [0],
      dataTypeEnum: DataTypeEnum,
      firstRender: true,
      waitingForData: true,
      resultKpiData: null as KpiDataObject,
      p2pDataAndMetaInfo: [] as Array<DataAndMetaInformationPathwaysToParisData>,
      arrayOfReportingPeriodWithDataId: [] as Array<ReportingPeriodOfDataSetWithId>,
      mapOfKpiKeysToDataObjects: new Map() as Map<string, KpiDataObject>,
      mapOfCategoryKeysToDataObjectArrays: new Map() as Map<string, Array<KpiDataObject>>,
    };
  },
  props: PanelProps,
  watch: {
    companyId() {
      this.arrayOfReportingPeriodWithDataId = [];
      this.fetchP2pData().catch((error) => console.log(error));
    },
    singleDataMetaInfoToDisplay() {
      if (!this.firstRender) {
        this.arrayOfReportingPeriodWithDataId = [];
        this.fetchP2pData().catch((error) => console.log(error));
      }
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  created() {
    this.fetchP2pData().catch((error) => console.log(error));
    this.firstRender = false;
  },

  methods: {
    humanizeString,
    /**
     * Fetches all accepted P2P datasets for the current company and converts them to the required frontend format.
     */
    async fetchP2pData() {
      try {
        this.waitingForData = true;
        const p2pDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getP2pDataControllerApi();
        if (this.singleDataMetaInfoToDisplay) {
          const singleP2pData = (
            await p2pDataControllerApi.getCompanyAssociatedP2pData(this.singleDataMetaInfoToDisplay.dataId)
          ).data.data;
          this.p2pDataAndMetaInfo = [{ metaInfo: this.singleDataMetaInfoToDisplay, data: singleP2pData }];
        } else {
          this.p2pDataAndMetaInfo = (
            await p2pDataControllerApi.getAllCompanyP2pData(assertDefined(this.companyId))
          ).data;
        }
        this.convertP2pDataToFrontendFormat();
        this.waitingForData = false;
      } catch (error) {
        console.error(error);
      }
    },

    /**
     * Creates kpi data objects to pass them to the data table.
     * @param kpiKey The field name of a kpi
     * @param kpiValue The corresponding value to the kpiKey
     * @param subcategory The sub category to which the kpi belongs
     * @param category category to which the kpi belongs to
     * @param dataIdOfP2pDataset The value of the date kpi of an LkSG dataset
     */
    createKpiDataObjects(
      kpiKey: string,
      kpiValue: KpiValue,
      subcategory: Subcategory,
      category: Category,
      dataIdOfP2pDataset: string
    ): void {
      const kpiField = assertDefined(subcategory.fields.find((field) => field.name === kpiKey));
      const kpiData = {
        categoryKey: category.name == "general" ? `_${category.name}` : category.name,
        categoryLabel: category.label ? category.label : category.name,
        subcategoryKey: subcategory.name == "general" ? `_${subcategory.name}` : subcategory.name,
        subcategoryLabel: subcategory.label ? subcategory.label : subcategory.name,
        kpiKey: kpiKey,
        kpiLabel: kpiField?.label ? kpiField.label : kpiKey,
        kpiDescription: kpiField?.description ? kpiField.description : "",
        kpiFormFieldComponent: kpiField?.component ?? "",
        content: { [dataIdOfP2pDataset]: (kpiField, kpiValue) },
      } as KpiDataObject;
      if (this.mapOfKpiKeysToDataObjects.has(kpiKey)) {
        Object.assign(kpiData.content, this.mapOfKpiKeysToDataObjects.get(kpiKey)?.content);
      }
      this.mapOfKpiKeysToDataObjects.set(kpiKey, kpiData);
      this.resultKpiData = kpiData;
    },
    /**
     * Retrieves and converts the stored array of P2P datasets in order to make it displayable in the frontend.
     */
    convertP2pDataToFrontendFormat(): void {
      if (this.p2pDataAndMetaInfo.length) {
        this.p2pDataAndMetaInfo.forEach((oneP2pDataset: DataAndMetaInformationPathwaysToParisData) => {
          const dataIdOfP2pDataset = oneP2pDataset.metaInfo?.dataId ?? "";
          const reportingPeriodOfP2pDataset = oneP2pDataset.metaInfo?.reportingPeriod ?? "";
          this.arrayOfReportingPeriodWithDataId.push({
            dataId: dataIdOfP2pDataset,
            reportingPeriod: reportingPeriodOfP2pDataset,
          });
          for (const [categoryKey, categoryObject] of Object.entries(oneP2pDataset.data) as [string, object] | null) {
            if (categoryObject == null) continue;
            const listOfDataObjects: Array<KpiDataObject> = [];
            const frameworkCategoryData = assertDefined(p2pDataModel.find((category) => category.name === categoryKey));
            this.iterateThroughSubcategories(
              categoryObject,
              categoryKey,
              frameworkCategoryData,
              dataIdOfP2pDataset,
              listOfDataObjects,
              oneP2pDataset.data
            );

            this.mapOfCategoryKeysToDataObjectArrays.set(frameworkCategoryData.label, listOfDataObjects);
          }
        });
      }
      this.arrayOfReportingPeriodWithDataId = sortReportingPeriodsToDisplayAsColumns(
        this.arrayOfReportingPeriodWithDataId as ReportingPeriodOfDataSetWithId[]
      );
    },
    /**
     * Iterates through all subcategories of a category
     * @param categoryObject the data object of the framework's category
     * @param categoryKey the key of the corresponding framework's category
     * @param frameworkCategoryData  the category object of the framework's category
     * @param dataIdOfP2pDataset  the data ID of the P2P dataset
     * @param listOfDataObjects a map containing the category and it's corresponding Kpis
     * @param oneP2pDataset dataset for which the show if conditions should be checked
     */
    iterateThroughSubcategories(
      categoryObject,
      categoryKey,
      frameworkCategoryData: Category,
      dataIdOfP2pDataset: string,
      listOfDataObjects: Array<KpiDataObject>,
      oneP2pDataset: PathwaysToParisData
    ) {
      for (const [subCategoryKey, subCategoryObject] of Object.entries(categoryObject as object) as [
        string,
        object | null
      ][]) {
        if (subCategoryObject == null) continue;
        this.iterateThroughSubcategoryKpis(
          subCategoryObject,
          categoryKey,
          subCategoryKey,
          frameworkCategoryData,
          dataIdOfP2pDataset,
          listOfDataObjects,
          oneP2pDataset
        );
      }
    },
    /**
     * Builds the result Kpi Data Object and adds it to the result list
     * @param subCategoryObject the data object of the framework's subcategory
     * @param categoryKey the key of the corresponding framework's category
     * @param subCategoryKey the key of the corresponding framework's subcategory
     * @param frameworkCategoryData the category object of the framework's category
     * @param dataIdOfP2pDataset the data ID of the P2P dataset
     * @param listOfDataObjects a map containing the category and it's corresponding Kpis
     * @param oneP2pDataset dataset for which the show if conditions should be checked
     */
    iterateThroughSubcategoryKpis(
      subCategoryObject: object,
      categoryKey,
      subCategoryKey: string,
      frameworkCategoryData: Category,
      dataIdOfP2pDataset: string,
      listOfDataObjects: Array<KpiDataObject>,
      oneP2pDataset: PathwaysToParisData
    ) {
      for (const [kpiKey, kpiValue] of Object.entries(subCategoryObject) as [string, object] | null) {
        if (kpiValue == null) continue;
        const subcategory = assertDefined(
          frameworkCategoryData.subcategories.find((subCategory) => subCategory.name === subCategoryKey)
        );
        const field = assertDefined(subcategory.fields.find((field) => field.name == kpiKey));

        //TODO computed property p2pDataAndMetaInfo. Map of fieldName to ShowIfBoolean, Remove when Review coment is closed
        if (field.showIf(oneP2pDataset)) {
          this.createKpiDataObjects(
            kpiKey as string,
            kpiValue as KpiValue,
            subcategory,
            frameworkCategoryData,
            dataIdOfP2pDataset
          );
          listOfDataObjects.push(this.resultKpiData);
        }
      }
    },
    /**
     * Checks whether a given category shall be displayed for at least one of the P2P datasets to display
     * @param categoryName The name of the category to check
     * @returns true if category shall be displayed, else false
     */
    shouldCategoryBeRendered(categoryName: string): boolean {
      const category = assertDefined(p2pDataModel.find((category) => category.label === categoryName));
      return this.p2pDataAndMetaInfo
        .map((dataAndMetaInfo) => dataAndMetaInfo.data)
        .some((singleP2pData) => category.showIf(singleP2pData));
    },
    /**
     * Retrieves the color for a given category from P2P Data Model
     * @param categoryName The name of the category whose color is searched
     * @returns color as string
     */
    colorOfCategory(categoryName: string): string {
      return assertDefined(p2pDataModel.find((category) => category.label === categoryName)).color;
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
  },
});
</script>
<style scoped lang="scss">
.d-category {
  height: 53.33px;
  vertical-align: middle;
}
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
