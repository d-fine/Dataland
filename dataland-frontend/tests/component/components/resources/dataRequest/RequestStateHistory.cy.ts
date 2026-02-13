import RequestStateHistory from '@/components/resources/dataRequest/RequestStateHistory.vue';
import { type RequestHistoryEntry } from '@clients/datasourcingservice';
import { getMountingFunction } from '@ct/testUtils/Mount';

describe('Component tests for the Request State History', function (): void {
  const dummyRequestHistoryEntry: RequestHistoryEntry[] = [];

  /**
   * Helper function to check the existence of columns in the state history table based on user role
   * @param isAdminUser - boolean indicating whether the user is an admin or not
   */
  function checkColumnExistence(isAdminUser: boolean): void {
    getMountingFunction()(RequestStateHistory, {
      props: {
        stateHistory: dummyRequestHistoryEntry,
        isAdmin: isAdminUser,
      },
    });
    cy.get('[data-test="stateHistoryTable"]').should('exist').and('be.visible');
    cy.get('[data-test="stateHistoryTable"] th').should('contain', 'Updated On');
    cy.get('[data-test="stateHistoryTable"] th').should('contain', 'State');
    cy.get('[data-test="stateHistoryTable"] th').should(isAdminUser ? 'contain' : 'not.contain', 'Request State');
    cy.get('[data-test="stateHistoryTable"] th').should(isAdminUser ? 'contain' : 'not.contain', 'Data Sourcing State');
    cy.get('[data-test="stateHistoryTable"] th').should(isAdminUser ? 'contain' : 'not.contain', 'Comment');
  }

  it('Check existence of columns in state history table for Admins', function () {
    checkColumnExistence(true);
  });

  it('Check existence of columns in state history table for non admin users', function () {
    checkColumnExistence(false);
  });
});
