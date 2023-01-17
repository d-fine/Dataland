import { describeIf } from "@e2e/support/TestUtility";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { LksgData } from "@clients/backend";
import { uploadOneEuTaxonomyFinancialsDatasetViaApi } from "../../utils/EuTaxonomyFinancialsUpload";
import { generateEuTaxonomyDataForFinancials } from "../../fixtures/eutaxonomy/financials/EuTaxonomyDataForFinancialsFixtures";
import { getPreparedLksgFixture, uploadCompanyAndLksgDataViaApi } from "../../utils/LksgApiUtils";

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

    let preparedFixtures: Array<FixtureData<LksgData>>;

    before(function () {
      cy.fixture("CompanyInformationWithLksgPreparedFixtures").then(function (jsonContent) {
        preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
      });
    });

    it("Check the dropdown menu works as expected", () => {
      const fixture = getPreparedLksgFixture("two-different-data-set-types", preparedFixtures);
      uploadCompanyAndLksgDataViaApi(fixture.companyInformation, fixture.t).then((uploadIds) => {
        getKeycloakToken(uploader_name, uploader_pw).then(async (token: string) => {
          uploadOneEuTaxonomyFinancialsDatasetViaApi(token, uploadIds.companyId, generateEuTaxonomyDataForFinancials());
        });
      });
      // TODO finish test after dropdown is there.
    });
  }
);
