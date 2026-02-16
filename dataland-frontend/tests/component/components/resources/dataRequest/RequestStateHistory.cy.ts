import RequestStateHistory from '@/components/resources/dataRequest/RequestStateHistory.vue';
import {
  DataSourcingState,
  DisplayedState,
  type ExtendedRequestHistoryEntry,
  type RequestHistoryEntry,
  RequestState,
} from '@clients/datasourcingservice';
import { getMountingFunction } from '@ct/testUtils/Mount';

describe('Component tests for the Request State History', function (): void {
  const dummyRequestHistoryEntry: RequestHistoryEntry[] = [
    {
      modificationDate: 1697049600000,
      displayedState: DisplayedState.Open,
    },
    {
      modificationDate: 1697049600000 + 600000,
      displayedState: DisplayedState.Validated,
    },

    {
      modificationDate: 1697049600000 + 2 * 600000,
      displayedState: DisplayedState.Withdrawn,
    },
  ];

  const dummyExtendedRequestHistoryEntry: ExtendedRequestHistoryEntry[] = [
    {
      modificationDate: 1697049600000,
      displayedState: DisplayedState.Open,
      requestState: RequestState.Open,
      adminComment: 'Request opened',
    },
    {
      modificationDate: 1697049600000 + 600000,
      displayedState: DisplayedState.Validated,
      requestState: RequestState.Processing,
      dataSourcingState: DataSourcingState.Initialized,
      adminComment: 'Processing request',
    },

    {
      modificationDate: 1697049600000 + 1.5 * 600000,
      displayedState: DisplayedState.DocumentSourcing,
      requestState: RequestState.Processing,
      dataSourcingState: DataSourcingState.DocumentSourcing,
      adminComment: 'Processing request',
    },

    {
      modificationDate: 1697049600000 + 2 * 600000,
      displayedState: DisplayedState.Withdrawn,
      requestState: RequestState.Withdrawn,
      dataSourcingState: DataSourcingState.DocumentSourcing,
      adminComment: 'Request withdrawn',
    },
  ];

  /**
   * Helper function to check the existence of columns in the state history table based on user role
   * @param isAdminUser - boolean indicating whether the user is an admin or not
   */
  function checkColumnExistence(isAdminUser: boolean, requestHistory: RequestHistoryEntry[]): void {
    getMountingFunction()(RequestStateHistory, {
      props: {
        stateHistory: requestHistory,
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
    checkColumnExistence(true, dummyExtendedRequestHistoryEntry);
  });

  it('Check existence of columns in state history table for non admin users', function () {
    checkColumnExistence(false, dummyRequestHistoryEntry);
  });
});
