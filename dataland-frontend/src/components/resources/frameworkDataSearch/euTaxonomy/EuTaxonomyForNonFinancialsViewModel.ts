import {
  type AssuranceDataAssuranceEnum,
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

interface EuTaxonomyDetailsPerCashFlowViewModel {
  totalAmount?: DataPointOneValueAmountWithCurrency;
  nonEligibleShare?: RelativeAndAbsoluteFinancialShare;
  eligibleShare?: RelativeAndAbsoluteFinancialShare;
  nonAlignedShare?: RelativeAndAbsoluteFinancialShare & { nonAlignedActivities?: EuTaxonomyActivity[] };
  alignedShare?: RelativeAndAbsoluteFinancialShare & {
    alignedActivities?: EuTaxonomyAlignedActivity[];
    substantialContributionToClimateChangeMitigation?: number;
    substantialContributionToClimateChangeAdaption?: number;
    substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResources?: number;
    substantialContributionToTransitionToACircularEconomy?: number;
    substantialContributionToPollutionPreventionAndControl?: number;
    substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystems?: number;
  };
  enablingShare?: { enablingShare?: number };
  transitionalShare?: { transitionalShare?: number };
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
  assurance?: {
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
    this.assurance =
      apiModel.general?.assurance?.assurance == undefined
        ? undefined
        : {
            assurance: {
              levelOfAssurance: apiModel.general.assurance.assurance,
              assuranceProvider: apiModel.general.assurance.provider,
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
        assurance:
          this.assurance == undefined
            ? undefined
            : {
                assurance: this.assurance?.assurance.levelOfAssurance,
                provider: this.assurance?.assurance.assuranceProvider,
              },
        numberOfEmployees: this.basicInformation?.basicInformation?.numberOfEmployees,
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
      eligibleShare: apiModel.eligibleShare,
      alignedShare: {
        ...(apiModel.alignedShare ?? {}),
        substantialContributionToClimateChangeMitigation: apiModel.substantialContributionToClimateChangeMitigation,
        substantialContributionToClimateChangeAdaption: apiModel.substantialContributionToClimateChangeAdaption,
        substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResources:
          apiModel.substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResources,
        substantialContributionToTransitionToACircularEconomy:
          apiModel.substantialContributionToTransitionToACircularEconomy,
        substantialContributionToPollutionPreventionAndControl:
          apiModel.substantialContributionToPollutionPreventionAndControl,
        substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystems:
          apiModel.substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystems,
        alignedActivities: apiModel.alignedActivities,
      },
      nonAlignedShare: {
        ...(apiModel.nonAlignedShare ?? {}),
        nonAlignedActivities: apiModel.nonAlignedActivities,
      },
      nonEligibleShare: apiModel.nonEligibleShare,
      enablingShare: { enablingShare: apiModel.enablingShare },
      transitionalShare: { transitionalShare: apiModel.transitionalShare },
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
      nonEligibleShare: details.nonEligibleShare,
      eligibleShare: details.eligibleShare,
      nonAlignedShare: details.nonAlignedShare,
      alignedShare: details.alignedShare,
      nonAlignedActivities: details.nonAlignedShare?.nonAlignedActivities,
      alignedActivities: details.alignedShare?.alignedActivities,
      substantialContributionToClimateChangeMitigation:
        details.alignedShare?.substantialContributionToClimateChangeMitigation,
      substantialContributionToClimateChangeAdaption:
        details.alignedShare?.substantialContributionToClimateChangeAdaption,
      substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResources:
        details.alignedShare?.substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResources,
      substantialContributionToTransitionToACircularEconomy:
        details.alignedShare?.substantialContributionToTransitionToACircularEconomy,
      substantialContributionToPollutionPreventionAndControl:
        details.alignedShare?.substantialContributionToPollutionPreventionAndControl,
      substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystems:
        details.alignedShare?.substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystems,
      enablingShare: details.enablingShare?.enablingShare,
      transitionalShare: details.transitionalShare?.transitionalShare,
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
