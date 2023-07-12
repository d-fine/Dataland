<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading P2P Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="mapOfKpiKeysToDataObjectsArrays.size > 0 && !waitingForData">
    <DataTable tableClass="onlyHeaders">
      <Column bodyClass="headers-bg" headerStyle="width: 30vw;" headerClass="horizontal-headers-size" header="KPIs">
      </Column>
      <Column
        v-for="reportingPeriodWithDataId of listOfDataSetReportingPeriods"
        headerClass="horizontal-headers-size"
        headerStyle="width: 30vw;"
        :field="reportingPeriodWithDataId.dataId"
        :header="reportingPeriodWithDataId.reportingPeriod"
        :key="reportingPeriodWithDataId.dataId"
      >
      </Column>
    </DataTable>
    <div v-for="(arrayOfKpiDataObject, index) in mapOfKpiKeysToDataObjectsArrays" :key="index" class="d-table-style">
      <div v-if="shouldCategoryBeRendered(arrayOfKpiDataObject[0])">
        <div>
          <div class="pt-2 pl-2 pb-2 w-full d-cursor-pointer border-bottom-table p-2" @click="toggleExpansion(index)">
            <div v-if="!isExpanded(index)">
              <span :class="`p-badge badge-${colorOfCategory(arrayOfKpiDataObject[0])}`">{{
                arrayOfKpiDataObject[0].toUpperCase()
              }}</span>
              <button class="pt-1 pr-3 d-cursor-pointer d-chevron-style">
                <span class="pr-1 pt-1 pi pi-chevron-right d-chevron-font"></span>
              </button>
            </div>
            <div v-if="isExpanded(index)">
              <span :class="`p-badge badge-${colorOfCategory(arrayOfKpiDataObject[0])}`">{{
                arrayOfKpiDataObject[0].toUpperCase()
              }}</span>
              <button class="pt-2 pr-3 d-cursor-pointer d-chevron-style">
                <span class="pr-1 pi pi-chevron-down d-chevron-font"></span>
              </button>
            </div>
          </div>
        </div>
        <div v-show="isExpanded(index)">
          <P2pCompanyDataTable
            :arrayOfKpiDataObjects="arrayOfKpiDataObject[1]"
            :list-of-reporting-periods-with-data-id="listOfDataSetReportingPeriods"
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
import P2pCompanyDataTable from "@/components/resources/frameworkDataSearch/DisplayFrameworkDataTable.vue";
import { p2pDataModel } from "@/components/resources/frameworkDataSearch/p2p/P2pDataModel";
import { ApiClientProvider } from "@/services/ApiClients";
import { ReportingPeriodOfDataSetWithId, sortReportingPeriodsToDisplayAsColumns } from "@/utils/DataTableDisplay";
import { Category, Subcategory } from "@/utils/GenericFrameworkTypes";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { reformatValueForDisplay } from "@/utils/FrameworkPanelDisplay";
import { DataAndMetaInformationPathwaysToParisData } from "@clients/backend";
import Keycloak from "keycloak-js";
import { defineComponent, inject } from "vue";
import Column from "primevue/column";
import DataTable from "primevue/datatable";

export default defineComponent({
  name: "P2pPanel",
  components: { P2pCompanyDataTable, DataTable, Column },
  data() {
    return {
      firstRender: true,
      waitingForData: true,
      resultKpiData: null as KpiDataObject,
      p2pDataAndMetaInfo: [] as Array<DataAndMetaInformationPathwaysToParisData>,
      listOfDataSetReportingPeriods: [] as Array<ReportingPeriodOfDataSetWithId>,
      mapOfKpiKeysToDataObjects: new Map() as Map<string, KpiDataObject>,
      mapOfKpiKeysToDataObjectsArrays: new Map() as Map<string, Array<KpiDataObject>>,
      expandedGroup: [],
    };
  },
  props: PanelProps,
  watch: {
    companyId() {
      this.listOfDataSetReportingPeriods = [];
      this.fetchP2pData().catch((error) => console.log(error));
    },
    singleDataMetaInfoToDisplay() {
      if (!this.firstRender) {
        this.listOfDataSetReportingPeriods = [];
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
        content: { [dataIdOfP2pDataset]: reformatValueForDisplay(kpiField, kpiValue) },
      } as KpiDataObject;
      if (this.mapOfKpiKeysToDataObjects.has(kpiKey)) {
        Object.assign(kpiData.content, this.mapOfKpiKeysToDataObjects.get(kpiKey)?.content);
      }
      this.mapOfKpiKeysToDataObjects.set(kpiKey, kpiData);
      this.resultKpiData = kpiData;
    },
    /**
     * Retrieves and converts the stored array of LkSG datasets in order to make it displayable in the frontend.
     */
    convertP2pDataToFrontendFormat(): void {
      if (this.p2pDataAndMetaInfo.length) {
        this.p2pDataAndMetaInfo.forEach((oneP2pDataset: DataAndMetaInformationPathwaysToParisData) => {
          const dataIdOfP2pDataset = oneP2pDataset.metaInfo?.dataId ?? "";
          const reportingPeriodOfP2pDataset = oneP2pDataset.metaInfo?.reportingPeriod ?? "";
          this.listOfDataSetReportingPeriods.push({
            dataId: dataIdOfP2pDataset,
            reportingPeriod: reportingPeriodOfP2pDataset,
          });
          for (const [categoryKey, categoryObject] of Object.entries(oneP2pDataset.data) as [string, object] | null) {
            if (categoryObject == null) continue;
            const listOfDataObjects = [];
            const categoryResult = assertDefined(p2pDataModel.find((category) => category.name === categoryKey));
            for (const [subCategoryKey, subCategoryObject] of Object.entries(categoryObject as object) as [
              string,
              object | null
            ][]) {
              if (subCategoryObject == null) continue;
              for (const [kpiKey, kpiValue] of Object.entries(subCategoryObject) as [string, object] | null) {
                if (kpiValue == null) continue;
                const subcategory = assertDefined(
                  p2pDataModel
                    .find((category) => category.name === categoryKey)
                    ?.subcategories.find((subCategory) => subCategory.name === subCategoryKey)
                );
                const field = assertDefined(subcategory.fields.find((field) => field.name == kpiKey));
                if (
                  this.p2pDataAndMetaInfo
                    .map((dataAndMetaInfo) => dataAndMetaInfo.data)
                    .some((singleP2pData) => field.showIf(singleP2pData))
                ) {
                  this.createKpiDataObjects(
                    kpiKey as string,
                    kpiValue as KpiValue,
                    subcategory,
                    categoryResult,
                    dataIdOfP2pDataset
                  );
                  listOfDataObjects.push(this.resultKpiData);
                }
              }
            }

            this.mapOfKpiKeysToDataObjectsArrays.set(categoryResult.label, listOfDataObjects);
          }
        });
      }
      this.listOfDataSetReportingPeriods = sortReportingPeriodsToDisplayAsColumns(
        this.listOfDataSetReportingPeriods as ReportingPeriodOfDataSetWithId[]
      );
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
     *
     * @param key element for which the check should be run
     * @returns if the element is expanded or not
     */
    isExpanded(key: string) {
      return this.expandedGroup.indexOf(key) !== -1;
    },
    /**
     *
     * @param key element for which the check should be run
     */
    toggleExpansion(key: string) {
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
//TODO extract styles to css classes, extract inline style to classes, remove unused code and check if there are already classes which have the styles needed
</style>
