import StatusHistory from '@/components/resources/dataRequest/StatusHistory.vue';
import { RequestStatus, type StoredDataRequestStatusObject } from '@clients/communitymanager';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';

describe('Component tests for the Status History', function (): void {
  const expectedStatus = ['Open', 'Answered', 'Resolved', 'Closed'];
  const dummyStatusHistory = [
    {
      status: RequestStatus.Open,
      creationTimestamp: 1714315046000,
    },
    {
      status: RequestStatus.Answered,
      creationTimestamp: 1714415046001,
    },
    {
      status: RequestStatus.Resolved,
      creationTimestamp: 1714615046002,
    },
    {
      status: RequestStatus.Closed,
      creationTimestamp: 1714655046003,
    },
  ] as Array<StoredDataRequestStatusObject>;
  it('Check functionality of Status History', function () {
    // TODO after DALA-4606 has been merged to main, and main into this feature branch, this ts-ignore can be removed
    // @ts-ignore
    cy.mountWithPlugins(StatusHistory, {
      keycloak: minimalKeycloakMock({}),
      props: {
        statusHistory: dummyStatusHistory,
      },
    });
    cy.get('[data-test="statusHistoryTable"]').should('not.be.visible');
    cy.get('[data-test="status_history_toggle"]')
      .should('exist')
      .should('contain.text', 'Show Request Status History')
      .click();

    cy.get('[data-test="creationTimestampEntry"]')
      .should('have.length', dummyStatusHistory.length)
      .each(($el, index) => {
        expect($el.text()).to.eq(convertUnixTimeInMsToDateString(dummyStatusHistory[index].creationTimestamp));
      });

    cy.get('[data-test="requestStatusEntry"]')
      .should('have.length', dummyStatusHistory.length)
      .each(($el, index) => {
        expect($el.text()).to.eq(expectedStatus[index]);
      });

    cy.get('[data-test="status_history_toggle"]').should('exist').should('contain.text', 'Hide Request Status History');
    cy.get('[data-test="status_history_toggle"]').click();

    cy.get('[data-test="statusHistoryTable"]').should('not.be.visible');
  });
});
