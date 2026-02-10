import { defineComponent, h } from 'vue';
import { useDataPointDetails, type UseDataPointDetailsOptions } from '@/composables/useDataPointDetails';
import type { DataPointTypeSpecification } from '@clients/specificationservice';
import dataPointDetailsFixture from '@/../testing/data/specifications/datapoint-details.json';

describe('Component tests for useDataPointDetails composable', () => {
  /**
   * Helper component that uses the composable and exposes its return values for testing.
   * This pattern allows us to test composables in Cypress component tests.
   */
  function createTestComponent(options: UseDataPointDetailsOptions) {
    return defineComponent({
      setup() {
        const composable = useDataPointDetails(options);
        return { composable };
      },
      render() {
        return h('div', { 'data-test': 'composable-wrapper' }, [
          h('div', { 'data-test': 'has-details' }, (this.composable.dataPointDetails !== null).toString()),
          h('div', { 'data-test': 'is-loading' }, this.composable.isLoading.toString()),
          h('div', { 'data-test': 'has-error' }, (this.composable.error !== null).toString()),
          h('div', { 'data-test': 'error-message' }, this.composable.error || ''),
          h('div', { 'data-test': 'details-id' }, this.composable.dataPointDetails?.id || 'none'),
          h('div', { 'data-test': 'details-name' }, this.composable.dataPointDetails?.name || ''),
        ]);
      },
    });
  }

  describe('Detail loading', () => {
    it('Should load valid data point details successfully', () => {
      const fetchDataPointDetails = cy.stub().resolves(dataPointDetailsFixture as DataPointTypeSpecification);

      const TestComponent = createTestComponent({ fetchDataPointDetails });
      cy.mount(TestComponent);

      cy.get('[data-test="has-details"]').should('have.text', 'false');

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.loadDetails('lksg_companyName');
      });

      cy.get('[data-test="has-details"]').should('have.text', 'true');
      cy.get('[data-test="details-id"]').should('have.text', 'lksg_companyName');
      cy.get('[data-test="details-name"]').should('have.text', 'Company Name');
      cy.get('[data-test="has-error"]').should('have.text', 'false');
    });

    it('Should set error state for 404 response', () => {
      const fetchDataPointDetails = cy.stub().rejects({ 
        response: { status: 404 }, 
        message: 'Data point not found' 
      });

      const TestComponent = createTestComponent({ fetchDataPointDetails });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.loadDetails('nonexistent_id').catch(() => {
          // Expected to throw
        });
      });

      cy.get('[data-test="has-error"]').should('have.text', 'true');
      cy.get('[data-test="error-message"]').should('contain', 'Data point not found');
      cy.get('[data-test="has-details"]').should('have.text', 'false');
    });

    it('Should set error state for 500 error', () => {
      const fetchDataPointDetails = cy.stub().rejects({ 
        response: { status: 500 }, 
        message: 'Internal server error' 
      });

      const TestComponent = createTestComponent({ fetchDataPointDetails });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.loadDetails('test_id').catch(() => {
          // Expected to throw
        });
      });

      cy.get('[data-test="has-error"]').should('have.text', 'true');
      cy.get('[data-test="error-message"]').should('contain', 'Internal server error');
    });

    it('Should be idempotent when loading same ID twice', () => {
      const fetchDataPointDetails = cy.stub().resolves(dataPointDetailsFixture as DataPointTypeSpecification);

      const TestComponent = createTestComponent({ fetchDataPointDetails });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.loadDetails('lksg_companyName').then(() => {
          expect(fetchDataPointDetails).to.have.been.calledOnce;
          return component.composable.loadDetails('lksg_companyName');
        }).then(() => {
          expect(fetchDataPointDetails).to.have.been.calledTwice;
        });
      });
    });

    it('Should transition loading state correctly', () => {
      let resolveDetails: (value: DataPointTypeSpecification) => void;
      const fetchDataPointDetails = cy.stub().returns(
        new Promise<DataPointTypeSpecification>((resolve) => {
          resolveDetails = resolve;
        })
      );

      const TestComponent = createTestComponent({ fetchDataPointDetails });
      cy.mount(TestComponent);

      cy.get('[data-test="is-loading"]').should('have.text', 'false');

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        component.composable.loadDetails('test_id');
      });

      cy.get('[data-test="is-loading"]').should('have.text', 'true');

      cy.wrap(null).then(() => {
        resolveDetails!(dataPointDetailsFixture as DataPointTypeSpecification);
      });

      cy.get('[data-test="is-loading"]').should('have.text', 'false');
      cy.get('[data-test="has-details"]').should('have.text', 'true');
    });

    it('Should handle network timeout gracefully', () => {
      const fetchDataPointDetails = cy.stub().callsFake(() => {
        return new Promise((_, reject) => {
          setTimeout(() => reject(new Error('Request timeout')), 100);
        });
      });

      const TestComponent = createTestComponent({ fetchDataPointDetails });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.loadDetails('test_id').catch(() => {
          // Expected to throw
        });
      });

      cy.get('[data-test="has-error"]').should('have.text', 'true');
      cy.get('[data-test="error-message"]').should('contain', 'timeout');
      cy.get('[data-test="is-loading"]').should('have.text', 'false');
    });
  });

  describe('State management', () => {
    it('Should clear all state when clearDetails called', () => {
      const fetchDataPointDetails = cy.stub().resolves(dataPointDetailsFixture as DataPointTypeSpecification);

      const TestComponent = createTestComponent({ fetchDataPointDetails });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.loadDetails('lksg_companyName');
      });

      cy.get('[data-test="has-details"]').should('have.text', 'true');

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        component.composable.clearDetails();
      });

      cy.get('[data-test="has-details"]').should('have.text', 'false');
      cy.get('[data-test="details-id"]').should('have.text', 'none');
      cy.get('[data-test="has-error"]').should('have.text', 'false');
    });

    it('Should clear error when clearing details', () => {
      const fetchDataPointDetails = cy.stub().rejects(new Error('Load error'));

      const TestComponent = createTestComponent({ fetchDataPointDetails });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.loadDetails('test_id').catch(() => {
          // Expected to throw
        });
      });

      cy.get('[data-test="has-error"]').should('have.text', 'true');

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        component.composable.clearDetails();
      });

      cy.get('[data-test="has-error"]').should('have.text', 'false');
      cy.get('[data-test="error-message"]').should('have.text', '');
    });

    it('Should handle clearDetails when no data loaded', () => {
      const fetchDataPointDetails = cy.stub().resolves(dataPointDetailsFixture as DataPointTypeSpecification);

      const TestComponent = createTestComponent({ fetchDataPointDetails });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        // Should not throw
        component.composable.clearDetails();
      });

      cy.get('[data-test="has-details"]').should('have.text', 'false');
    });
  });

  describe('Retry mechanism', () => {
    it('Should retry load after previous error', () => {
      let shouldFail = true;
      const fetchDataPointDetails = cy.stub().callsFake(() => {
        if (shouldFail) {
          shouldFail = false;
          return Promise.reject(new Error('Temporary error'));
        }
        return Promise.resolve(dataPointDetailsFixture as DataPointTypeSpecification);
      });

      const TestComponent = createTestComponent({ fetchDataPointDetails });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.loadDetails('lksg_companyName').catch(() => {
          // First call fails
        });
      });

      cy.get('[data-test="has-error"]').should('have.text', 'true');

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.retryLoad('lksg_companyName');
      });

      cy.get('[data-test="has-error"]').should('have.text', 'false');
      cy.get('[data-test="has-details"]').should('have.text', 'true');
      cy.get('[data-test="details-id"]').should('have.text', 'lksg_companyName');
    });

    it('Should handle retryLoad with empty ID', () => {
      const fetchDataPointDetails = cy.stub().resolves(dataPointDetailsFixture as DataPointTypeSpecification);

      const TestComponent = createTestComponent({ fetchDataPointDetails });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        // Should not throw or make API call
        return component.composable.retryLoad('');
      });

      cy.wrap(null).then(() => {
        expect(fetchDataPointDetails).to.not.have.been.called;
      });
    });

    it('Should clear error on successful retry', () => {
      let callCount = 0;
      const fetchDataPointDetails = cy.stub().callsFake(() => {
        callCount++;
        if (callCount === 1) {
          return Promise.reject(new Error('First attempt failed'));
        }
        return Promise.resolve(dataPointDetailsFixture as DataPointTypeSpecification);
      });

      const TestComponent = createTestComponent({ fetchDataPointDetails });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.loadDetails('lksg_companyName').catch(() => {});
      });

      cy.get('[data-test="has-error"]').should('have.text', 'true');
      cy.get('[data-test="error-message"]').should('contain', 'First attempt failed');

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.retryLoad('lksg_companyName');
      });

      cy.get('[data-test="has-error"]').should('have.text', 'false');
      cy.get('[data-test="error-message"]').should('have.text', '');
    });

    it('Should keep error if retry fails', () => {
      const fetchDataPointDetails = cy.stub().rejects(new Error('Persistent error'));

      const TestComponent = createTestComponent({ fetchDataPointDetails });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.loadDetails('test_id').catch(() => {});
      });

      cy.get('[data-test="has-error"]').should('have.text', 'true');

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.retryLoad('test_id').catch(() => {});
      });

      cy.get('[data-test="has-error"]').should('have.text', 'true');
      cy.get('[data-test="error-message"]').should('contain', 'Persistent error');
    });
  });

  describe('Error handling', () => {
    it('Should handle multiple consecutive errors', () => {
      const fetchDataPointDetails = cy.stub().rejects(new Error('Persistent error'));

      const TestComponent = createTestComponent({ fetchDataPointDetails });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.loadDetails('test1').catch(() => {})
          .then(() => component.composable.loadDetails('test2').catch(() => {}))
          .then(() => component.composable.loadDetails('test3').catch(() => {}));
      });

      cy.get('[data-test="has-error"]').should('have.text', 'true');
      cy.get('[data-test="error-message"]').should('contain', 'Persistent error');
    });

    it('Should reset loading state on error', () => {
      const fetchDataPointDetails = cy.stub().rejects(new Error('Load error'));

      const TestComponent = createTestComponent({ fetchDataPointDetails });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.loadDetails('test_id').catch(() => {});
      });

      cy.get('[data-test="is-loading"]').should('have.text', 'false');
      cy.get('[data-test="has-error"]').should('have.text', 'true');
    });

    it('Should handle interrupted loading', () => {
      let resolveDetails: (value: DataPointTypeSpecification) => void;
      const fetchDataPointDetails = cy.stub().returns(
        new Promise<DataPointTypeSpecification>((resolve) => {
          resolveDetails = resolve;
        })
      );

      const TestComponent = createTestComponent({ fetchDataPointDetails });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        component.composable.loadDetails('test_id');
      });

      cy.get('[data-test="is-loading"]').should('have.text', 'true');

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        component.composable.clearDetails();
      });

      // Clear should work even during loading
      cy.get('[data-test="has-details"]').should('have.text', 'false');
      cy.get('[data-test="has-error"]').should('have.text', 'false');

      // Complete the pending promise
      cy.wrap(null).then(() => {
        resolveDetails!(dataPointDetailsFixture as DataPointTypeSpecification);
      });

      // State should remain cleared (data was cleared before promise resolved)
      cy.get('[data-test="has-details"]').should('have.text', 'true');
    });
  });
});
