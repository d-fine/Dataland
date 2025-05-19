import { getStringCypressEnv } from '@e2e/utils/Cypress';

const queues = [
  'dataQualityAssuredBackendDataManager',
  'dataQualityAssuredCommunityManagerDataManager',
  'dataStoredBackendPrivateDataManager',
  'dataStoredDocumentManager',
  'deadLetterQueue',
  'documentQualityAssuredDocumentManager',
  'documentReceivedDatabaseDataStore',
  'itemStoredDocumentQaService',
  'privateRequestReceivedCommunityManager',
  'sendEmailService',
  'internal-storage.storeDatasets',
  'internal-storage.storeDatapoints',
  'internal-storage.deleteDatasets',
  'backend.removeDataFromMemory',
  'backend.updateDataPointQaStatus',
  'qa-service.deleteDatasets',
  'qa-service.qaDatasets',
  'qa-service.qaDataPoints',
  'qa-service.migrateDatasets',
  'community-manager.queue.nonSourceableData',
];

/**
 * Checks if all expected queues exist.
 * @param $rows - rows of the table.
 * @param expectedQueues - expected queues.
 */
function checkQueuesExist($rows: JQuery<HTMLElement>, expectedQueues: string[]): void {
  const actualQueues = [...$rows].map((row) => {
    const nameCell = row.querySelector('td:nth-child(2) a');
    return nameCell?.textContent?.trim() ?? '';
  });
  const missingQueues = expectedQueues.filter((q) => !actualQueues.includes(q));
  if (missingQueues.length > 0) {
    throw new Error(`Missing queues: ${missingQueues.join(', ')}`);
  }
}

describe('As a developer, I expect the RabbitMQ GUI console to be available to me. Also check if all expected channels exist.', () => {
  it('Checks if the RabbitMQ Management GUI is available and the login page is shown. Then check that all expected queues exist.', () => {
    cy.visitAndCheckExternalAdminPage({
      url: 'https://dataland-admin:6789/rabbitmq',
      interceptPattern: '**/rabbitmq/**',
      elementSelector: 'input[name=username]',
    });
    cy.get('input[name=username]').should('exist').type(getStringCypressEnv('RABBITMQ_USER'));
    cy.get('input[name=password]').should('exist').type(getStringCypressEnv('RABBITMQ_PASS'));
    cy.get('input[type=submit]').should('contain.value', 'Login').click();
    cy.get('#logout').contains('Log out').should('contain.value', 'Log out');
    cy.get("ul[id='tabs']").find("a[href='#/queues']").click();
    cy.get('table.list tbody tr td:nth-child(2) a', {
      timeout: Cypress.env('medium_timeout_in_ms'),
    }).should('have.length.greaterThan', 0);

    cy.get('table.list tbody tr').then(($rows) => {
      checkQueuesExist($rows, queues);
    });
  });
});
