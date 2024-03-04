import { type Category } from "@/utils/GenericFrameworkTypes";
import { type LksgData } from "@clients/backend";
import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";

export const lksgDataModel = [
  {
    name: "general",
    label: "General",
    color: "orange",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "masterData",
        label: "Master Data",
        fields: [
          {
            name: "dataDate",
            label: "Data Date",
            description: "The date until when the information collected is valid",

            component: "DateFormField",
            required: true,
            showIf: (): boolean => true,
            validation: "required",
          },
          {
            name: "headOfficeInGermany",
            label: "Head Office in Germany",
            description:
              "Is your head office, administrative headquarters, registered office, or subsidiary located in Germany?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "groupOfCompanies",
            label: "Group of Companies",
            description: "Do you belong to a group of companies?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "groupOfCompaniesName",
            label: "Group of Companies Name",
            description: "If yes, name of company group",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean => dataset.general?.masterData?.groupOfCompanies == "Yes",
          },
          {
            name: "industry",
            label: "Industry",
            description: "In which industry is your company primarily active (select all that apply)?",

            component: "NaceCodeFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "numberOfEmployees",
            label: "Number of Employees",
            description: "Total number of employees (including temporary workers with assignment duration >6 months)",

            component: "BaseDataPointFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "min:0",
          },
          {
            name: "seasonalOrMigrantWorkers",
            label: "Seasonal or Migrant Workers",
            description: "Do your company employ seasonal or migrant workers?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "shareOfTemporaryWorkers",
            label: "Share of Temporary Workers",
            description: "What share of the total number of employees in your company is made up by temporary workers?",
            options: [
              {
                label: "<10%",
                value: "Smaller10",
              },
              {
                label: "10-25%",
                value: "Between10And25",
              },
              {
                label: "25-50%",
                value: "Between25And50",
              },
              {
                label: ">50%",
                value: "Greater50",
              },
            ],

            component: "RadioButtonsFormField",
            required: false,
            showIf: (dataset: LksgData): boolean => dataset.general?.masterData?.seasonalOrMigrantWorkers == "Yes",
          },
          {
            name: "annualTotalRevenue",
            label: "Annual Total Revenue",
            description: "Total revenue per annum",

            component: "AmountWithCurrencyFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "fixedAndWorkingCapital",
            label: "Fixed and Working Capital",
            description:
              "Combined fixed and working capital (only for own operations) in same currency than total revenue",

            component: "AmountWithCurrencyFormField",
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
      {
        name: "productionSpecific",
        label: "Production-specific",
        fields: [
          {
            name: "manufacturingCompany",
            label: "Manufacturing Company",
            description: "Is your company a manufacturing company?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "capacity",
            label: "Capacity",
            description: "Production capacity per year, e.g. quantity with units.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean => dataset.general?.productionSpecific?.manufacturingCompany == "Yes",
          },
          {
            name: "productionViaSubcontracting",
            label: "Production via Subcontracting",
            description: "Is the production done via subcontracting?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean => dataset.general?.productionSpecific?.manufacturingCompany == "Yes",
          },
          {
            name: "subcontractingCompaniesCountries",
            label: "Subcontracting Companies Countries",
            description: "In which countries do the subcontracting companies operate?",
            options: getDataset(DropdownDatasetIdentifier.CountryCodesIso2),

            component: "MultiSelectFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.general?.productionSpecific?.productionViaSubcontracting == "Yes",
          },
          {
            name: "subcontractingCompaniesIndustries",
            label: "Subcontracting Companies Industries",
            description: "In which industries do the subcontracting companies operate?",

            component: "NaceCodeFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.general?.productionSpecific?.productionViaSubcontracting == "Yes",
          },
          {
            name: "productionSites",
            label: "Production Sites",
            description: "Do you have production sites in your company?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean => dataset.general?.productionSpecific?.manufacturingCompany == "Yes",
          },
          {
            name: "numberOfProductionSites",
            label: "Number of Production Sites",
            description: "How many production sites are there?",

            component: "NumberFormField",
            required: false,
            showIf: (dataset: LksgData): boolean => dataset.general?.productionSpecific?.productionSites == "Yes",
          },
          {
            name: "listOfProductionSites",
            label: "List Of Production Sites",
            description: "Please list the production sites in your company.",

            component: "ProductionSitesFormField",
            required: false,
            showIf: (dataset: LksgData): boolean => dataset.general?.productionSpecific?.productionSites == "Yes",
          },
          {
            name: "market",
            label: "Market",
            description: "Does your business focus predominantly on national or international markets?",
            options: [
              {
                label: "National",
                value: "National",
              },
              {
                label: "International",
                value: "International",
              },
              {
                label: "Both",
                value: "Both",
              },
            ],

            component: "RadioButtonsFormField",
            required: false,
            showIf: (dataset: LksgData): boolean => dataset.general?.productionSpecific?.manufacturingCompany == "Yes",
          },
          {
            name: "specificProcurement",
            label: "Specific Procurement",
            description: "Does your company have one of the specific procurement models?",
            options: [
              {
                label: "Short-lived and changing business relationships",
                value: "ShortLivedAndChangingBusinessRelationships",
              },
              {
                label: "High price pressure",
                value: "HighPricePressure",
              },
              {
                label: "Tightly timed or short-term adjusted delivery deadlines and conditions with suppliers",
                value: "TightlyTimedOrShortTermAdjustedDeliveryDeadlinesAndConditionsWithSuppliers",
              },
              {
                label: "None of the above",
                value: "NoneOfTheAbove",
              },
            ],

            component: "MultiSelectFormField",
            required: false,
            showIf: (dataset: LksgData): boolean => dataset.general?.productionSpecific?.manufacturingCompany == "Yes",
          },
        ],
      },
      {
        name: "productionSpecificOwnOperations",
        label: "Production-specific - Own Operations",
        fields: [
          {
            name: "mostImportantProducts",
            label: "Most Important Products",
            description:
              "Please give an overview of the most important products or services in terms of sales that your company manufactures, distributes and/or offers (own operations)",

            component: "MostImportantProductsFormField",
            required: false,
            showIf: (dataset: LksgData): boolean => dataset.general?.productionSpecific?.manufacturingCompany == "Yes",
          },
          {
            name: "procurementCategories",
            label: "Procurement Categories",
            description: "Name their procurement categories (products, raw materials, services) (own operations)",

            component: "ProcurementCategoriesFormField",
            required: false,
            showIf: (dataset: LksgData): boolean => dataset.general?.productionSpecific?.manufacturingCompany == "Yes",
          },
        ],
      },
    ],
  },
  {
    name: "governance",
    label: "Governance",
    color: "",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "riskManagementOwnOperations",
        label: "Risk management - Own Operations",
        fields: [
          {
            name: "riskManagementSystem",
            label: "Risk Management System",
            description: "Does your company have an adequate and effective Risk Management system?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "riskAnalysisInFiscalYear",
            label: "Risk Analysis in Fiscal Year",
            description: "Did you perform a risk analysis as part of risk management this fiscal year?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.riskManagementOwnOperations?.riskManagementSystem?.value == "Yes",
          },
          {
            name: "risksIdentified",
            label: "Risks Identified",
            description: "Were risks identified during this period?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.riskManagementOwnOperations?.riskAnalysisInFiscalYear == "Yes",
          },
          {
            name: "identifiedRisks",
            label: "Identified Risks",
            description: "Which risks were specifically identified in the risk analysis?",
            options: [
              {
                label: "Child labor",
                value: "ChildLabor",
              },
              {
                label: "Forced Labor",
                value: "ForcedLabor",
              },
              {
                label: "Slavery",
                value: "Slavery",
              },
              {
                label: "Disregard for occupational health/safety",
                value: "DisregardForOccupationalHealthOrSafety",
              },
              {
                label: "Disregard for freedom of association",
                value: "DisregardForFreedomOfAssociation",
              },
              {
                label: "Unequal treatment of employment",
                value: "UnequalTreatmentOfEmployment",
              },
              {
                label: "Withholding adequate wages",
                value: "WithholdingAdequateWages",
              },
              {
                label: "Contamination of soil/water/air, noise emissions, excessive water consumption",
                value: "ContaminationOfSoilWaterAirOrNoiseEmissionsOrExcessiveWaterConsumption",
              },
              {
                label: "Unlawful eviction/deprivation of land, forest and water",
                value: "UnlawfulEvictionOrDeprivationOfLandOrForestAndWater",
              },
              {
                label: "Use of private/public security forces with disregard for human rights",
                value: "UseOfPrivatePublicSecurityForcesWithDisregardForHumanRights",
              },
              {
                label: "Use of mercury, mercury waste (Minamata Convention)",
                value: "UseOfMercuryOrMercuryWaste",
              },
              {
                label: "Production and use of persistent organic pollutants (POPs Convention)",
                value: "ProductionAndUseOfPersistentOrganicPollutants",
              },
              {
                label: "Export/import of hazardous waste (Basel Convention)",
                value: "ExportImportOfHazardousWaste",
              },
            ],

            component: "MultiSelectFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.riskManagementOwnOperations?.risksIdentified == "Yes",
          },
          {
            name: "counteractingMeasures",
            label: "Counteracting Measures",
            description: "Have measures been defined to counteract the risks?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.riskManagementOwnOperations?.risksIdentified == "Yes",
          },
          {
            name: "whichCounteractingMeasures",
            label: "Which Counteracting Measures",
            description: "Which measures have been applied to counteract the risks?",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.riskManagementOwnOperations?.counteractingMeasures == "Yes",
          },
          {
            name: "regulatedRiskManagementResponsibility",
            label: "Regulated Risk Management Responsibility",
            description:
              "Is the responsibility for Risk Management in your company regulated, for example by appointing a human rights officer?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.riskManagementOwnOperations?.riskManagementSystem?.value == "Yes",
          },
        ],
      },
      {
        name: "grievanceMechanismOwnOperations",
        label: "Grievance mechanism - Own Operations",
        fields: [
          {
            name: "grievanceHandlingMechanism",
            label: "Grievance Handling Mechanism",
            description:
              "Has your company implemented a grievance handling mechanism (e.g. anonymous whistleblowing system) to protect human and environmental rights in your business?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "grievanceHandlingReportingAccessible",
            label: "Grievance Handling Reporting Accessible",
            description:
              "Can all affected stakeholders and rights holders, i.e. both internal (e.g. employees) and external stakeholders (e.g. suppliers and their employees, NGOs) access the grievance reporting/whistleblowing system?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism?.value == "Yes",
          },
          {
            name: "appropriateGrievanceHandlingInformation",
            label: "Appropriate Grievance Handling Information",
            description:
              "Is the grievance procedure adapted to your company context and articulated in a way that is understandable to the target groups?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism?.value == "Yes",
          },
          {
            name: "appropriateGrievanceHandlingSupport",
            label: "Appropriate Grievance Handling Support",
            description:
              "Is the necessary support provided in a way that the target groups can actually use the procedure?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism?.value == "Yes",
          },
          {
            name: "accessToExpertiseForGrievanceHandling",
            label: "Access to Expertise for Grievance Handling",
            description:
              "Do the target groups have access to the expertise, advice and information that they need to participate in the grievance procedure in a fair, informed and respectful manner?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism?.value == "Yes",
          },
          {
            name: "grievanceComplaints",
            label: "Grievance Complaints",
            description: "Have there been any complaints being entered into the system?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism?.value == "Yes",
          },
          {
            name: "complaintsNumber",
            label: "Complaints Number",
            description: "How many complaints have been received (for the reported fiscal year)?",

            component: "BaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.grievanceMechanismOwnOperations?.grievanceComplaints == "Yes",
            validation: "min:0",
          },
          {
            name: "complaintsRiskPosition",
            label: "Complaints Risk Position",
            description: "Please define the respective risk position of each complaint",
            options: [
              {
                label: "Child labor",
                value: "ChildLabor",
              },
              {
                label: "Forced Labor",
                value: "ForcedLabor",
              },
              {
                label: "Slavery",
                value: "Slavery",
              },
              {
                label: "Disregard for occupational health/safety",
                value: "DisregardForOccupationalHealthOrSafety",
              },
              {
                label: "Disregard for freedom of association",
                value: "DisregardForFreedomOfAssociation",
              },
              {
                label: "Unequal treatment of employment",
                value: "UnequalTreatmentOfEmployment",
              },
              {
                label: "Withholding adequate wages",
                value: "WithholdingAdequateWages",
              },
              {
                label: "Contamination of soil/water/air, noise emissions, excessive water consumption",
                value: "ContaminationOfSoilWaterAirOrNoiseEmissionsOrExcessiveWaterConsumption",
              },
              {
                label: "Unlawful eviction/deprivation of land, forest and water",
                value: "UnlawfulEvictionOrDeprivationOfLandOrForestAndWater",
              },
              {
                label: "Use of private/public security forces with disregard for human rights",
                value: "UseOfPrivatePublicSecurityForcesWithDisregardForHumanRights",
              },
              {
                label: "Use of mercury, mercury waste (Minamata Convention)",
                value: "UseOfMercuryOrMercuryWaste",
              },
              {
                label: "Production and use of persistent organic pollutants (POPs Convention)",
                value: "ProductionAndUseOfPersistentOrganicPollutants",
              },
              {
                label: "Export/import of hazardous waste (Basel Convention)",
                value: "ExportImportOfHazardousWaste",
              },
            ],

            component: "MultiSelectFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.grievanceMechanismOwnOperations?.grievanceComplaints == "Yes",
          },
          {
            name: "complaintsReason",
            label: "Complaints Reason",
            description: "Please specify the complaint.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.grievanceMechanismOwnOperations?.grievanceComplaints == "Yes",
          },
          {
            name: "actionsForComplaintsUndertaken",
            label: "Actions for Complaints Undertaken",
            description: "Were measures taken to address the complaints?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.grievanceMechanismOwnOperations?.grievanceComplaints == "Yes",
          },
          {
            name: "whichActionsForComplaintsUndertaken",
            label: "Which Actions for Complaints Undertaken",
            description: "Which measures were taken to address the reported complaints?",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.grievanceMechanismOwnOperations?.actionsForComplaintsUndertaken == "Yes",
          },
          {
            name: "publicAccessToGrievanceHandling",
            label: "Public Access to Grievance Handling",
            description:
              "Does your company have publicly accessible rules that clearly describe the process for dealing with complaints?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism?.value == "Yes",
          },
          {
            name: "whistleblowerProtection",
            label: "Whistleblower Protection",
            description: "Are whistleblowers effectively protected from disadvantage or punishment?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism?.value == "Yes",
          },
          {
            name: "dueDiligenceProcessForGrievanceHandling",
            label: "Due Diligence Process for Grievance Handling",
            description: "Are insights from reported complaints used to improve your due diligence process?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism?.value == "Yes",
          },
        ],
      },
      {
        name: "certificationsPoliciesAndResponsibilities",
        label: "Certifications, policies and responsibilities",
        fields: [
          {
            name: "additionalCertifications",
            label: "Additional Certifications",
            description:
              "Does your company hold further certification / verfication / best practices etc. that mitigate human rights and/or environmental risks? If yes, please share the documents with us",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "codeOfConduct",
            label: "Code of Conduct",
            description:
              "Has your company implemented and enforced internal behavioral guidelines that address the issues of human rights protection and respect for the environment  (e.g. within the code of conduct)?  If yes, please share the relevant document with us.",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "codeOfConductTraining",
            label: "Code of Conduct Training",
            description:
              "Are your employees regularly made aware of your internal rules of conduct and trained on them?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.certificationsPoliciesAndResponsibilities?.codeOfConduct?.value == "Yes",
          },
          {
            name: "supplierCodeOfConduct",
            label: "Supplier Code of Conduct",
            description:
              "Does your company have a supplier code of conduct? If yes, please share the supplier code of conduct with us.",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "policyStatement",
            label: "Policy Statement",
            description:
              "Does your company have a policy statement on its human rights strategy? If yes, please share the policy with us.",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "humanRightsStrategy",
            label: "Human Rights Strategy",
            description:
              "In which relevant departments/business processes has the anchoring of the human rights strategy been ensured?",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.certificationsPoliciesAndResponsibilities?.policyStatement?.value == "Yes",
          },
          {
            name: "environmentalImpactPolicy",
            label: "Environmental Impact Policy",
            description:
              "Does your company have an environmental impact policy? If yes, please share the policy with us.",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "fairWorkingConditionsPolicy",
            label: "Fair Working Conditions Policy",
            description:
              "Does your company have a fair working conditions policy? If yes, please share the policy with us.",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
      {
        name: "generalViolations",
        label: "General violations",
        fields: [
          {
            name: "responsibilitiesForFairWorkingConditions",
            label: "Responsibilities for Fair Working Conditions",
            description:
              "Has your company established official responsibilities for the topic of fair working conditions, according to the nature and extent of the enterprise’s business activities?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "responsibilitiesForTheEnvironment",
            label: "Responsibilities for the Environment",
            description:
              "Has your company established official responsibilities for the topic of the environment, according to the nature and extent of the enterprise’s business activities?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "responsibilitiesForOccupationalSafety",
            label: "Responsibilities for Occupational Safety",
            description:
              "Has your company established official responsibilities for the topic of occupational safety, according to the nature and extent of the enterprise’s business activities?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "legalProceedings",
            label: "Legal Proceedings",
            description:
              "Has your company been involved in legal disputes in the last 5 years (including currently ongoing disputes) with third parties regarding human rights and environmental violations?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "humanRightsOrEnvironmentalViolations",
            label: "Human Rights or Environmental Violations",
            description:
              "Have there been any human rights or environmental violations on your company’s part in the last 5 years?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "humanRightsOrEnvironmentalViolationsDefinition",
            label: "Human Rights or Environmental Violations Definition",
            description: "Please define those violations.",
            options: [
              {
                label: "Child labor",
                value: "ChildLabor",
              },
              {
                label: "Forced Labor",
                value: "ForcedLabor",
              },
              {
                label: "Slavery",
                value: "Slavery",
              },
              {
                label: "Disregard for occupational health/safety",
                value: "DisregardForOccupationalHealthOrSafety",
              },
              {
                label: "Disregard for freedom of association",
                value: "DisregardForFreedomOfAssociation",
              },
              {
                label: "Unequal treatment of employment",
                value: "UnequalTreatmentOfEmployment",
              },
              {
                label: "Withholding adequate wages",
                value: "WithholdingAdequateWages",
              },
              {
                label: "Contamination of soil/water/air, noise emissions, excessive water consumption",
                value: "ContaminationOfSoilWaterAirOrNoiseEmissionsOrExcessiveWaterConsumption",
              },
              {
                label: "Unlawful eviction/deprivation of land, forest and water",
                value: "UnlawfulEvictionOrDeprivationOfLandOrForestAndWater",
              },
              {
                label: "Use of private/public security forces with disregard for human rights",
                value: "UseOfPrivatePublicSecurityForcesWithDisregardForHumanRights",
              },
              {
                label: "Use of mercury, mercury waste (Minamata Convention)",
                value: "UseOfMercuryOrMercuryWaste",
              },
              {
                label: "Production and use of persistent organic pollutants (POPs Convention)",
                value: "ProductionAndUseOfPersistentOrganicPollutants",
              },
              {
                label: "Export/import of hazardous waste (Basel Convention)",
                value: "ExportImportOfHazardousWaste",
              },
            ],

            component: "MultiSelectFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.generalViolations?.humanRightsOrEnvironmentalViolations == "Yes",
          },
          {
            name: "humanRightsOrEnvironmentalViolationsMeasures",
            label: "Human Rights or Environmental Violations Measures",
            description: "Have measures been taken to address this violation?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.generalViolations?.humanRightsOrEnvironmentalViolations == "Yes",
          },
          {
            name: "humanRightsOrEnvironmentalViolationsMeasuresDefinition",
            label: "Human Rights or Environmental Violations Measures Definition",
            description: "Please define these measures.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.generalViolations?.humanRightsOrEnvironmentalViolationsMeasures == "Yes",
          },
          {
            name: "highRiskCountriesRawMaterials",
            label: "High Risk Countries Raw Materials",
            description: "Do you source materials from countries associated with high-risk or conflict?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "highRiskCountriesRawMaterialsLocation",
            label: "High Risk Countries Raw Materials Location",
            description: "From which conflict/high-risk regions do you source your raw materials?",
            options: getDataset(DropdownDatasetIdentifier.CountryCodesIso2),

            component: "MultiSelectFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.generalViolations?.highRiskCountriesRawMaterials == "Yes",
          },
          {
            name: "highRiskCountriesActivity",
            label: "High Risk Countries Activity",
            description:
              "Does your company operate in countries where there are high risks for human rights and/or the environment?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "highRiskCountries",
            label: "High Risk Countries",
            description: "Which ones?",
            options: getDataset(DropdownDatasetIdentifier.CountryCodesIso2),

            component: "MultiSelectFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.generalViolations?.highRiskCountriesActivity == "Yes",
          },
          {
            name: "highRiskCountriesProcurement",
            label: "High Risk Countries Procurement",
            description:
              "Does your company procure from countries with high risks for human rights and/or the environment?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "highRiskCountriesProcurementName",
            label: "High Risk Countries Procurement Name",
            description: "Which ones?",
            options: getDataset(DropdownDatasetIdentifier.CountryCodesIso2),

            component: "MultiSelectFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.governance?.generalViolations?.highRiskCountriesProcurement == "Yes",
          },
        ],
      },
    ],
  },
  {
    name: "social",
    label: "Social",
    color: "",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "childLabor",
        label: "Child labor",
        fields: [
          {
            name: "employeeSUnder18",
            label: "Employee(s) Under 18",
            description: "Does your company have employees under the age of 18?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "employeeSUnder15",
            label: "Employee(s) Under 15",
            description:
              "With regard to the place of employment and the applicable laws: do you employ school-age children or children under the age of 15 on a full-time basis?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean => dataset.social?.childLabor?.employeeSUnder18 == "Yes",
          },
          {
            name: "employeeSUnder18InApprenticeship",
            label: "Employee(s) Under 18 in Apprenticeship",
            description:
              "Are your employees under the age of 18 exclusively apprentices within the meaning of the locally applicable laws?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean => dataset.social?.childLabor?.employeeSUnder18 == "Yes",
          },
          {
            name: "worstFormsOfChildLabor",
            label: "Worst Forms of Child Labor",
            description: "Have there been any worst forms of child labor in your company in the last 5 years?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean => dataset.social?.childLabor?.employeeSUnder18 == "Yes",
          },
          {
            name: "worstFormsOfChildLaborProhibition",
            label: "Worst Forms of Child Labor Prohibition",
            description:
              "Is the prohibition of the worst forms of child labor ensured in your company? These include: all forms of slavery or practices similar to slavery; the use, procuring or offering of a child for prostitution, the production of pornography or pornographic performances; the use, procuring or offering of a child for illicit activities, in particular for the production or trafficking of drugs; work which, by its nature or the circumstances in which it is performed, is likely to be harmful to the health, safety, or morals of children",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean => dataset.social?.childLabor?.worstFormsOfChildLabor == "Yes",
          },
          {
            name: "worstFormsOfChildLaborForms",
            label: "Worst Forms of Child Labor Forms",
            description: "Which of these worst forms of child labor are not prevented?",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean => dataset.social?.childLabor?.worstFormsOfChildLabor == "Yes",
          },
          {
            name: "measuresForPreventionOfEmploymentUnderLocalMinimumAge",
            label: "Measures for  Prevention of Employment Under Local Minimum Age ",
            description:
              "Does your company take measures to prevent the employment of children under the local minimum age?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "employmentUnderLocalMinimumAgePreventionEmploymentContracts",
            label: "Employment Under Local Minimum Age Prevention -  Employment Contracts",
            description: "Do you have a formal recruitment process, including employment contracts?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.childLabor?.measuresForPreventionOfEmploymentUnderLocalMinimumAge == "Yes",
          },
          {
            name: "employmentUnderLocalMinimumAgePreventionJobDescription",
            label: "Employment Under Local Minimum Age Prevention  -  Job Description",
            description:
              "Do you have a clear job description for employees under local minimum age as part of your recruitment process and employment contracts?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.childLabor?.measuresForPreventionOfEmploymentUnderLocalMinimumAge == "Yes",
          },
          {
            name: "employmentUnderLocalMinimumAgePreventionIdentityDocuments",
            label: "Employment Under Local Minimum Age Prevention - Identity Documents",
            description: "Do you check official documents, such as identity documents or certificates?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.childLabor?.measuresForPreventionOfEmploymentUnderLocalMinimumAge == "Yes",
          },
          {
            name: "employmentUnderLocalMinimumAgePreventionTraining",
            label: "Employment Under Local Minimum Age Prevention - Training",
            description:
              "Do you offer awareness trainings for people involved in the recruitment process to prevent child labor?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.childLabor?.measuresForPreventionOfEmploymentUnderLocalMinimumAge == "Yes",
          },
          {
            name: "employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge",
            label: "Employment Under Local Minimum Age Prevention - Checking Of Legal Minimum Age",
            description: "Do you regularly check that your employees are at least of minimum age?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.childLabor?.measuresForPreventionOfEmploymentUnderLocalMinimumAge == "Yes",
          },
          {
            name: "childLaborPreventionPolicy",
            label: "Child Labor Prevention Policy",
            description:
              "Does your company have a policy to prevent child labor? If yes, please share the policy with us.",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.childLabor?.measuresForPreventionOfEmploymentUnderLocalMinimumAge == "Yes",
          },
          {
            name: "additionalChildLaborOtherMeasures",
            label: "Additional Child Labor Other Measures",
            description:
              "Have any other measures been taken to prevent the employment of children under the locally applicable minimum age?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.childLabor?.measuresForPreventionOfEmploymentUnderLocalMinimumAge == "Yes",
          },
          {
            name: "additionalChildLaborOtherMeasuresDescription",
            label: "Additional Child Labor Other Measures Description",
            description:
              "Please list any other measures (if available) you take to prevent the employment of children under the locally applicable minimum age?",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.childLabor?.additionalChildLaborOtherMeasures?.value == "Yes",
          },
        ],
      },
      {
        name: "forcedLaborSlavery",
        label: "Forced labor, slavery",
        fields: [
          {
            name: "forcedLaborAndSlaveryPractices",
            label: "Forced Labor and Slavery Practices",
            description:
              "Does your company have practices that lead or may lead to forced labor and/or slavery? The following are included: Creating unacceptable working and living conditions by working in hazardous conditions or within unacceptable accommodations provided by the employer; Excessive levels of overtime; Use of intimidation, threats, and/or punishment; Other types of forced labor (e.g. debt bondage, human trafficking)",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "forcedLaborAndSlaveryPracticesSpecification",
            label: "Forced Labor and Slavery Practices Specification",
            description: "Please specify which practices apply.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPractices == "Yes",
          },
          {
            name: "forcedLaborAndSlaveryPreventionMeasures",
            label: "Forced Labor and Slavery Prevention Measures",
            description: "Does your company take measures to prevent forced labor and slavery?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "forcedLaborAndSlaveryPreventionEmploymentContracts",
            label: "Forced Labor and Slavery Prevention - Employment Contracts",
            description:
              "Do you have a formal hiring process, including employment contracts in the employee's local language, with appropriate wage and termination clauses?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures == "Yes",
          },
          {
            name: "forcedLaborAndSlaveryPreventionIdentityDocuments",
            label: "Forced Labor and Slavery Prevention - Identity Documents",
            description: "Do you prohibit the withholding of identity documents?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures == "Yes",
          },
          {
            name: "forcedLaborAndSlaveryPreventionFreeMovement",
            label: "Forced Labor and Slavery Prevention - Free Movement",
            description:
              "Do you ensure that doors and windows can be opened to allow the free movement of employees, as well as the ability to leave the company's premises at any time?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures == "Yes",
          },
          {
            name: "forcedLaborAndSlaveryPreventionProvisionSocialRoomsAndToilets",
            label: "Forced Labor and Slavery Prevention - Provision Social Rooms and Toilets",
            description: "Do you provide social rooms and toilets that can be used at any time?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures == "Yes",
          },
          {
            name: "forcedLaborAndSlaveryPreventionTraining",
            label: "Forced Labor and Slavery Prevention - Training",
            description:
              "Do you offer awareness trainings for people involved in the recruitment process to prevent forced labor?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures == "Yes",
          },
          {
            name: "forcedLaborPreventionPolicy",
            label: "Forced Labor Prevention Policy",
            description:
              "Does your company have a policy to prevent forced labor? If yes, please share the policy with us.",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures == "Yes",
          },
          {
            name: "forcedLaborAndSlaveryPreventionOtherMeasures",
            label: "Forced Labor and Slavery Prevention Other Measures",
            description: "Have any other measures been taken to prevent forced labor and slavery?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures == "Yes",
          },
          {
            name: "forcedLaborAndSlaveryPreventionOtherMeasuresDescription",
            label: "Forced Labor and Slavery Prevention Other Measures Description",
            description: "Please list any other measures (if available) you take to prevent forced labor and slavery.",

            component: "FreeTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionOtherMeasures?.value == "Yes",
          },
        ],
      },
      {
        name: "withholdingAdequateWages",
        label: "Withholding adequate wages",
        fields: [
          {
            name: "adequateWageWithholding",
            label: "Adequate Wage Withholding",
            description: "Is your company currently withholding adequate wages (adequate in the sense of local laws)?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "adequateWagesMeasures",
            label: "Adequate Wages Measures",
            description: "Are any measures taken to prevent the withholding of adequate wages?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "documentedWorkingHoursAndWages",
            label: "Documented Working Hours and Wages",
            description: "Does your company document the working hours and wages of its employees?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.withholdingAdequateWages?.adequateWagesMeasures == "Yes",
          },
          {
            name: "adequateLivingWage",
            label: "Adequate Living Wage",
            description:
              "Does your company pay employees adequate living wages? (the appropriate wage is at least the minimum wage set by the applicable law and is otherwise measured according to the law of the place of employment)",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.withholdingAdequateWages?.adequateWagesMeasures == "Yes",
          },
          {
            name: "regularWagesProcessFlow",
            label: "Regular Wages Process Flow",
            description:
              "Has your company implemented the payment of wages through standardized and regular process flows?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.withholdingAdequateWages?.adequateWagesMeasures == "Yes",
          },
          {
            name: "fixedHourlyWages",
            label: "Fixed Hourly Wages",
            description: "Do fixed hourly wages exist in your company?",

            component: "YesNoNaFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.withholdingAdequateWages?.adequateWagesMeasures == "Yes",
          },
          {
            name: "fixedPieceworkWages",
            label: "Fixed Piecework Wages",
            description: "Does your company have fixed piecework wages (pay per unit)?",

            component: "YesNoNaFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.withholdingAdequateWages?.adequateWagesMeasures == "Yes",
          },
          {
            name: "adequateWageOtherMeasures",
            label: "Adequate Wage Other Measures",
            description: "Have any other measures been taken to prevent withholding adequate wages?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.withholdingAdequateWages?.adequateWagesMeasures == "Yes",
          },
          {
            name: "adequateWageOtherMeasuresDescription",
            label: "Adequate Wage Other Measures Description",
            description: "Please list other measures (if available) you take to prevent withholding adequate wages?",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.withholdingAdequateWages?.adequateWageOtherMeasures?.value == "Yes",
          },
        ],
      },
      {
        name: "disregardForOccupationalHealthSafety",
        label: "Disregard for occupational health/safety",
        fields: [
          {
            name: "lowSkillWork",
            label: "Low Skill Work",
            description: "Do your employees perform low-skill or repetitive manual labor?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "hazardousMachines",
            label: "Hazardous Machines",
            description: "Are hazardous machines used in the manufacturing of (preliminary) products?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "oshMeasures",
            label: "OSH Measures",
            description: "Does your company take measures to prevent the disregard for occupational health and safety?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "oshPolicy",
            label: "OSH Policy",
            description:
              "Has your company implemented and enforced a formal occupational health and safety (OSH) policy that complies with local laws, industry requirements, and international standards?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.disregardForOccupationalHealthSafety?.oshMeasures == "Yes",
          },
          {
            name: "oshTraining",
            label: "OSH Training",
            description: "Has your company introduced mandatory training for employees to improve occupational safety?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.disregardForOccupationalHealthSafety?.oshMeasures == "Yes",
          },
          {
            name: "healthAndSafetyPolicy",
            label: "Health and Safety Policy",
            description: "Does your company have a Health and Safety Policy? If yes, please share the policy with us.",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.disregardForOccupationalHealthSafety?.oshMeasures == "Yes",
          },
          {
            name: "otherOshMeasures",
            label: "Other OSH Measures",
            description:
              "Have any other measures been taken to prevent the disregard for occupational health and safety?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.disregardForOccupationalHealthSafety?.oshMeasures == "Yes",
          },
          {
            name: "otherOshMeasuresDescription",
            label: "Other OSH Measures Description",
            description:
              "Please list other measures (if available) you take to prevent the disregard for occupational health and safety?",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.disregardForOccupationalHealthSafety?.otherOshMeasures?.value == "Yes",
          },
          {
            name: "under10WorkplaceAccidents",
            label: "Under 10 Workplace Accidents",
            description:
              "Have there been less than 10 incidents in which employees suffered work-related injuries with serious consequences in the past fiscal year?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
      {
        name: "disregardForFreedomOfAssociation",
        label: "Disregard for freedom of association",
        fields: [
          {
            name: "freedomOfAssociation",
            label: "Freedom Of Association",
            description: "Does your company ensure that employees are free to form or join trade unions?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "employeeRepresentation",
            label: "Employee Representation",
            description: "What is your percentage of employees who are represented by trade unions?",

            unit: "%",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.disregardForFreedomOfAssociation?.freedomOfAssociation == "Yes",
            validation: "between:0,100",
          },
          {
            name: "freedomOfAssociationDisregardPrevention",
            label: "Freedom of Association Disregard Prevention",
            description: "Does your company take measures to prevent the disregard for freedom of association?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "discriminationForTradeUnionMembers",
            label: "Discrimination for Trade Union Members",
            description:
              "Does your company ensure that no consequences are taken against employees in the event of the formation, joining, and membership of a trade union?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.disregardForFreedomOfAssociation?.freedomOfAssociationDisregardPrevention == "Yes",
          },
          {
            name: "freedomOfOperationForTradeUnion",
            label: "Freedom of Operation for Trade Union",
            description:
              "Does your company ensure that trade unions are free to operate in accordance with the law in the place of employment?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.disregardForFreedomOfAssociation?.freedomOfAssociationDisregardPrevention == "Yes",
          },
          {
            name: "freedomOfAssociationTraining",
            label: "Freedom of Association Training",
            description:
              "Do employees receive information about their rights as a part of training, notices, or company brochures?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.disregardForFreedomOfAssociation?.freedomOfAssociationDisregardPrevention == "Yes",
          },
          {
            name: "worksCouncil",
            label: "Works Council",
            description:
              "Does your company have a works council or employee representative committee (if these are legal according to local law)?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.disregardForFreedomOfAssociation?.freedomOfAssociationDisregardPrevention == "Yes",
          },
          {
            name: "freedomOfAssociationOtherMeasures",
            label: "Freedom of Association Other Measures",
            description: "Have other measures been taken to prevent the disregard for freedom of association?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.disregardForFreedomOfAssociation?.freedomOfAssociationDisregardPrevention == "Yes",
          },
          {
            name: "freedomOfAssociationOtherMeasuresDescription",
            label: "Freedom of Association Other Measures Description",
            description:
              "Please list other measures (if available) you take to prevent the disregard for freedom of association.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.disregardForFreedomOfAssociation?.freedomOfAssociationOtherMeasures?.value == "Yes",
          },
        ],
      },
      {
        name: "unequalTreatmentOfEmployment",
        label: "Unequal treatment of employment",
        fields: [
          {
            name: "unequalTreatmentOfEmployment",
            label: "Unequal Treatment of Employment",
            description:
              "Does your company treat employees unequally because of national/ethnic origin, social origin, health status, disability, sexual orientation, age, gender, political opinion, religion or belief?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "unequalTreatmentOfEmploymentPreventionMeasures",
            label: "Unequal Treatment of Employment Prevention Measures",
            description: "Does your company take measures to prevent unequal treatment of employment?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.unequalTreatmentOfEmployment?.unequalTreatmentOfEmployment == "Yes",
          },
          {
            name: "diversityAndInclusionRole",
            label: "Diversity and Inclusion Role",
            description:
              "Is a member of your company's management responsible for promoting diversity in the workforce and among business partners?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.unequalTreatmentOfEmployment?.unequalTreatmentOfEmployment == "Yes",
          },
          {
            name: "preventionOfMistreatments",
            label: "Prevention of Mistreatments",
            description:
              "Does your company's management promote a work environment free from physical, sexual, mental abuse, threats or other forms of mistreatment? (e.g. diversity program)",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.unequalTreatmentOfEmployment?.unequalTreatmentOfEmployment == "Yes",
          },
          {
            name: "unequalTreatmentPreventionTraining",
            label: "Unequal Treatment Prevention Training",
            description:
              "Has your company introduced mandatory offers and training for employees that target unequal treatment of employment?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.unequalTreatmentOfEmployment?.unequalTreatmentOfEmployment == "Yes",
          },
          {
            name: "equalOpportunitiesOfficer",
            label: "Equal Opportunities Officer",
            description: "Do you have an equal opportunities officer or a similar function?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.unequalTreatmentOfEmployment?.unequalTreatmentOfEmployment == "Yes",
          },
          {
            name: "equalEmploymentPolicy",
            label: "Equal Employment Policy",
            description: "Does your company have an equal employment policy? If yes, please share the policy with us.",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.unequalTreatmentOfEmployment?.unequalTreatmentOfEmployment == "Yes",
          },
          {
            name: "unequalTreatmentPreventionOtherMeasures",
            label: "Unequal Treatment Prevention Other Measures",
            description: "Have other measures been taken to prevent unequal treatment of employment?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.unequalTreatmentOfEmployment?.unequalTreatmentOfEmployment == "Yes",
          },
          {
            name: "unequalTreatmentPreventionOtherMeasuresDescription",
            label: "Unequal Treatment Prevention Other Measures Description",
            description:
              "Please list other measures (if available) you take to prevent unequal treatment of employment.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.unequalTreatmentOfEmployment?.unequalTreatmentPreventionOtherMeasures?.value == "Yes",
          },
        ],
      },
      {
        name: "contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption",
        label: "Contamination of soil/water/air, noise emissions, excessive water consumption",
        fields: [
          {
            name: "harmfulSoilChange",
            label: "Harmful Soil Change",
            description: "Is there a risk of your company causing a harmful soil change?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "soilDegradation",
            label: "Soil Degradation",
            description:
              "Does your company have measures in place to prevent the degradation of the local soil structure caused by the use of heavy machinery?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.harmfulSoilChange ==
              "Yes",
          },
          {
            name: "soilErosion",
            label: "Soil Erosion",
            description:
              "Does your company have measures in place to prevent soil erosion caused by deforestation or overgrazing?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.harmfulSoilChange ==
              "Yes",
          },
          {
            name: "soilBorneDiseases",
            label: "Soil-borne Diseases",
            description:
              "Does your company have measures in place to prevent the development of soil-borne diseases and pests to maintain soil fertility?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.harmfulSoilChange ==
              "Yes",
          },
          {
            name: "soilContamination",
            label: "Soil Contamination",
            description:
              "Does your company have measures in place to prevent soil contamination caused by antibiotics and toxins?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.harmfulSoilChange ==
              "Yes",
          },
          {
            name: "soilSalinization",
            label: "Soil Salinization",
            description: "Does your company have measures in place to prevent soil salinization?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.harmfulSoilChange ==
              "Yes",
          },
          {
            name: "soilProtectionPolicy",
            label: "Soil Protection Policy",
            description: "Does your company have a soil protection policy? If yes, please share the policy with us.",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.harmfulSoilChange ==
              "Yes",
          },
          {
            name: "soilSpotChecks",
            label: "Soil Spot Checks",
            description:
              "Does your company carry out regular spot checks of the soils with corresponding documentation?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.harmfulSoilChange ==
              "Yes",
          },
          {
            name: "harmfulWaterPollution",
            label: "Harmful Water Pollution",
            description: "Is there a risk of your company causing harmful water pollution?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "fertilizersOrPollutants",
            label: "Fertilizers or Pollutants",
            description: "Does your company use fertilizers or pollutants such as chemicals or heavy metals?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulWaterPollution == "Yes",
          },
          {
            name: "wasteWaterFiltration",
            label: "Waste Water Filtration",
            description: "Does your company have waste water filtration systems?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulWaterPollution == "Yes",
          },
          {
            name: "waterProtectionPolicy",
            label: "Water Protection Policy",
            description: "Does your company have a water protection policy? If yes, please share the policy with us. ",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulWaterPollution == "Yes",
          },
          {
            name: "waterSpotChecks",
            label: "Water Spot Checks",
            description:
              "Does your company carry out regular spot checks of the waters with corresponding documentation?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulWaterPollution == "Yes",
          },
          {
            name: "harmfulAirPollution",
            label: "Harmful Air Pollution",
            description: "Is there a risk of harmful air pollution caused by your company?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "airFiltration",
            label: "Air Filtration",
            description: "Does your company have air filtration systems?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.harmfulAirPollution ==
              "Yes",
          },
          {
            name: "airQualityProtectionPolicy",
            label: "Air Quality Protection Policy",
            description:
              "Does your company have an air quality protection policy? If yes, please share the policy with us. ",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.harmfulAirPollution ==
              "Yes",
          },
          {
            name: "airQualitySpotChecks",
            label: "Air Quality Spot Checks",
            description:
              "Does your company conduct regular spot checks of air quality with corresponding documentation?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.harmfulAirPollution ==
              "Yes",
          },
          {
            name: "harmfulNoiseEmission",
            label: "Harmful Noise Emission",
            description: "Is there a risk of harmful noise emission caused by your company?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "reductionOfNoiseEmissions",
            label: "Reduction of Noise Emissions",
            description: "Has your company implemented structural measures to reduce noise emissions?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulNoiseEmission == "Yes",
          },
          {
            name: "noiseReductionPolicy",
            label: "Noise Reduction Policy",
            description: "Does your company have a noise reduction policy? If yes, please share the policy with us. ",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulNoiseEmission == "Yes",
          },
          {
            name: "noiseEmissionsSpotChecks",
            label: "Noise Emissions Spot Checks",
            description:
              "Does your company carry out regular spot checks of noise emissions with corresponding documentation?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulNoiseEmission == "Yes",
          },
          {
            name: "excessiveWaterConsumption",
            label: "Excessive Water Consumption",
            description: "Is there a risk of excessive water consumption in your company?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "waterSavingMeasures",
            label: "Water Saving Measures",
            description: "Does your company take measures to prevent excessive water consumption?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.excessiveWaterConsumption == "Yes",
          },
          {
            name: "waterSavingMeasuresName",
            label: "Water Saving Measures Name",
            description: "If yes, which ones?",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.waterSavingMeasures ==
              "Yes",
          },
          {
            name: "waterUseReductionPolicy",
            label: "Water Use Reduction Policy",
            description:
              "Does your company have a water use reduction policy? If yes, please share the policy with us. ",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.excessiveWaterConsumption == "Yes",
          },
          {
            name: "waterConsumptionSpotChecks",
            label: "Water Consumption Spot Checks",
            description:
              "Does your company carry out regular spot checks of water consumption with corresponding documentation?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.excessiveWaterConsumption == "Yes",
          },
          {
            name: "waterSources",
            label: "Water Sources",
            description:
              "Does your company use water sources that are important for the local population or agriculture?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.excessiveWaterConsumption == "Yes",
          },
          {
            name: "contaminationPreventionMeasures",
            label: "Contamination Prevention Measures",
            description:
              "Have other measures been taken to prevent the risk of harmful soil change, water pollution, air pollution, harmful noise emission or excessive water consumption that: Significantly affects the natural basis for food production; Denies a person access to safe drinking water; Impedes or destroys a person's access to sanitary facilities; Harms the health of any person",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "contaminationPreventionMeasuresDescription",
            label: "Contamination Prevention Measures Description",
            description:
              "Please list any other measures (if available) you are taking to prevent the risk of harmful soil change, water pollution, air pollution, harmful noise emission or excessive water consumption that: Significantly affects the natural basis for food production; Denies a person access to safe drinking water; Impedes or destroys a person's access to sanitary facilities; Harms the health of any person",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.contaminationPreventionMeasures?.value == "Yes",
          },
        ],
      },
      {
        name: "unlawfulEvictionDeprivationOfLandForestAndWater",
        label: "Unlawful eviction/deprivation of land, forest and water",
        fields: [
          {
            name: "unlawfulEvictionAndTakingOfLand",
            label: "Unlawful Eviction and Taking of Land",
            description:
              "Is your company, as a result of the acquisition, development, or other use of land, forests, or bodies of water, which secures a person's livelihood, at risk of carrying out: Unlawful evictions; Unlawful claims of land, forests, or water?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "unlawfulEvictionAndTakingOfLandRisk",
            label: "Unlawful Eviction and Taking of Land - Risk",
            description: "If so, what exactly is the risk?",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.unlawfulEvictionDeprivationOfLandForestAndWater?.unlawfulEvictionAndTakingOfLand == "Yes",
          },
          {
            name: "unlawfulEvictionAndTakingOfLandMeasures",
            label: "Unlawful Eviction and Taking of Land - Measures",
            description:
              "Has your company developed and implemented measures that avoid, reduce, mitigate, or remedy direct and indirect negative impacts on the land, and natural resources of indigenous peoples and local communities?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "modelContractsForLandPurchaseOrLeasing",
            label: "Model Contracts for Land Purchase or Leasing",
            description: "Does your company have model contracts for buying or leasing land?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.unlawfulEvictionDeprivationOfLandForestAndWater
                ?.unlawfulEvictionAndTakingOfLandMeasures == "Yes",
          },
          {
            name: "involvementOfLocalsInDecisionMaking",
            label: "Involvement of Locals in Decision-Making",
            description: "Are local communities and stakeholders involved in decision-making processes?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.unlawfulEvictionDeprivationOfLandForestAndWater
                ?.unlawfulEvictionAndTakingOfLandMeasures == "Yes",
          },
          {
            name: "governanceOfTenurePolicy",
            label: "Governance of Tenure Policy",
            description: "Does your company have a policy for the governance of tenure?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.unlawfulEvictionDeprivationOfLandForestAndWater
                ?.unlawfulEvictionAndTakingOfLandMeasures == "Yes",
          },
          {
            name: "unlawfulEvictionAndTakingOfLandOtherMeasures",
            label: "Unlawful Eviction and Taking of Land - Other Measures",
            description:
              "Have other measures been taken to avoid, reduce, mitigate, or remedy direct and indirect adverse impacts on the lands and natural resources of indigenous peoples and local communities?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.unlawfulEvictionDeprivationOfLandForestAndWater
                ?.unlawfulEvictionAndTakingOfLandMeasures == "Yes",
          },
          {
            name: "unlawfulEvictionAndTakingOfLandOtherMeasuresDescription",
            label: "Unlawful Eviction and Taking of Land - Other Measures Description",
            description:
              "Please list other measures (if available) you take to avoid, reduce, mitigate, or remedy direct and indirect adverse impacts on the lands and natural resources of indigenous peoples and local communities.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.unlawfulEvictionDeprivationOfLandForestAndWater
                ?.unlawfulEvictionAndTakingOfLandOtherMeasures?.value == "Yes",
          },
          {
            name: "voluntaryGuidelinesOnTheResponsibleGovernanceOfTenure",
            label: "Voluntary Guidelines on the Responsible Governance of Tenure",
            description:
              "Have you implemented the voluntary guidelines on the responsible governance of tenure in your company?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
      {
        name: "useOfPrivatePublicSecurityForcesWithDisregardForHumanRights",
        label: "Use of private/public security forces with disregard for human rights",
        fields: [
          {
            name: "useOfPrivatePublicSecurityForces",
            label: "Use of Private Public Security Forces",
            description: "Does your company use private and/or public security forces to protect company projects?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights",
            label: "Use of Private Public Security Forces and Risk of Violation of Human Rights",
            description:
              "Does your company have measures in place to prevent your security forces from: Violating the prohibition of torture or cruel, inhuman, or degrading treatment; Damaging life or limbs; Impairing the right to exercise the freedom of association?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForces == "Yes",
          },
          {
            name: "instructionOfSecurityForces",
            label: "Instruction of Security Forces",
            description: "Do you have adequate instructions for the security forces?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights == "Yes",
          },
          {
            name: "humanRightsTraining",
            label: "Human Rights Training",
            description: "Are security forces trained on human rights?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights == "Yes",
          },
          {
            name: "stateSecurityForces",
            label: "State Security Forces",
            description:
              "Have the state security forces been checked on former human right violations before they were commissioned? (Only in the case of state security forces) ",

            component: "YesNoNaFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights == "Yes",
          },
          {
            name: "privateSecurityForces",
            label: "Private Security Forces",
            description:
              "Have the contractual relationships with private security forces been designed in a way that they comply with the applicable legal framework? (Only in the case of private security forces)",

            component: "YesNoNaBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights == "Yes",
          },
          {
            name: "useOfPrivatePublicSecurityForcesMeasures",
            label: "Use of Private Public Security Forces Measures",
            description:
              "Have other measures been taken to prevent the use of private and/or public security forces that violate human rights?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights == "Yes",
          },
          {
            name: "useOfPrivatePublicSecurityForcesMeasuresDescription",
            label: "Use of Private Public Security Forces Measures Description",
            description:
              "Please list any other measures you are taking to prevent the use of private and/or public security forces that violate human rights.",

            component: "FreeTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesMeasures?.value == "Yes",
          },
        ],
      },
    ],
  },
  {
    name: "environmental",
    label: "Environmental",
    color: "",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "useOfMercuryMercuryWasteMinamataConvention",
        label: "Use of mercury, mercury waste (Minamata Convention)",
        fields: [
          {
            name: "mercuryAndMercuryWasteHandling",
            label: "Mercury and Mercury Waste Handling",
            description: "Does your company deal with mercury or mercury waste as part of its business model?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "mercuryAddedProductsHandling",
            label: "Mercury Added-Products Handling",
            description:
              "Are you involved in the manufacturing, use, treatment, import, or export of products containing mercury?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAndMercuryWasteHandling ==
              "Yes",
          },
          {
            name: "mercuryAddedProductsHandlingRiskOfExposure",
            label: "Mercury Added-Products Handling - Risk of Exposure",
            description:
              "Is there a risk of manufacturing, importing or exporting products containing mercury that are not subject to the Annex A Part 1 exemption of the Minamata Convention (BGBI. 2017 II p.610, 611)?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAndMercuryWasteHandling ==
              "Yes",
          },
          {
            name: "mercuryAddedProductsHandlingRiskOfDisposal",
            label: "Mercury Added-Products Handling - Risk of Disposal",
            description:
              "If there are products that are contaminated with mercury, is there a risk within your company that mercury waste will be disposed of not in accordance with the provisions of Article 11 of the Minamata Agreement (BGBI. 2017 II p. 610, 611)?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAndMercuryWasteHandling ==
              "Yes",
          },
          {
            name: "mercuryAndMercuryCompoundsProductionAndUse",
            label: "Mercury and Mercury Compounds Production and Use",
            description: "Are there manufacturing processes in your company that use mercury or mercury compounds?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAndMercuryWasteHandling ==
              "Yes",
          },
          {
            name: "mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure",
            label: "Mercury and Mercury Compounds Production and Use - Risk of Exposure",
            description:
              "Is there a risk in your company that mercury or mercury compounds used in the manufacturing process have already exceeded the specified phase-out date and are therefore prohibited according to Article 5(2), Annex B of the Minamata Agreement (BGBI. 2017 II p. 616, 617)?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAndMercuryWasteHandling ==
              "Yes",
          },
          {
            name: "mercuryAndMercuryWasteUsePreventionMeasures",
            label: "Mercury and Mercury Waste Use Prevention Measures",
            description: "Does your company take measures to prevent the use of mercury and mercury waste?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAndMercuryWasteHandling ==
              "Yes",
          },
          {
            name: "mercuryAndMercuryWasteHandlingPolicy",
            label: "Mercury and Mercury Waste Handling Policy",
            description:
              "Does your company have a policy for safely handling mercury or mercury waste? If yes, please share the policy with us.\n\n",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.useOfMercuryMercuryWasteMinamataConvention
                ?.mercuryAndMercuryWasteUsePreventionMeasures == "Yes",
          },
          {
            name: "mercuryAndMercuryWasteUsePreventionOtherMeasures",
            label: "Mercury and Mercury Waste Use Prevention Other Measures",
            description: "Have other measures been taken to prevent the use of mercury and mercury waste?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.useOfMercuryMercuryWasteMinamataConvention
                ?.mercuryAndMercuryWasteUsePreventionMeasures == "Yes",
          },
          {
            name: "mercuryAndMercuryWasteUsePreventionOtherMeasuresDescription",
            label: "Mercury and Mercury Waste Use Prevention Other Measures Description",
            description:
              "Please list other measures (if available) you take to prevent the use of mercury and mercury waste.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.useOfMercuryMercuryWasteMinamataConvention
                ?.mercuryAndMercuryWasteUsePreventionOtherMeasures?.value == "Yes",
          },
        ],
      },
      {
        name: "productionAndUseOfPersistentOrganicPollutantsPopsConvention",
        label: "Production and use of persistent organic pollutants (POPs Convention)",
        fields: [
          {
            name: "persistentOrganicPollutantsProductionAndUse",
            label: "Persistent Organic Pollutants Production and Use",
            description:
              "Do you use and/or produce persistent organic pollutants (POPs), i.e. chemical compounds that don´t decompose, or transform very slowly in the environment?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "persistentOrganicPollutantsUsed",
            label: "Persistent Organic Pollutants Used",
            description: "If yes, which organic pollutants are used and/or produced?",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse == "Yes",
          },
          {
            name: "persistentOrganicPollutantsProductionAndUseRiskOfExposure",
            label: "Persistent Organic Pollutants Production and Use - Risk Of Exposure",
            description:
              "Is there a risk in your company that these organic pollutants fall under Article 3(1)(a), Annex A of the Stockholm Convention of May 23rd 2001 on persistent organic pollutants (BGBl. 2002 II p. 803-804) (POPs Convention) and therefore banned?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse == "Yes",
          },
          {
            name: "persistentOrganicPollutantsProductionAndUseRiskOfDisposal",
            label: "Persistent Organic Pollutants Production and Use - Risk Of Disposal",
            description:
              "In relation to the waste of these pollutants, is there a risk that they will be subject to the rules laid down in the applicable legal system in accordance with the provisions of Article 6(1)(d)(i) and (ii) of the POP Convention (BGBI. 2002 II p. 803, 804) and will: Not be handled, collected, stored, or transported in an environmentally sound manner; Not be disposed of in an environmentally friendly manner, i.e. disposed of in such a way that the persistent organic pollutants contained therein are destroyed or irreversibly converted?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse == "Yes",
          },
          {
            name: "persistentOrganicPollutantsUsePreventionMeasures",
            label: "Persistent Organic Pollutants Use Prevention Measures",
            description: "Does your company take measures to prevent the use of persistent organic pollutants (POP)?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse == "Yes",
          },
          {
            name: "persistentOrganicPollutantsUsePolicy",
            label: "Persistent Organic Pollutants Use Policy",
            description:
              "Does your company have a policy for handling these materials? If yes, please share the policy with us. ",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsUsePreventionMeasures == "Yes",
          },
          {
            name: "persistentOrganicPollutantsUsePreventionOtherMeasures",
            label: "Persistent Organic Pollutants Use Prevention Other Measures",
            description: "Have other measures been taken to prevent the use of persistent organic pollutants (POP)?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsUsePreventionMeasures == "Yes",
          },
          {
            name: "persistentOrganicPollutantsUsePreventionOtherMeasuresDescription",
            label: "Persistent Organic Pollutants Use Prevention Other Measures Description",
            description:
              "Please list other measures (if available) you take to prevent the use of persistent organic pollutants (POP).",

            component: "FreeTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsUsePreventionOtherMeasures?.value == "Yes",
          },
        ],
      },
      {
        name: "exportImportOfHazardousWasteBaselConvention",
        label: "Export/import of hazardous waste (Basel Convention)",
        fields: [
          {
            name: "persistentOrganicPollutantsProductionAndUseTransboundaryMovements",
            label: "Persistent Organic Pollutants Production And Use - Transboundary Movements",
            description:
              "Is there a risk in your company that: Hazardous waste within the meaning of the Basel Convention  (Article 1(1), BGBI. 1994 II p. 2703, 2704) or other waste that requires special consideration (household waste or its byproducts) is transported across borders?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "persistentOrganicPollutantsProductionAndUseRiskForImportingState",
            label: "Persistent Organic Pollutants Production and Use - Risk for Importing State",
            description:
              'Are these wastes transported or shipped to an importing State that is subject to the Basel Convention and has not given its written consent to the specific import (if that importing State has not prohibited the importation of that hazardous waste) (Article 4(1)(c)); is not a contracting party (Article 4, paragraph 5); does not treat waste in an environmentally friendly manner because it does not have the appropriate capacity for environmentally friendly disposal and cannot guarantee this elsewhere either (Article 4 paragraph 8 sentence 1) or\ntransported by a party that has banned the import of such hazardous and other wastes (Article 4(1)(b) Basel Convention)? (The term "importing state" includes: a contracting party to which a transboundary shipment of hazardous waste or other waste is planned for the purpose of disposal or for the purpose of loading prior to disposal in an area not under the sovereignty of a state. (Article 2 No. 11)',

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.exportImportOfHazardousWasteBaselConvention
                ?.persistentOrganicPollutantsProductionAndUseTransboundaryMovements == "Yes",
          },
          {
            name: "hazardousWasteTransboundaryMovementsLocatedOecdEuLiechtenstein",
            label: "Hazardous Waste Transboundary Movements - Located OECD, EU, Liechtenstein",
            description: "Is your company based in a country that is within the OECD, EU, or Liechtenstein?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.exportImportOfHazardousWasteBaselConvention
                ?.persistentOrganicPollutantsProductionAndUseTransboundaryMovements == "Yes",
          },
          {
            name: "hazardousWasteTransboundaryMovementsOutsideOecdEuOrLiechtenstein",
            label: "Hazardous Waste Transboundary Movements - Outside OECD, EU, or Liechtenstein",
            description:
              "Is there a risk in your company that hazardous waste is transported to a country that is outside the OECD, EU / Liechtenstein?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.exportImportOfHazardousWasteBaselConvention
                ?.persistentOrganicPollutantsProductionAndUseTransboundaryMovements == "Yes",
          },
          {
            name: "hazardousWasteTransportPreventionMeasures",
            label: "Hazardous Waste Transport Prevention Measures",
            description: "Does your company take measures to prevent the transport of hazardous waste?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.exportImportOfHazardousWasteBaselConvention
                ?.persistentOrganicPollutantsProductionAndUseTransboundaryMovements == "Yes",
          },
          {
            name: "wastePolicy",
            label: "Waste Policy",
            description: "Does your company have a waste policy? If yes, please share the policy with us. ",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.exportImportOfHazardousWasteBaselConvention
                ?.persistentOrganicPollutantsProductionAndUseRiskForImportingState == "Yes",
          },
          {
            name: "hazardousWasteTransportPreventionOtherMeasures",
            label: "Hazardous Waste Transport Prevention Other Measures",
            description: "Have other measures been taken to prevent the transport of hazardous waste?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.exportImportOfHazardousWasteBaselConvention
                ?.persistentOrganicPollutantsProductionAndUseRiskForImportingState == "Yes",
          },
          {
            name: "hazardousWasteDisposal",
            label: "Hazardous Waste Disposal",
            description:
              "Do you dispose of hazardous waste in accordance with the Basel Convention (Article 1(1), BGBI. 1994 II p. 2703, 2704)?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "hazardousWasteDisposalRiskOfImport",
            label: "Hazardous Waste Disposal - Risk of Import",
            description:
              "Are you at risk of having these hazardous wastes imported from a country that is not a member of the Basel Convention?",

            component: "YesNoFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.exportImportOfHazardousWasteBaselConvention?.hazardousWasteDisposal == "Yes",
          },
          {
            name: "hazardousWasteDisposalOtherWasteImport",
            label: "Hazardous Waste Disposal - Other Waste Import",
            description:
              "Do you import other wastes that require special consideration (household waste, residues from incineration of household waste) (Article 1(2))?",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.exportImportOfHazardousWasteBaselConvention?.hazardousWasteDisposal == "Yes",
          },
          {
            name: "hazardousWasteDisposalOtherWasteImportDescription",
            label: "Hazardous Waste Disposal - Other Waste Import Description",
            description:
              "Please describe the other imported wastes that require special consideration (household waste, residues from incineration of household waste) (Article 1(2)).",

            component: "FreeTextFormField",
            required: false,
            showIf: (dataset: LksgData): boolean =>
              dataset.environmental?.exportImportOfHazardousWasteBaselConvention?.hazardousWasteDisposalOtherWasteImport
                ?.value == "Yes",
          },
        ],
      },
    ],
  },
] as Category[];
