import SfdrPanel from "@/components/resources/frameworkDataSearch/sfdr/SfdrPanel.vue";
import { QAStatus } from "@clients/backend";
import { mount } from "cypress/vue";
import Keycloak from "keycloak-js";

describe("Component test for Sfdr", () => {
  it("Should display only accepted datasets", () => {
    cy.intercept("**/api/data/sfdr/company/*", {
      statusCode: 200,
      body: [
        {
          metaInfo: { dataId: "1", qaStatus: QAStatus.Accepted },
          data: { social: { general: { fiscalYearEnd: "2023-01-01" } } },
        },
        {
          metaInfo: { dataId: "2", qaStatus: QAStatus.Pending },
          data: { social: { general: { fiscalYearEnd: "2024-01-01" } } },
        },
      ],
    });
    mount(SfdrPanel, {
      setup() {
        return {
          getKeycloakPromise: (): Promise<Keycloak> => {
            return Promise.resolve({} as Keycloak);
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
