import { admin_name, admin_pw, reader_name, reader_pw, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { generateCompanyInformation } from "@e2e/fixtures/CompanyFixtures";
import { generateLksgData } from "@e2e/fixtures/lksg/LksgDataFixtures";
import { uploadCompanyAndLksgDataViaApi } from "@e2e/utils/LksgUpload";
import { faker } from "@faker-js/faker";
import { humanizeString } from "@/utils/StringHumanizer";
import { DataTypeEnum } from "@clients/backend";
import { describeIf } from "@e2e/support/TestUtility";

describe(
  "As a user, I expect the search functionality on the /companies page to behave as I expect",
  { scrollBehavior: false },
  function () {
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
            return uploadCompanyAndLksgDataViaApi(token, companyInformation, generateLksgData());
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
          cy.intercept("**/api/public/companies*onlyCurrentUserAsUploader*").as(companiesInterceptAlias);
          cy.get(getTabSelector(1)).click();
          cy.wait(`@${companiesInterceptAlias}`, { timeout: Cypress.env("long_timeout_in_ms") as number });
          cy.url().should("contain", "/datasets");
          validateTabBar(1);

          cy.get(newDatasetButtonSelector).should("exist");

          const expectedHeaders = ["COMPANY", "DATA FRAMEWORK", "SUBMISSION DATE"];
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

        it("Check that a user who has no dataset associated with him has no table displayed", () => {
          cy.ensureLoggedIn(admin_name, admin_pw);
          cy.visit("/datasets");
          cy.get(newDatasetButtonSelector).should("exist");
          cy.get("table").parent().parent().parent().should("have.class", "hidden");
        });

        it("Check that a user who has no upload permission has no new dataset button displayed", () => {
          cy.ensureLoggedIn(reader_name, reader_pw);
          cy.visit("/datasets");
          cy.contains("No datasets uploaded").should("exist");
          cy.get(newDatasetButtonSelector).should("not.exist");
        });

        let newCompanyName = "";
        it("Check if search filter works as expected", () => {
          cy.ensureLoggedIn(uploader_name, uploader_pw);
          cy.visit("/datasets");
          newCompanyName = `Overview Search ${faker.company.bsNoun()}`;
          cy.get(searchBarSelector).type(newCompanyName);
          cy.get("tbody td").then((elements) => {
            expect(elements.length).to.equal(1);
          });
          uploadCompanyWithLksgDataset(newCompanyName);
          cy.reload();
          cy.visit("/datasets");
          cy.get(searchBarSelector).type(newCompanyName);
          cy.get("tbody td").then((elements) => {
            expect(elements.length).to.equal(4);
          });
          cy.get("tbody td").first().should("contain", newCompanyName);
        });

        it("Check if the table rows look as expected", () => {
          cy.ensureLoggedIn(uploader_name, uploader_pw);
          cy.visit("/datasets");
          cy.get(searchBarSelector).type(newCompanyName);
          const expectedRowContents = [
            `COMPANY${newCompanyName}`,
            `DATA FRAMEWORK${humanizeString(DataTypeEnum.Lksg)}`,
          ];
          cy.get("tbody td").each((element, index) => {
            if (index < expectedRowContents.length) {
              expect(element.text()).to.equal(expectedRowContents[index]);
            } else if (index == 2) {
              expect(Date.parse(element.text().substring(15)).toString()).not.to.equal(NaN.toString());
            } else if (index == 3) {
              expect(element.text()).to.contain("VIEW");
            }
          });
          cy.get("tbody td a").click();
          cy.url().should("contain", `/frameworks/${DataTypeEnum.Lksg}`);
        });
      }
    );
  }
);
