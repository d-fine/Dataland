import {
    AssuranceData,
    CompanyReport,
    CreditInstitutionKpis,
    DataMetaInformation,
    EligibilityKpis,
    EuTaxonomyDataForFinancials,
    EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
    FiscalYearDeviation,
    InsuranceKpis,
    InvestmentFirmKpis,
    LksgData,
    LksgEnvironmental,
    LksgGeneral,
    LksgGovernance,
    LksgSocial,
    P2pAmmonia,
    P2pAutomotive, P2pCement, P2pElectricityGeneration,
    P2pFreightTransportByRoad,
    P2pGeneral,
    P2pHvcPlastics, P2pLivestockFarming,
    P2pRealEstate,
    P2pSteel,
    PathwaysToParisData,
    SfdrData,
    SfdrEnvironmental, SfdrGeneral,
    SfdrSocial,
    SmeData,
    SmeGeneral,
    SmeInsurances,
    SmePower,
    SmeProduction,
    YesNo,
    YesNoNa
} from "@clients/backend";
import {FrameworkData} from "@/utils/GenericFrameworkTypes";

export interface DataAndMetaInformationViewModel<T extends FrameworkViewModel> {
    metaInfo: DataMetaInformation;
    data: T ;
}

export interface FrameworkViewModel {
    toApiModel(): FrameworkData
}

class EuTaxonomyDataForFinancialsViewModel implements FrameworkViewModel, EuTaxonomyDataForFinancials {
    financialServicesTypes?: Array<EuTaxonomyDataForFinancialsFinancialServicesTypesEnum>;
    eligibilityKpis?: { [key: string]: EligibilityKpis; };
    creditInstitutionKpis?: CreditInstitutionKpis;
    investmentFirmKpis?: InvestmentFirmKpis;
    insuranceKpis?: InsuranceKpis;
    fiscalYearDeviation?: FiscalYearDeviation;
    fiscalYearEnd?: string;
    scopeOfEntities?: YesNoNa;
    nfrdMandatory?: YesNo;
    euTaxonomyActivityLevelReporting?: YesNo;
    assurance?: AssuranceData;
    numberOfEmployees?: number;
    referencedReports?: { [key: string]: CompanyReport; };

    constructor(apiModel: EuTaxonomyDataForFinancials) {
        this.financialServicesTypes = apiModel.financialServicesTypes;
        this.eligibilityKpis = apiModel.eligibilityKpis;
        this.creditInstitutionKpis = apiModel.creditInstitutionKpis;
        this.investmentFirmKpis = apiModel.investmentFirmKpis;
        this.insuranceKpis = apiModel.insuranceKpis;
        this.fiscalYearDeviation = apiModel.fiscalYearDeviation;
        this.fiscalYearEnd = apiModel.fiscalYearEnd;
        this.scopeOfEntities = apiModel.scopeOfEntities;
        this.nfrdMandatory = apiModel.nfrdMandatory;
        this.euTaxonomyActivityLevelReporting = apiModel.euTaxonomyActivityLevelReporting;
        this.assurance = apiModel.assurance;
        this.numberOfEmployees = apiModel.numberOfEmployees;
        this.referencedReports = apiModel.referencedReports;
    }

    toApiModel(): EuTaxonomyDataForFinancials {
        return this;
    }
}

class LksgDataViewModel implements FrameworkViewModel, LksgData {
    general: LksgGeneral;
    governance?: LksgGovernance;
    social?: LksgSocial;
    environmental?: LksgEnvironmental;

    constructor(apiModel: LksgData) {
        this.general = apiModel.general;
        this.governance = apiModel.governance;
        this.social = apiModel.social;
        this.environmental = apiModel.environmental;
    }

    toApiModel(): LksgData {
        return this;
    }
}

class SfdrDataViewModel implements FrameworkViewModel, SfdrData {
    general: SfdrGeneral
    environmental?: SfdrEnvironmental;
    social?: SfdrSocial;

    constructor(apiModel: SfdrData) {
        this.general = apiModel.general
        this.environmental = apiModel.environmental;
        this.social = apiModel.social;
    }

    toApiModel(): SfdrData {
        return this;
    }
}

class SmeDataViewModel implements FrameworkViewModel, SmeData {
    general: SmeGeneral;
    production?: SmeProduction;
    power?: SmePower;
    insurances?: SmeInsurances;

    constructor(apiModel: SmeData) {
        this.general = apiModel.general;
        this.production = apiModel.production;
        this.power = apiModel.power;
        this.insurances = apiModel.insurances;
    }

    toApiModel(): SmeData {
        return this;
    }
}

class PathwaysToParisDataViewModel implements FrameworkViewModel, PathwaysToParisData {
    general: P2pGeneral;
    ammonia?: P2pAmmonia;
    automotive?: P2pAutomotive;
    hvcPlastics?: P2pHvcPlastics;
    commercialRealEstate?: P2pRealEstate;
    residentialRealEstate?: P2pRealEstate;
    steel?: P2pSteel;
    freightTransportByRoad?: P2pFreightTransportByRoad;
    electricityGeneration?: P2pElectricityGeneration;
    livestockFarming?: P2pLivestockFarming;
    cement?: P2pCement;

    constructor(apiModel: PathwaysToParisData) {
        this.general = apiModel.general;
        this.ammonia = apiModel.ammonia;
        this.automotive = apiModel.automotive;
        this.hvcPlastics = apiModel.hvcPlastics;
        this.commercialRealEstate = apiModel.commercialRealEstate;
        this.residentialRealEstate = apiModel.residentialRealEstate;
        this.steel = apiModel.steel;
        this.freightTransportByRoad = apiModel.freightTransportByRoad;
        this.electricityGeneration = apiModel.electricityGeneration;
        this.livestockFarming = apiModel.livestockFarming;
        this.cement = apiModel.cement;
    }

    toApiModel() {
        return this;
    }
}

// TODO think about this file in the end, since lots of boilerplate code