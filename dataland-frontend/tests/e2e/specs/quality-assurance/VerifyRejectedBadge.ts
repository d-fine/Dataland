import { describeIf } from '@e2e/support/TestUtility';
import { type FixtureData } from '@sharedUtils/Fixtures';
import { type LksgData } from '@clients/backend';
import { getAdminToken, ensureLoggedInAsAdmin } from '@e2e/utils/Auth';
import { uploadCompanyAndFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import { generateCompanyInformation } from '@e2e/fixtures/CompanyFixtures';
import LksgBaseFrameworkDefinition from '@/frameworks/lksg/BaseFrameworkDefinition';

describeIf(
  "Validation for correct display of 'Rejected' badge",
  {
    executionEnvironments: ['developmentLocal', 'ci'],
  },
  () => {
    beforeEach(() => {
      ensureLoggedInAsAdmin();
    });

    let lksgFixture: FixtureData<LksgData>;

    before(function () {
      cy.fixture('CompanyInformationWithLksgData').then(function (jsonContent: Array<FixtureData<LksgData>>) {
        lksgFixture = jsonContent[0]!;
      });
    });

    it('Verifies that the badge is shown as expected when an uploaded Lksg dataset gets rejected', () => {
      cy.intercept('/api/data/lksg*', { middleware: true }, (req) => {
        req.headers['REQUIRE-QA'] = 'true';
      });
      getAdminToken().then((token: string) => {
        return uploadCompanyAndFrameworkDataForPublicToolboxFramework(
          LksgBaseFrameworkDefinition,
          token,
          generateCompanyInformation(),
          lksgFixture.t,
          lksgFixture.reportingPeriod
        ).then((uploadIds) => {
          cy.intercept(`**/api/data/lksg/${uploadIds.dataId}`).as('getPostedDataset');
          cy.visit(`/companies/${uploadIds.companyId}/frameworks/lksg/${uploadIds.dataId}`);
          cy.wait('@getPostedDataset');
          cy.get('[data-test="qaRejectButton"]').click();
          cy.intercept('**/api/users/**').as('getMyDatasets');
          cy.visit(`/datasets`);
          cy.wait('@getMyDatasets');
          cy.get(`[data-test="view-dataset-button"]`)
            .parents('tr[role=row]')
            .find('[data-test="qa-status"]')
            .should('exist')
            .should('contain', 'Rejected');
        });
      });
    });
  }
);
