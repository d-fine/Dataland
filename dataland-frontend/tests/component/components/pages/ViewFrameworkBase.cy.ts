// @ts-nocheck
import ViewFrameworkBase from '@/components/generics/ViewFrameworkBase.vue';
import { type DataMetaInformation, DataTypeEnum } from '@clients/backend';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { KEYCLOAK_ROLE_UPLOADER, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakUtils';

describe('Component test for ViewFrameworkBase', () => {
  it('Should proper set the drop down options based on data', () => {
    cy.intercept('**/api/metadata*', { fixture: 'MetaInfoDataMocksForOneCompany', times: 1 }).as('metaDataFetch');
    cy.mountWithPlugins(ViewFrameworkBase, {
      keycloak: minimalKeycloakMock({}),
      global: {
        stubs: ['CompanyInformation'],
      },
    }).then((mounted) => {
      void mounted.wrapper.setProps({
        dataType: DataTypeEnum.EutaxonomyFinancials,
        companyID: 'mock-company-id',
      });
      cy.wait('@metaDataFetch').then(() => {
        expect(mounted.wrapper.vm.dataTypesInDropdown).to.be.an('array').that.is.not.empty;
        expect(mounted.wrapper.vm.dataTypesInDropdown).to.deep.equal([
          {
            label: humanizeStringOrNumber(DataTypeEnum.EutaxonomyFinancials),
            value: DataTypeEnum.EutaxonomyFinancials,
          },
          {
            label: humanizeStringOrNumber(DataTypeEnum.EutaxonomyNonFinancials),
            value: DataTypeEnum.EutaxonomyNonFinancials,
          },
          { label: humanizeStringOrNumber(DataTypeEnum.Lksg), value: DataTypeEnum.Lksg },
          { label: humanizeStringOrNumber(DataTypeEnum.Sfdr), value: DataTypeEnum.Sfdr },
        ]);
      });
    });
  });

  it('Should proper set the map of reporting periods to meta info based on data', () => {
    cy.intercept('**/api/metadata*', { fixture: 'MetaInfoDataMocksForOneCompany', times: 1 }).as('metaDataFetch');
    cy.mountWithPlugins(ViewFrameworkBase, {
      keycloak: minimalKeycloakMock({}),
      global: {
        stubs: ['CompanyInformation'],
      },
    }).then((mounted) => {
      cy.fixture('MetaInfoAssociatedWithReportingPeriodByDataTypeMock.json').then(
        async (data: {
          'eutaxonomy-financials': [[string, DataMetaInformation]];
          lksg: [[string, DataMetaInformation]];
        }) => {
          cy.intercept('**/api/metadata*', { fixture: 'MetaInfoDataMocksForOneCompany', times: 1 }).as('metaDataFetch');
          await mounted.wrapper.setProps({ dataType: DataTypeEnum.Lksg });
          expect(mounted.wrapper.props('dataType')).to.eq(DataTypeEnum.Lksg);
          cy.wait('@metaDataFetch').then(() => {
            expect(Array.from(mounted.wrapper.vm.mapOfReportingPeriodToActiveDataset)).to.deep.equal(
              data[DataTypeEnum.Lksg]
            );
          });
        }
      );
    });
  });

  it('Should not display the edit and create new dataset button on the framework view page for a data reader', () => {
    cy.intercept('**/api/metadata**', []);
    cy.mountWithPlugins(ViewFrameworkBase, {
      keycloak: minimalKeycloakMock({}),
      global: {
        stubs: ['CompanyInformation'],
      },
    }).then((mounted) => {
      void mounted.wrapper.setProps({
        dataType: DataTypeEnum.Lksg,
        companyID: 'mock-company-id',
      });
      cy.get('button[data-test=editDatasetButton]').should('not.exist');
      cy.get('a[data-test=gotoNewDatasetButton]').should('not.exist');
    });
  });

  it(
    'Should display the edit and create new dataset button for users with ' +
      'upload permission and framework with edit page',
    () => {
      const keycloakMock = minimalKeycloakMock({
        roles: [KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_UPLOADER],
      });
      cy.intercept('**/api/metadata**', []);
      cy.mountWithPlugins(ViewFrameworkBase, {
        keycloak: keycloakMock,
        global: {
          stubs: ['CompanyInformation'],
        },
      }).then((mounted) => {
        void mounted.wrapper.setProps({
          dataType: DataTypeEnum.Lksg,
          companyID: 'mock-company-id',
        });
        cy.get('a[data-test=gotoNewDatasetButton] > button').should('exist');
        cy.get('button[data-test=editDatasetButton]').should('exist');
      });
    }
  );

  it('Should display the download data button for data reader ' + 'and open download modal', () => {
    cy.intercept('**/api/metadata*', []);
    cy.mountWithPlugins(ViewFrameworkBase, {
      keycloak: minimalKeycloakMock({}),
      global: {
        stubs: ['CompanyInformation'],
      },
    }).then((mounted) => {
      void mounted.wrapper.setProps({});

      cy.get('button[data-test=downloadDataButton]').should('exist').click();
      cy.get('[data-test=downloadModal]').should('exist');
    });
  });

  it('Should run handleDatasetDownload with year and format selection and call forceFileDownload', () => {
    const selectedYear = '2022';
    const selectedFormat = 'csv';
    const dataId = '1234';
    const mockDataMetaInfo = new Map([[selectedYear, { dataId: dataId }]]);

    cy.intercept('**/api/data/**/**/csv/*', { requestStatus: 200, times: 1 }).as('exportCsv');
    const keycloakMock = minimalKeycloakMock({
      roles: [KEYCLOAK_ROLE_USER],
    });

    cy.mountWithPlugins(ViewFrameworkBase, {
      keycloak: keycloakMock,
    }).then(async (mounted) => {
      mounted.wrapper.setProps({
        dataType: DataTypeEnum.Sfdr,
      });
      cy.stub(window.URL, 'createObjectURL').returns('the actual data to download');
      mounted.wrapper.vm.mapOfReportingPeriodToActiveDataset = mockDataMetaInfo;

      cy.spy(mounted.wrapper.vm, 'forceFileDownload').as('forceFileDownload');
      await mounted.wrapper.vm.handleDatasetDownload(selectedYear, selectedFormat);
      const expectedFilename = `${dataId}.${selectedFormat}`;

      cy.get('@forceFileDownload').should('be.called');
      cy.get('@forceFileDownload').should('be.calledWith', 'mock csv data', expectedFilename);
    });
  });
});
