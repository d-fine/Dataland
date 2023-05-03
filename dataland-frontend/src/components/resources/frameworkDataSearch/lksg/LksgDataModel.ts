import { LksgData } from "@clients/backend";
import { getDataset, DropdownDatasetIdentifier } from "@/utils/PremadeDropdownDatasets";
import { Category } from "@/utils/GenericFrameworkTypes";

export const lksgDataModel = [
  {
    name: "general",
    label: "General",
    color: "orange",
    subcategories: [
      {
        name: "masterData",
        label: "Master Data",
        fields: [
          {
            showIf: (): boolean => true,
            name: "dataDate",
            description: "The date until when the information collected is valid",
            component: "DateFormField",
            required: true,
            label: "Data Date",
          },
          {
            showIf: (): boolean => true,
            name: "headOfficeInGermany",
            description:
              "Is your head office, administrative headquarters, registered office or subsidiary located in Germany?",
            component: "YesNoFormField",
            required: true,
            label: "Head Office in Germany",
          },
          {
            showIf: (): boolean => true,
            name: "groupOfCompanies",
            description: "Do you belong to a group of companies?",
            component: "YesNoFormField",
            required: true,
            label: "Group of Companies",
          },
          {
            showIf: (dataModel: LksgData): boolean => dataModel?.general?.masterData?.groupOfCompanies === "Yes",
            name: "groupOfCompaniesName",
            description: "What is the group of companies called?",
            component: "InputTextFormField",
            required: true,
            label: "Group of Companies Name",
          },
          {
            showIf: (): boolean => true,
            name: "industry",
            description: "In which industry is your company primarily active?",
            component: "NaceCodeFormField",
            required: true,
            label: "Industry",
          },
          {
            showIf: (): boolean => true,
            name: "numberOfEmployees",
            description:
              "What is the total number of employees (including temporary workers with assignment duration >6 months)?",
            component: "NumberFormField",
            required: true,
            label: "Number of Employees",
          },
          {
            showIf: (): boolean => true,
            name: "seasonalOrMigrantWorkers",
            description: "Do you employ seasonal or migrant workers?",
            component: "YesNoFormField",
            required: true,
            label: "Seasonal or Migrant Workers",
          },
          {
            showIf: (): boolean => true,
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
            name: "shareOfTemporaryWorkers",
            description: "What is the share of temporary workers vs total number of employees in the company?",
            component: "RadioButtonsFormField",
            required: true,
            label: "Share of Temporary Workers",
          },
          {
            showIf: (): boolean => true,
            options: getDataset(DropdownDatasetIdentifier.CurrencyCodes),
            placeholder: "Select Currency",
            name: "totalRevenueCurrency",
            description: "The 3-letter code (ISO 4217) representing the currency used for the total revenue",
            component: "SingleSelectFormField",
            required: true,
            label: "Total Revenue Currency",
          },
          {
            showIf: (): boolean => true,
            name: "totalRevenue",
            description: "Total revenue p. a.",
            component: "NumberFormField",
            required: true,
            label: "Total Revenue",
          },
          {
            showIf: (): boolean => true,
            name: "fixedAndWorkingCapital",
            description: "What is your fixed and working capital? (only for own operations)",
            component: "NumberFormField",
            required: false,
            label: "Fixed and Working Capital",
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
            description: "Is your company a manufacturing company?",
            component: "YesNoFormField",
            required: true,
            label: "Manufacturing Company",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.general?.productionSpecific?.manufacturingCompany === "Yes",
            name: "capacity",
            description: "If yes, what is your production capacity per year, e.g. units/year?",
            component: "InputTextFormField",
            required: true,
            label: "Capacity",
          },
          {
            showIf: (): boolean => true,
            name: "isContractProcessing",
            description: "Is production done via subcontracting?",
            component: "YesNoFormField",
            required: true,
            label: "Is Contract Processing",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.general?.productionSpecific?.isContractProcessing === "Yes",
            options: getDataset(DropdownDatasetIdentifier.CountryCodes),
            placeholder: "Select Country",
            name: "subcontractingCompaniesCountries",
            description: "In which countries do the subcontracting companies operate?",
            component: "MultiSelectFormField",
            required: true,
            label: "Subcontracting Companies Countries",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.general?.productionSpecific?.isContractProcessing === "Yes",
            name: "subcontractingCompaniesIndustries",
            description: "In which industries do the subcontracting companies operate?",
            component: "NaceCodeFormField",
            required: true,
            label: "Subcontracting Companies Industries",
          },
          {
            showIf: (): boolean => true,
            name: "multipleProductionSites",
            description: "Do you have production sites in your company?",
            component: "YesNoFormField",
            required: true,
            label: "Multiple Production Sites",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.general?.productionSpecific?.multipleProductionSites === "Yes",
            name: "listOfProductionSites",
            description: "Please list the production sites in your company.",
            component: "ProductionSiteFormField",
            required: true,
            label: "List Of Production Sites",
          },
          {
            showIf: (): boolean => true,
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
            name: "market",
            description: "Does your business focus predominantly on national or international markets?",
            component: "RadioButtonsFormField",
            required: true,
            label: "Market",
          },
          {
            showIf: (): boolean => true,
            name: "specificProcurement",
            description:
              "Does your company have specific procurement models such as: short-lived and changing business relationships, or high price pressure or tightly timed or short-term adjusted delivery deadlines and conditions with suppliers",
            component: "YesNoFormField",
            required: true,
            label: "Specific Procurement",
          },
        ],
      },
    ],
  },
  {
    name: "governance",
    label: "Governance",
    color: "blue",
    subcategories: [
      {
        name: "riskManagementOwnOperations",
        label: "Risk management - Own Operations",
        fields: [
          {
            showIf: (): boolean => true,
            name: "adequateAndEffectiveRiskManagementSystem",
            description: "Does your company have an adequate and effective Risk Management system?",
            component: "YesNoFormField",
            required: false,
            label: "Adequate and Effective Risk Management System",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.adequateAndEffectiveRiskManagementSystem === "Yes",
            name: "riskManagementSystemFiscalYear",
            description: "Did you perform a risk analysis as part of risk management in this fiscal year?",
            component: "YesNoFormField",
            required: false,
            label: "Risk Management System Fiscal Year",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemFiscalYear === "Yes",
            name: "riskManagementSystemRisks",
            description: "Were risks identified during this period?",
            component: "YesNoFormField",
            required: false,
            label: "Risk Management System Risks",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemRisks === "Yes",
            name: "riskManagementSystemIdentifiedRisks",
            description: "Which risks were specifically identified in the risk analysis?",
            component: "InputTextFormField",
            required: false,
            label: "Risk Management System Identified Risks",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemRisks === "Yes",
            name: "riskManagementSystemCounteract",
            description: "Have measures been defined to counteract these risks?",
            component: "YesNoFormField",
            required: false,
            label: "Risk Management System Counteract",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemCounteract === "Yes",
            name: "riskManagementSystemMeasures",
            description: "What measures have been applied to counteract the risks?",
            component: "InputTextFormField",
            required: false,
            label: "Risk Management System Measures",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.adequateAndEffectiveRiskManagementSystem === "Yes",
            name: "riskManagementSystemResponsibility",
            description:
              "Is the responsibility for the Risk Management in your company regulated, for example by appointing a human rights officer?",
            component: "YesNoFormField",
            required: false,
            label: "Risk Management System Responsibility",
          },
          {
            showIf: (): boolean => true,
            name: "environmentalManagementSystem",
            description: "Is an environmental management system implemented in your company?",
            component: "YesNoFormField",
            required: false,
            label: "Environmental Management System",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.environmentalManagementSystem === "Yes",
            name: "environmentalManagementSystemInternationalCertification",
            description: "Is the environmental management system internationally recognised and certified?",
            component: "YesNoFormField",
            required: false,
            label: "Environmental Management System International Certification",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.environmentalManagementSystem === "Yes",
            name: "environmentalManagementSystemNationalCertification",
            description: "Is the environmental management system nationally recognised and certified?",
            component: "YesNoFormField",
            required: false,
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
            description:
              "Has your company implemented a grievance mechanism (e.g., anonymous whistleblowing system) to protect human and environmental rights in your business?",
            component: "YesNoFormField",
            required: false,
            label: "Grievance Handling Mechanism",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            name: "grievanceHandlingMechanismUsedForReporting",
            description:
              "Can all affected stakeholders and rights holders, i.e. both internal (e.g. employees) and external stakeholders (e.g. suppliers and their employees, NGOs) use the grievance channel/whistleblowing system for reporting?",
            component: "YesNoFormField",
            required: false,
            label: "Grievance Handling Mechanism Used For Reporting",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            name: "grievanceMechanismInformationProvided",
            description:
              "Is information about the process provided in a way that is adapted to the context and target groups?",
            component: "YesNoFormField",
            required: false,
            label: "Grievance Mechanism Information Provided",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            name: "grievanceMechanismSupportProvided",
            description: "Is the necessary support provided so that the target groups can actually use the procedure?",
            component: "YesNoFormField",
            required: false,
            label: "Grievance Mechanism Support Provided",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            name: "grievanceMechanismAccessToExpertise",
            description:
              "Do the target groups have access to the expertise, advice and information that they need to participate in the grievance procedure in a fair, informed and respectful manner?",
            component: "YesNoFormField",
            required: false,
            label: "Grievance Mechanism Access to Expertise",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            name: "grievanceMechanismComplaints",
            description: "Have there been any complaints that have entered the system in the past?",
            component: "YesNoFormField",
            required: false,
            label: "Grievance Mechanism Complaints",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaints === "Yes",
            name: "grievanceMechanismComplaintsNumber",
            description: "How many complaints have been received?",
            component: "NumberFormField",
            required: false,
            label: "Grievance Mechanism Complaints Number",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaints === "Yes",
            name: "grievanceMechanismComplaintsReason",
            description: "What complaints have been received?",
            component: "InputTextFormField",
            required: false,
            label: "Grievance Mechanism Complaints Reason",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaints === "Yes",
            name: "grievanceMechanismComplaintsAction",
            description: "Have actions been taken to address the complaints?",
            component: "YesNoFormField",
            required: false,
            label: "Grievance Mechanism Complaints Action",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaintsAction === "Yes",
            name: "grievanceMechanismComplaintsActionUndertaken",
            description: "What actions have been taken?",
            component: "InputTextFormField",
            required: false,
            label: "Grievance Mechanism Complaints Action undertaken",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            name: "grievanceMechanismPublicAccess",
            description:
              "Does your company have publicly accessible rules that clearly describe the process for dealing with complaints?",
            component: "YesNoFormField",
            required: false,
            label: "Grievance Mechanism Public Access",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            name: "grievanceMechanismProtection",
            description: "Does the process effectively protect whistleblowers from disadvantage or punishment?",
            component: "YesNoFormField",
            required: false,
            label: "Grievance Mechanism Protection",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            name: "grievanceMechanismDueDiligenceProcess",
            description:
              "Do the findings from the processing of clues flow into the adjustment of your own due diligence processes?",
            component: "YesNoFormField",
            required: false,
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
            description:
              "Is your company SA8000 certified? If yes, please share the certificate with us. (Corporate Social Responsibility)",
            component: "YesNoFormField",
            required: true,
            label: "SA8000 Certification",
          },
          {
            showIf: (): boolean => true,
            name: "smetaSocialAuditConcept",
            description:
              "Does your company apply a social audit concept as defined by SMETA (Sedex Members Ethical Trade Audit)? (social audit)",
            component: "YesNoFormField",
            required: true,
            label: "SMETA Social Audit Concept",
          },
          {
            showIf: (): boolean => true,
            name: "betterWorkProgramCertificate",
            description:
              "Do the production sites where the goods are produced participate in the BetterWork program? If yes, please share the certificate with us. (private label only)",
            component: "YesNoFormField",
            required: true,
            label: "Better Work Program Certificate",
          },
          {
            showIf: (): boolean => true,
            name: "iso45001Certification",
            description:
              "Is your company ISO45001 certified? If yes, please share the certificate with us. (Management Systems of Occupational Health and Safety)",
            component: "YesNoFormField",
            required: true,
            label: "ISO 45001 Certification",
          },
          {
            showIf: (): boolean => true,
            name: "iso14000Certification",
            description: "Is your company ISO14000 certified? If yes, please share the certificate with us.",
            component: "YesNoFormField",
            required: true,
            label: "ISO 14000 Certification",
          },
          {
            showIf: (): boolean => true,
            name: "emasCertification",
            description:
              "Is your company certified according to EMAS? If yes, please share the certificate with us. (Voluntary environmental management)",
            component: "YesNoFormField",
            required: true,
            label: "EMAS Certification",
          },
          {
            showIf: (): boolean => true,
            name: "iso37001Certification",
            description:
              "Is your company ISO37001 certified? If yes, please share the certificate with us. (Anti-bribery management systems)",
            component: "YesNoFormField",
            required: true,
            label: "ISO 37001 Certification",
          },
          {
            showIf: (): boolean => true,
            name: "iso37301Certification",
            description:
              "Is your company ISO37301 certified? If yes, please share the certificate with us. (Compliance Management System)",
            component: "YesNoFormField",
            required: true,
            label: "ISO37301 Certification",
          },
          {
            showIf: (): boolean => true,
            name: "riskManagementSystemCertification",
            description: "Is the Risk Management System internationally recognized and certified? (e.g.: ISO 31000)",
            component: "YesNoFormField",
            required: true,
            label: "Risk Management System Certification",
          },
          {
            showIf: (): boolean => true,
            name: "amforiBsciAuditReport",
            description:
              "Does your company have a current amfori BSCI audit report? If yes, please share the certificate with us.",
            component: "YesNoFormField",
            required: true,
            label: "amfori BSCI Audit Report",
          },
          {
            showIf: (): boolean => true,
            name: "responsibleBusinessAssociationCertification",
            description:
              "Is your company Responsible Business Association (RBA) certified? If yes, please share the certificate with us. (Social Responsibility)",
            component: "YesNoFormField",
            required: true,
            label: "Responsible Business Association Certification",
          },
          {
            showIf: (): boolean => true,
            name: "fairLaborAssociationCertification",
            description:
              "Is your company Fair Labor Association (FLA) certified? If yes, please share the certificate with us. (Adherence to international and national labor laws)",
            component: "YesNoFormField",
            required: true,
            label: "Fair Labor Association Certification",
          },
          {
            showIf: (): boolean => true,
            name: "additionalAudits",
            description:
              "Please list other (sector-specific) audits (if available) to which your company is certified.",
            component: "InputTextFormField",
            required: false,
            label: "Additional Audits",
          },
          {
            showIf: (): boolean => true,
            name: "codeOfConduct",
            description:
              "Has your company implemented and enforced (e.g., within the Code Of Conducts) internal behavioural guidelines that address the issues of human rights protection and respect for the environment?",
            component: "YesNoFormField",
            required: true,
            label: "Code Of Conduct",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.certificationsPoliciesAndResponsibilities?.codeOfConduct === "Yes",
            name: "codeOfConductTraining",
            description:
              "Are your employees regularly made aware of the internal rules of conduct and trained on them?",
            component: "YesNoFormField",
            required: true,
            label: "Code Of Conduct Training",
          },
          {
            showIf: (): boolean => true,
            name: "supplierCodeOfConduct",
            description:
              "Does your company have a Supplier Code Of Conduct? (If yes, please share the Supplier Code of Conduct with us)",
            component: "YesNoFormField",
            required: true,
            label: "Supplier Code Of Conduct",
          },
          {
            showIf: (): boolean => true,
            name: "policyStatement",
            description: "Does your company have a policy statement on its human rights strategy?",
            component: "YesNoFormField",
            required: true,
            label: "Policy Statement",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.certificationsPoliciesAndResponsibilities?.policyStatement === "Yes",
            name: "humanRightsStrategy",
            description:
              "In which relevant departments/business processes has the anchoring of the human rights strategy been ensured",
            component: "InputTextFormField",
            required: false,
            label: "Human Rights Strategy",
          },
          {
            showIf: (): boolean => true,
            name: "environmentalImpactPolicy",
            description:
              "Does your company have a Environmental Impact Policy? (If yes, please share the policy with us)",
            component: "YesNoFormField",
            required: true,
            label: "Environmental Impact Policy",
          },
          {
            showIf: (): boolean => true,
            name: "fairWorkingConditionsPolicy",
            description:
              "Does your company have a Fair Working Conditions Policy? (If yes, please share the policy with us)",
            component: "YesNoFormField",
            required: true,
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
            description:
              "Has your company established, according to the nature and extent of the enterprise’s business activities, official responsibilities for the topic of fair working conditions?",
            component: "YesNoFormField",
            required: true,
            label: "Responsibilities For Fair Working Conditions",
          },
          {
            showIf: (): boolean => true,
            name: "responsibilitiesForTheEnvironment",
            description:
              "Has your company established, according to the nature and extent of the enterprise’s business activities, official responsibilities for the topic of the environment?",
            component: "YesNoFormField",
            required: true,
            label: "Responsibilities For The Environment",
          },
          {
            showIf: (): boolean => true,
            name: "responsibilitiesForOccupationalSafety",
            description:
              "Has your company established, according to the nature and extent of the enterprise’s business activities, official responsibilities for the topic of occupational safety?",
            component: "YesNoFormField",
            required: true,
            label: "Responsibilities For Occupational Safety",
          },
          {
            showIf: (): boolean => true,
            name: "legalProceedings",
            description:
              "Has your company been involved in the last 5 years, in legal disputes (including currently ongoing disputes) with third parties regarding human rights and environmental violations?",
            component: "YesNoFormField",
            required: true,
            label: "Legal Proceedings",
          },
          {
            showIf: (): boolean => true,
            name: "humanRightsViolation",
            description:
              "Have there been any violations of human rights or environmental aspects on your part in the last 5 years?",
            component: "YesNoFormField",
            required: true,
            label: "Human Rights Violation",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.humanRightsViolation === "Yes",
            name: "humanRightsViolations",
            description: "What were the violations?",
            component: "InputTextFormField",
            required: true,
            label: "Human Rights Violations",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.humanRightsViolation === "Yes",
            name: "humanRightsViolationAction",
            description: "Has action been taken to address the violations?",
            component: "YesNoFormField",
            required: true,
            label: "Human Rights Violation Action",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.humanRightsViolationAction === "Yes",
            name: "humanRightsViolationActionMeasures",
            description: "What measures have been taken?",
            component: "InputTextFormField",
            required: true,
            label: "Human Rights Violation Action Measures",
          },
          {
            showIf: (): boolean => true,
            name: "highRiskCountriesRawMaterials",
            description: "Do you source your raw materials from verified conflict or high-risk regions?",
            component: "YesNoFormField",
            required: true,
            label: "High Risk Countries Raw Materials",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.highRiskCountriesRawMaterials === "Yes",
            options: getDataset(DropdownDatasetIdentifier.CountryCodes),
            placeholder: "Select Country",
            name: "highRiskCountriesRawMaterialsLocation",
            description: "From which conflict/high-risk regions do you source your raw materials?",
            component: "MultiSelectFormField",
            required: true,
            label: "High Risk Countries Raw Materials Location",
          },
          {
            showIf: (): boolean => true,
            name: "highRiskCountriesActivity",
            description:
              "Does your company have activities in countries where there are high risks for human rights and/or the environment?",
            component: "YesNoFormField",
            required: true,
            label: "High Risk Countries Activity",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.highRiskCountriesActivity === "Yes",
            options: getDataset(DropdownDatasetIdentifier.CountryCodes),
            placeholder: "Select Country",
            name: "highRiskCountries",
            description: "In which high risk countries does your company have activities?",
            component: "MultiSelectFormField",
            required: true,
            label: "High Risk Countries",
          },
          {
            showIf: (): boolean => true,
            name: "highRiskCountriesProcurement",
            description:
              "Does your company procure from countries with high risks for human rights and/or the environment?",
            component: "YesNoFormField",
            required: true,
            label: "High Risk Countries Procurement",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.highRiskCountriesProcurement === "Yes",
            options: getDataset(DropdownDatasetIdentifier.CountryCodes),
            placeholder: "Select Country",
            name: "highRiskCountriesProcurementName",
            description: "From which high risk countries does your company procure from?",
            component: "MultiSelectFormField",
            required: false,
            label: "High Risk Countries Procurement Name",
          },
        ],
      },
    ],
  },
  {
    name: "social",
    label: "Social",
    color: "yellow",
    subcategories: [
      {
        name: "childLabor",
        label: "Child labor",
        fields: [
          {
            showIf: (): boolean => true,
            name: "employeeUnder18",
            description: "Does your company have employees under the age of 18?",
            component: "YesNoFormField",
            required: false,
            label: "Employee Under 18",
          },
          {
            showIf: (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.employeeUnder18 === "Yes",
            name: "employeeUnder18Under15",
            description:
              "With regard to the place of employment and the applicable laws: do you employ school-age children or children under the age of 15 on a full-time basis?",
            component: "YesNoFormField",
            required: false,
            label: "Employee Under 18 Under 15",
          },
          {
            showIf: (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.employeeUnder18 === "Yes",
            name: "employeeUnder18Apprentices",
            description:
              "Are the employees under 18 years of age exclusively apprentices within the meaning of the locally applicable laws?",
            component: "YesNoFormField",
            required: false,
            label: "Employee Under 18 Apprentices",
          },
          {
            showIf: (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.employeeUnder18 === "Yes",
            name: "worstFormsOfChildLaborProhibition",
            description:
              "Is the prohibition of the worst forms of child labor ensured in your company? This includes: all forms of slavery or practices similar to slavery, the use, procuring or offering of a child for prostitution, the production of pornography or pornographic performances, the use, procuring or offering of a child for illicit activities, in particular for the production or trafficking of drugs, work which, by its nature or the circumstances in which it is performed, is likely to be harmful to the health, safety, or morals of children",
            component: "YesNoFormField",
            required: false,
            label: "Worst Forms of Child Labor Prohibition",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.worstFormsOfChildLaborProhibition === "Yes",
            name: "worstFormsOfChildLaborForms",
            description: "What forms of worst forms of child labor have been identified?",
            component: "InputTextFormField",
            required: false,
            label: "Worst Forms of Child Labor Forms",
          },
          {
            showIf: (): boolean => true,
            name: "employmentUnderLocalMinimumAgePrevention",
            description:
              "Does your company take measures to prevent the employment of children under the local minimum age?",
            component: "YesNoFormField",
            required: false,
            label: "Employment Under Local Minimum Age Prevention",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            name: "employmentUnderLocalMinimumAgePreventionEmploymentContracts",
            description:
              "Is a formal recruitment process including the conclusion of employment contracts such a measure?",
            component: "YesNoFormField",
            required: false,
            label: "Employment Under Local Minimum Age Prevention Employment Contracts",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            name: "employmentUnderLocalMinimumAgePreventionJobDescription",
            description:
              "Is a clear job description for employees under the local minimum age in the hiring process and employment contracts such a measure? (group of people between 15 and 18 years)",
            component: "YesNoFormField",
            required: false,
            label: "Employment Under Local Minimum Age Prevention Job Description",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            name: "employmentUnderLocalMinimumAgePreventionIdentityDocuments",
            description:
              "Is the control of official documents (e.g. identity documents and certificates) such a measure?",
            component: "YesNoFormField",
            required: false,
            label: "Employment Under Local Minimum Age Prevention Identity Documents",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            name: "employmentUnderLocalMinimumAgePreventionTraining",
            description:
              "Is raising the awareness of staff involved in the recruitment process through training such a measure?",
            component: "YesNoFormField",
            required: false,
            label: "Employment Under Local Minimum Age Prevention Training",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            name: "employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge",
            description: "Is the regular checking of the legal minimum age such a measure?",
            component: "YesNoFormField",
            required: false,
            label: "Employment Under Local Minimum Age Prevention Checking Of Legal Minimum Age",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            name: "childLaborMeasures",
            description:
              "Please list any other measures (if available) you take to prevent the employment of children under the locally applicable minimum age?",
            component: "InputTextFormField",
            required: false,
            label: "Child Labor Measures",
          },
          {
            showIf: (): boolean => true,
            name: "childLaborPreventionPolicy",
            description:
              "Does your company have a policy to prevent child labor? (If yes, please share the policy with us)",
            component: "YesNoFormField",
            required: false,
            label: "Child Labor Prevention Policy",
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
            description:
              "Does your company have practices that lead or may lead to forced labor and/or slavery?  The following are included: Creating unacceptable working and living conditions by working in hazardous conditions or in unacceptable accommodations provided by the employer; Excessive levels of overtime; Use of intimidation, threats, and/or punishment; Other types of forced labor (e.g. debt bondage, human trafficking)",
            component: "YesNoFormField",
            required: false,
            label: "Forced Labor And Slavery Prevention",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPrevention === "Yes",
            name: "forcedLaborAndSlaveryPreventionPractices",
            description: "Please specify which of the points exist?",
            component: "InputTextFormField",
            required: false,
            label: "Forced Labor And Slavery Prevention Practices",
          },
          {
            showIf: (): boolean => true,
            name: "forcedLaborAndSlaveryPreventionMeasures",
            description: "Does your company take measures to prevent forced labor and slavery?",
            component: "YesNoFormField",
            required: false,
            label: "Forced Labor And Slavery Prevention Measures",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            name: "forcedLaborAndSlaveryPreventionEmploymentContracts",
            description:
              "Is a formal hiring process, including employment contracts in the employee's local language, with appropriate wage and termination clauses, such a measure?",
            component: "YesNoFormField",
            required: false,
            label: "Forced Labor And Slavery Prevention Employment Contracts",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            name: "forcedLaborAndSlaveryPreventionIdentityDocuments",
            description: "Is a ban on the retention of identity documents such a measure?",
            component: "YesNoFormField",
            required: false,
            label: "Forced Labor And Slavery Prevention Identity Documents",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            name: "forcedLaborAndSlaveryPreventionFreeMovement",
            description:
              "Is ensuring the free movement of employees through doors and windows that can be opened to leave the building/premises of your company at any time such a measure?",
            component: "YesNoFormField",
            required: false,
            label: "Forced Labor And Slavery Prevention Free Movement",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            name: "forcedLaborAndSlaveryPreventionProvisionSocialRoomsAndToilets",
            description: "Is the provision of social rooms and toilets that can be visited at any time such a measure?",
            component: "YesNoFormField",
            required: false,
            label: "Forced Labor And Slavery Prevention Provision Social Rooms and Toilets",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            name: "forcedLaborAndSlaveryPreventionTraining",
            description:
              "Is raising the awareness of staff involved in the recruitment process through training such a measure?",
            component: "YesNoFormField",
            required: false,
            label: "Forced Labor And Slavery Prevention Training",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            name: "forcedLaborAndSlaveryPreventionMeasuresOther",
            description: "Please list other measures (if available) you take to prevent forced labor and slavery?",
            component: "InputTextFormField",
            required: false,
            label: "Forced Labor and Slavery Prevention Measures (Other)",
          },
          {
            showIf: (): boolean => true,
            name: "forcedLaborPreventionPolicy",
            description:
              "Does your company have a policy to prevent forced labor? (If yes, please share the policy with us)",
            component: "YesNoFormField",
            required: false,
            label: "Forced Labor Prevention Policy",
          },
        ],
      },
      {
        name: "withholdingAdequateWages",
        label: "Withholding adequate wages",
        fields: [
          {
            showIf: (): boolean => true,
            name: "adequateWage",
            description: "Is your company currently withholding adequate wages (adequate in the sense of local laws)?",
            component: "YesNoFormField",
            required: false,
            label: "Adequate Wage",
          },
          {
            showIf: (): boolean => true,
            name: "adequateWageBeingWithheld",
            description: "Are any measures taken in your company to prevent that adequate wages being withheld?",
            component: "YesNoFormField",
            required: false,
            label: "Adequate Wage being withheld",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequateWageBeingWithheld === "Yes",
            name: "documentedWorkingHoursAndWages",
            description: "Does your company document the working hours and wages of its employees?",
            component: "YesNoFormField",
            required: false,
            label: "Documented Working Hours And Wages",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequateWageBeingWithheld === "Yes",
            name: "adequateLivingWage",
            description:
              "Does your company pay employees adequate wages? (the appropriate wage is at least the minimum wage set by the applicable law and is otherwise measured according to the law of the place of employment).",
            component: "YesNoFormField",
            required: false,
            label: "Adequate Living Wage",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequateWageBeingWithheld === "Yes",
            name: "regularWagesProcessFlow",
            description:
              "Has your company implemented the payment of wages through standardised and regular process flows?",
            component: "YesNoFormField",
            required: false,
            label: "Regular Wages Process Flow",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequateWageBeingWithheld === "Yes",
            name: "fixedHourlyWages",
            description: "Do fixed hourly wages exist in your company?",
            component: "YesNoNaFormField",
            required: false,
            label: "Fixed Hourly Wages",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequateWageBeingWithheld === "Yes",
            name: "fixedPieceworkWages",
            description: "Does your company have fixed piecework wages?",
            component: "YesNoNaFormField",
            required: false,
            label: "Fixed Piecework Wages",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequateWageBeingWithheld === "Yes",
            name: "adequateWageMeasures",
            description:
              "Please list other measures (if available) you take to prevent the withholding adequate wages?",
            component: "InputTextFormField",
            required: false,
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
            description: "Do your employees perform low-skill manual work or repetitive manual work?",
            component: "YesNoFormField",
            required: false,
            label: "Low Skill Work",
          },
          {
            showIf: (): boolean => true,
            name: "hazardousMachines",
            description: "Are hazardous machines used in the manufacture of (preliminary) products?",
            component: "YesNoFormField",
            required: false,
            label: "Hazardous Machines",
          },
          {
            showIf: (): boolean => true,
            name: "oshPolicy",
            description:
              "Has your company implemented and enforced a formal occupational health and safety (OSH) policy that complies with local laws, industry requirements and international standards?",
            component: "YesNoFormField",
            required: false,
            label: "OSH Policy",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyPersonalProtectiveEquipment",
            description: "Is the subject area of personal protective equipment addressed by this OSH Directive?",
            component: "YesNoNaFormField",
            required: false,
            label: "OSH Policy Personal Protective Equipment",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyMachineSafety",
            description: "Is the subject area of machine safety addressed by this OSH Directive?",
            component: "YesNoNaFormField",
            required: false,
            label: "OSH Policy Machine Safety",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyDisasterBehaviouralResponse",
            description: "Is the subject area of behaviour in the event of a disaster addressed by this OSH Directive?",
            component: "YesNoFormField",
            required: false,
            label: "OSH Policy Disaster Behavioural Response",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyAccidentsBehaviouralResponse",
            description:
              "Is the subject area of behaviour in the event of and prevention of accidents addressed by this OSH Directive?",
            component: "YesNoFormField",
            required: false,
            label: "OSH Policy Accidents Behavioural Response",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyWorkplaceErgonomics",
            description: "Is the subject area of workplace ergonomics addressed by this OSH Directive?",
            component: "YesNoFormField",
            required: false,
            label: "OSH Policy Workplace Ergonomics",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyAccessToWork",
            description: "Is access to the work secluded/is the workplace difficult to access?",
            component: "YesNoFormField",
            required: false,
            label: "OSH Policy Access to work",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyHandlingChemicalsAndOtherHazardousSubstances",
            description:
              "Is the subject area of handling chemical, physical or biological substances addressed by this OSH Directive?",
            component: "YesNoNaFormField",
            required: false,
            label: "OSH Policy Handling Chemicals And Other Hazardous Substances",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyFireProtection",
            description: "Is the subject area of fire protection addressed by this OSH Directive?",
            component: "YesNoFormField",
            required: false,
            label: "OSH Policy Fire Protection",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyWorkingHours",
            description:
              "Is the subject area of regulation of working hours, overtime and rest breaks be addressed by this OSH Directive?",
            component: "YesNoFormField",
            required: false,
            label: "OSH Policy Working Hours",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyTrainingAddressed",
            description:
              "Is the subject area of training and instruction of employees with regard to occupational health and safety addressed by this OSH Directive?",
            component: "YesNoFormField",
            required: false,
            label: "OSH Policy Training Addressed",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            name: "oshPolicyTraining",
            description: "Are your employees regularly made aware of the OSH Directive and trained on them?",
            component: "YesNoFormField",
            required: false,
            label: "OSH Policy Training",
          },
          {
            showIf: (): boolean => true,
            name: "oshManagementSystem",
            description: "Is an occupational health and safety management system implemented in your company?",
            component: "YesNoFormField",
            required: false,
            label: "OSH Management System",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshManagementSystem === "Yes",
            name: "oshManagementSystemInternationalCertification",
            description: "Is the OSH management system internationally recognised and certified?",
            component: "YesNoFormField",
            required: false,
            label: "OSH Management System International Certification",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshManagementSystem === "Yes",
            name: "oshManagementSystemNationalCertification",
            description: "Is the OSH management system nationally recognised and certified?",
            component: "YesNoFormField",
            required: false,
            label: "OSH Management System National Certification",
          },
          {
            showIf: (): boolean => true,
            name: "workplaceAccidentsUnder10",
            description:
              "Is the number of incidents in which employees suffered work-related injuries with serious consequences less than 10 in the past fiscal year?",
            component: "YesNoFormField",
            required: false,
            label: "Workplace Accidents Under 10",
          },
          {
            showIf: (): boolean => true,
            name: "oshTraining",
            description:
              "Has your company introduced mandatory offers and training for employees to improve occupational safety?",
            component: "YesNoFormField",
            required: false,
            label: "OSH Training",
          },
          {
            showIf: (): boolean => true,
            name: "healthAndSafetyPolicy",
            description: "Does your company have a Health And Safety Policy? (If yes, please share the policy with us)",
            component: "YesNoFormField",
            required: false,
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
            description: "Does your company ensure that employees are free to form or join trade unions?",
            component: "YesNoFormField",
            required: false,
            label: "Freedom Of Association",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForFreedomOfAssociation?.freedomOfAssociation === "Yes",
            name: "representedEmployees",
            description: "What is your percentage of employees who are represented by trade unions?",
            component: "NumberFormField",
            required: false,
            label: "Represented Employees",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForFreedomOfAssociation?.freedomOfAssociation === "Yes",
            name: "discriminationForTradeUnionMembers",
            description:
              "Does your company ensure that no discrimination is practised or other consequences taken against employees in the event of the formation, joining and membership of a trade union?",
            component: "YesNoFormField",
            required: false,
            label: "Discrimination For Trade Union Members",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForFreedomOfAssociation?.freedomOfAssociation === "Yes",
            name: "freedomOfOperationForTradeUnion",
            description:
              "Does your company ensure that trade unions are free to operate in accordance with the law of the place of employment?",
            component: "YesNoFormField",
            required: false,
            label: "Freedom Of Operation For Trade Union",
          },
          {
            showIf: (): boolean => true,
            name: "freedomOfAssociationTraining",
            description:
              "Do employees receive information about their rights as part of training and/or intranet, notices or company brochures?",
            component: "YesNoFormField",
            required: false,
            label: "Freedom Of Association Training",
          },
          {
            showIf: (): boolean => true,
            name: "worksCouncil",
            description:
              "Does your company have a works council or an employee representative committee (if it can be set up in your company in accordance with local applicable legal provisions)?",
            component: "YesNoFormField",
            required: false,
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
            name: "unequalTreatmentOfEmployment",
            description:
              "Does your company treat employees unequally because of national/ethnic origin, social origin, health status, disability, sexual orientation, age, gender, political opinion, religion or belief?",
            component: "YesNoFormField",
            required: false,
            label: "Unequal Treatment of Employment",
          },
          {
            showIf: (): boolean => true,
            name: "diversityAndInclusionRole",
            description:
              "Is a member of your company's management responsible for promoting diversity in the workforce and among business partners?",
            component: "YesNoFormField",
            required: false,
            label: "Diversity And Inclusion Role",
          },
          {
            showIf: (): boolean => true,
            name: "preventionOfMistreatments",
            description:
              "Does your company's management promote a work environment free from physical, sexual, mental and verbal abuse, threats or other forms of mistreatment? (e.g. diversity program)",
            component: "YesNoFormField",
            required: false,
            label: "Prevention Of Mistreatments",
          },
          {
            showIf: (): boolean => true,
            name: "equalOpportunitiesOfficer",
            description: "Is an equal opportunities officer (or similar function) implemented in your company?",
            component: "YesNoFormField",
            required: false,
            label: "Equal Opportunities Officer",
          },
          {
            showIf: (): boolean => true,
            name: "fairAndEthicalRecruitmentPolicy",
            description:
              "Does your company have a Fair And Ethical Recruitment Policy? (If yes, please share the policy with us)",
            component: "YesNoFormField",
            required: false,
            label: "Fair And Ethical Recruitment Policy",
          },
          {
            showIf: (): boolean => true,
            name: "equalOpportunitiesAndNonDiscriminationPolicy",
            description:
              "Does your company have a Equal Opportunities And Non-discrimination Policy? (If yes, please share the policy with us)",
            component: "YesNoFormField",
            required: false,
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
            description: "Is there a risk of your company causing a harmful soil change?",
            component: "YesNoFormField",
            required: false,
            label: "Harmful Soil Change",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            name: "soilDegradation",
            description:
              "Does your company have measures in place to prevent the degradation of soil structure caused by the use of heavy machinery?",
            component: "YesNoFormField",
            required: false,
            label: "Soil Degradation",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            name: "soilErosion",
            description:
              "Does your company have measures in place to prevent soil erosion caused by deforestation or overgrazing?",
            component: "YesNoFormField",
            required: false,
            label: "Soil Erosion",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            name: "soilBornDiseases",
            description:
              "Does your company have measures in place to prevent the development of soil-borne diseases and pests and to maintain soil fertility?",
            component: "YesNoFormField",
            required: false,
            label: "Soil Born Diseases",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            name: "soilContamination",
            description:
              "Does your company have measures in place to prevent soil contamination by antibiotics and toxins?",
            component: "YesNoFormField",
            required: false,
            label: "Soil Contamination",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            name: "soilSalinisation",
            description: "Does your company have measures in place to prevent soil salinisation?",
            component: "YesNoFormField",
            required: false,
            label: "Soil Salinisation",
          },
          {
            showIf: (): boolean => true,
            name: "harmfulWaterPollution",
            description: "Is there a risk of your company causing harmful water pollution?",
            component: "YesNoFormField",
            required: false,
            label: "Harmful Water Pollution",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulWaterPollution === "Yes",
            name: "fertilisersOrPollutants",
            description: "Does your company use fertilisers or pollutants such as chemicals or heavy metals?",
            component: "YesNoFormField",
            required: false,
            label: "Fertilisers Or Pollutants",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulWaterPollution === "Yes",
            name: "wasteWaterFiltration",
            description: "Does your company have filtration systems for the waste water?",
            component: "YesNoFormField",
            required: false,
            label: "Waste Water Filtration",
          },
          {
            showIf: (): boolean => true,
            name: "harmfulAirPollution",
            description: "Is there a risk of harmful air pollution in your company?",
            component: "YesNoFormField",
            required: false,
            label: "Harmful Air Pollution",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulAirPollution === "Yes",
            name: "airFiltration",
            description: "Does your company have air filtration systems?",
            component: "YesNoFormField",
            required: false,
            label: "Air Filtration",
          },
          {
            showIf: (): boolean => true,
            name: "harmfulNoiseEmission",
            description: "Is there a risk of harmful noise emission in your company?",
            component: "YesNoFormField",
            required: false,
            label: "Harmful Noise Emission",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulNoiseEmission === "Yes",
            name: "reduceNoiseEmissions",
            description: "Has your company implemented structural measures to reduce noise emissions?",
            component: "YesNoFormField",
            required: false,
            label: "Reduce Noise Emissions",
          },
          {
            showIf: (): boolean => true,
            name: "excessiveWaterConsumption",
            description: "Is there a risk of excessive water consumption in your company?",
            component: "YesNoFormField",
            required: false,
            label: "Excessive Water Consumption",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.excessiveWaterConsumption === "Yes",
            name: "waterSavingMeasures",
            description: "Do you take water-saving measures in your companies?",
            component: "YesNoFormField",
            required: false,
            label: "Water Saving Measures",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.waterSavingMeasures === "Yes",
            name: "waterSavingMeasuresName",
            description: "If yes, which ones?",
            component: "InputTextFormField",
            required: false,
            label: "Water Saving Measures Name",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.excessiveWaterConsumption === "Yes",
            name: "pipeMaintaining",
            description: "Are water pipes regularly checked and maintained?",
            component: "YesNoFormField",
            required: false,
            label: "Pipe Maintaining",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.excessiveWaterConsumption === "Yes",
            name: "waterSources",
            description:
              "Does your company use water sources that are important for the local population or local agriculture?",
            component: "YesNoFormField",
            required: false,
            label: "Water Sources",
          },
          {
            showIf: (): boolean => true,
            name: "contaminationMeasures",
            description:
              "Please list any other measures (if available) you are taking to prevent the risk of harmful soil change, water pollution, air pollution, harmful noise emission or excessive water consumption that: O significantly affects the natural basis for food preservation and production O denies a person access to safe drinking water O impedes or destroys a person's access to sanitary facilities, or O harms the health of any person",
            component: "InputTextFormField",
            required: false,
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
            description:
              "Is your company, as a result of the acquisition, development and/or other use of land, forests and/or bodies of water, which secures a person's livelihood, at risk of \n O an unlawful eviction\n O carrying out an unlawful taking of land, forests and/or water?",
            component: "YesNoFormField",
            required: false,
            label: "Unlawful Eviction And Taking Of Land",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.unlawfulEvictionDeprivationOfLandForestAndWater?.unlawfulEvictionAndTakingOfLand ===
              "Yes",
            name: "unlawfulEvictionAndTakingOfLandRisk",
            description: "If so, what exactly is the risk?",
            component: "InputTextFormField",
            required: false,
            label: "Unlawful Eviction And Taking Of Land Risk",
          },
          {
            showIf: (): boolean => true,
            name: "unlawfulEvictionAndTakingOfLandStrategies",
            description:
              "Has your company developed and implemented strategies that avoid, reduce, mitigate or remedy direct and indirect negative impacts on the land and natural resources of indigenous peoples and local communities?",
            component: "YesNoFormField",
            required: false,
            label: "Unlawful Eviction And Taking Of Land Strategies",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.unlawfulEvictionDeprivationOfLandForestAndWater
                ?.unlawfulEvictionAndTakingOfLandStrategies === "Yes",
            name: "unlawfulEvictionAndTakingOfLandStrategiesName",
            description: "If yes, which ones?",
            component: "InputTextFormField",
            required: false,
            label: "Unlawful Eviction And Taking Of Land Strategies Name",
          },
          {
            showIf: (): boolean => true,
            name: "voluntaryGuidelinesOnTheResponsibleGovernanceOfTenure",
            description:
              "Have you implemented the Voluntary Guidelines on the Responsible Governance of Tenure in your company?",
            component: "YesNoFormField",
            required: false,
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
            description:
              "Does your company use private and/or public security forces to protect company projects or similar?",
            component: "YesNoFormField",
            required: false,
            label: "Use Of Private Public Security Forces",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForces === "Yes",
            name: "useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights",
            description:
              "Does your company have measures in place to prevent security forces during an operation from O violate the prohibition of torture and/or cruel, inhuman and/or degrading treatment O damages life or limb O impairs the right to organize and the freedom of association?",
            component: "YesNoFormField",
            required: false,
            label: "Use Of Private Public Security Forces And Risk Of Violation Of Human Rights",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
            name: "instructionOfSecurityForces",
            description: "Is an adequate instruction of the security forces such a measure?",
            component: "YesNoFormField",
            required: false,
            label: "Instruction Of Security Forces",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
            name: "humanRightsTraining",
            description: "Is training on human rights such a measure?",
            component: "YesNoFormField",
            required: false,
            label: "Human Rights Training",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
            name: "stateSecurityForces",
            description:
              "(Only in the case of state security forces) Before the security forces were commissioned, was it checked whether serious human rights violations by these units had already been documented?",
            component: "YesNoNaFormField",
            required: false,
            label: "State Security Forces",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
            name: "privateSecurityForces",
            description:
              "(Only in the case of private security forces) Have the contractual relationships with the security guards been designed in such a way that they comply with the applicable legal framework?",
            component: "YesNoNaFormField",
            required: false,
            label: "Private Security Forces",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
            name: "useOfPrivatePublicSecurityForcesMeasures",
            description:
              "Please list any other measures (if available) you are taking to prevent the use of private/public security forces in violation of human rights?",
            component: "InputTextFormField",
            required: false,
            label: "Use Of Private Public Security Forces Measures",
          },
        ],
      },
    ],
  },
  {
    name: "environmental",
    label: "Environmental",
    color: "green",
    subcategories: [
      {
        name: "useOfMercuryMercuryWasteMinamataConvention",
        label: "Use of mercury, mercury waste (Minamata Convention)",
        fields: [
          {
            showIf: (): boolean => true,
            name: "mercuryAndMercuryWasteHandling",
            description: "Does your company deal with mercury and mercury waste as part of its business model?",
            component: "YesNoFormField",
            required: false,
            label: "Mercury And Mercury Waste Handling",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAndMercuryWasteHandling ===
              "Yes",
            name: "mercuryAndMercuryWasteHandlingPolicy",
            description:
              "Does your company have a policy for handling these materials? (If yes, please share the policy with us)",
            component: "YesNoFormField",
            required: false,
            label: "Mercury And Mercury Waste Handling Policy",
          },
          {
            showIf: (): boolean => true,
            name: "mercuryAddedProductsHandling",
            description:
              "Are you involved in the manufacture, use, treatment, and/or import or export of products containing mercury?",
            component: "YesNoFormField",
            required: false,
            label: "Mercury Added-Products Handling",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling ===
              "Yes",
            name: "mercuryAddedProductsHandlingRiskOfExposure",
            description:
              "Is there a risk of manufacturing, importing or exporting products containing mercury that are not subject to the exemption under Annex A Part 1 of the Minamata Convention (BGBI. 2017 II p.610, 611)?",
            component: "YesNoFormField",
            required: false,
            label: "Mercury Added-Products Handling Risk Of Exposure",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling ===
              "Yes",
            name: "mercuryAddedProductsHandlingRiskOfDisposal",
            description:
              "If there are products that are only contaminated with mercury: Is there a risk within your company that mercury waste will be disposed of contrary to the provisions of Article 11 of the Minamata Agreement (BGBI. 2017 II p. 610, 611)?",
            component: "YesNoFormField",
            required: false,
            label: "Mercury Added-Products Handling Risk Of Disposal",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling ===
              "Yes",
            name: "mercuryAndMercuryCompoundsProductionAndUse",
            description: "Are there manufacturing processes in your company that use mercury and/or mercury compounds?",
            component: "YesNoFormField",
            required: false,
            label: "Mercury and Mercury Compounds Production and Use",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling ===
              "Yes",
            name: "mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure",
            description:
              "Is there a risk in your company that mercury and/or mercury compounds used in manufacturing processes, that are regulated according to Article 5 Paragraph 2 and Annex B of the Minamata Agreement (Federal Law Gazette 2017 II p. 610, 611), have already exceeded the specified phase-out date and are therefore prohibited?",
            component: "YesNoFormField",
            required: false,
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
            description:
              "Do you use and/or produce persistent organic pollutants (POPs), i.e. chemical compounds that break down and/or transform very slowly in the environment?",
            component: "YesNoFormField",
            required: false,
            label: "Persistent Organic Pollutants Production and Use",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse === "Yes",
            name: "persistentOrganicPollutantsUsed",
            description: "If yes, which organic pollutants are used?",
            component: "InputTextFormField",
            required: false,
            label: "Persistent Organic Pollutants Used",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse === "Yes",
            name: "persistentOrganicPollutantsProductionAndUseRiskOfExposure",
            description:
              "Is there a risk in your company that these organic pollutants fall under Article 3 paragraph 1 letter a and Annex A of the Stockholm Convention of 23 May 2001 on persistent organic pollutants (Federal Law Gazette 2002 II p. 803, 804) (POPs Convention) and are therefore banned?",
            component: "YesNoFormField",
            required: false,
            label: "Persistent Organic Pollutants Production And Use Risk Of Exposure",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse === "Yes",
            name: "persistentOrganicPollutantsProductionAndUseRiskOfDisposal",
            description:
              "In relation to the waste of these pollutants, is there a risk that they will be subject to the rules laid down in the applicable legal system in accordance with the provisions of Article 6(1)(d)(i) and (ii) of the POP -Convention (BGBI. 2002 II p. 803, 804) and will O not be handled / collected / stored / transported in an environmentally sound manner O not be disposed of in an environmentally friendly manner, i.e. if possible disposed of in such a way that the persistent organic pollutants contained therein are destroyed or irreversibly converted?",
            component: "YesNoFormField",
            required: false,
            label: "Persistent Organic Pollutants Production And Use Risk Of Disposal",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse === "Yes",
            name: "legalRestrictedWasteProcesses",
            description:
              "Does your company have processes or measures in place to ensure the lawful handling of (hazardous) waste?",
            component: "YesNoFormField",
            required: false,
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
            description:
              "Is there a risk in your company that: hazardous waste within the meaning of the Basel Convention (Article 1 Paragraph 1, BGBI. 1994 II p. 2703, 2704) or other wastes that require special consideration (household waste, residues from incineration of household waste) (Article 1(2)) are transported across borders?",
            component: "YesNoFormField",
            required: false,
            label: "Persistent Organic Pollutants Production And Use Transboundary Movements",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention
                ?.persistentOrganicPollutantsProductionAndUseTransboundaryMovements === "Yes",
            name: "persistentOrganicPollutantsProductionAndUseRiskForImportingState",
            description:
              "Are these wastes transported or shipped to an importing State that is subject to the Basel Convention and O has not given its written consent to the specific import (if that importing State has not prohibited the importation of that hazardous waste) (Article 4(1)(c)) O is not a contracting party (Article 4, paragraph 5) O does not treat waste in an environmentally friendly manner because it does not have the appropriate capacity for environmentally friendly disposal and cannot guarantee this elsewhere either (Article 4 paragraph 8 sentence 1) or O transported by a Party that has banned the import of such hazardous and other wastes (Article 4(1)(b) Basel Convention)?  (The term importing state includes: a contracting party to which a transboundary shipment of hazardous waste or other waste is planned for the purpose of disposal or for the purpose of loading prior to disposal in an area not under the sovereignty of a state. (Article 2 No. 11)",
            component: "YesNoFormField",
            required: false,
            label: "Persistent Organic Pollutants Production and Use Risk for Importing State",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention
                ?.persistentOrganicPollutantsProductionAndUseTransboundaryMovements === "Yes",
            name: "hazardousWasteTransboundaryMovementsLocatedOecdEuLiechtenstein",
            description: "Is your company based in a country that is within the OECD, EU, or Liechtenstein?",
            component: "YesNoFormField",
            required: false,
            label: "Hazardous Waste Transboundary Movements Located OECD, EU, Liechtenstein",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention
                ?.persistentOrganicPollutantsProductionAndUseTransboundaryMovements === "Yes",
            name: "hazardousWasteTransboundaryMovementsOutsideOecdEuLiechtenstein",
            description:
              "Is there a risk in your company that hazardous waste is transported to a country that is outside the OECD, EU / Liechtenstein?",
            component: "YesNoFormField",
            required: false,
            label: "Hazardous Waste Transboundary Movements Outside OECD, EU, Liechtenstein",
          },
          {
            showIf: (): boolean => true,
            name: "hazardousWasteDisposal",
            description:
              "Do you dispose of hazardous waste within the meaning of the Basel Convention (Article 1 Paragraph 1, BGBI. 1994 II p. 2703, 2704)?",
            component: "YesNoFormField",
            required: false,
            label: "Hazardous Waste Disposal",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention?.hazardousWasteDisposal === "Yes",
            name: "hazardousWasteDisposalRiskOfImport",
            description:
              "Are you at risk of having this waste imported from a country that is not a party to the Basel Convention?",
            component: "YesNoFormField",
            required: false,
            label: "Hazardous Waste Disposal Risk Of Import",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention?.hazardousWasteDisposal === "Yes",
            name: "hazardousAndOtherWasteImport",
            description:
              "Do you import other wastes that require special consideration (household waste, residues from incineration of household waste) (Article 1(2))?",
            component: "YesNoFormField",
            required: false,
            label: "Hazardous And Other Waste Import",
          },
        ],
      },
    ],
  },
] as Array<Category>;
