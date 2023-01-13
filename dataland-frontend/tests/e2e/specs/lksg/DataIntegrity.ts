import { describeIf } from "@e2e/support/TestUtility";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { DataTypeEnum } from "@clients/backend";
import { getOneCompanyThatHasDataForDataType } from "../../utils/ApiUtils";

const timeout = 120 * 1000;
describeIf(
  "As a user, I expect Lksg data that I upload for a company to be displayed correctly",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
    });

    /*
        let preparedFixtures: Array<FixtureData<EuTaxonomyDataForNonFinancials>>;

        before(function () {
          cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancialsPreparedFixtures").then(function (jsonContent) {
            preparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForNonFinancials>>;
          });
        });



        function getPreparedFixture(name: string): FixtureData<EuTaxonomyDataForNonFinancials> {
          const preparedFixture = preparedFixtures.find((it): boolean => it.companyInformation.companyName == name)!;
          if (!preparedFixture) {
            throw new ReferenceError(
              "Variable preparedFixture is undefined because the provided company name could not be found in the prepared fixtures."
            );
          } else {
            return preparedFixture;
          }
        }
          TODO activate and adjust as soon as (or if) we have prepared fixtures for lskg
         */

    /**
     * todo
     */
    function pickOneUploadedLksgDataSetAndVerifyLksgPageForIt(): void {
      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return getOneCompanyThatHasDataForDataType(token, DataTypeEnum.Lksg).then((storedCompany) => {
          // TODO get one lksg data set for this company and put it into a variable
          // TODO then start to do the actual test:
          cy.intercept("**/api/data/lksg/*").as("retrieveLksgData");
          cy.visitAndCheckAppMount(`/companies/${storedCompany.companyId}/frameworks/lksg`);
          cy.wait("@retrieveLksgData", { timeout: timeout }).then(() => {
            // TODO verify code by comparing the displayed data to the actual lksg dataset and by general
            // TODO checks
          });
        });
      });
    }

    it("Visit Lksg view page for an existing Lksg dataset and verify that it is displayed as expected", () => {
      pickOneUploadedLksgDataSetAndVerifyLksgPageForIt();
    });
  }
);
