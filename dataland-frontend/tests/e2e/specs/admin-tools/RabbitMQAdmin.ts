import { getStringCypressEnv } from '@e2e/utils/Cypress';

const queues = [
  'dataQualityAssuredBackendDataManager',
  'dataQualityAssuredCommunityManagerDataManager',
  'dataQualityAssuredCommunityManagerNotificationService',
  'dataStoredBackendDataManager',
  'dataStoredBackendPrivateDataManager',
  'dataStoredDocumentManager',
  'deadLetterQueue',
  'documentQualityAssuredDocumentManager',
  'documentReceivedDatabaseDataStore',
  'itemStoredDataQaService',
  'itemStoredDeleteQaInfoQaService',
  'itemStoredDocumentQaService',
  'privateRequestReceivedCommunityManager',
  'privateRequestReceivedCommunityManagerNotificationService',
  'privateRequestReceivedEurodatDataStore',
  'requestReceivedInternalStorageDatabaseDataStore',
  'sendEmailService',
  'storage.datapoint.fanout',
];

describe('As a developer, I expect the RabbitMQ GUI console to be available to me. Also check if all expected channels exist.', () => {
  it('Checks if the RabbitMQ Management GUI is available and the login page is shown. Then check that all expected queues exist.', () => {
    cy.visit('http://dataland-admin:6789/rabbitmq');
    cy.get('input[name=username]').should('exist').type(getStringCypressEnv('RABBITMQ_USER'));
    cy.get('input[name=password]').should('exist').type(getStringCypressEnv('RABBITMQ_PASS'));
    cy.get('input[type=submit]').should('contain.value', 'Login').click();
    cy.get('#logout').contains('Log out').should('contain.value', 'Log out');
    cy.get("ul[id='tabs'").find("a[href='#/queues']").click();
    cy.contains('table th', 'Overview')
      .invoke('parents', 'table')
      .find('tbody tr')
      .its('length')
      .then((rowCount) => {
        cy.wrap(queues.length).should('eq', rowCount);
      });
    cy.get('table[class=list]')
      .should('exist')
      .then(() => {
        queues.forEach((queue) => {
          cy.get('table[class=list]').contains(queue).should('contain.text', queue);
        });
      });
  });
});
