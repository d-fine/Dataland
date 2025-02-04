// @ts-nocheck
import DocumentLink from '@/components/resources/frameworkDataSearch/DocumentLink.vue';
import DataPointDataTable from '@/components/general/DataPointDataTable.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { DataTypeEnum } from '@clients/backend';

describe('check that the document link component works and is displayed correctly', function (): void {
  it('Check that there are no icons before and after triggering a download', function (): void {
    cy.intercept('**/documents/dummyFile**', {
      statusCode: 200,
    }).as('downloadComplete');
    cy.mountWithPlugins(DocumentLink, {
      keycloak: minimalKeycloakMock({}),

      // @ts-ignore
      props: {
        downloadName: 'Test',
        fileReference: 'dummyFileReference',
        dataType: DataTypeEnum.Heimathafen,
      },
    }).then(() => {
      validateNoIcons();
      cy.get("[data-test='Report-Download-Test']").should('exist').click();
      cy.wait('@downloadComplete').then(() => {
        validateNoIcons();
      });
    });
  });
  it('Check that Download Progress Spinner appears if the prop changes', function (): void {
    cy.mountWithPlugins(DocumentLink, {
      // @ts-ignore
      props: {
        downloadName: 'Test',
        fileReference: 'dummyFileReference',
        dataType: DataTypeEnum.Heimathafen,
      },
      data() {
        return {
          percentCompleted: undefined,
        };
      },
    }).then((mounted) => {
      validateNoIcons();

      void mounted.wrapper
        .setData({
          percentCompleted: 50,
        })
        .then(() => {
          cy.get('[data-test="spinner-icon"]').should('exist');
          cy.get("[data-test='percentage-text']").should('exist').should('have.text', '50%');
          cy.get("[data-test='checkmark-icon']").should('not.exist');
        });
    });
  });

  it('Check that Download Progress Spinner disappears and the checkmark appears', function (): void {
    cy.mountWithPlugins(DocumentLink, {
      // @ts-ignore
      props: {
        downloadName: 'Test',
        fileReference: 'dummyFileReference',
      },
      data() {
        return {
          percentCompleted: 50,
        };
      },
    }).then((mounted) => {
      cy.get('[data-test="spinner-icon"]').should('exist');
      cy.get("[data-test='percentage-text']").should('exist').should('have.text', '50%');
      cy.get("[data-test='checkmark-icon']").should('not.exist');
      void mounted.wrapper
        .setData({
          percentCompleted: 100,
        })
        .then(() => {
          cy.get("[data-test='checkmark-icon']").should('exist');
          cy.get('[data-test="spinner-icon"]').should('not.exist');
          cy.get("[data-test='percentage-text']").should('not.exist');
        });
    });
  });
  it('Check that Download Progress Checkmark disappears again', function (): void {
    cy.mountWithPlugins(DocumentLink, {
      // @ts-ignore
      props: {
        downloadName: 'Test',
        fileReference: 'dummyFileReference',
      },
      data() {
        return {
          percentCompleted: 100,
        };
      },
    }).then((mounted) => {
      cy.get('[data-test="spinner-icon"]').should('not.exist');
      cy.get("[data-test='percentage-text']").should('not.exist');
      cy.get("[data-test='checkmark-icon']").should('exist');
      void mounted.wrapper
        .setData({
          percentCompleted: undefined,
        })
        .then(() => {
          validateNoIcons();
        });
    });
  });
  it('Check that the label does not display "page" when page number is null', function (): void {
    cy.mountWithPlugins(DataPointDataTable, {
      keycloak: minimalKeycloakMock({}),

      // @ts-ignore
      props: {},
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
            dataType: DataTypeEnum.Heimathafen,
          },
        };
      },
    }).then(() => {
      cy.get("[data-test='Report-Download-FileName']").should('contain', 'FileName');
      cy.get("[data-test='Report-Download-FileName']").should('not.contain', 'page null');
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
