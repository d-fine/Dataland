import {
  type AssuranceDataAssuranceEnum,
  type DataAndMetaInformationEuTaxonomyDataForNonFinancials,
  type DataMetaInformation,
  type EuTaxonomyActivity,
  type EuTaxonomyAlignedActivity,
  type FiscalYearDeviation,
  type EuTaxonomyDataForNonFinancials,
  type EuTaxonomyDetailsPerCashFlowType,
  type RelativeAndAbsoluteFinancialShare,
  type YesNo,
  type YesNoNa,
  type DataPointWithUnitBigDecimal,
} from "@clients/backend";
import { type DataAndMetaInformationViewModel, type FrameworkViewModel } from "@/components/resources/ViewModel";

interface EuTaxonomyDetailsPerCashFlowViewModel {
  totalAmount?: DataPointWithUnitBigDecimal | null;
  nonEligibleShare?: RelativeAndAbsoluteFinancialShare | null;
  eligibleShare?: RelativeAndAbsoluteFinancialShare | null;
  nonAlignedShare?: (RelativeAndAbsoluteFinancialShare & { nonAlignedActivities?: EuTaxonomyActivity[] | null }) | null;
  alignedShare?:
    | (RelativeAndAbsoluteFinancialShare & {
        alignedActivities?: EuTaxonomyAlignedActivity[] | null;
        substantialContributionToClimateChangeMitigationInPercent?: number | null;
        substantialContributionToClimateChangeAdaptionInPercent?: number | null;
        substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent?: number | null;
        substantialContributionToTransitionToACircularEconomyInPercent?: number | null;
        substantialContributionToPollutionPreventionAndControlInPercent?: number | null;
        substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent?: number | null;
      })
    | null;
  enablingShare?: { enablingShareInPercent?: number | null } | null;
  transitionalShare?: { transitionalShareInPercent?: number | null } | null;
}

export class EuTaxonomyForNonFinancialsViewModel implements FrameworkViewModel {
  general?: {
    general: {
      fiscalYearDeviation?: FiscalYearDeviation | null;
      fiscalYearEnd?: string | null;
      scopeOfEntities?: YesNoNa | null;
      nfrdMandatory?: YesNo | null;
      euTaxonomyActivityLevelReporting?: YesNo | null;
      numberOfEmployees?: number | null;
    };
  };
  assurance?: {
    assurance: {
      levelOfAssurance: AssuranceDataAssuranceEnum;
      assuranceProvider?: string | null;
    };
  };
  revenue?: EuTaxonomyDetailsPerCashFlowViewModel | null;
  capex?: EuTaxonomyDetailsPerCashFlowViewModel | null;
  opex?: EuTaxonomyDetailsPerCashFlowViewModel | null;

  constructor(apiModel: EuTaxonomyDataForNonFinancials) {
    this.general = {
      general: {
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
        fiscalYearDeviation: this.general?.general?.fiscalYearDeviation,
        fiscalYearEnd: this.general?.general?.fiscalYearEnd,
        scopeOfEntities: this.general?.general?.scopeOfEntities,
        nfrdMandatory: this.general?.general?.nfrdMandatory,
        euTaxonomyActivityLevelReporting: this.general?.general?.euTaxonomyActivityLevelReporting,
        assurance:
          this.assurance == undefined
            ? undefined
            : {
                assurance: this.assurance?.assurance.levelOfAssurance,
                provider: this.assurance?.assurance.assuranceProvider,
              },
        numberOfEmployees: this.general?.general?.numberOfEmployees,
      },
      revenue: EuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowViewModelToApiModel(this.revenue),
      capex: EuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowViewModelToApiModel(this.capex),
      opex: EuTaxonomyForNonFinancialsViewModel.convertDetailsPerCashFlowViewModelToApiModel(this.opex),
    };
  }

  private static convertDetailsPerCashFlowApiModelToViewModel(
    apiModel?: EuTaxonomyDetailsPerCashFlowType | null,
  ): EuTaxonomyDetailsPerCashFlowViewModel | undefined {
    if (apiModel == undefined) {
      return undefined;
    }
    return {
      totalAmount: apiModel.totalAmount,
      eligibleShare: apiModel.eligibleShare,
      alignedShare: {
        ...(apiModel.alignedShare ?? {}),
        substantialContributionToClimateChangeMitigationInPercent:
          apiModel.substantialContributionToClimateChangeMitigationInPercent,
        substantialContributionToClimateChangeAdaptionInPercent:
          apiModel.substantialContributionToClimateChangeAdaptionInPercent,
        substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent:
          apiModel.substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent,
        substantialContributionToTransitionToACircularEconomyInPercent:
          apiModel.substantialContributionToTransitionToACircularEconomyInPercent,
        substantialContributionToPollutionPreventionAndControlInPercent:
          apiModel.substantialContributionToPollutionPreventionAndControlInPercent,
        substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent:
          apiModel.substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent,
        alignedActivities: apiModel.alignedActivities,
      },
      nonAlignedShare: {
        ...(apiModel.nonAlignedShare ?? {}),
        nonAlignedActivities: apiModel.nonAlignedActivities,
      },
      nonEligibleShare: apiModel.nonEligibleShare,
      enablingShare: { enablingShareInPercent: apiModel.enablingShareInPercent },
      transitionalShare: { transitionalShareInPercent: apiModel.transitionalShareInPercent },
    };
  }

  private static convertDetailsPerCashFlowViewModelToApiModel(
    viewModel?: EuTaxonomyDetailsPerCashFlowViewModel | null,
  ): EuTaxonomyDetailsPerCashFlowType | undefined {
    if (viewModel == undefined) {
      return undefined;
    }
    return {
      totalAmount: viewModel.totalAmount,
      nonEligibleShare: viewModel.nonEligibleShare,
      eligibleShare: viewModel.eligibleShare,
      nonAlignedShare: viewModel.nonAlignedShare,
      alignedShare: viewModel.alignedShare,
      nonAlignedActivities: viewModel.nonAlignedShare?.nonAlignedActivities,
      alignedActivities: viewModel.alignedShare?.alignedActivities,
      substantialContributionToClimateChangeMitigationInPercent:
        viewModel.alignedShare?.substantialContributionToClimateChangeMitigationInPercent,
      substantialContributionToClimateChangeAdaptionInPercent:
        viewModel.alignedShare?.substantialContributionToClimateChangeAdaptionInPercent,
      substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent:
        viewModel.alignedShare?.substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent,
      substantialContributionToTransitionToACircularEconomyInPercent:
        viewModel.alignedShare?.substantialContributionToTransitionToACircularEconomyInPercent,
      substantialContributionToPollutionPreventionAndControlInPercent:
        viewModel.alignedShare?.substantialContributionToPollutionPreventionAndControlInPercent,
      substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent:
        viewModel.alignedShare?.substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent,
      enablingShareInPercent: viewModel.enablingShare?.enablingShareInPercent,
      transitionalShareInPercent: viewModel.transitionalShare?.transitionalShareInPercent,
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
