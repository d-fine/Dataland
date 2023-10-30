import {type Category} from "@/utils/GenericFrameworkTypes";


export const euTaxonomyDataModel = [
    {
        "name": "general",
        "label": "General",
        "subcategories": [
            {
                "name": "Social",
                "label": "Social",
                "fields": [
                    {
                        "name": "referencedReports",
                        "label": "Referenced Reports",
                        "description": "Please upload all relevant reports for this dataset in the PDF format.",
                        "unit": "",
                        "component": "UploadReports",
                        "evidenceDesired": false,
                        "required": false
                    },
                    {
                        "name": "assurance",
                        "label": "Assurance",
                        "description": "Level of Assurance of the EU Taxonomy disclosure (Reasonable Assurance, Limited Assurance, None)",
                        "unit": "",
                        "component": "AssuranceFormField",
                        "evidenceDesired": true,
                        "required": false
                    },
                    {
                        "label": "Fiscal Year Deviation",
                        "name": "fiscalYearDeviation",
                        "description": "Fiscal Year (Deviation/ No Deviation)",
                        "component": "RadioButtonsFormField",
                        "options": [
                            {
                                "label": "Deviation",
                                "value": "Deviation"
                            },
                            {
                                "label": "No Deviation",
                                "value": "NoDeviation"
                            }
                        ],
                        "unit": null,
                        "required": false
                    },
                    {
                        "label": "Fiscal Year End",
                        "name": "fiscalYearEnd",
                        "description": "The date the fiscal year ends",
                        "component": "DateFormField",
                        "unit": null,
                        "required": false
                    },
                    {
                        "label": "Scope Of Entities",
                        "name": "scopeOfEntities",
                        "description": "Does a list of legal entities covered by Sust./Annual/Integrated/ESEF report match with a list of legal entities covered by Audited Consolidated Financial Statement ",
                        "component": "YesNoNaFormField",
                        "required": true,
                        "unit": null
                    },
                    {
                        "label": "EU Taxonomy Activity Level Reporting",
                        "name": "euTaxonomyActivityLevelReporting",
                        "description": "Activity Level disclosure",
                        "component": "YesNoFormField",
                        "required": true,
                        "unit": null
                    },
                    {
                        "label": "Number Of Employees",
                        "name": "numberOfEmployees",
                        "description": "Total number of employees (including temporary workers)",
                        "component": "NumberFormField",
                        "unit": null,
                        "required": false
                    },
                    {
                        "label": "NFRD Mandatory",
                        "name": "nfrdMandatory",
                        "description": "The reporting obligation for companies whose number of employees is greater or equal to 500",
                        "component": "YesNoFormField",
                        "unit": null,
                        "required": false
                    }
                ]
            },
            {
                "name": "environmental",
                "label": "Environmental",
                "fields": [
                    {
                        "label": "Exposures To Taxonomy-eligible Economic Activities Credit Institution",
                        "name": "exposuresToTaxonomyEligibleEconomicActivitiesCreditInstitution",
                        "description": "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Exposures To Taxonomy Non-eligible Economic Activities Credit Institution",
                        "name": "exposuresToTaxonomyNonEligibleEconomicActivitiesCreditInstitution",
                        "description": "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. I.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Exposures To Central Governments, Central Banks, Supranational Issuers Credit Institution",
                        "name": "exposuresToCentralGovernmentsCentralBanksSupranationalIssuersCreditInstitution",
                        "description": "For financial companies (FS), the percentage of total assets exposed to central governments, central banks, supranational issuers",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Exposures To Derivatives Credit Institution",
                        "name": "exposuresToDerivativesCreditInstitution",
                        "description": "For financial companies (FS), the percentage of total assets exposed to derivatives",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Exposures To Non-NFRD Entities Credit Institution",
                        "name": "exposuresToNonNfrdEntitiesCreditInstitution",
                        "description": "For financial companies (FS), the percentage of total assets exposed to non-NFRD entities",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Trading Portfolio",
                        "name": "tradingPortfolio",
                        "description": "For Credit Institutions, the trading portfolio as a percentage of total assets",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "On-demand Interbank Loans",
                        "name": "onDemandInterbankLoans",
                        "description": "For Credit Institutions, the on demand interbank loans as a percentage of total assets",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Trading Portfolio & On-demand Interbank Loans",
                        "name": "tradingPortfolioOnDemandInterbankLoans",
                        "description": "For Credit Institutions, the trading portfolio and the on demand interbank loans as a percentage of total assets",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Exposures To Taxonomy-eligible Economic Activities Insurance/Reinsurance",
                        "name": "exposuresToTaxonomyEligibleEconomicActivitiesInsuranceReinsurance",
                        "description": "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Exposures To Taxonomy Non-eligible Economic Activities Insurance/Reinsurance",
                        "name": "exposuresToTaxonomyNonEligibleEconomicActivitiesInsuranceReinsurance",
                        "description": "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. I.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Exposures To Central Governments, Central Banks, Supranational Issuers Insurance/Reinsurance",
                        "name": "exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInsuranceReinsurance",
                        "description": "For financial companies (FS), the percentage of total assets exposed to central governments, central banks, supranational issuers",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Exposures To Derivatives Insurance/Reinsurance",
                        "name": "exposuresToDerivativesInsuranceReinsurance",
                        "description": "For financial companies (FS), the percentage of total assets exposed to derivatives",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Exposures To Non-NFRD Entities Insurance/Reinsurance",
                        "name": "exposuresToNonNfrdEntitiesInsuranceReinsurance",
                        "description": "For financial companies (FS), the percentage of total assets exposed to non-NFRD entities",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Taxonomy-eligible Non-life Insurance Economic Activities",
                        "name": "taxonomyEligibleNonLifeInsuranceEconomicActivities",
                        "description": "For Insurance/Reinsurance companies, the percentage of Taxonomy-eligible non-life insurance economics activities. Insurance and reinsurance undertakings other than life insurance undertakings shall calculate the KPI related to underwriting activities and present the ‘gross premiums written’ non-life insurance revenue or, as applicable, reinsurance revenue corresponding to Taxonomy-aligned insurance or reinsurance activities",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Exposures To Taxonomy-eligible Economic Activities Asset Management Company",
                        "name": "exposuresToTaxonomyEligibleEconomicActivitiesAssetManagementCompany",
                        "description": "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Exposures To Taxonomy Non-eligible Economic Activities Asset Management Company",
                        "name": "exposuresToTaxonomyNonEligibleEconomicActivitiesAssetManagementCompany",
                        "description": "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. I.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Exposures To Central Governments, Central Banks, Supranational Issuers Asset Management Company",
                        "name": "exposuresToCentralGovernmentsCentralBanksSupranationalIssuersAssetManagementCompany",
                        "description": "For financial companies (FS), the percentage of total assets exposed to central governments, central banks, supranational issuers",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Exposures To Derivatives Asset Management Company",
                        "name": "exposuresToDerivativesAssetManagementCompany",
                        "description": "For financial companies (FS), the percentage of total assets exposed to derivatives",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Exposures To Non-NFRD Entities Asset Management Company",
                        "name": "exposuresToNonNfrdEntitiesAssetManagementCompany",
                        "description": "For financial companies (FS), the percentage of total assets exposed to non-NFRD entities",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Exposures To Taxonomy-eligible Economic Activities Investment Firm",
                        "name": "exposuresToTaxonomyEligibleEconomicActivitiesInvestmentFirm",
                        "description": "For financial companies (FS), the percentage of total assets exposed to taxonomy-eligible economic activities",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Exposures To Taxonomy Non-eligible Economic Activities Investment Firm",
                        "name": "exposuresToTaxonomyNonEligibleEconomicActivitiesInvestmentFirm",
                        "description": "For financial companies (FS), the percentage of total assets exposed to taxonomy non-eligible economic activities. I.e., to types of economic activity that is not described and does not have technical screening criteria set out in the EU Taxonomy Regulation",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Exposures To Central Governments, Central Banks, Supranational Issuers Investment Firm",
                        "name": "exposuresToCentralGovernmentsCentralBanksSupranationalIssuersInvestmentFirm",
                        "description": "For financial companies (FS), the percentage of total assets exposed to central governments, central banks, supranational issuers",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Exposures To Derivatives Investment Firm",
                        "name": "exposuresToDerivativesInvestmentFirm",
                        "description": "For financial companies (FS), the percentage of total assets exposed to derivatives",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Exposures To Non-NFRD Entities Investment Firm",
                        "name": "exposuresToNonNfrdEntitiesInvestmentFirm",
                        "description": "For financial companies (FS), the percentage of total assets exposed to non-NFRD entities",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Green Asset Ratio Credit Institution",
                        "name": "greenAssetRatioCreditInstitution",
                        "description": "The proportion of the of credit institution’s assets financing and invested in taxonomy-aligned economic activities as a proportion of total covered assets",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    },
                    {
                        "label": "Green Asset Ratio Investment Firm",
                        "name": "greenAssetRatioInvestmentFirm",
                        "description": "The proportion of the of credit institution’s assets financing and invested in taxonomy-aligned economic activities as a proportion of total covered assets",
                        "component": "DataPointFormField",
                        "unit": "%",
                        "required": false
                    }
                ]
            }
        ]
    }
] as Array<Category>;