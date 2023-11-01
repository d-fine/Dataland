
// ***  missing are financialServicesTypes, eligibilityKpis, assurance, referencedReports
// as I don't know how to add em

[
    {
        label: "insuranceKpis taxonomyEligibleNonLifeInsuranceActivitiesInPercent",
        explanation: "",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: dataPointValueGetterFactory("insuranceKpis.TaxonomyEligibleNonLifeInsuranceActivitiesInPercent", {unit: ''})
    },
    {
        label: "investmentFirmKpis greenAssetRatioInPercent",
        explanation: "",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: dataPointValueGetterFactory("investmentFirm.KpisGreenAssetRatioInPercent", {unit: ''})
    },
    {
        label: "creditInstitutionKpis greenAssetRatioInPercent",
        explanation: "",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: dataPointValueGetterFactory("creditInstitutionKpis.GreenAssetRatioInPercent", {unit: ''})
    },
    {
        label: "creditInstitutionKpis tradingPortfolioAndInterbankLoansInPercent",
        explanation: "",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: dataPointValueGetterFactory("creditInstitutionKpis.TradingPortfolioAndInterbankLoansInPercent", {unit: ''})
    },
    {
        label: "creditInstitutionKpis tradingPortfolioInPercent",
        explanation: "",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: dataPointValueGetterFactory("creditInstitutionKpis.TradingPortfolioInPercent", {unit: ''})
    },
    {
        label: "creditInstitutionKpis interbankLoansInPercent",
        explanation: "",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: dataPointValueGetterFactory("creditInstitutionKpis.InterbankLoansInPercent", {unit: ''})
    },
    {
        label: "Fiscal Year Deviation",
        explanation: "Fiscal Year (Deviation/ No Deviation)",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: singleSelectValueGetterFactory("fiscalYearDeviation", {unit: '', options: [{label:'Deviation',value:'Deviation'},{label:'No Deviation',value:'NoDeviation'}]})
    },
    {
        label: "Fiscal Year End",
        explanation: "The date the fiscal year ends",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: plainStringValueGetterFactory("fiscalYearEnd", {unit: ''})
    },
    {
        label: "Scope Of Entities",
        explanation: "Does a list of legal entities covered by Sust./Annual/Integrated/ESEF report match with a list of legal entities covered by Audited Consolidated Financial Statement ",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: yesNoValueGetterFactory("scopeOfEntities", {unit: ''})
    },
    {
        label: "EU Taxonomy Activity Level Reporting",
        explanation: "Activity Level disclosure",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: yesNoValueGetterFactory("euTaxonomyActivityLevelReporting", {unit: ''})
    },
    {
        label: "Number Of Employees",
        explanation: "Total number of employees (including temporary workers)",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: numberValueGetterFactory("numberOfEmployees", {unit: ''})
    },
    {
        label: "NFRD Mandatory",
        explanation: "The reporting obligation for companies whose number of employees is greater or equal to 500",
        type: "cell",
        shouldDisplay: (): boolean => true,
        valueGetter: yesNoValueGetterFactory("nfrdMandatory", {unit: ''})
    }
]