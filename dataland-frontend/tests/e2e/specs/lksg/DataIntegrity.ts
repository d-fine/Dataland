import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw, getBaseUrl } from '@e2e/utils/Cypress';
import { getKeycloakToken } from '@e2e/utils/Auth';
import {
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  type LksgData,
  LksgDataControllerApi,
  type StoredCompany,
} from '@clients/backend';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { assignCompanyOwnershipToDatalandAdmin, isDatasetApproved } from '@e2e/utils/CompanyRolesUtils';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import { compareObjectKeysAndValuesDeep } from '@e2e/utils/GeneralUtils';
import LksgBaseFrameworkDefinition from '@/frameworks/lksg/BaseFrameworkDefinition';

describeIf(
  'As a user, I expect to be able to upload LkSG data via an upload form, and that the uploaded data is displayed ' +
    'correctly in the frontend',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function (): void {
    let lksgFixtureWithNoNullFields: FixtureData<LksgData>;
    let lksgFixtureWithMinimalFields: FixtureData<LksgData>;

    before(function () {
      cy.fixture('CompanyInformationWithLksgPreparedFixtures').then(function (jsonContent) {
        const preparedFixturesLksg = jsonContent as Array<FixtureData<LksgData>>;
        lksgFixtureWithNoNullFields = getPreparedFixture('lksg-all-fields', preparedFixturesLksg);
        lksgFixtureWithMinimalFields = getPreparedFixture('lksg-almost-only-nulls', preparedFixturesLksg);
      });
      Cypress.env('excludeBypassQaIntercept', true);
    });

    afterEach(function () {
      Cypress.env('excludeBypassQaIntercept', false);
    });

    it(
      'Create a company and a Lksg dataset via api, then re-upload it with the upload form in Edit mode and ' +
        'assure that the re-uploaded dataset equals the pre-uploaded one',
      () => {
        cy.ensureLoggedIn(admin_name, admin_pw);
        const uniqueCompanyMarker = Date.now().toString();
        const testCompanyName = 'Company-Created-In-Lksg-Blanket-Test' + uniqueCompanyMarker;
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
            return assignCompanyOwnershipToDatalandAdmin(token, storedCompany.companyId).then(() => {
              return uploadFrameworkDataForPublicToolboxFramework(
                LksgBaseFrameworkDefinition,
                token,
                storedCompany.companyId,
                '2021',
                lksgFixtureWithNoNullFields.t
              ).then((dataMetaInformation) => {
                interceptsAndSubmitsDataset(storedCompany, dataMetaInformation, testCompanyName);
                cy.wait('@postCompanyAssociatedData', { timeout: Cypress.env('medium_timeout_in_ms') as number }).then(
                  (postInterception) => {
                    cy.url().should('eq', getBaseUrl() + '/datasets');
                    isDatasetApproved();
                    const dataMetaInformationOfReuploadedDataset = postInterception.response
                      ?.body as DataMetaInformation;
                    return new LksgDataControllerApi(new Configuration({ accessToken: token }))
                      .getCompanyAssociatedLksgData(dataMetaInformationOfReuploadedDataset.dataId)
                      .then((axiosGetResponse) => {
                        let frontendSubmittedLksgDataset = axiosGetResponse.data.data;
                        let originallyUploadedLksgDataset = lksgFixtureWithNoNullFields.t;

                        frontendSubmittedLksgDataset.general?.productionSpecific?.specificProcurement?.sort();
                        frontendSubmittedLksgDataset.governance?.riskManagementOwnOperations?.identifiedRisks?.sort();
                        frontendSubmittedLksgDataset = sortComplaintsRiskObject(frontendSubmittedLksgDataset);
                        frontendSubmittedLksgDataset.governance?.generalViolations?.humanRightsOrEnvironmentalViolationsDefinition?.sort();

                        originallyUploadedLksgDataset.general?.productionSpecific?.specificProcurement?.sort();
                        originallyUploadedLksgDataset.governance?.riskManagementOwnOperations?.identifiedRisks?.sort();
                        originallyUploadedLksgDataset = sortComplaintsRiskObject(originallyUploadedLksgDataset);
                        originallyUploadedLksgDataset.governance?.generalViolations?.humanRightsOrEnvironmentalViolationsDefinition?.sort();

                        compareObjectKeysAndValuesDeep(
                          originallyUploadedLksgDataset as unknown as Record<string, object>,
                          frontendSubmittedLksgDataset as unknown as Record<string, object>,
                          '',
                          ['publicationDate']
                        );
                      });
                  }
                );
              });
            });
          });
        });
      }
    );

    /**
     * Defines interceps and submits data on the lksg upload for the lksg blanket test
     * @param storedCompany stored company information
     * @param dataMetaInformation meta data information
     * @param testCompanyName name of the company
     */
    function interceptsAndSubmitsDataset(
      storedCompany: StoredCompany,
      dataMetaInformation: DataMetaInformation,
      testCompanyName: string
    ): void {
      cy.intercept('**/api/companies/' + storedCompany.companyId + '/info').as('getCompanyInformation');
      cy.visitAndCheckAppMount(
        '/companies/' +
          storedCompany.companyId +
          '/frameworks/' +
          DataTypeEnum.Lksg +
          '/upload?templateDataId=' +
          dataMetaInformation.dataId
      );
      cy.wait('@getCompanyInformation', { timeout: Cypress.env('medium_timeout_in_ms') as number });
      cy.get('h1').should('contain', testCompanyName);
      cy.intercept({
        url: `**/api/data/${DataTypeEnum.Lksg}*`,
        times: 1,
      }).as('postCompanyAssociatedData');
      submitButton.clickButton();
    }

    /**
     * Sorts the complaintsRiskPosition Array in respect to an index inside the Object
     * @param dataset frontend dataset to modify
     * @returns sorted frontend object
     */
    function sortComplaintsRiskObject(dataset: LksgData): LksgData {
      dataset.governance?.grievanceMechanismOwnOperations?.complaintsRiskPosition?.forEach((element) =>
        element.riskPositions.sort()
      );
      dataset.governance?.grievanceMechanismOwnOperations?.complaintsRiskPosition?.sort((a, b) => {
        const comparisonA = a.specifiedComplaint;
        const comparisonB = b.specifiedComplaint;
        return comparisonA.localeCompare(comparisonB);
      });
      return dataset;
    }

    it(
      'Create a company and a Lksg dataset via api with most entries being null and then verify that it can be' +
        'reuploaded.',
      () => {
        cy.ensureLoggedIn(admin_name, admin_pw);
        const uniqueCompanyMarker = Date.now().toString();
        const testCompanyName = 'Company-Created-In-Lksg-Minimal-Blanket-Test' + uniqueCompanyMarker;
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
            return uploadFrameworkDataForPublicToolboxFramework(
              LksgBaseFrameworkDefinition,
              token,
              storedCompany.companyId,
              '2021',
              lksgFixtureWithMinimalFields.t
            ).then((dataMetaInformation) => {
              interceptsAndSubmitsDataset(storedCompany, dataMetaInformation, testCompanyName);
              cy.wait('@postCompanyAssociatedData', { timeout: Cypress.env('medium_timeout_in_ms') as number }).then(
                (postInterception) => {
                  cy.url().should('eq', getBaseUrl() + '/datasets');
                  const dataMetaInformationOfReuploadedDataset = postInterception.response?.body as DataMetaInformation;
                  return new LksgDataControllerApi(new Configuration({ accessToken: token }))
                    .getCompanyAssociatedLksgData(dataMetaInformationOfReuploadedDataset.dataId)
                    .then((axiosGetResponse) => {
                      assert(axiosGetResponse.status == 200);
                    });
                }
              );
            });
          });
        });
      }
    );
  }
);
