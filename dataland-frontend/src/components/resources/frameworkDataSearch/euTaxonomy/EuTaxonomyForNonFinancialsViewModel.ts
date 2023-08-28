import {
  AssuranceDataAssuranceEnum,
  type DataAndMetaInformationEuTaxonomyDataForNonFinancials,
  type DataMetaInformation,
  type DataPointOneValueBigDecimal,
  type EuTaxonomyActivity,
  type EuTaxonomyAlignedActivity,
  type FiscalYearDeviation,
  type EuTaxonomyDataForNonFinancials,
  type EuTaxonomyDetailsPerCashFlowType,
  type RelativeAndAbsoluteFinancialShare,
  type YesNo,
  type YesNoNa,
} from "@clients/backend";
import { type DataAndMetaInformationViewModel, type FrameworkViewModel } from "@/components/resources/ViewModel";

interface EuTaxonomyDetailsPerCashFlowViewModel {
  totalAmount?: DataPointOneValueBigDecimal;
  totalNonEligibleShare?: RelativeAndAbsoluteFinancialShare;
  totalEligibleShare?: RelativeAndAbsoluteFinancialShare;
  totalNonAlignedShare?: RelativeAndAbsoluteFinancialShare & { nonAlignedActivities?: EuTaxonomyActivity[] };
  totalAlignedShare?: RelativeAndAbsoluteFinancialShare & {
    alignedActivities?: EuTaxonomyAlignedActivity[];
    substantialContributionCriteria?: { [key: string]: number };
  };
  totalEnablingShare?: { totalEnablingShare?: number };
  totalTransitionalShare?: { totalTransitionalShare?: number };
}

export class EuTaxonomyForNonFinancialsViewModel implements FrameworkViewModel {
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
  };
  revenue?: EuTaxonomyDetailsPerCashFlowViewModel;
  capex?: EuTaxonomyDetailsPerCashFlowViewModel;
  opex?: EuTaxonomyDetailsPerCashFlowViewModel;

  constructor(apiModel: EuTaxonomyDataForNonFinancials) {
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
        levelOfAssurance: apiModel.general?.assurance?.assurance ?? AssuranceDataAssuranceEnum.None,
        assuranceProvider: apiModel.general?.assurance?.provider,
      },
    };
    this.revenue = EuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowApiModelToViewModel(apiModel.revenue);
    this.capex = EuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowApiModelToViewModel(apiModel.capex);
    this.opex = EuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowApiModelToViewModel(apiModel.opex);
  }

  toApiModel(): EuTaxonomyDataForNonFinancials {
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
      revenue: EuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowViewModelToApiModel(this.revenue),
      capex: EuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowViewModelToApiModel(this.capex),
      opex: EuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowViewModelToApiModel(this.opex),
    };
  }

  private static convertDetailsPerCashFlowApiModelToViewModel(
    apiModel?: EuTaxonomyDetailsPerCashFlowType,
  ): EuTaxonomyDetailsPerCashFlowViewModel | undefined {
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
      totalEnablingShare: { totalEnablingShare: apiModel.totalEnablingShare },
      totalTransitionalShare: { totalTransitionalShare: apiModel.totalTransitionalShare },
    };
  }

  private static convertDetailsPerCashFlowViewModelToApiModel(
    details?: EuTaxonomyDetailsPerCashFlowViewModel,
  ): EuTaxonomyDetailsPerCashFlowType | undefined {
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
      totalEnablingShare: details.totalEnablingShare?.totalEnablingShare,
      totalTransitionalShare: details.totalTransitionalShare?.totalTransitionalShare,
    };
  }
}

export class DataAndMetaInformationEuTaxonomyForNonFinancialsViewModel
  implements DataAndMetaInformationViewModel<EuTaxonomyForNonFinancialsViewModel>
{
  metaInfo: DataMetaInformation;
  data: EuTaxonomyForNonFinancialsViewModel;

  constructor(dataAndMetaInfoApiModel: DataAndMetaInformationEuTaxonomyDataForNonFinancials) {
    this.metaInfo = dataAndMetaInfoApiModel.metaInfo;
    this.data = new EuTaxonomyForNonFinancialsViewModel(dataAndMetaInfoApiModel.data);
  }

  toApiModel(): DataAndMetaInformationEuTaxonomyDataForNonFinancials {
    return {
      metaInfo: this.metaInfo,
      data: this.data.toApiModel(),
    };
  }
}
