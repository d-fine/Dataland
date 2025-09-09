import { ensureLoggedIn, getKeycloakToken } from '@e2e/utils/Auth.ts';
import { reader_name, reader_pw, uploader_name, uploader_pw } from '@e2e/utils/Cypress.ts';
import { searchBasicCompanyInformationForDataType } from '@e2e/utils/GeneralApiUtils.ts';
import { DataTypeEnum, type BasicCompanyInformation } from '@clients/backend';

describe('Test header tabs visibility for user role', () => {
  let alphaCompanyIdAndName: { companyId: string; companyName: string };
  const tabNames = ['COMPANIES', 'MY DATASETS', 'MY PORTFOLIOS', 'MY DATA REQUESTS'];

  before(() => {
    getKeycloakToken(reader_name, reader_pw)
      .then((token: string) => {
        return cy.wrap(searchBasicCompanyInformationForDataType(token, DataTypeEnum.EutaxonomyNonFinancials));
      })
      .then((basicCompanyInfos) => {
        const infos = basicCompanyInfos as BasicCompanyInformation[];
        expect(infos).to.be.not.empty;
        alphaCompanyIdAndName = {
          companyId: infos[0].companyId,
          companyName: infos[0].companyName,
        };
      });
  });

  const assertTabVisibility = (tabName: string, isVisible: boolean): void => {
    cy.contains(tabName).should(isVisible ? 'be.visible' : 'not.exist');
  };

  const assertHeaderTabsVisibility = (isVisible: boolean): void => {
    tabNames.forEach((tab) => assertTabVisibility(tab, isVisible));
  };

  const visitPagesAndCheckTabsVisibility = (urls: string[], isVisible: boolean): void => {
    urls.forEach((url) => {
      cy.visit(url);
      assertHeaderTabsVisibility(isVisible);
    });
  };

  it('shows no tabs when not logged in', () => {
    visitPagesAndCheckTabsVisibility(['/', '/terms'], false);
  });

  it('shows tabs on company pages', () => {
    ensureLoggedIn(reader_name, reader_pw);
    visitPagesAndCheckTabsVisibility(
      [
        '/companies',
        `/companies/${alphaCompanyIdAndName.companyId}`,
        `/companies/${alphaCompanyIdAndName.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`,
        `/companies/${alphaCompanyIdAndName.companyId}/documents`,
      ],
      true
    );
  });

  it('shows tabs on datasets pages', () => {
    ensureLoggedIn(uploader_name, uploader_pw);
    visitPagesAndCheckTabsVisibility(['/datasets'], true);
    cy.get('.p-button-label').click();
    assertHeaderTabsVisibility(true);
    cy.get('#company_search_bar_standard').type(alphaCompanyIdAndName.companyName);
    cy.get('[data-pc-section="list"]').contains(alphaCompanyIdAndName.companyName).click();
    assertHeaderTabsVisibility(true);
    cy.get('.p-button-label').contains('Create Dataset').click();
    assertHeaderTabsVisibility(true);
  });

  it('shows tabs on portfolios pages', () => {
    ensureLoggedIn(reader_name, reader_pw);
    visitPagesAndCheckTabsVisibility(['/portfolios'], true);
  });

  it('shows tabs on data requests pages', () => {
    ensureLoggedIn(reader_name, reader_pw);
    visitPagesAndCheckTabsVisibility(['/bulkdatarequest', '/requests'], true);
  });
});
