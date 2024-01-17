import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, getBaseUrl } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import {
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  type EsgQuestionnaireData,
  EsgQuestionnaireDataControllerApi,
} from "@clients/backend";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { uploadGenericFrameworkData } from "@e2e/utils/FrameworkUpload";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { getBaseFrameworkDefinition } from "@/frameworks/BaseFrameworkRegistry";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { compareObjectKeysAndValuesDeep } from "@e2e/utils/GeneralUtils";

let esgquestionnaireFixtureForTest: FixtureData<EsgQuestionnaireData>;
before(function () {
  cy.fixture("CompanyInformationWithEsgQuestionnairePreparedFixtures").then(function (jsonContent) {
    const preparedFixturesEsgQuestionnaire = jsonContent as Array<FixtureData<EsgQuestionnaireData>>;
    esgquestionnaireFixtureForTest = getPreparedFixture(
      "EsgQuestionnaire-dataset-with-no-null-fields",
      preparedFixturesEsgQuestionnaire,
    );
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
        const uniqueCompanyMarkerWithDate = Date.now().toString();
        const testCompanyNameEsgQuestionnaire =
          "Company-Created-In-EsgQuestionnaire-Blanket-Test-" + uniqueCompanyMarkerWithDate;
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyNameEsgQuestionnaire)).then(
            (storedCompany) => {
              return uploadGenericFrameworkData(
                token,
                storedCompany.companyId,
                "2021",
                esgquestionnaireFixtureForTest.t,
                (config) => getBaseFrameworkDefinition(DataTypeEnum.EsgQuestionnaire)!.getFrameworkApiClient(config),
              ).then((dataMetaInformation) => {
                cy.intercept(`**/api/data/${DataTypeEnum.EsgQuestionnaire}/${dataMetaInformation.dataId}`).as(
                  "fetchDataForPrefill",
                );
                cy.visitAndCheckAppMount(
                  "/companies/" +
                    storedCompany.companyId +
                    "/frameworks/" +
                    DataTypeEnum.EsgQuestionnaire +
                    "/upload?templateDataId=" +
                    dataMetaInformation.dataId,
                );
                cy.wait("@fetchDataForPrefill", { timeout: Cypress.env("medium_timeout_in_ms") as number });
                cy.get("h1").should("contain", testCompanyNameEsgQuestionnaire);
                cy.intercept({
                  url: `**/api/data/${DataTypeEnum.EsgQuestionnaire}`,
                  times: 1,
                }).as("postCompanyAssociatedData");
                submitButton.clickButton();
                cy.wait("@postCompanyAssociatedData", { timeout: Cypress.env("medium_timeout_in_ms") as number }).then(
                  (postInterception) => {
                    cy.url().should("eq", getBaseUrl() + "/datasets");
                    const dataMetaInformationOfReuploadedDataset = postInterception.response
                      ?.body as DataMetaInformation;
                    return new EsgQuestionnaireDataControllerApi(new Configuration({ accessToken: token }))
                      .getCompanyAssociatedEsgQuestionnaireData(dataMetaInformationOfReuploadedDataset.dataId)
                      .then((axiosResponse) => {
                        const frontendSubmittedEsgQuestionnaireDataset = axiosResponse.data.data;

                        esgquestionnaireFixtureForTest.t.allgemein?.sektoren?.auflistungDerSektoren?.sort();
                        esgquestionnaireFixtureForTest.t.umwelt?.taxonomie?.euTaxonomieKompassAktivitaeten?.sort();
                        esgquestionnaireFixtureForTest.t.unternehmensfuehrungGovernance?.unternehmensrichtlinien?.veroeffentlichteUnternehmensrichtlinien?.sort();

                        frontendSubmittedEsgQuestionnaireDataset.allgemein?.sektoren?.auflistungDerSektoren?.sort();
                        frontendSubmittedEsgQuestionnaireDataset.umwelt?.taxonomie?.euTaxonomieKompassAktivitaeten?.sort();
                        frontendSubmittedEsgQuestionnaireDataset.unternehmensfuehrungGovernance?.unternehmensrichtlinien?.veroeffentlichteUnternehmensrichtlinien?.sort();

                        compareObjectKeysAndValuesDeep(
                          esgquestionnaireFixtureForTest.t as Record<string, object>,
                          frontendSubmittedEsgQuestionnaireDataset as Record<string, object>,
                        );
                      });
                  },
                );
              });
            },
          );
        });
      },
    );
  },
);
