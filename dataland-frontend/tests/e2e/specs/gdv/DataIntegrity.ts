import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { DataTypeEnum, type GdvData } from "@clients/backend";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { uploadGenericFrameworkData } from "@e2e/utils/FrameworkUpload";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { getBaseFrameworkDefinition } from "@/frameworks/BaseFrameworkRegistry";

let gdvFixtureForTest: FixtureData<GdvData>;
before(function () {
  cy.fixture("CompanyInformationWithGdvPreparedFixtures").then(function (jsonContent) {
    const preparedFixturesGdv = jsonContent as Array<FixtureData<GdvData>>;
    gdvFixtureForTest = getPreparedFixture("Gdv-dataset-with-no-null-fields", preparedFixturesGdv);
  });
});

describeIf(
  "As a user, I expect to be able to edit and submit GDV data via the upload form",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
    });

    it(
      "Create a company and a GDV dataset via api, then re-upload it with the upload form in Edit mode and " +
        "assure that the re-uploaded dataset equals the pre-uploaded one",
      () => {
        const uniqueCompanyMarker = Date.now().toString();
        const testCompanyName = "Company-Created-In-Gdv-Blanket-Test-" + uniqueCompanyMarker;
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
            return uploadGenericFrameworkData(token, storedCompany.companyId, "2021", gdvFixtureForTest.t, (config) =>
              getBaseFrameworkDefinition(DataTypeEnum.Gdv)!.getFrameworkApiClient(config),
            ).then(() => {
              cy.log("dummy"); // TODO test code needs to be written.  Compare to other blanket tests
            });
          });
        });
      },
    );
  },
);
