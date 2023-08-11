import {
    DataAndMetaInformationEuTaxonomyDataForNonFinancials,
    DataMetaInformation,
    DataPointBigDecimal,
    EuTaxonomyActivity,
    EuTaxonomyAlignedActivity,
    EuTaxonomyDataForNonFinancials, EuTaxonomyDetailsPerCashFlowType,
    EuTaxonomyGeneral, FinancialShare
} from "@clients/backend";
import {
    DataAndMetaInformationViewModel,
    FrameworkViewModel
} from "@/components/resources/frameworkDataSearch/ThreeLayerDataTable.vue";

export interface MoneyAmount {
    absoluteAmount?: number;
    currency?: string;
}

interface FinancialShareViewModel {
    percentage?: number;
    absoluteShare?: MoneyAmount;
}

interface DetailsPerCashFlowViewModel {
    totalAmount?: DataPointBigDecimal;
    totalNonEligibleShare?: FinancialShareViewModel;
    totalEligibleShare?: FinancialShareViewModel;
    totalEligibleNotAlignedShare?: FinancialShareViewModel & { activities?: EuTaxonomyActivity[] };
    totalAlignedShare?: FinancialShareViewModel & { alignedActivities?: EuTaxonomyAlignedActivity[] };
    enablingAlignedShare?: number;
    transitionalAlignedShare?: number;
}

export class EuTaxonomyForNonFinancialsViewModel implements FrameworkViewModel {
    general: { general: EuTaxonomyGeneral };
    revenue?: DetailsPerCashFlowViewModel;
    capEx?: DetailsPerCashFlowViewModel;
    opEx?: DetailsPerCashFlowViewModel;

    constructor(apiModel: EuTaxonomyDataForNonFinancials) {
        this.general = { general: apiModel.general! }; // TODO must be split into basic information and assurance
        this.revenue = EuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowApiModelToViewModel(apiModel.revenue)
        this.capEx = EuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowApiModelToViewModel(apiModel.capex)
        this.opEx = EuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowApiModelToViewModel(apiModel.opex)
    }

    toApiModel(): EuTaxonomyDataForNonFinancials {
        return {
            general: this.general.general,
            revenue: EuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowViewModelToApiModel()
        }
    }

    private static convertDetailsPerCashFlowApiModelToViewModel(apiModel?: EuTaxonomyDetailsPerCashFlowType): DetailsPerCashFlowViewModel | undefined {
        if(apiModel == undefined) { return undefined; }
        return {
            totalAmount: apiModel.totalAmount,
            totalNonEligibleShare: this.convertFinancialShareApiModelToViewModel(apiModel.totalNonEligibleShare),
            totalEligibleShare: this.convertFinancialShareApiModelToViewModel(apiModel.totalEligibleShare),
            totalEligibleNotAlignedShare: {
                ...(EuTaxonomyForNonFinancialsViewModel.convertFinancialShareApiModelToViewModel(apiModel.totalEligibleNonAlignedShare) ?? {}),
                alignedActivities: apiModel.alignedActivities,
            },
            totalAlignedShare: {
                ...(EuTaxonomyForNonFinancialsViewModel.convertFinancialShareApiModelToViewModel(apiModel.totalAlignedShare) ?? {}),
                alignedActivities: apiModel.alignedActivities,
            },
            enablingAlignedShare: apiModel.enablingAlignedShare,
            transitionalAlignedShare: apiModel.transitionalAlignedShare,
        }
    };

    private static convertFinancialShareApiModelToViewModel(financialShare?: FinancialShare): FinancialShareViewModel | undefined {
        if(financialShare == undefined) { return undefined; }
        return {
            percentage: financialShare.percentage,
            absoluteShare: {
                absoluteAmount: financialShare?.absoluteShare,
                currency: financialShare?.currency,
            },
        };
    };

    private static convertDetailsPerCashFlowViewModelToApiModel(details?: DetailsPerCashFlowViewModel): EuTaxonomyDetailsPerCashFlowType | undefined {
        if(details == undefined) { return undefined; }
        return {
            totalAmount: details.totalAmount,
            totalNonEligibleShare: EuTaxonomyForNonFinancialsViewModel.convertFinancialShareViewModelToApiModel(details.totalNonEligibleShare),
            totalEligibleShare: EuTaxonomyForNonFinancialsViewModel.convertFinancialShareViewModelToApiModel(details.totalEligibleShare),
            totalEligibleNotAligendShare: EuTaxonomyForNonFinancialsViewModel.convertFinancialShareViewModelToApiModel(details.totalEligibleNotAlignedShare),
            totalAligendShare: EuTaxonomyForNonFinancialsViewModel.convertFinancialShareViewModelToApiModel(details.totalAlignedShare),
            eligibleNotAlignedActivities: details.totalEligibleNotAlignedShare?.activities,
            alignedActivities: details.totalAlignedShare?.alignedActivities,
            enablingAlignedShare: details.enablingAlignedShare,
            transitionalAlignedShare: details.transitionalAlignedShare,
        };
    }

    private static convertFinancialShareViewModelToApiModel(financialShare?: FinancialShareViewModel): FinancialShare | undefined {
        if(financialShare == undefined) { return undefined; }
        return {
            percentage: financialShare.percentage,
            absoluteShare: financialShare.absoluteShare?.absoluteAmount,
            currency: financialShare.absoluteShare?.currency,
        };
    };
}

export class DataAndMetaInformationEuTaxonomyForNonFinancialsViewModel implements DataAndMetaInformationViewModel {
    metaInfo: DataMetaInformation;
    data: EuTaxonomyForNonFinancialsViewModel;

    constructor(dataAndMetaInfoApiModel: DataAndMetaInformationEuTaxonomyDataForNonFinancials) {
        this.metaInfo = dataAndMetaInfoApiModel.metaInfo;
        this.data = new EuTaxonomyForNonFinancialsViewModel(dataAndMetaInfoApiModel.data)
    }

    toApiModel(): DataAndMetaInformationEuTaxonomyDataForNonFinancials {
        return {
            metaInfo: this.metaInfo,
            data: this.data.toApiModel(),
        }
    }
}