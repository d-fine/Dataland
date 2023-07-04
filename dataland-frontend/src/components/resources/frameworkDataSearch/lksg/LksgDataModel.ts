import { LksgData } from "@clients/backend";
import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";
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
              "Is your head office, administrative headquarters, registered office or subsidiary located in Germany?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "groupOfCompanies",
            label: "Group of Companies",
            description: "Do you belong to a group of companies?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "groupOfCompaniesName",
            label: "Group of Companies Name",
            description: "What is the group of companies called?",
            component: "InputTextFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean => dataModel?.general?.masterData?.groupOfCompanies === "Yes",
          },
          {
            name: "industry",
            label: "Industry",
            description: "In which industry is your company primarily active?",
            component: "NaceCodeFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "numberOfEmployees",
            label: "Number of Employees",
            description:
              "What is the total number of employees (including temporary workers with assignment duration >6 months)?",
            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "min:0",
          },
          {
            name: "seasonalOrMigrantWorkers",
            label: "Seasonal or Migrant Workers",
            description: "Do you employ seasonal or migrant workers?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "shareOfTemporaryWorkers",
            label: "Share of Temporary Workers",
            description: "What is the share of temporary workers vs total number of employees in the company?",
            component: "RadioButtonsFormField",
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
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "totalRevenueCurrency",
            label: "Total Revenue Currency",
            description: "The 3-letter code (ISO 4217) representing the currency used for the total revenue",
            component: "SingleSelectFormField",
            options: getDataset(DropdownDatasetIdentifier.CurrencyCodes),
            required: false,
            showIf: (): boolean => true,
            placeholder: "Select Currency",
          },
          {
            name: "totalRevenue",
            label: "Total Revenue",
            description: "Total revenue p. a.",
            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "fixedAndWorkingCapital",
            label: "Fixed and Working Capital",
            description: "What is your fixed and working capital? (only for own operations)",
            component: "NumberFormField",
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
            certificateRequiredIfYes: false,
          },
          {
            name: "capacity",
            label: "Capacity",
            description: "If yes, what is your production capacity per year, e.g. units/year?",
            component: "InputTextFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.general?.productionSpecific?.manufacturingCompany === "Yes",
          },
          {
            name: "isContractProcessing",
            label: "Is Contract Processing",
            description: "Is production done via subcontracting?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.general?.productionSpecific?.manufacturingCompany === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "subcontractingCompaniesCountries",
            label: "Subcontracting Companies Countries",
            description: "In which countries do the subcontracting companies operate?",
            component: "MultiSelectFormField",
            options: getDataset(DropdownDatasetIdentifier.CountryCodes),
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.general?.productionSpecific?.isContractProcessing === "Yes",
            placeholder: "Select Country",
          },
          {
            name: "subcontractingCompaniesIndustries",
            label: "Subcontracting Companies Industries",
            description: "In which industries do the subcontracting companies operate?",
            component: "NaceCodeFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.general?.productionSpecific?.isContractProcessing === "Yes",
          },
          {
            name: "productionSites",
            label: "Production Sites",
            description: "Do you have production sites in your company?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.general?.productionSpecific?.manufacturingCompany === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "listOfProductionSites",
            label: "List Of Production Sites",
            description: "Please list the production sites in your company.",
            component: "ProductionSitesFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean => dataModel?.general?.productionSpecific?.productionSites === "Yes",
          },
          {
            name: "market",
            label: "Market",
            description: "Does your business focus predominantly on national or international markets?",
            component: "RadioButtonsFormField",
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
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.general?.productionSpecific?.manufacturingCompany === "Yes",
          },
          {
            name: "specificProcurement",
            label: "Specific Procurement",
            description:
              "Does your company have specific procurement models such as: short-lived and changing business relationships, or high price pressure or tightly timed or short-term adjusted delivery deadlines and conditions with suppliers",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.general?.productionSpecific?.manufacturingCompany === "Yes",
            certificateRequiredIfYes: false,
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
              "Please give an overview of the most important products or services in terms of sales that your company manufactures and/or distributes or offers (own operations)",
            component: "MostImportantProductsFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.general?.productionSpecific?.manufacturingCompany === "Yes",
          },
          {
            name: "procurementCategories",
            label: "Procurement Categories",
            description: "Name their procurement categories (products, raw materials, services) (own operations)",
            component: "ProcurementCategoriesFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.general?.productionSpecific?.manufacturingCompany === "Yes",
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
            name: "adequateAndEffectiveRiskManagementSystem",
            label: "Adequate and Effective Risk Management System",
            description: "Does your company have an adequate and effective Risk Management system?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "riskManagementSystemFiscalYear",
            label: "Risk Management System Fiscal Year",
            description: "Did you perform a risk analysis as part of risk management in this fiscal year?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.adequateAndEffectiveRiskManagementSystem === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "riskManagementSystemRisks",
            label: "Risk Management System Risks",
            description: "Were risks identified during this period?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemFiscalYear === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "riskManagementSystemIdentifiedRisks",
            label: "Risk Management System Identified Risks",
            description: "Which risks were specifically identified in the risk analysis?",
            component: "InputTextFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemRisks === "Yes",
          },
          {
            name: "riskManagementSystemCounteract",
            label: "Risk Management System Counteract",
            description: "Have measures been defined to counteract these risks?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemRisks === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "riskManagementSystemMeasures",
            label: "Risk Management System Measures",
            description: "What measures have been applied to counteract the risks?",
            component: "InputTextFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemCounteract === "Yes",
          },
          {
            name: "riskManagementSystemResponsibility",
            label: "Risk Management System Responsibility",
            description:
              "Is the responsibility for the Risk Management in your company regulated, for example by appointing a human rights officer?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.adequateAndEffectiveRiskManagementSystem === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "environmentalManagementSystem",
            label: "Environmental Management System",
            description: "Is an environmental management system implemented in your company?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "environmentalManagementSystemInternationalCertification",
            label: "Environmental Management System International Certification",
            description: "Is the environmental management system internationally recognised and certified?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.environmentalManagementSystem === "Yes",
            certificateRequiredIfYes: true,
          },
          {
            name: "environmentalManagementSystemNationalCertification",
            label: "Environmental Management System National Certification",
            description: "Is the environmental management system nationally recognised and certified?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.environmentalManagementSystem === "Yes",
            certificateRequiredIfYes: true,
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
              "Has your company implemented a grievance mechanism (e.g., anonymous whistleblowing system) to protect human and environmental rights in your business?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "grievanceHandlingMechanismUsedForReporting",
            label: "Grievance Handling Mechanism Used For Reporting",
            description:
              "Can all affected stakeholders and rights holders, i.e. both internal (e.g. employees) and external stakeholders (e.g. suppliers and their employees, NGOs) use the grievance channel/whistleblowing system for reporting?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "grievanceMechanismInformationProvided",
            label: "Grievance Mechanism Information Provided",
            description:
              "Is information about the process provided in a way that is adapted to the context and target groups?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "grievanceMechanismSupportProvided",
            label: "Grievance Mechanism Support Provided",
            description: "Is the necessary support provided so that the target groups can actually use the procedure?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "grievanceMechanismAccessToExpertise",
            label: "Grievance Mechanism Access to Expertise",
            description:
              "Do the target groups have access to the expertise, advice and information that they need to participate in the grievance procedure in a fair, informed and respectful manner?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "grievanceMechanismComplaints",
            label: "Grievance Mechanism Complaints",
            description: "Have there been any complaints that have entered the system in the past?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "grievanceMechanismComplaintsNumber",
            label: "Grievance Mechanism Complaints Number",
            description: "How many complaints have been received?",
            component: "NumberFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaints === "Yes",
            validation: "min:0",
          },
          {
            name: "grievanceMechanismComplaintsReason",
            label: "Grievance Mechanism Complaints Reason",
            description: "What complaints have been received?",
            component: "InputTextFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaints === "Yes",
          },
          {
            name: "grievanceMechanismComplaintsAction",
            label: "Grievance Mechanism Complaints Action",
            description: "Have actions been taken to address the complaints?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaints === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "grievanceMechanismComplaintsActionUndertaken",
            label: "Grievance Mechanism Complaints Action undertaken",
            description: "What actions have been taken?",
            component: "InputTextFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaintsAction === "Yes",
          },
          {
            name: "grievanceMechanismPublicAccess",
            label: "Grievance Mechanism Public Access",
            description:
              "Does your company have publicly accessible rules that clearly describe the process for dealing with complaints?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "grievanceMechanismProtection",
            label: "Grievance Mechanism Protection",
            description: "Does the process effectively protect whistleblowers from disadvantage or punishment?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "grievanceMechanismDueDiligenceProcess",
            label: "Grievance Mechanism Due Diligence Process",
            description:
              "Do the findings from the processing of clues flow into the adjustment of your own due diligence processes?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            certificateRequiredIfYes: false,
          },
        ],
      },
      {
        name: "certificationsPoliciesAndResponsibilities",
        label: "Certifications, policies and responsibilities",
        fields: [
          {
            name: "sa8000Certification",
            label: "SA8000 Certification",
            description:
              "Is your company SA8000 certified? If yes, please share the certificate with us. (Corporate Social Responsibility)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
          },
          {
            name: "smetaSocialAuditConcept",
            label: "SMETA Social Audit Concept",
            description:
              "Does your company apply a social audit concept as defined by SMETA (Sedex Members Ethical Trade Audit)? (social audit)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "betterWorkProgramCertificate",
            label: "Better Work Program Certificate",
            description:
              "Do the production sites where the goods are produced participate in the BetterWork program? If yes, please share the certificate with us. (private label only)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
          },
          {
            name: "iso45001Certification",
            label: "ISO 45001 Certification",
            description:
              "Is your company ISO45001 certified? If yes, please share the certificate with us. (Management Systems of Occupational Health and Safety)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
          },
          {
            name: "iso14000Certification",
            label: "ISO 14000 Certification",
            description: "Is your company ISO14000 certified? If yes, please share the certificate with us.",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
          },
          {
            name: "emasCertification",
            label: "EMAS Certification",
            description:
              "Is your company certified according to EMAS? If yes, please share the certificate with us. (Voluntary environmental management)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
          },
          {
            name: "iso37001Certification",
            label: "ISO 37001 Certification",
            description:
              "Is your company ISO37001 certified? If yes, please share the certificate with us. (Anti-bribery management systems)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
          },
          {
            name: "iso37301Certification",
            label: "ISO37301 Certification",
            description:
              "Is your company ISO37301 certified? If yes, please share the certificate with us. (Compliance Management System)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
          },
          {
            name: "riskManagementSystemCertification",
            label: "Risk Management System Certification",
            description: "Is the Risk Management System internationally recognized and certified? (e.g.: ISO 31000)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
          },
          {
            name: "amforiBsciAuditReport",
            label: "amfori BSCI Audit Report",
            description:
              "Does your company have a current amfori BSCI audit report? If yes, please share the certificate with us.",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
          },
          {
            name: "responsibleBusinessAssociationCertification",
            label: "Responsible Business Association Certification",
            description:
              "Is your company Responsible Business Association (RBA) certified? If yes, please share the certificate with us. (Social Responsibility)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
          },
          {
            name: "fairLaborAssociationCertification",
            label: "Fair Labor Association Certification",
            description:
              "Is your company Fair Labor Association (FLA) certified? If yes, please share the certificate with us. (Adherence to international and national labor laws)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
          },
          {
            name: "additionalAudits",
            label: "Additional Audits",
            description:
              "Please list other (sector-specific) audits (if available) to which your company is certified.",
            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "codeOfConduct",
            label: "Code Of Conduct",
            description:
              "Has your company implemented and enforced (e.g., within the Code Of Conducts) internal behavioural guidelines that address the issues of human rights protection and respect for the environment?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "codeOfConductTraining",
            label: "Code Of Conduct Training",
            description:
              "Are your employees regularly made aware of the internal rules of conduct and trained on them?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.certificationsPoliciesAndResponsibilities?.codeOfConduct === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "supplierCodeOfConduct",
            label: "Supplier Code Of Conduct",
            description:
              "Does your company have a Supplier Code Of Conduct? (If yes, please share the Supplier Code of Conduct with us)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
          },
          {
            name: "policyStatement",
            label: "Policy Statement",
            description: "Does your company have a policy statement on its human rights strategy?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "humanRightsStrategy",
            label: "Human Rights Strategy",
            description:
              "In which relevant departments/business processes has the anchoring of the human rights strategy been ensured",
            component: "InputTextFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.certificationsPoliciesAndResponsibilities?.policyStatement === "Yes",
          },
          {
            name: "environmentalImpactPolicy",
            label: "Environmental Impact Policy",
            description:
              "Does your company have a Environmental Impact Policy? (If yes, please share the policy with us)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
          },
          {
            name: "fairWorkingConditionsPolicy",
            label: "Fair Working Conditions Policy",
            description:
              "Does your company have a Fair Working Conditions Policy? (If yes, please share the policy with us)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
          },
        ],
      },
      {
        name: "generalViolations",
        label: "General violations",
        fields: [
          {
            name: "responsibilitiesForFairWorkingConditions",
            label: "Responsibilities For Fair Working Conditions",
            description:
              "Has your company established, according to the nature and extent of the enterprise’s business activities, official responsibilities for the topic of fair working conditions?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "responsibilitiesForTheEnvironment",
            label: "Responsibilities For The Environment",
            description:
              "Has your company established, according to the nature and extent of the enterprise’s business activities, official responsibilities for the topic of the environment?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "responsibilitiesForOccupationalSafety",
            label: "Responsibilities For Occupational Safety",
            description:
              "Has your company established, according to the nature and extent of the enterprise’s business activities, official responsibilities for the topic of occupational safety?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "legalProceedings",
            label: "Legal Proceedings",
            description:
              "Has your company been involved in the last 5 years, in legal disputes (including currently ongoing disputes) with third parties regarding human rights and environmental violations?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "humanRightsViolation",
            label: "Human Rights Violation",
            description:
              "Have there been any violations of human rights or environmental aspects on your part in the last 5 years?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "humanRightsViolations",
            label: "Human Rights Violations",
            description: "What were the violations?",
            component: "InputTextFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.humanRightsViolation === "Yes",
          },
          {
            name: "humanRightsViolationAction",
            label: "Human Rights Violation Action",
            description: "Has action been taken to address the violations?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.humanRightsViolation === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "humanRightsViolationActionMeasures",
            label: "Human Rights Violation Action Measures",
            description: "What measures have been taken?",
            component: "InputTextFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.humanRightsViolationAction === "Yes",
          },
          {
            name: "highRiskCountriesRawMaterials",
            label: "High Risk Countries Raw Materials",
            description: "Do you source your raw materials from verified conflict or high-risk regions?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "highRiskCountriesRawMaterialsLocation",
            label: "High Risk Countries Raw Materials Location",
            description: "From which conflict/high-risk regions do you source your raw materials?",
            component: "MultiSelectFormField",
            options: getDataset(DropdownDatasetIdentifier.CountryCodes),
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.highRiskCountriesRawMaterials === "Yes",
            placeholder: "Select Country",
          },
          {
            name: "highRiskCountriesActivity",
            label: "High Risk Countries Activity",
            description:
              "Does your company have activities in countries where there are high risks for human rights and/or the environment?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "highRiskCountries",
            label: "High Risk Countries",
            description: "In which high risk countries does your company have activities?",
            component: "MultiSelectFormField",
            options: getDataset(DropdownDatasetIdentifier.CountryCodes),
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.highRiskCountriesActivity === "Yes",
            placeholder: "Select Country",
          },
          {
            name: "highRiskCountriesProcurement",
            label: "High Risk Countries Procurement",
            description:
              "Does your company procure from countries with high risks for human rights and/or the environment?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "highRiskCountriesProcurementName",
            label: "High Risk Countries Procurement Name",
            description: "From which high risk countries does your company procure from?",
            component: "MultiSelectFormField",
            options: getDataset(DropdownDatasetIdentifier.CountryCodes),
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.highRiskCountriesProcurement === "Yes",
            placeholder: "Select Country",
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
            name: "employeeUnder18",
            label: "Employee Under 18",
            description: "Does your company have employees under the age of 18?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "employeeUnder15",
            label: "Employee Under 15",
            description:
              "With regard to the place of employment and the applicable laws: do you employ school-age children or children under the age of 15 on a full-time basis?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.employeeUnder18 === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "employeeUnder18Apprentices",
            label: "Employee Under 18 Apprentices",
            description:
              "Are the employees under 18 years of age exclusively apprentices within the meaning of the locally applicable laws?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.employeeUnder18 === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "worstFormsOfChildLaborProhibition",
            label: "Worst Forms of Child Labor Prohibition",
            description:
              "Is the prohibition of the worst forms of child labor ensured in your company? This includes: all forms of slavery or practices similar to slavery, the use, procuring or offering of a child for prostitution, the production of pornography or pornographic performances, the use, procuring or offering of a child for illicit activities, in particular for the production or trafficking of drugs, work which, by its nature or the circumstances in which it is performed, is likely to be harmful to the health, safety, or morals of children",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.employeeUnder18 === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "worstFormsOfChildLaborForms",
            label: "Worst Forms of Child Labor Forms",
            description: "What forms of worst forms of child labor have been identified?",
            component: "InputTextFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.worstFormsOfChildLaborProhibition === "Yes",
          },
          {
            name: "employmentUnderLocalMinimumAgePrevention",
            label: "Employment Under Local Minimum Age Prevention",
            description:
              "Does your company take measures to prevent the employment of children under the local minimum age?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "employmentUnderLocalMinimumAgePreventionEmploymentContracts",
            label: "Employment Under Local Minimum Age Prevention Employment Contracts",
            description:
              "Is a formal recruitment process including the conclusion of employment contracts such a measure?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "employmentUnderLocalMinimumAgePreventionJobDescription",
            label: "Employment Under Local Minimum Age Prevention Job Description",
            description:
              "Is a clear job description for employees under the local minimum age in the hiring process and employment contracts such a measure? (group of people between 15 and 18 years)",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "employmentUnderLocalMinimumAgePreventionIdentityDocuments",
            label: "Employment Under Local Minimum Age Prevention Identity Documents",
            description:
              "Is the control of official documents (e.g. identity documents and certificates) such a measure?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "employmentUnderLocalMinimumAgePreventionTraining",
            label: "Employment Under Local Minimum Age Prevention Training",
            description:
              "Is raising the awareness of staff involved in the recruitment process through training such a measure?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge",
            label: "Employment Under Local Minimum Age Prevention Checking Of Legal Minimum Age",
            description: "Is the regular checking of the legal minimum age such a measure?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "childLaborMeasures",
            label: "Child Labor Measures",
            description:
              "Please list any other measures (if available) you take to prevent the employment of children under the locally applicable minimum age?",
            component: "InputTextFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
          },
          {
            name: "childLaborPreventionPolicy",
            label: "Child Labor Prevention Policy",
            description:
              "Does your company have a policy to prevent child labor? (If yes, please share the policy with us)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
          },
        ],
      },
      {
        name: "forcedLaborSlavery",
        label: "Forced labor, slavery",
        fields: [
          {
            name: "forcedLaborAndSlaveryPrevention",
            label: "Forced Labor And Slavery Prevention",
            description:
              "Does your company have practices that lead or may lead to forced labor and/or slavery?  The following are included: Creating unacceptable working and living conditions by working in hazardous conditions or in unacceptable accommodations provided by the employer; Excessive levels of overtime; Use of intimidation, threats, and/or punishment; Other types of forced labor (e.g. debt bondage, human trafficking)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "forcedLaborAndSlaveryPreventionPractices",
            label: "Forced Labor And Slavery Prevention Practices",
            description: "Please specify which of the points exist?",
            component: "InputTextFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPrevention === "Yes",
          },
          {
            name: "forcedLaborAndSlaveryPreventionMeasures",
            label: "Forced Labor And Slavery Prevention Measures",
            description: "Does your company take measures to prevent forced labor and slavery?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "forcedLaborAndSlaveryPreventionEmploymentContracts",
            label: "Forced Labor And Slavery Prevention Employment Contracts",
            description:
              "Is a formal hiring process, including employment contracts in the employee's local language, with appropriate wage and termination clauses, such a measure?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "forcedLaborAndSlaveryPreventionIdentityDocuments",
            label: "Forced Labor And Slavery Prevention Identity Documents",
            description: "Is a ban on the retention of identity documents such a measure?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "forcedLaborAndSlaveryPreventionFreeMovement",
            label: "Forced Labor And Slavery Prevention Free Movement",
            description:
              "Is ensuring the free movement of employees through doors and windows that can be opened to leave the building/premises of your company at any time such a measure?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "forcedLaborAndSlaveryPreventionProvisionSocialRoomsAndToilets",
            label: "Forced Labor And Slavery Prevention Provision Social Rooms and Toilets",
            description: "Is the provision of social rooms and toilets that can be visited at any time such a measure?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "forcedLaborAndSlaveryPreventionTraining",
            label: "Forced Labor And Slavery Prevention Training",
            description:
              "Is raising the awareness of staff involved in the recruitment process through training such a measure?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "forcedLaborAndSlaveryPreventionMeasuresOther",
            label: "Forced Labor and Slavery Prevention Measures (Other)",
            description: "Please list other measures (if available) you take to prevent forced labor and slavery?",
            component: "InputTextFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
          },
          {
            name: "forcedLaborPreventionPolicy",
            label: "Forced Labor Prevention Policy",
            description:
              "Does your company have a policy to prevent forced labor? (If yes, please share the policy with us)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
          },
        ],
      },
      {
        name: "withholdingAdequateWages",
        label: "Withholding adequate wages",
        fields: [
          {
            name: "adequateWage",
            label: "Adequate Wage",
            description: "Is your company currently withholding adequate wages (adequate in the sense of local laws)?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "adequateWageBeingWithheld",
            label: "Adequate Wage being withheld",
            description: "Are any measures taken in your company to prevent that adequate wages being withheld?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "documentedWorkingHoursAndWages",
            label: "Documented Working Hours And Wages",
            description: "Does your company document the working hours and wages of its employees?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequateWageBeingWithheld === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "adequateLivingWage",
            label: "Adequate Living Wage",
            description:
              "Does your company pay employees adequate wages? (the appropriate wage is at least the minimum wage set by the applicable law and is otherwise measured according to the law of the place of employment).",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequateWageBeingWithheld === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "regularWagesProcessFlow",
            label: "Regular Wages Process Flow",
            description:
              "Has your company implemented the payment of wages through standardised and regular process flows?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequateWageBeingWithheld === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "fixedHourlyWages",
            label: "Fixed Hourly Wages",
            description: "Do fixed hourly wages exist in your company?",
            component: "YesNoNaFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequateWageBeingWithheld === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "fixedPieceworkWages",
            label: "Fixed Piecework Wages",
            description: "Does your company have fixed piecework wages?",
            component: "YesNoNaFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequateWageBeingWithheld === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "adequateWageMeasures",
            label: "Adequate Wage Measures",
            description:
              "Please list other measures (if available) you take to prevent the withholding adequate wages?",
            component: "InputTextFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequateWageBeingWithheld === "Yes",
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
            description: "Do your employees perform low-skill manual work or repetitive manual work?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "hazardousMachines",
            label: "Hazardous Machines",
            description: "Are hazardous machines used in the manufacture of (preliminary) products?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "oshPolicy",
            label: "OSH Policy",
            description:
              "Has your company implemented and enforced a formal occupational health and safety (OSH) policy that complies with local laws, industry requirements and international standards?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "oshPolicyPersonalProtectiveEquipment",
            label: "OSH Policy Personal Protective Equipment",
            description: "Is the subject area of personal protective equipment addressed by this OSH Directive?",
            component: "YesNoNaFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "oshPolicyMachineSafety",
            label: "OSH Policy Machine Safety",
            description: "Is the subject area of machine safety addressed by this OSH Directive?",
            component: "YesNoNaFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "oshPolicyDisasterBehaviouralResponse",
            label: "OSH Policy Disaster Behavioural Response",
            description: "Is the subject area of behaviour in the event of a disaster addressed by this OSH Directive?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "oshPolicyAccidentsBehaviouralResponse",
            label: "OSH Policy Accidents Behavioural Response",
            description:
              "Is the subject area of behaviour in the event of and prevention of accidents addressed by this OSH Directive?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "oshPolicyWorkplaceErgonomics",
            label: "OSH Policy Workplace Ergonomics",
            description: "Is the subject area of workplace ergonomics addressed by this OSH Directive?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "oshPolicyAccessToWork",
            label: "OSH Policy Access to work",
            description: "Is access to the work secluded/is the workplace difficult to access?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "oshPolicyHandlingChemicalsAndOtherHazardousSubstances",
            label: "OSH Policy Handling Chemicals And Other Hazardous Substances",
            description:
              "Is the subject area of handling chemical, physical or biological substances addressed by this OSH Directive?",
            component: "YesNoNaFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "oshPolicyFireProtection",
            label: "OSH Policy Fire Protection",
            description: "Is the subject area of fire protection addressed by this OSH Directive?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "oshPolicyWorkingHours",
            label: "OSH Policy Working Hours",
            description:
              "Is the subject area of regulation of working hours, overtime and rest breaks be addressed by this OSH Directive?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "oshPolicyTrainingAddressed",
            label: "OSH Policy Training Addressed",
            description:
              "Is the subject area of training and instruction of employees with regard to occupational health and safety addressed by this OSH Directive?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "oshPolicyTraining",
            label: "OSH Policy Training",
            description: "Are your employees regularly made aware of the OSH Directive and trained on them?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "oshManagementSystem",
            label: "OSH Management System",
            description: "Is an occupational health and safety management system implemented in your company?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "oshManagementSystemInternationalCertification",
            label: "OSH Management System International Certification",
            description: "Is the OSH management system internationally recognised and certified?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshManagementSystem === "Yes",
            certificateRequiredIfYes: true,
          },
          {
            name: "oshManagementSystemNationalCertification",
            label: "OSH Management System National Certification",
            description: "Is the OSH management system nationally recognised and certified?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshManagementSystem === "Yes",
            certificateRequiredIfYes: true,
          },
          {
            name: "workplaceAccidentsUnder10",
            label: "Workplace Accidents Under 10",
            description:
              "Is the number of incidents in which employees suffered work-related injuries with serious consequences less than 10 in the past fiscal year?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "oshTraining",
            label: "OSH Training",
            description:
              "Has your company introduced mandatory offers and training for employees to improve occupational safety?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "healthAndSafetyPolicy",
            label: "Health And Safety Policy",
            description: "Does your company have a Health And Safety Policy? (If yes, please share the policy with us)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
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
            certificateRequiredIfYes: false,
          },
          {
            name: "representedEmployees",
            label: "Represented Employees",
            description: "What is your percentage of employees who are represented by trade unions?",
            component: "PercentageFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForFreedomOfAssociation?.freedomOfAssociation === "Yes",
          },
          {
            name: "discriminationForTradeUnionMembers",
            label: "Discrimination For Trade Union Members",
            description:
              "Does your company ensure that no discrimination is practised or other consequences taken against employees in the event of the formation, joining and membership of a trade union?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForFreedomOfAssociation?.freedomOfAssociation === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "freedomOfOperationForTradeUnion",
            label: "Freedom Of Operation For Trade Union",
            description:
              "Does your company ensure that trade unions are free to operate in accordance with the law of the place of employment?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForFreedomOfAssociation?.freedomOfAssociation === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "freedomOfAssociationTraining",
            label: "Freedom Of Association Training",
            description:
              "Do employees receive information about their rights as part of training and/or intranet, notices or company brochures?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "worksCouncil",
            label: "Works Council",
            description:
              "Does your company have a works council or an employee representative committee (if it can be set up in your company in accordance with local applicable legal provisions)?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
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
            certificateRequiredIfYes: false,
          },
          {
            name: "diversityAndInclusionRole",
            label: "Diversity And Inclusion Role",
            description:
              "Is a member of your company's management responsible for promoting diversity in the workforce and among business partners?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "preventionOfMistreatments",
            label: "Prevention Of Mistreatments",
            description:
              "Does your company's management promote a work environment free from physical, sexual, mental and verbal abuse, threats or other forms of mistreatment? (e.g. diversity program)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "equalOpportunitiesOfficer",
            label: "Equal Opportunities Officer",
            description: "Is an equal opportunities officer (or similar function) implemented in your company?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "fairAndEthicalRecruitmentPolicy",
            label: "Fair And Ethical Recruitment Policy",
            description:
              "Does your company have a Fair And Ethical Recruitment Policy? (If yes, please share the policy with us)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
          },
          {
            name: "equalOpportunitiesAndNonDiscriminationPolicy",
            label: "Equal Opportunities And Non-discrimination Policy",
            description:
              "Does your company have a Equal Opportunities And Non-discrimination Policy? (If yes, please share the policy with us)",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
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
            certificateRequiredIfYes: false,
          },
          {
            name: "soilDegradation",
            label: "Soil Degradation",
            description:
              "Does your company have measures in place to prevent the degradation of soil structure caused by the use of heavy machinery?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "soilErosion",
            label: "Soil Erosion",
            description:
              "Does your company have measures in place to prevent soil erosion caused by deforestation or overgrazing?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "soilBornDiseases",
            label: "Soil Born Diseases",
            description:
              "Does your company have measures in place to prevent the development of soil-borne diseases and pests and to maintain soil fertility?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "soilContamination",
            label: "Soil Contamination",
            description:
              "Does your company have measures in place to prevent soil contamination by antibiotics and toxins?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "soilSalinisation",
            label: "Soil Salinisation",
            description: "Does your company have measures in place to prevent soil salinisation?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "harmfulWaterPollution",
            label: "Harmful Water Pollution",
            description: "Is there a risk of your company causing harmful water pollution?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "fertilisersOrPollutants",
            label: "Fertilisers Or Pollutants",
            description: "Does your company use fertilisers or pollutants such as chemicals or heavy metals?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulWaterPollution === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "wasteWaterFiltration",
            label: "Waste Water Filtration",
            description: "Does your company have filtration systems for the waste water?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulWaterPollution === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "harmfulAirPollution",
            label: "Harmful Air Pollution",
            description: "Is there a risk of harmful air pollution in your company?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "airFiltration",
            label: "Air Filtration",
            description: "Does your company have air filtration systems?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulAirPollution === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "harmfulNoiseEmission",
            label: "Harmful Noise Emission",
            description: "Is there a risk of harmful noise emission in your company?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "reduceNoiseEmissions",
            label: "Reduce Noise Emissions",
            description: "Has your company implemented structural measures to reduce noise emissions?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulNoiseEmission === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "excessiveWaterConsumption",
            label: "Excessive Water Consumption",
            description: "Is there a risk of excessive water consumption in your company?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "waterSavingMeasures",
            label: "Water Saving Measures",
            description: "Do you take water-saving measures in your companies?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.excessiveWaterConsumption === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "waterSavingMeasuresName",
            label: "Water Saving Measures Name",
            description: "If yes, which ones?",
            component: "InputTextFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.waterSavingMeasures === "Yes",
          },
          {
            name: "pipeMaintaining",
            label: "Pipe Maintaining",
            description: "Are water pipes regularly checked and maintained?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.excessiveWaterConsumption === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "waterSources",
            label: "Water Sources",
            description:
              "Does your company use water sources that are important for the local population or local agriculture?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.excessiveWaterConsumption === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "contaminationMeasures",
            label: "Contamination Measures",
            description:
              "Please list any other measures (if available) you are taking to prevent the risk of harmful soil change, water pollution, air pollution, harmful noise emission or excessive water consumption that: O significantly affects the natural basis for food preservation and production O denies a person access to safe drinking water O impedes or destroys a person's access to sanitary facilities, or O harms the health of any person",
            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
      {
        name: "unlawfulEvictionDeprivationOfLandForestAndWater",
        label: "Unlawful eviction/deprivation of land, forest and water",
        fields: [
          {
            name: "unlawfulEvictionAndTakingOfLand",
            label: "Unlawful Eviction And Taking Of Land",
            description:
              "Is your company, as a result of the acquisition, development and/or other use of land, forests and/or bodies of water, which secures a person's livelihood, at risk of \n O an unlawful eviction\n O carrying out an unlawful taking of land, forests and/or water?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "unlawfulEvictionAndTakingOfLandRisk",
            label: "Unlawful Eviction And Taking Of Land Risk",
            description: "If so, what exactly is the risk?",
            component: "InputTextFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.unlawfulEvictionDeprivationOfLandForestAndWater?.unlawfulEvictionAndTakingOfLand ===
              "Yes",
          },
          {
            name: "unlawfulEvictionAndTakingOfLandStrategies",
            label: "Unlawful Eviction And Taking Of Land Strategies",
            description:
              "Has your company developed and implemented strategies that avoid, reduce, mitigate or remedy direct and indirect negative impacts on the land and natural resources of indigenous peoples and local communities?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "unlawfulEvictionAndTakingOfLandStrategiesName",
            label: "Unlawful Eviction And Taking Of Land Strategies Name",
            description: "If yes, which ones?",
            component: "InputTextFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.unlawfulEvictionDeprivationOfLandForestAndWater
                ?.unlawfulEvictionAndTakingOfLandStrategies === "Yes",
          },
          {
            name: "voluntaryGuidelinesOnTheResponsibleGovernanceOfTenure",
            label: "Voluntary Guidelines on the Responsible Governance of Tenure",
            description:
              "Have you implemented the Voluntary Guidelines on the Responsible Governance of Tenure in your company?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
        ],
      },
      {
        name: "useOfPrivatePublicSecurityForcesWithDisregardForHumanRights",
        label: "Use of private/public security forces with disregard for human rights",
        fields: [
          {
            name: "useOfPrivatePublicSecurityForces",
            label: "Use Of Private Public Security Forces",
            description:
              "Does your company use private and/or public security forces to protect company projects or similar?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights",
            label: "Use Of Private Public Security Forces And Risk Of Violation Of Human Rights",
            description:
              "Does your company have measures in place to prevent security forces during an operation from O violate the prohibition of torture and/or cruel, inhuman and/or degrading treatment O damages life or limb O impairs the right to organize and the freedom of association?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForces === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "instructionOfSecurityForces",
            label: "Instruction Of Security Forces",
            description: "Is an adequate instruction of the security forces such a measure?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "humanRightsTraining",
            label: "Human Rights Training",
            description: "Is training on human rights such a measure?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "stateSecurityForces",
            label: "State Security Forces",
            description:
              "(Only in the case of state security forces) Before the security forces were commissioned, was it checked whether serious human rights violations by these units had already been documented?",
            component: "YesNoNaFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "privateSecurityForces",
            label: "Private Security Forces",
            description:
              "(Only in the case of private security forces) Have the contractual relationships with the security guards been designed in such a way that they comply with the applicable legal framework?",
            component: "YesNoNaFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "useOfPrivatePublicSecurityForcesMeasures",
            label: "Use Of Private Public Security Forces Measures",
            description:
              "Please list any other measures (if available) you are taking to prevent the use of private/public security forces in violation of human rights?",
            component: "InputTextFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
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
            name: "mercuryAndMercuryWasteHandling",
            label: "Mercury And Mercury Waste Handling",
            description: "Does your company deal with mercury and mercury waste as part of its business model?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "mercuryAndMercuryWasteHandlingPolicy",
            label: "Mercury And Mercury Waste Handling Policy",
            description:
              "Does your company have a policy for handling these materials? (If yes, please share the policy with us)",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAndMercuryWasteHandling ===
              "Yes",
            certificateRequiredIfYes: true,
          },
          {
            name: "mercuryAddedProductsHandling",
            label: "Mercury Added-Products Handling",
            description:
              "Are you involved in the manufacture, use, treatment, and/or import or export of products containing mercury?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "mercuryAddedProductsHandlingRiskOfExposure",
            label: "Mercury Added-Products Handling Risk Of Exposure",
            description:
              "Is there a risk of manufacturing, importing or exporting products containing mercury that are not subject to the exemption under Annex A Part 1 of the Minamata Convention (BGBI. 2017 II p.610, 611)?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling ===
              "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "mercuryAddedProductsHandlingRiskOfDisposal",
            label: "Mercury Added-Products Handling Risk Of Disposal",
            description:
              "If there are products that are only contaminated with mercury: Is there a risk within your company that mercury waste will be disposed of contrary to the provisions of Article 11 of the Minamata Agreement (BGBI. 2017 II p. 610, 611)?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling ===
              "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "mercuryAndMercuryCompoundsProductionAndUse",
            label: "Mercury and Mercury Compounds Production and Use",
            description: "Are there manufacturing processes in your company that use mercury and/or mercury compounds?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling ===
              "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure",
            label: "Mercury And Mercury Compounds Production And Use Risk Of Exposure",
            description:
              "Is there a risk in your company that mercury and/or mercury compounds used in manufacturing processes, that are regulated according to Article 5 Paragraph 2 and Annex B of the Minamata Agreement (Federal Law Gazette 2017 II p. 610, 611), have already exceeded the specified phase-out date and are therefore prohibited?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling ===
              "Yes",
            certificateRequiredIfYes: false,
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
              "Do you use and/or produce persistent organic pollutants (POPs), i.e. chemical compounds that break down and/or transform very slowly in the environment?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "persistentOrganicPollutantsUsed",
            label: "Persistent Organic Pollutants Used",
            description: "If yes, which organic pollutants are used?",
            component: "InputTextFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse === "Yes",
          },
          {
            name: "persistentOrganicPollutantsProductionAndUseRiskOfExposure",
            label: "Persistent Organic Pollutants Production And Use Risk Of Exposure",
            description:
              "Is there a risk in your company that these organic pollutants fall under Article 3 paragraph 1 letter a and Annex A of the Stockholm Convention of 23 May 2001 on persistent organic pollutants (Federal Law Gazette 2002 II p. 803, 804) (POPs Convention) and are therefore banned?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "persistentOrganicPollutantsProductionAndUseRiskOfDisposal",
            label: "Persistent Organic Pollutants Production And Use Risk Of Disposal",
            description:
              "In relation to the waste of these pollutants, is there a risk that they will be subject to the rules laid down in the applicable legal system in accordance with the provisions of Article 6(1)(d)(i) and (ii) of the POP -Convention (BGBI. 2002 II p. 803, 804) and will O not be handled / collected / stored / transported in an environmentally sound manner O not be disposed of in an environmentally friendly manner, i.e. if possible disposed of in such a way that the persistent organic pollutants contained therein are destroyed or irreversibly converted?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "legalRestrictedWasteProcesses",
            label: "Legal Restricted Waste Processes",
            description:
              "Does your company have processes or measures in place to ensure the lawful handling of (hazardous) waste?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse === "Yes",
            certificateRequiredIfYes: false,
          },
        ],
      },
      {
        name: "exportImportOfHazardousWasteBaselConvention",
        label: "Export/import of hazardous waste (Basel Convention)",
        fields: [
          {
            name: "persistentOrganicPollutantsProductionAndUseTransboundaryMovements",
            label: "Persistent Organic Pollutants Production And Use Transboundary Movements",
            description:
              "Is there a risk in your company that: hazardous waste within the meaning of the Basel Convention (Article 1 Paragraph 1, BGBI. 1994 II p. 2703, 2704) or other wastes that require special consideration (household waste, residues from incineration of household waste) (Article 1(2)) are transported across borders?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "persistentOrganicPollutantsProductionAndUseRiskForImportingState",
            label: "Persistent Organic Pollutants Production and Use Risk for Importing State",
            description:
              "Are these wastes transported or shipped to an importing State that is subject to the Basel Convention and O has not given its written consent to the specific import (if that importing State has not prohibited the importation of that hazardous waste) (Article 4(1)(c)) O is not a contracting party (Article 4, paragraph 5) O does not treat waste in an environmentally friendly manner because it does not have the appropriate capacity for environmentally friendly disposal and cannot guarantee this elsewhere either (Article 4 paragraph 8 sentence 1) or O transported by a Party that has banned the import of such hazardous and other wastes (Article 4(1)(b) Basel Convention)?  (The term importing state includes: a contracting party to which a transboundary shipment of hazardous waste or other waste is planned for the purpose of disposal or for the purpose of loading prior to disposal in an area not under the sovereignty of a state. (Article 2 No. 11)",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention
                ?.persistentOrganicPollutantsProductionAndUseTransboundaryMovements === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "hazardousWasteTransboundaryMovementsLocatedOecdEuLiechtenstein",
            label: "Hazardous Waste Transboundary Movements Located OECD, EU, Liechtenstein",
            description: "Is your company based in a country that is within the OECD, EU, or Liechtenstein?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention
                ?.persistentOrganicPollutantsProductionAndUseTransboundaryMovements === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "hazardousWasteTransboundaryMovementsOutsideOecdEuLiechtenstein",
            label: "Hazardous Waste Transboundary Movements Outside OECD, EU, Liechtenstein",
            description:
              "Is there a risk in your company that hazardous waste is transported to a country that is outside the OECD, EU / Liechtenstein?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention
                ?.persistentOrganicPollutantsProductionAndUseTransboundaryMovements === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "hazardousWasteDisposal",
            label: "Hazardous Waste Disposal",
            description:
              "Do you dispose of hazardous waste within the meaning of the Basel Convention (Article 1 Paragraph 1, BGBI. 1994 II p. 2703, 2704)?",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "hazardousWasteDisposalRiskOfImport",
            label: "Hazardous Waste Disposal Risk Of Import",
            description:
              "Are you at risk of having this waste imported from a country that is not a party to the Basel Convention?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention?.hazardousWasteDisposal === "Yes",
            certificateRequiredIfYes: false,
          },
          {
            name: "hazardousAndOtherWasteImport",
            label: "Hazardous And Other Waste Import",
            description:
              "Do you import other wastes that require special consideration (household waste, residues from incineration of household waste) (Article 1(2))?",
            component: "YesNoFormField",
            required: false,
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention?.hazardousWasteDisposal === "Yes",
            certificateRequiredIfYes: false,
          },
        ],
      },
    ],
  },
] as Array<Category>;
