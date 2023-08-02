import { LksgData } from "@clients/backend";
import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";
import { Category } from "@/utils/GenericFrameworkTypes";


export const lksgDataModel = [ {
  name : "general",
  label : "General",
  color : "orange",
  showIf : (): boolean => true,
  subcategories : [ {
    name : "masterData",
    label : "Master Data",
    fields : [ {
      name : "dataDate",
      label : "Data Date",
      description : "The date until when the information collected is valid",
      unit : "",
      component : "DateFormField",
      evidenceDesired : false,
      required : true,
      showIf : (): boolean => true,
      validation : "required"
    }, {
      name : "headOfficeInGermany",
      label : "Head Office in Germany",
      description : "Is your head office, administrative headquarters, registered office, or subsidiary located in Germany?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "groupOfCompanies",
      label : "Group of Companies",
      description : "Do you belong to a group of companies?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "groupOfCompaniesName",
      label : "Group of Companies Name",
      description : "If yes, name of company group",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.general?.masterData?.groupOfCompanies === 'Yes'
    }, {
      name : "industry",
      label : "Industry",
      description : "In which industry is your company primarily active (select all that apply)?",
      unit : "",
      component : "NaceCodeFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true
    }, {
      name : "numberOfEmployees",
      label : "Number of Employees",
      description : "Total number of employees (including temporary workers with assignment duration >6 months)",
      unit : "",
      component : "NumberFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      validation : "min:0"
    }, {
      name : "seasonalOrMigrantWorkers",
      label : "Seasonal or Migrant Workers",
      description : "Do your company employ seasonal or migrant workers?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "shareOfTemporaryWorkers",
      label : "Share of Temporary Workers",
      description : "What share of the total number of employees in your company is made up by temporary workers?",
      unit : "Percentage",
      component : "RadioButtonsFormField",
      evidenceDesired : false,
      options : [ {
        label : "<10%",
        value : "Smaller10"
      }, {
        label : "10-25%",
        value : "Between10And25"
      }, {
        label : "25-50%",
        value : "Between25And50"
      }, {
        label : ">50%",
        value : "Greater50"
      } ],
      required : false,
      showIf : (): boolean => true
    }, {
      name : "annualTotalRevenue",
      label : "Annual Total Revenue",
      description : "Total revenue per annum",
      unit : "",
      component : "NumberFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true
    }, {
      name : "totalRevenueCurrency",
      label : "Total Revenue Currency",
      description : "The 3-letter alpha code that represents the currency used for the total revenue",
      unit : "",
      component : "SingleSelectFormField",
      evidenceDesired : false,
      options : getDataset(DropdownDatasetIdentifier.CurrencyCodes),
      required : false,
      showIf : (): boolean => true,
      placeholder : "Select Currency"
    }, {
      name : "fixedAndWorkingCapital",
      label : "Fixed and Working Capital",
      description : "Combined fixed and working capital (only for own operations) in same currency than total revenue",
      unit : "",
      component : "NumberFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true
    } ]
  }, {
    name : "productionSpecific",
    label : "Production-specific",
    fields : [ {
      name : "manufacturingCompany",
      label : "Manufacturing Company",
      description : "Is your company a manufacturing company?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "capacity",
      label : "Capacity",
      description : "Production capacity per year, e.g. quantity with units.",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.general?.productionSpecific?.manufacturingCompany === 'Yes'
    }, {
      name : "productionViaSubcontracting",
      label : "Production via Subcontracting",
      description : "Is the production done via subcontracting?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.general?.productionSpecific?.manufacturingCompany === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "subcontractingCompaniesCountries",
      label : "Subcontracting Companies Countries",
      description : "In which countries do the subcontracting companies operate?",
      unit : "",
      component : "MultiSelectFormField",
      evidenceDesired : false,
      options : getDataset(DropdownDatasetIdentifier.CountryCodesIso2),
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.general?.productionSpecific?.productionViaSubcontracting === 'Yes',
      placeholder : "Select Country"
    }, {
      name : "subcontractingCompaniesIndustries",
      label : "Subcontracting Companies Industries",
      description : "In which industries do the subcontracting companies operate?",
      unit : "",
      component : "NaceCodeFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.general?.productionSpecific?.productionViaSubcontracting === 'Yes'
    }, {
      name : "productionSites",
      label : "Production Sites",
      description : "Do you have production sites in your company?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.general?.productionSpecific?.manufacturingCompany === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "listOfProductionSites",
      label : "List Of Production Sites",
      description : "Please list the production sites in your company.",
      unit : "",
      component : "ProductionSitesFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.general?.productionSpecific?.productionSites === 'Yes'
    }, {
      name : "market",
      label : "Market",
      description : "Does your business focus predominantly on national or international markets?",
      unit : "",
      component : "RadioButtonsFormField",
      evidenceDesired : false,
      options : [ {
        label : "National",
        value : "National"
      }, {
        label : "International",
        value : "International"
      }, {
        label : "Both",
        value : "Both"
      } ],
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.general?.productionSpecific?.manufacturingCompany === 'Yes'
    }, {
      name : "specificProcurement",
      label : "Specific Procurement",
      description : "Does your company have specific procurement models such as: short-lived and changing business relationships, or high price pressure or tightly timed or short-term adjusted delivery deadlines and conditions with suppliers",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.general?.productionSpecific?.manufacturingCompany === 'Yes',
      certificateRequiredIfYes : false
    } ]
  }, {
    name : "productionSpecificOwnOperations",
    label : "Production-specific - Own Operations",
    fields : [ {
      name : "mostImportantProducts",
      label : "Most Important Products",
      description : "Please give an overview of the most important products or services in terms of sales that your company manufactures, distributes and/or offers (own operations)",
      unit : "",
      component : "MostImportantProductsFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.general?.productionSpecific?.manufacturingCompany === 'Yes'
    }, {
      name : "productsServicesCategoriesPurchased",
      label : "Products/Services Categories purchased",
      description : "Name their procurement categories (products, raw materials, services) (own operations)",
      unit : "",
      component : "ProcurementCategoriesFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.general?.productionSpecific?.manufacturingCompany === 'Yes'
    } ]
  } ]
}, {
  name : "governance",
  label : "Governance",
  color : "blue",
  showIf : (): boolean => true,
  subcategories : [ {
    name : "riskManagementOwnOperations",
    label : "Risk management - Own Operations",
    fields : [ {
      name : "riskManagementSystem",
      label : "Risk Management System",
      description : "Does your company have an adequate and effective Risk Management system?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "riskManagementSystemFiscalYear",
      label : "Risk Management System Fiscal Year",
      description : "Did you perform a risk analysis as part of risk management this fiscal year?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystem === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "riskManagementSystemRisks",
      label : "Risk Management System Risks",
      description : "Were risks identified during this period?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemFiscalYear === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "riskManagementSystemIdentifiedRisks",
      label : "Risk Management System Identified Risks",
      description : "Which risks were specifically identified in the risk analysis?",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemRisks === 'Yes'
    }, {
      name : "riskManagementSystemCounteract",
      label : "Risk Management System Counteract",
      description : "Have measures been defined to counteract the risks?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemRisks === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "riskManagementSystemMeasures",
      label : "Risk Management System Measures",
      description : "What measures have been applied to counteract the risks?",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystemCounteract === 'Yes'
    }, {
      name : "riskManagementSystemResponsibility",
      label : "Risk Management System Responsibility",
      description : "Is the responsibility for Risk Management in your company regulated, for example by appointing a human rights officer?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.riskManagementOwnOperations?.riskManagementSystem === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "environmentalManagementSystem",
      label : "Environmental Management System",
      description : "Has an environmental management system been implemented in your company?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "environmentalManagementSystemInternationalCertification",
      label : "Environmental Management System International Certification",
      description : "Is the environmental management system internationally recognized and certified?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.riskManagementOwnOperations?.environmentalManagementSystem === 'Yes',
      certificateRequiredIfYes : true
    }, {
      name : "environmentalManagementSystemNationalCertification",
      label : "Environmental Management System National Certification",
      description : "Is the environmental management system nationally recognized and certified?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.riskManagementOwnOperations?.environmentalManagementSystem === 'Yes',
      certificateRequiredIfYes : true
    } ]
  }, {
    name : "grievanceMechanismOwnOperations",
    label : "Grievance mechanism - Own Operations",
    fields : [ {
      name : "grievanceHandlingMechanism",
      label : "Grievance Handling Mechanism",
      description : "Has your company implemented a grievance handling mechanism (e.g. anonymous whistleblowing system) to protect human and environmental rights in your business?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "grievanceHandlingMechanismUsedForReporting",
      label : "Grievance Handling Mechanism used for Reporting",
      description : "Can all affected stakeholders and rights holders, i.e. both internal (e.g. employees) and external stakeholders (e.g. suppliers and their employees, NGOs) access the grievance reporting/whistleblowing system?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "grievanceMechanismInformationProvided",
      label : "Grievance Mechanism Information provided",
      description : "Is information about the grievance reporting process adapted to the correct context for all target groups/stakeholders?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "grievanceMechanismSupportProvided",
      label : "Grievance Mechanism Support provided",
      description : "Is the necessary support provided in a way that the target groups can actually use the procedure?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "grievanceMechanismAccessToExpertise",
      label : "Grievance Mechanism Access to Expertise",
      description : "Do the target groups have access to the expertise, advice and information that they need to participate in the grievance procedure in a fair, informed and respectful manner?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "grievanceMechanismComplaints",
      label : "Grievance Mechanism Complaints",
      description : "Have there been any complaints being entered into the system?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "grievanceMechanismComplaintsNumber",
      label : "Grievance Mechanism Complaints Number",
      description : "How many complaints have been received (for the reported Fiscal Year)?",
      unit : "",
      component : "NumberFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaints === 'Yes',
      validation : "min:0"
    }, {
      name : "grievanceMechanismComplaintsReason",
      label : "Grievance Mechanism Complaints Reason",
      description : "What kind of complaints have been received?",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaints === 'Yes'
    }, {
      name : "grievanceMechanismComplaintsAction",
      label : "Grievance Mechanism Complaints Action",
      description : "Have actions been taken to address the complaints?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaints === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "grievanceMechanismComplaintsActionUndertaken",
      label : "Grievance Mechanism Complaints Action undertaken",
      description : "What actions have been taken?",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceMechanismComplaintsAction === 'Yes'
    }, {
      name : "grievanceMechanismPublicAccess",
      label : "Grievance Mechanism Public Access",
      description : "Does your company have publicly accessible rules that clearly describe the process for dealing with complaints?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "grievanceMechanismProtection",
      label : "Grievance Mechanism Protection",
      description : "Does the process effectively protect whistleblowers from disadvantage or punishment?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "grievanceMechanismDueDiligenceProcess",
      label : "Grievance Mechanism Due Diligence Process",
      description : "Do the findings from the processing of clues flow into the adjustment of your own due diligence processes?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.grievanceMechanismOwnOperations?.grievanceHandlingMechanism === 'Yes',
      certificateRequiredIfYes : false
    } ]
  }, {
    name : "certificationsPoliciesAndResponsibilities",
    label : "Certifications, policies and responsibilities",
    fields : [ {
      name : "sa8000Certification",
      label : "SA8000 Certification",
      description : "Is your company SA8000 certified (Corporate Social Responsibility)? If yes, please share the certificate with us.",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : true
    }, {
      name : "smetaSocialAuditConcept",
      label : "SMETA Social Audit Concept",
      description : "Does your company apply a social audit concept as defined by SMETA (Sedex Members Ethical Trade Audit)? ",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "betterWorkProgramCertificate",
      label : "Better Work Program Certificate",
      description : "Do the production sites where the goods are produced participate in the BetterWork program? If yes, please share the certificate with us. (private label only)",
      unit : "",
      component : "YesNoNaFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : true
    }, {
      name : "iso45001Certification",
      label : "ISO 45001 Certification",
      description : "Is your company ISO 45001 certified  (Management Systems of Occupational Health and Safety)? If yes, please share the certificate with us. ",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : true
    }, {
      name : "iso14001Certification",
      label : "ISO 14001 Certification",
      description : "Is your company ISO 14001 certified (Environmental Management)? If yes, please share the certificate with us.",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : true
    }, {
      name : "emasCertification",
      label : "EMAS Certification",
      description : "Is your company EMAS certified (voluntary environmental management)? If yes, please share the certificate with us.",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : true
    }, {
      name : "iso37001Certification",
      label : "ISO 37001 Certification",
      description : "Is your company ISO 37001 certified (anti-bribery management systems)? If yes, please share the certificate with us. ",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : true
    }, {
      name : "iso37301Certification",
      label : "ISO 37301 Certification",
      description : "Is your company ISO 37301 certified (compliance management system)? If yes, please share the certificate with us.",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : true
    }, {
      name : "riskManagementSystemCertification",
      label : "Risk Management System Certification",
      description : "Is your risk management system internationally recognized and certified? (e.g.: ISO 31000)",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : true
    }, {
      name : "amforiBsciAuditReport",
      label : "amfori BSCI Audit Report",
      description : "Does your company have a current amfori BSCI audit report? If yes, please share the certificate with us.",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : true
    }, {
      name : "responsibleBusinessAssociationCertification",
      label : "Responsible Business Association Certification",
      description : "Is your company Responsible Business Association (RBA) certified  (social responsibility)? If yes, please share the certificate with us.",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : true
    }, {
      name : "fairLaborAssociationCertification",
      label : "Fair Labor Association Certification",
      description : "Is your company Fair Labor Association (FLA) certified (adherence to international and national labor laws)? If yes, please share the certificate with us.",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : true
    }, {
      name : "additionalAudits",
      label : "Additional Audits",
      description : "Please list other (sector-specific) audits (if available) to which your company is certified.",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true
    }, {
      name : "codeOfConduct",
      label : "Code of Conduct",
      description : "Has your company implemented and enforced internal behavioral guidelines that address the issues of human rights protection and respect for the environment  (e.g. within the code of conduct)?\n\n",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "codeOfConductTraining",
      label : "Code of Conduct Training",
      description : "Are your employees regularly made aware of your internal rules of conduct and trained on them?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.certificationsPoliciesAndResponsibilities?.codeOfConduct === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "supplierCodeOfConduct",
      label : "Supplier Code of Conduct",
      description : "Does your company have a supplier code of conduct? If yes, please share the supplier code of conduct with us.",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : true
    }, {
      name : "policyStatement",
      label : "Policy Statement",
      description : "Does your company have a policy statement on its human rights strategy?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "humanRightsStrategy",
      label : "Human Rights Strategy",
      description : "In which relevant departments/business processes has the anchoring of the human rights strategy been ensured?",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.certificationsPoliciesAndResponsibilities?.policyStatement === 'Yes'
    }, {
      name : "environmentalImpactPolicy",
      label : "Environmental Impact Policy",
      description : "Does your company have an environmental impact policy? If yes, please share the policy with us.",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : true
    }, {
      name : "fairWorkingConditionsPolicy",
      label : "Fair Working Conditions Policy",
      description : "Does your company have a fair working conditions policy? If yes, please share the policy with us.",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : true
    } ]
  }, {
    name : "generalViolations",
    label : "General violations",
    fields : [ {
      name : "responsibilitiesForFairWorkingConditions",
      label : "Responsibilities for Fair Working Conditions",
      description : "Has your company established official responsibilities for the topic of fair working conditions, according to the nature and extent of the enterprise’s business activities?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "responsibilitiesForTheEnvironment",
      label : "Responsibilities for the Environment",
      description : "Has your company established official responsibilities for the topic of the environment, according to the nature and extent of the enterprise’s business activities?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "responsibilitiesForOccupationalSafety",
      label : "Responsibilities for Occupational Safety",
      description : "Has your company established official responsibilities for the topic of occupational safety, according to the nature and extent of the enterprise’s business activities?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "legalProceedings",
      label : "Legal Proceedings",
      description : "Has your company been involved in legal disputes in the last 5 years (including currently ongoing disputes) with third parties regarding human rights and environmental violations?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "humanRightsViolationS",
      label : "Human Rights Violation(s)",
      description : "Have there been any human rights or environmental violations on your company’s part in the last 5 years?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "humanRightsViolations",
      label : "Human Rights Violations",
      description : "What were the violations?",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.generalViolations?.humanRightsViolationS === 'Yes'
    }, {
      name : "humanRightsViolationAction",
      label : "Human Rights Violation Action",
      description : "Has action been taken to address the violations?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.generalViolations?.humanRightsViolationS === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "humanRightsViolationActionMeasures",
      label : "Human Rights Violation Action Measures",
      description : "What measures have been taken?",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.generalViolations?.humanRightsViolationAction === 'Yes'
    }, {
      name : "highRiskCountriesRawMaterials",
      label : "High Risk Countries Raw Materials",
      description : "Do you source materials from countries associated with high-risk or conflict?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "highRiskCountriesRawMaterialsLocation",
      label : "High Risk Countries Raw Materials Location",
      description : "From which conflict/high-risk regions do you source your raw materials?",
      unit : "",
      component : "MultiSelectFormField",
      evidenceDesired : false,
      options : getDataset(DropdownDatasetIdentifier.CountryCodesIso2),
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.generalViolations?.highRiskCountriesRawMaterials === 'Yes',
      placeholder : "Select Country"
    }, {
      name : "highRiskCountriesActivity",
      label : "High Risk Countries Activity",
      description : "Does your company operate in countries where there are high risks for human rights and/or the environment?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "highRiskCountries",
      label : "High Risk Countries",
      description : "Which ones?",
      unit : "",
      component : "MultiSelectFormField",
      evidenceDesired : false,
      options : getDataset(DropdownDatasetIdentifier.CountryCodesIso2),
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.generalViolations?.highRiskCountriesActivity === 'Yes',
      placeholder : "Select Country"
    }, {
      name : "highRiskCountriesProcurement",
      label : "High Risk Countries Procurement",
      description : "Does your company procure from countries with high risks for human rights and/or the environment?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "highRiskCountriesProcurementName",
      label : "High Risk Countries Procurement Name",
      description : "Which ones?",
      unit : "",
      component : "MultiSelectFormField",
      evidenceDesired : false,
      options : getDataset(DropdownDatasetIdentifier.CountryCodesIso2),
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.governance?.generalViolations?.highRiskCountriesProcurement === 'Yes',
      placeholder : "Select Country"
    } ]
  } ]
}, {
  name : "social",
  label : "Social",
  color : "yellow",
  showIf : (): boolean => true,
  subcategories : [ {
    name : "childLabor",
    label : "Child labor",
    fields : [ {
      name : "childLaborPreventionPolicy",
      label : "Child Labor Prevention Policy",
      description : "Does your company have a policy to prevent child labor? If yes, please share the policy with us.",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : true
    }, {
      name : "employeeSUnder18",
      label : "Employee(s) Under 18",
      description : "Does your company have employees under the age of 18?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "employeeSUnder15",
      label : "Employee(s) Under 15",
      description : "With regard to the place of employment and the applicable laws: do you employ school-age children or children under the age of 15 on a full-time basis?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.employeeSUnder18 === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "employeeSUnder18InApprenticeship",
      label : "Employee(s) Under 18 in Apprenticeship",
      description : "Are your employees under the age of 18 exclusively apprentices within the meaning of the locally applicable laws?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.employeeSUnder18 === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "worstFormsOfChildLaborProhibition",
      label : "Worst Forms of Child Labor Prohibition",
      description : "Is the prohibition of the worst forms of child labor ensured in your company? This includes: all forms of slavery or practices similar to slavery, the use, procuring or offering of a child for prostitution, the production of pornography or pornographic performances, the use, procuring or offering of a child for illicit activities, in particular for the production or trafficking of drugs, work which, by its nature or the circumstances in which it is performed, is likely to be harmful to the health, safety, or morals of children",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.employeeSUnder18 === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "worstFormsOfChildLabor",
      label : "Worst Forms of Child Labor",
      description : "Have there been any worst forms of child labor in your company in the last 5 years?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.employeeSUnder18 === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "worstFormsOfChildLaborForms",
      label : "Worst Forms of Child Labor Forms",
      description : "Which worst forms of child labor have been identified?",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.worstFormsOfChildLabor === 'Yes'
    }, {
      name : "measuresForPreventionOfEmploymentUnderLocalMinimumAge",
      label : "Measures for  Prevention of Employment Under Local Minimum Age ",
      description : "Does your company take measures to prevent the employment of children under the local minimum age?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "employmentUnderLocalMinimumAgePreventionEmploymentContracts",
      label : "Employment Under Local Minimum Age Prevention -  Employment Contracts",
      description : "Is a formal recruitment process including the conclusion of employment contracts one of these measures?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.measuresForPreventionOfEmploymentUnderLocalMinimumAge === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "employmentUnderLocalMinimumAgePreventionJobDescription",
      label : "Employment Under Local Minimum Age Prevention  -  Job Description",
      description : "Is a clear job description for employees under the local minimum age in the hiring process and employment contracts one of these measures (group of people between 15 and 18 years)?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.measuresForPreventionOfEmploymentUnderLocalMinimumAge === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "employmentUnderLocalMinimumAgePreventionIdentityDocuments",
      label : "Employment Under Local Minimum Age Prevention - Identity Documents",
      description : "Is the control of official documents (e.g. identity documents and certificates) one of these measures?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.measuresForPreventionOfEmploymentUnderLocalMinimumAge === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "employmentUnderLocalMinimumAgePreventionTraining",
      label : "Employment Under Local Minimum Age Prevention - Training",
      description : "Is raising the awareness of staff involved in the recruitment process through training such a measure?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.measuresForPreventionOfEmploymentUnderLocalMinimumAge === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge",
      label : "Employment Under Local Minimum Age Prevention - Checking Of Legal Minimum Age",
      description : "Is the regular checking of the legal minimum age one of these measures?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.measuresForPreventionOfEmploymentUnderLocalMinimumAge === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "additionalChildLaborMeasures",
      label : "Additional Child Labor Measures",
      description : "Please list any other measures (if available) you take to prevent the employment of children under the locally applicable minimum age?",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.childLabor?.measuresForPreventionOfEmploymentUnderLocalMinimumAge === 'Yes'
    } ]
  }, {
    name : "forcedLaborSlavery",
    label : "Forced labor, slavery",
    fields : [ {
      name : "forcedLaborAndSlaveryPrevention",
      label : "Forced Labor and Slavery Prevention",
      description : "Does your company have practices that lead or may lead to forced labor and/or slavery?\n \n The following are included:\n O Creating unacceptable working and living conditions by working in hazardous conditions or within unacceptable accommodations provided by the employer\n O Excessive levels of overtime\n O Use of intimidation, threats, and/or punishment\n O Other types of forced labor (e.g. debt bondage, human trafficking)",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "forcedLaborAndSlaveryPreventionPractices",
      label : "Forced Labor and Slavery Prevention Practices",
      description : "Please specify which of these practices do exist.",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPrevention === 'Yes'
    }, {
      name : "forcedLaborPreventionPolicy",
      label : "Forced Labor Prevention Policy",
      description : "Does your company have a policy to prevent forced labor? If yes, please share the policy with us.",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : true
    }, {
      name : "forcedLaborAndSlaveryPreventionMeasures",
      label : "Forced Labor and Slavery Prevention Measures",
      description : "Does your company take measures to prevent forced labor and slavery?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "forcedLaborAndSlaveryPreventionEmploymentContracts",
      label : "Forced Labor and Slavery Prevention - Employment Contracts",
      description : "Is a formal hiring process including employment contracts in the employee's local language, with appropriate wage and termination clauses one of these measures?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "forcedLaborAndSlaveryPreventionIdentityDocuments",
      label : "Forced Labor and Slavery Prevention - Identity Documents",
      description : "Is a ban on the retention of identity documents one of these measures?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "forcedLaborAndSlaveryPreventionFreeMovement",
      label : "Forced Labor and Slavery Prevention - Free Movement",
      description : "Is the free movement of employees through doors and windows that can be opened to leave the building/premises of your company at any time  one of these measures?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "forcedLaborAndSlaveryPreventionProvisionSocialRoomsAndToilets",
      label : "Forced Labor and Slavery Prevention - Provision Social Rooms and Toilets",
      description : "Is the ensuring of social rooms and toilets that can be visited at any time one of these measures?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "forcedLaborAndSlaveryPreventionTraining",
      label : "Forced Labor and Slavery Prevention - Training",
      description : "Is raising the awareness of staff around forced labor and slavery involved in the recruitment process through training one of these measures?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "forcedLaborAndSlaveryPreventionMeasuresOther",
      label : "Forced Labor and Slavery Prevention Measures (Other)",
      description : "Please list any other measures (if available) you take to prevent forced labor and slavery?",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.forcedLaborSlavery?.forcedLaborAndSlaveryPreventionMeasures === 'Yes'
    } ]
  }, {
    name : "withholdingAdequateWages",
    label : "Withholding adequate wages",
    fields : [ {
      name : "adequateWage",
      label : "Adequate Wage",
      description : "Is your company currently withholding adequate wages (adequate in the sense of local laws)?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "adequateWagesMeasures",
      label : "Adequate Wages Measures",
      description : "Are any measures taken in your company to prevent adequate wages being withheld?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "documentedWorkingHoursAndWages",
      label : "Documented Working Hours and Wages",
      description : "Does your company document the working hours and wages of its employees?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.withholdingAdequateWages?.adequateWagesMeasures === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "adequateLivingWage",
      label : "Adequate Living Wage",
      description : "Does your company pay employees adequate living wages? (the appropriate wage is at least the minimum wage set by the applicable law and is otherwise measured according to the law of the place of employment).",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.withholdingAdequateWages?.adequateWagesMeasures === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "regularWagesProcessFlow",
      label : "Regular Wages Process Flow",
      description : "Has your company implemented the payment of wages through standardized and regular process flows?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.withholdingAdequateWages?.adequateWagesMeasures === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "fixedHourlyWages",
      label : "Fixed Hourly Wages",
      description : "Do fixed hourly wages exist in your company?",
      unit : "",
      component : "YesNoNaFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.withholdingAdequateWages?.adequateWagesMeasures === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "fixedPieceworkWages",
      label : "Fixed Piecework Wages",
      description : "Does your company have fixed piecework wages (pay per unit)?",
      unit : "",
      component : "YesNoNaFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.withholdingAdequateWages?.adequateWagesMeasures === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "adequateWageMeasures",
      label : "Adequate Wage Measures",
      description : "Please list other measures (if available) you take to prevent withholding adequate wages?",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.withholdingAdequateWages?.adequateWagesMeasures === 'Yes'
    } ]
  }, {
    name : "disregardForOccupationalHealthSafety",
    label : "Disregard for occupational health/safety",
    fields : [ {
      name : "lowSkillWork",
      label : "Low Skill Work",
      description : "Do your employees perform low-skill or repetitive manual labor?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "hazardousMachines",
      label : "Hazardous Machines",
      description : "Are hazardous machines used in the manufacturing of (preliminary) products?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "oshPolicy",
      label : "OSH Policy",
      description : "Has your company implemented and enforced a formal occupational health and safety (OSH) policy that complies with local laws, industry requirements, and international standards?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "oshPolicyPersonalProtectiveEquipment",
      label : "OSH Policy - Personal Protective Equipment",
      description : "Is the topic of personal protective equipment addressed by this OSH Directive?",
      unit : "",
      component : "YesNoNaFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "oshPolicyMachineSafety",
      label : "OSH Policy - Machine Safety",
      description : "Is the topic of machine safety addressed by this OSH Directive?",
      unit : "",
      component : "YesNoNaFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "oshPolicyDisasterBehavioralResponse",
      label : "OSH Policy - Disaster Behavioral Response",
      description : "Is the topic of disaster response addressed by this OSH Directive?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "oshPolicyAccidentsBehavioralResponse",
      label : "OSH Policy - Accidents Behavioral Response",
      description : "Is the topic of behavioral response in the event of an accident addressed by this OSH Directive?\n\n",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "oshPolicyWorkplaceErgonomics",
      label : "OSH Policy - Workplace Ergonomics",
      description : "Is the topic of workplace ergonomics addressed by this OSH Directive?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "oshPolicyAccessToWork",
      label : "OSH Policy - Access to work",
      description : "Is access to the workplace secluded (is the workplace difficult to access)?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "oshPolicyHandlingChemicalsAndOtherHazardousSubstances",
      label : "OSH Policy - Handling Chemicals and Other Hazardous Substances",
      description : "Is the topic of handling chemical, physical, or biological substances addressed by this OSH Directive?",
      unit : "",
      component : "YesNoNaFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "oshPolicyFireProtection",
      label : "OSH Policy - Fire Protection",
      description : "Is the topic of fire protection addressed by this OSH Directive?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "oshPolicyWorkingHours",
      label : "OSH Policy - Working Hours",
      description : "Is the topic of the regulation of working hours, overtime, and rest breaks addressed by this OSH Directive?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "oshPolicyTrainingAddressed",
      label : "OSH Policy - Training Addressed",
      description : "Is the topic of the training and instruction of employees with regard to occupational health and safety addressed by this OSH Directive?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "oshPolicyTraining",
      label : "OSH Policy - Training",
      description : "Are your employees regularly made aware of the OSH Directive and trained on them?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.disregardForOccupationalHealthSafety?.oshPolicy === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "oshManagementSystem",
      label : "OSH Management System",
      description : "Is an occupational health and safety management system implemented in your company?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "oshManagementSystemInternationalCertification",
      label : "OSH Management System - International Certification",
      description : "Is the OSH management system internationally recognized and certified?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.disregardForOccupationalHealthSafety?.oshManagementSystem === 'Yes',
      certificateRequiredIfYes : true
    }, {
      name : "oshManagementSystemNationalCertification",
      label : "OSH Management System - National Certification",
      description : "Is the OSH management system nationally recognized and certified?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.disregardForOccupationalHealthSafety?.oshManagementSystem === 'Yes',
      certificateRequiredIfYes : true
    }, {
      name : "under10WorkplaceAccidents",
      label : "Under 10 Workplace Accidents",
      description : "Have there been less than 10 incidents in which employees suffered work-related injuries with serious consequences in the past fiscal year?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "oshTraining",
      label : "OSH Training",
      description : "Has your company introduced mandatory training for employees to improve occupational safety?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "healthAndSafetyPolicy",
      label : "Health and Safety Policy",
      description : "Does your company have a Health and Safety Policy? If yes, please share the policy with us.",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : true
    } ]
  }, {
    name : "disregardForFreedomOfAssociation",
    label : "Disregard for freedom of association",
    fields : [ {
      name : "freedomOfAssociation",
      label : "Freedom Of Association",
      description : "Does your company ensure that employees are free to form or join trade unions?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "employeeRepresentation",
      label : "Employee Representation",
      description : "What is your percentage of employees who are represented by trade unions?",
      unit : "Percentage",
      component : "PercentageFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.disregardForFreedomOfAssociation?.freedomOfAssociation === 'Yes'
    }, {
      name : "discriminationForTradeUnionMembers",
      label : "Discrimination for Trade Union Members",
      description : "Does your company ensure that no consequences are taken against employees in the event of the formation, joining, and membership of a trade union?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.disregardForFreedomOfAssociation?.freedomOfAssociation === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "freedomOfOperationForTradeUnion",
      label : "Freedom of Operation for Trade Union",
      description : "Does your company ensure that trade unions are free to operate in accordance with the law in the place of employment?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.disregardForFreedomOfAssociation?.freedomOfAssociation === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "freedomOfAssociationTraining",
      label : "Freedom of Association Training",
      description : "Do employees receive information about their rights as a part of training, notices, or company brochures?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "worksCouncil",
      label : "Works Council",
      description : "Does your company have a works council or employee representative committee (if these are legal according to local law)?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    } ]
  }, {
    name : "unequalTreatmentOfEmployment",
    label : "Unequal treatment of employment",
    fields : [ {
      name : "unequalTreatmentOfEmployment",
      label : "Unequal Treatment of Employment",
      description : "Does your company treat employees unequally because of national/ethnic origin, social origin, health status, disability, sexual orientation, age, gender, political opinion, religion or belief?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "diversityAndInclusionRole",
      label : "Diversity and Inclusion Role",
      description : "Is a member of your company's management responsible for promoting diversity in the workforce and among business partners?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "preventionOfMistreatments",
      label : "Prevention of Mistreatments",
      description : "Does your company's management promote a work environment free from physical, sexual, mental abuse, threats or other forms of mistreatment? (e.g. diversity program)",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "equalOpportunitiesOfficer",
      label : "Equal Opportunities Officer",
      description : "Is an equal opportunities officer (or similar function) present in your company?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "fairAndEthicalRecruitmentPolicy",
      label : "Fair and Ethical Recruitment Policy",
      description : "Does your company have a Fair and Ethical Recruitment Policy? If yes, please share the policy with us.",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : true
    }, {
      name : "equalOpportunitiesAndNonDiscriminationPolicy",
      label : "Equal Opportunities and Non-discrimination Policy",
      description : "Does your company have an Equal Opportunities and Non-discrimination Policy? If yes, please share the policy with us.",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : true
    } ]
  }, {
    name : "contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption",
    label : "Contamination of soil/water/air, noise emissions, excessive water consumption",
    fields : [ {
      name : "harmfulSoilImpact",
      label : "Harmful Soil Impact",
      description : "Is there a risk of your company causing a harmful soil impact?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "soilDegradation",
      label : "Soil Degradation",
      description : "Does your company have measures in place to prevent the degradation of the local soil structure caused by the use of heavy machinery?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.harmfulSoilImpact === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "soilErosion",
      label : "Soil Erosion",
      description : "Does your company have measures in place to prevent soil erosion caused by deforestation or overgrazing?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.harmfulSoilImpact === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "soilBorneDiseases",
      label : "Soil-borne Diseases",
      description : "Does your company have measures in place to prevent the development of soil-borne diseases and pests to maintain soil fertility?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.harmfulSoilImpact === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "soilContamination",
      label : "Soil Contamination",
      description : "Does your company have measures in place to prevent soil contamination caused by antibiotics and toxins?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.harmfulSoilImpact === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "soilSalinization",
      label : "Soil Salinization",
      description : "Does your company have measures in place to prevent soil salinization?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.harmfulSoilImpact === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "harmfulWaterPollution",
      label : "Harmful Water Pollution",
      description : "Is there a risk of your company causing harmful water pollution?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "fertilizersOrPollutants",
      label : "Fertilizers or Pollutants",
      description : "Does your company use fertilizers or pollutants such as chemicals or heavy metals?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.harmfulWaterPollution === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "wasteWaterFiltration",
      label : "Waste Water Filtration",
      description : "Does your company have waste water filtration systems?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.harmfulWaterPollution === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "harmfulAirPollution",
      label : "Harmful Air Pollution",
      description : "Is there a risk of harmful air pollution caused by your company?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "airFiltration",
      label : "Air Filtration",
      description : "Does your company have air filtration systems?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.harmfulAirPollution === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "harmfulNoiseEmission",
      label : "Harmful Noise Emission",
      description : "Is there a risk of harmful noise emission caused by your company?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "reduceNoiseEmissions",
      label : "Reduce Noise Emissions",
      description : "Has your company implemented structural measures to reduce noise emissions?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.harmfulNoiseEmission === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "excessiveWaterConsumption",
      label : "Excessive Water Consumption",
      description : "Is there a risk of excessive water consumption in your company?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "waterSavingMeasures",
      label : "Water Saving Measures",
      description : "Do you have water-saving measures in your companies?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.excessiveWaterConsumption === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "waterSavingMeasuresName",
      label : "Water Saving Measures Name",
      description : "If yes, which ones?",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.waterSavingMeasures === 'Yes'
    }, {
      name : "pipeMaintaining",
      label : "Pipe Maintaining",
      description : "Are water pipes regularly checked and maintained?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.excessiveWaterConsumption === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "waterSources",
      label : "Water Sources",
      description : "Does your company use water sources that are important for the local population or agriculture?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?.excessiveWaterConsumption === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "contaminationMeasures",
      label : "Contamination Measures",
      description : "Please list any other measures (if available) you are taking to prevent the risk of harmful soil change, water pollution, air pollution, harmful noise emission or excessive water consumption that:\n O Significantly affects the natural basis for food production\n O Denies a person access to safe drinking water\n O Impedes or destroys a person's access to sanitary facilities\n O Harms the health of any person",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true
    } ]
  }, {
    name : "unlawfulEvictionDeprivationOfLandForestAndWater",
    label : "Unlawful eviction/deprivation of land, forest and water",
    fields : [ {
      name : "unlawfulEvictionAndTakingOfLand",
      label : "Unlawful Eviction and Taking of Land",
      description : "Is your company, as a result of the acquisition, development, or other use of land, forests, or bodies of water, which secures a person's livelihood, at risk of: \n O An unlawful eviction\n O Carrying out an unlawful taking of land, forests, or water.",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "unlawfulEvictionAndTakingOfLandRisk",
      label : "Unlawful Eviction and Taking of Land - Risk",
      description : "If so, what exactly is the risk?",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.unlawfulEvictionDeprivationOfLandForestAndWater?.unlawfulEvictionAndTakingOfLand === 'Yes'
    }, {
      name : "unlawfulEvictionAndTakingOfLandStrategies",
      label : "Unlawful Eviction and Taking of Land - Strategies",
      description : "Has your company developed and implemented strategies that avoid, reduce, mitigate, or remedy direct and indirect negative impacts on the land, and natural resources of indigenous peoples and local communities?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "unlawfulEvictionAndTakingOfLandStrategiesName",
      label : "Unlawful Eviction And Taking Of Land - Strategies Name",
      description : "If yes, which ones?",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.unlawfulEvictionDeprivationOfLandForestAndWater?.unlawfulEvictionAndTakingOfLandStrategies === 'Yes'
    }, {
      name : "voluntaryGuidelinesOnTheResponsibleGovernanceOfTenure",
      label : "Voluntary Guidelines on the Responsible Governance of Tenure",
      description : "Have you implemented the voluntary guidelines on the responsible governance of tenure in your company?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    } ]
  }, {
    name : "useOfPrivatePublicSecurityForcesWithDisregardForHumanRights",
    label : "Use of private/public security forces with disregard for human rights",
    fields : [ {
      name : "useOfPrivatePublicSecurityForces",
      label : "Use of Private Public Security Forces",
      description : "Does your company use private or public security forces to protect company projects?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights",
      label : "Use of Private Public Security Forces and Risk of Violation of Human Rights",
      description : "Does your company have measures in place to prevent your security forces from:\n O Violating the prohibition of torture or cruel, inhuman, or degrading treatment\n O Damaging life or limbs\n O Impairing the right to exercise the freedom of association?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights?.useOfPrivatePublicSecurityForces === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "instructionOfSecurityForces",
      label : "Instruction of Security Forces",
      description : "Is the adequate instruction and training of your security forces a current measure?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "humanRightsTraining",
      label : "Human Rights Training",
      description : "Is the training of your security forces on human rights a current measure?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "stateSecurityForces",
      label : "State Security Forces",
      description : "Before the state security forces were commissioned, was it reviewed whether serious human rights violations had already been documented by these units?",
      unit : "",
      component : "YesNoNaFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "privateSecurityForces",
      label : "Private Security Forces",
      description : "Have the contractual relationships with your private security guards been designed in such a way that complies with the applicable legal framework?",
      unit : "",
      component : "YesNoNaFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "useOfPrivatePublicSecurityForcesMeasures",
      label : "Use of Private Public Security Forces Measures",
      description : "Please list any other measures you are taking to prevent the use of private or public security forces that violate human rights?",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.social?.useOfPrivatePublicSecurityForcesWithDisregardForHumanRights?.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights === 'Yes'
    } ]
  } ]
}, {
  name : "environmental",
  label : "Environmental",
  color : "green",
  showIf : (): boolean => true,
  subcategories : [ {
    name : "useOfMercuryMercuryWasteMinamataConvention",
    label : "Use of mercury, mercury waste (Minamata Convention)",
    fields : [ {
      name : "mercuryAndMercuryWasteHandling",
      label : "Mercury and Mercury Waste Handling",
      description : "Does your company deal with mercury or mercury waste as part of its business model?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "mercuryAndMercuryWasteHandlingPolicy",
      label : "Mercury and Mercury Waste Handling Policy",
      description : "Does your company have a policy for safely handling mercury or mercury waste? If yes, please share the policy with us.\n\n",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAndMercuryWasteHandling === 'Yes',
      certificateRequiredIfYes : true
    }, {
      name : "mercuryAddedProductsHandling",
      label : "Mercury Added-Products Handling",
      description : "Are you involved in the manufacturing, use, treatment, import, or export of products containing mercury?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "mercuryAddedProductsHandlingRiskOfExposure",
      label : "Mercury Added-Products Handling - Risk of Exposure",
      description : "Is there a risk of manufacturing, importing or exporting products containing mercury that are not subject to the Annex A Part 1 exemption of the Minamata Convention (BGBI. 2017 II p.610, 611)?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "mercuryAddedProductsHandlingRiskOfDisposal",
      label : "Mercury Added-Products Handling - Risk of Disposal",
      description : "If there are products that are contaminated with mercury, is there a risk within your company that mercury waste will be disposed of not in accordance with the provisions of Article 11 of the Minamata Agreement (BGBI. 2017 II p. 610, 611)?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "mercuryAndMercuryCompoundsProductionAndUse",
      label : "Mercury and Mercury Compounds Production and Use",
      description : "Are there manufacturing processes in your company that use mercury or mercury compounds?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure",
      label : "Mercury and Mercury Compounds Production and Use - Risk of Exposure",
      description : "Is there a risk in your company that mercury or mercury compounds used in the manufacturing process have already exceeded the specified phase-out date and are therefore prohibited according to Article 5(2), Annex B of the Minamata Agreement (BGBI. 2017 II p. 616, 617)?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.environmental?.useOfMercuryMercuryWasteMinamataConvention?.mercuryAddedProductsHandling === 'Yes',
      certificateRequiredIfYes : false
    } ]
  }, {
    name : "productionAndUseOfPersistentOrganicPollutantsPopsConvention",
    label : "Production and use of persistent organic pollutants (POPs Convention)",
    fields : [ {
      name : "persistentOrganicPollutantsProductionAndUse",
      label : "Persistent Organic Pollutants Production and Use",
      description : "Do you use and/or produce persistent organic pollutants (POPs)? I.e. chemical compounds that break down or transform very slowly in the environment.",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "persistentOrganicPollutantsUsed",
      label : "Persistent Organic Pollutants Used",
      description : "If yes, which organic pollutants are used?",
      unit : "",
      component : "InputTextFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention?.persistentOrganicPollutantsProductionAndUse === 'Yes'
    }, {
      name : "persistentOrganicPollutantsProductionAndUseRiskOfExposure",
      label : "Persistent Organic Pollutants Production and Use - Risk Of Exposure",
      description : "IIs there a risk in your company that these organic pollutants fall under Article 3(1)(a), Annex A of the Stockholm Convention of May 23rd 2001 on persistent organic pollutants (BGBl. 2002 II p. 803-804) and therefore banned?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention?.persistentOrganicPollutantsProductionAndUse === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "persistentOrganicPollutantsProductionAndUseRiskOfDisposal",
      label : "Persistent Organic Pollutants Production and Use - Risk Of Disposal",
      description : "Regarding the waste of these pollutants, is there a risk that they are subject to the rules and provisions of Article 6(1)(d)(i) and (ii) of the POP Convention (BGBI. 2002 II p. 803, 804) and will:\nO Not be handled, collected, stored, or transported in an environmentally sound manner\nO Not be disposed of in an environmentally friendly manner. I.e. disposed of in such a way that the persistent organic pollutants contained therein are destroyed or irreversibly altered?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention?.persistentOrganicPollutantsProductionAndUse === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "legalRestrictedWasteProcesses",
      label : "Legal Restricted Waste Processes",
      description : "Does your company have measures in place to ensure the lawful handling of (hazardous) waste?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.environmental?.productionAndUseOfPersistentOrganicPollutantsPopsConvention?.persistentOrganicPollutantsProductionAndUse === 'Yes',
      certificateRequiredIfYes : false
    } ]
  }, {
    name : "exportImportOfHazardousWasteBaselConvention",
    label : "Export/import of hazardous waste (Basel Convention)",
    fields : [ {
      name : "persistentOrganicPollutantsProductionAndUseTransboundaryMovements",
      label : "Persistent Organic Pollutants Production And Use - Transboundary Movements",
      description : "Is there a risk in your company that\n O Hazardous waste within the meaning of the Basel Convention  (Article 1(1), BGBI. 1994 II p. 2703, 2704) or\n O Other waste that requires special consideration (household waste or its byproducts) is transported across borders?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "persistentOrganicPollutantsProductionAndUseRiskForImportingState",
      label : "Persistent Organic Pollutants Production and Use - Risk for Importing State",
      description : "Is this waste transported to an importing State that is subject to the Basel Convention and:\nO Has not given its written consent for the specific import (if that importing State has not prohibited the importation of that hazardous waste) (Article 4(1)(c))\nO Is not a contracting party (Article 4(5))\nO Does not treat waste in an environmentally friendly manner because it does not have the appropriate capacity for environmentally friendly disposal, and cannot guarantee this elsewhere either (Article 4(8))\nO Transported by a party that has banned the import of such hazardous wastes (Article 4(1)(b) Basel Convention)?\n\n(The term \"importing state\" includes: a contracting party to which a transboundary shipment of hazardous waste is planned for the disposal or purpose of unloading, prior to disposal in an area not under the sovereignty of a state (Article 2(11)).",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention?.persistentOrganicPollutantsProductionAndUseTransboundaryMovements === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "hazardousWasteTransboundaryMovementsLocatedOecdEuLiechtenstein",
      label : "Hazardous Waste Transboundary Movements - Located OECD, EU, Liechtenstein",
      description : "Is your company based in a country that is within the OECD, EU, or Liechtenstein?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention?.persistentOrganicPollutantsProductionAndUseTransboundaryMovements === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "hazardousWasteTransboundaryMovementsOutsideOecdEuOrLiechtenstein",
      label : "Hazardous Waste Transboundary Movements - Outside OECD, EU, or Liechtenstein",
      description : "Is there a risk in your company that hazardous waste is transported to a country that is outside the OECD, EU / Liechtenstein?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention?.persistentOrganicPollutantsProductionAndUseTransboundaryMovements === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "hazardousWasteDisposal",
      label : "Hazardous Waste Disposal",
      description : "Do you dispose of hazardous waste in accordance with the Basel Convention (Article 1(1), BGBI. 1994 II p. 2703, 2704)?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (): boolean => true,
      certificateRequiredIfYes : false
    }, {
      name : "hazardousWasteDisposalRiskOfImport",
      label : "Hazardous Waste Disposal - Risk of Import",
      description : "Are you at risk of having these hazardous wastes imported from a country that is not a member of the Basel Convention?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention?.hazardousWasteDisposal === 'Yes',
      certificateRequiredIfYes : false
    }, {
      name : "hazardousWasteDisposalOtherWasteImport",
      label : "Hazardous Waste Disposal - Other Waste Import",
      description : "Do you import other wastes that require special consideration (household waste or its byproducts)?",
      unit : "",
      component : "YesNoFormField",
      evidenceDesired : false,
      required : false,
      showIf : (dataModel: LksgData): boolean => dataModel?.environmental?.exportImportOfHazardousWasteBaselConvention?.hazardousWasteDisposal === 'Yes',
      certificateRequiredIfYes : false
    } ]
  } ]
} ] as Array<Category>;
