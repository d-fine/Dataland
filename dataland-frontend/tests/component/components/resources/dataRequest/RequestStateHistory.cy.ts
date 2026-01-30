import RequestStateHistory from '@/components/resources/dataRequest/RequestStateHistory.vue';
import {
  RequestState,
  type StoredRequest,
  type DataSourcingWithoutReferences,
  DataSourcingState,
} from '@clients/datasourcingservice';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
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
      lastModifiedDate: dummyCreationTimestamp + 2 * 600000,
      state: RequestState.Processed,
    },
  ] as Array<StoredRequest>;
  it('renders correct state history table with request history only', function () {
    getMountingFunction()(RequestStateHistory, {
      props: {
        stateHistory: dummyStateHistory,
        isAdmin: true,
      },
    });

    cy.get('[data-test="stateHistoryTable"]').should('exist').and('be.visible');
    cy.get('[data-test="lastModifiedDate"]').should('have.length', dummyStateHistory.length);
    cy.get('[data-test="historyType"]').should('have.length', dummyStateHistory.length);

    const sortedHistory = [...dummyStateHistory].sort((a, b) => b.lastModifiedDate - a.lastModifiedDate);

    for (const [idx, entry] of sortedHistory.entries()) {
      cy.get('[data-test="lastModifiedDate"]')
        .eq(idx)
        .should('contain.text', convertUnixTimeInMsToDateString(entry.lastModifiedDate));
      cy.get('[data-test="historyType"]').eq(idx).should('contain.text', 'Request');
      cy.get('.dataland-inline-tag').eq(idx).should('contain.text', entry.state);
      const expectedComment = entry.adminComment || '—';
      cy.get('[data-test="adminComment"]').eq(idx).should('contain.text', expectedComment);
    }
  });

  it('renders correct combined history table with both request and data sourcing history', function () {
    const dataSourcingHistory: DataSourcingWithoutReferences[] = [
      {
        dataSourcingId: 'dummy-data-sourcing-id',
        companyId: 'dummy-company-id',
        reportingPeriod: '2024',
        dataType: 'sfdr',
        state: DataSourcingState.DocumentSourcing,
        adminComment: 'Document sourcing started',
        lastModifiedDate: dummyCreationTimestamp + 3 * 600000,
      },
    ];

    getMountingFunction()(RequestStateHistory, {
      props: {
        stateHistory: dummyStateHistory,
        dataSourcingHistory: dataSourcingHistory,
        isAdmin: true,
      },
    });

    cy.get('[data-test="stateHistoryTable"]').should('exist').and('be.visible');
    const totalEntries = dummyStateHistory.length + dataSourcingHistory.length;
    cy.get('[data-test="lastModifiedDate"]').should('have.length', totalEntries);
    cy.get('[data-test="historyType"]').should('have.length', totalEntries);

    cy.get('[data-test="historyType"]').contains('Request').should('exist');
    cy.get('[data-test="historyType"]').contains('Data Sourcing').should('exist');
    cy.get('.dataland-inline-tag').contains(RequestState.Open).should('exist');
    cy.get('.dataland-inline-tag').contains('DocumentSourcing').should('exist');
  });
});
