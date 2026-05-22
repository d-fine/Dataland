import { type BasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkDefinition';
import { type CompanyRoleAssignment } from '@clients/communitymanager';
import { type DataMetaInformation, type DataTypeEnum, type StoredCompany } from '@clients/backend';
import { describeIf } from '@e2e/support/TestUtility';
import { getAdminToken } from '@e2e/utils/Auth';
import { assignCompanyOwnershipToDatalandAdmin } from '@e2e/utils/CompanyRolesUtils';
import { generateDummyCompanyInformation, getOrUploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import { compareObjectKeysAndValuesDeep } from '@e2e/utils/GeneralUtils';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';

const mediumTimeoutInMs = Number(Cypress.expose('medium_timeout_in_ms') ?? 30000);
const longTimeoutInMs = Number(Cypress.expose('long_timeout_in_ms') ?? 100000);

type SetupResult = {
  token: string;
  storedCompany: StoredCompany;
  dataId: string;
};

export abstract class BaseDataIntegrityTest<TFrameworkData extends object> {
  protected fixtureData!: FixtureData<TFrameworkData>;

  /**
   * Registers fixture loading and test execution for the concrete framework.
   */
  registerDataIntegrityTest(): void {
    this.loadFixtureData();

    describeIf(
      this.getDescribeText(),
      {
        executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
      },
      (): void => {
        before((): void => {
          Cypress.expose('excludeBypassQaIntercept', true);
        });

        it(this.getTestTitle(), (): void => {
          const uniqueCompanyMarker = Date.now().toString();
          const testCompanyName = this.getTestCompanyPrefix() + uniqueCompanyMarker;

          cy.wrap(null, { timeout: longTimeoutInMs })
            .then(() => this.setupCompanyAndFramework(testCompanyName))
            .then(({ token, storedCompany, dataId }) => {
              cy.ensureLoggedInAsAdmin();
              cy.intercept({
                url: `**/api/data/${this.getDataTypeEnum()}/**`,
                times: 1,
              }).as('getUploadedData');
              cy.visitAndCheckAppMount(`/companies/${storedCompany.companyId}/frameworks/${this.getDataTypeEnum()}`);
              cy.wait('@getUploadedData', {
                timeout: mediumTimeoutInMs,
              });
              cy.get('h1').should('contain', testCompanyName);
              cy.wrap(null).then(() => this.validateUploadedDataset(token, dataId, this.fixtureData.t));
            });
        });
      }
    );
  }

  /**
   * @returns the framework definition used to upload data in this test.
   */
  protected abstract getFrameworkDefinition(): BasePublicFrameworkDefinition<TFrameworkData>;

  /**
   * @returns fixture file name used for preloaded datasets.
   */
  protected abstract getFixtureFileName(): string;

  /**
   * @returns fixture key used to select the concrete prepared fixture.
   */
  protected abstract getFixtureName(): string;

  /**
   * @returns full describe text displayed by Cypress.
   */
  protected abstract getDescribeText(): string;

  /**
   * @returns full test case title displayed by Cypress.
   */
  protected abstract getTestTitle(): string;

  /**
   * @returns test company name prefix used to generate a unique company.
   */
  protected abstract getTestCompanyPrefix(): string;

  /**
   * @returns data type enum used for intercept and URL routing.
   */
  protected abstract getDataTypeEnum(): DataTypeEnum;

  /**
   * @param token access token for API calls.
   * @param dataId id of the uploaded dataset.
   * @param initiallyUploadedData expected dataset payload.
   */
  protected abstract validateUploadedDataset(
    token: string,
    dataId: string,
    initiallyUploadedData: TFrameworkData
  ): Promise<void>;

  /**
   * Compares uploaded and retrieved data while allowing backend-managed publication date differences.
   */
  protected compareUploadedData(initiallyUploadedData: TFrameworkData, datasetFromBackend: TFrameworkData): void {
    compareObjectKeysAndValuesDeep(
      initiallyUploadedData as Record<string, object>,
      datasetFromBackend as Record<string, object>,
      undefined,
      ['publicationDate']
    );
  }

  /**
   * Loads and prepares fixture data before running the test.
   */
  private loadFixtureData(): void {
    before((): void => {
      cy.fixture(this.getFixtureFileName()).then((jsonContent): void => {
        const preparedFixtures = jsonContent as Array<FixtureData<TFrameworkData>>;
        this.fixtureData = getPreparedFixture(this.getFixtureName(), preparedFixtures);
      });
    });
  }

  /**
   * @returns keycloak token for the admin user.
   */
  private getToken(): Cypress.Chainable<string> {
    return getAdminToken();
  }

  /**
   * @returns created company from backend.
   */
  private createCompany(token: string, testCompanyName: string): Promise<StoredCompany> {
    return getOrUploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName));
  }

  /**
   * @returns assignment confirmation after setting company owner role.
   */
  private assignOwnership(token: string, companyId: string): Promise<CompanyRoleAssignment> {
    return assignCompanyOwnershipToDatalandAdmin(token, companyId);
  }

  /**
   * @returns metadata of the uploaded framework dataset.
   */
  private uploadFrameworkData(token: string, companyId: string): Promise<DataMetaInformation> {
    return uploadFrameworkDataForPublicToolboxFramework(
      this.getFrameworkDefinition(),
      token,
      companyId,
      this.fixtureData.reportingPeriod,
      this.fixtureData.t
    );
  }

  /**
   * @returns token, company and uploaded dataset id.
   */
  private setupCompanyAndFramework(testCompanyName: string): Cypress.Chainable<SetupResult> {
    let token: string;
    let storedCompany: StoredCompany;
    return this.getToken()
      .then((receivedToken) => {
        token = receivedToken;
        return this.createCompany(token, testCompanyName);
      })
      .then((company) => {
        storedCompany = company;
        return this.assignOwnership(token, storedCompany.companyId);
      })
      .then(() => this.uploadFrameworkData(token, storedCompany.companyId))
      .then(({ dataId }) => ({ token, storedCompany, dataId }));
  }
}
