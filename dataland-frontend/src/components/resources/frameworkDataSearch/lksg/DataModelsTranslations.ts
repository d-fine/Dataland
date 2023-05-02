import { LksgData } from "@clients/backend";

export const listOfProductionSitesConvertedNames = {
  name: "Name",
  isInHouseProductionOrIsContractProcessing: "Is In-house Production Or Is Contract Processing",
  country: "Country",
  city: "City",
  streetAndHouseNumber: "Street and house number",
  postalCode: "Postal Code",
  listOfGoodsOrServices: "List Of Goods Or Services",
};

export interface Category {
  name: string;
  label: string;
  color: string;
  subcategories: Array<Subcategory>;
}

export interface Subcategory {
  name: string;
  label: string;
  fields: Array<Field>;
}

interface Field {
  name: string;
  label: string;
  description: string;
  component: string;
  dependency: string;
  validation: string;

  // input field specific values
  placeholder: string;

  // selection specific values
  options: Option[];
}

export interface Option {
  label: string;
  value: string;
}

export const lksgDataModel = [
  {
    name: "general",
    label: "General",
    color: "BLUE",
    subcategories: [
      {
        name: "masterData",
        label: "Master Data",
        fields: [
          {
            showIf: (): boolean => true,
            name: "dataDate",
            component: "DateFormField",
            description: "The date until for which the information collected is valid",
            label: "Data Date",
          },
          {
            showIf: (): boolean => true,
            name: "name",
            component: "InputTextFormField",
            description: "Give the name (including legal form) of your company",
            label: "Name",
          },
          {
            showIf: (): boolean => true,
            name: "address",
            component: "AddressFormField",
            description: "Enter the address of your company (format: street, house number, zip code, city, country)",
            label: "Address",
          },
          {
            showIf: (): boolean => true,
            name: "headOffice",
            component: "YesNoFormField",
            description:
              "Is your head office, administrative headquarters, registered office or subsidiary located in Germany?",
            label: "Head Office",
          },
          {
            showIf: (): boolean => true,
            name: "commercialRegister",
            component: "InputTextFormField",
            description: "State your VAT number",
            label: "Commercial Register",
          },
          {
            showIf: (): boolean => true,
            name: "groupOfCompanies",
            component: "YesNoFormField",
            description: "Do you belong to a group of companies?",
            label: "Group of Companies",
          },
          {
            showIf: (dataModel: LksgData): boolean => dataModel?.general?.masterData?.groupOfCompanies === "Yes",
            name: "groupOfCompaniesName",
            component: "InputTextFormField",
            description: "What is the group of companies called?",
            label: "Group of Companies Name",
          },
          {
            showIf: (): boolean => true,
            name: "industry",
            component: "NaceCodeFormField",
            description: "In which industry is your company primarily active?",
            label: "Industry",
          },
          {
            showIf: (): boolean => true,
            name: "numberOfEmployees",
            component: "NumberFormField",
            description:
              "What is the total number of employees (including temporary workers with assignment duration >6 months)?",
            label: "Number Of Employees",
          },
          {
            showIf: (): boolean => true,
            name: "seasonalOrMigrantWorkers",
            component: "YesNoFormField",
            description: "Do you employ seasonal or migrant workers?",
            label: "Seasonal or Migrant Workers",
          },
          {
            showIf: (): boolean => true,
            options: [
              {
                label: "<10%",
                value: "<10%",
              },
              {
                label: "10-25%",
                value: "1025%",
              },
              {
                label: "25%-50%",
                value: "25%50%",
              },
              {
                label: ">50%",
                value: ">50%",
              },
            ],
            name: "shareOfTemporaryWorkers",
            component: "RadioButtonsFormField",
            description: "What is the share of temporary workers vs total number of employees in the company?",
            label: "Share Of Temporary Workers",
          },
          {
            showIf: (): boolean => true,
            name: "totalRevenueCurrency",
            component: "SingleSelectFormField",
            description: "The 3-letter code (ISO 4217) representing the currency used for the total revenue",
            label: "Total Revenue Currency",
          },
          {
            showIf: (): boolean => true,
            name: "totalRevenue",
            component: "NumberFormField",
            description: "Total revenue p. a.",
            label: "Total Revenue",
          },
          {
            showIf: (): boolean => true,
            name: "fixedAndWorkigCapital",
            component: "NumberFormField",
            description: "What is your fixed and working capital? (only for own operations)",
            label: "Fixed and Workig Capital",
          },
        ],
      },
      {
        name: "productionSpecific",
        label: "Production-specific",
        fields: [
          {
            showIf: (): boolean => true,
            name: "manufacturingCompany",
            component: "YesNoFormField",
            description: "Is your company a manufacturing company?",
            label: "Manufacturing Company",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.general?.productionSpecific?.manufacturingCompany === "Yes",
            name: "capacity",
            component: "InputTextFormField",
            description: "If yes, what is your production capacity per year, e.g. units/year?",
            label: "Capacity",
          },
          {
            showIf: (): boolean => true,
            name: "isContractProcessing",
            component: "YesNoFormField",
            description: "Is production done via subcontracting?",
            label: "Is Contract Processing",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.general?.productionSpecific?.isContractProcessing === "Yes",
            name: "subcontractingCompaniesCountries",
            component: "MultiSelectFormField",
            description: "In which countries do the subcontracting companies operate?",
            label: "Subcontracting Companies Countries",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.general?.productionSpecific?.isContractProcessing === "Yes",
            name: "subcontractingCompaniesIndustries",
            component: "NaceCodeFormField",
            description: "In which industries do the subcontracting companies operate?",
            label: "Subcontracting Companies Industries",
          },
          {
            showIf: (): boolean => true,
            name: "productionSites",
            component: "YesNoFormField",
            description: "Do you have production sites in your company?",
            label: "Production Sites",
          },
          {
            showIf: (dataModel: LksgData): boolean => dataModel?.general?.productionSpecific?.productionSites === "Yes",
            name: "listOfProductionSites",
            component: "ProductionSiteFormField",
            description: "Please list the production sites in your company.",
            label: "List Of Production Sites",
          },
          {
            showIf: (): boolean => true,
            options: [
              {
                label: "National",
                value: "national",
              },
              {
                label: "International",
                value: "international",
              },
              {
                label: "Both",
                value: "both",
              },
            ],
            name: "market",
            component: "RadioButtonsFormField",
            description: "Does your business focus predominantly on national or international markets?",
            label: "Market",
          },
          {
            showIf: (): boolean => true,
            name: "specificProcurement",
            component: "YesNoFormField",
            description:
              "Does your company have specific procurement models such as: short-lived and changing business relationships, or high price pressure or tightly timed or short-term adjusted delivery deadlines and conditions with suppliers",
            label: "Specific Procurement",
          },
        ],
      },
      {
        name: "productionSpecificOwnOperations",
        label: "Production-specific - Own Operations",
        fields: [
          {
            showIf: (): boolean => true,
            name: "mostImportantProducts",
            component: "InputTextFormField",
            description:
              "Please give an overview of the most important products or services in terms of sales that your company manufactures and/or distributes or offers (own operations)",
            label: "Most Important Products",
          },
          {
            showIf: (): boolean => true,
            name: "productionSteps",
            component: "InputTextFormField",
            description: "Please give a brief overview of the production steps/activities undertaken",
            label: "Production Steps",
          },
          {
            showIf: (): boolean => true,
            name: "relatedCorporateSupplyChain",
            component: "InputTextFormField",
            description:
              "Please give an overview of the related corporate supply chain(s) and key business relationships (by procurement or order volume) (own operations)",
            label: "Related Corporate Supply Chain",
          },
          {
            showIf: (): boolean => true,
            name: "productCategories",
            component: "InputTextFormField",
            description: "Name their procurement categories (products, raw materials, services) (own operations)",
            label: "Product Categories",
          },
          {
            showIf: (): boolean => true,
            name: "definitionProductTypeService",
            component: "InputTextFormField",
            description: "Define the procured product types/services per category (own operations)",
            label: "Definition Product Type/Service",
          },
          {
            showIf: (): boolean => true,
            name: "sourcingCountryPerCategory",
            component: "InputTextFormField",
            description: "Name the sourcing countries per category (own operations)",
            label: "Sourcing Country per Category",
          },
          {
            showIf: (): boolean => true,
            name: "numberOfDirectSuppliers",
            component: "InputTextFormField",
            description: "State the number of direct suppliers per procurement category and country (own operations)",
            label: "Number of direct Suppliers",
          },
          {
            showIf: (): boolean => true,
            name: "orderVolumePerProcurement",
            component: "InputTextFormField",
            description:
              "State your order volume per procurement category in the last fiscal year (percentage of total volume) (own operations)",
            label: "Order Volume per Procurement",
          },
        ],
      },
    ],
  },
  {
    name: "governance",
    label: "Governance",
    color: "BLUE",
    subcategories: [
      {
        name: "riskManagementOwnOperations",
        label: "Risk management - Own Operations",
        fields: [
          {
            showIf: (): boolean => true,
            name: "adequateAndEffectiveRiskManagementSystem",
            component: "YesNoFormField",
            description: "Does your company have an adequate and effective Risk Management system?",
            label: "Adequate and Effective Risk Management System",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.adequateAndEffectiveRiskManagementSystem === "Yes",
            name: "riskManagementSystemFiscalYear",
            component: "YesNoFormField",
            description: "Did you perform a risk analysis as part of risk management in this fiscal year?",
            label: "Risk Management System Fiscal Year",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemFiscalYear === "Yes",
            name: "riskManagementSystemRisks",
            component: "YesNoFormField",
            description: "Were risks identified during this period?",
            label: "Risk Management System Risks",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemRisks === "Yes",
            name: "riskManagementSystemIdentifiedRisks",
            component: "InputTextFormField",
            description: "Which risks were specifically identified in the risk analysis?",
            label: "Risk Management System Identified Risks",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemRisks === "Yes",
            name: "riskManagementSystemCounteract",
            component: "YesNoFormField",
            description: "Have measures been defined to counteract these risks?",
            label: "Risk Management System Counteract",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemCounteract === "Yes",
            name: "riskManagementSystemMeasures",
            component: "InputTextFormField",
            description: "What measures have been applied to counteract the risks?",
            label: "Risk Management System Measures",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.adequateAndEffectiveRiskManagementSystem === "Yes",
            name: "riskManagementSystemResponsibility",
            component: "YesNoFormField",
            description:
              "Is the responsibility for the Risk Management in your company regulated, for example by appointing a human rights officer?",
            label: "Risk Management System Responsibility",
          },
          {
            showIf: (): boolean => true,
            name: "environmentalManagementSystem",
            component: "YesNoFormField",
            description: "Is an environmental management system implemented in your company?",
            label: "Environmental Management System",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.environmentalManagementSystem === "Yes",
            name: "environmentalManagementSystemInternationalCertification",
            component: "YesNoFormField",
            description: "Is the environmental management system internationally recognised and certified?",
            label: "Environmental Management System International Certification",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.environmentalManagementSystem === "Yes",
            name: "environmentalManagementSystemNationalCertification",
            component: "YesNoFormField",
            description: "Is the environmental management system nationally recognised and certified?",
            label: "Environmental Management System National Certification",
          },
        ],
      },
      {
        name: "grievanceMechanismOwnOperations",
        label: "Grievance mechanism - Own Operations",
        fields: [
          {
            showIf: (): boolean => true,
            name: "grievanceHandlingMechanism",
            component: "YesNoFormField",
            description:
              "Has your company implemented a grievance mechanism (e.g., anonymous whistleblowing system) to protect human and environmental rights in your business?",
            label: "Grievance Handling Mechanism",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            name: "grievanceHandlingMechanismUsedForReporting",
            component: "YesNoFormField",
            description:
              "Can all affected stakeholders and rights holders, i.e. both internal (e.g. employees) and external stakeholders (e.g. suppliers and their employees, NGOs) use the grievance channel/whistleblowing system for reporting?",
            label: "Grievance Handling Mechanism Used For Reporting",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            name: "grievanceMechanismInformationProvided",
            component: "YesNoFormField",
            description:
              "Is information about the process provided in a way that is adapted to the context and target groups?",
            label: "Grievance Mechanism Information Provided",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            name: "grievanceMechanismSupportProvided",
            component: "YesNoFormField",
            description: "Is the necessary support provided so that the target groups can actually use the procedure?",
            label: "Grievance Mechanism Support Provided",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            name: "grievanceMechanismAccessToExpertise",
            component: "YesNoFormField",
            description:
              "Do the target groups have access to expertise, advice and information, they need to participate in the grievance procedure in a fair, informed and respectful manner?",
            label: "Grievance Mechanism Access to Expertise",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            name: "grievanceMechanismComplaints",
            component: "YesNoFormField",
            description: "Have there been any complaints that have entered the system in the past?",
            label: "Grievance Mechanism Complaints",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaints === "Yes",
            name: "grievanceMechanismComplaintsNumber",
            component: "NumberFormField",
            description: "How many complaints have been received?",
            label: "Grievance Mechanism Complaints Number",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaints === "Yes",
            name: "grievanceMechanismComplaintsReason",
            component: "InputTextFormField",
            description: "What complaints have been received?",
            label: "Grievance Mechanism Complaints Reason",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaints === "Yes",
            name: "grievanceMechanismComplaintsAction",
            component: "YesNoFormField",
            description: "Have actions been taken to address the complaints?",
            label: "Grievance Mechanism Complaints Action",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaintsAction === "Yes",
            name: "grievanceMechanismComplaintsActionUndertaken",
            component: "InputTextFormField",
            description: "What actions have been taken?",
            label: "Grievance Mechanism Complaints Action undertaken",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            name: "grievanceMechanismPublicAccess",
            component: "YesNoFormField",
            description:
              "Does your company have publicly accessible rules of procedure for the complaints procedure that clearly describe the process for dealing with complaints?",
            label: "Grievance Mechanism Public Access",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            name: "grievanceMechanismProtection",
            component: "YesNoFormField",
            description: "Does the process effectively protect whistleblowers from disadvantage or punishment?",
            label: "Grievance Mechanism Protection",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            name: "grievanceMechanismDueDiligenceProcess",
            component: "YesNoFormField",
            description:
              "Do the findings from the processing of clues flow into the adjustment of your own due diligence processes?",
            label: "Grievance Mechanism Due Diligence Process",
          },
        ],
      },
      {
        name: "certificationsPoliciesAndResponsibilities",
        label: "Certifications, policies and responsibilities",
        fields: [
          {
            showIf: (): boolean => true,
            name: "sa8000Certification",
            component: "YesNoFormField",
            description:
              "Is your company SA8000 certified? If yes, please share the certificate with us. (Corporate Social Responsibility)",
            label: "SA8000 Certification",
          },
          {
            showIf: (): boolean => true,
            name: "smetaSocialAuditConcept",
            component: "YesNoFormField",
            description:
              "Does your company apply a social audit concept as defined by SMETA (Sedex Members Ethical Trade Audit)? (social audit)",
            label: "SMETA Social Audit Concept",
          },
          {
            showIf: (): boolean => true,
            name: "betterWorkProgramCertificate",
            component: "YesNoFormField",
            description:
              "Do the production sites where the goods are produced participate in the BetterWork program? If yes, please share the certificate with us. (private label only)",
            label: "Better Work Program Certificate",
          },
          {
            showIf: (): boolean => true,
            name: "iso45001Certification",
            component: "YesNoFormField",
            description:
              "Is your company ISO45001 certified? If yes, please share the certificate with us. (Management Systems of Occupational Health and Safety)",
            label: "ISO 45001 Certification",
          },
          {
            showIf: (): boolean => true,
            name: "iso14000Certification",
            component: "YesNoFormField",
            description: "Is your company ISO14000 certified? If yes, please share the certificate with us.",
            label: "ISO 14000 Certification",
          },
          {
            showIf: (): boolean => true,
            name: "emasCertification",
            component: "YesNoFormField",
            description:
              "Is your company certified according to EMAS? If yes, please share the certificate with us. (Voluntary environmental management)",
            label: "EMAS Certification",
          },
          {
            showIf: (): boolean => true,
            name: "iso37001Certification",
            component: "YesNoFormField",
            description:
              "Is your company ISO37001 certified? If yes, please share the certificate with us. (Anti-bribery management systems)",
            label: "ISO 37001 Certification",
          },
          {
            showIf: (): boolean => true,
            name: "iso37301Certification",
            component: "YesNoFormField",
            description:
              "Is your company ISO37301 certified? If yes, please share the certificate with us. (Compliance Management System)",
            label: "ISO37301 Certification",
          },
          {
            showIf: (): boolean => true,
            name: "riskManagementSystemCertification",
            component: "YesNoFormField",
            description: "Is the Risk Management System internationally recognized and certified? (e.g.: ISO 31000)",
            label: "Risk Management System Certification",
          },
          {
            showIf: (): boolean => true,
            name: "amforiBsciAuditReport",
            component: "YesNoFormField",
            description:
              "Does your company have a current amfori BSCI audit report? If yes, please share the certificate with us.",
            label: "amfori BSCI Audit Report",
          },
          {
            showIf: (): boolean => true,
            name: "responsibleBusinessAssociationCertification",
            component: "YesNoFormField",
            description:
              "Is your company Responsible Business Association (RBA) certified? If yes, please share the certificate with us. (Social Responsibility)",
            label: "Responsible Business Association Certification",
          },
          {
            showIf: (): boolean => true,
            name: "fairLaborAssociationCertification",
            component: "YesNoFormField",
            description:
              "Is your company Fair Labor Association (FLA) certified? If yes, please share the certificate with us. (Adherence to international and national labor laws)",
            label: "Fair Labor Association Certification",
          },
          {
            showIf: (): boolean => true,
            name: "additionalAudits",
            component: "InputTextFormField",
            description:
              "Please list other (sector-specific) audits (if available) to which your company is certified.",
            label: "Additional Audits",
          },
          {
            showIf: (): boolean => true,
            name: "codeOfConduct",
            component: "YesNoFormField",
            description:
              "Has your company implemented and enforced (e.g., within the Code Of Conducts) internal behavioural guidelines that address the issues of human rights protection and respect for the environment?",
            label: "Code Of Conduct",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.certificationsPoliciesAndResponsibilities?.codeOfConduct === "Yes",
            name: "codeOfConductTraining",
            component: "YesNoFormField",
            description:
              "Are your employees regularly made aware of the internal rules of conduct and trained on them?",
            label: "Code Of Conduct Training",
          },
          {
            showIf: (): boolean => true,
            name: "supplierCodeOfConduct",
            component: "YesNoFormField",
            description:
              "Does your company have a Supplier Code Of Conduct? (If yes, please share the Supplier Code of Conduct with us)",
            label: "Supplier Code Of Conduct",
          },
          {
            showIf: (): boolean => true,
            name: "policyStatement",
            component: "YesNoFormField",
            description: "Does your company have a policy statement on its human rights strategy?",
            label: "Policy Statement",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.certificationsPoliciesAndResponsibilities?.policyStatement === "Yes",
            name: "humanRightsStrategy",
            component: "InputTextFormField",
            description:
              "In which relevant departments/business processes has the anchoring of the human rights strategy been ensured",
            label: "Human Rights Strategy",
          },
          {
            showIf: (): boolean => true,
            name: "environmentalImpactPolicy",
            component: "YesNoFormField",
            description:
              "Does your company have a Environmental Impact Policy? (If yes, please share the policy with us)",
            label: "Environmental Impact Policy",
          },
          {
            showIf: (): boolean => true,
            name: "fairWorkingConditionsPolicy",
            component: "YesNoFormField",
            description:
              "Does your company have a Fair Working Conditions Policy? (If yes, please share the policy with us)",
            label: "Fair Working Conditions Policy",
          },
        ],
      },
      {
        name: "generalViolations",
        label: "General violations",
        fields: [
          {
            showIf: (): boolean => true,
            name: "responsibilitiesForFairWorkingConditions",
            component: "YesNoFormField",
            description:
              "Has your company established, according to the nature and extent of the enterprise’s business activities, official responsibilities for the topic of fair working conditions?",
            label: "Responsibilities For Fair Working Conditions",
          },
          {
            showIf: (): boolean => true,
            name: "responsibilitiesForTheEnvironment",
            component: "YesNoFormField",
            description:
              "Has your company established, according to the nature and extent of the enterprise’s business activities, official responsibilities for the topic of the environment?",
            label: "Responsibilities For The Environment",
          },
          {
            showIf: (): boolean => true,
            name: "responsibilitiesForOccupationalSafety",
            component: "YesNoFormField",
            description:
              "Has your company established, according to the nature and extent of the enterprise’s business activities, official responsibilities for the topic of occupational safety?",
            label: "Responsibilities For Occupational Safety",
          },
          {
            showIf: (): boolean => true,
            name: "legalProceedings",
            component: "YesNoFormField",
            description:
              "Has your company been involved in the last 5 years, in legal disputes (including currently ongoing disputes) with third parties regarding human rights and environmental violations?",
            label: "Legal Proceedings",
          },
          {
            showIf: (): boolean => true,
            name: "humanRightsViolation",
            component: "YesNoFormField",
            description:
              "Have there been any violations of human rights or environmental aspects on your part in the last 5 years?",
            label: "Human Rights Violation",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.humanRightsViolation === "Yes",
            name: "humanRightsViolationLocation",
            component: "InputTextFormField",
            description: "What were the violations?",
            label: "Human Rights Violation Location",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.humanRightsViolation === "Yes",
            name: "humanRightsViolationAction",
            component: "YesNoFormField",
            description: "Has action been taken to address the violations?",
            label: "Human Rights Violation Action",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.humanRightsViolationAction === "Yes",
            name: "humanRightsViolationActionMeasures",
            component: "InputTextFormField",
            description: "What measures have been taken?",
            label: "Human Rights Violation Action Measures",
          },
          {
            showIf: (): boolean => true,
            name: "highRiskCountriesRawMaterials",
            component: "YesNoFormField",
            description: "Do you source your raw materials from verified conflict or high-risk regions?",
            label: "High Risk Countries Raw Materials",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.highRiskCountriesRawMaterials === "Yes",
            name: "highRiskCountriesRawMaterialsLocation",
            component: "MultiSelectFormField",
            description: "From which conflict/high-risk regions do you source your raw materials?",
            label: "High Risk Countries Raw Materials Location",
          },
          {
            showIf: (): boolean => true,
            name: "highRiskCountriesActivity",
            component: "YesNoFormField",
            description:
              "Does your company have activities in countries where there are high risks for human rights and/or the environment?",
            label: "High Risk Countries Activity",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.highRiskCountriesActivity === "Yes",
            name: "highRiskCountries",
            component: "MultiSelectFormField",
            description: "In which high risk countries does your company have activities?",
            label: "High Risk Countries",
          },
          {
            showIf: (): boolean => true,
            name: "highRiskCountriesProcurement",
            component: "YesNoFormField",
            description:
              "Does your company procure from countries with high risks for human rights and/or the environment?",
            label: "High Risk Countries Procurement",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.highRiskCountriesProcurement === "Yes",
            name: "highRiskCountriesProcurementName",
            component: "MultiSelectFormField",
            description: "From which high risk countries does your company procure from?",
            label: "High Risk Countries Procurement Name",
          },
        ],
      },
    ],
  },
  {
    name: "social",
    label: "Social",
    color: "BLUE",
    subcategories: [
      {
        name: "childLabor",
        label: "Child labor",
        fields: [
          {
            showIf: (): boolean => true,
            name: "employeeUnder18",
            component: "YesNoFormField",
            description: "Does your company have employees under the age of 18?",
            label: "Employee Under 18",
          },
          {
            showIf: (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.employeeUnder18 === "Yes",
            name: "employeeUnder18Under15",
            component: "YesNoFormField",
            description:
              "With regard to the place of employment and the applicable laws: do you employ school-age children or children under the age of 15 on a full-time basis?",
            label: "Employee Under 18 Under 15",
          },
          {
            showIf: (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.employeeUnder18 === "Yes",
            name: "employeeUnder18Apprentices",
            component: "YesNoFormField",
            description:
              "Are the employees under 18 years of age exclusively apprentices within the meaning of the locally applicable laws?",
            label: "Employee Under 18 Apprentices",
          },
          {
            showIf: (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.employeeUnder18 === "Yes",
            name: "worstFormsOfChildLabor",
            component: "YesNoFormField",
            description:
              "Is the prohibition of the worst forms of child labor ensured in your company? This includes: all forms of slavery or practices similar to slavery, the use, procuring or offering of a child for prostitution, the production of pornography or pornographic performances, the use, procuring or offering of a child for illicit activities, in particular for the production or trafficking of drugs, work which, by its nature or the circumstances in which it is performed, is likely to be harmful to the health, safety, or morals of children",
            label: "Worst Forms of Child Labor",
          },
          {
            showIf: (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.worstFormsOfChildLabor === "Yes",
            name: "worstFormsOfChildLaborForms",
            component: "InputTextFormField",
            description: "What forms of worst forms of child labor have been identified?",
            label: "Worst Forms of Child Labor Forms",
          },
          {
            showIf: (): boolean => true,
            name: "employmentUnderLocalMinimumAgePrevention",
            component: "YesNoFormField",
            description:
              "Does your company take measures to prevent the employment of children under the local minimum age?",
            label: "Employment Under Local Minimum Age Prevention",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            name: "employmentUnderLocalMinimumAgePreventionEmploymentContracts",
            component: "YesNoFormField",
            description:
              "Is a formal recruitment process including the conclusion of employment contracts such a measure?",
            label: "Employment Under Local Minimum Age Prevention Employment Contracts",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            name: "employmentUnderLocalMinimumAgePreventionJobDescription",
            component: "YesNoFormField",
            description:
              "Is a clear job description for employees under the local minimum age in the hiring process and employment contracts such a measure? (group of people between 15 and 18 years)",
            label: "Employment Under Local Minimum Age Prevention Job Description",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            name: "employmentUnderLocalMinimumAgePreventionIdentityDocuments",
            component: "YesNoFormField",
            description:
              "Is the control of official documents (e.g. identity documents and certificates) such a measure?",
            label: "Employment Under Local Minimum Age Prevention Identity Documents",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            name: "employmentUnderLocalMinimumAgePreventionTraining",
            component: "YesNoFormField",
            description:
              "Is raising the awareness of staff involved in the recruitment process through training such a measure?",
            label: "Employment Under Local Minimum Age Prevention Training",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            name: "employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge",
            component: "YesNoFormField",
            description: "Is the regular checking of the legal minimum age such a measure?",
            label: "Employment Under Local Minimum Age Prevention Checking Of Legal Minimum Age",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            name: "childLaborMeasures",
            component: "InputTextFormField",
            description:
              "Please list any other measures (if available) you take to prevent the employment of children under the locally applicable minimum age?",
            label: "Child Labor Measures",
          },
          {
            showIf: (): boolean => true,
            name: "childLaborPolicy",
            component: "YesNoFormField",
            description: "Does your company have a Child Labor Policy? (If yes, please share the policy with us)",
            label: "Child Labor Policy",
          },
        ],
      },
      {
        name: "forcedLaborSlavery",
        label: "Forced labor, slavery",
        fields: [
          {
            showIf: (): boolean => true,
            name: "forcedLaborAndSlaveryPrevention",
            component: "YesNoFormField",
            description:
              "Does your company have practices that lead or may lead to forced labor and/or slavery?  The following are included: Creating unacceptable working and living conditions by working in hazardous conditions or in unacceptable accommodations provided by the employer; Excessive levels of overtime; Use of intimidation, threats, and/or punishment; Other types of forced labor (e.g. debt bondage, human trafficking)",
            label: "Forced Labor And Slavery Prevention",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPrevention === "Yes",
            name: "forcedLaborAndSlaveryPreventionPractices",
            component: "InputTextFormField",
            description: "Please specify which of the points exist?",
            label: "Forced Labor And Slavery Prevention Practices",
          },
          {
            showIf: (): boolean => true,
            name: "forcedLaborAndSlaveryPreventionMeasures",
            component: "YesNoFormField",
            description: "Does your company take measures to prevent forced labor and slavery?",
            label: "Forced Labor And Slavery Prevention Measures",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            name: "forcedLaborAndSlaveryPreventionEmploymentContracts",
            component: "YesNoFormField",
            description:
              "Is a formal hiring process, including employment contracts in the employee's local language, with appropriate wage and termination clauses, such a measure?",
            label: "Forced Labor And Slavery Prevention Employment Contracts",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            name: "forcedLaborAndSlaveryPreventionIdentityDocuments",
            component: "YesNoFormField",
            description: "Is a ban on the retention of identity documents such a measure?",
            label: "Forced Labor And Slavery Prevention Identity Documents",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            name: "forcedLaborAndSlaveryPreventionFreeMovement",
            component: "YesNoFormField",
            description:
              "Is ensuring the free movement of employees through doors and windows that can be opened to leave the building/premises of your company at any time such a measure?",
            label: "Forced Labor And Slavery Prevention Free Movement",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            name: "forcedLaborAndSlaveryPreventionProvisionSocialRoomsAndToilets",
            component: "YesNoFormField",
            description: "Is the provision of social rooms and toilets that can be visited at any time such a measure?",
            label: "Forced Labor And Slavery Prevention Provision Social Rooms and Toilets",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            name: "forcedLaborAndSlaveryPreventionTraining",
            component: "YesNoFormField",
            description:
              "Is raising the awareness of staff involved in the recruitment process through training such a measure?",
            label: "Forced Labor And Slavery Prevention Training",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            name: "forcedLaborAndSlaveryMeasures",
            component: "InputTextFormField",
            description: "Please list other measures (if available) you take to prevent forced labor and slavery?",
            label: "Forced Labor and Slavery Measures",
          },
          {
            showIf: (): boolean => true,
            name: "forcedLaborPolicy",
            component: "YesNoFormField",
            description: "Does your company have a Forced Labor Policy? (If yes, please share the policy with us)",
            label: "Forced Labor Policy",
          },
        ],
      },
      {
        name: "withholdingAdequateWages",
        label: "Withholding adequate wages",
        fields: [
          {
            showIf: (): boolean => true,
            name: "adequatWage",
            component: "YesNoFormField",
            description: "Is your company currently withholding adequate wages (adequate in the sense of local laws)?",
            label: "Adequat Wage",
          },
          {
            showIf: (): boolean => true,
            name: "adequatWageBeingWithheld",
            component: "YesNoFormField",
            description: "Are any measures taken in your company to prevent that adequate wages being withheld?",
            label: "Adequat Wage being withheld",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequatWageBeingWithheld === "Yes",
            name: "documentedWorkingHoursAndWages",
            component: "YesNoFormField",
            description: "Does your company document the working hours and wages of its employees?",
            label: "Documented Working Hours And Wages",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequatWageBeingWithheld === "Yes",
            name: "adequateLivingWage",
            component: "YesNoFormField",
            description:
              "Does your company pay employees a reasonable wage? (the appropriate wage is at least the minimum wage set by the applicable law and is otherwise measured according to the law of the place of employment).",
            label: "Adequate Living Wage",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequatWageBeingWithheld === "Yes",
            name: "regularWagesProcessFlow",
            component: "YesNoFormField",
            description:
              "Has your company implemented the payment of wages through standardised and regular process flows?",
            label: "Regular Wages Process Flow",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequatWageBeingWithheld === "Yes",
            name: "fixedHourlyWages",
            component: "YesNoNaFormField",
            description: "Do fixed hourly wages exist in your company?",
            label: "Fixed Hourly Wages",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequatWageBeingWithheld === "Yes",
            name: "fixedPieceworkWages",
            component: "YesNoNaFormField",
            description: "Does your company have fixed piecework wages?",
            label: "Fixed Piecework Wages",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequatWageBeingWithheld === "Yes",
            name: "adequateWageMeasures",
            component: "InputTextFormField",
            description:
              "Please list other measures (if available) you take to prevent the withholding adequate wages?",
            label: "Adequate Wage Measures",
          },
        ],
      },
      {
        name: "disregardForOccupationalHealthSafety",
        label: "Disregard for occupational health/safety",
        fields: [
          {
            showIf: (): boolean => true,
            name: "lowSkillWork",
            component: "YesNoFormField",
            description: "Do your employees perform low-skill manual work or repetitive manual work?",
            label: "Low Skill Work",
          },
          {
            showIf: (): boolean => true,
            name: "hazardousMachines",
            component: "YesNoFormField",
            description: "Are hazardous machines used in the manufacture of (preliminary) products?",
            label: "Hazardous Machines",
          },
          {
            showIf: (): boolean => true,
            name: "oshPolicy",
            component: "YesNoFormField",
            description:
              "Has your company implemented and enforced a formal occupational health and safety (OSH) policy that complies with local laws, industry requirements and international standards?",
            label: "OSH Policy",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyPersonalProtectiveEquipment",
            component: "YesNoNaFormField",
            description: "Is the subject area of personal protective equipment addressed by this OSH Directive?",
            label: "OSH Policy Personal Protective Equipment",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyMachineSafety",
            component: "YesNoNaFormField",
            description: "Is the subject area of machine safety addressed by this OSH Directive?",
            label: "OSH Policy Machine Safety",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyDisasterBehaviouralResponse",
            component: "YesNoFormField",
            description: "Is the subject area of behaviour in the event of a disaster addressed by this OSH Directive?",
            label: "OSH Policy Disaster Behavioural Response",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyAccidentsBehaviouralResponse",
            component: "YesNoFormField",
            description:
              "Is the subject area of behaviour in the event of and prevention of accidents addressed by this OSH Directive?",
            label: "OSH Policy Accidents Behavioural Response",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyWorkplaceErgonomics",
            component: "YesNoFormField",
            description: "Is the subject area of workplace ergonomics addressed by this OSH Directive?",
            label: "OSH Policy Workplace Ergonomics",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyAccessToWork",
            component: "YesNoFormField",
            description: "Is access to the work secluded/is the workplace difficult to access?",
            label: "OSH Policy Access to work",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyHandlingChemicalsAndOtherHazardousSubstances",
            component: "YesNoNaFormField",
            description:
              "Is the subject area of handling chemical, physical or biological substances addressed by this OSH Directive?",
            label: "OSH Policy Handling Chemicals And Other Hazardous Substances",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyFireProtection",
            component: "YesNoFormField",
            description: "Is the subject area of fire protection addressed by this OSH Directive?",
            label: "OSH Policy Fire Protection",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyWorkingHours",
            component: "YesNoFormField",
            description:
              "Is the subject area of regulation of working hours, overtime and rest breaks be addressed by this OSH Directive?",
            label: "OSH Policy Working Hours",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyTrainingAddressed",
            component: "YesNoFormField",
            description:
              "Is the subject area of training and instruction of employees with regard to occupational health and safety addressed by this OSH Directive?",
            label: "OSH Policy Training Addressed",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyTraining",
            component: "YesNoFormField",
            description: "Are your employees regularly made aware of the OSH Directive and trained on them?",
            label: "OSH Policy Training",
          },
          {
            showIf: (): boolean => true,
            name: "oshManagementSystem",
            component: "YesNoFormField",
            description: "Is an occupational health and safety management system implemented in your company?",
            label: "OSH Management System",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshManagementSystem === "Yes",
            name: "oshManagementSystemInternationalCertification",
            component: "YesNoFormField",
            description: "Is the OSH management system internationally recognised and certified?",
            label: "OSH Management System International Certification",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshManagementSystem === "Yes",
            name: "oshManagementSystemNationalCertification",
            component: "YesNoFormField",
            description: "Is the OSH management system nationally recognised and certified?",
            label: "OSH Management System National Certification",
          },
          {
            showIf: (): boolean => true,
            name: "workplaceAccidentsUnder10",
            component: "YesNoFormField",
            description:
              "Is the number of incidents in which employees suffered work-related injuries with serious consequences less than 10 in the past fiscal year?",
            label: "Workplace Accidents Under 10",
          },
          {
            showIf: (): boolean => true,
            name: "oshTraining",
            component: "YesNoFormField",
            description:
              "Has your company introduced mandatory offers and training for employees to improve occupational safety?",
            label: "OSH Training",
          },
          {
            showIf: (): boolean => true,
            name: "healthAndSafetyPolicy",
            component: "YesNoFormField",
            description: "Does your company have a Health And Safety Policy? (If yes, please share the policy with us)",
            label: "Health And Safety Policy",
          },
        ],
      },
      {
        name: "disregardForFreedomOfAssociation",
        label: "Disregard for freedom of association",
        fields: [
          {
            showIf: (): boolean => true,
            name: "freedomOfAssociation",
            component: "YesNoFormField",
            description: "Does your company ensure that employees are free to form or join trade unions?",
            label: "Freedom Of Association",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForFreedomOfAssociation?.freedomOfAssociation === "Yes",
            name: "representedEmployees",
            component: "NumberFormField",
            description: "What is your percentage of employees who are represented by trade unions?",
            label: "Represented Employees",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForFreedomOfAssociation?.freedomOfAssociation === "Yes",
            name: "discriminationForTradeUnionMembers",
            component: "YesNoFormField",
            description:
              "Does your company ensure that no discrimination is practised or other consequences taken against employees in the event of the formation, joining and membership of a trade union?",
            label: "Discrimination For Trade Union Members",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForFreedomOfAssociation?.freedomOfAssociation === "Yes",
            name: "freedomOfOperationForTradeUnion",
            component: "YesNoFormField",
            description:
              "Does your company ensure that trade unions are free to operate in accordance with the law of the place of employment?",
            label: "Freedom Of Operation For Trade Union",
          },
          {
            showIf: (): boolean => true,
            name: "freedomOfAssociationTraining",
            component: "YesNoFormField",
            description:
              "Do employees receive information about their rights as part of training and/or intranet, notices or company brochures?",
            label: "Freedom Of Association Training",
          },
          {
            showIf: (): boolean => true,
            name: "worksCouncil",
            component: "YesNoFormField",
            description:
              "Does your company have a works council or an employee representative committee (if it can be set up in your company in accordance with local applicable legal provisions)?",
            label: "Works Council",
          },
        ],
      },
      {
        name: "unequalTreatmentOfEmployment",
        label: "Unequal treatment of employment",
        fields: [
          {
            showIf: (): boolean => true,
            name: "umequalTreatmentOfEmployment",
            component: "YesNoFormField",
            description:
              "Does your company treat employees unequally because of national/ethnic origin, social origin, health status, disability, sexual orientation, age, gender, political opinion, religion or belief?",
            label: "Umequal Treatment of Employment",
          },
          {
            showIf: (): boolean => true,
            name: "diversityAndInclusionRole",
            component: "YesNoFormField",
            description:
              "Is a member of your company's management responsible for promoting diversity in the workforce and among business partners?",
            label: "Diversity And Inclusion Role",
          },
          {
            showIf: (): boolean => true,
            name: "preventionOfMistreatments",
            component: "YesNoFormField",
            description:
              "Does your company's management promote a work environment free from physical, sexual, mental and verbal abuse, threats or other forms of mistreatment? (e.g. diversity program)",
            label: "Prevention Of Mistreatments",
          },
          {
            showIf: (): boolean => true,
            name: "equalOpportunitiesOfficer",
            component: "YesNoFormField",
            description: "Is an equal opportunities officer (or similar function) implemented in your company?",
            label: "Equal Opportunities Officer",
          },
          {
            showIf: (): boolean => true,
            name: "fairAndEthicalRecruitmentPolicy",
            component: "YesNoFormField",
            description:
              "Does your company have a Fair And Ethical Recruitment Policy? (If yes, please share the policy with us)",
            label: "Fair And Ethical Recruitment Policy",
          },
          {
            showIf: (): boolean => true,
            name: "equalOpportunitiesAndNonDiscriminationPolicy",
            component: "YesNoFormField",
            description:
              "Does your company have a Equal Opportunities And Non-discrimination Policy? (If yes, please share the policy with us)",
            label: "Equal Opportunities And Non-discrimination Policy",
          },
        ],
      },
      {
        name: "contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption",
        label: "Contamination of soil/water/air, noise emissions, excessive water consumption",
        fields: [
          {
            showIf: (): boolean => true,
            name: "harmfulSoilChange",
            component: "YesNoFormField",
            description: "Is there a risk of causing a harmful soil change in your company?",
            label: "Harmful Soil Change",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            name: "soilDegradation",
            component: "YesNoFormField",
            description:
              "Does your company have measures in place to prevent the degradation of soil structure caused by the use of heavy machinery?",
            label: "Soil Degradation",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            name: "soilErosion",
            component: "YesNoFormField",
            description:
              "Does your company have measures in place to prevent soil erosion caused by deforestation or overgrazing?",
            label: "Soil Erosion",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            name: "soilBornDiseases",
            component: "YesNoFormField",
            description:
              "Does your company have measures in place to prevent the development of soil-borne diseases and pests and to maintain soil fertility?",
            label: "Soil Born Diseases",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            name: "soilContamination",
            component: "YesNoFormField",
            description:
              "Does your company have measures in place to prevent soil contamination by antibiotics and toxins?",
            label: "Soil Contamination",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            name: "soilSalinisation",
            component: "YesNoFormField",
            description: "Does your company have measures in place to prevent soil salinisation?",
            label: "Soil Salinisation",
          },
          {
            showIf: (): boolean => true,
            name: "harmfulWaterPollution",
            component: "YesNoFormField",
            description: "Is there a risk of harmful water pollution in your company?",
            label: "Harmful Water Pollution",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulWaterPollution === "Yes",
            name: "fertilisersOrPollutants",
            component: "YesNoFormField",
            description: "Does your company use fertilisers or pollutants such as chemicals or heavy metals?",
            label: "Fertilisers Or Pollutants",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulWaterPollution === "Yes",
            name: "wasteWaterFiltration",
            component: "YesNoFormField",
            description: "Does your company have filtration systems for the waste water?",
            label: "Waste Water Filtration",
          },
          {
            showIf: (): boolean => true,
            name: "harmfulAirPollution",
            component: "YesNoFormField",
            description: "Is there a risk of harmful air pollution in your company?",
            label: "Harmful Air Pollution",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulAirPollution === "Yes",
            name: "airFiltration",
            component: "YesNoFormField",
            description: "Does your company have air filtration systems?",
            label: "Air Filtration",
          },
          {
            showIf: (): boolean => true,
            name: "harmfulNoiseEmission",
            component: "YesNoFormField",
            description: "Is there a risk of harmful noise emission in your company?",
            label: "Harmful Noise Emission",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulNoiseEmission === "Yes",
            name: "reduceNoiseEmissions",
            component: "YesNoFormField",
            description: "Has your company implemented structural measures to reduce noise emissions?",
            label: "Reduce Noise Emissions",
          },
          {
            showIf: (): boolean => true,
            name: "excessiveWaterConsumption",
            component: "YesNoFormField",
            description: "Is there a risk of excessive water consumption in your company?",
            label: "Excessive Water Consumption",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.excessiveWaterConsumption === "Yes",
            name: "waterSavingMeasures",
            component: "YesNoFormField",
            description: "Do you take water-saving measures in your companies?",
            label: "Water Saving Measures",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.waterSavingMeasures === "Yes",
            name: "waterSavingMeasuresName",
            component: "InputTextFormField",
            description: "If yes, which ones?",
            label: "Water Saving Measures Name",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.excessiveWaterConsumption === "Yes",
            name: "pipeMaintaining",
            component: "YesNoFormField",
            description: "Are water pipes regularly checked and maintained?",
            label: "Pipe Maintaining",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.excessiveWaterConsumption === "Yes",
            name: "waterSources",
            component: "YesNoFormField",
            description:
              "Does your company use water sources that are important for the local population or local agriculture?",
            label: "Water Sources",
          },
          {
            showIf: (): boolean => true,
            name: "contaminationMeasures",
            component: "InputTextFormField",
            description:
              "Please list any other measures (if available) you are taking to prevent the risk of harmful soil change, water pollution, air pollution, harmful noise emission or excessive water consumption that: O significantly affects the natural basis for food preservation and production O denies a person access to safe drinking water O impedes or destroys a person's access to sanitary facilities, or O harms the health of any person",
            label: "Contamination Measures",
          },
        ],
      },
      {
        name: "unlawfulEvictionDeprivationOfLandForestAndWater",
        label: "Unlawful eviction/deprivation of land, forest and water",
        fields: [
          {
            showIf: (): boolean => true,
            name: "unlawfulEvictionAndTakingOfLand",
            component: "YesNoFormField",
            description:
              "Is your company as a result of the acquisition, development and/or other use of land, forests and/or bodies of water, the use of which secures a person's livelihood at risk of O an unlawful eviction O carrying out an unlawful taking of land, forests and/or water?",
            label: "Unlawful Eviction And Taking Of Land",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.unlawfulEvictionDeprivationOfLandForestAndWater?.unlawfulEvictionAndTakingOfLand ===
              "Yes",
            name: "unlawfulEvictionAndTakingOfLandRisk",
            component: "InputTextFormField",
            description: "If so, what exactly is the risk?",
            label: "Unlawful Eviction And Taking Of Land Risk",
          },
          {
            showIf: (): boolean => true,
            name: "unlawfulEvictionAndTakingOfLandStrategies",
            component: "YesNoFormField",
            description:
              "Has your company developed and implemented strategies that avoid, reduce, mitigate or remedy direct and indirect negative impacts on the land and natural resources of indigenous peoples and local communities?",
            label: "Unlawful Eviction And Taking Of Land Strategies",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.unlawfulEvictionDeprivationOfLandForestAndWater
                ?.unlawfulEvictionAndTakingOfLandStrategies === "Yes",
            name: "unlawfulEvictionAndTakingOfLandStrategiesName",
            component: "InputTextFormField",
            description: "If yes, which ones?",
            label: "Unlawful Eviction And Taking Of Land Strategies Name",
          },
          {
            showIf: (): boolean => true,
            name: "voluntaryGuidelinesOnTheResponsibleGovernanceOfTenure",
            component: "YesNoFormField",
            description:
              "Have you implemented the Voluntary Guidelines on the Responsible Governance of Tenure in your company?",
            label: "Voluntary Guidelines on the Responsible Governance of Tenure",
          },
        ],
      },
      {
        name: "useOfPrivatePublicSecurityForcesWithDisregardForHumanRights",
        label: "Use of private/public security forces with disregard for human rights",
        fields: [
          {
            showIf: (): boolean => true,
            name: "useOfPrivatePublicSecurityForces",
            component: "YesNoFormField",
            description:
              "Does your company use private and/or public security forces to protect company projects or similar?",
            label: "Use Of Private Public Security Forces",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForces === "Yes",
            name: "useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights",
            component: "YesNoFormField",
            description:
              "Does your company have measures in place to prevent security forces during an operation from O violate the prohibition of torture and/or cruel, inhuman and/or degrading treatment O damages life or limb O impairs the right to organize and the freedom of association?",
            label: "Use Of Private Public Security Forces And Risk Of Violation Of Human Rights",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
            name: "instructionOfSecurityForces",
            component: "YesNoFormField",
            description: "Is an adequate instruction of the security forces such a measure?",
            label: "Instruction Of Security Forces",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
            name: "humanRightsTraining",
            component: "YesNoFormField",
            description: "Is training on human rights such a measure?",
            label: "Human Rights Training",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
            name: "stateSecurityForces",
            component: "YesNoNaFormField",
            description:
              "(Only in the case of state security forces) Before the security forces were commissioned, was it checked whether serious human rights violations by these units had already been documented?",
            label: "State Security Forces",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
            name: "privateSecurityForces",
            component: "YesNoNaFormField",
            description:
              "(Only in the case of private security forces) Have the contractual relationships with the security guards been designed in such a way that they comply with the applicable legal framework?",
            label: "Private Security Forces",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
            name: "useOfPrivatePublicSecurityForcesMeasures",
            component: "InputTextFormField",
            description:
              "Please list any other measures (if available) you are taking to prevent the use of private/public security forces in violation of human rights?",
            label: "Use Of Private Public Security Forces Measures",
          },
        ],
      },
    ],
  },
  {
    name: "environmental",
    label: "Environmental",
    color: "BLUE",
    subcategories: [
      {
        name: "useOfMercuryMercuryWasteMinamataConvention",
        label: "Use of mercury, mercury waste (Minamata Convention)",
        fields: [
          {
            showIf: (): boolean => true,
            name: "mercuryAndMercuryWasteHandling",
            component: "YesNoFormField",
            description: "Does your company deal with mercury and mercury waste as part of its business model?",
            label: "Mercury And Mercury Waste Handling",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAndMercuryWasteHandling ===
              "Yes",
            name: "mercuryAndMercuryWasteHandlingPolicy",
            component: "YesNoFormField",
            description:
              "Does your company have a policy for handling these materials? (If yes, please share the policy with us)",
            label: "Mercury And Mercury Waste Handling Policy",
          },
          {
            showIf: (): boolean => true,
            name: "mercuryAddedProductsHandling",
            component: "YesNoFormField",
            description:
              "Are you involved in the manufacture, use, treatment, and/or import or export of products containing mercury?",
            label: "Mercury Added-Products Handling",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling ===
              "Yes",
            name: "mercuryAddedProductsHandlingRiskOfExposure",
            component: "YesNoFormField",
            description:
              "Is there a risk of manufacturing, importing or exporting products containing mercury that are not subject to the exemption under Annex A Part 1 of the Minamata Convention (BGBI. 2017 II p.610, 611)?",
            label: "Mercury Added-Products Handling Risk Of Exposure",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling ===
              "Yes",
            name: "mercuryAddedProductsHandlingRiskOfDisposal",
            component: "YesNoFormField",
            description:
              "If there are products that are only contaminated with mercury: Is there a risk within your company that mercury waste will be disposed of contrary to the provisions of Article 11 of the Minamata Agreement (BGBI. 2017 II p. 610, 611)?",
            label: "Mercury Added-Products Handling Risk Of Disposal",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling ===
              "Yes",
            name: "mercuryAndMercuryCompoundsProductionAndUse",
            component: "YesNoFormField",
            description: "Are there manufacturing processes in your company that use mercury and/or mercury compounds?",
            label: "Mercury and Mercury Compounds Production and Use",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling ===
              "Yes",
            name: "mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure",
            component: "YesNoFormField",
            description:
              "Is there a risk in your company that mercury and/or mercury compounds used in manufacturing processes, that are regulated according to Article 5 Paragraph 2 and Annex B of the Minamata Agreement (Federal Law Gazette 2017 II p. 610, 611), have already exceeded the specified phase-out date and are therefore prohibited?",
            label: "Mercury And Mercury Compounds Production And Use Risk Of Exposure",
          },
        ],
      },
      {
        name: "productionAndUseOfPersistentOrganicPollutantsPopsConvention",
        label: "Production and use of persistent organic pollutants (POPs Convention)",
        fields: [
          {
            showIf: (): boolean => true,
            name: "persistentOrganicPollutantsProductionAndUse",
            component: "YesNoFormField",
            description:
              "Do you use and/or produce persistent organic pollutants (POPs), i.e. chemical compounds that break down and/or transform very slowly in the environment?",
            label: "Persistent Organic Pollutants Production and Use",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse === "Yes",
            name: "persistentOrganicPollutantsUsed",
            component: "InputTextFormField",
            description: "If yes, which organic pollutants are used?",
            label: "Persistent Organic Pollutants Used",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse === "Yes",
            name: "persistentOrganicPollutantsProductionAndUseRiskOfExposure",
            component: "YesNoFormField",
            description:
              "Is there a risk in your company that these organic pollutants fall under Article 3 paragraph 1 letter a and Annex A of the Stockholm Convention of 23 May 2001 on persistent organic pollutants (Federal Law Gazette 2002 II p. 803, 804) (POPs Convention) and are therefore banned?",
            label: "Persistent Organic Pollutants Production And Use Risk Of Exposure",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse === "Yes",
            name: "persistentOrganicPollutantsProductionAndUseRiskOfDisposal",
            component: "YesNoFormField",
            description:
              "In relation to the waste of these pollutants, is there a risk that they will be subject to the rules laid down in the applicable legal system in accordance with the provisions of Article 6(1)(d)(i) and (ii) of the POP -Convention (BGBI. 2002 II p. 803, 804) and will O not be handled / collected / stored / transported in an environmentally sound manner O not be disposed of in an environmentally friendly manner, i.e. if possible disposed of in such a way that the persistent organic pollutants contained therein are destroyed or irreversibly converted?",
            label: "Persistent Organic Pollutants Production And Use Risk Of Disposal",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse === "Yes",
            name: "legalRestrictedWasteProcesses",
            component: "YesNoFormField",
            description:
              "Does your company have processes or measures in place to ensure the lawful handling of (hazardous) waste?",
            label: "Legal Restricted Waste Processes",
          },
        ],
      },
      {
        name: "exportImportOfHazardousWasteBaselConvention",
        label: "Export/import of hazardous waste (Basel Convention)",
        fields: [
          {
            showIf: (): boolean => true,
            name: "persistentOrganicPollutantsProductionAndUseTransboundaryMovements",
            component: "YesNoFormField",
            description:
              "Is there a risk in your company that: hazardous waste within the meaning of the Basel Convention (Article 1 Paragraph 1, BGBI. 1994 II p. 2703, 2704) or other wastes that require special consideration (household waste, residues from incineration of household waste) (Article 1(2)) are transported across borders?",
            label: "Persistent Organic Pollutants Production And Use Transboundary Movements",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention
                ?.persistentOrganicPollutantsProductionAndUseTransboundaryMovements === "Yes",
            name: "persistentOrganicPollutantsProductionAndUseRiskForImportingState",
            component: "YesNoFormField",
            description:
              "Are these wastes transported or shipped to an importing State that is subject to the Basel Convention and O has not given its written consent to the specific import (if that importing State has not prohibited the importation of that hazardous waste) (Article 4(1)(c)) O is not a contracting party (Article 4, paragraph 5) O does not treat waste in an environmentally friendly manner because it does not have the appropriate capacity for environmentally friendly disposal and cannot guarantee this elsewhere either (Article 4 paragraph 8 sentence 1) or O transported by a Party that has banned the import of such hazardous and other wastes (Article 4(1)(b) Basel Convention)?  (The term importing state includes: a contracting party to which a transboundary shipment of hazardous waste or other waste is planned for the purpose of disposal or for the purpose of loading prior to disposal in an area not under the sovereignty of a state. (Article 2 No. 11)",
            label: "Persistent Organic Pollutants Production and Use Risk for Importing State",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention
                ?.persistentOrganicPollutantsProductionAndUseTransboundaryMovements === "Yes",
            name: "hazardousWasteTransboundaryMovementsLocatedOecdEuLiechtenstein",
            component: "YesNoFormField",
            description: "Is your company based in a country that is within the OECD, EU, or Liechtenstein?",
            label: "Hazardous Waste Transboundary Movements Located OECD, EU, Liechtenstein",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention
                ?.persistentOrganicPollutantsProductionAndUseTransboundaryMovements === "Yes",
            name: "hazardousWasteTransboundaryMovementsOutsideOecdEuLiechtenstein",
            component: "YesNoFormField",
            description:
              "Is there a risk in your company that hazardous waste is transported to a country that is outside the OECD, EU / Liechtenstein?",
            label: "Hazardous Waste Transboundary Movements Outside OECD, EU, Liechtenstein",
          },
          {
            showIf: (): boolean => true,
            name: "hazardousWasteDisposal",
            component: "YesNoFormField",
            description:
              "Do you dispose of hazardous waste within the meaning of the Basel Convention (Article 1 Paragraph 1, BGBI. 1994 II p. 2703, 2704)?",
            label: "Hazardous Waste Disposal",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention?.hazardousWasteDisposal === "Yes",
            name: "hazardousWasteDisposalRiskOfImport",
            component: "YesNoFormField",
            description:
              "Are you at risk of having this waste imported from a country that is not a party to the Basel Convention?",
            label: "Hazardous Waste Disposal Risk Of Import",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention?.hazardousWasteDisposal === "Yes",
            name: "hazardousAndOtherWasteImport",
            component: "YesNoFormField",
            description:
              "Do you import other wastes that require special consideration (household waste, residues from incineration of household waste) (Article 1(2))?",
            label: "Hazardous And Other Waste Import",
          },
        ],
      },
    ],
  },
];
