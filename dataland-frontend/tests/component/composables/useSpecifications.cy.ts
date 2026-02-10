import { defineComponent, h } from 'vue';
import { useSpecifications, type UseSpecificationsOptions } from '@/composables/useSpecifications';
import type { SimpleFrameworkSpecification, FrameworkSpecification, DataPointTypeSpecification } from '@clients/specificationservice';
import frameworkListFixture from '@/../testing/data/specifications/framework-list.json';
import lksgFrameworkFixture from '@/../testing/data/specifications/lksg-framework.json';
import emptyFrameworkFixture from '@/../testing/data/specifications/empty-framework.json';
import nestedSchemaFixture from '@/../testing/data/specifications/nested-schema.json';

describe('Component tests for useSpecifications composable', () => {
  /**
   * Helper component that uses the composable and exposes its return values for testing.
   * This pattern allows us to test composables in Cypress component tests.
   */
  function createTestComponent(options: UseSpecificationsOptions) {
    return defineComponent({
      setup() {
        const composable = useSpecifications(options);
        return { composable };
      },
      render() {
        return h('div', { 'data-test': 'composable-wrapper' }, [
          h('div', { 'data-test': 'frameworks-count' }, this.composable.frameworks.length.toString()),
          h('div', { 'data-test': 'is-loading-frameworks' }, this.composable.isLoadingFrameworks.toString()),
          h('div', { 'data-test': 'is-loading-specification' }, this.composable.isLoadingSpecification.toString()),
          h('div', { 'data-test': 'is-loading-datapoint-details' }, this.composable.isLoadingDataPointDetails.toString()),
          h('div', { 'data-test': 'has-error' }, (this.composable.error !== null).toString()),
          h('div', { 'data-test': 'error-message' }, this.composable.error || ''),
          h('div', { 'data-test': 'selected-framework-id' }, this.composable.selectedFramework?.id || 'none'),
          h('div', { 'data-test': 'parsed-schema-count' }, this.composable.selectedFramework?.parsedSchema.length.toString() || '0'),
        ]);
      },
    });
  }

  describe('Framework loading', () => {
    it('Should load framework list successfully', () => {
      const fetchFrameworks = cy.stub().resolves(frameworkListFixture as SimpleFrameworkSpecification[]);
      const fetchSpecification = cy.stub().resolves({} as FrameworkSpecification);

      const TestComponent = createTestComponent({ fetchFrameworks, fetchSpecification });
      cy.mount(TestComponent);

      cy.get('[data-test="frameworks-count"]').should('have.text', '0');
      cy.get('[data-test="is-loading-frameworks"]').should('have.text', 'false');

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.loadFrameworks();
      });

      cy.get('[data-test="is-loading-frameworks"]').should('have.text', 'false');
      cy.get('[data-test="frameworks-count"]').should('have.text', '3');
      cy.get('[data-test="has-error"]').should('have.text', 'false');
    });

    it('Should return empty array for empty framework list', () => {
      const fetchFrameworks = cy.stub().resolves([]);
      const fetchSpecification = cy.stub().resolves({} as FrameworkSpecification);

      const TestComponent = createTestComponent({ fetchFrameworks, fetchSpecification });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.loadFrameworks();
      });

      cy.get('[data-test="frameworks-count"]').should('have.text', '0');
      cy.get('[data-test="has-error"]').should('have.text', 'false');
    });

    it('Should sort frameworks alphabetically by name', () => {
      const unsortedFrameworks: SimpleFrameworkSpecification[] = [
        { id: 'sfdr', name: 'SFDR', businessDefinition: 'SFDR desc' },
        { id: 'eutaxonomy', name: 'EU Taxonomy for non-financial companies', businessDefinition: 'EU Taxonomy desc' },
        { id: 'lksg', name: 'LkSG', businessDefinition: 'LkSG desc' },
      ];
      const fetchFrameworks = cy.stub().resolves(unsortedFrameworks);
      const fetchSpecification = cy.stub().resolves({} as FrameworkSpecification);

      const TestComponent = createTestComponent({ fetchFrameworks, fetchSpecification });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.loadFrameworks().then(() => {
          expect(component.composable.frameworks[0].name).to.equal('EU Taxonomy for non-financial companies');
          expect(component.composable.frameworks[1].name).to.equal('LkSG');
          expect(component.composable.frameworks[2].name).to.equal('SFDR');
        });
      });
    });

    it('Should handle API error during framework loading', () => {
      const fetchFrameworks = cy.stub().rejects(new Error('Network error'));
      const fetchSpecification = cy.stub().resolves({} as FrameworkSpecification);

      const TestComponent = createTestComponent({ fetchFrameworks, fetchSpecification });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.loadFrameworks().catch(() => {
          // Expected to throw
        });
      });

      cy.get('[data-test="has-error"]').should('have.text', 'true');
      cy.get('[data-test="error-message"]').should('contain', 'Network error');
      cy.get('[data-test="is-loading-frameworks"]').should('have.text', 'false');
    });

    it('Should handle network timeout gracefully', () => {
      const fetchFrameworks = cy.stub().callsFake(() => {
        return new Promise((_, reject) => {
          setTimeout(() => reject(new Error('Request timeout')), 100);
        });
      });
      const fetchSpecification = cy.stub().resolves({} as FrameworkSpecification);

      const TestComponent = createTestComponent({ fetchFrameworks, fetchSpecification });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.loadFrameworks().catch(() => {
          // Expected to throw
        });
      });

      cy.get('[data-test="has-error"]').should('have.text', 'true');
      cy.get('[data-test="error-message"]').should('contain', 'timeout');
    });

    it('Should transition loading state correctly', () => {
      let resolveFrameworks: (value: SimpleFrameworkSpecification[]) => void;
      const fetchFrameworks = cy.stub().returns(
        new Promise((resolve) => {
          resolveFrameworks = resolve;
        })
      );
      const fetchSpecification = cy.stub().resolves({} as FrameworkSpecification);

      const TestComponent = createTestComponent({ fetchFrameworks, fetchSpecification });
      cy.mount(TestComponent);

      cy.get('[data-test="is-loading-frameworks"]').should('have.text', 'false');

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        component.composable.loadFrameworks();
      });

      cy.get('[data-test="is-loading-frameworks"]').should('have.text', 'true');

      cy.wrap(null).then(() => {
        resolveFrameworks!(frameworkListFixture as SimpleFrameworkSpecification[]);
      });

      cy.get('[data-test="is-loading-frameworks"]').should('have.text', 'false');
      cy.get('[data-test="frameworks-count"]').should('have.text', '3');
    });
  });

  describe('Framework selection', () => {
    it('Should load valid framework specification by ID', () => {
      const fetchFrameworks = cy.stub().resolves(frameworkListFixture as SimpleFrameworkSpecification[]);
      const fetchSpecification = cy.stub().resolves(lksgFrameworkFixture as FrameworkSpecification);

      const TestComponent = createTestComponent({ fetchFrameworks, fetchSpecification });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.selectFramework('lksg');
      });

      cy.get('[data-test="selected-framework-id"]').should('have.text', 'lksg');
      cy.get('[data-test="parsed-schema-count"]').should('not.equal', '0');
      cy.get('[data-test="has-error"]').should('have.text', 'false');
    });

    it('Should set error state for 404 response', () => {
      const fetchFrameworks = cy.stub().resolves(frameworkListFixture as SimpleFrameworkSpecification[]);
      const fetchSpecification = cy.stub().rejects({ response: { status: 404 }, message: 'Framework not found' });

      const TestComponent = createTestComponent({ fetchFrameworks, fetchSpecification });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.selectFramework('nonexistent').catch(() => {
          // Expected to throw
        });
      });

      cy.get('[data-test="has-error"]').should('have.text', 'true');
      cy.get('[data-test="selected-framework-id"]').should('have.text', 'none');
    });

    it('Should set error state for 500 error', () => {
      const fetchFrameworks = cy.stub().resolves(frameworkListFixture as SimpleFrameworkSpecification[]);
      const fetchSpecification = cy.stub().rejects({ response: { status: 500 }, message: 'Internal server error' });

      const TestComponent = createTestComponent({ fetchFrameworks, fetchSpecification });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.selectFramework('lksg').catch(() => {
          // Expected to throw
        });
      });

      cy.get('[data-test="has-error"]').should('have.text', 'true');
      cy.get('[data-test="error-message"]').should('contain', 'Internal server error');
    });

    it('Should be idempotent when selecting same framework twice', () => {
      const fetchFrameworks = cy.stub().resolves(frameworkListFixture as SimpleFrameworkSpecification[]);
      const fetchSpecification = cy.stub().resolves(lksgFrameworkFixture as FrameworkSpecification);

      const TestComponent = createTestComponent({ fetchFrameworks, fetchSpecification });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.selectFramework('lksg').then(() => {
          expect(fetchSpecification).to.have.been.calledOnce;
          return component.composable.selectFramework('lksg');
        }).then(() => {
          expect(fetchSpecification).to.have.been.calledTwice;
        });
      });
    });
  });

  describe('Schema parsing', () => {
    it('Should parse empty schema JSON correctly', () => {
      const fetchFrameworks = cy.stub().resolves(frameworkListFixture as SimpleFrameworkSpecification[]);
      const fetchSpecification = cy.stub().resolves(emptyFrameworkFixture as FrameworkSpecification);

      const TestComponent = createTestComponent({ fetchFrameworks, fetchSpecification });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.selectFramework('empty-test');
      });

      cy.get('[data-test="parsed-schema-count"]').should('have.text', '0');
      cy.get('[data-test="has-error"]').should('have.text', 'false');
    });

    it('Should parse deeply nested schema structure', () => {
      const fetchFrameworks = cy.stub().resolves(frameworkListFixture as SimpleFrameworkSpecification[]);
      const fetchSpecification = cy.stub().resolves(nestedSchemaFixture as FrameworkSpecification);

      const TestComponent = createTestComponent({ fetchFrameworks, fetchSpecification });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.selectFramework('nested-test').then(() => {
          expect(component.composable.selectedFramework).to.not.be.null;
          expect(component.composable.selectedFramework.parsedSchema.length).to.be.greaterThan(0);
          
          // Verify nested structure was parsed (level1 should exist)
          const rootNodes = component.composable.selectedFramework.parsedSchema;
          expect(rootNodes[0].type).to.equal('section');
          expect(rootNodes[0].key).to.equal('level1');
        });
      });
    });

    it('Should extract aliasExport for data points when present', () => {
      const fetchFrameworks = cy.stub().resolves(frameworkListFixture as SimpleFrameworkSpecification[]);
      const fetchSpecification = cy.stub().resolves(lksgFrameworkFixture as FrameworkSpecification);

      const TestComponent = createTestComponent({ fetchFrameworks, fetchSpecification });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.selectFramework('lksg').then(() => {
          const schema = component.composable.selectedFramework.parsedSchema;
          
          // Find a data point (traverse schema)
          function findDataPoint(nodes: any[]): any {
            for (const node of nodes) {
              if (node.type === 'dataPoint' && node.aliasExport) {
                return node;
              }
              if (node.type === 'section' && node.children) {
                const found = findDataPoint(node.children);
                if (found) return found;
              }
            }
            return null;
          }
          
          const dataPoint = findDataPoint(schema);
          expect(dataPoint).to.not.be.null;
          expect(dataPoint.aliasExport).to.be.a('string');
        });
      });
    });

    it('Should handle malformed JSON gracefully', () => {
      const fetchFrameworks = cy.stub().resolves(frameworkListFixture as SimpleFrameworkSpecification[]);
      const malformedFramework: FrameworkSpecification = {
        id: 'malformed',
        name: 'Malformed',
        businessDefinition: 'Test',
        schema: 'not valid JSON {{{',
      };
      const fetchSpecification = cy.stub().resolves(malformedFramework);

      const TestComponent = createTestComponent({ fetchFrameworks, fetchSpecification });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.selectFramework('malformed');
      });

      cy.get('[data-test="has-error"]').should('have.text', 'true');
      cy.get('[data-test="error-message"]').should('contain', 'parse');
    });
  });

  describe('Batch data point loading', () => {
    it('Should not make extra API calls when batch loading disabled', () => {
      const fetchFrameworks = cy.stub().resolves(frameworkListFixture as SimpleFrameworkSpecification[]);
      const fetchSpecification = cy.stub().resolves(lksgFrameworkFixture as FrameworkSpecification);
      const fetchDataPointDetails = cy.stub().resolves({} as DataPointTypeSpecification);

      const TestComponent = createTestComponent({
        fetchFrameworks,
        fetchSpecification,
        fetchDataPointDetails,
        enableBatchDataPointLoading: false,
      });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.selectFramework('lksg');
      });

      cy.wrap(null).then(() => {
        expect(fetchDataPointDetails).to.not.have.been.called;
      });
    });

    it('Should make batch requests when enabled with data points', () => {
      const fetchFrameworks = cy.stub().resolves(frameworkListFixture as SimpleFrameworkSpecification[]);
      const fetchSpecification = cy.stub().resolves(lksgFrameworkFixture as FrameworkSpecification);
      const fetchDataPointDetails = cy.stub().resolves({
        id: 'test',
        name: 'Test',
        businessDefinition: 'Test definition',
        dataPointBaseType: { id: 'plainString' },
      } as DataPointTypeSpecification);

      const TestComponent = createTestComponent({
        fetchFrameworks,
        fetchSpecification,
        fetchDataPointDetails,
        enableBatchDataPointLoading: true,
      });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.selectFramework('lksg');
      });

      cy.wrap(null).then(() => {
        expect(fetchDataPointDetails).to.have.been.called;
      });
    });

    it('Should handle partial batch failures gracefully', () => {
      const fetchFrameworks = cy.stub().resolves(frameworkListFixture as SimpleFrameworkSpecification[]);
      const fetchSpecification = cy.stub().resolves(lksgFrameworkFixture as FrameworkSpecification);
      let callCount = 0;
      const fetchDataPointDetails = cy.stub().callsFake(() => {
        callCount++;
        if (callCount % 2 === 0) {
          return Promise.reject(new Error('Failed to load'));
        }
        return Promise.resolve({
          id: 'test',
          name: 'Test',
          businessDefinition: 'Test definition',
          dataPointBaseType: { id: 'plainString' },
        } as DataPointTypeSpecification);
      });

      const TestComponent = createTestComponent({
        fetchFrameworks,
        fetchSpecification,
        fetchDataPointDetails,
        enableBatchDataPointLoading: true,
      });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.selectFramework('lksg');
      });

      // Should complete without throwing error
      cy.get('[data-test="selected-framework-id"]').should('have.text', 'lksg');
      cy.get('[data-test="is-loading-datapoint-details"]').should('have.text', 'false');
    });

    it('Should track batch loading state', () => {
      const fetchFrameworks = cy.stub().resolves(frameworkListFixture as SimpleFrameworkSpecification[]);
      const fetchSpecification = cy.stub().resolves(lksgFrameworkFixture as FrameworkSpecification);
      
      let resolveDataPoint: (value: DataPointTypeSpecification) => void;
      const fetchDataPointDetails = cy.stub().returns(
        new Promise<DataPointTypeSpecification>((resolve) => {
          resolveDataPoint = resolve;
        })
      );

      const TestComponent = createTestComponent({
        fetchFrameworks,
        fetchSpecification,
        fetchDataPointDetails,
        enableBatchDataPointLoading: true,
      });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        component.composable.selectFramework('lksg');
      });

      cy.get('[data-test="is-loading-datapoint-details"]').should('have.text', 'true');

      cy.wrap(null).then(() => {
        resolveDataPoint!({
          id: 'test',
          name: 'Test',
          businessDefinition: 'Test',
          dataPointBaseType: { id: 'plainString' },
        } as DataPointTypeSpecification);
      });

      cy.get('[data-test="is-loading-datapoint-details"]').should('have.text', 'false');
    });
  });

  describe('Error recovery', () => {
    it('Should clear error state on successful retry', () => {
      const fetchFrameworks = cy.stub().resolves(frameworkListFixture as SimpleFrameworkSpecification[]);
      let shouldFail = true;
      const fetchSpecification = cy.stub().callsFake(() => {
        if (shouldFail) {
          shouldFail = false;
          return Promise.reject(new Error('Temporary error'));
        }
        return Promise.resolve(lksgFrameworkFixture as FrameworkSpecification);
      });

      const TestComponent = createTestComponent({ fetchFrameworks, fetchSpecification });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.selectFramework('lksg').catch(() => {
          // First call fails
        });
      });

      cy.get('[data-test="has-error"]').should('have.text', 'true');

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.selectFramework('lksg');
      });

      cy.get('[data-test="has-error"]').should('have.text', 'false');
      cy.get('[data-test="selected-framework-id"]').should('have.text', 'lksg');
    });

    it('Should handle multiple consecutive errors', () => {
      const fetchFrameworks = cy.stub().resolves(frameworkListFixture as SimpleFrameworkSpecification[]);
      const fetchSpecification = cy.stub().rejects(new Error('Persistent error'));

      const TestComponent = createTestComponent({ fetchFrameworks, fetchSpecification });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.selectFramework('lksg').catch(() => {})
          .then(() => component.composable.selectFramework('lksg').catch(() => {}))
          .then(() => component.composable.selectFramework('lksg').catch(() => {}));
      });

      cy.get('[data-test="has-error"]').should('have.text', 'true');
      cy.get('[data-test="error-message"]').should('contain', 'Persistent error');
    });

    it('Should reset loading states correctly on error', () => {
      const fetchFrameworks = cy.stub().rejects(new Error('Load error'));
      const fetchSpecification = cy.stub().resolves({} as FrameworkSpecification);

      const TestComponent = createTestComponent({ fetchFrameworks, fetchSpecification });
      cy.mount(TestComponent);

      cy.wrap(null).then(() => {
        const component = Cypress.vueWrapper.vm as any;
        return component.composable.loadFrameworks().catch(() => {
          // Expected to throw
        });
      });

      cy.get('[data-test="is-loading-frameworks"]').should('have.text', 'false');
      cy.get('[data-test="has-error"]').should('have.text', 'true');
    });
  });
});
