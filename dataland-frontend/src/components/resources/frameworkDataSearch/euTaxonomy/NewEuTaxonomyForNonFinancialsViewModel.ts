import {
  AssuranceDataAssuranceEnum,
  type DataAndMetaInformationEuTaxonomyDataForNonFinancials,
  type DataMetaInformation,
  type DataPointOneValueAmountWithCurrency,
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
import { EnvironmentalObjective } from "@/api-models/EnvironmentalObjective";

interface NewEuTaxonomyDetailsPerCashFlowViewModel {
  totalAmount?: DataPointOneValueAmountWithCurrency;
  totalNonEligibleShare?: RelativeAndAbsoluteFinancialShare;
  totalEligibleShare?: RelativeAndAbsoluteFinancialShare;
  totalNonAlignedShare?: RelativeAndAbsoluteFinancialShare & { nonAlignedActivities?: EuTaxonomyActivity[] };
  totalAlignedShare?: RelativeAndAbsoluteFinancialShare & {
    alignedActivities?: EuTaxonomyAlignedActivity[];
    [EnvironmentalObjective.ClimateMitigation]?: number;
    [EnvironmentalObjective.ClimateAdaptation]?: number;
    [EnvironmentalObjective.Water]?: number;
    [EnvironmentalObjective.CircularEconomy]?: number;
    [EnvironmentalObjective.PollutionPrevention]?: number;
    [EnvironmentalObjective.Biodiversity]?: number;
  };
  totalEnablingShare?: { totalEnablingShare?: number };
  totalTransitionalShare?: { totalTransitionalShare?: number };
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
  };
  revenue?: NewEuTaxonomyDetailsPerCashFlowViewModel;
  capex?: NewEuTaxonomyDetailsPerCashFlowViewModel;
  opex?: NewEuTaxonomyDetailsPerCashFlowViewModel;

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
    this.revenue = NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowApiModelToViewModel(
      apiModel.revenue,
    );
    this.capex = NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowApiModelToViewModel(apiModel.capex);
    this.opex = NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowApiModelToViewModel(apiModel.opex);
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
      revenue: NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowViewModelToApiModel(this.revenue),
      capex: NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowViewModelToApiModel(this.capex),
      opex: NewEuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowViewModelToApiModel(this.opex),
    };
  }

  private static convertDetailsPerCashFlowApiModelToViewModel(
    apiModel?: EuTaxonomyDetailsPerCashFlowType,
  ): NewEuTaxonomyDetailsPerCashFlowViewModel | undefined {
    if (apiModel == undefined) {
      return undefined;
    }
    return {
      totalAmount: apiModel.totalAmount,
      totalEligibleShare: apiModel.totalEligibleShare,
      totalAlignedShare: {
        ...(apiModel.totalAlignedShare ?? {}),
        [EnvironmentalObjective.ClimateMitigation]: apiModel.substantialContributionToClimateChangeMitigation,
        [EnvironmentalObjective.ClimateAdaptation]: apiModel.substantialContributionToClimateChangeAdaption,
        [EnvironmentalObjective.Water]: apiModel.substantialContributionToSustainableWaterUse,
        [EnvironmentalObjective.CircularEconomy]: apiModel.substantialContributionToCircularEconomy,
        [EnvironmentalObjective.PollutionPrevention]: apiModel.substantialContributionToPollutionPreventionAndControl,
        [EnvironmentalObjective.Biodiversity]: apiModel.substantialContributionToBiodiversity,
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
    details?: NewEuTaxonomyDetailsPerCashFlowViewModel,
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
      substantialContributionToClimateChangeMitigation:
        details.totalAlignedShare?.[EnvironmentalObjective.ClimateMitigation],
      substantialContributionToClimateChangeAdaption:
        details.totalAlignedShare?.[EnvironmentalObjective.ClimateAdaptation],
      substantialContributionToSustainableWaterUse: details.totalAlignedShare?.[EnvironmentalObjective.Water],
      substantialContributionToCircularEconomy: details.totalAlignedShare?.[EnvironmentalObjective.CircularEconomy],
      substantialContributionToPollutionPreventionAndControl:
        details.totalAlignedShare?.[EnvironmentalObjective.PollutionPrevention],
      substantialContributionToBiodiversity: details.totalAlignedShare?.[EnvironmentalObjective.Biodiversity],
      totalEnablingShare: details.totalEnablingShare?.totalEnablingShare,
      totalTransitionalShare: details.totalTransitionalShare?.totalTransitionalShare,
    };
  }
}

export class DataAndMetaInformationNewEuTaxonomyForNonFinancialsViewModel
  implements DataAndMetaInformationViewModel<NewEuTaxonomyForNonFinancialsViewModel>
{
  metaInfo: DataMetaInformation;
  data: NewEuTaxonomyForNonFinancialsViewModel;

  constructor(dataAndMetaInfoApiModel: DataAndMetaInformationEuTaxonomyDataForNonFinancials) {
    this.metaInfo = dataAndMetaInfoApiModel.metaInfo;
    this.data = new NewEuTaxonomyForNonFinancialsViewModel(dataAndMetaInfoApiModel.data);
  }

  toApiModel(): DataAndMetaInformationEuTaxonomyDataForNonFinancials {
    return {
      metaInfo: this.metaInfo,
      data: this.data.toApiModel(),
    };
  }
}
