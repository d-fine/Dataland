import { DataTypeEnum, EuTaxonomyDataForFinancials, StoredCompany } from "@clients/backend";
import { describeIf } from "@e2e/support/TestUtility";
import { generateDummyCompanyInformation } from "@e2e/utils/CompanyUpload";
import {
  fillEligibilityKpis,
  fillEuTaxonomyForFinancialsRequiredFields,
  fillField,
} from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { uploadCompanyViaApiAndEuTaxonomyDataViaForm } from "@e2e/utils/GeneralApiUtils";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";

describeIf(
  "As a user, I expect to be able to edit datasets with multiple reporting periods",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  function () {
    let testData: FixtureData<EuTaxonomyDataForFinancials>;
    const uuid = new Date().getTime();
    const companyName = `company-for-testing-edit-button-${uuid}`;
    const testCompany = generateDummyCompanyInformation(companyName);

    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures").then(function (jsonContent) {
        const preparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
        testData = getPreparedFixture("company-for-all-types", preparedFixtures);
      });
    });

    it("Check whether newly added dataset has Pending status and can be approved by a reviewer", () => {
      uploadCompanyViaApiAndEuTaxonomyDataViaForm<EuTaxonomyDataForFinancials>(
        DataTypeEnum.EutaxonomyFinancials,
        testCompany,
        testData.t,
        (data, company) => addTwoDatasetsWithDifferentReportingPeriods(data, company),
        () => void 0,
        testEditDataButton
      );
    });
  }
);

/**
 * Adds two new datasets with different reporting periods
 * @param data the data to fill the form with
 * @param storedCompany details of the company that was created
 */
function addTwoDatasetsWithDifferentReportingPeriods(
  data: EuTaxonomyDataForFinancials,
  storedCompany: StoredCompany
): void {
  addCreditInstitutionDataset(data, "2022");
  cy.pause();
  cy.visitAndCheckAppMount(
    `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}/upload`
  );
  addCreditInstitutionDataset(data, "2021");
}

/**
 * Fills the eutaxonomy-financials upload form with the given dataset
 * @param data the data to fill the form with
 * @param reportingPeriod (optional) to specify reporting period
 */
function addCreditInstitutionDataset(data: EuTaxonomyDataForFinancials, reportingPeriod?: string): void {
  fillEuTaxonomyForFinancialsRequiredFields(data, reportingPeriod);

  cy.get('[data-test="MultiSelectfinancialServicesTypes"]')
    .click()
    .get("div.p-multiselect-panel")
    .find("li.p-multiselect-item")
    .first()
    .click({ force: true });

  cy.get('[data-test="addKpisButton"]').click({ force: true });

  fillEligibilityKpis("creditInstitutionKpis", data.eligibilityKpis?.CreditInstitution);
  fillField(
    "creditInstitutionKpis",
    "tradingPortfolioAndInterbankLoans",
    data.creditInstitutionKpis?.tradingPortfolioAndInterbankLoans
  );
  fillField("creditInstitutionKpis", "tradingPortfolio", data.creditInstitutionKpis?.tradingPortfolio);
  fillField("creditInstitutionKpis", "interbankLoans", data.creditInstitutionKpis?.interbankLoans);
  fillField("creditInstitutionKpis", "greenAssetRatio", data.creditInstitutionKpis?.greenAssetRatio);

  cy.wait(4000);
  cy.get('button[data-test="submitButton"]').click();
  cy.wait(4000);
  cy.visit("/companies").wait(1000);
}

/**
 * Tests that the item was added and is visible on the QA list
 */
function testEditDataButton(): void {
  cy.pause();
}
