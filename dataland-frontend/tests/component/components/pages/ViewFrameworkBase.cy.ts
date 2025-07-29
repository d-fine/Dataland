import ViewFrameworkBase from '@/components/generics/ViewFrameworkBase.vue';
import { DataTypeEnum } from '@clients/backend';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { KEYCLOAK_ROLE_UPLOADER, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakRoles';

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

  it('Should proper set the available reporting periods based on data', () => {
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
        expect(component.dataType).to.eq(DataTypeEnum.Lksg);
        expect(component.availableReportingPeriods).to.deep.equal(['2021', '2022']);
      });
    });
  });

  it('Should not display the edit and create new dataset button on the framework view page for a data reader', () => {
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
        cy.get('button[data-test=editDatasetButton]').should('not.exist');
        cy.get('a[data-test=gotoNewDatasetButton]').should('not.exist');
      });
    });
  });

  it('Should display the edit and create new dataset button for users with upload permission and framework with edit page', () => {
    const keycloakMock = minimalKeycloakMock({
      roles: [KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_UPLOADER],
    });
    //@ts-ignore
    cy.mountWithPlugins(ViewFrameworkBase, {
      keycloak: keycloakMock,
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
        cy.get('a[data-test=gotoNewDatasetButton] > button').should('exist');
        cy.get('button[data-test=editDatasetButton]').should('exist');
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
