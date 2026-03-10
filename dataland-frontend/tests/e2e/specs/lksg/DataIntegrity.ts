import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw, getBaseUrl } from '@e2e/utils/Cypress';
import { getKeycloakToken } from '@e2e/utils/Auth';
import {
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  type LksgData,
  LksgDataControllerApi,
  type RiskPositionType,
  type StoredCompany,
} from '@clients/backend';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { assignCompanyOwnershipToDatalandAdmin, isDatasetAccepted } from '@e2e/utils/CompanyRolesUtils';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import { compareObjectKeysAndValuesDeep } from '@e2e/utils/GeneralUtils';
import LksgBaseFrameworkDefinition from '@/frameworks/lksg/BaseFrameworkDefinition';

/**
 * Defines intercepts and submits data on the lksg upload for the lksg blanket test
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
 * Sorts the riskPositions Array by converting the
 * elements into strings and explicitly defining a compare function
 * @param riskPositions an array of risk positions
 * @returns sorted riskPositions
 */
function sortRiskPositions(riskPositions: RiskPositionType[]): RiskPositionType[] {
  return riskPositions.sort((a: RiskPositionType, b: RiskPositionType) => String(a).localeCompare(String(b)));
}

/**
 * Fetches only the response status code for a LKSG dataset fetch call.
 * @param token keycloak access token
 * @param dataId id of the dataset to fetch
 * @returns HTTP status code
 */
async function fetchReuploadedDatasetStatus(token: string, dataId: string): Promise<number> {
  const axiosGetResponse = await new LksgDataControllerApi(
    new Configuration({ accessToken: token })
  ).getCompanyAssociatedLksgData(dataId);
  return axiosGetResponse.status;
}

