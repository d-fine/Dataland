import { EuTaxonomyDataForNonFinancials } from "@clients/backend";
import { Category } from "@/utils/GenericFrameworkTypes";


export const newEuTaxonomyForNonFinancialsDataModel = [ {
    name : "general",
    label : "General",
    color : "orange",
    showIf : (): boolean => true,
    subcategories : [ {
        name : "general",
        label : "General",
        fields : [ {
            name : "fiscalYearDeviation",
            label : "Fiscal Year Deviation",
            description : "Fiscal Year (Deviation/ No Deviation)",
            unit : "",
            component : "RadioButtonsFormField",
            evidenceDesired : false,
            options : [ {
                label : "Deviation",
                value : "Deviation"
            }, {
                label : "No Deviation",
                value : "NoDeviation"
            } ],
            required : false,
            showIf : (): boolean => true
        }, {
            name : "fiscalYearEnd",
            label : "Fiscal Year End",
            description : "The date the fiscal year ends",
            unit : "",
            component : "DateFormField",
            evidenceDesired : false,
            required : true,
            showIf : (): boolean => true,
            validation : "required"
        }, {
            name : "referencedReports",
            label : "Referenced Reports",
            description : "Does your company have a current annual report, sustainability report, integrated report or ESEF report? If yes, please share the information with us.",
            unit : "",
            component : "UploadReports",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "scopeOfEntities",
            label : "Scope Of Entities",
            description : "Does a list of legal entities covered by Sust./Annual/Integrated/ESEF report match with a list of legal entities covered by Audited Consolidated Financial Statement ",
            unit : "",
            component : "YesNoNaFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true,
            certificateRequiredIfYes : false
        }, {
            name : "euTaxonomyActivityLevelReporting",
            label : "EU Taxonomy Activity Level Reporting",
            description : "Activity Level disclosure",
            unit : "",
            component : "YesNoFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true,
            certificateRequiredIfYes : false
        }, {
            name : "numberOfEmployees",
            label : "Number Of Employees",
            description : "Total number of employees (including temporary workers)",
            unit : "",
            component : "NumberFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "nfrdMandatory",
            label : "NFRD Mandatory",
            description : "The reporting obligation for companies whose number of employees is greater or equal to 500",
            unit : "",
            component : "YesNoFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true,
            certificateRequiredIfYes : false
        }, {
            name : "assurance",
            label : "Assurance",
            description : "",
            unit : "",
            component : "AssuranceFormField",
            evidenceDesired : true,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "revenue",
        label : "Revenue",
        fields : [ {
            name : "totalAmount",
            label : "Total Amount",
            description : "Total Revenue for the financial year. I.e. income arising in the course of an entity's ordinary activities., the amounts derived from the sale of products and the provision of services after deducting sales rebates and value added tax and other taxes directly linked to turnover. Overall turnover is equivalent to a firm's total revenues over some period of time",
            unit : "",
            component : "DataPointFormField",
            evidenceDesired : true,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "totalNonEligibleShare",
            label : "Total Non-Eligible Share",
            description : "",
            unit : "",
            component : "FinancialShareFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "totalEligibleShare",
            label : "Total Eligible Share",
            description : "Percentage and absolute share of the Revenue where the economic activity meets taxonomy criteria for substantial contribution to climate change mitigation and does no serious harm to the other environmental objectives (DNSH criteria)",
            unit : "",
            component : "FinancialShareFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "totalEligibleNonAlignedShare",
            label : "Total Eligible Non-Aligned Share",
            description : "",
            unit : "",
            component : "FinancialShareFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "eligibleNonAlignedActivities",
            label : "Eligible Non-Aligned Activities",
            description : "",
            unit : "",
            component : "ActivitiesFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "totalAlignedShare",
            label : "Total Aligned Share",
            description : "Percentage and absolute share of the Revenue that is taxonomy-aligned, i.e., generated by an eligible economic activity that is making a substantial contribution to at least one of the climate and environmental objectives, while also doing no significant harm to the remaining objectives and meeting minimum standards on human rights and labour standards",
            unit : "",
            component : "FinancialShareFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "alignedActivities",
            label : "Aligned Activities",
            description : "",
            unit : "",
            component : "AlignedActivitiesFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "totalEnablingShare",
            label : "Total Enabling Share",
            description : "",
            unit : "",
            component : "PercentageFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "totalTransitionalShare",
            label : "Total Transitional Share",
            description : "",
            unit : "",
            component : "PercentageFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "capex",
        label : "CapEx",
        fields : [ {
            name : "totalAmount",
            label : "Total Amount",
            description : "Total CapEx for the financial year. A capital expenditure (CapEx) is a payment for goods or services recorded, or capitalized, on the balance sheet instead of expensed on the income statement",
            unit : "",
            component : "DataPointFormField",
            evidenceDesired : true,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "totalNonEligibleShare",
            label : "Total Non-Eligible Share",
            description : "",
            unit : "",
            component : "FinancialShareFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "totalEligibleShare",
            label : "Total Eligible Share",
            description : "Percentage and absolute share of the CapEx that is part of a plan to meet taxonomy criteria for substantial contribution to climate change adaptation and relevant DNSH criteria",
            unit : "",
            component : "FinancialShareFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "totalEligibleNonAlignedShare",
            label : "Total Eligible Non-Aligned Share",
            description : "",
            unit : "",
            component : "FinancialShareFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "eligibleNonAlignedActivities",
            label : "Eligible Non-Aligned Activities",
            description : "",
            unit : "",
            component : "ActivitiesFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "totalAlignedShare",
            label : "Total Aligned Share",
            description : "Percentage and absolute share of the CapEx that is either already taxonomy-aligned or is part of a credible plan to extend or reach taxonomy alignment. I.e., an eligible economic activity that is making a substantial contribution to at least one of the climate and environmental objectives, while also doing no significant harm to the remaining objectives and meeting minimum standards on human rights and labour standards",
            unit : "",
            component : "FinancialShareFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "alignedActivities",
            label : "Aligned Activities",
            description : "",
            unit : "",
            component : "AlignedActivitiesFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "totalEnablingShare",
            label : "Total Enabling Share",
            description : "",
            unit : "",
            component : "PercentageFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "totalTransitionalShare",
            label : "Total Transitional Share",
            description : "",
            unit : "",
            component : "PercentageFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "opex",
        label : "OpEx",
        fields : [ {
            name : "totalAmount",
            label : "Total Amount",
            description : "Total OpEx for the financial year. Operating expenses (OpEx) are shorter term expenses required to meet the ongoing operational costs of running a business",
            unit : "",
            component : "DataPointFormField",
            evidenceDesired : true,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "totalNonEligibleShare",
            label : "Total Non-Eligible Share",
            description : "",
            unit : "",
            component : "FinancialShareFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "totalEligibleShare",
            label : "Total Eligible Share",
            description : "Percentage and absolute share of the OpEx that is part of a plan to meet taxonomy criteria for substantial contribution to climate change adaptation and relevant DNSH criteria",
            unit : "",
            component : "FinancialShareFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "totalEligibleNonAlignedShare",
            label : "Total Eligible Non-Aligned Share",
            description : "",
            unit : "",
            component : "FinancialShareFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "eligibleNonAlignedActivities",
            label : "Eligible Non-Aligned Activities",
            description : "",
            unit : "",
            component : "ActivitiesFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "totalAlignedShare",
            label : "Total Aligned Share",
            description : "Percentage and absolute share of the OpEx that is associated with taxonomy-aligned activities. I.e., for an eligible economic activity that is making a substantial contribution to at least one of the climate and environmental objectives, while also doing no significant harm to the remaining objectives and meeting minimum standards on human rights and labour standards",
            unit : "",
            component : "FinancialShareFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "alignedActivities",
            label : "Aligned Activities",
            description : "",
            unit : "",
            component : "AlignedActivitiesFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "totalEnablingShare",
            label : "Total Enabling Share",
            description : "",
            unit : "",
            component : "PercentageFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "totalTransitionalShare",
            label : "Total Transitional Share",
            description : "",
            unit : "",
            component : "PercentageFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    } ]
} ] as Array<Category>;
