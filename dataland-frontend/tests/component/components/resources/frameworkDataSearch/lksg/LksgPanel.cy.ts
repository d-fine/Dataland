import LksgPanel from "@/components/resources/frameworkDataSearch/lksg/LksgPanel.vue";
import { DataAndMetaInformationLksgData, DatasetQualityStatus, LksgData } from "@clients/backend";
import { mount } from "cypress/vue";
import Keycloak from "keycloak-js";

describe("Component test for LksgPanel", () => {
  it("Should display the total revenue kpi in the correct format", () => {
    const pseudoLksgData = { social: { general: { dataDate: "2023-01-01", totalRevenue: 1234567.89 } } };
    mount(LksgPanel, {
      data() {
        return {
          waitingForData: false,
          lksgDataAndMetaInfo: [{ data: pseudoLksgData as LksgData } as DataAndMetaInformationLksgData],
        };
      },
      created() {
        (this.convertLksgDataToFrontendFormat as () => void)();
      },
    });
    cy.get("td:contains('1.23 MM')").should("exist");
  });

  it("Should display only accepted datasets", () => {
    cy.intercept("**/api/data/lksg/company/*", {
      statusCode: 200,
      body: [
        {
          metaInfo: { dataId: "1", qualityStatus: DatasetQualityStatus.Accepted },
          data: { social: { general: { dataDate: "2023-01-01" } } },
        },
        {
          metaInfo: { dataId: "2", qualityStatus: DatasetQualityStatus.Pending },
          data: { social: { general: { dataDate: "2024-01-01" } } },
        },
      ],
    }).as("alias");
    mount(LksgPanel, {
      setup() {
        return {
          getKeycloakPromise: (): Promise<Keycloak> => {
            return Promise.resolve(new Keycloak());
          },
        };
      },
      props: {
        companyId: {
          type: String,
          default: "",
        },
      },
    });
    cy.get("td:contains('2023')").should("exist");
    cy.get("td:contains('2024')").should("not.exist");
  });
});
