import { defineComponent, h, type VNode } from 'vue';
import { useQueryClient } from '@tanstack/vue-query';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import { useSaveDataPointAwaitingActivation } from '@/utils/useSaveDataPointAwaitingActivation.ts';

const metaInfo = (currentlyActive: boolean): object => ({
  dataPointId: 'new-id',
  dataPointType: 'extendedEnumYesNo',
  companyId: 'c',
  reportingPeriod: '2024',
  uploaderUserId: 'u',
  uploadTime: 1,
  currentlyActive,
  qaStatus: 'Accepted',
});

/**
 * Mounts a minimal component exposing the composable through a button and a pending indicator.
 * @param onDone callback passed to the save function
 * @param onError callback passed to the save function
 * @param onInvalidate spy that is called with the query key of each invalidation
 */
function mountHarness(onDone: unknown, onError: unknown, onInvalidate: (queryKey: unknown) => void): void {
  const Harness = defineComponent({
    setup() {
      const { save, isPending } = useSaveDataPointAwaitingActivation();
      const queryClient = useQueryClient();
      const originalInvalidate = queryClient.invalidateQueries.bind(queryClient);
      queryClient.invalidateQueries = ((filters?: { queryKey?: readonly unknown[] }): Promise<void> => {
        onInvalidate(filters?.queryKey);
        return originalInvalidate(filters);
      }) as typeof queryClient.invalidateQueries;
      return (): VNode =>
        h('div', [
          h(
            'button',
            {
              'data-test': 'save',
              onClick: () =>
                save(
                  {
                    uploadedDataPoint: {
                      dataPoint: '{}',
                      dataPointType: 'extendedEnumYesNo',
                      companyId: 'c',
                      reportingPeriod: '2024',
                    },
                    bypassQa: true,
                  },
                  {
                    onDone: onDone as () => void,
                    onError: onError as () => void,
                  }
                ),
            },
            'save'
          ),
          h('span', { 'data-test': 'pending' }, String(isPending.value)),
        ]);
    },
  });
  //@ts-ignore
  cy.mountWithPlugins(Harness, { keycloak: minimalKeycloakMock({}) });
}

describe('useSaveDataPointAwaitingActivation', () => {
  it('finishes without polling if the posted data point is already active', () => {
    const onDone = cy.stub().as('onDone');
    const onError = cy.stub().as('onError');
    const onInvalidate = cy.stub().as('onInvalidate');
    let metaInfoRequests = 0;
    cy.intercept('POST', '**/data-points?*', { statusCode: 200, body: metaInfo(true) }).as('post');
    cy.intercept('GET', '**/data-points/new-id/metadata', (req) => {
      metaInfoRequests++;
      req.reply(metaInfo(true));
    });

    mountHarness(onDone, onError, onInvalidate);
    cy.get('[data-test="save"]').click();
    cy.wait('@post');

    cy.get('@onDone').should('have.been.calledOnceWith', { activated: true });
    cy.get('@onInvalidate').should('have.been.calledWith', ['frameworkData']);
    cy.get('@onError').should('not.have.been.called');
    cy.get('[data-test="pending"]').should('have.text', 'false');
    cy.then(() => expect(metaInfoRequests).to.equal(0));
  });

  it('stays pending and polls until the data point is active, then reports activated', () => {
    const onDone = cy.stub().as('onDone');
    const onError = cy.stub().as('onError');
    const onInvalidate = cy.stub().as('onInvalidate');
    let metaInfoRequests = 0;
    cy.intercept('POST', '**/data-points?*', { statusCode: 200, body: metaInfo(false) }).as('post');
    cy.intercept('GET', '**/data-points/new-id/metadata', (req) => {
      metaInfoRequests++;
      req.reply(metaInfo(metaInfoRequests >= 3));
    });

    mountHarness(onDone, onError, onInvalidate);
    cy.get('[data-test="save"]').click();
    cy.wait('@post');

    cy.get('[data-test="pending"]').should('have.text', 'true');
    cy.get('@onDone').should('not.have.been.called');
    cy.get('@onDone').should('have.been.calledOnceWith', { activated: true });
    cy.get('@onInvalidate').should('have.been.calledWith', ['frameworkData']);
    cy.get('[data-test="pending"]').should('have.text', 'false');
    cy.get('@onError').should('not.have.been.called');
    cy.then(() => expect(metaInfoRequests).to.be.at.least(3));
  });

  it('reports activated=false and still invalidates framework data if activation times out', () => {
    const onDone = cy.stub().as('onDone');
    const onError = cy.stub().as('onError');
    const onInvalidate = cy.stub().as('onInvalidate');
    cy.intercept('POST', '**/data-points?*', { statusCode: 200, body: metaInfo(false) }).as('post');
    cy.intercept('GET', '**/data-points/new-id/metadata', { statusCode: 200, body: metaInfo(false) });

    cy.clock();
    mountHarness(onDone, onError, onInvalidate);
    cy.get('[data-test="save"]').click();
    cy.wait('@post');

    cy.get('[data-test="pending"]').should('have.text', 'true');
    cy.tick(14000);
    cy.get('@onDone').should('not.have.been.called');
    cy.tick(2000);

    cy.get('@onDone').should('have.been.calledOnceWith', { activated: false });
    cy.get('@onInvalidate').should('have.been.calledWith', ['frameworkData']);
    cy.get('[data-test="pending"]').should('have.text', 'false');
    cy.get('@onError').should('not.have.been.called');
  });

  it('calls onError and does not wait or invalidate if the POST fails', () => {
    const onDone = cy.stub().as('onDone');
    const onError = cy.stub().as('onError');
    const onInvalidate = cy.stub().as('onInvalidate');
    cy.intercept('POST', '**/data-points?*', { statusCode: 500, body: {} }).as('post');

    mountHarness(onDone, onError, onInvalidate);
    cy.get('[data-test="save"]').click();
    cy.wait('@post');

    cy.get('@onError').should('have.been.calledOnce');
    cy.get('@onDone').should('not.have.been.called');
    cy.get('[data-test="pending"]').should('have.text', 'false');
    cy.get('@onInvalidate').should('not.have.been.calledWith', ['frameworkData']);
  });
});
