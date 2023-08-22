import {
  type AssuranceData,
  type CompanyReport,
  type CreditInstitutionKpis,
  type DataMetaInformation,
  type EligibilityKpis,
  type EuTaxonomyDataForFinancials,
  type EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
  type FiscalYearDeviation,
  type InsuranceKpis,
  type InvestmentFirmKpis,
  type LksgData,
  type LksgEnvironmental,
  type LksgGeneral,
  type LksgGovernance,
  type LksgSocial,
  type P2pAmmonia,
  type P2pAutomotive,
  type P2pCement,
  type P2pElectricityGeneration,
  type P2pFreightTransportByRoad,
  type P2pGeneral,
  type P2pHvcPlastics,
  type P2pLivestockFarming,
  type P2pRealEstate,
  type P2pSteel,
  type PathwaysToParisData,
  type SfdrData,
  type SfdrEnvironmental,
  type SfdrGeneral,
  type SfdrSocial,
  type SmeData,
  type SmeGeneral,
  type SmeInsurances,
  type SmePower,
  type SmeProduction,
  type YesNo,
  type YesNoNa,
} from "@clients/backend";
import { type FrameworkData } from "@/utils/GenericFrameworkTypes";

export interface DataAndMetaInformation<T> {
  metaInfo: DataMetaInformation;
  data: T;
}

export type DataAndMetaInformationViewModel<T extends FrameworkViewModel> = DataAndMetaInformation<T>;

export interface FrameworkViewModel {
  toApiModel(): FrameworkData;
}

/**
 * Create a data view model with identity toApiModel conversion
 * @param input the data meta information object to wrap.
 * @returns a DataAndMetaInformationViewModel with an identity toApiModel function.
 */
export function getViewModelWithIdentityApiModel<T extends FrameworkData>(
  input: DataAndMetaInformation<T>,
): DataAndMetaInformationViewModel<T & FrameworkViewModel> {
  return {
    metaInfo: input.metaInfo,
    data: {
      ...input.data,
      toApiModel: () => input.data,
    },
  };
}
