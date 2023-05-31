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
            showIf: (): boolean => true,
            name: "dataDate",
            description: "The date until when the information collected is valid",
            label: "Data Date",
            required: true,
            component: "DateFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "headOfficeInGermany",
            description:
              "Is your head office, administrative headquarters, registered office or subsidiary located in Germany?",
            label: "Head Office in Germany",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "groupOfCompanies",
            description: "Do you belong to a group of companies?",
            label: "Group of Companies",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (dataModel: LksgData): boolean => dataModel?.general?.masterData?.groupOfCompanies === "Yes",
            name: "groupOfCompaniesName",
            description: "What is the group of companies called?",
            label: "Group of Companies Name",
            required: true,
            component: "InputTextFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            validation: "required|length:1",
            name: "industry",
            description: "In which industry is your company primarily active?",
            label: "Industry",
            required: true,
            component: "NaceCodeFormField",
          },
          {
            showIf: (): boolean => true,
            name: "numberOfEmployees",
            description:
              "What is the total number of employees (including temporary workers with assignment duration >6 months)?",
            label: "Number of Employees",
            required: true,
            component: "NumberFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "seasonalOrMigrantWorkers",
            description: "Do you employ seasonal or migrant workers?",
            label: "Seasonal or Migrant Workers",
            required: true,
            component: "YesNoFormField",
            validation: "required",
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
            label: "Share of Temporary Workers",
            required: true,
            component: "RadioButtonsFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            options: getDataset(DropdownDatasetIdentifier.CurrencyCodes),
            placeholder: "Select Currency",
            validation: "required",
            name: "totalRevenueCurrency",
            description: "The 3-letter code (ISO 4217) representing the currency used for the total revenue",
            label: "Total Revenue Currency",
            required: true,
            component: "SingleSelectFormField",
          },
          {
            showIf: (): boolean => true,
            name: "totalRevenue",
            description: "Total revenue p. a.",
            label: "Total Revenue",
            required: true,
            component: "NumberFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            name: "fixedAndWorkingCapital",
            description: "What is your fixed and working capital? (only for own operations)",
            label: "Fixed and Working Capital",
            required: false,
            component: "NumberFormField",
          },
        ],
      },
      {
        name: "productionSpecific",
        label: "Production-specific",
        fields: [
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "manufacturingCompany",
            description: "Is your company a manufacturing company?",
            label: "Manufacturing Company",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.general?.productionSpecific?.manufacturingCompany === "Yes",
            name: "capacity",
            description: "If yes, what is your production capacity per year, e.g. units/year?",
            label: "Capacity",
            required: true,
            component: "InputTextFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "isContractProcessing",
            description: "Is production done via subcontracting?",
            label: "Is Contract Processing",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.general?.productionSpecific?.isContractProcessing === "Yes",
            options: getDataset(DropdownDatasetIdentifier.CountryCodes),
            placeholder: "Select Country",
            validation: "required|length:1",
            name: "subcontractingCompaniesCountries",
            description: "In which countries do the subcontracting companies operate?",
            label: "Subcontracting Companies Countries",
            required: true,
            component: "MultiSelectFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.general?.productionSpecific?.isContractProcessing === "Yes",
            validation: "required|length:1",
            name: "subcontractingCompaniesIndustries",
            description: "In which industries do the subcontracting companies operate?",
            label: "Subcontracting Companies Industries",
            required: true,
            component: "NaceCodeFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "productionSites",
            description: "Do you have production sites in your company?",
            label: "Production Sites",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (dataModel: LksgData): boolean => dataModel?.general?.productionSpecific?.productionSites === "Yes",
            name: "listOfProductionSites",
            description: "Please list the production sites in your company.",
            label: "List Of Production Sites",
            required: true,
            component: "ProductionSiteFormField",
            validation: "required",
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
            label: "Market",
            required: true,
            component: "RadioButtonsFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "specificProcurement",
            description:
              "Does your company have specific procurement models such as: short-lived and changing business relationships, or high price pressure or tightly timed or short-term adjusted delivery deadlines and conditions with suppliers",
            label: "Specific Procurement",
            required: true,
            component: "YesNoFormField",
            validation: "required",
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
            certificateRequiredIfYes: false,
            name: "adequateAndEffectiveRiskManagementSystem",
            description: "Does your company have an adequate and effective Risk Management system?",
            label: "Adequate and Effective Risk Management System",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.adequateAndEffectiveRiskManagementSystem === "Yes",
            certificateRequiredIfYes: false,
            name: "riskManagementSystemFiscalYear",
            description: "Did you perform a risk analysis as part of risk management in this fiscal year?",
            label: "Risk Management System Fiscal Year",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemFiscalYear === "Yes",
            certificateRequiredIfYes: false,
            name: "riskManagementSystemRisks",
            description: "Were risks identified during this period?",
            label: "Risk Management System Risks",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemRisks === "Yes",
            name: "riskManagementSystemIdentifiedRisks",
            description: "Which risks were specifically identified in the risk analysis?",
            label: "Risk Management System Identified Risks",
            required: false,
            component: "InputTextFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemRisks === "Yes",
            certificateRequiredIfYes: false,
            name: "riskManagementSystemCounteract",
            description: "Have measures been defined to counteract these risks?",
            label: "Risk Management System Counteract",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemCounteract === "Yes",
            name: "riskManagementSystemMeasures",
            description: "What measures have been applied to counteract the risks?",
            label: "Risk Management System Measures",
            required: false,
            component: "InputTextFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.adequateAndEffectiveRiskManagementSystem === "Yes",
            certificateRequiredIfYes: false,
            name: "riskManagementSystemResponsibility",
            description:
              "Is the responsibility for the Risk Management in your company regulated, for example by appointing a human rights officer?",
            label: "Risk Management System Responsibility",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "environmentalManagementSystem",
            description: "Is an environmental management system implemented in your company?",
            label: "Environmental Management System",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.environmentalManagementSystem === "Yes",
            certificateRequiredIfYes: true,
            name: "environmentalManagementSystemInternationalCertification",
            description: "Is the environmental management system internationally recognised and certified?",
            label: "Environmental Management System International Certification",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.riskManagementOwnOperations?.environmentalManagementSystem === "Yes",
            certificateRequiredIfYes: true,
            name: "environmentalManagementSystemNationalCertification",
            description: "Is the environmental management system nationally recognised and certified?",
            label: "Environmental Management System National Certification",
            required: false,
            component: "YesNoFormField",
          },
        ],
      },
      {
        name: "grievanceMechanismOwnOperations",
        label: "Grievance mechanism - Own Operations",
        fields: [
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "grievanceHandlingMechanism",
            description:
              "Has your company implemented a grievance mechanism (e.g., anonymous whistleblowing system) to protect human and environmental rights in your business?",
            label: "Grievance Handling Mechanism",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            certificateRequiredIfYes: false,
            name: "grievanceHandlingMechanismUsedForReporting",
            description:
              "Can all affected stakeholders and rights holders, i.e. both internal (e.g. employees) and external stakeholders (e.g. suppliers and their employees, NGOs) use the grievance channel/whistleblowing system for reporting?",
            label: "Grievance Handling Mechanism Used For Reporting",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            certificateRequiredIfYes: false,
            name: "grievanceMechanismInformationProvided",
            description:
              "Is information about the process provided in a way that is adapted to the context and target groups?",
            label: "Grievance Mechanism Information Provided",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            certificateRequiredIfYes: false,
            name: "grievanceMechanismSupportProvided",
            description: "Is the necessary support provided so that the target groups can actually use the procedure?",
            label: "Grievance Mechanism Support Provided",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            certificateRequiredIfYes: false,
            name: "grievanceMechanismAccessToExpertise",
            description:
              "Do the target groups have access to the expertise, advice and information that they need to participate in the grievance procedure in a fair, informed and respectful manner?",
            label: "Grievance Mechanism Access to Expertise",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            certificateRequiredIfYes: false,
            name: "grievanceMechanismComplaints",
            description: "Have there been any complaints that have entered the system in the past?",
            label: "Grievance Mechanism Complaints",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaints === "Yes",
            name: "grievanceMechanismComplaintsNumber",
            description: "How many complaints have been received?",
            label: "Grievance Mechanism Complaints Number",
            required: false,
            component: "NumberFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaints === "Yes",
            name: "grievanceMechanismComplaintsReason",
            description: "What complaints have been received?",
            label: "Grievance Mechanism Complaints Reason",
            required: false,
            component: "InputTextFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaints === "Yes",
            certificateRequiredIfYes: false,
            name: "grievanceMechanismComplaintsAction",
            description: "Have actions been taken to address the complaints?",
            label: "Grievance Mechanism Complaints Action",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaintsAction === "Yes",
            name: "grievanceMechanismComplaintsActionUndertaken",
            description: "What actions have been taken?",
            label: "Grievance Mechanism Complaints Action undertaken",
            required: false,
            component: "InputTextFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            certificateRequiredIfYes: false,
            name: "grievanceMechanismPublicAccess",
            description:
              "Does your company have publicly accessible rules that clearly describe the process for dealing with complaints?",
            label: "Grievance Mechanism Public Access",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            certificateRequiredIfYes: false,
            name: "grievanceMechanismProtection",
            description: "Does the process effectively protect whistleblowers from disadvantage or punishment?",
            label: "Grievance Mechanism Protection",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === "Yes",
            certificateRequiredIfYes: false,
            name: "grievanceMechanismDueDiligenceProcess",
            description:
              "Do the findings from the processing of clues flow into the adjustment of your own due diligence processes?",
            label: "Grievance Mechanism Due Diligence Process",
            required: false,
            component: "YesNoFormField",
          },
        ],
      },
      {
        name: "certificationsPoliciesAndResponsibilities",
        label: "Certifications, policies and responsibilities",
        fields: [
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
            name: "sa8000Certification",
            description:
              "Is your company SA8000 certified? If yes, please share the certificate with us. (Corporate Social Responsibility)",
            label: "SA8000 Certification",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "smetaSocialAuditConcept",
            description:
              "Does your company apply a social audit concept as defined by SMETA (Sedex Members Ethical Trade Audit)? (social audit)",
            label: "SMETA Social Audit Concept",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
            name: "betterWorkProgramCertificate",
            description:
              "Do the production sites where the goods are produced participate in the BetterWork program? If yes, please share the certificate with us. (private label only)",
            label: "Better Work Program Certificate",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
            name: "iso45001Certification",
            description:
              "Is your company ISO45001 certified? If yes, please share the certificate with us. (Management Systems of Occupational Health and Safety)",
            label: "ISO 45001 Certification",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
            name: "iso14000Certification",
            description: "Is your company ISO14000 certified? If yes, please share the certificate with us.",
            label: "ISO 14000 Certification",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
            name: "emasCertification",
            description:
              "Is your company certified according to EMAS? If yes, please share the certificate with us. (Voluntary environmental management)",
            label: "EMAS Certification",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
            name: "iso37001Certification",
            description:
              "Is your company ISO37001 certified? If yes, please share the certificate with us. (Anti-bribery management systems)",
            label: "ISO 37001 Certification",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
            name: "iso37301Certification",
            description:
              "Is your company ISO37301 certified? If yes, please share the certificate with us. (Compliance Management System)",
            label: "ISO37301 Certification",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
            name: "riskManagementSystemCertification",
            description: "Is the Risk Management System internationally recognized and certified? (e.g.: ISO 31000)",
            label: "Risk Management System Certification",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
            name: "amforiBsciAuditReport",
            description:
              "Does your company have a current amfori BSCI audit report? If yes, please share the certificate with us.",
            label: "amfori BSCI Audit Report",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
            name: "responsibleBusinessAssociationCertification",
            description:
              "Is your company Responsible Business Association (RBA) certified? If yes, please share the certificate with us. (Social Responsibility)",
            label: "Responsible Business Association Certification",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
            name: "fairLaborAssociationCertification",
            description:
              "Is your company Fair Labor Association (FLA) certified? If yes, please share the certificate with us. (Adherence to international and national labor laws)",
            label: "Fair Labor Association Certification",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            name: "additionalAudits",
            description:
              "Please list other (sector-specific) audits (if available) to which your company is certified.",
            label: "Additional Audits",
            required: false,
            component: "InputTextFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "codeOfConduct",
            description:
              "Has your company implemented and enforced (e.g., within the Code Of Conducts) internal behavioural guidelines that address the issues of human rights protection and respect for the environment?",
            label: "Code Of Conduct",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.certificationsPoliciesAndResponsibilities?.codeOfConduct === "Yes",
            certificateRequiredIfYes: false,
            name: "codeOfConductTraining",
            description:
              "Are your employees regularly made aware of the internal rules of conduct and trained on them?",
            label: "Code Of Conduct Training",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
            name: "supplierCodeOfConduct",
            description:
              "Does your company have a Supplier Code Of Conduct? (If yes, please share the Supplier Code of Conduct with us)",
            label: "Supplier Code Of Conduct",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "policyStatement",
            description: "Does your company have a policy statement on its human rights strategy?",
            label: "Policy Statement",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.certificationsPoliciesAndResponsibilities?.policyStatement === "Yes",
            name: "humanRightsStrategy",
            description:
              "In which relevant departments/business processes has the anchoring of the human rights strategy been ensured",
            label: "Human Rights Strategy",
            required: false,
            component: "InputTextFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
            name: "environmentalImpactPolicy",
            description:
              "Does your company have a Environmental Impact Policy? (If yes, please share the policy with us)",
            label: "Environmental Impact Policy",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
            name: "fairWorkingConditionsPolicy",
            description:
              "Does your company have a Fair Working Conditions Policy? (If yes, please share the policy with us)",
            label: "Fair Working Conditions Policy",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
        ],
      },
      {
        name: "generalViolations",
        label: "General violations",
        fields: [
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "responsibilitiesForFairWorkingConditions",
            description:
              "Has your company established, according to the nature and extent of the enterprise’s business activities, official responsibilities for the topic of fair working conditions?",
            label: "Responsibilities For Fair Working Conditions",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "responsibilitiesForTheEnvironment",
            description:
              "Has your company established, according to the nature and extent of the enterprise’s business activities, official responsibilities for the topic of the environment?",
            label: "Responsibilities For The Environment",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "responsibilitiesForOccupationalSafety",
            description:
              "Has your company established, according to the nature and extent of the enterprise’s business activities, official responsibilities for the topic of occupational safety?",
            label: "Responsibilities For Occupational Safety",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "legalProceedings",
            description:
              "Has your company been involved in the last 5 years, in legal disputes (including currently ongoing disputes) with third parties regarding human rights and environmental violations?",
            label: "Legal Proceedings",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "humanRightsViolation",
            description:
              "Have there been any violations of human rights or environmental aspects on your part in the last 5 years?",
            label: "Human Rights Violation",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.humanRightsViolation === "Yes",
            name: "humanRightsViolations",
            description: "What were the violations?",
            label: "Human Rights Violations",
            required: true,
            component: "InputTextFormField",
            validation: "required",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.humanRightsViolation === "Yes",
            certificateRequiredIfYes: false,
            name: "humanRightsViolationAction",
            description: "Has action been taken to address the violations?",
            label: "Human Rights Violation Action",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.humanRightsViolationAction === "Yes",
            name: "humanRightsViolationActionMeasures",
            description: "What measures have been taken?",
            label: "Human Rights Violation Action Measures",
            required: true,
            component: "InputTextFormField",
            validation: "required",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "highRiskCountriesRawMaterials",
            description: "Do you source your raw materials from verified conflict or high-risk regions?",
            label: "High Risk Countries Raw Materials",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.highRiskCountriesRawMaterials === "Yes",
            options: getDataset(DropdownDatasetIdentifier.CountryCodes),
            placeholder: "Select Country",
            validation: "required|length:1",
            name: "highRiskCountriesRawMaterialsLocation",
            description: "From which conflict/high-risk regions do you source your raw materials?",
            label: "High Risk Countries Raw Materials Location",
            required: true,
            component: "MultiSelectFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "highRiskCountriesActivity",
            description:
              "Does your company have activities in countries where there are high risks for human rights and/or the environment?",
            label: "High Risk Countries Activity",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.highRiskCountriesActivity === "Yes",
            options: getDataset(DropdownDatasetIdentifier.CountryCodes),
            placeholder: "Select Country",
            validation: "required|length:1",
            name: "highRiskCountries",
            description: "In which high risk countries does your company have activities?",
            label: "High Risk Countries",
            required: true,
            component: "MultiSelectFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "highRiskCountriesProcurement",
            description:
              "Does your company procure from countries with high risks for human rights and/or the environment?",
            label: "High Risk Countries Procurement",
            required: true,
            component: "YesNoFormField",
            validation: "required",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.governance?.generalViolations?.highRiskCountriesProcurement === "Yes",
            options: getDataset(DropdownDatasetIdentifier.CountryCodes),
            placeholder: "Select Country",
            name: "highRiskCountriesProcurementName",
            description: "From which high risk countries does your company procure from?",
            label: "High Risk Countries Procurement Name",
            required: false,
            component: "MultiSelectFormField",
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
            certificateRequiredIfYes: false,
            name: "employeeUnder18",
            description: "Does your company have employees under the age of 18?",
            label: "Employee Under 18",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.employeeUnder18 === "Yes",
            certificateRequiredIfYes: false,
            name: "employeeUnder18Under15",
            description:
              "With regard to the place of employment and the applicable laws: do you employ school-age children or children under the age of 15 on a full-time basis?",
            label: "Employee Under 18 Under 15",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.employeeUnder18 === "Yes",
            certificateRequiredIfYes: false,
            name: "employeeUnder18Apprentices",
            description:
              "Are the employees under 18 years of age exclusively apprentices within the meaning of the locally applicable laws?",
            label: "Employee Under 18 Apprentices",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.employeeUnder18 === "Yes",
            certificateRequiredIfYes: false,
            name: "worstFormsOfChildLaborProhibition",
            description:
              "Is the prohibition of the worst forms of child labor ensured in your company? This includes: all forms of slavery or practices similar to slavery, the use, procuring or offering of a child for prostitution, the production of pornography or pornographic performances, the use, procuring or offering of a child for illicit activities, in particular for the production or trafficking of drugs, work which, by its nature or the circumstances in which it is performed, is likely to be harmful to the health, safety, or morals of children",
            label: "Worst Forms of Child Labor Prohibition",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.worstFormsOfChildLaborProhibition === "Yes",
            name: "worstFormsOfChildLaborForms",
            description: "What forms of worst forms of child labor have been identified?",
            label: "Worst Forms of Child Labor Forms",
            required: false,
            component: "InputTextFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "employmentUnderLocalMinimumAgePrevention",
            description:
              "Does your company take measures to prevent the employment of children under the local minimum age?",
            label: "Employment Under Local Minimum Age Prevention",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            certificateRequiredIfYes: false,
            name: "employmentUnderLocalMinimumAgePreventionEmploymentContracts",
            description:
              "Is a formal recruitment process including the conclusion of employment contracts such a measure?",
            label: "Employment Under Local Minimum Age Prevention Employment Contracts",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            certificateRequiredIfYes: false,
            name: "employmentUnderLocalMinimumAgePreventionJobDescription",
            description:
              "Is a clear job description for employees under the local minimum age in the hiring process and employment contracts such a measure? (group of people between 15 and 18 years)",
            label: "Employment Under Local Minimum Age Prevention Job Description",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            certificateRequiredIfYes: false,
            name: "employmentUnderLocalMinimumAgePreventionIdentityDocuments",
            description:
              "Is the control of official documents (e.g. identity documents and certificates) such a measure?",
            label: "Employment Under Local Minimum Age Prevention Identity Documents",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            certificateRequiredIfYes: false,
            name: "employmentUnderLocalMinimumAgePreventionTraining",
            description:
              "Is raising the awareness of staff involved in the recruitment process through training such a measure?",
            label: "Employment Under Local Minimum Age Prevention Training",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            certificateRequiredIfYes: false,
            name: "employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge",
            description: "Is the regular checking of the legal minimum age such a measure?",
            label: "Employment Under Local Minimum Age Prevention Checking Of Legal Minimum Age",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.childLabor?.employmentUnderLocalMinimumAgePrevention === "Yes",
            name: "childLaborMeasures",
            description:
              "Please list any other measures (if available) you take to prevent the employment of children under the locally applicable minimum age?",
            label: "Child Labor Measures",
            required: false,
            component: "InputTextFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
            name: "childLaborPreventionPolicy",
            description:
              "Does your company have a policy to prevent child labor? (If yes, please share the policy with us)",
            label: "Child Labor Prevention Policy",
            required: false,
            component: "YesNoFormField",
          },
        ],
      },
      {
        name: "forcedLaborSlavery",
        label: "Forced labor, slavery",
        fields: [
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "forcedLaborAndSlaveryPrevention",
            description:
              "Does your company have practices that lead or may lead to forced labor and/or slavery?  The following are included: Creating unacceptable working and living conditions by working in hazardous conditions or in unacceptable accommodations provided by the employer; Excessive levels of overtime; Use of intimidation, threats, and/or punishment; Other types of forced labor (e.g. debt bondage, human trafficking)",
            label: "Forced Labor And Slavery Prevention",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPrevention === "Yes",
            name: "forcedLaborAndSlaveryPreventionPractices",
            description: "Please specify which of the points exist?",
            label: "Forced Labor And Slavery Prevention Practices",
            required: false,
            component: "InputTextFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "forcedLaborAndSlaveryPreventionMeasures",
            description: "Does your company take measures to prevent forced labor and slavery?",
            label: "Forced Labor And Slavery Prevention Measures",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            certificateRequiredIfYes: false,
            name: "forcedLaborAndSlaveryPreventionEmploymentContracts",
            description:
              "Is a formal hiring process, including employment contracts in the employee's local language, with appropriate wage and termination clauses, such a measure?",
            label: "Forced Labor And Slavery Prevention Employment Contracts",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            certificateRequiredIfYes: false,
            name: "forcedLaborAndSlaveryPreventionIdentityDocuments",
            description: "Is a ban on the retention of identity documents such a measure?",
            label: "Forced Labor And Slavery Prevention Identity Documents",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            certificateRequiredIfYes: false,
            name: "forcedLaborAndSlaveryPreventionFreeMovement",
            description:
              "Is ensuring the free movement of employees through doors and windows that can be opened to leave the building/premises of your company at any time such a measure?",
            label: "Forced Labor And Slavery Prevention Free Movement",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            certificateRequiredIfYes: false,
            name: "forcedLaborAndSlaveryPreventionProvisionSocialRoomsAndToilets",
            description: "Is the provision of social rooms and toilets that can be visited at any time such a measure?",
            label: "Forced Labor And Slavery Prevention Provision Social Rooms and Toilets",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            certificateRequiredIfYes: false,
            name: "forcedLaborAndSlaveryPreventionTraining",
            description:
              "Is raising the awareness of staff involved in the recruitment process through training such a measure?",
            label: "Forced Labor And Slavery Prevention Training",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === "Yes",
            name: "forcedLaborAndSlaveryPreventionMeasuresOther",
            description: "Please list other measures (if available) you take to prevent forced labor and slavery?",
            label: "Forced Labor and Slavery Prevention Measures (Other)",
            required: false,
            component: "InputTextFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
            name: "forcedLaborPreventionPolicy",
            description:
              "Does your company have a policy to prevent forced labor? (If yes, please share the policy with us)",
            label: "Forced Labor Prevention Policy",
            required: false,
            component: "YesNoFormField",
          },
        ],
      },
      {
        name: "withholdingAdequateWages",
        label: "Withholding adequate wages",
        fields: [
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "adequateWage",
            description: "Is your company currently withholding adequate wages (adequate in the sense of local laws)?",
            label: "Adequate Wage",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "adequateWageBeingWithheld",
            description: "Are any measures taken in your company to prevent that adequate wages being withheld?",
            label: "Adequate Wage being withheld",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequateWageBeingWithheld === "Yes",
            certificateRequiredIfYes: false,
            name: "documentedWorkingHoursAndWages",
            description: "Does your company document the working hours and wages of its employees?",
            label: "Documented Working Hours And Wages",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequateWageBeingWithheld === "Yes",
            certificateRequiredIfYes: false,
            name: "adequateLivingWage",
            description:
              "Does your company pay employees adequate wages? (the appropriate wage is at least the minimum wage set by the applicable law and is otherwise measured according to the law of the place of employment).",
            label: "Adequate Living Wage",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequateWageBeingWithheld === "Yes",
            certificateRequiredIfYes: false,
            name: "regularWagesProcessFlow",
            description:
              "Has your company implemented the payment of wages through standardised and regular process flows?",
            label: "Regular Wages Process Flow",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequateWageBeingWithheld === "Yes",
            certificateRequiredIfYes: false,
            name: "fixedHourlyWages",
            description: "Do fixed hourly wages exist in your company?",
            label: "Fixed Hourly Wages",
            required: false,
            component: "YesNoNaFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequateWageBeingWithheld === "Yes",
            certificateRequiredIfYes: false,
            name: "fixedPieceworkWages",
            description: "Does your company have fixed piecework wages?",
            label: "Fixed Piecework Wages",
            required: false,
            component: "YesNoNaFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.withholdingAdequateWages?.adequateWageBeingWithheld === "Yes",
            name: "adequateWageMeasures",
            description:
              "Please list other measures (if available) you take to prevent the withholding adequate wages?",
            label: "Adequate Wage Measures",
            required: false,
            component: "InputTextFormField",
          },
        ],
      },
      {
        name: "disregardForOccupationalHealthSafety",
        label: "Disregard for occupational health/safety",
        fields: [
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "lowSkillWork",
            description: "Do your employees perform low-skill manual work or repetitive manual work?",
            label: "Low Skill Work",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "hazardousMachines",
            description: "Are hazardous machines used in the manufacture of (preliminary) products?",
            label: "Hazardous Machines",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "oshPolicy",
            description:
              "Has your company implemented and enforced a formal occupational health and safety (OSH) policy that complies with local laws, industry requirements and international standards?",
            label: "OSH Policy",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
            name: "oshPolicyPersonalProtectiveEquipment",
            description: "Is the subject area of personal protective equipment addressed by this OSH Directive?",
            label: "OSH Policy Personal Protective Equipment",
            required: false,
            component: "YesNoNaFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
            name: "oshPolicyMachineSafety",
            description: "Is the subject area of machine safety addressed by this OSH Directive?",
            label: "OSH Policy Machine Safety",
            required: false,
            component: "YesNoNaFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
            name: "oshPolicyDisasterBehaviouralResponse",
            description: "Is the subject area of behaviour in the event of a disaster addressed by this OSH Directive?",
            label: "OSH Policy Disaster Behavioural Response",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
            name: "oshPolicyAccidentsBehaviouralResponse",
            description:
              "Is the subject area of behaviour in the event of and prevention of accidents addressed by this OSH Directive?",
            label: "OSH Policy Accidents Behavioural Response",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
            name: "oshPolicyWorkplaceErgonomics",
            description: "Is the subject area of workplace ergonomics addressed by this OSH Directive?",
            label: "OSH Policy Workplace Ergonomics",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
            name: "oshPolicyAccessToWork",
            description: "Is access to the work secluded/is the workplace difficult to access?",
            label: "OSH Policy Access to work",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
            name: "oshPolicyHandlingChemicalsAndOtherHazardousSubstances",
            description:
              "Is the subject area of handling chemical, physical or biological substances addressed by this OSH Directive?",
            label: "OSH Policy Handling Chemicals And Other Hazardous Substances",
            required: false,
            component: "YesNoNaFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
            name: "oshPolicyFireProtection",
            description: "Is the subject area of fire protection addressed by this OSH Directive?",
            label: "OSH Policy Fire Protection",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
            name: "oshPolicyWorkingHours",
            description:
              "Is the subject area of regulation of working hours, overtime and rest breaks be addressed by this OSH Directive?",
            label: "OSH Policy Working Hours",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
            name: "oshPolicyTrainingAddressed",
            description:
              "Is the subject area of training and instruction of employees with regard to occupational health and safety addressed by this OSH Directive?",
            label: "OSH Policy Training Addressed",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === "Yes",
            certificateRequiredIfYes: false,
            name: "oshPolicyTraining",
            description: "Are your employees regularly made aware of the OSH Directive and trained on them?",
            label: "OSH Policy Training",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "oshManagementSystem",
            description: "Is an occupational health and safety management system implemented in your company?",
            label: "OSH Management System",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshManagementSystem === "Yes",
            certificateRequiredIfYes: true,
            name: "oshManagementSystemInternationalCertification",
            description: "Is the OSH management system internationally recognised and certified?",
            label: "OSH Management System International Certification",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForOccupationalHealthSafety?.oshManagementSystem === "Yes",
            certificateRequiredIfYes: true,
            name: "oshManagementSystemNationalCertification",
            description: "Is the OSH management system nationally recognised and certified?",
            label: "OSH Management System National Certification",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "workplaceAccidentsUnder10",
            description:
              "Is the number of incidents in which employees suffered work-related injuries with serious consequences less than 10 in the past fiscal year?",
            label: "Workplace Accidents Under 10",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "oshTraining",
            description:
              "Has your company introduced mandatory offers and training for employees to improve occupational safety?",
            label: "OSH Training",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
            name: "healthAndSafetyPolicy",
            description: "Does your company have a Health And Safety Policy? (If yes, please share the policy with us)",
            label: "Health And Safety Policy",
            required: false,
            component: "YesNoFormField",
          },
        ],
      },
      {
        name: "disregardForFreedomOfAssociation",
        label: "Disregard for freedom of association",
        fields: [
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "freedomOfAssociation",
            description: "Does your company ensure that employees are free to form or join trade unions?",
            label: "Freedom Of Association",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForFreedomOfAssociation?.freedomOfAssociation === "Yes",
            name: "representedEmployees",
            description: "What is your percentage of employees who are represented by trade unions?",
            label: "Represented Employees",
            required: false,
            component: "NumberFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForFreedomOfAssociation?.freedomOfAssociation === "Yes",
            certificateRequiredIfYes: false,
            name: "discriminationForTradeUnionMembers",
            description:
              "Does your company ensure that no discrimination is practised or other consequences taken against employees in the event of the formation, joining and membership of a trade union?",
            label: "Discrimination For Trade Union Members",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.disregardForFreedomOfAssociation?.freedomOfAssociation === "Yes",
            certificateRequiredIfYes: false,
            name: "freedomOfOperationForTradeUnion",
            description:
              "Does your company ensure that trade unions are free to operate in accordance with the law of the place of employment?",
            label: "Freedom Of Operation For Trade Union",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "freedomOfAssociationTraining",
            description:
              "Do employees receive information about their rights as part of training and/or intranet, notices or company brochures?",
            label: "Freedom Of Association Training",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "worksCouncil",
            description:
              "Does your company have a works council or an employee representative committee (if it can be set up in your company in accordance with local applicable legal provisions)?",
            label: "Works Council",
            required: false,
            component: "YesNoFormField",
          },
        ],
      },
      {
        name: "unequalTreatmentOfEmployment",
        label: "Unequal treatment of employment",
        fields: [
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "unequalTreatmentOfEmployment",
            description:
              "Does your company treat employees unequally because of national/ethnic origin, social origin, health status, disability, sexual orientation, age, gender, political opinion, religion or belief?",
            label: "Unequal Treatment of Employment",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "diversityAndInclusionRole",
            description:
              "Is a member of your company's management responsible for promoting diversity in the workforce and among business partners?",
            label: "Diversity And Inclusion Role",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "preventionOfMistreatments",
            description:
              "Does your company's management promote a work environment free from physical, sexual, mental and verbal abuse, threats or other forms of mistreatment? (e.g. diversity program)",
            label: "Prevention Of Mistreatments",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "equalOpportunitiesOfficer",
            description: "Is an equal opportunities officer (or similar function) implemented in your company?",
            label: "Equal Opportunities Officer",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
            name: "fairAndEthicalRecruitmentPolicy",
            description:
              "Does your company have a Fair And Ethical Recruitment Policy? (If yes, please share the policy with us)",
            label: "Fair And Ethical Recruitment Policy",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: true,
            name: "equalOpportunitiesAndNonDiscriminationPolicy",
            description:
              "Does your company have a Equal Opportunities And Non-discrimination Policy? (If yes, please share the policy with us)",
            label: "Equal Opportunities And Non-discrimination Policy",
            required: false,
            component: "YesNoFormField",
          },
        ],
      },
      {
        name: "contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption",
        label: "Contamination of soil/water/air, noise emissions, excessive water consumption",
        fields: [
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "harmfulSoilChange",
            description: "Is there a risk of your company causing a harmful soil change?",
            label: "Harmful Soil Change",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            certificateRequiredIfYes: false,
            name: "soilDegradation",
            description:
              "Does your company have measures in place to prevent the degradation of soil structure caused by the use of heavy machinery?",
            label: "Soil Degradation",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            certificateRequiredIfYes: false,
            name: "soilErosion",
            description:
              "Does your company have measures in place to prevent soil erosion caused by deforestation or overgrazing?",
            label: "Soil Erosion",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            certificateRequiredIfYes: false,
            name: "soilBornDiseases",
            description:
              "Does your company have measures in place to prevent the development of soil-borne diseases and pests and to maintain soil fertility?",
            label: "Soil Born Diseases",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            certificateRequiredIfYes: false,
            name: "soilContamination",
            description:
              "Does your company have measures in place to prevent soil contamination by antibiotics and toxins?",
            label: "Soil Contamination",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulSoilChange === "Yes",
            certificateRequiredIfYes: false,
            name: "soilSalinisation",
            description: "Does your company have measures in place to prevent soil salinisation?",
            label: "Soil Salinisation",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "harmfulWaterPollution",
            description: "Is there a risk of your company causing harmful water pollution?",
            label: "Harmful Water Pollution",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulWaterPollution === "Yes",
            certificateRequiredIfYes: false,
            name: "fertilisersOrPollutants",
            description: "Does your company use fertilisers or pollutants such as chemicals or heavy metals?",
            label: "Fertilisers Or Pollutants",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulWaterPollution === "Yes",
            certificateRequiredIfYes: false,
            name: "wasteWaterFiltration",
            description: "Does your company have filtration systems for the waste water?",
            label: "Waste Water Filtration",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "harmfulAirPollution",
            description: "Is there a risk of harmful air pollution in your company?",
            label: "Harmful Air Pollution",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulAirPollution === "Yes",
            certificateRequiredIfYes: false,
            name: "airFiltration",
            description: "Does your company have air filtration systems?",
            label: "Air Filtration",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "harmfulNoiseEmission",
            description: "Is there a risk of harmful noise emission in your company?",
            label: "Harmful Noise Emission",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.harmfulNoiseEmission === "Yes",
            certificateRequiredIfYes: false,
            name: "reduceNoiseEmissions",
            description: "Has your company implemented structural measures to reduce noise emissions?",
            label: "Reduce Noise Emissions",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "excessiveWaterConsumption",
            description: "Is there a risk of excessive water consumption in your company?",
            label: "Excessive Water Consumption",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.excessiveWaterConsumption === "Yes",
            certificateRequiredIfYes: false,
            name: "waterSavingMeasures",
            description: "Do you take water-saving measures in your companies?",
            label: "Water Saving Measures",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.waterSavingMeasures === "Yes",
            name: "waterSavingMeasuresName",
            description: "If yes, which ones?",
            label: "Water Saving Measures Name",
            required: false,
            component: "InputTextFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.excessiveWaterConsumption === "Yes",
            certificateRequiredIfYes: false,
            name: "pipeMaintaining",
            description: "Are water pipes regularly checked and maintained?",
            label: "Pipe Maintaining",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
                ?.excessiveWaterConsumption === "Yes",
            certificateRequiredIfYes: false,
            name: "waterSources",
            description:
              "Does your company use water sources that are important for the local population or local agriculture?",
            label: "Water Sources",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            name: "contaminationMeasures",
            description:
              "Please list any other measures (if available) you are taking to prevent the risk of harmful soil change, water pollution, air pollution, harmful noise emission or excessive water consumption that: O significantly affects the natural basis for food preservation and production O denies a person access to safe drinking water O impedes or destroys a person's access to sanitary facilities, or O harms the health of any person",
            label: "Contamination Measures",
            required: false,
            component: "InputTextFormField",
          },
        ],
      },
      {
        name: "unlawfulEvictionDeprivationOfLandForestAndWater",
        label: "Unlawful eviction/deprivation of land, forest and water",
        fields: [
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "unlawfulEvictionAndTakingOfLand",
            description:
              "Is your company, as a result of the acquisition, development and/or other use of land, forests and/or bodies of water, which secures a person's livelihood, at risk of \n O an unlawful eviction\n O carrying out an unlawful taking of land, forests and/or water?",
            label: "Unlawful Eviction And Taking Of Land",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.unlawfulEvictionDeprivationOfLandForestAndWater?.unlawfulEvictionAndTakingOfLand ===
              "Yes",
            name: "unlawfulEvictionAndTakingOfLandRisk",
            description: "If so, what exactly is the risk?",
            label: "Unlawful Eviction And Taking Of Land Risk",
            required: false,
            component: "InputTextFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "unlawfulEvictionAndTakingOfLandStrategies",
            description:
              "Has your company developed and implemented strategies that avoid, reduce, mitigate or remedy direct and indirect negative impacts on the land and natural resources of indigenous peoples and local communities?",
            label: "Unlawful Eviction And Taking Of Land Strategies",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.unlawfulEvictionDeprivationOfLandForestAndWater
                ?.unlawfulEvictionAndTakingOfLandStrategies === "Yes",
            name: "unlawfulEvictionAndTakingOfLandStrategiesName",
            description: "If yes, which ones?",
            label: "Unlawful Eviction And Taking Of Land Strategies Name",
            required: false,
            component: "InputTextFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "voluntaryGuidelinesOnTheResponsibleGovernanceOfTenure",
            description:
              "Have you implemented the Voluntary Guidelines on the Responsible Governance of Tenure in your company?",
            label: "Voluntary Guidelines on the Responsible Governance of Tenure",
            required: false,
            component: "YesNoFormField",
          },
        ],
      },
      {
        name: "useOfPrivatePublicSecurityForcesWithDisregardForHumanRights",
        label: "Use of private/public security forces with disregard for human rights",
        fields: [
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "useOfPrivatePublicSecurityForces",
            description:
              "Does your company use private and/or public security forces to protect company projects or similar?",
            label: "Use Of Private Public Security Forces",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForces === "Yes",
            certificateRequiredIfYes: false,
            name: "useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights",
            description:
              "Does your company have measures in place to prevent security forces during an operation from O violate the prohibition of torture and/or cruel, inhuman and/or degrading treatment O damages life or limb O impairs the right to organize and the freedom of association?",
            label: "Use Of Private Public Security Forces And Risk Of Violation Of Human Rights",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
            certificateRequiredIfYes: false,
            name: "instructionOfSecurityForces",
            description: "Is an adequate instruction of the security forces such a measure?",
            label: "Instruction Of Security Forces",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
            certificateRequiredIfYes: false,
            name: "humanRightsTraining",
            description: "Is training on human rights such a measure?",
            label: "Human Rights Training",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
            certificateRequiredIfYes: false,
            name: "stateSecurityForces",
            description:
              "(Only in the case of state security forces) Before the security forces were commissioned, was it checked whether serious human rights violations by these units had already been documented?",
            label: "State Security Forces",
            required: false,
            component: "YesNoNaFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
            certificateRequiredIfYes: false,
            name: "privateSecurityForces",
            description:
              "(Only in the case of private security forces) Have the contractual relationships with the security guards been designed in such a way that they comply with the applicable legal framework?",
            label: "Private Security Forces",
            required: false,
            component: "YesNoNaFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights
                ?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === "Yes",
            name: "useOfPrivatePublicSecurityForcesMeasures",
            description:
              "Please list any other measures (if available) you are taking to prevent the use of private/public security forces in violation of human rights?",
            label: "Use Of Private Public Security Forces Measures",
            required: false,
            component: "InputTextFormField",
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
            certificateRequiredIfYes: false,
            name: "mercuryAndMercuryWasteHandling",
            description: "Does your company deal with mercury and mercury waste as part of its business model?",
            label: "Mercury And Mercury Waste Handling",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAndMercuryWasteHandling ===
              "Yes",
            certificateRequiredIfYes: true,
            name: "mercuryAndMercuryWasteHandlingPolicy",
            description:
              "Does your company have a policy for handling these materials? (If yes, please share the policy with us)",
            label: "Mercury And Mercury Waste Handling Policy",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "mercuryAddedProductsHandling",
            description:
              "Are you involved in the manufacture, use, treatment, and/or import or export of products containing mercury?",
            label: "Mercury Added-Products Handling",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling ===
              "Yes",
            certificateRequiredIfYes: false,
            name: "mercuryAddedProductsHandlingRiskOfExposure",
            description:
              "Is there a risk of manufacturing, importing or exporting products containing mercury that are not subject to the exemption under Annex A Part 1 of the Minamata Convention (BGBI. 2017 II p.610, 611)?",
            label: "Mercury Added-Products Handling Risk Of Exposure",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling ===
              "Yes",
            certificateRequiredIfYes: false,
            name: "mercuryAddedProductsHandlingRiskOfDisposal",
            description:
              "If there are products that are only contaminated with mercury: Is there a risk within your company that mercury waste will be disposed of contrary to the provisions of Article 11 of the Minamata Agreement (BGBI. 2017 II p. 610, 611)?",
            label: "Mercury Added-Products Handling Risk Of Disposal",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling ===
              "Yes",
            certificateRequiredIfYes: false,
            name: "mercuryAndMercuryCompoundsProductionAndUse",
            description: "Are there manufacturing processes in your company that use mercury and/or mercury compounds?",
            label: "Mercury and Mercury Compounds Production and Use",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling ===
              "Yes",
            certificateRequiredIfYes: false,
            name: "mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure",
            description:
              "Is there a risk in your company that mercury and/or mercury compounds used in manufacturing processes, that are regulated according to Article 5 Paragraph 2 and Annex B of the Minamata Agreement (Federal Law Gazette 2017 II p. 610, 611), have already exceeded the specified phase-out date and are therefore prohibited?",
            label: "Mercury And Mercury Compounds Production And Use Risk Of Exposure",
            required: false,
            component: "YesNoFormField",
          },
        ],
      },
      {
        name: "productionAndUseOfPersistentOrganicPollutantsPopsConvention",
        label: "Production and use of persistent organic pollutants (POPs Convention)",
        fields: [
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "persistentOrganicPollutantsProductionAndUse",
            description:
              "Do you use and/or produce persistent organic pollutants (POPs), i.e. chemical compounds that break down and/or transform very slowly in the environment?",
            label: "Persistent Organic Pollutants Production and Use",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse === "Yes",
            name: "persistentOrganicPollutantsUsed",
            description: "If yes, which organic pollutants are used?",
            label: "Persistent Organic Pollutants Used",
            required: false,
            component: "InputTextFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse === "Yes",
            certificateRequiredIfYes: false,
            name: "persistentOrganicPollutantsProductionAndUseRiskOfExposure",
            description:
              "Is there a risk in your company that these organic pollutants fall under Article 3 paragraph 1 letter a and Annex A of the Stockholm Convention of 23 May 2001 on persistent organic pollutants (Federal Law Gazette 2002 II p. 803, 804) (POPs Convention) and are therefore banned?",
            label: "Persistent Organic Pollutants Production And Use Risk Of Exposure",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse === "Yes",
            certificateRequiredIfYes: false,
            name: "persistentOrganicPollutantsProductionAndUseRiskOfDisposal",
            description:
              "In relation to the waste of these pollutants, is there a risk that they will be subject to the rules laid down in the applicable legal system in accordance with the provisions of Article 6(1)(d)(i) and (ii) of the POP -Convention (BGBI. 2002 II p. 803, 804) and will O not be handled / collected / stored / transported in an environmentally sound manner O not be disposed of in an environmentally friendly manner, i.e. if possible disposed of in such a way that the persistent organic pollutants contained therein are destroyed or irreversibly converted?",
            label: "Persistent Organic Pollutants Production And Use Risk Of Disposal",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention
                ?.persistentOrganicPollutantsProductionAndUse === "Yes",
            certificateRequiredIfYes: false,
            name: "legalRestrictedWasteProcesses",
            description:
              "Does your company have processes or measures in place to ensure the lawful handling of (hazardous) waste?",
            label: "Legal Restricted Waste Processes",
            required: false,
            component: "YesNoFormField",
          },
        ],
      },
      {
        name: "exportImportOfHazardousWasteBaselConvention",
        label: "Export/import of hazardous waste (Basel Convention)",
        fields: [
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "persistentOrganicPollutantsProductionAndUseTransboundaryMovements",
            description:
              "Is there a risk in your company that: hazardous waste within the meaning of the Basel Convention (Article 1 Paragraph 1, BGBI. 1994 II p. 2703, 2704) or other wastes that require special consideration (household waste, residues from incineration of household waste) (Article 1(2)) are transported across borders?",
            label: "Persistent Organic Pollutants Production And Use Transboundary Movements",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention
                ?.persistentOrganicPollutantsProductionAndUseTransboundaryMovements === "Yes",
            certificateRequiredIfYes: false,
            name: "persistentOrganicPollutantsProductionAndUseRiskForImportingState",
            description:
              "Are these wastes transported or shipped to an importing State that is subject to the Basel Convention and O has not given its written consent to the specific import (if that importing State has not prohibited the importation of that hazardous waste) (Article 4(1)(c)) O is not a contracting party (Article 4, paragraph 5) O does not treat waste in an environmentally friendly manner because it does not have the appropriate capacity for environmentally friendly disposal and cannot guarantee this elsewhere either (Article 4 paragraph 8 sentence 1) or O transported by a Party that has banned the import of such hazardous and other wastes (Article 4(1)(b) Basel Convention)?  (The term importing state includes: a contracting party to which a transboundary shipment of hazardous waste or other waste is planned for the purpose of disposal or for the purpose of loading prior to disposal in an area not under the sovereignty of a state. (Article 2 No. 11)",
            label: "Persistent Organic Pollutants Production and Use Risk for Importing State",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention
                ?.persistentOrganicPollutantsProductionAndUseTransboundaryMovements === "Yes",
            certificateRequiredIfYes: false,
            name: "hazardousWasteTransboundaryMovementsLocatedOecdEuLiechtenstein",
            description: "Is your company based in a country that is within the OECD, EU, or Liechtenstein?",
            label: "Hazardous Waste Transboundary Movements Located OECD, EU, Liechtenstein",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention
                ?.persistentOrganicPollutantsProductionAndUseTransboundaryMovements === "Yes",
            certificateRequiredIfYes: false,
            name: "hazardousWasteTransboundaryMovementsOutsideOecdEuLiechtenstein",
            description:
              "Is there a risk in your company that hazardous waste is transported to a country that is outside the OECD, EU / Liechtenstein?",
            label: "Hazardous Waste Transboundary Movements Outside OECD, EU, Liechtenstein",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
            name: "hazardousWasteDisposal",
            description:
              "Do you dispose of hazardous waste within the meaning of the Basel Convention (Article 1 Paragraph 1, BGBI. 1994 II p. 2703, 2704)?",
            label: "Hazardous Waste Disposal",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention?.hazardousWasteDisposal === "Yes",
            certificateRequiredIfYes: false,
            name: "hazardousWasteDisposalRiskOfImport",
            description:
              "Are you at risk of having this waste imported from a country that is not a party to the Basel Convention?",
            label: "Hazardous Waste Disposal Risk Of Import",
            required: false,
            component: "YesNoFormField",
          },
          {
            showIf: (dataModel: LksgData): boolean =>
              dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention?.hazardousWasteDisposal === "Yes",
            certificateRequiredIfYes: false,
            name: "hazardousAndOtherWasteImport",
            description:
              "Do you import other wastes that require special consideration (household waste, residues from incineration of household waste) (Article 1(2))?",
            label: "Hazardous And Other Waste Import",
            required: false,
            component: "YesNoFormField",
          },
        ],
      },
    ],
  },
] as Array<Category>;
