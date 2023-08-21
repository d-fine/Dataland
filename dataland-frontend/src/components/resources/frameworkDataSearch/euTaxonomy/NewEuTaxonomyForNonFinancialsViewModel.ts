import {
    DataAndMetaInformationEuTaxonomyDataForNonFinancials,
    DataMetaInformation,
    DataPointBigDecimal,
    EuTaxonomyActivity,
    EuTaxonomyAlignedActivity,
    EuTaxonomyDataForNonFinancials, EuTaxonomyDetailsPerCashFlowType,
    EuTaxonomyGeneral, FinancialShare
} from "@clients/backend";
import {DataAndMetaInformationViewModel, FrameworkViewModel} from "@/components/resources/ViewModel";

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
    totalNonAlignedShare?: FinancialShareViewModel & { nonAlignedActivities?: EuTaxonomyActivity[] };
    totalAlignedShare?: FinancialShareViewModel & { alignedActivities?: EuTaxonomyAlignedActivity[] };
    enablingAlignedShare?: number;
    transitionalAlignedShare?: number;
}

export class NewEuTaxonomyForNonFinancialsViewModel implements FrameworkViewModel {
    general: { general: EuTaxonomyGeneral };
    revenue?: DetailsPerCashFlowViewModel;
    capex?: DetailsPerCashFlowViewModel;
    opex?: DetailsPerCashFlowViewModel;

    constructor(apiModel: EuTaxonomyDataForNonFinancials) {
        this.general = { general: apiModel.general! }; // TODO must be split into basic information and assurance
        this.revenue = NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowApiModelToViewModel(apiModel.revenue)
        this.capex = NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowApiModelToViewModel(apiModel.capex)
        this.opex = NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowApiModelToViewModel(apiModel.opex)
    }

    toApiModel(): EuTaxonomyDataForNonFinancials {
        return {
            general: this.general.general,
            revenue: NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowViewModelToApiModel()
        }
    }

    private static convertDetailsPerCashFlowApiModelToViewModel(apiModel?: EuTaxonomyDetailsPerCashFlowType): DetailsPerCashFlowViewModel | undefined {
        if(apiModel == undefined) { return undefined; }
        return {
            totalAmount: apiModel.totalAmount,
            totalNonEligibleShare: this.convertFinancialShareApiModelToViewModel(apiModel.totalNonEligibleShare),
            totalEligibleShare: this.convertFinancialShareApiModelToViewModel(apiModel.totalEligibleShare),
            totalNonAlignedShare: {
                ...(NewEuTaxonomyForNonFinancialsViewModel.convertFinancialShareApiModelToViewModel(apiModel.totalNonAlignedShare) ?? {}),
                nonAlignedActivities: apiModel.nonAlignedActivities,
            },
            totalAlignedShare: {
                ...(NewEuTaxonomyForNonFinancialsViewModel.convertFinancialShareApiModelToViewModel(apiModel.totalAlignedShare) ?? {}),
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
            totalNonEligibleShare: NewEuTaxonomyForNonFinancialsViewModel.convertFinancialShareViewModelToApiModel(details.totalNonEligibleShare),
            totalEligibleShare: NewEuTaxonomyForNonFinancialsViewModel.convertFinancialShareViewModelToApiModel(details.totalEligibleShare),
            totalNonAlignedShare: NewEuTaxonomyForNonFinancialsViewModel.convertFinancialShareViewModelToApiModel(details.totalNonAlignedShare),
            totalAlignedShare: NewEuTaxonomyForNonFinancialsViewModel.convertFinancialShareViewModelToApiModel(details.totalAlignedShare),
            nonAlignedActivities: details.totalNonAlignedShare?.nonAlignedActivities,
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
    data: NewEuTaxonomyForNonFinancialsViewModel;

    constructor(dataAndMetaInfoApiModel: DataAndMetaInformationEuTaxonomyDataForNonFinancials) {
        this.metaInfo = dataAndMetaInfoApiModel.metaInfo;
        this.data = new NewEuTaxonomyForNonFinancialsViewModel(dataAndMetaInfoApiModel.data)
    }

    toApiModel(): DataAndMetaInformationEuTaxonomyDataForNonFinancials {
        return {
            metaInfo: this.metaInfo,
            data: this.data.toApiModel(),
        }
    }
}