import {
    AvailableMLDTDisplayObjectTypes,
    MLDTDisplayComponentName
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import MultiSelectModal from "@/components/resources/dataTable/modals/MultiSelectModal.vue";
import {
    euTaxonomyKpiInfoMappings,
    euTaxonomyKpiNameMappings,
} from "@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel";
import {formatNumberToReadableFormat} from "@/utils/Formatter";
import {getDataPointGetterFactory} from "@/components/resources/dataTable/conversion/Utils";
import {
    singleSelectValueGetterFactory
} from "@/components/resources/dataTable/conversion/SingleSelectValueGetterFactory";
import {plainStringValueGetterFactory} from "@/components/resources/dataTable/conversion/PlainStringValueGetterFactory";
import {yesNoValueGetterFactory} from "@/components/resources/dataTable/conversion/YesNoValueGetterFactory";
import {numberValueGetterFactory} from "@/components/resources/dataTable/conversion/NumberValueGetterFactory";
import {DropdownOption} from "@/utils/PremadeDropdownDatasets";
import {LksgData} from "@clients/backend";
import {Field, FrameworkData} from "@/utils/GenericFrameworkTypes";
import {multiSelectValueGetterFactory} from "@/components/resources/dataTable/conversion/MultiSelectValueGetterFactory";


const sampleField: Field = {
    showIf: () => true,
    name: "",
    label: "",
    description: "",
    unit: "",
    component: "",
}

const sampleFormatter = function (dataPoint: any) {
    return dataPoint?.value;
}

const generateEligibilityKpis = function (name, color="yellow") {
    function sampleCell(field) {
        const label = `${name} ${field}`;
        return {
            type: "cell",
            label,
            shouldDisplay: () => true,
            valueGetter: getDataPointGetterFactory(`eligibilityKpis.${name}.${field}`,
                {...sampleField, label}, sampleFormatter)
        }
    }

    return {
        type: "section",
        label: name,
        labelBadgeColor: color,
        expandOnPageLoad: false,
        shouldDisplay: (dataset) => (dataset.financialServicesTypes.includes(name)),
        children: [
            sampleCell("taxonomyEligibleActivityInPercent"),
            sampleCell("taxonomyNonEligibleActivityInPercent"),
            sampleCell("derivativesInPercent"),
            sampleCell("banksAndIssuersInPercent"),
            sampleCell("investmentNonNfrdInPercent")
        ]
    }
}

export const configForEutaxonomyFinancialsMLDT = [
    {
        type: "cell",
        label: euTaxonomyKpiNameMappings.financialServicesTypes,
        explanation: euTaxonomyKpiInfoMappings.financialServicesTypes,
        shouldDisplay: (): boolean => true,
        valueGetter: multiSelectValueGetterFactory("financialServicesTypes",
            {
                ...sampleField, label: euTaxonomyKpiNameMappings.financialServicesTypes, options: [
                    {
                        value: "CreditInstitution",
                        label: "CreditInstitution"
                    },
                    {
                        value: "InsuranceOrReinsurance",
                        label: "InsuranceOrReinsurance"
                    },
                    {
                        value: "AssetManagement",
                        label: "AssetManagement"
                    },
                    {
                        value: "InvestmentFirm",
                        label: "InvestmentFirm"
                    },
                ]
            })
    },
    {
        type: "section",
        label: "Eligibility Kpis",
        labelBadgeColor: "orange",
        expandOnPageLoad: true,
        shouldDisplay: () => true,
        children: [
            generateEligibilityKpis("CreditInstitution","green"),
            generateEligibilityKpis("InsuranceOrReinsurance", "red"),
            generateEligibilityKpis("AssetManagement", "blue"),
            generateEligibilityKpis("InvestmentFirm", "purple")
        ],
    },
    {
        label: "insuranceKpis taxonomyEligibleNonLifeInsuranceActivitiesInPercent",
        explanation: "",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: getDataPointGetterFactory("insuranceKpis.TaxonomyEligibleNonLifeInsuranceActivitiesInPercent",
            {...sampleField, label: "insuranceKpis taxonomyEligibleNonLifeInsuranceActivitiesInPercent"}, sampleFormatter)
    },
    {
        label: "investmentFirmKpis greenAssetRatioInPercent",
        explanation: "",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: getDataPointGetterFactory("investmentFirm.KpisGreenAssetRatioInPercent",
            {...sampleField, label: "investmentFirmKpis greenAssetRatioInPercent"}, sampleFormatter)
    },
    {
        label: "creditInstitutionKpis greenAssetRatioInPercent",
        explanation: "",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: getDataPointGetterFactory("creditInstitutionKpis.GreenAssetRatioInPercent",
            {...sampleField, label: "creditInstitutionKpis greenAssetRatioInPercent"}, sampleFormatter)
    },
    {
        label: "creditInstitutionKpis tradingPortfolioAndInterbankLoansInPercent",
        explanation: "",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: getDataPointGetterFactory("creditInstitutionKpis.TradingPortfolioAndInterbankLoansInPercent",
            {...sampleField, label: "creditInstitutionKpis tradingPortfolioAndInterbankLoansInPercent"}, sampleFormatter)
    },
    {
        label: "creditInstitutionKpis tradingPortfolioInPercent",
        explanation: "",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: getDataPointGetterFactory("creditInstitutionKpis.TradingPortfolioInPercent",
            {...sampleField, label: "creditInstitutionKpis tradingPortfolioInPercent"}, sampleFormatter)
    },
    {
        label: "creditInstitutionKpis interbankLoansInPercent",
        explanation: "",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: getDataPointGetterFactory("creditInstitutionKpis.InterbankLoansInPercent",
            {...sampleField, label: "creditInstitutionKpis interbankLoansInPercent"}, sampleFormatter)
    },
    {
        label: "Fiscal Year Deviation",
        explanation: "Fiscal Year (Deviation/ No Deviation)",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: singleSelectValueGetterFactory("fiscalYearDeviation", {
            ...sampleField,
            options: [{label: 'Deviation', value: 'Deviation'}, {label: 'No Deviation', value: 'NoDeviation'}]
        })
    },
    {
        label: "Fiscal Year End",
        explanation: "The date the fiscal year ends",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: plainStringValueGetterFactory("fiscalYearEnd")
    },
    {
        label: "Scope Of Entities",
        explanation: "Does a list of legal entities covered by Sust./Annual/Integrated/ESEF report match with a list of legal entities covered by Audited Consolidated Financial Statement ",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: yesNoValueGetterFactory("scopeOfEntities")
    },
    {
        label: "EU Taxonomy Activity Level Reporting",
        explanation: "Activity Level disclosure",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: yesNoValueGetterFactory("euTaxonomyActivityLevelReporting")
    },
    {
        label: "Number Of Employees",
        explanation: "Total number of employees (including temporary workers)",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: numberValueGetterFactory("numberOfEmployees", sampleField)
    },
    {
        label: "NFRD Mandatory",
        explanation: "The reporting obligation for companies whose number of employees is greater or equal to 500",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: yesNoValueGetterFactory("nfrdMandatory")
    }

];

// @ts-nocheck
// ***  missing are assurance, referencedReports
// as I don't know how to add em

