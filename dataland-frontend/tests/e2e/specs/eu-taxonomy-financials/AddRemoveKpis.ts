import { DataTypeEnum, EuTaxonomyDataForFinancials } from "@clients/backend";
import { describeIf } from "@e2e/support/TestUtility";
import { generateDummyCompanyInformation } from "@e2e/utils/CompanyUpload";
import { admin_name, admin_pw } from "@e2e/utils/Cypress";
import { fillAndValidateEuTaxonomyCreditInstitutionForm } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { uploadCompanyViaApiAndEuTaxonomyDataViaForm } from "@e2e/utils/GeneralUtils";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";

describeIf(
  "As a user, I expect to be able to add and remove Eligible KPIs and send the form successfully",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    onlyExecuteOnDatabaseReset: false,
  },
  function () {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
    });

    let testData: FixtureData<EuTaxonomyDataForFinancials>;
    const testCompany = generateDummyCompanyInformation("company-for-testing-kpi-sections");

    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures").then(function (jsonContent) {
        const preparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
        testData = getPreparedFixture("company-for-all-types", preparedFixtures);
      });
    });

    it("Check whether it is possible to add and delete KPIs and send the form successfully", () => {
      uploadCompanyViaApiAndEuTaxonomyDataViaForm<EuTaxonomyDataForFinancials>(
        DataTypeEnum.EutaxonomyFinancials,
        testCompany,
        testData.t,
        (data) => fillAndValidateEuTaxonomyCreditInstitutionForm(data),
        () => undefined,
        () => undefined
      );
    });
  }
);
