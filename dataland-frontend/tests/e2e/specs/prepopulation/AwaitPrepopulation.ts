import { CompanyInformation, EuTaxonomyData } from "../../../../build/clients/backend/api";
import { retrieveCompanyIdsList } from "../../utils/ApiUtils";

describe("I want to ensure that the prepopulation has finished before executing any further tests", () => {
  let companiesWithData: Array<{ companyInformation: CompanyInformation; euTaxonomyData: EuTaxonomyData }>;
  before(function () {
    cy.fixture("CompanyInformationWithEuTaxonomyData").then(function (companies) {
      companiesWithData = companies;
    });
  });

  it(
    "Should wait until prepopulation has finished",
    {
      retries: {
        runMode: 250,
        openMode: 250,
      },
    },
    () => {
      cy.wait(5000)
        .then(() => retrieveCompanyIdsList())
        .then((ids) => {
          if (ids.length < companiesWithData.length) {
            throw Error(`Only found ${ids.length} companies (Expecting ${companiesWithData.length})`);
          }
        });
    }
  );
});
