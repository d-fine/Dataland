<template>
    <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">Loading {{ humanizeString(DataTypeEnum.EutaxonomyNonFinancials) }} Data...</p>
        <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f"/>
    </div>
    <div v-show="!waitingForData">
        <ThreeLayerTable
            :data-model="euTaxonomyForNonFinancialsDisplayDataModel"
            :data-and-meta-info="convertedDataAndMetaInfo"
            @data-converted="handleFinishedDataConversion"
            :format-value-for-display="formatValueForDisplay"
            :modal-column-headers="euTaxonomyForNonFinancialsModalColumnHeaders"
        />
    </div>
</template>

<script lang="ts">
import {PanelProps} from "@/components/resources/frameworkDataSearch/PanelComponentOptions";
import {ApiClientProvider} from "@/services/ApiClients";
import {assertDefined} from "@/utils/TypeScriptUtils";
import {
    DataAndMetaInformationEuTaxonomyDataForNonFinancials,
    DataTypeEnum,
} from "@clients/backend";
import Keycloak from "keycloak-js";
import {defineComponent, inject} from "vue";
import {humanizeString} from "@/utils/StringHumanizer";
import ThreeLayerTable from "@/components/resources/frameworkDataSearch/ThreeLayerDataTable.vue";
import {KpiValue} from "@/components/resources/frameworkDataSearch/KpiDataObject";
import { Field, Subcategory } from "@/utils/GenericFrameworkTypes";
import {
    euTaxonomyForNonFinancialsModalColumnHeaders
} from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsModalColumnHeaders";
import {
    newEuTaxonomyForNonFinancialsDisplayDataModel
} from "@/components/resources/frameworkDataSearch/euTaxonomy/NewEuTaxonomyForNonFinancialsDisplayDataModel";
import {
    DataAndMetaInformationEuTaxonomyForNonFinancialsViewModel,
    MoneyAmount
} from "@/components/resources/frameworkDataSearch/euTaxonomy/NewEuTaxonomyForNonFinancialsViewModel";

export default defineComponent({
    name: "EuTaxonomyForNonFinancialsPanel",
    components: {ThreeLayerTable},
    data() {
        return {
            DataTypeEnum,
            firstRender: true,
            waitingForData: true,
            convertedDataAndMetaInfo: [] as Array<DataAndMetaInformationEuTaxonomyForNonFinancialsViewModel>,
            euTaxonomyForNonFinancialsModalColumnHeaders,
            euTaxonomyForNonFinancialsDisplayDataModel: newEuTaxonomyForNonFinancialsDisplayDataModel,
        };
    },
    props: PanelProps,
    watch: {
        companyId() {
            this.fetchSmeData().catch((error) => console.log(error));
        },
        singleDataMetaInfoToDisplay() {
            if (!this.firstRender) {
                this.fetchSmeData().catch((error) => console.log(error));
            }
        },
    },
    setup() {
        return {
            getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
        };
    },
    created() {
        this.fetchSmeData().catch((error) => console.log(error));
        this.firstRender = false;
    },

    methods: {
        humanizeString,
        /**
         * Fetches all accepted SME datasets for the current company and converts them to the required frontend format.
         */
        async fetchSmeData() {
            try {
                let fetchedData: DataAndMetaInformationEuTaxonomyDataForNonFinancials[];
                this.waitingForData = true;
                const euTaxonomyForNonFinancialsDataControllerApi = await new ApiClientProvider(
                    assertDefined(this.getKeycloakPromise)(),
                ).getEuTaxonomyDataForNonFinancialsControllerApi();
                if (this.singleDataMetaInfoToDisplay) {
                    const singleeuTaxonomyForNonFinancialsDataData = (
                        await euTaxonomyForNonFinancialsDataControllerApi.getCompanyAssociatedEuTaxonomyDataForNonFinancials(this.singleDataMetaInfoToDisplay.dataId)
                    ).data.data;
                    fetchedData = [{metaInfo: this.singleDataMetaInfoToDisplay, data: singleeuTaxonomyForNonFinancialsDataData}];
                } else {
                    fetchedData = (
                        await euTaxonomyForNonFinancialsDataControllerApi.getAllCompanyEuTaxonomyDataForNonFinancials(assertDefined(this.companyId))
                    ).data;
                }
                this.convertedDataAndMetaInfo = fetchedData.map((dataAndMetaInfo) => (
                    new DataAndMetaInformationEuTaxonomyForNonFinancialsViewModel(dataAndMetaInfo)
                ));
            } catch (error) {
                console.error(error);
            }
        },
        /**
         * Handles the ThreeLayerTableEvent of finishing the data conversion
         */
        handleFinishedDataConversion() {
            this.waitingForData = false;
        },
        /**
         * Formats KPI values for display
         * @param field the considered KPI field
         * @param value the value to be formatted
         * @returns the formatted value
         */
        formatValueForDisplay(field: Field, value: KpiValue): KpiValue {
            if (value == null) {
                return value;
            } else if (field.name == "percentage") {
                return `${value} %`;
            } else if (field.name == "absoluteShare") {
                const moneyAmount = value as MoneyAmount;
                if(moneyAmount.absoluteAmount == undefined) { return null; }
                return `${moneyAmount.absoluteAmount}` + moneyAmount.currency ? ` ${moneyAmount.currency}` : "";
            }
            return value;
        },
    },
});
</script>
