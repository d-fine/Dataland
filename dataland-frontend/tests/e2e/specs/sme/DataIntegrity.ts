import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, getBaseUrl } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import {
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  SmeDataControllerApi,
  type SmeData,
  type StoredCompany,
} from "@clients/backend";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { uploadFrameworkData } from "@e2e/utils/FrameworkUpload";
import { compareObjectKeysAndValuesDeep } from "@e2e/utils/GeneralUtils";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";

let smeFixtureForTest: FixtureData<SmeData>;

let tokenForAdminUser: string;
let storedTestCompany: StoredCompany;
let dataMetaInfoOfTestDataset: DataMetaInformation;
before(function () {
  cy.fixture("CompanyInformationWithSmePreparedFixtures").then(function (jsonContent) {
    const preparedFixturesSme = jsonContent as Array<FixtureData<SmeData>>;
    smeFixtureForTest = getPreparedFixture("Sme-dataset-with-no-null-fields", preparedFixturesSme);
  });

  const uniqueCompanyMarker = Date.now().toString();
  const testCompanyName = "Company-Created-In-Sme-Blanket-Test-" + uniqueCompanyMarker;
  getKeycloakToken(admin_name, admin_pw)
    .then((token: string) => {
      tokenForAdminUser = token;
      return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName));
    })
    .then((storedCompany) => {
      storedTestCompany = storedCompany;
      return uploadFrameworkData(
        DataTypeEnum.Sme,
        tokenForAdminUser,
        storedCompany.companyId,
        "2021",
        smeFixtureForTest.t,
      );
    })
    .then((dataMetaInfo) => {
      dataMetaInfoOfTestDataset = dataMetaInfo;
    });
});

describeIf(
  "As a user, I expect to be able to edit and submit Sme data via the upload form",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function (): void {
    it(
      "Create a company and a Sme dataset via api, then re-upload it with the upload form in Edit mode and " +
        "assure that the re-uploaded dataset equals the pre-uploaded one",
      () => {
        cy.ensureLoggedIn(admin_name, admin_pw);
        cy.intercept("**/api/companies/" + storedTestCompany.companyId + "/info").as("getCompanyInformation");
        cy.visitAndCheckAppMount(
          "/companies/" +
            storedTestCompany.companyId +
            "/frameworks/" +
            DataTypeEnum.Sme +
            "/upload?templateDataId=" +
            dataMetaInfoOfTestDataset.dataId,
        );
        cy.wait("@getCompanyInformation", { timeout: Cypress.env("medium_timeout_in_ms") as number });
        cy.get("h1").should("contain", storedTestCompany.companyInformation.companyName);
        cy.intercept({
          url: `**/api/data/${DataTypeEnum.Sme}`,
          times: 1,
        }).as("postCompanyAssociatedData");
        submitButton.clickButton();
        cy.wait("@postCompanyAssociatedData", { timeout: Cypress.env("medium_timeout_in_ms") as number })
          .then((postResponseInterception) => {
            cy.url().should("eq", getBaseUrl() + "/datasets");
            const dataMetaInformationOfReuploadedDataset = postResponseInterception.response
              ?.body as DataMetaInformation;
            return new SmeDataControllerApi(
              new Configuration({ accessToken: tokenForAdminUser }),
            ).getCompanyAssociatedSmeData(dataMetaInformationOfReuploadedDataset.dataId);
          })
          .then((axiosGetResponse) => {
            const frontendSubmittedSmeDataset = axiosGetResponse.data.data;
            frontendSubmittedSmeDataset.insurances?.naturalHazards?.naturalHazardsCovered?.sort();
            compareObjectKeysAndValuesDeep(
              smeFixtureForTest.t as unknown as Record<string, object>,
              frontendSubmittedSmeDataset as unknown as Record<string, object>,
            );
          });
      },
    );
  },
);
