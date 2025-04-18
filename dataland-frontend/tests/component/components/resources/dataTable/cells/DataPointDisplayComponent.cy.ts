// @ts-nocheck
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import DataPointDisplayComponent from '@/components/resources/dataTable/cells/DataPointDisplayComponent.vue';

it('tests if modal with link into position in text file works', () => {
  cy.mountWithDialog(
    DataPointDisplayComponent,
    {
      keycloak: minimalKeycloakMock({}),
    },
    {
      content: {
        displayValue: {
          fieldLabel: 'testingFieldLabel',
          value: 'testingValue',
          dataSource: {
            page: '5',
            fileName: 'testFileName',
            fileReference: 'fileReference',
          },
        },
      },
    }
  ).then(() => {});
  //test if modal opens when link is clicked
  cy.get('a').click();
  cy.get('.p-dialog-header').should('exist');
  //test if required fields are present and filled with content
  cy.get(':nth-child(1)').should('contain.text', 'Value');
  cy.get('.p-datatable-body > :nth-child(1)').should('not.be.empty');
  cy.get(':nth-child(2)').should('contain.text', 'Data source');
  cy.get('.p-dialog-content').should('contain', 'a');
  cy.get(':nth-child(3)').should('contain.text', 'Page');
  cy.get('.p-datatable-body > :nth-child(3)').should('contain.text', '5');
  //test if optional/empty field is not displayed
  cy.get('.p-datatable-body > tr').should('have.length', 3);

  //populate first optional field
  cy.mountWithDialog(
    DataPointDisplayComponent,
    {
      keycloak: minimalKeycloakMock({}),
    },
    {
      content: {
        displayValue: {
          fieldLabel: 'testingFieldLabel',
          value: 'testingValue',
          dataSource: {
            page: '5-7',
            fileName: 'testFileName',
            fileReference: 'fileReference',
          },
          quality: 'GoodQuality',
        },
      },
    }
  ).then(() => {});
  cy.get('a').click();
  //test if optional field is displayed when content is present and table content adjusts to page
  // range instead of single page
  cy.get(':nth-child(4)').should('contain.text', 'Pages');
  cy.get('.p-datatable-body > :nth-child(4)').should('contain.text', '5-7');
  cy.get('.p-datatable-body > tr').should('have.length', 4);

  //populate second optional field
  cy.mountWithDialog(
    DataPointDisplayComponent,
    {
      keycloak: minimalKeycloakMock({}),
    },
    {
      content: {
        displayValue: {
          fieldLabel: 'testingFieldLabel',
          value: 'testingValue',
          dataSource: {
            page: '5',
            fileName: 'testFileName',
            fileReference: 'fileReference',
          },
          comment: 'Test Comment',
        },
      },
    }
  ).then(() => {});
  //test if optional field is displayed when content is present
  cy.get('a').click();
  cy.get('.p-datatable-body > tr').should('have.length', 4);

  //populate second optinal field
  cy.mountWithDialog(
    DataPointDisplayComponent,
    {
      keycloak: minimalKeycloakMock({}),
    },
    {
      content: {
        displayValue: {
          fieldLabel: 'testingFieldLabel',
          value: 'testingValue',
          dataSource: {
            page: '5',
            fileName: 'testFileName',
            fileReference: 'fileReference',
          },
          quality: 'MaxQuality',
          comment: 'Testin both optional fields',
        },
      },
    }
  ).then(() => {});
  //test if optional field is displayed when content is present
  cy.get('a').click();
  cy.get('.p-datatable-body > tr').should('have.length', 5);
});
