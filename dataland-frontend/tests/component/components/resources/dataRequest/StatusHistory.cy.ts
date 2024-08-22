// @ts-nocheck
import StatusHistoryComponent from '@/components/resources/dataRequest/StatusHistory.vue';
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
      creationTimestamp: 1714415046000,
    },
    {
      status: RequestStatus.Resolved,
      creationTimestamp: 1714615046000,
    },
    {
      status: RequestStatus.Closed,
      creationTimestamp: 1714655046000,
    },
  ] as Array<StoredDataRequestStatusObject>;
  it('Check functionality of Status History', function () {
    cy.mountWithPlugins(StatusHistoryComponent, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        statusHistory: dummyStatusHistory,
      },
    });
    cy.get('[data-test="creation_timestamp"]').should('not.be.visible');
    cy.get('[data-test="request_status"]').should('not.be.visible');
    cy.get('[data-test="status_history_toggle"]').should('exist').should('contain.text', 'Show Request Status History');
    cy.get('[data-test="status_history_toggle"]').click();

    cy.get('[data-test="creation_timestamp"]').should('be.visible');
    cy.get('[data-test="creation_timestamp"]').should('have.length', 4);
    cy.get('[data-test="creation_timestamp"]').each(($el, index) => {
      expect($el.text()).to.eq(convertUnixTimeInMsToDateString(dummyStatusHistory[index].creationTimestamp));
    });

    cy.get('[data-test="request_status"]').should('be.visible');
    cy.get('[data-test="request_status"]').should('have.length', 4);
    cy.get('[data-test="request_status"]').each(($el, index) => {
      expect($el.text()).to.eq(expectedStatus[index]);
    });

    cy.get('[data-test="status_history_toggle"]').should('exist').should('contain.text', 'Hide Request Status History');
    cy.get('[data-test="status_history_toggle"]').click();

    cy.get('[data-test="creation_timestamp"]').should('not.be.visible');
    cy.get('[data-test="request_status"]').should('not.be.visible');
  });
});
