import RequestStateHistory from '@/components/resources/dataRequest/RequestStateHistory.vue';
import { RequestState, type StoredRequest } from '@clients/datasourcingservice';
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
  it('renders correct state history table', function () {
    getMountingFunction()(RequestStateHistory, {
      props: {
        stateHistory: dummyStateHistory,
      },
    });

    cy.get('[data-test="stateHistoryTable"]').should('exist').and('be.visible');
    cy.get('[data-test="lastModifiedDate"]').should('have.length', dummyStateHistory.length);
    for (const [idx, entry] of dummyStateHistory.entries()) {
      cy.get('[data-test="lastModifiedDate"]')
        .eq(idx)
        .should('contain.text', convertUnixTimeInMsToDateString(entry.lastModifiedDate));
      cy.get('.dataland-inline-tag').eq(idx).should('contain.text', entry.state);
      const expectedComment = entry.adminComment || '—';
      cy.get('[data-test="adminComment"]').eq(idx).should('contain.text', expectedComment);
    }
  });
});