describeIf(
  'As a user, I expect to be able to upload LkSG data via an upload form, and that the uploaded data is displayed ' +
    'correctly in the frontend',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function (): void {
    let lksgFixtureWithNoNullFields: FixtureData<LksgData>;
    let lksgFixtureWithMinimalFields: FixtureData<LksgData>;

    type UploadedLksgContext = {
      token: string;
      storedCompany: StoredCompany;
      dataMetaInformation: DataMetaInformation;
    };

    before(function () {
      cy.fixture('CompanyInformationWithLksgPreparedFixtures').then(function (jsonContent) {
        const preparedFixturesLksg = jsonContent as Array<FixtureData<LksgData>>;
        lksgFixtureWithNoNullFields = getPreparedFixture('lksg-all-fields', preparedFixturesLksg);
        lksgFixtureWithMinimalFields = getPreparedFixture('lksg-almost-only-nulls', preparedFixturesLksg);
      });
      Cypress.env('excludeBypassQaIntercept', true);
    });

    /**
     * Creates a company, optionally assigns ownership and uploads the initial LKSG dataset.
     * @param token keycloak access token
     * @param testCompanyName name used for the generated dummy company
     * @param fixture prepared fixture used for upload data
     * @param assignOwnership whether ownership should be assigned to dataland admin
     * @returns token, stored company and uploaded dataset metadata
     */
    async function createCompanyAndUploadDataset(
      token: string,
      testCompanyName: string,
      fixture: FixtureData<LksgData>,
      assignOwnership: boolean
    ): Promise<UploadedLksgContext> {
      const storedCompany = await uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName));
      if (assignOwnership) {
        await assignCompanyOwnershipToDatalandAdmin(token, storedCompany.companyId);
      }
      const dataMetaInformation = await uploadFrameworkDataForPublicToolboxFramework(
        LksgBaseFrameworkDefinition,
        token,
        storedCompany.companyId,
        '2021',
        fixture.t
      );
      return {
        token,
        storedCompany,
        dataMetaInformation,
      };
    }

    /**
     * Submits the LKSG edit form and returns metadata of the reuploaded dataset.
     * @param storedCompany stored company information
     * @param dataMetaInformation metadata of the dataset used for edit prefill
     * @param testCompanyName expected company name displayed in the header
     * @returns metadata of the reuploaded dataset
     */
    function submitInEditModeAndGetMeta(
      storedCompany: StoredCompany,
      dataMetaInformation: DataMetaInformation,
      testCompanyName: string
    ): Cypress.Chainable<DataMetaInformation> {
      interceptsAndSubmitsDataset(storedCompany, dataMetaInformation, testCompanyName);
      return cy
        .wait('@postCompanyAssociatedData', { timeout: Cypress.env('medium_timeout_in_ms') as number })
        .then((postInterception) => {
          cy.url().should('eq', getBaseUrl() + '/datasets');
          isDatasetAccepted();
          return postInterception.response?.body as DataMetaInformation;
        });
    }

    /**
     * Fetches a reuploaded LKSG dataset from backend.
     * @param token keycloak access token
     * @param dataId id of the dataset to fetch
     * @returns LKSG dataset payload
     */
    async function fetchReuploadedDataset(token: string, dataId: string): Promise<LksgData> {
      const axiosGetResponse = await new LksgDataControllerApi(
        new Configuration({ accessToken: token })
      ).getCompanyAssociatedLksgData(dataId);
      return axiosGetResponse.data.data;
    }

    /**
     * Submits the LKSG edit form and fetches the reuploaded dataset from backend.
     * @param token keycloak access token
     * @param storedCompany stored company information
     * @param dataMetaInformation metadata of the dataset used for edit prefill
     * @param testCompanyName expected company name displayed in the header
     * @returns LKSG dataset payload
     */
    function submitInEditModeAndFetchReuploadedDataset(
      token: string,
      storedCompany: StoredCompany,
      dataMetaInformation: DataMetaInformation,
      testCompanyName: string
    ): Cypress.Chainable<LksgData> {
      return submitInEditModeAndGetMeta(storedCompany, dataMetaInformation, testCompanyName).then(
        (dataMetaInformationOfReuploadedDataset) => {
          return fetchReuploadedDataset(token, dataMetaInformationOfReuploadedDataset.dataId);
        }
      );
    }

    /**
     * Normalizes order-dependent LKSG fields before deep comparison.
     * @param dataset dataset to normalize
     * @returns normalized dataset
     */
    function normalizeLksgDataset(dataset: LksgData): LksgData {
      dataset.general?.productionSpecific?.specificProcurement?.sort();
      dataset.governance?.riskManagementOwnOperations?.identifiedRisks?.sort();
      dataset.governance?.generalViolations?.humanRightsOrEnvironmentalViolationsDefinition?.sort();
      return sortComplaintsRiskObject(dataset);
    }

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
        getKeycloakToken(admin_name, admin_pw)
          .then((token: string) => {
            return createCompanyAndUploadDataset(token, testCompanyName, lksgFixtureWithNoNullFields, true);
          })
          .then(({ token, storedCompany, dataMetaInformation }) => {
            return submitInEditModeAndFetchReuploadedDataset(
              token,
              storedCompany,
              dataMetaInformation,
              testCompanyName
            );
          })
          .then((frontendSubmittedLksgDataset) => {
            const normalizedFrontendSubmittedLksgDataset = normalizeLksgDataset(frontendSubmittedLksgDataset);
            const normalizedOriginallyUploadedLksgDataset = normalizeLksgDataset(lksgFixtureWithNoNullFields.t);
            compareObjectKeysAndValuesDeep(
              normalizedOriginallyUploadedLksgDataset as unknown as Record<string, object>,
              normalizedFrontendSubmittedLksgDataset as unknown as Record<string, object>,
              '',
              ['publicationDate']
            );
          });
      }
    );

    /**
     * Sorts the complaintsRiskPosition Array in respect to an index inside the Object
     * @param dataset frontend dataset to modify
     * @returns sorted frontend object
     */
    function sortComplaintsRiskObject(dataset: LksgData): LksgData {
      const complaintsRiskPosition = dataset.governance?.grievanceMechanismOwnOperations?.complaintsRiskPosition;
      if (complaintsRiskPosition) {
        for (const element of complaintsRiskPosition) {
          element.riskPositions = sortRiskPositions(element.riskPositions);
        }
        complaintsRiskPosition.sort((a, b) => {
          const comparisonA = a.specifiedComplaint;
          const comparisonB = b.specifiedComplaint;
          return comparisonA.localeCompare(comparisonB);
        });
      }
      return dataset;
    }

    it(
      'Create a company and a Lksg dataset via api with most entries being null and then verify that it can be ' +
        'reuploaded.',
      () => {
        cy.ensureLoggedIn(admin_name, admin_pw);
        const uniqueCompanyMarker = Date.now().toString();
        const testCompanyName = 'Company-Created-In-Lksg-Minimal-Blanket-Test' + uniqueCompanyMarker;
        getKeycloakToken(admin_name, admin_pw)
          .then((token: string) => {
            return createCompanyAndUploadDataset(token, testCompanyName, lksgFixtureWithMinimalFields, false);
          })
          .then(({ token, storedCompany, dataMetaInformation }) => {
            return submitInEditModeAndGetMeta(storedCompany, dataMetaInformation, testCompanyName).then(
              (dataMetaInformationOfReuploadedDataset) => {
                return fetchReuploadedDatasetStatus(token, dataMetaInformationOfReuploadedDataset.dataId);
              }
            );
          })
          .then((statusCode) => {
            assert(statusCode == 200);
          });
      }
    );
  }
);
