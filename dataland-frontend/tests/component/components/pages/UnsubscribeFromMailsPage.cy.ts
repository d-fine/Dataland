import UnsubscribeFromMailsPage from '@/components/pages/UnsubscribeFromMailsPage.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { getMountingFunction } from '@ct/testUtils/Mount';

describe('Component tests for the Quality Assurance page', () => {
  const testUuid = '123e4567-e89b-12d3-a456-426614174000';
  const expectedSuccessTextFromApi = `Successfully unsubscribed email address corresponding to the UUID: ${testUuid}`;
  const expectedFailTextFromApi = `There is no email address corresponding to the UUID: ${testUuid}.`;

  it('Check unsubscribe page for a valid uuid', () => {
    getMountingFunction({ keycloak: minimalKeycloakMock() })(UnsubscribeFromMailsPage, {
      props: {
        subscriptionId: testUuid,
      },
    });

    cy.intercept(`**/email/subscriptions/${testUuid}`, expectedSuccessTextFromApi).as('unsubscribe');
    cy.get('[data-test="unsubscribeButton"]').click();
    cy.wait('@unsubscribe');

    cy.get('[data-test="unsubscribeMessage"]').should(
      'have.text',
      'You have been successfully removed from our mailing list.'
    );
    cy.get('[data-test="unsubscribeButton"]').should('not.exist');
  });

  it('Check unsubscribe page for an invalid uuid', () => {
    getMountingFunction({ keycloak: minimalKeycloakMock() })(UnsubscribeFromMailsPage, {
      props: {
        subscriptionId: testUuid,
      },
    });

    cy.intercept(`**/email/subscriptions/${testUuid}`, expectedFailTextFromApi).as('unsubscribe');
    cy.get('[data-test="unsubscribeButton"]').click();
    cy.wait('@unsubscribe');

    cy.get('[data-test="unsubscribeMessage"]').should(
      'have.text',
      'This UUID does not belong to any email address in our mailing list.'
    );
    cy.get('[data-test="unsubscribeButton"]').should('not.exist');
  });
});
