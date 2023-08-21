import {
    DataAndMetaInformationNewEuTaxonomyDataForNonFinancials,
    DataMetaInformation, DataPointOneValueAmountWithCurrency,
    EuTaxonomyActivity,
    EuTaxonomyAlignedActivity,
    EuTaxonomyGeneral,
    NewEuTaxonomyDataForNonFinancials,
    NewEuTaxonomyDetailsPerCashFlowType,
    RelativeAndAbsoluteFinancialShare
} from "@clients/backend";
import {DataAndMetaInformationViewModel, FrameworkViewModel} from "@/components/resources/ViewModel";

interface DetailsPerCashFlowViewModel { // TODO
    totalAmount?: DataPointOneValueAmountWithCurrency;
    totalNonEligibleShare?: RelativeAndAbsoluteFinancialShare;
    totalEligibleShare?: RelativeAndAbsoluteFinancialShare;
    totalNonAlignedShare?: RelativeAndAbsoluteFinancialShare & { nonAlignedActivities?: EuTaxonomyActivity[] };
    totalAlignedShare?: RelativeAndAbsoluteFinancialShare & { alignedActivities?: EuTaxonomyAlignedActivity[], substantialContributionCriteria?: { [key: string]: number; } };
    totalEnablingShare?: number;
    totalTransitionalShare?: number;
}

export class NewEuTaxonomyForNonFinancialsViewModel implements FrameworkViewModel {
    general?: { general?: EuTaxonomyGeneral };
    revenue?: DetailsPerCashFlowViewModel;
    capex?: DetailsPerCashFlowViewModel;
    opex?: DetailsPerCashFlowViewModel;

    constructor(apiModel: NewEuTaxonomyDataForNonFinancials) {
        this.general = { general: apiModel.general! }; // TODO must be split into basic information and assurance
        this.revenue = NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowApiModelToViewModel(apiModel.revenue)
        this.capex = NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowApiModelToViewModel(apiModel.capex)
        this.opex = NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowApiModelToViewModel(apiModel.opex)
    }

    toApiModel(): NewEuTaxonomyDataForNonFinancials {
        return {
            general: this.general?.general,
            revenue: NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowViewModelToApiModel(this.revenue),
            capex: NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowViewModelToApiModel(this.capex),
            opex: NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowViewModelToApiModel(this.opex),
        }
    }

    private static convertDetailsPerCashFlowApiModelToViewModel(apiModel?: NewEuTaxonomyDetailsPerCashFlowType): DetailsPerCashFlowViewModel | undefined {
        if(apiModel == undefined) { return undefined; }
        return {
            totalAmount: apiModel.totalAmount,
            totalNonEligibleShare: apiModel.totalNonEligibleShare,
            totalEligibleShare: apiModel.totalEligibleShare,
            totalNonAlignedShare: {
                ...apiModel.totalNonAlignedShare ?? {},   // TODO investigate:  Waspassiert bei ... von undefined
                nonAlignedActivities: apiModel.nonAlignedActivities,
            },
            totalAlignedShare: {
                ...apiModel.totalAlignedShare ?? {},  // TODO investigate:  Waspassiert bei ... von undefined
                alignedActivities: apiModel.alignedActivities,
                substantialContributionCriteria: apiModel.substantialContributionCriteria
            },
            totalEnablingShare: apiModel.totalEnablingShare,
            totalTransitionalShare: apiModel.totalTransitionalShare,
        }
    };

    private static convertDetailsPerCashFlowViewModelToApiModel(details?: DetailsPerCashFlowViewModel): NewEuTaxonomyDetailsPerCashFlowType | undefined {
        if(details == undefined) { return undefined; }
        return {
            totalAmount: details.totalAmount,
            totalNonEligibleShare: details.totalNonEligibleShare,
            totalEligibleShare: details.totalEligibleShare,
            totalNonAlignedShare: details.totalNonAlignedShare,
            totalAlignedShare: details.totalAlignedShare,
            nonAlignedActivities: details.totalNonAlignedShare?.nonAlignedActivities,
            alignedActivities: details.totalAlignedShare?.alignedActivities,
            totalEnablingShare: details.totalEnablingShare,
            totalTransitionalShare: details.totalTransitionalShare,
        };
    }
}

export class DataAndMetaInformationEuTaxonomyForNonFinancialsViewModel implements DataAndMetaInformationViewModel<NewEuTaxonomyForNonFinancialsViewModel> {
    metaInfo: DataMetaInformation;
    data: NewEuTaxonomyForNonFinancialsViewModel;

    constructor(dataAndMetaInfoApiModel: DataAndMetaInformationNewEuTaxonomyDataForNonFinancials) {
        this.metaInfo = dataAndMetaInfoApiModel.metaInfo;
        this.data = new NewEuTaxonomyForNonFinancialsViewModel(dataAndMetaInfoApiModel.data)
    }

    toApiModel(): DataAndMetaInformationNewEuTaxonomyDataForNonFinancials {
        return {
            metaInfo: this.metaInfo,
            data: this.data.toApiModel(),
        }
    }
}