import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw, getBaseUrl } from '@e2e/utils/Cypress';
import { getKeycloakToken } from '@e2e/utils/Auth';
import {
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  type EsgQuestionnaireData,
  EsgQuestionnaireDataControllerApi,
} from '@clients/backend';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { uploadGenericFrameworkData } from '@e2e/utils/FrameworkUpload';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { assignCompanyOwnershipToDatalandAdmin, isDatasetApproved } from '@e2e/utils/CompanyRolesUtils';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import { compareObjectKeysAndValuesDeep } from '@e2e/utils/GeneralUtils';

let esgQuestionnaireFixtureForTest: FixtureData<EsgQuestionnaireData>;
before(function () {
  cy.fixture('CompanyInformationWithEsgQuestionnairePreparedFixtures').then(function (jsonContent) {
    const preparedFixturesEsgQuestionnaire = jsonContent as Array<FixtureData<EsgQuestionnaireData>>;
    esgQuestionnaireFixtureForTest = getPreparedFixture(
      'EsgQuestionnaire-dataset-with-no-null-fields',
      preparedFixturesEsgQuestionnaire
    );
  });
});

describeIf(
  'As a user, I expect to be able to edit and submit ESG Questionnaire data via the upload form',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      Cypress.env('excludeBypassQaIntercept', true);
    });

    it(
      'Create a company and a ESG Questionnaire dataset via api, then re-upload it with the upload form in ' +
        'Edit mode and assure that the re-uploaded dataset equals the pre-uploaded one',
      () => {
        const uniqueCompanyMarkerWithDate = Date.now().toString();
        const testCompanyNameEsgQuestionnaire =
          'Company-Created-In-EsgQuestionnaire-Blanket-Test-' + uniqueCompanyMarkerWithDate;
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyNameEsgQuestionnaire)).then(
            (storedCompany) => {
              return assignCompanyOwnershipToDatalandAdmin(token, storedCompany.companyId).then(() => {
                return uploadGenericFrameworkData(
                  token,
                  storedCompany.companyId,
                  '2021',
                  esgQuestionnaireFixtureForTest.t,
                  (config) =>
                    getBasePublicFrameworkDefinition(DataTypeEnum.EsgQuestionnaire)!.getPublicFrameworkApiClient(config)
                ).then((dataMetaInformation) => {
                  cy.intercept(`**/api/data/${DataTypeEnum.EsgQuestionnaire}/${dataMetaInformation.dataId}`).as(
                    'fetchDataForPrefill'
                  );
                  cy.visitAndCheckAppMount(
                    '/companies/' +
                      storedCompany.companyId +
                      '/frameworks/' +
                      DataTypeEnum.EsgQuestionnaire +
                      '/upload?templateDataId=' +
                      dataMetaInformation.dataId
                  );
                  cy.wait('@fetchDataForPrefill', { timeout: Cypress.env('medium_timeout_in_ms') as number });
                  cy.get('h1').should('contain', testCompanyNameEsgQuestionnaire);
                  cy.intercept({
                    url: `**/api/data/${DataTypeEnum.EsgQuestionnaire}?bypassQa=true`,
                    times: 1,
                  }).as('postCompanyAssociatedData');
                  submitButton.clickButton();
                  cy.wait('@postCompanyAssociatedData', {
                    timeout: Cypress.env('medium_timeout_in_ms') as number,
                  }).then((postInterception) => {
                    cy.url().should('eq', getBaseUrl() + '/datasets');
                    isDatasetApproved();
                    const dataMetaInformationOfReuploadedDataset = postInterception.response
                      ?.body as DataMetaInformation;
                    return new EsgQuestionnaireDataControllerApi(new Configuration({ accessToken: token }))
                      .getCompanyAssociatedEsgQuestionnaireData(dataMetaInformationOfReuploadedDataset.dataId)
                      .then((axiosResponse) => {
                        const frontendSubmittedEsgQuestionnaireDataset = axiosResponse.data.data;

                        // esgQuestionnaireFixtureForTest.t.allgemein?.sektoren?.auflistungDerSektoren?.sort(); TODO deactivate for now
                        esgQuestionnaireFixtureForTest.t.umwelt?.taxonomie?.euTaxonomieKompassAktivitaeten?.sort();
                        esgQuestionnaireFixtureForTest.t.unternehmensfuehrungGovernance?.unternehmensrichtlinien?.veroeffentlichteUnternehmensrichtlinien?.sort();

                        // frontendSubmittedEsgQuestionnaireDataset.allgemein?.sektoren?.auflistungDerSektoren?.sort(); TODO deactivate for now
                        frontendSubmittedEsgQuestionnaireDataset.umwelt?.taxonomie?.euTaxonomieKompassAktivitaeten?.sort();
                        frontendSubmittedEsgQuestionnaireDataset.unternehmensfuehrungGovernance?.unternehmensrichtlinien?.veroeffentlichteUnternehmensrichtlinien?.sort();

                        compareObjectKeysAndValuesDeep(
                          esgQuestionnaireFixtureForTest.t as unknown as Record<string, object>,
                          frontendSubmittedEsgQuestionnaireDataset as unknown as Record<string, object>
                        );
                      });
                  });
                });
              });
            }
          );
        });
      }
    );
  }
);
