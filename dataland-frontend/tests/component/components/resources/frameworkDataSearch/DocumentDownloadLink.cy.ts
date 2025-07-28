import DocumentDownloadLink from '@/components/resources/frameworkDataSearch/DocumentDownloadLink.vue';
import DataPointDataTable from '@/components/general/DataPointDataTable.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { DataTypeEnum } from '@clients/backend';

describe('check that the document link component works and is displayed correctly for a logged in user', function (): void {
  it('Check that there are no icons before and after triggering a download', function (): void {
    cy.intercept('**/documents/dummyFile**', {
      statusCode: 200,
    }).as('downloadComplete');
    //@ts-ignore
    cy.mountWithPlugins(DocumentDownloadLink, {
      keycloak: minimalKeycloakMock({}),

      props: {
        documentDownloadInfo: {
          downloadName: 'Test',
          fileReference: 'dummyFileReference',
          dataType: DataTypeEnum.Lksg,
        },
      },
    }).then(() => {
      validateNoIcons();
      cy.get("[data-test='Report-Download-Test']").click();
      cy.wait('@downloadComplete').then(() => {
        validateNoIcons();
      });
    });
  });

  it('Check that Download Progress Spinner appears for a logged in user if the prop changes', function (): void {
    //@ts-ignore
    cy.mountWithPlugins(DocumentDownloadLink, {
      keycloak: minimalKeycloakMock({
        authenticated: true,
      }),

      props: {
        documentDownloadInfo: {
          downloadName: 'Test',
          fileReference: 'dummyFileReference',
          dataType: DataTypeEnum.Lksg,
        },
      },
    }).then((mounted) => {
      validateNoIcons();

      mounted.wrapper.vm.percentCompleted = 50;

      cy.get('[data-test="spinner-icon"]').should('exist');
      cy.get("[data-test='percentage-text']").should('exist').should('have.text', '50%');
      cy.get("[data-test='checkmark-icon']").should('not.exist');
    });
  });

  it('Check that Download Progress Spinner disappears for a logged in user and the checkmark appears', function (): void {
    //@ts-ignore
    cy.mountWithPlugins(DocumentDownloadLink, {
      keycloak: minimalKeycloakMock({
        authenticated: true,
      }),

      props: {
        documentDownloadInfo: {
          downloadName: 'Test',
          fileReference: 'dummyFileReference',
          dataType: DataTypeEnum.Lksg,
        },
      },
    }).then((mounted) => {
      mounted.wrapper.vm.percentCompleted = 50;
      mounted.wrapper.vm.percentCompleted = 100;

      cy.get('[data-test="spinner-icon"]').should('not.exist');
      cy.get("[data-test='percentage-text']").should('not.exist');
      cy.get("[data-test='checkmark-icon']").should('exist');
    });
  });

  it('Check that Download Progress Checkmark disappears again for a logged in user', function (): void {
    //@ts-ignore
    cy.mountWithPlugins(DocumentDownloadLink, {
      keycloak: minimalKeycloakMock({
        authenticated: true,
      }),

      props: {
        documentDownloadInfo: {
          downloadName: 'Test',
          fileReference: 'dummyFileReference',
        },
      },
    }).then((mounted) => {
      mounted.wrapper.vm.percentCompleted = 50;
      mounted.wrapper.vm.percentCompleted = 100;
      mounted.wrapper.vm.percentCompleted = undefined;

      validateNoIcons();
    });
  });

  it('Check that document download link behaves as expected for a non logged in user', function (): void {
    //@ts-ignore
    cy.mountWithPlugins(DocumentDownloadLink, {
      keycloak: minimalKeycloakMock({
        authenticated: false,
      }),

      props: {
        documentDownloadInfo: {
          downloadName: 'Test',
          fileReference: 'dummyFileReference',
        },
      },
    }).then(() => {
      cy.get('[data-test="download-icon"]').should('not.exist');
      cy.get('[data-test="spinner-icon"]').should('not.exist');
      cy.get('[data-test="download-text-Test"]').should('not.have.attr', '@click');
    });
  });

  it('Check that the label does not display "page" when page number is null', function (): void {
    //@ts-ignore
    cy.mountWithPlugins(DataPointDataTable, {
      keycloak: minimalKeycloakMock({
        authenticated: true,
      }),
      data() {
        return {
          dialogData: {
            dataPointDisplay: {
              value: 'Some Value',
              quality: 'Some quality',
              dataSource: {
                fileName: 'FileName',
                page: null,
              },
              comment: 'Some comment',
            },
            dataId: '12345',
            dataType: DataTypeEnum.EutaxonomyFinancials,
          },
        };
      },
    }).then(() => {
      cy.get("[data-test='Report-Download-FileName']").should('contain', 'FileName');
      cy.get("[data-test='Report-Download-FileName']").should('not.contain', 'page');
    });
  });
});

/**
 * Checks that no icons exist
 */
function validateNoIcons(): void {
  cy.get('[data-test="spinner-icon"]').should('not.exist');
  cy.get("[data-test='percentage-text']").should('not.exist');
  cy.get("[data-test='checkmark-icon']").should('not.exist');
}
