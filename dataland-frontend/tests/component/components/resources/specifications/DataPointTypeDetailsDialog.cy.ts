import DataPointTypeDetailsDialog from '@/components/resources/specifications/DataPointTypeDetailsDialog.vue';
import type { DataPointTypeSpecification } from '@clients/specificationservice';
import dataPointDetailsFixture from '@testing/data/specifications/datapoint-details.json';
import dataPointWithConstraintsFixture from '@testing/data/specifications/datapoint-with-constraints.json';

describe('Component tests for DataPointTypeDetailsDialog', () => {
  const mockDetails = dataPointDetailsFixture as DataPointTypeSpecification;
  const mockDetailsWithConstraints = dataPointWithConstraintsFixture as DataPointTypeSpecification;

  describe('Dialog visibility', () => {
    it('Should not display when visible prop is false', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: false,
          dataPointTypeId: null,
          dataPointAliasExport: undefined,
          dataPointDetails: null,
          isLoading: false,
          error: null,
        },
      });

      cy.get('[role="dialog"]').should('not.exist');
    });

    it('Should display when visible prop is true', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_companyName',
          dataPointAliasExport: 'Company Name',
          dataPointDetails: mockDetails,
          isLoading: false,
          error: null,
        },
      });

      cy.get('[role="dialog"]').should('be.visible');
    });

    it('Should close when clicking close button', () => {
      const closeSpy = cy.spy().as('closeSpy');
      const updateVisibleSpy = cy.spy().as('updateVisibleSpy');

      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_companyName',
          dataPointAliasExport: 'Company Name',
          dataPointDetails: mockDetails,
          isLoading: false,
          error: null,
          'onUpdate:visible': updateVisibleSpy,
          'onClose': closeSpy,
        },
      });

      cy.get('[data-test="close-dialog"]').click();
      cy.get('@closeSpy').should('have.been.called');
    });

    it('Should emit update:visible when closing', () => {
      const updateVisibleSpy = cy.spy().as('updateVisibleSpy');

      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_companyName',
          dataPointAliasExport: 'Company Name',
          dataPointDetails: mockDetails,
          isLoading: false,
          error: null,
          'onUpdate:visible': updateVisibleSpy,
        },
      });

      cy.get('[data-test="close-dialog"]').click();
      cy.get('@updateVisibleSpy').should('have.been.calledWith', false);
    });

    it('Should close on ESC key press', () => {
      const updateVisibleSpy = cy.spy().as('updateVisibleSpy');

      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_companyName',
          dataPointAliasExport: 'Company Name',
          dataPointDetails: mockDetails,
          isLoading: false,
          error: null,
          'onUpdate:visible': updateVisibleSpy,
        },
      });

      cy.get('[role="dialog"]').trigger('keydown', { key: 'Escape' });
      cy.wait(100);
      cy.get('@updateVisibleSpy').should('have.been.called');
    });
  });

  describe('Loading states', () => {
    it('Should show spinner while loading', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_companyName',
          dataPointAliasExport: 'Company Name',
          dataPointDetails: null,
          isLoading: true,
          error: null,
        },
      });

      cy.get('.dialog-loading').should('be.visible');
      cy.get('.p-progress-spinner').should('exist');
      cy.get('.dialog-loading').should('contain.text', 'Loading');
    });

    it('Should hide spinner when data loaded', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_companyName',
          dataPointAliasExport: 'Company Name',
          dataPointDetails: mockDetails,
          isLoading: false,
          error: null,
        },
      });

      cy.get('.dialog-loading').should('not.exist');
      cy.get('.p-progress-spinner').should('not.exist');
    });
  });

  describe('Data display', () => {
    it('Should display data point name as dialog header', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_companyName',
          dataPointAliasExport: 'Company Name',
          dataPointDetails: mockDetails,
          isLoading: false,
          error: null,
        },
      });

      cy.get('.p-dialog-header').should('contain.text', 'Company Name');
    });

    it('Should display business definition prominently', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_companyName',
          dataPointAliasExport: 'Company Name',
          dataPointDetails: mockDetails,
          isLoading: false,
          error: null,
        },
      });

      cy.get('.detail-value-prominent').should('contain.text', 'full legal name');
    });

    it('Should show human-readable base type with technical ID', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_companyName',
          dataPointAliasExport: 'Company Name',
          dataPointDetails: mockDetails,
          isLoading: false,
          error: null,
        },
      });

      // Readable type
      cy.get('.type-primary').should('contain.text', 'Text');
      // Technical type
      cy.get('.type-secondary').should('contain.text', 'plainString');
    });

    it('Should display technical details in collapsible accordion', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_companyName',
          dataPointAliasExport: 'Company Name',
          dataPointDetails: mockDetails,
          isLoading: false,
          error: null,
        },
      });

      // Accordion should exist
      cy.get('.technical-accordion').should('exist');
      cy.get('.accordion-header').should('contain.text', 'Technical Details');
    });

    it('Should start with accordion collapsed by default', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_companyName',
          dataPointAliasExport: 'Company Name',
          dataPointDetails: mockDetails,
          isLoading: false,
          error: null,
        },
      });

      // Technical details should not be immediately visible
      cy.get('.p-accordionpanel').should('not.have.class', 'p-accordionpanel-active');
    });

    it('Should expand accordion on click', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_companyName',
          dataPointAliasExport: 'Company Name',
          dataPointDetails: mockDetails,
          isLoading: false,
          error: null,
        },
      });

      cy.get('.accordion-header').click();
      cy.get('.p-accordionpanel').should('have.class', 'p-accordionpanel-active');
    });

    it('Should display constraints as formatted list', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_numberOfEmployees',
          dataPointAliasExport: 'Number of Employees',
          dataPointDetails: mockDetailsWithConstraints,
          isLoading: false,
          error: null,
        },
      });

      cy.get('.accordion-header').click();
      cy.get('.detail-code').should('contain.text', 'Must be a positive integer');
      cy.get('.detail-code').should('contain.text', 'Minimum value: 1');
    });

    it('Should show message when no constraints exist', () => {
      const noConstraints: DataPointTypeSpecification = {
        ...mockDetails,
        constraints: [],
      };

      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'test_id',
          dataPointAliasExport: 'Test',
          dataPointDetails: noConstraints,
          isLoading: false,
          error: null,
        },
      });

      cy.get('.accordion-header').click();
      cy.get('.detail-code').should('contain.text', 'No constraints defined');
    });

    it('Should display frameworks using this data point', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_companyName',
          dataPointAliasExport: 'Company Name',
          dataPointDetails: mockDetails,
          isLoading: false,
          error: null,
        },
      });

      cy.get('.accordion-header').click();
      cy.contains('Used By Frameworks').should('be.visible');
    });

    it('Should handle missing optional fields gracefully', () => {
      const minimalDetails: DataPointTypeSpecification = {
        id: 'test_minimal',
        name: 'Minimal Data Point',
        businessDefinition: 'Test definition',
        dataPointBaseType: { id: 'plainString', ref: 'http://example.com' },
        dataPointType: { id: 'test_minimal', ref: 'http://example.com' },
      };

      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'test_minimal',
          dataPointAliasExport: undefined,
          dataPointDetails: minimalDetails,
          isLoading: false,
          error: null,
        },
      });

      cy.get('[role="dialog"]').should('be.visible');
      cy.get('.detail-value-prominent').should('contain.text', 'Test definition');
    });

    it('Should handle very long business definition', () => {
      const longDefinition: DataPointTypeSpecification = {
        ...mockDetails,
        businessDefinition: 'This is a very long business definition that contains multiple paragraphs and detailed information about the data point. '.repeat(10),
      };

      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'test_id',
          dataPointAliasExport: 'Test',
          dataPointDetails: longDefinition,
          isLoading: false,
          error: null,
        },
      });

      cy.get('.detail-value-prominent').should('be.visible');
      // Should wrap correctly without horizontal scroll
      cy.get('.detail-value-prominent').should('have.css', 'overflow-x', 'visible');
    });
  });

  describe('Error handling', () => {
    it('Should display error message for 404 error', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'nonexistent',
          dataPointAliasExport: 'Test',
          dataPointDetails: null,
          isLoading: false,
          error: 'Data point not found',
        },
      });

      cy.get('.dialog-error').should('be.visible');
      cy.get('.dialog-error').should('contain.text', 'Data point not found');
    });

    it('Should display generic error for 500 error', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'test_id',
          dataPointAliasExport: 'Test',
          dataPointDetails: null,
          isLoading: false,
          error: 'Failed to load details',
        },
      });

      cy.get('.dialog-error').should('be.visible');
      cy.get('.dialog-error').should('contain.text', 'Failed to load details');
    });

    it('Should show retry button when error occurs', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'test_id',
          dataPointAliasExport: 'Test',
          dataPointDetails: null,
          isLoading: false,
          error: 'Network error',
        },
      });

      cy.get('.dialog-error').should('be.visible');
      cy.contains('button', 'Retry').should('be.visible');
    });

    it('Should emit retry event with dataPointTypeId when retry button clicked', () => {
      const retrySpy = cy.spy().as('retrySpy');

      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'test_id',
          dataPointAliasExport: 'Test',
          dataPointDetails: null,
          isLoading: false,
          error: 'Network error',
          'onRetry': retrySpy,
        },
      });

      cy.contains('button', 'Retry').click();
      cy.get('@retrySpy').should('have.been.calledWith', 'test_id');
    });

    it('Should not close dialog when error is shown', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'test_id',
          dataPointAliasExport: 'Test',
          dataPointDetails: null,
          isLoading: false,
          error: 'Error message',
        },
      });

      cy.get('[role="dialog"]').should('be.visible');
      cy.get('.dialog-error').should('be.visible');
    });
  });

  describe('Props handling', () => {
    it('Should show placeholder when dataPointTypeId is null', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: null,
          dataPointAliasExport: undefined,
          dataPointDetails: null,
          isLoading: false,
          error: null,
        },
      });

      cy.get('.dialog-empty').should('be.visible');
      cy.get('.dialog-empty').should('contain.text', 'No data point details available');
    });

    it('Should handle prop changes while dialog open', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_companyName',
          dataPointAliasExport: 'Company Name',
          dataPointDetails: mockDetails,
          isLoading: false,
          error: null,
        },
      }).then(({ wrapper }) => {
        // Update props to show loading
        wrapper.setProps({
          isLoading: true,
          dataPointDetails: null,
        });

        cy.get('.dialog-loading').should('be.visible');
      });
    });
  });

  describe('Accessibility', () => {
    it('Should have aria-modal attribute', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_companyName',
          dataPointAliasExport: 'Company Name',
          dataPointDetails: mockDetails,
          isLoading: false,
          error: null,
        },
      });

      cy.get('[role="dialog"]').should('have.attr', 'aria-modal', 'true');
    });

    it('Should have aria-label', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_companyName',
          dataPointAliasExport: 'Company Name',
          dataPointDetails: mockDetails,
          isLoading: false,
          error: null,
        },
      });

      cy.get('[role="dialog"]').should('have.attr', 'aria-label');
    });

    it('Should use semantic heading for dialog title', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_companyName',
          dataPointAliasExport: 'Company Name',
          dataPointDetails: mockDetails,
          isLoading: false,
          error: null,
        },
      });

      // PrimeVue Dialog header should be properly marked
      cy.get('.p-dialog-header').should('exist');
    });

    it('Should have data-test attribute on close button', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_companyName',
          dataPointAliasExport: 'Company Name',
          dataPointDetails: mockDetails,
          isLoading: false,
          error: null,
        },
      });

      cy.get('[data-test="close-dialog"]').should('exist');
    });

    it('Should use definition list for structured data', () => {
      cy.mountWithPlugins(DataPointTypeDetailsDialog, {
        props: {
          visible: true,
          dataPointTypeId: 'lksg_companyName',
          dataPointAliasExport: 'Company Name',
          dataPointDetails: mockDetails,
          isLoading: false,
          error: null,
        },
      });

      cy.get('dl.details-list').should('exist');
      cy.get('dt.detail-label').should('have.length.at.least', 1);
      cy.get('dd.detail-value').should('have.length.at.least', 1);
    });
  });
});
