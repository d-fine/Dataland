import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";
import { Category } from "@/utils/GenericFrameworkTypes";


export const sfdrDataModel = [ {
    name : "social",
    label : "Social",
    color : "yellow",
    showIf : (): boolean => true,
    subcategories : [ {
        name : "general",
        label : "General",
        fields : [ {
            name : "fiscalYear",
            label : "Fiscal Year",
            description : "",
            component : "InputTextFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "fiscalYearEnd",
            label : "Fiscal Year End",
            description : "",
            component : "DateFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "annualReport",
            label : "Annual Report",
            description : "",
            component : "InputTextFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "groupLevelAnnualReport",
            label : "Group Level Annual Report",
            description : "",
            component : "YesNoNaFormField",
            required : false,
            showIf : (): boolean => true,
            certificateRequiredIfYes : false
        }, {
            name : "annualReportDate",
            label : "Annual Report Date",
            description : "",
            component : "DateFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "annualReportCurrency",
            label : "Annual Report Currency",
            description : "",
            component : "MultiSelectFormField",
            options : getDataset(DropdownDatasetIdentifier.CountryCodesIso3),
            required : false,
            showIf : (): boolean => true,
            placeholder : "Select Country"
        }, {
            name : "sustainabilityReport",
            label : "Sustainability Report",
            description : "",
            component : "InputTextFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "groupLevelSustainabilityReport",
            label : "Group Level Sustainability Report",
            description : "",
            component : "YesNoNaFormField",
            required : false,
            showIf : (): boolean => true,
            certificateRequiredIfYes : false
        }, {
            name : "sustainabilityReportDate",
            label : "Sustainability Report Date",
            description : "",
            component : "DateFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "sustainabilityReportCurrency",
            label : "Sustainability Report Currency",
            description : "",
            component : "MultiSelectFormField",
            options : getDataset(DropdownDatasetIdentifier.CountryCodesIso3),
            required : false,
            showIf : (): boolean => true,
            placeholder : "Select Country"
        }, {
            name : "integratedReport",
            label : "Integrated Report",
            description : "",
            component : "InputTextFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "groupLevelIntegratedReport",
            label : "Group Level Integrated Report",
            description : "",
            component : "YesNoNaFormField",
            required : false,
            showIf : (): boolean => true,
            certificateRequiredIfYes : false
        }, {
            name : "integratedReportDate",
            label : "Integrated Report Date",
            description : "",
            component : "DateFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "integratedReportCurrency",
            label : "Integrated Report Currency",
            description : "",
            component : "MultiSelectFormField",
            options : getDataset(DropdownDatasetIdentifier.CountryCodesIso3),
            required : false,
            showIf : (): boolean => true,
            placeholder : "Select Country"
        }, {
            name : "esefReport",
            label : "ESEF Report",
            description : "",
            component : "InputTextFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "groupLevelEsefReport",
            label : "Group Level ESEF Report",
            description : "",
            component : "YesNoNaFormField",
            required : false,
            showIf : (): boolean => true,
            certificateRequiredIfYes : false
        }, {
            name : "esefReportDate",
            label : "ESEF Report Date",
            description : "",
            component : "DateFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "esefReportCurrency",
            label : "ESEF Report Currency",
            description : "",
            component : "MultiSelectFormField",
            options : getDataset(DropdownDatasetIdentifier.CountryCodesIso3),
            required : false,
            showIf : (): boolean => true,
            placeholder : "Select Country"
        }, {
            name : "scopeOfEntities",
            label : "Scope Of Entities",
            description : "",
            component : "YesNoNaFormField",
            required : false,
            showIf : (): boolean => true,
            certificateRequiredIfYes : false
        } ]
    }, {
        name : "socialAndEmployeeMatters",
        label : "Social and employee matters",
        fields : [ {
            name : "humanRightsLegalProceedings",
            label : "Human Rights Legal Proceedings",
            description : "Does the company have been involved in Human Rights related legal proceedings?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "iloCoreLabourStandards",
            label : "ILO Core Labour Standards",
            description : "Does the company abides by ILO Core Labour Standards",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "environmentalPolicy",
            label : "Environmental Policy",
            description : "Does the company have a statement of environmental policy?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "corruptionLegalProceedings",
            label : "Corruption Legal Proceedings",
            description : "Does the company have  been involved in corruption related legal proceedings?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "transparencyDisclosurePolicy",
            label : "Transparency Disclosure Policy",
            description : "Does the company have a discloses policies related to transparency?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "humanRightsDueDiligencePolicy",
            label : "Human Rights Due Diligence Policy",
            description : "Does the company have a policies and procedures to support/respect human rights and carry out due diligence to ensure that the business activities do not have a negative human rights impact ?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "childForcedDiscriminationPolicy",
            label : "Child Forced Discrimination Policy",
            description : "Does the company have a policies and procedures to abolish all forms of child labour, forced labour and eliminate discrimination in the workplace ?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "iso14001Certificate",
            label : "ISO 14001 Certificate",
            description : "Does the company have an ISO 14001 certificate ?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "briberyCorruptionPolicy",
            label : "Bribery Corruption Policy",
            description : "Does the company have a policies and control systems in place to fight corruption ?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "fairBusinessMarketingAdvertisingPolicy",
            label : "Fair Business Marketing Advertising Policy",
            description : "Does the company have a policies and procedures in place to apply fair business, marketing and advertising practices and to guarantee the safety and quality of the goods and services ? ",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "technologiesExpertiseTransferPolicy",
            label : "Technologies Expertise Transfer Policy",
            description : "Does the company have a policies and procedures in place to permit the transfer and rapid dissemination of technologies and expertise ?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "fairCompetitionPolicy",
            label : "Fair Competition Policy",
            description : "Does the company have a policies and procedures in place related to fair competition and anti-competitive cartels ?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "violationOfTaxRulesAndRegulation",
            label : "Violation Of Tax Rules And Regulation",
            description : "Does the company makes contribution to public finances within the framework of applicable law and regulations, in accordance with the tax rules and regulations of the host countries and co-operate with the tax authorities?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "unGlobalCompactPrinciplesCompliancePolicy",
            label : "UN Global Compact Principles Compliance Policy",
            description : "Does the company have policies and procedures in place to monitor compliance with the UNGC principles or OECD Guidelines for Multinational Enterprises or grievance /complaints handling mechanisms to address violations of the UNGC principles or OECD Guidelines for Multinational Enterprises ?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "oecdGuidelinesForMultinationalEnterprisesPolicy",
            label : "OECD Guidelines For Multinational Enterprises Policy",
            description : "Does the company have policies and procedures in place to monitor compliance with the UNGC principles or OECD Guidelines for Multinational Enterprises or grievance /complaints handling mechanisms to address violations of the UNGC principles or OECD Guidelines for Multinational Enterprises?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "averageGrossHourlyEarningsMaleEmployees",
            label : "Average Gross Hourly Earnings Male Employees",
            description : "Average gross hourly earnings of male employees",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "averageGrossHourlyEarningsFemaleEmployees",
            label : "Average Gross Hourly Earnings Female Employees",
            description : "Average gross hourly earnings of female employees",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "femaleBoardMembers",
            label : "Female Board Members",
            description : "Number of females on the board. Board means the administrative, management or supervisory body of a company",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "maleBoardMembers",
            label : "Male Board Members",
            description : "Number of males on the board. Board means the administrative, management or supervisory body of a company",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "controversialWeaponsExposure",
            label : "Controversial Weapons Exposure",
            description : "Does the company involved in the manufacture or selling of controversial weapons such as anti- personnel mines, cluster munitions, chemical weapons and biological weapons ?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "workplaceAccidentPreventionPolicy",
            label : "Workplace Accident Prevention Policy",
            description : "Does the company have a workplace accident prevention policy?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "rateOfAccidents",
            label : "Rate Of Accidents",
            description : "What is the Rate Of Accidents in the company?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "workdaysLost",
            label : "Workdays Lost",
            description : "Number of workdays lost to injuries, accidents, fatalities or illness",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "supplierCodeOfConduct",
            label : "Supplier Code Of Conduct",
            description : "Does the company have a Supplier Code Of Conduct (against unsafe working conditions, precarious work, child labour and forced labour)?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "grievanceHandlingMechanism",
            label : "Grievance Handling Mechanism",
            description : "Does the company have a grievance/complaints handling mechanism related to employee matters?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "whistleblowerProtectionPolicy",
            label : "Whistleblower Protection Policy",
            description : "Does the company have policies and procedures for the protection of whistleblowers?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "reportedIncidentsOfDiscrimination",
            label : "Reported Incidents Of Discrimination",
            description : "Number of incidents of discrimination reported",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "sanctionsIncidentsOfDiscrimination",
            label : "Sanctions Incidents Of Discrimination",
            description : "Number of discrimination related incidents reported that lead to any kind of penalty and/or fine",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "ceoToEmployeePayGap",
            label : "CEO to Employee Pay Gap",
            description : "Ratio of the annual total compensation for the highest compensated individual to the median annual total compensation for all employees (excluding the highest-compensated individual)",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "greenSecurities",
        label : "Green securities",
        fields : [ {
            name : "securitiesNotCertifiedAsGreen",
            label : "Securities Not Certified As Green",
            description : "Does the company have investments not certified as green under a future EU legal act setting up an EU Green Bond Standard?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "humanRights",
        label : "Human rights",
        fields : [ {
            name : "humanRightsPolicy",
            label : "Human Rights Policy",
            description : "Does the company have a human rights policy in place?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "humanRightsDueDiligence",
            label : "Human Rights Due Diligence ",
            description : "Does the company have a due diligence process to identify, prevent, mitigate and address adverse human rights impacts?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "traffickingInHumanBeingsPolicy",
            label : "Trafficking In Human Beings Policy",
            description : "Does the company have a policies and procedures against trafficking in human beings ?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "reportedChildLabourIncidents",
            label : "Reported Child Labour Incidents",
            description : "Does the company exposed to operations and suppliers at significant risk of incidents of child labour exposed to hazardous work, in terms of of geographic areas or type of operation?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "reportedForcedOrCompulsoryLabourIncidents",
            label : "Reported Forced Or Compulsory Labour Incidents",
            description : "Does the company exposed to operations and suppliers at significant risk of incidents of forced or compulsory labour in terms in terms of geographic areas and/or the type of operation?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "reportedIncidentsOfHumanRights",
            label : "Reported Incidents Of Human Rights",
            description : "Number of cases of severe human rights issues and incidents connected to the company.",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "antiCorruptionAndAntiBribery",
        label : "Anti-corruption and anti-bribery",
        fields : [ {
            name : "reportedCasesOfBriberyCorruption",
            label : "Reported Cases Of Bribery Corruption",
            description : "Number of cases with identified insufficiencies in actions taken to address breaches in procedures and standards of anti-corruption and anti-bribery",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "reportedConvictionsOfBriberyCorruption",
            label : "Reported Convictions Of Bribery Corruption",
            description : "Numbers of convictions for violations of anti-corruption and anti-bribery laws",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "reportedFinesOfBriberyCorruption",
            label : "Reported Fines Of Bribery Corruption",
            description : "Amount of fines for violations of anti-corruption and anti-bribery laws.",
            component : "CompanyReportFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "annualReport",
            label : "Annual Report",
            description : "",
            component : "CompanyReportFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "sustainabilityReport",
            label : "Sustainability Report",
            description : "",
            component : "CompanyReportFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "integratedReport",
            label : "Integrated Report",
            description : "",
            component : "CompanyReportFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "esefReport",
            label : "Esef Report",
            description : "",
            component : "CompanyReportFormField",
            required : false,
            showIf : (): boolean => true
        } ]
    } ]
}, {
    name : "environmental",
    label : "Environmental",
    color : "green",
    showIf : (): boolean => true,
    subcategories : [ {
        name : "greenhouseGasEmissions",
        label : "Greenhouse gas emissions ",
        fields : [ {
            name : "scope1",
            label : "Scope 1",
            description : "What is the amount of the company's Scope 1 emissions?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "scope2",
            label : "Scope 2",
            description : "What is the amount of the company's Scope 2 emissions?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "scope3",
            label : "Scope 3",
            description : "What is the amount of the company's Scope 3 emissions ?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "enterpriseValue",
            label : "Enterprise Value ",
            description : "Company Enterprise Value",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "totalRevenue",
            label : "Total Revenue",
            description : "Company Total Revenue ",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "fossilFuelSectorExposure",
            label : "Fossil Fuel Sector Exposure",
            description : "Does the company derive any revenues from fossil fuels?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        } ]
    }, {
        name : "energyPerformance",
        label : "Energy performance",
        fields : [ {
            name : "renewableEnergyProduction",
            label : "Renewable Energy Production",
            description : "Value of energy produced from renewable energy sources ",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "renewableEnergyConsumption",
            label : "Renewable Energy Consumption",
            description : "Value of energy consumed from renewable energy sources ",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "nonRenewableEnergyConsumption",
            label : "Non-Renewable Energy Consumption",
            description : "Value of energy consumed from non-renewable energy sources ",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "nonRenewableEnergyProduction",
            label : "Non-Renewable Energy Production",
            description : "Value of energy produced from non-renewable energy sources ",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "highImpactClimateSectorEnergyConsumptionNaceA",
            label : "High Impact Climate Sector Energy Consumption NACE A",
            description : "Total energy consumption per high impact climate sector",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "highImpactClimateSectorEnergyConsumptionNaceB",
            label : "High Impact Climate Sector Energy Consumption NACE B",
            description : "Total energy consumption per high impact climate sector",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "highImpactClimateSectorEnergyConsumptionNaceC",
            label : "High Impact Climate Sector Energy Consumption NACE C",
            description : "Total energy consumption per high impact climate sector",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "highImpactClimateSectorEnergyConsumptionNaceD",
            label : "High Impact Climate Sector Energy Consumption NACE D",
            description : "Total energy consumption per high impact climate sector",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "highImpactClimateSectorEnergyConsumptionNaceE",
            label : "High Impact Climate Sector Energy Consumption NACE E",
            description : "Total energy consumption per high impact climate sector",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "highImpactClimateSectorEnergyConsumptionNaceF",
            label : "High Impact Climate Sector Energy Consumption NACE F",
            description : "Total energy consumption per high impact climate sector",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "highImpactClimateSectorEnergyConsumptionNaceG",
            label : "High Impact Climate Sector Energy Consumption NACE G",
            description : "Total energy consumption per high impact climate sector",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "highImpactClimateSectorEnergyConsumptionNaceH",
            label : "High Impact Climate Sector Energy Consumption NACE H",
            description : "Total energy consumption per high impact climate sector",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "highImpactClimateSectorEnergyConsumptionNaceL",
            label : "High Impact Climate Sector Energy Consumption NACE L",
            description : "Total energy consumption per high impact climate sector",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "totalHighImpactClimateSectorEnergyConsumption",
            label : "Total High Impact Climate Sector Energy Consumption",
            description : "Total energy consumption per high impact climate sector",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "nonRenewableEnergyConsumptionFossilFuels",
            label : "Non-Renewable Energy Consumption Fossil Fuels",
            description : "Total amount of non-renewable energy consumption",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "nonRenewableEnergyConsumptionCrudeOil",
            label : "Non-Renewable Energy Consumption Crude Oil",
            description : "Energy consumption by non-renewable energy sources such as crude oil",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "nonRenewableEnergyConsumptionNaturalGas",
            label : "Non-Renewable Energy Consumption Natural Gas",
            description : "Energy consumption by non-renewable energy sources such as natural gas",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "nonRenewableEnergyConsumptionLignite",
            label : "Non-Renewable Energy Consumption Lignite",
            description : "Energy consumption by non-renewable energy sources such as lignite",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "nonRenewableEnergyConsumptionCoal",
            label : "Non-Renewable Energy Consumption Coal",
            description : "Energy consumption by non-renewable energy sources such as coal",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "nonRenewableEnergyConsumptionNuclearEnergy",
            label : "Non-Renewable Energy Consumption Nuclear Energy",
            description : "Energy consumption by non-renewable energy sources such as nuclear energy (Uranium)",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "nonRenewableEnergyConsumptionOther",
            label : "Non-Renewable Energy Consumption Other",
            description : "Energy consumption by non-renewable energy sources from any other available (used) non-renewable source of energy",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "biodiversity",
        label : "Biodiversity",
        fields : [ {
            name : "primaryForestAndWoodedLandOfNativeSpeciesExposure",
            label : "Primary Forest And Wooded Land Of Native Species Exposure",
            description : "Does the company have sites/operations located in primary forest and other wooded land, that is forest and other wooded land of native species, where there is no clearly visible indication of human activity and the ecological processes are not significantly disturbed - where activities of the company negatively affect those areas?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "protectedAreasExposure",
            label : "Protected Areas Exposure",
            description : "Does the company have sites/operations located in areas designatedby law or by the relevant competent authority for nature protection purposes, unless evidence is provided that the production of that raw material did not interfere with those nature protection purposes - where activities of the company negatively affect those areas?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "rareOrEndangeredEcosystemsExposure",
            label : "Rare Or Endangered Ecosystems Exposure",
            description : "Does the company have sites/operations located in  areas designated for the protection of rare, threatened or endangered ecosystems or species recognised by international agreements or included in lists drawn up by intergovernmental organisations or the International Union for the Conservation of Nature, subject to their recognition by the Commission (Commission may also recognise areas for the protection of rare, threatened or endangered ecosystems or species recognised by international agreements or included in lists drawn up by intergovernmental organisations or the International Union for the Conservation of Nature), unless evidence is provided that the production of that raw material did not interfere with those nature protection purposes  - where activities of the company negatively affect those areas?",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        } ]
    }, {
        name : "water",
        label : "Water",
        fields : [ {
            name : "emissionsToWater",
            label : "Emissions To Water",
            description : "Tonnes of Emissions To Water generated by the company.",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "waterConsumption",
            label : "Water Consumption",
            description : "Amount of water consumed by the company",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "waterReused",
            label : "Water Reused",
            description : "Amount of water reused/reclaimed by the company",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "waterManagementPolicy",
            label : "Water Management Policy",
            description : "Does the compant have a policies and procedures for water management ?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "waterStressAreaExposure",
            label : "Water Stress Area Exposure",
            description : "Does the company has sites located in areas of high water stress without a water management policy ?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "waste",
        label : "Waste",
        fields : [ {
            name : "hazardousWaste",
            label : "Hazardous Waste",
            description : "Tonnes of hazardous waste generated by the company.",
            component : "DataPointFormField",
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "manufactureOfAgrochemicalPesticidesProducts",
            label : "Manufacture Of Agrochemical Pesticides Products",
            description : "Is the company involved in the manufacture of pesticides and other agrochemical products?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "landDegradationDesertificationSoilSealingExposure",
            label : "Land Degradation Desertification Soil Sealing Exposure",
            description : "Do the company's activities cause land degradation, desertification or soil sealing?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "sustainableAgriculturePolicy",
            label : "Sustainable Agriculture Policy",
            description : "Does the company have sustainable land/agriculture practices or policies?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "sustainableOceansAndSeasPolicy",
            label : "Sustainable Oceans And Seas Policy",
            description : "Does the company have sustainable oceans/seas practices or policies ?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "wasteNonRecycled",
            label : "Waste Non-Recycled",
            description : "Tonnes of non-recycled waste generated by the company.",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "threatenedSpeciesExposure",
            label : "Threatened Species Exposure",
            description : "Does the company involved in operations that affect threatened species?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "biodiversityProtectionPolicy",
            label : "Biodiversity Protection Policy",
            description : "Does the company have a biodiversity policy in place which covers operational sites owned, leased, managed in, or adjacent to, a protected area or an area of high biodiversity value outside protected areas?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "deforestationPolicy",
            label : "Deforestation Policy",
            description : "Does the company have a policies and procedures to address deforestation ?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "emissions",
        label : "Emissions",
        fields : [ {
            name : "inorganicPollutants",
            label : "Inorganic Pollutants",
            description : "Amount of emissions of Inorganic Pollutants",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "airPollutants",
            label : "Air Pollutants",
            description : "Amount of emissions of Air Pollutants",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "ozoneDepletionSubstances",
            label : "Ozone Depletion Substances",
            description : "Amount of emissions of Ozone Depletion Substances",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        }, {
            name : "carbonReductionInitiatives",
            label : "Carbon Reduction Initiatives",
            description : "Does the company have carbon emission reduction initiatives aimed at aligning with the Paris Agreement in place?",
            component : "DataPointFormField",
            required : false,
            showIf : (): boolean => true
        } ]
    } ]
} ] as Array<Category>;
