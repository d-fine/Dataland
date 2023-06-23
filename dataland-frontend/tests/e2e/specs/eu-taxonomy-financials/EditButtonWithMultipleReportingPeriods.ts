import { DataTypeEnum, EuTaxonomyDataForFinancials, StoredCompany } from "@clients/backend";
import { describeIf } from "@e2e/support/TestUtility";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation, prepareUniqueCompany, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { admin_name, admin_pw } from "@e2e/utils/Cypress";
import { prepareFixture, uploadOneEuTaxonomyFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";

describeIf(
  "As a user, I expect to be able to edit datasets with multiple reporting periods",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  function () {
    let testData: FixtureData<EuTaxonomyDataForFinancials>;
    const testCompany = prepareUniqueCompany("company-for-testing-edit-button");

    before(function () {
      const fixtureType = "CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures";
      prepareFixture<EuTaxonomyDataForFinancials>(fixtureType).then((preparedFixtures) => {
        testData = getPreparedFixture("company-for-all-types", preparedFixtures);
      });
    });

    it("Check whether newly added dataset has Pending status and can be approved by a reviewer", () => {
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompany.companyName)).then(
          (storedCompany): void => {
            cy.ensureLoggedIn(admin_name, admin_pw);

            const datasetFor2022 = uploadOneEuTaxonomyFinancialsDatasetViaApi(
              token,
              storedCompany.companyId,
              "2022",
              testData.t
            );

            const datasetFor2021 = uploadOneEuTaxonomyFinancialsDatasetViaApi(
              token,
              storedCompany.companyId,
              "2021",
              testData.t
            );

            Promise.all([datasetFor2022, datasetFor2021])
              .then(() => testEditDataButton(storedCompany))
              .catch(console.error);
          }
        );
      });
    });
  }
);

/**
 * Tests that the item was added and is visible on the QA list
 * @param storedCompany details of the company that was created
 */
function testEditDataButton(storedCompany: StoredCompany): void {
  cy.visitAndCheckAppMount(`/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}`);

  cy.get('[data-test="editDatasetButton"').should("exist").click();
  cy.get('[data-test="select-reporting-period-dialog"')
    .should("exist")
    .get('[data-test="reporting-periods"')
    .last()
    .should("contain", "2021")
    .click();

  cy.get('[data-test="companyNameTitle"').should("contain", storedCompany.companyInformation.companyName);
  cy.get('[data-test="reportingPeriod" input').should("contain", "2021");
}
