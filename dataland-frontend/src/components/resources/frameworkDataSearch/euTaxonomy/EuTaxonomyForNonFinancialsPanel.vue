<template>
    <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">Loading {{ humanizeString(DataTypeEnum.EutaxonomyNonFinancials) }} Data...</p>
        <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f"/>
    </div>
    <div v-show="!waitingForData">
        <ThreeLayerTable
            :data-model="euTaxonomyForNonFinancialsDataModel"
            :data-and-meta-info="convertedDataAndMetaInfo"
            @data-converted="handleFinishedDataConversion"
            :format-value-for-display="formatValueForDisplay"
            :modal-column-headers="euTaxonomyForNonFinancialsModalColumnHeaders"
        />
    </div>
</template>

<script lang="ts">
import {PanelProps} from "@/components/resources/frameworkDataSearch/PanelComponentOptions";
import {smeDataModel} from "@/components/resources/frameworkDataSearch/sme/SmeDataModel";
import {ApiClientProvider} from "@/services/ApiClients";
import {assertDefined} from "@/utils/TypeScriptUtils";
import {
    DataAndMetaInformationEuTaxonomyDataForNonFinancials,
    DataAndMetaInformationSmeData,
    DataMetaInformation,
    DataTypeEnum,
    EuTaxonomyActivity,
    EuTaxonomyAlignedActivity,
    EuTaxonomyDataForNonFinancials,
    EuTaxonomyDetailsPerCashFlowType,
    EuTaxonomyGeneral, FinancialShare,
    SmeProduct,
    SmeProductionSite
} from "@clients/backend";
import Keycloak from "keycloak-js";
import {defineComponent, inject} from "vue";
import {humanizeString} from "@/utils/StringHumanizer";
import ThreeLayerTable from "@/components/resources/frameworkDataSearch/ThreeLayerDataTable.vue";
import {KpiValue} from "@/components/resources/frameworkDataSearch/KpiDataObject";
import {Field} from "@/utils/GenericFrameworkTypes";
import {smeModalColumnHeaders} from "@/components/resources/frameworkDataSearch/sme/SmeModalColumnHeaders";
import {convertToMillions} from "@/utils/NumberConversionUtils";
import {
    euTaxonomyForNonFinancialsDataModel
} from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsDataModel";
import {
    euTaxonomyForNonFinancialsModalColumnHeaders
} from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsModalColumnHeaders";

export default defineComponent({
    name: "EuTaxonomyForNonFinancialsPanel",
    computed: {
        euTaxonomyForNonFinancialsDataModel() {
            return euTaxonomyForNonFinancialsDataModel
        }
    },
    components: {ThreeLayerTable},
    data() {
        return {
            DataTypeEnum,
            euTaxonomyForNonFinancialsDataModel,
            firstRender: true,
            waitingForData: true,
            convertedDataAndMetaInfo: [] as Array<DataAndMetaInformationEuTaxonomyForNonFinancialsViewModel>,
            euTaxonomyForNonFinancialsModalColumnHeaders,
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
                this.convertedDataAndMetaInfo = this.convertApiModelToViewModel(fetchedData);
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

        convertApiModelToViewModel(dataAndMetaInfos: DataAndMetaInformationEuTaxonomyDataForNonFinancials[]): DataAndMetaInformationEuTaxonomyForNonFinancialsViewModel[] {
            return dataAndMetaInfos.map((dataAndMetaInfo) => {
                const apiModel = dataAndMetaInfo.data
                let convertedModel = { general: { general: apiModel.general! }} as EuTaxonomyForNonFinancialsViewModel // TODO must be split into basic information and assurance
                convertedModel.revenue = this.convertDetailsPerCashFlowApiModelToViewModel(apiModel.revenue)
                convertedModel.capEx = this.convertDetailsPerCashFlowApiModelToViewModel(apiModel.capex)
                convertedModel.opEx = this.convertDetailsPerCashFlowApiModelToViewModel(apiModel.opex)
                return { metaInfo: dataAndMetaInfo.metaInfo, data: convertedModel };
            });
        },

        convertDetailsPerCashFlowApiModelToViewModel(apiModel?: EuTaxonomyDetailsPerCashFlowType): DetailsPerCashFlowViewModel | undefined {
            if(apiModel == undefined) { return undefined; }
            return {
                totalAmount: apiModel.totalAmount,
                totalNonEligibleShare: this.convertFinancialShareApiModelToViewModel(apiModel.totalNonEligibleShare),
                totalEligibleShare: this.convertFinancialShareApiModelToViewModel(apiModel.totalEligibleShare),
                totalEligibleNotAlignedShare: {
                    ...(this.convertFinancialShareApiModelToViewModel(apiModel.totalEligibleNonAlignedShare) ?? {}),
                    alignedActivities: apiModel.alignedActivities,
                },
                totalAlignedShare: {
                    ...(this.convertFinancialShareApiModelToViewModel(apiModel.totalAlignedShare) ?? {}),
                    alignedActivities: apiModel.alignedActivities,
                },
                enablingAlignedShare: apiModel.enablingAlignedShare,
                transitionalAlignedShare: apiModel.transitionalAlignedShare,
            }
        },

        convertFinancialShareApiModelToViewModel(financialShare?: FinancialShare): FinancialShareViewModel | undefined {
            if(financialShare == undefined) { return undefined; }
            return {
                percentage: financialShare.percentage,
                absoluteShare: {
                    absoluteAmount: financialShare?.absoluteShare,
                    currency: financialShare?.currency,
                },
            };
        },
    },
});

interface MoneyAmount {
    absoluteAmount?: number;
    currency?: string;
}

interface FinancialShareViewModel {
    percentage?: number;
    absoluteShare?: MoneyAmount;
}

interface DetailsPerCashFlowViewModel {
    totalAmount?: { value?: number };
    totalNonEligibleShare?: FinancialShareViewModel;
    totalEligibleShare?: FinancialShareViewModel;
    totalEligibleNotAlignedShare?: FinancialShareViewModel & { activities?: EuTaxonomyActivity[] };
    totalAlignedShare?: FinancialShareViewModel & { alignedActivities?: EuTaxonomyAlignedActivity[] };
    enablingAlignedShare?: number;
    transitionalAlignedShare?: number;
}

interface EuTaxonomyForNonFinancialsViewModel {
    general: { general: EuTaxonomyGeneral };
    revenue?: DetailsPerCashFlowViewModel;
    capEx?: DetailsPerCashFlowViewModel;
    opEx?: DetailsPerCashFlowViewModel;
}

interface DataAndMetaInformationEuTaxonomyForNonFinancialsViewModel {
    metaInfo: DataMetaInformation;
    data: EuTaxonomyForNonFinancialsViewModel;
}

</script>
