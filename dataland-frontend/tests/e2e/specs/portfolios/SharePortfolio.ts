import { admin_name, admin_pw, reader_name, reader_pw, reader_userId } from '@e2e/utils/Cypress';
import { describeIf } from '@e2e/support/TestUtility';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { Configuration, NotificationFrequency, PortfolioControllerApi } from '@clients/userservice';

describeIf(
  'As a user I want to share portfolios with other users who can view and remove them',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    let portfolioId: string;
    let portfolioName: string;

    before(() => {
      const timestamp = Date.now();
      portfolioName = `E2E-Share-${timestamp}`;

      getKeycloakToken(admin_name, admin_pw).then(async (token) => {
        const companyToUpload = generateDummyCompanyInformation(
          `Company-Created-For-Share-Portfolio-Test-${timestamp}`
        );
        const uploadResult = await uploadCompanyViaApi(token, companyToUpload);
        const companyId = uploadResult.companyId;

        const portfolioApi = new PortfolioControllerApi(new Configuration({ accessToken: token }));
        const response = await portfolioApi.createPortfolio({
          portfolioName: portfolioName,
          identifiers: [companyId] as unknown as Set<string>,
          isMonitored: false,
          monitoredFrameworks: [] as unknown as Set<string>,
          notificationFrequency: NotificationFrequency.Weekly,
          timeWindowThreshold: undefined,
          sharedUserIds: [] as unknown as Set<string>,
        });
        portfolioId = response.data.portfolioId;
        cy.wrap(portfolioId).should('not.be.empty');
      });
    });

    beforeEach(() => {
      cy.intercept('GET', '**/users/portfolios/names').as('getPortfolioNames');
      cy.intercept('GET', '**/users/portfolios/**/enriched-portfolio').as('getEnrichedPortfolio');
      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.visitAndCheckAppMount('/portfolios');
      cy.wait(['@getPortfolioNames']);
    });

    it('Share portfolio with user, verify receiver can see it, and receiver removes it from view', () => {
      cy.intercept('PATCH', '**/users/portfolios/**/sharing').as('patchSharing');
      cy.intercept('POST', '**/community/emails/validation').as('emailValidation');
      cy.intercept('GET', '**/portfolios/**/access-rights').as('getAccessRights');

      cy.get(`[data-test="${portfolioName}"]`, { timeout: Cypress.env('medium_timeout_in_ms') as number }).should(
        'exist'
      );
      cy.get(`[data-test="${portfolioName}"]`).click();
      cy.wait(['@getEnrichedPortfolio']);

      cy.get(`[data-test="portfolio-${portfolioName}"] [data-test="share-portfolio"]`).click({
        timeout: Cypress.env('medium_timeout_in_ms') as number,
      });

      cy.get('.p-dialog').find('.p-dialog-header').contains('Manage Portfolio Access');
      cy.wait('@getAccessRights');

      cy.get('[data-test="email-input-field"]').should('be.visible');
      cy.get('[data-test="email-input-field"]').clear();
      cy.get('[data-test="email-input-field"]').type('data.reader@example.com');
      cy.get('[data-test="select-user-button"]').click();
      cy.wait('@emailValidation');

      cy.get('[data-test="users-with-access-listbox"]', {
        timeout: Cypress.env('medium_timeout_in_ms') as number,
      }).should('contain', 'data.reader@example.com');

      cy.get('[data-test="save-changes-button"]').click({
        timeout: Cypress.env('medium_timeout_in_ms') as number,
      });

      cy.wait('@patchSharing').then((interception) => {
        const body = interception.request?.body;
        expect(body.sharedUserIds).to.be.an('array').with.length(1);
        expect(body.sharedUserIds[0]).to.equal(reader_userId);
      });

      cy.get('.p-dialog-mask').should('not.exist');
      cy.get(`[data-test="portfolio-${portfolioName}"] [data-test="shared-users-tag"]`)
        .should('be.visible')
        .and('contain', 'Shared with 1 user');

      cy.ensureLoggedIn(reader_name, reader_pw);
      cy.intercept('GET', '**/users/portfolios/shared/names').as('getSharedPortfolios');
      cy.visitAndCheckAppMount('/shared-portfolios');
      cy.wait('@getSharedPortfolios');

      cy.get(`[data-test="${portfolioName}"]`, { timeout: Cypress.env('medium_timeout_in_ms') as number }).should(
        'exist'
      );
      cy.get(`[data-test="${portfolioName}"]`).click();

      cy.get(`[data-test="portfolio-${portfolioName}"] [data-test="shared-by-tag"]`)
        .should('be.visible')
        .and('contain', 'Shared by');

      cy.intercept('DELETE', '**/users/portfolios/shared/**').as('deleteSharing');
      cy.get(`[data-test="portfolio-${portfolioName}"] [data-test="remove-portfolio"]`).click({
        timeout: Cypress.env('medium_timeout_in_ms') as number,
      });

      cy.get('[data-test="remove-sharing-modal"]').should('be.visible');
      cy.get('[data-test="remove-confirmation-button"]').click();
      cy.wait('@deleteSharing').its('response.statusCode').should('eq', 204);
      cy.get(`[data-test="${portfolioName}"]`).should('not.exist');
    });

    after(() => {
      getKeycloakToken(admin_name, admin_pw).then(async (token) => {
        const portfolioApi = new PortfolioControllerApi(new Configuration({ accessToken: token }));
        await portfolioApi.deletePortfolio(portfolioId);
      });
    });
  }
);
