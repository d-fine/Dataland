import RequestStateHistory from '@/components/resources/dataRequest/RequestStateHistory.vue';
import {
  RequestState,
  type StoredRequest,
  type DataSourcingWithoutReferences,
  DataSourcingState,
} from '@clients/datasourcingservice';
import { getMountingFunction } from '@ct/testUtils/Mount';

describe('Component tests for the Request State History', function (): void {
  const dummyCreationTimestamp = 1714315046000;
  const dummyRequest = {
    id: 'dummy-request-id',
    companyId: 'dummy-company-id',
    reportingPeriod: '2024',
    dataType: 'sfdr',
    userId: 'dummy-user-id',
    creationTimestamp: dummyCreationTimestamp,
    lastModifiedDate: dummyCreationTimestamp,
    requestPriority: 'Low',
    state: RequestState.Open,
  } as StoredRequest;
  const dummyStateHistory = [
    dummyRequest,
    {
      ...dummyRequest,
      lastModifiedDate: dummyCreationTimestamp + 600000,
      state: RequestState.Processing,
      adminComment: 'Processing started',
    },
    {
      ...dummyRequest,
      lastModifiedDate: dummyCreationTimestamp + 3 * 600000,
      state: RequestState.Withdrawn,
      adminComment: 'Request withdrawn by user',
    },
    {
      ...dummyRequest,
      lastModifiedDate: dummyCreationTimestamp + 2 * 600000,
      state: RequestState.Processed,
    },
  ] as Array<StoredRequest>;

  const dummyDataSourcingHistory = [
    {
      dataSourcingId: 'dummy-data-sourcing-id',
      companyId: 'dummy-company-id',
      reportingPeriod: '2024',
      dataType: 'sfdr',
      state: DataSourcingState.Initialized,
      lastModifiedDate: dummyCreationTimestamp + 1.1 * 600000,
    },
    {
      dataSourcingId: 'dummy-data-sourcing-id',
      companyId: 'dummy-company-id',
      reportingPeriod: '2024',
      dataType: 'sfdr',
      state: DataSourcingState.DocumentSourcing,
      lastModifiedDate: dummyCreationTimestamp + 1.2 * 600000,
    },
    {
      dataSourcingId: 'dummy-data-sourcing-id',
      companyId: 'dummy-company-id',
      reportingPeriod: '2024',
      dataType: 'sfdr',
      state: DataSourcingState.DocumentSourcingDone,
      lastModifiedDate: dummyCreationTimestamp + 1.8 * 600000,
    },
    {
      dataSourcingId: 'dummy-data-sourcing-id',
      companyId: 'dummy-company-id',
      reportingPeriod: '2024',
      dataType: 'sfdr',
      state: DataSourcingState.DataVerification,
      lastModifiedDate: dummyCreationTimestamp + 1.9 * 600000,
    },
    {
      dataSourcingId: 'dummy-data-sourcing-id',
      companyId: 'dummy-company-id',
      reportingPeriod: '2024',
      dataType: 'sfdr',
      state: DataSourcingState.Done,
      lastModifiedDate: dummyCreationTimestamp + 2 * 600000,
    },
    {
      dataSourcingId: 'dummy-data-sourcing-id',
      companyId: 'dummy-company-id',
      reportingPeriod: '2024',
      dataType: 'sfdr',
      state: DataSourcingState.DocumentSourcing,
      lastModifiedDate: dummyCreationTimestamp + 4 * 600000,
    },
  ] as Array<DataSourcingWithoutReferences>;

  /**
   * Helper function to check the existence of columns in the state history table based on user role
   * @param isAdminUser - boolean indicating whether the user is an admin or not
   */
  function checkColumnExistence(isAdminUser: boolean): void {
    getMountingFunction()(RequestStateHistory, {
      props: {
        stateHistory: dummyStateHistory,
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

  it('Check that entries with requestState = Processing and dataSourcingState = null are not shown', function () {
    getMountingFunction()(RequestStateHistory, {
      props: {
        stateHistory: dummyStateHistory,
        isAdmin: true,
      },
    });
    cy.get('[data-test="lastModifiedDate"]').should('have.length', dummyStateHistory.length - 1);
  });

  it('Check that entries with requestState = Processed and dataSourcingState = DataVerification are not shown', function () {
    getMountingFunction()(RequestStateHistory, {
      props: {
        stateHistory: [dummyStateHistory[3]] as Array<StoredRequest>,
        dataSourcingHistory: [
          dummyDataSourcingHistory[3],
          dummyDataSourcingHistory[4],
        ] as Array<DataSourcingWithoutReferences>,
        isAdmin: true,
      },
    });
    cy.get('[data-test="lastModifiedDate"]').should('have.length', 2);
  });

  it('Check that sorting is chronological', function () {
    getMountingFunction()(RequestStateHistory, {
      props: {
        stateHistory: dummyStateHistory,
        isAdmin: true,
      },
    });
    cy.get('[data-test="lastModifiedDate"]').then(($dates) => {
      const renderedDates = $dates.toArray().map((el) => el.textContent?.trim() ?? '');
      let currentDate = Number.NEGATIVE_INFINITY;
      for (const date of renderedDates) {
        expect(Date.parse(date)).greaterThan(currentDate);
        currentDate = Date.parse(date);
      }
    });
  });

  it('Check that same admin comment is visible for equal request states', function () {
    getMountingFunction()(RequestStateHistory, {
      props: {
        stateHistory: dummyStateHistory,
        dataSourcingHistory: dummyDataSourcingHistory,
        isAdmin: true,
      },
    });

    cy.get('[data-test="stateHistoryTable"]').should('exist').and('be.visible');

    cy.get('[data-test="stateHistoryTable"] tr').then(($rows) => {
      const stateToComments: Record<string, string> = {};
      $rows.each((_, row) => {
        const state = row.querySelector('[data-test="requestState"]')?.textContent?.trim();
        const comment = row.querySelector('[data-test="adminComment"]')?.textContent?.trim();
        if (state && comment) {
          if (stateToComments[state]) {
            expect(comment).eq(stateToComments[state]);
          } else {
            stateToComments[state] = comment;
          }
        }
      });
    });
  });

  it('Check that consecutive rows with same mixed status are only shown once in non admin view', function () {
    getMountingFunction()(RequestStateHistory, {
      props: {
        stateHistory: [dummyStateHistory[2], dummyStateHistory[3]] as Array<StoredRequest>,
        dataSourcingHistory: [
          dummyDataSourcingHistory[3],
          dummyDataSourcingHistory[4],
          dummyDataSourcingHistory[5],
        ] as Array<DataSourcingWithoutReferences>,
        isAdmin: false,
      },
    });
    cy.get('[data-test="lastModifiedDate"]').should('have.length', 3);
  });
});
