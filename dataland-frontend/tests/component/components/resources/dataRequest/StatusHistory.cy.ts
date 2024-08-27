import StatusHistory from '@/components/resources/dataRequest/StatusHistory.vue';
import { AccessStatus, RequestStatus, type StoredDataRequestStatusObject } from '@clients/communitymanager';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { convertUnixTimeInMsToDateString } from '@/utils/DataFormatUtils';
import { getMountingFunction } from '@ct/testUtils/Mount';

describe('Component tests for the Status History', function (): void {
  const expectedStatus = [RequestStatus.Open, RequestStatus.Answered, RequestStatus.Resolved, RequestStatus.Closed];
  const expectedAccessStatus = [AccessStatus.Pending, AccessStatus.Pending, AccessStatus.Granted, AccessStatus.Revoked];
  const dummyStatusHistory = [
    {
      status: RequestStatus.Open,
      creationTimestamp: 1714315046000,
      accessStatus: AccessStatus.Pending,
    },
    {
      status: RequestStatus.Answered,
      creationTimestamp: 1714415046001,
      accessStatus: AccessStatus.Pending,
    },
    {
      status: RequestStatus.Resolved,
      creationTimestamp: 1714615046002,
      accessStatus: AccessStatus.Granted,
    },
    {
      status: RequestStatus.Closed,
      creationTimestamp: 1714655046003,
      accessStatus: AccessStatus.Revoked,
    },
  ] as Array<StoredDataRequestStatusObject>;
  it('Check functionality of Status History', function () {
    getMountingFunction()(StatusHistory, {
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

    cy.get('[data-test="accessStatusEntry"]')
      .should('have.length', dummyStatusHistory.length)
      .each(($el, index) => {
        expect($el.text()).to.eq(expectedAccessStatus[index]);
      });

    cy.get('[data-test="status_history_toggle"]').should('exist').should('contain.text', 'Hide Request Status History');
    cy.get('[data-test="status_history_toggle"]').click();

    cy.get('[data-test="statusHistoryTable"]').should('not.be.visible');
  });
});
