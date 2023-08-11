import { EuTaxonomyDataForNonFinancials } from "@clients/backend";
import { Category } from "@/utils/GenericFrameworkTypes";


export const euTaxonomyForNonFinancialsDisplayDataModel = [ {
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
    } ]
}, {
    name : "revenue",
    label : "Revenue",
    color : "orange",
    showIf : (): boolean => true,
    subcategories : [ {
        name : "totalAmount",
        label : "Total Amount",
        fields : [ {
            name : "quality",
            label : "Quality",
            description : "The quality of the provided data",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "value",
            label : "Value",
            description : "The provided data",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "dataSource",
            label : "Data Source",
            description : "",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => false
        }, {
            name : "comment",
            label : "Comment",
            description : "",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => false
        } ]
    }, {
        name : "totalNonEligibleShare",
        label : "Total Non-Eligible Share",
        fields : [ {
            name : "percentage",
            label : "Percentage",
            description : "The relative share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "absoluteShare",
            label : "Absolute share",
            description : "The absolute share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "totalEligibleShare",
        label : "Total Eligible Share",
        fields : [ {
            name : "percentage",
            label : "Percentage",
            description : "The relative share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "absoluteShare",
            label : "Absolute share",
            description : "The absolute share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "totalEligibleNonAlignedShare",
        label : "Total Eligible Non-Aligned Share",
        fields : [ {
            name : "percentage",
            label : "Percentage",
            description : "The relative share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "absoluteShare",
            label : "Absolute share",
            description : "The absolute share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "eligibleNonAlignedActivities",
        label : "Eligible Non-Aligned Activities",
        fields : [ {
            name : "eligibleNonAlignedActivities",
            label : "Eligible Non-Aligned Activities",
            description : "",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "totalAlignedShare",
        label : "Total Aligned Share",
        fields : [ {
            name : "percentage",
            label : "Percentage",
            description : "The relative share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "absoluteShare",
            label : "Absolute share",
            description : "The absolute share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "alignedActivities",
        label : "Aligned Activities",
        fields : [ {
            name : "alignedActivities",
            label : "Aligned Activities",
            description : "",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "totalEnablingShare",
        label : "Total Enabling Share",
        fields : [ {
            name : "totalEnablingShare",
            label : "Total Enabling Share",
            description : "",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "totalTransitionalShare",
        label : "Total Transitional Share",
        fields : [ {
            name : "totalTransitionalShare",
            label : "Total Transitional Share",
            description : "",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    } ]
}, {
    name : "capex",
    label : "CapEx",
    color : "orange",
    showIf : (): boolean => true,
    subcategories : [ {
        name : "totalAmount",
        label : "Total Amount",
        fields : [ {
            name : "quality",
            label : "Quality",
            description : "The quality of the provided data",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "value",
            label : "Value",
            description : "The provided data",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "dataSource",
            label : "Data Source",
            description : "",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => false
        }, {
            name : "comment",
            label : "Comment",
            description : "",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => false
        } ]
    }, {
        name : "totalNonEligibleShare",
        label : "Total Non-Eligible Share",
        fields : [ {
            name : "percentage",
            label : "Percentage",
            description : "The relative share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "absoluteShare",
            label : "Absolute share",
            description : "The absolute share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "totalEligibleShare",
        label : "Total Eligible Share",
        fields : [ {
            name : "percentage",
            label : "Percentage",
            description : "The relative share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "absoluteShare",
            label : "Absolute share",
            description : "The absolute share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "totalEligibleNonAlignedShare",
        label : "Total Eligible Non-Aligned Share",
        fields : [ {
            name : "percentage",
            label : "Percentage",
            description : "The relative share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "absoluteShare",
            label : "Absolute share",
            description : "The absolute share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "eligibleNonAlignedActivities",
        label : "Eligible Non-Aligned Activities",
        fields : [ {
            name : "eligibleNonAlignedActivities",
            label : "Eligible Non-Aligned Activities",
            description : "",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "totalAlignedShare",
        label : "Total Aligned Share",
        fields : [ {
            name : "percentage",
            label : "Percentage",
            description : "The relative share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "absoluteShare",
            label : "Absolute share",
            description : "The absolute share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "alignedActivities",
        label : "Aligned Activities",
        fields : [ {
            name : "alignedActivities",
            label : "Aligned Activities",
            description : "",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "totalEnablingShare",
        label : "Total Enabling Share",
        fields : [ {
            name : "totalEnablingShare",
            label : "Total Enabling Share",
            description : "",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "totalTransitionalShare",
        label : "Total Transitional Share",
        fields : [ {
            name : "totalTransitionalShare",
            label : "Total Transitional Share",
            description : "",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    } ]
}, {
    name : "opex",
    label : "OpEx",
    color : "orange",
    showIf : (): boolean => true,
    subcategories : [ {
        name : "totalAmount",
        label : "Total Amount",
        fields : [ {
            name : "quality",
            label : "Quality",
            description : "The quality of the provided data",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "value",
            label : "Value",
            description : "The provided data",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "dataSource",
            label : "Data Source",
            description : "",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => false
        }, {
            name : "comment",
            label : "Comment",
            description : "",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => false
        } ]
    }, {
        name : "totalNonEligibleShare",
        label : "Total Non-Eligible Share",
        fields : [ {
            name : "percentage",
            label : "Percentage",
            description : "The relative share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "absoluteShare",
            label : "Absolute share",
            description : "The absolute share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "totalEligibleShare",
        label : "Total Eligible Share",
        fields : [ {
            name : "percentage",
            label : "Percentage",
            description : "The relative share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "absoluteShare",
            label : "Absolute share",
            description : "The absolute share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "totalEligibleNonAlignedShare",
        label : "Total Eligible Non-Aligned Share",
        fields : [ {
            name : "percentage",
            label : "Percentage",
            description : "The relative share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "absoluteShare",
            label : "Absolute share",
            description : "The absolute share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "eligibleNonAlignedActivities",
        label : "Eligible Non-Aligned Activities",
        fields : [ {
            name : "eligibleNonAlignedActivities",
            label : "Eligible Non-Aligned Activities",
            description : "",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "totalAlignedShare",
        label : "Total Aligned Share",
        fields : [ {
            name : "percentage",
            label : "Percentage",
            description : "The relative share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        }, {
            name : "absoluteShare",
            label : "Absolute share",
            description : "The absolute share on the financial asset",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "alignedActivities",
        label : "Aligned Activities",
        fields : [ {
            name : "alignedActivities",
            label : "Aligned Activities",
            description : "",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "totalEnablingShare",
        label : "Total Enabling Share",
        fields : [ {
            name : "totalEnablingShare",
            label : "Total Enabling Share",
            description : "",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    }, {
        name : "totalTransitionalShare",
        label : "Total Transitional Share",
        fields : [ {
            name : "totalTransitionalShare",
            label : "Total Transitional Share",
            description : "",
            unit : "",
            component : "UndefinedFormField",
            evidenceDesired : false,
            required : false,
            showIf : (): boolean => true
        } ]
    } ]
} ] as Array<Category>;
