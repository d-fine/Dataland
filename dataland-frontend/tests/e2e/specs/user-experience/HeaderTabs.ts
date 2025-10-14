import { ensureLoggedIn } from '@e2e/utils/Auth.ts';
import { describeIf } from '@e2e/support/TestUtility';
import { reader_name, reader_pw, uploader_name, uploader_pw } from '@e2e/utils/Cypress.ts';
import { DataTypeEnum } from '@clients/backend';

describeIf(
  'Test header tabs visibility for user role',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    const tabNames = ['COMPANIES', 'MY DATASETS', 'MY PORTFOLIOS', 'MY DATA REQUESTS'];
    const dummyCompany = {
      companyId: 'dummy-company-id',
      companyName: 'Dummy Company',
    };

    const assertTabVisibility = (tabName: string, isVisible: boolean): void => {
      cy.contains(tabName).should(isVisible ? 'be.visible' : 'not.exist');
    };

    const assertHeaderTabsVisibility = (isVisible: boolean): void => {
      for (const tab of tabNames) {
        assertTabVisibility(tab, isVisible);
      }
    };

    const visitPagesAndCheckTabsVisibility = (urls: string[], isVisible: boolean): void => {
      for (const url of urls) {
        cy.visitAndCheckAppMount(url);
        assertHeaderTabsVisibility(isVisible);
      }
    };

    it('shows no tabs when not logged in', () => {
      visitPagesAndCheckTabsVisibility(['/', '/terms'], false);
    });

    it('shows tabs on company pages', () => {
      ensureLoggedIn(uploader_name, uploader_pw);
      cy.intercept('GET', '/api/companies/meta-information', []);
      cy.intercept('GET', '/api/companies*', []);
      cy.intercept('GET', '/api/metadata*', []);
      cy.intercept('GET', `/api/companies/${dummyCompany.companyId}/info`, []);
      visitPagesAndCheckTabsVisibility(
        [
          '/companies',
          '/companies/choose',
          `/companies/${dummyCompany.companyId}`,
          `/companies/${dummyCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`,
          `/companies/${dummyCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload`,
          `/companies/${dummyCompany.companyId}/frameworks/upload`,
          `/companies/${dummyCompany.companyId}/documents`,
        ],
        true
      );
    });

    it('shows tabs on datasets pages', () => {
      ensureLoggedIn(uploader_name, uploader_pw);
      visitPagesAndCheckTabsVisibility(['/datasets'], true);
    });

    it('shows tabs on portfolios pages', () => {
      ensureLoggedIn(reader_name, reader_pw);
      visitPagesAndCheckTabsVisibility(['/portfolios'], true);
    });

    it('shows tabs on data requests pages', () => {
      ensureLoggedIn(reader_name, reader_pw);
      visitPagesAndCheckTabsVisibility(['/requests'], true);
    });
  }
);
