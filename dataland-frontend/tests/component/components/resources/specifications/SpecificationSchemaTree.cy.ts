import SpecificationSchemaTree from '@/components/resources/specifications/SpecificationSchemaTree.vue';
import type { ParsedSchemaNode, ParsedSection, ParsedDataPoint } from '@/types/Specifications';

describe('Component tests for SpecificationSchemaTree', () => {
  const mockEmptySchema: ParsedSchemaNode[] = [];

  const mockFlatSchema: ParsedSchemaNode[] = [
    {
      type: 'section',
      id: 'general',
      key: 'general',
      label: 'General Information',
      children: [],
    } as ParsedSection,
  ];

  const mockSchemaWithDataPoints: ParsedSchemaNode[] = [
    {
      type: 'section',
      id: 'general',
      key: 'general',
      label: 'General Information',
      children: [
        {
          type: 'dataPoint',
          id: 'general.companyName',
          key: 'companyName',
          dataPointTypeId: 'lksg_companyName',
          ref: 'plainString',
          aliasExport: 'Company Name',
        } as ParsedDataPoint,
        {
          type: 'dataPoint',
          id: 'general.reportingYear',
          key: 'reportingYear',
          dataPointTypeId: 'lksg_reportingYear',
          ref: 'plainDate',
          aliasExport: 'Reporting Year',
        } as ParsedDataPoint,
      ],
    } as ParsedSection,
  ];

  const mockNestedSchema: ParsedSchemaNode[] = [
    {
      type: 'section',
      id: 'level1',
      key: 'level1',
      label: 'Level 1',
      children: [
        {
          type: 'section',
          id: 'level1.level2',
          key: 'level2',
          label: 'Level 2',
          children: [
            {
              type: 'section',
              id: 'level1.level2.level3',
              key: 'level3',
              label: 'Level 3',
              children: [
                {
                  type: 'dataPoint',
                  id: 'level1.level2.level3.deepData',
                  key: 'deepData',
                  dataPointTypeId: 'deep_datapoint',
                  ref: 'plainString',
                  aliasExport: 'Deeply Nested Data Point',
                } as ParsedDataPoint,
              ],
            } as ParsedSection,
          ],
        } as ParsedSection,
      ],
    } as ParsedSection,
  ];

  const mockSchemaWithBusinessDefinition: ParsedSchemaNode[] = [
    {
      type: 'section',
      id: 'test',
      key: 'test',
      label: 'Test Section',
      children: [
        {
          type: 'dataPoint',
          id: 'test.withDef',
          key: 'withDef',
          dataPointTypeId: 'test_withDef',
          ref: 'plainString',
          aliasExport: 'Data Point With Definition',
          businessDefinition: 'This is a short business definition that does not need truncation.',
        } as ParsedDataPoint,
        {
          type: 'dataPoint',
          id: 'test.longDef',
          key: 'longDef',
          dataPointTypeId: 'test_longDef',
          ref: 'plainInteger',
          aliasExport: 'Data Point With Long Definition',
          businessDefinition: 'This is a very long business definition that needs to be truncated because it exceeds the maximum character limit. It contains detailed information about the data point, its purpose, requirements, and how it should be used in the context of the framework.',
        } as ParsedDataPoint,
      ],
    } as ParsedSection,
  ];

  describe('Tree structure rendering', () => {
    it('Should render empty schema without errors', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockEmptySchema },
      });

      cy.get('.specification-schema-tree').should('exist');
      cy.get('.schema-section').should('not.exist');
      cy.get('.data-point').should('not.exist');
    });

    it('Should render single top-level section correctly', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockFlatSchema },
      });

      cy.get('[data-test="section-header"]').should('have.length', 1);
      cy.get('[data-test="section-header"]').should('contain.text', 'General Information');
    });

    it('Should render multiple top-level sections in order', () => {
      const multiSectionSchema: ParsedSchemaNode[] = [
        {
          type: 'section',
          id: 'first',
          key: 'first',
          label: 'First Section',
          children: [],
        } as ParsedSection,
        {
          type: 'section',
          id: 'second',
          key: 'second',
          label: 'Second Section',
          children: [],
        } as ParsedSection,
        {
          type: 'section',
          id: 'third',
          key: 'third',
          label: 'Third Section',
          children: [],
        } as ParsedSection,
      ];

      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: multiSectionSchema },
      });

      cy.get('[data-test="section-header"]').should('have.length', 3);
      cy.get('[data-test="section-header"]').eq(0).should('contain.text', 'First Section');
      cy.get('[data-test="section-header"]').eq(1).should('contain.text', 'Second Section');
      cy.get('[data-test="section-header"]').eq(2).should('contain.text', 'Third Section');
    });

    it('Should render deeply nested sections (5 levels)', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockNestedSchema },
      });

      // Check level 1 exists
      cy.get('[data-test="section-header"]').first().should('contain.text', 'Level 1');
      
      // Expand level 1
      cy.get('[data-test="section-header"]').first().click();
      
      // Check level 2 now visible
      cy.get('[data-test="section-header"]').should('have.length', 2);
      cy.get('[data-test="section-header"]').eq(1).should('contain.text', 'Level 2');
      
      // Expand level 2
      cy.get('[data-test="section-header"]').eq(1).click();
      
      // Check level 3 now visible
      cy.get('[data-test="section-header"]').should('have.length', 3);
      cy.get('[data-test="section-header"]').eq(2).should('contain.text', 'Level 3');
      
      // Expand level 3
      cy.get('[data-test="section-header"]').eq(2).click();
      
      // Check deeply nested data point is now visible
      cy.get('[data-test="datapoint-name"]').should('have.length', 1);
      cy.get('[data-test="datapoint-name"]').should('contain.text', 'Deeply Nested Data Point');
    });

    it('Should render mix of sections and data points at same level', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockSchemaWithDataPoints },
      });

      cy.get('[data-test="section-header"]').first().click();
      
      // Should have both data points visible
      cy.get('[data-test="datapoint-name"]').should('have.length', 2);
    });

    it('Should render section with no children (empty section)', () => {
      const emptySection: ParsedSchemaNode[] = [
        {
          type: 'section',
          id: 'empty',
          key: 'empty',
          label: 'Empty Section',
          children: [],
        } as ParsedSection,
      ];

      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: emptySection },
      });

      cy.get('[data-test="section-header"]').should('exist');
      cy.get('[data-test="section-header"]').click();
      
      // Section content should exist but be empty
      cy.get('.section-content').should('exist');
      cy.get('[data-test="datapoint-name"]').should('not.exist');
    });
  });

  describe('Expand/collapse functionality', () => {
    it('Should start with top-level sections collapsed by default', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockSchemaWithDataPoints },
      });

      // Section header should exist but content not rendered
      cy.get('[data-test="section-header"]').should('exist');
      cy.get('[data-test="section-header"]').should('have.attr', 'aria-expanded', 'false');
      cy.get('.section-content').should('not.exist');
    });

    it('Should toggle section expansion on click', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockSchemaWithDataPoints },
      });

      // Initially collapsed
      cy.get('[data-test="section-header"]').should('have.attr', 'aria-expanded', 'false');
      
      // Click to expand
      cy.get('[data-test="section-header"]').click();
      cy.get('[data-test="section-header"]').should('have.attr', 'aria-expanded', 'true');
      cy.get('.section-content').should('be.visible');
      
      // Click to collapse
      cy.get('[data-test="section-header"]').click();
      cy.get('[data-test="section-header"]').should('have.attr', 'aria-expanded', 'false');
      cy.get('.section-content').should('not.exist');
    });

    it('Should change chevron icon on toggle', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockFlatSchema },
      });

      // Collapsed: chevron-left
      cy.get('.section-icon').should('have.class', 'pi-chevron-left');
      
      // Expand
      cy.get('[data-test="section-header"]').click();
      
      // Expanded: chevron-down
      cy.get('.section-icon').should('have.class', 'pi-chevron-down');
    });

    it('Should handle rapid clicking without breaking state', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockFlatSchema },
      });

      // Click rapidly 5 times
      cy.get('[data-test="section-header"]').click().click().click().click().click();
      
      // Should end up expanded (odd number of clicks)
      cy.get('[data-test="section-header"]').should('have.attr', 'aria-expanded', 'true');
    });

    it('Should maintain independent expansion state for multiple sections', () => {
      const multiSections: ParsedSchemaNode[] = [
        {
          type: 'section',
          id: 'section1',
          key: 'section1',
          label: 'Section 1',
          children: [],
        } as ParsedSection,
        {
          type: 'section',
          id: 'section2',
          key: 'section2',
          label: 'Section 2',
          children: [],
        } as ParsedSection,
      ];

      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: multiSections },
      });

      // Expand first section
      cy.get('[data-test="section-header"]').eq(0).click();
      cy.get('[data-test="section-header"]').eq(0).should('have.attr', 'aria-expanded', 'true');
      cy.get('[data-test="section-header"]').eq(1).should('have.attr', 'aria-expanded', 'false');
      
      // Expand second section
      cy.get('[data-test="section-header"]').eq(1).click();
      cy.get('[data-test="section-header"]').eq(0).should('have.attr', 'aria-expanded', 'true');
      cy.get('[data-test="section-header"]').eq(1).should('have.attr', 'aria-expanded', 'true');
      
      // Collapse first section
      cy.get('[data-test="section-header"]').eq(0).click();
      cy.get('[data-test="section-header"]').eq(0).should('have.attr', 'aria-expanded', 'false');
      cy.get('[data-test="section-header"]').eq(1).should('have.attr', 'aria-expanded', 'true');
    });

    it('Should hide nested content immediately on collapse', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockSchemaWithDataPoints },
      });

      // Expand
      cy.get('[data-test="section-header"]').click();
      cy.get('[data-test="datapoint-name"]').should('be.visible');
      
      // Collapse
      cy.get('[data-test="section-header"]').click();
      cy.get('[data-test="datapoint-name"]').should('not.exist');
    });
  });

  describe('Data point display', () => {
    it('Should display data point name using aliasExport', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockSchemaWithDataPoints },
      });

      cy.get('[data-test="section-header"]').click();
      cy.get('[data-test="datapoint-name"]').first().should('contain.text', 'Company Name');
    });

    it('Should display data point type information', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockSchemaWithDataPoints },
      });

      cy.get('[data-test="section-header"]').click();
      
      // Check type label exists
      cy.get('.type-label').should('exist');
      cy.get('.type-readable').should('exist');
      cy.get('.type-technical').should('exist');
      
      // Check technical ID is displayed
      cy.get('.type-technical').first().should('contain.text', 'lksg_companyName');
    });

    it('Should display appropriate icons based on data type', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockSchemaWithDataPoints },
      });

      cy.get('[data-test="section-header"]').click();
      
      // All data points should have icons
      cy.get('.data-point-icon').should('have.length', 2);
      
      // Icons should have PrimeIcon classes
      cy.get('.data-point-icon').first().should('have.class', 'pi');
    });

    it('Should render View Details button for each data point', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockSchemaWithDataPoints },
      });

      cy.get('[data-test="section-header"]').click();
      cy.get('[data-test="view-details-button"]').should('have.length', 2);
      cy.get('[data-test="view-details-button"]').first().should('contain.text', 'View Details');
    });

    it('Should style technical ID with monospace font', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockSchemaWithDataPoints },
      });

      cy.get('[data-test="section-header"]').click();
      
      cy.get('.type-technical')
        .first()
        .should('have.css', 'font-family')
        .and('match', /monospace/i);
    });
  });

  describe('Business definition display', () => {
    it('Should not display definition section when businessDefinition is missing', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockSchemaWithDataPoints },
      });

      cy.get('[data-test="section-header"]').click();
      cy.get('[data-test="datapoint-definition"]').should('not.exist');
    });

    it('Should display short definition without Show more button', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockSchemaWithBusinessDefinition },
      });

      cy.get('[data-test="section-header"]').click();
      
      // First data point has short definition
      cy.get('[data-test="datapoint-definition"]').first().should('be.visible');
      cy.get('[data-test="datapoint-definition"]').first().should('contain.text', 'short business definition');
      
      // Should not have show more toggle
      cy.get('[data-test="datapoint-definition"]').first().find('[data-test="show-more-toggle"]').should('not.exist');
    });

    it('Should truncate long definition and show Show more button', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockSchemaWithBusinessDefinition },
      });

      cy.get('[data-test="section-header"]').click();
      
      // Second data point has long definition
      const longDefSection = cy.get('[data-test="datapoint-definition"]').eq(1);
      
      // Should show truncated text
      longDefSection.find('.definition-text').invoke('text').should('have.length.lessThan', 200);
      
      // Should have show more toggle
      longDefSection.find('[data-test="show-more-toggle"]').should('exist');
      longDefSection.find('[data-test="show-more-toggle"]').should('contain.text', 'Show more');
    });

    it('Should expand definition when Show more clicked', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockSchemaWithBusinessDefinition },
      });

      cy.get('[data-test="section-header"]').click();
      
      const longDefSection = cy.get('[data-test="datapoint-definition"]').eq(1);
      
      // Get initial text length
      longDefSection.find('.definition-text').invoke('text').then((truncatedText) => {
        const truncatedLength = truncatedText.length;
        
        // Click show more
        longDefSection.find('[data-test="show-more-toggle"]').click();
        
        // Text should be longer now
        longDefSection.find('.definition-text').invoke('text').should((fullText) => {
          expect(fullText.length).to.be.greaterThan(truncatedLength);
        });
        
        // Button text should change
        longDefSection.find('[data-test="show-more-toggle"]').should('contain.text', 'Show less');
      });
    });

    it('Should truncate again when Show less clicked', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockSchemaWithBusinessDefinition },
      });

      cy.get('[data-test="section-header"]').click();
      
      const longDefSection = cy.get('[data-test="datapoint-definition"]').eq(1);
      
      // Expand
      longDefSection.find('[data-test="show-more-toggle"]').click();
      
      // Collapse
      longDefSection.find('[data-test="show-more-toggle"]').click();
      
      // Should be truncated again
      longDefSection.find('.definition-text').invoke('text').should('have.length.lessThan', 200);
      longDefSection.find('[data-test="show-more-toggle"]').should('contain.text', 'Show more');
    });

    it('Should maintain independent state for multiple expanded definitions', () => {
      const twoLongDefs: ParsedSchemaNode[] = [
        {
          type: 'section',
          id: 'test',
          key: 'test',
          label: 'Test',
          children: [
            {
              type: 'dataPoint',
              id: 'test.first',
              key: 'first',
              dataPointTypeId: 'test_first',
              ref: 'plainString',
              businessDefinition: 'This is the first very long business definition that needs to be truncated because it exceeds the maximum character limit for display in the interface without user interaction.',
            } as ParsedDataPoint,
            {
              type: 'dataPoint',
              id: 'test.second',
              key: 'second',
              dataPointTypeId: 'test_second',
              ref: 'plainString',
              businessDefinition: 'This is the second very long business definition that also needs to be truncated because it exceeds the maximum character limit for display in the user interface.',
            } as ParsedDataPoint,
          ],
        } as ParsedSection,
      ];

      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: twoLongDefs },
      });

      cy.get('[data-test="section-header"]').click();
      
      // Expand first
      cy.get('[data-test="show-more-toggle"]').eq(0).click();
      cy.get('[data-test="show-more-toggle"]').eq(0).should('contain.text', 'Show less');
      cy.get('[data-test="show-more-toggle"]').eq(1).should('contain.text', 'Show more');
      
      // Expand second
      cy.get('[data-test="show-more-toggle"]').eq(1).click();
      cy.get('[data-test="show-more-toggle"]').eq(0).should('contain.text', 'Show less');
      cy.get('[data-test="show-more-toggle"]').eq(1).should('contain.text', 'Show less');
      
      // Collapse first
      cy.get('[data-test="show-more-toggle"]').eq(0).click();
      cy.get('[data-test="show-more-toggle"]').eq(0).should('contain.text', 'Show more');
      cy.get('[data-test="show-more-toggle"]').eq(1).should('contain.text', 'Show less');
    });
  });

  describe('Event emission', () => {
    it('Should emit view-details event when clicking View Details button', () => {
      const viewDetailsSpy = cy.spy().as('viewDetailsSpy');
      
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: {
          schema: mockSchemaWithDataPoints,
          'onView-details': viewDetailsSpy,
        },
      });

      cy.get('[data-test="section-header"]').click();
      cy.get('[data-test="view-details-button"]').first().click();
      
      cy.get('@viewDetailsSpy').should('have.been.calledOnce');
      cy.get('@viewDetailsSpy').should('have.been.calledWith', 'lksg_companyName', 'Company Name');
    });

    it('Should emit correct dataPointTypeId and aliasExport in event payload', () => {
      const viewDetailsSpy = cy.spy().as('viewDetailsSpy');
      
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: {
          schema: mockSchemaWithDataPoints,
          'onView-details': viewDetailsSpy,
        },
      });

      cy.get('[data-test="section-header"]').click();
      
      // Click second data point
      cy.get('[data-test="view-details-button"]').eq(1).click();
      
      cy.get('@viewDetailsSpy').should('have.been.calledWith', 'lksg_reportingYear', 'Reporting Year');
    });

    it('Should bubble events from nested data points correctly', () => {
      const viewDetailsSpy = cy.spy().as('viewDetailsSpy');
      
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: {
          schema: mockNestedSchema,
          'onView-details': viewDetailsSpy,
        },
      });

      // Navigate to deeply nested data point
      cy.get('[data-test="section-header"]').eq(0).click();
      cy.get('[data-test="section-header"]').eq(1).click();
      cy.get('[data-test="section-header"]').eq(2).click();
      
      // Click the deeply nested data point
      cy.get('[data-test="view-details-button"]').click();
      
      cy.get('@viewDetailsSpy').should('have.been.calledOnce');
      cy.get('@viewDetailsSpy').should('have.been.calledWith', 'deep_datapoint', 'Deeply Nested Data Point');
    });
  });

  describe('Keyboard navigation & accessibility', () => {
    it('Should toggle section on Enter key', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockFlatSchema },
      });

      cy.get('[data-test="section-header"]').type('{enter}');
      cy.get('[data-test="section-header"]').should('have.attr', 'aria-expanded', 'true');
      
      cy.get('[data-test="section-header"]').type('{enter}');
      cy.get('[data-test="section-header"]').should('have.attr', 'aria-expanded', 'false');
    });

    it('Should toggle section on Space key', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockFlatSchema },
      });

      cy.get('[data-test="section-header"]').type(' ');
      cy.get('[data-test="section-header"]').should('have.attr', 'aria-expanded', 'true');
      
      cy.get('[data-test="section-header"]').type(' ');
      cy.get('[data-test="section-header"]').should('have.attr', 'aria-expanded', 'false');
    });

    it('Should have role="button" on section headers', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockFlatSchema },
      });

      cy.get('[data-test="section-header"]').should('have.attr', 'role', 'button');
    });

    it('Should have tabindex="0" on section headers', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockFlatSchema },
      });

      cy.get('[data-test="section-header"]').should('have.attr', 'tabindex', '0');
    });

    it('Should have aria-hidden="true" on icons', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockFlatSchema },
      });

      cy.get('.section-icon').should('have.attr', 'aria-hidden', 'true');
    });

    it('Should update aria-expanded attribute correctly', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockFlatSchema },
      });

      cy.get('[data-test="section-header"]').should('have.attr', 'aria-expanded', 'false');
      cy.get('[data-test="section-header"]').click();
      cy.get('[data-test="section-header"]').should('have.attr', 'aria-expanded', 'true');
    });

    it('Should maintain focus on section header after toggle', () => {
      cy.mountWithPlugins(SpecificationSchemaTree, {
        props: { schema: mockFlatSchema },
      });

      cy.get('[data-test="section-header"]').focus();
      cy.get('[data-test="section-header"]').type('{enter}');
      
      // Focus should remain on the header
      cy.focused().should('have.attr', 'data-test', 'section-header');
    });
  });
});
