import NewEuTaxonomyForNonFinancialsPanel from "@/components/resources/frameworkDataSearch/euTaxonomy/NewEuTaxonomyForNonFinancialsPanel.vue"
import {type FixtureData, getPreparedFixture} from "@sharedUtils/Fixtures";
import {
    type NewEuTaxonomyDataForNonFinancials
} from "@clients/backend";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { NewEuTaxonomyForNonFinancialsViewModel
} from "@/components/resources/frameworkDataSearch/euTaxonomy/NewEuTaxonomyForNonFinancialsViewModel";
import {
    type newEuTaxonomyForNonFinancialsDisplayDataModel
} from "@/components/resources/frameworkDataSearch/euTaxonomy/NewEuTaxonomyForNonFinancialsDisplayDataModel";
describe("Component test for the NewEUTaxonomy Page", () => {
    let preparedFixtures: Array<FixtureData<NewEuTaxonomyDataForNonFinancials>>;

    before(() => {
        cy.fixture("CompanyInformationWithNewEuTaxonomyDataForNonFinancialsPreparedFixtures").then(function (jsonContent) {
            preparedFixtures = jsonContent as Array<FixtureData<NewEuTaxonomyDataForNonFinancials>>;
        });
    });


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
        const preparedFixture = getPreparedFixture("only-eligible-numbers", preparedFixtures);

        const mockData:NewEuTaxonomyDataForNonFinancials = {
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
        };

        const newEuTaxonomyForNonFinancialsViewModel = new NewEuTaxonomyForNonFinancialsViewModel(mockData);
        const dataAndMetaInfo: Array<NewEuTaxonomyForNonFinancialsViewModel> = [newEuTaxonomyForNonFinancialsViewModel];
        cy.mountWithPlugins(NewEuTaxonomyForNonFinancialsPanel, {
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


