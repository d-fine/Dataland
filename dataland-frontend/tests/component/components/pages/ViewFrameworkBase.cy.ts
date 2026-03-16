import ViewFrameworkBase from '@/components/generics/ViewFrameworkBase.vue';
import { DataTypeEnum } from '@clients/backend';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';

describe('Component test for ViewFrameworkBase', () => {
  beforeEach(() => {
    cy.intercept('/api/metadata*', { fixture: 'MetaInfoDataMocksForOneCompany', times: 1 }).as('metaDataFetch');
    cy.intercept('**/api/data/**/companies/*', {
      fixture: 'DataAndMetaInfoMocksForOneCompany',
      times: 1,
    }).as('dataFetch');
  });

  it('Should proper set the drop down options based on data', () => {
    //@ts-ignore
    cy.mountWithPlugins(ViewFrameworkBase, {
      keycloak: minimalKeycloakMock({}),
      global: {
        stubs: ['CompanyInformation'],
      },
      props: {
        dataType: DataTypeEnum.EutaxonomyFinancials,
        companyID: 'mock-company-id',
      },
    }).then(({ component }) => {
      cy.wait('@dataFetch').then(() => {
        assert(component.isDataProcessedSuccessfully);
        expect(component.dataMetaInformation.length).to.equal(9);
      });
    });
  });

  it('Should display the download data button for data reader and open download modal', () => {
    //@ts-ignore
    cy.mountWithPlugins(ViewFrameworkBase, {
      keycloak: minimalKeycloakMock({}),
      global: {
        stubs: ['CompanyInformation'],
      },
      props: {
        dataType: DataTypeEnum.Lksg,
        companyID: 'mock-company-id',
      },
    }).then(({ component }) => {
      cy.wait('@dataFetch').then(() => {
        assert(component.isDataProcessedSuccessfully);
        cy.get('button[data-test=downloadDataButton]').should('exist').click();
      });
    });
  });
});
