import { type EutaxonomyFinancialsData, type SfdrData, type StoredCompany } from '@clients/backend';
import { describeIf } from '@e2e/support/TestUtility.ts';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { admin_name, admin_pw } from '@e2e/utils/Cypress';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import EuTaxonomyFinancialsBaseFrameworkDefinition from '@/frameworks/eutaxonomy-financials/BaseFrameworkDefinition';
import SfdrBaseFrameworkDefinition from '@/frameworks/sfdr/BaseFrameworkDefinition';

describeIf(
  'As a user, I expect to be able to log in',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function () {
    let storedCompany: StoredCompany;
    let preparedEuTaxonomyFixtures: Array<FixtureData<EutaxonomyFinancialsData>>;
    let preparedSfdrFixtures: Array<FixtureData<SfdrData>>;

    before(function () {
      cy.fixture('CompanyInformationWithEutaxonomyFinancialsPreparedFixtures').then(function (jsonContent) {
        preparedEuTaxonomyFixtures = jsonContent as Array<FixtureData<EutaxonomyFinancialsData>>;
      });

      cy.fixture('CompanyInformationWithSfdrPreparedFixtures').then(function (jsonContent) {
        preparedSfdrFixtures = jsonContent as Array<FixtureData<SfdrData>>;
      });

      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        const testCompany = generateDummyCompanyInformation(`company-for-testing-judgement-${Date.now()}`);
        return uploadCompanyViaApi(token, testCompany).then((newCompany) => (storedCompany = newCompany));
      });
    });

    it('Check whether newly added dataset is displayed at the QA Overview', () => {
      const euTaxonomyData = getPreparedFixture('lightweight-eu-taxo-financials-dataset', preparedEuTaxonomyFixtures);

      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadFrameworkDataForPublicToolboxFramework(
          EuTaxonomyFinancialsBaseFrameworkDefinition,
          token,
          storedCompany.companyId,
          '2022',
          euTaxonomyData.t,
          false
        );
      });
    });

    it('Start and finish a Judgement', () => {
      const sfdrData = getPreparedFixture('Sfdr-dataset-with-no-null-fields', preparedSfdrFixtures);

      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadFrameworkDataForPublicToolboxFramework(
          SfdrBaseFrameworkDefinition,
          token,
          storedCompany.companyId,
          '2021',
          sfdrData.t,
          false
        );
      });
    });
  }
);
