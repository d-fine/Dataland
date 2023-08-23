import {
  type AssuranceDataAssuranceEnum,
  type DataAndMetaInformationNewEuTaxonomyDataForNonFinancials,
  type DataMetaInformation,
  type DataPointOneValueAmountWithCurrency,
  type EuTaxonomyActivity,
  type EuTaxonomyAlignedActivity,
  type FiscalYearDeviation,
  type NewEuTaxonomyDataForNonFinancials,
  type NewEuTaxonomyDetailsPerCashFlowType,
  type RelativeAndAbsoluteFinancialShare,
  type YesNo,
  type YesNoNa,
} from "@clients/backend";
import { type DataAndMetaInformationViewModel, type FrameworkViewModel } from "@/components/resources/ViewModel";

interface NewEuTaxonomyDetailsPerCashFlowViewModel {
  totalAmount?: DataPointOneValueAmountWithCurrency;
  totalNonEligibleShare?: RelativeAndAbsoluteFinancialShare;
  totalEligibleShare?: RelativeAndAbsoluteFinancialShare;
  totalNonAlignedShare?: RelativeAndAbsoluteFinancialShare & { nonAlignedActivities?: EuTaxonomyActivity[] };
  totalAlignedShare?: RelativeAndAbsoluteFinancialShare & {
    alignedActivities?: EuTaxonomyAlignedActivity[];
    substantialContributionCriteria?: { [key: string]: number };
  };
  totalEnablingShare?: number;
  totalTransitionalShare?: number;
}

export class NewEuTaxonomyForNonFinancialsViewModel implements FrameworkViewModel {
  basicInformation?: {
    basicInformation: {
      fiscalYearDeviation?: FiscalYearDeviation;
      fiscalYearEnd?: string;
      scopeOfEntities?: YesNoNa;
      nfrdMandatory?: YesNo;
      euTaxonomyActivityLevelReporting?: YesNo;
      numberOfEmployees?: number;
    };
  };
  assurance: {
    assurance: {
      levelOfAssurance: AssuranceDataAssuranceEnum;
      assuranceProvider?: string;
    };
  }; // TODO type this properly later
  revenue?: NewEuTaxonomyDetailsPerCashFlowViewModel;
  capex?: NewEuTaxonomyDetailsPerCashFlowViewModel;
  opex?: NewEuTaxonomyDetailsPerCashFlowViewModel;

  constructor(apiModel: NewEuTaxonomyDataForNonFinancials) {
    this.basicInformation = {
      basicInformation: {
        fiscalYearDeviation: apiModel.general?.fiscalYearDeviation,
        fiscalYearEnd: apiModel.general?.fiscalYearEnd,
        scopeOfEntities: apiModel.general?.scopeOfEntities,
        euTaxonomyActivityLevelReporting: apiModel.general?.euTaxonomyActivityLevelReporting,
        numberOfEmployees: apiModel.general?.numberOfEmployees,
        nfrdMandatory: apiModel.general?.nfrdMandatory,
      },
    };
    this.assurance = {
      assurance: {
        levelOfAssurance: apiModel.general!.assurance!.assurance, // TODO undefined safety
        assuranceProvider: apiModel.general?.assurance?.provider,
      },
    };
    this.revenue = NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowApiModelToViewModel(
      apiModel.revenue,
    );
    this.capex = NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowApiModelToViewModel(apiModel.capex);
    this.opex = NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowApiModelToViewModel(apiModel.opex);
  }

  toApiModel(): NewEuTaxonomyDataForNonFinancials {
    return {
      general: {
        fiscalYearDeviation: this.basicInformation?.basicInformation?.fiscalYearDeviation,
        fiscalYearEnd: this.basicInformation?.basicInformation?.fiscalYearEnd,
        scopeOfEntities: this.basicInformation?.basicInformation?.scopeOfEntities,
        nfrdMandatory: this.basicInformation?.basicInformation?.nfrdMandatory,
        euTaxonomyActivityLevelReporting: this.basicInformation?.basicInformation?.euTaxonomyActivityLevelReporting,
        assurance: {
          assurance: this.assurance.assurance.levelOfAssurance,
          provider: this.assurance.assurance.assuranceProvider,
        },
        numberOfEmployees: this.basicInformation?.basicInformation.numberOfEmployees,
      },
      revenue: NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowViewModelToApiModel(this.revenue),
      capex: NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowViewModelToApiModel(this.capex),
      opex: NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowViewModelToApiModel(this.opex),
    };
  }

  private static convertDetailsPerCashFlowApiModelToViewModel(
    apiModel?: NewEuTaxonomyDetailsPerCashFlowType,
  ): NewEuTaxonomyDetailsPerCashFlowViewModel | undefined {
    if (apiModel == undefined) {
      return undefined;
    }
    return {
      totalAmount: apiModel.totalAmount,
      totalEligibleShare: apiModel.totalEligibleShare,
      totalAlignedShare: {
        ...(apiModel.totalAlignedShare ?? {}),
        ...apiModel.substantialContributionCriteria,
        alignedActivities: apiModel.alignedActivities,
      },
      totalNonAlignedShare: {
        ...(apiModel.totalNonAlignedShare ?? {}),
        nonAlignedActivities: apiModel.nonAlignedActivities,
      },
      totalNonEligibleShare: apiModel.totalNonEligibleShare,
      totalEnablingShare: apiModel.totalEnablingShare,
      totalTransitionalShare: apiModel.totalTransitionalShare,
    };
  }

  private static convertDetailsPerCashFlowViewModelToApiModel(
    details?: NewEuTaxonomyDetailsPerCashFlowViewModel,
  ): NewEuTaxonomyDetailsPerCashFlowType | undefined {
    if (details == undefined) {
      return undefined;
    }
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

export class DataAndMetaInformationNewEuTaxonomyForNonFinancialsViewModel
  implements DataAndMetaInformationViewModel<NewEuTaxonomyForNonFinancialsViewModel>
{
  metaInfo: DataMetaInformation;
  data: NewEuTaxonomyForNonFinancialsViewModel;

  constructor(dataAndMetaInfoApiModel: DataAndMetaInformationNewEuTaxonomyDataForNonFinancials) {
    this.metaInfo = dataAndMetaInfoApiModel.metaInfo;
    this.data = new NewEuTaxonomyForNonFinancialsViewModel(dataAndMetaInfoApiModel.data);
  }

  toApiModel(): DataAndMetaInformationNewEuTaxonomyDataForNonFinancials {
    return {
      metaInfo: this.metaInfo,
      data: this.data.toApiModel(),
    };
  }
}
