import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { generateCompanyInformation } from "@e2e/fixtures/CompanyFixtures";
import { generateLksgData } from "@e2e/fixtures/lksg/LksgDataFixtures";
import { uploadCompanyAndLksgDataViaApi } from "@e2e/utils/LksgUpload";
import { faker } from "@faker-js/faker";
import { describeIf } from "@e2e/support/TestUtility";

describe("As a user, I expect the View My Datasets page to behave as I expect", { scrollBehavior: false }, function () {
  describeIf(
    "",
    {
      executionEnvironments: ["developmentLocal", "ci"],
      dataEnvironments: ["fakeFixtures"],
    },
    () => {
      const newDatasetButtonSelector = "button[aria-label='New Dataset']";
      const searchBarSelector = "input";

      /**
       * Generates a new company object and uploads it via API
       *
       * @param companyName name of the generated company
       */
      function uploadCompanyWithLksgDataset(companyName: string): void {
        getKeycloakToken(uploader_name, uploader_pw).then((token) => {
          const companyInformation = generateCompanyInformation();
          companyInformation.companyName = companyName;
          return uploadCompanyAndLksgDataViaApi(token, companyInformation, generateLksgData(), "2023");
        });
      }

      /**
       * Get the selector for a tab given by input number
       *
       * @param tabIndex number identifying the tab
       * @returns the selector to choose a tab
       */
      function getTabSelector(tabIndex: number): string {
        return `.p-tabview-header[data-index="${tabIndex}"]`;
      }

      /**
       * Validates the tab bar identified by the input
       *
       * @param activeTabIndex number identifying the tab bar
       */
      function validateTabBar(activeTabIndex: number): void {
        cy.get(getTabSelector(0)).should("have.text", "AVAILABLE DATASETS");
        cy.get(getTabSelector(1)).should("have.text", "MY DATASETS");
        const inactiveTabIndex = (activeTabIndex + 1) % 2;
        cy.get(getTabSelector(activeTabIndex)).should("have.class", "p-highlight");
        cy.get(getTabSelector(inactiveTabIndex)).should("not.have.class", "p-highlight");
      }

      it("Check page content", function () {
        cy.ensureLoggedIn(uploader_name, uploader_pw);
        cy.visitAndCheckAppMount("/companies");
        validateTabBar(0);
        cy.wait(3000);
        const companiesInterceptAlias = "companies";
        cy.intercept("**/api/companies*onlyCurrentUserAsUploader*").as(companiesInterceptAlias);
        cy.get(getTabSelector(1)).click();
        cy.wait(`@${companiesInterceptAlias}`, { timeout: Cypress.env("long_timeout_in_ms") as number });
        cy.url().should("contain", "/datasets");
        validateTabBar(1);

        cy.get(newDatasetButtonSelector).should("exist");

        const expectedHeaders = ["COMPANY", "DATA FRAMEWORK", "SUBMISSION DATE", "REPORTING PERIOD", "STATUS"];
        expectedHeaders.forEach((value) => {
          cy.get(`table th:contains(${value})`).should("exist");
        });
        const unexpectedHeaders = ["YEAR"];
        unexpectedHeaders.forEach((value) => {
          cy.get(`table th:contains(${value})`).should("not.exist");
        });
        cy.get("th").each((element) => {
          if (!expectedHeaders.includes(element.text())) {
            expect(element.html()).to.contain("<input");
          }
        });
      });

      it("Check if new dataset button leads to choose company page and if the back button works as expected", () => {
        cy.ensureLoggedIn(uploader_name, uploader_pw);
        cy.visit("/datasets");
        validateTabBar(1);
        cy.get(newDatasetButtonSelector).click();
        cy.url().should("contain", "/companies/choose");
        cy.get("[title='back_button']").click();
        cy.url().should("contain", "/datasets");
      });

      let newCompanyName = "";
      it("Check if search filter works as expected", () => {
        cy.ensureLoggedIn(uploader_name, uploader_pw);
        cy.visit("/datasets");
        newCompanyName = `Overview Search ${faker.company.bsNoun()}`;
        cy.get(searchBarSelector).type(newCompanyName);
        cy.get("tbody td").should((elements) => {
          expect(elements.length).to.equal(1);
        });

        uploadCompanyWithLksgDataset(newCompanyName);
        cy.reload();
        cy.visit("/datasets");
        cy.get(searchBarSelector).type(newCompanyName);
        cy.get("tbody td").should((elements) => {
          expect(elements.length).to.equal(6);
        });
        cy.get("tbody td").first().should("contain", newCompanyName);
      });
    }
  );
});
