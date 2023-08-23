import ThreeLayerDataTable from "@/components/resources/frameworkDataSearch/ThreeLayerDataTable.vue"
import {type FixtureData, getPreparedFixture} from "@sharedUtils/Fixtures";
import {
    DataAndMetaInformationNewEuTaxonomyDataForNonFinancials,
    type NewEuTaxonomyDataForNonFinancials,
} from "@clients/backend";
import {minimalKeycloakMock} from "../../testUtils/Keycloak";
import {
    newEuTaxonomyForNonFinancialsDisplayDataModel
} from "../../../../src/components/resources/frameworkDataSearch/euTaxonomy/NewEuTaxonomyForNonFinancialsDisplayDataModel";
import {
    DataAndMetaInformationNewEuTaxonomyForNonFinancialsViewModel,
    NewEuTaxonomyForNonFinancialsViewModel
} from "../../../../src/components/resources/frameworkDataSearch/euTaxonomy/NewEuTaxonomyForNonFinancialsViewModel";
import {DataAndMetaInformationViewModel, FrameworkViewModel} from "../../../../src/components/resources/ViewModel";
describe("Component test for the NewEUTaxonomy Page", () => {


    //const kpiList:string[] = ["GENERAL", "REVENUE", "CAPEX", "OPEX"]; These 2 lists have to be used when running the cypress test locally, because the fixtures are different.
    //const kpiListOrderChanged = ["REVENUE", "GENERAL", "OPEX", "CAPEX"];
    const kpiList:string[] = ["BASIC INFORMATION","ASSURANCE","REVENUE","CAPEX", "OPEX"];
    const dataTestList:string[] = ["Basic Information","Assurance","Revenue","CapEx", "OpEx"];
    const kpiListOrderChanged = ["ASSURANCE","OPEX", "BASIC INFORMATION", "REVENUE", "CAPEX"];

    const subcategoryList:string[][] = [["Basic Information"],
        ["Assurance"],
        ["Total Aligned Revenue", "Total Revenue", "Total Eligible Revenue", "Total Non-Aligned Revenue", "Total Non-Eligible Revenue"],
        ["Total Aligned CapEx", "Total CapEx", "Total Eligible CapEx", "Total Non-Aligned CapEx", "Total Non-Eligible CapEx"],
        ["Total Aligned OpEx", "Total OpEx", "Total Eligible OpEx", "Total Non-Aligned OpEx", "Total Non-Eligible OpEx"]];

    const subcategoryDataTestList:string[][] = [["_basicInformation"],
        ["assurance"],
        ["totalAlignedShare", "totalAmount", "totalEligibleShare", "totalNonAlignedShare", "totalNonEligibleShare"],
        ["totalAlignedShare", "totalAmount", "totalEligibleShare", "totalNonAlignedShare", "totalNonEligibleShare"],
        ["totalAlignedShare", "totalAmount", "totalEligibleShare", "totalNonAlignedShare", "totalNonEligibleShare"]];

    it("Check order of the displayed KPIs and category entries", () => {

        const mockData: DataAndMetaInformationNewEuTaxonomyDataForNonFinancials = {
            metaInfo: {
                "dataId":"a9d75a0a-some-fake-dataId-549632b19782",
                "companyId":"1e946cac-some-fake-ID-762f40e",
                "dataType":"eutaxonomy-financials",
                "uploaderUserId":"c5ef10b1-some-fake-uploaderUserId-e62ea226ee83",
                "uploadTime":1678194542,
                "reportingPeriod":"2019",
                "currentlyActive":true,
                "qaStatus":"Accepted"
            },
            data: {
                "general": {
                    "fiscalYearDeviation": "Deviation",
                    "fiscalYearEnd": "2023-08-23",
                    "scopeOfEntities": "Yes",
                    "nfrdMandatory": "Yes",
                    "euTaxonomyActivityLevelReporting": "Yes",
                    "assurance": {
                        "assurance": "None",
                        "provider": "string",
                        "dataSource": {
                            "report": "string",
                            "page": 0,
                            "tagName": "string"
                        }
                    },
                    "numberOfEmployees": 0,
                    "referencedReports": {
                        "additionalProp1": {
                            "reference": "string",
                            "isGroupLevel": "Yes",
                            "reportDate": "2023-08-23",
                            "currency": "string"
                        },
                        "additionalProp2": {
                            "reference": "string",
                            "isGroupLevel": "Yes",
                            "reportDate": "2023-08-23",
                            "currency": "string"
                        },
                        "additionalProp3": {
                            "reference": "string",
                            "isGroupLevel": "Yes",
                            "reportDate": "2023-08-23",
                            "currency": "string"
                        }
                    }
                },
                "revenue": {
                    "totalAmount": {
                        "quality": "Audited",
                        "dataSource": {
                            "report": "string",
                            "page": 0,
                            "tagName": "string"
                        },
                        "comment": "string",
                        "value": {
                            "amount": 0,
                            "currency": "string"
                        }
                    },
                    "totalNonEligibleShare": {
                        "relativeShareInPercent": 0,
                        "absoluteShare": {
                            "amount": 0,
                            "currency": "string"
                        }
                    },
                    "totalEligibleShare": {
                        "relativeShareInPercent": 0,
                        "absoluteShare": {
                            "amount": 0,
                            "currency": "string"
                        }
                    },
                    "totalNonAlignedShare": {
                        "relativeShareInPercent": 0,
                        "absoluteShare": {
                            "amount": 0,
                            "currency": "string"
                        }
                    },
                    "nonAlignedActivities": [
                        {
                            "activityName": "Afforestation",
                            "naceCodes": [
                                "string"
                            ],
                            "share": {
                                "relativeShareInPercent": 0,
                                "absoluteShare": {
                                    "amount": 0,
                                    "currency": "string"
                                }
                            }
                        }
                    ],
                    "totalAlignedShare": {
                        "relativeShareInPercent": 0,
                        "absoluteShare": {
                            "amount": 0,
                            "currency": "string"
                        }
                    },
                    "substantialContributionCriteria": {
                        "ClimateMitigation": 20,
                        "ClimateAdaptation": 20,
                        "Water": 20
                    },
                    "alignedActivities": [
                        {
                            "activityName": "Afforestation",
                            "naceCodes": [
                                "string"
                            ],
                            "share": {
                                "relativeShareInPercent": 0,
                                "absoluteShare": {
                                    "amount": 0,
                                    "currency": "string"
                                }
                            },
                            "substantialContributionCriteria": {
                                "ClimateMitigation": 20,
                                "ClimateAdaptation": 20,
                                "Water": 20
                            },
                            "dnshCriteria": {
                                "ClimateMitigation": "Yes",
                                "ClimateAdaptation": "Yes",
                                "Water": "No"
                            },
                            "minimumSafeguards": "Yes"
                        }
                    ],
                    "totalEnablingShare": 0,
                    "totalTransitionalShare": 0
                },
                "capex": {
                    "totalAmount": {
                        "quality": "Audited",
                        "dataSource": {
                            "report": "string",
                            "page": 0,
                            "tagName": "string"
                        },
                        "comment": "string",
                        "value": {
                            "amount": 0,
                            "currency": "string"
                        }
                    },
                    "totalNonEligibleShare": {
                        "relativeShareInPercent": 0,
                        "absoluteShare": {
                            "amount": 0,
                            "currency": "string"
                        }
                    },
                    "totalEligibleShare": {
                        "relativeShareInPercent": 0,
                        "absoluteShare": {
                            "amount": 0,
                            "currency": "string"
                        }
                    },
                    "totalNonAlignedShare": {
                        "relativeShareInPercent": 0,
                        "absoluteShare": {
                            "amount": 0,
                            "currency": "string"
                        }
                    },
                    "nonAlignedActivities": [
                        {
                            "activityName": "Afforestation",
                            "naceCodes": [
                                "string"
                            ],
                            "share": {
                                "relativeShareInPercent": 0,
                                "absoluteShare": {
                                    "amount": 0,
                                    "currency": "string"
                                }
                            }
                        }
                    ],
                    "totalAlignedShare": {
                        "relativeShareInPercent": 0,
                        "absoluteShare": {
                            "amount": 0,
                            "currency": "string"
                        }
                    },
                    "substantialContributionCriteria": {
                        "ClimateMitigation": 20,
                        "ClimateAdaptation": 20,
                        "Water": 20
                    },
                    "alignedActivities": [
                        {
                            "activityName": "Afforestation",
                            "naceCodes": [
                                "string"
                            ],
                            "share": {
                                "relativeShareInPercent": 0,
                                "absoluteShare": {
                                    "amount": 0,
                                    "currency": "string"
                                }
                            },
                            "substantialContributionCriteria": {
                                "ClimateMitigation": 20,
                                "ClimateAdaptation": 20,
                                "Water": 20
                            },
                            "dnshCriteria": {
                                "ClimateMitigation": "Yes",
                                "ClimateAdaptation": "Yes",
                                "Water": "No"
                            },
                            "minimumSafeguards": "Yes"
                        }
                    ],
                    "totalEnablingShare": 0,
                    "totalTransitionalShare": 0
                },
                "opex": {
                    "totalAmount": {
                        "quality": "Audited",
                        "dataSource": {
                            "report": "string",
                            "page": 0,
                            "tagName": "string"
                        },
                        "comment": "string",
                        "value": {
                            "amount": 0,
                            "currency": "string"
                        }
                    },
                    "totalNonEligibleShare": {
                        "relativeShareInPercent": 0,
                        "absoluteShare": {
                            "amount": 0,
                            "currency": "string"
                        }
                    },
                    "totalEligibleShare": {
                        "relativeShareInPercent": 0,
                        "absoluteShare": {
                            "amount": 0,
                            "currency": "string"
                        }
                    },
                    "totalNonAlignedShare": {
                        "relativeShareInPercent": 0,
                        "absoluteShare": {
                            "amount": 0,
                            "currency": "string"
                        }
                    },
                    "nonAlignedActivities": [
                        {
                            "activityName": "Afforestation",
                            "naceCodes": [
                                "string"
                            ],
                            "share": {
                                "relativeShareInPercent": 0,
                                "absoluteShare": {
                                    "amount": 0,
                                    "currency": "string"
                                }
                            }
                        }
                    ],
                    "totalAlignedShare": {
                        "relativeShareInPercent": 0,
                        "absoluteShare": {
                            "amount": 0,
                            "currency": "string"
                        }
                    },
                    "substantialContributionCriteria": {
                        "ClimateMitigation": 20,
                        "ClimateAdaptation": 20,
                        "Water": 20
                    },
                    "alignedActivities": [
                        {
                            "activityName": "Afforestation",
                            "naceCodes": [
                                "string"
                            ],
                            "share": {
                                "relativeShareInPercent": 0,
                                "absoluteShare": {
                                    "amount": 0,
                                    "currency": "string"
                                }
                            },
                            "substantialContributionCriteria": {
                                "ClimateMitigation": 20,
                                "ClimateAdaptation": 20,
                                "Water": 20
                            },
                            "dnshCriteria": {
                                "ClimateMitigation": "Yes",
                                "ClimateAdaptation": "Yes",
                                "Water": "No"
                            },
                            "minimumSafeguards": "Yes"
                        }
                    ],
                    "totalEnablingShare": 0,
                    "totalTransitionalShare": 0
                }
            }
            }


        const singleMockDataAndMetaInfo = new DataAndMetaInformationNewEuTaxonomyForNonFinancialsViewModel(mockData);
        const dataAndMetaInfo: Array<DataAndMetaInformationNewEuTaxonomyForNonFinancialsViewModel>
            = [singleMockDataAndMetaInfo];

        cy.mountWithPlugins(ThreeLayerDataTable, {
            keycloak: minimalKeycloakMock({}),
            // eslint-disable-next-line @typescript-eslint/ban-ts-comment
            // @ts-ignore
            props: {
                dataModel: newEuTaxonomyForNonFinancialsDisplayDataModel,
                dataAndMetaInfo: dataAndMetaInfo,
            }
        }).then(() => {


            cy.get("[data-test='ThreeLayerTableTest']").get(".d-table-style")
                .each((element, index) => {
                    cy.wrap(element).eq(0).eq(0).get(".p-badge").eq(index).should("have.text", kpiList[index]);
                });

            cy.wait(50);

            cy.get("[data-test='ThreeLayerTableTest']").get(".d-table-style")
                .each((element, index) => {
                    cy.wrap(element).eq(0).eq(0).get(".p-badge").eq(index).should("not.have.text", kpiListOrderChanged[index]);
                });

            cy.get("[data-test='TwoLayerTest']").eq(0).get(" [data-test='_basicInformation'").should("contain", "Basic Information");
            cy.get(`[data-test='${dataTestList[0]}']`).click();

            //cy.get(".p-rowgroup-header").filter(':visible').eq(index).get(`span[data-test="${subcategoryDataTestList[index]}"]`).should("contain",`${subcategoryList[index]}`);

            cy.wait(3000);
            let row;
            for (let i = 1; i < dataTestList.length; i++) {
                row = subcategoryDataTestList[i];
                cy.get(`[data-test='${dataTestList[i]}']`).click();
                for (let j = 0; j < row.length; j++) {
                    cy.get(".p-rowgroup-header").filter(':visible').eq(j)
                        .get(`span[id="${row[j]}"]`)
                        .should("contain", `${subcategoryList[i][j]}`);

                }
                cy.get(`[data-test='${dataTestList[i]}']`).click();
                cy.wait(3000);
            }
        });
    })


});


