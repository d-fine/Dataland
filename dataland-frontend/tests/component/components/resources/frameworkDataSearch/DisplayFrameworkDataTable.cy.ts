import TwoLayerDataTable from "@/components/resources/frameworkDataSearch/TwoLayerDataTable.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { DocumentReference, YesNo } from "@clients/backend";
import { KpiDataObject, KpiValue } from "@/components/resources/frameworkDataSearch/KpiDataObject";

describe("Component test for TwoLayerDataTable", () => {
  const dataId = "dummyId";
  const dummyDataSource = { name: "document", reference: "123" } as DocumentReference;
  const emptyStringDataSource = { name: "", reference: "" } as DocumentReference;
  it("Check that certificate/policy download links are displayed as expected", () => {
    const kpiDataObjects = [
      generateBaseDataPointKpi(YesNo.Yes, "Certification 1", dummyDataSource),
      generateBaseDataPointKpi(YesNo.No, "2 Certificate", dummyDataSource),
      generateBaseDataPointKpi(YesNo.Yes, "Certification 3", {} as DocumentReference),
      generateBaseDataPointKpi(YesNo.No, "Certification 4", {} as DocumentReference),
      generateBaseDataPointKpi(YesNo.Yes, "Certification A", emptyStringDataSource),
      generateBaseDataPointKpi(YesNo.No, "Certification B", emptyStringDataSource),
      generateBaseDataPointKpi(YesNo.Yes, "Certification 5"),
      generateBaseDataPointKpi(YesNo.Yes, "Policy 1", dummyDataSource),
      generateBaseDataPointKpi(YesNo.Yes, "Policy 2", {} as DocumentReference),
      generateBaseDataPointKpi(YesNo.No, "Policy 3", {} as DocumentReference),
      generateBaseDataPointKpi(YesNo.Yes, "Policy A", emptyStringDataSource),
      generateBaseDataPointKpi(YesNo.No, "Policy B", emptyStringDataSource),
    ];

    const reportingPeriodWithDataId = { dataId: dataId, reportingPeriod: "2023" };
    cy.mountWithPlugins(TwoLayerDataTable, {
      keycloak: minimalKeycloakMock({}),
      data() {
        return {
          arrayOfKpiDataObjects: kpiDataObjects,
          listOfReportingPeriodsWithDataId: [reportingPeriodWithDataId],
        };
      },
    });

    cy.contains("tr", "Certification 1").find("td").last().find("i[data-test=download-icon]").should("be.visible");
    cy.contains("tr", "Certification 1").find("td").last().find(".underline").should("contain.text", "Certified");
    cy.contains("tr", "2 Certificate").find("td").last().find(".underline").should("not.exist");
    cy.contains("tr", "2 Certificate").find("td").last().should("contain.text", "Uncertified");
    cy.contains("tr", "Certification 3").find("td").last().find(".underline").should("not.exist");
    cy.contains("tr", "Certification 3").find("td").last().should("contain.text", "Certified");
    cy.contains("tr", "Certification 4").find("td").last().find(".underline").should("not.exist");
    cy.contains("tr", "Certification 4").find("td").last().should("contain.text", "Uncertified");
    cy.contains("tr", "Certification A").find("td").last().find(".underline").should("not.exist");
    cy.contains("tr", "Certification A").find("td").last().should("contain.text", "Certified");
    cy.contains("tr", "Certification B").find("td").last().find(".underline").should("not.exist");
    cy.contains("tr", "Certification B").find("td").last().should("contain.text", "Uncertified");
    cy.contains("tr", "Certification 5").find("td").last().find(".underline").should("not.exist");
    cy.contains("tr", "Certification 5").find("td").last().should("contain.text", "Certified");
    cy.contains("tr", "Policy 1").find("td").last().find(".underline").should("contain.text", "Yes");
    cy.contains("tr", "Policy 2").find("td").last().find(".underline").should("not.exist");
    cy.contains("tr", "Policy 2").find("td").last().should("contain.text", "Yes");
    cy.contains("tr", "Policy 3").find("td").last().find(".underline").should("not.exist");
    cy.contains("tr", "Policy 3").find("td").last().should("contain.text", "No");
    cy.contains("tr", "Policy A").find("td").last().find(".underline").should("not.exist");
    cy.contains("tr", "Policy A").find("td").last().should("contain.text", "Yes");
    cy.contains("tr", "Policy B").find("td").last().find(".underline").should("not.exist");
    cy.contains("tr", "Policy B").find("td").last().should("contain.text", "No");
  });

  /**
   * Constructs a KpiDataObject object
   * @param value the value of the kpi
   * @param label the display name to describe the kpi
   * @param dataSource the dataSource to reference
   * @returns the constructed KpiDataObject object
   */
  function generateBaseDataPointKpi(value: YesNo, label: string, dataSource?: DocumentReference): KpiDataObject {
    return {
      subcategoryKey: "_masterData",
      subcategoryLabel: "Master Data",
      categoryKey: "_masterData",
      categoryLabel: "Master Data",
      kpiKey: "dummy",
      kpiLabel: label,
      kpiDescription: "",
      kpiFormFieldComponent: "",
      content: { [dataId]: { value: value, dataSource: dataSource } as KpiValue },
    } as KpiDataObject;
  }
});
