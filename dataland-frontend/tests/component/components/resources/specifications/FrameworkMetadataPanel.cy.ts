import FrameworkMetadataPanel from '@/components/resources/specifications/FrameworkMetadataPanel.vue';
import type { FrameworkSpecificationWithParsedSchema } from '@/types/Specifications';
import lksgFrameworkFixture from '@/../testing/data/specifications/lksg-framework.json';

describe('Component tests for FrameworkMetadataPanel', () => {
  const mockFramework: FrameworkSpecificationWithParsedSchema = {
    id: lksgFrameworkFixture.id,
    name: lksgFrameworkFixture.name,
    businessDefinition: lksgFrameworkFixture.businessDefinition,
    schemaJson: lksgFrameworkFixture.schema,
    parsedSchema: [],
    framework: {
      id: lksgFrameworkFixture.id,
    },
  };

  const mockFrameworkWithoutDefinition: FrameworkSpecificationWithParsedSchema = {
    id: 'test-framework',
    name: 'Test Framework',
    businessDefinition: '',
    schemaJson: '{}',
    parsedSchema: [],
    framework: {
      id: 'test-framework',
    },
  };

  it('Should display framework name as heading', () => {
    cy.mountWithPlugins(FrameworkMetadataPanel, {
      props: { framework: mockFramework },
    });

    cy.get('[data-test="framework-metadata"]').should('be.visible');
    cy.get('.framework-name').should('have.text', 'LkSG');
  });

  it('Should display business definition', () => {
    cy.mountWithPlugins(FrameworkMetadataPanel, {
      props: { framework: mockFramework },
    });

    cy.get('.metadata-value').should('contain.text', 'German Supply Chain Due Diligence Act');
  });

  it('Should display framework ID', () => {
    cy.mountWithPlugins(FrameworkMetadataPanel, {
      props: { framework: mockFramework },
    });

    cy.get('.framework-id').should('contain.text', 'lksg');
    cy.get('.metadata-value-code').should('contain.text', 'lksg');
  });

  it('Should handle missing business definition gracefully', () => {
    cy.mountWithPlugins(FrameworkMetadataPanel, {
      props: { framework: mockFrameworkWithoutDefinition },
    });

    cy.get('[data-test="framework-metadata"]').should('be.visible');
    cy.get('.framework-name').should('have.text', 'Test Framework');
    // Business definition should be empty or show empty state
    cy.get('.metadata-value').first().should('have.text', '');
  });

  it('Should display all sections with proper semantic markup', () => {
    cy.mountWithPlugins(FrameworkMetadataPanel, {
      props: { framework: mockFramework },
    });

    // Check heading level
    cy.get('h2.framework-name').should('exist');
    
    // Check definition list structure
    cy.get('dl.metadata-list').should('exist');
    cy.get('dt.metadata-label').should('have.length.at.least', 1);
    cy.get('dd.metadata-value').should('have.length.at.least', 1);
  });

  it('Should handle framework with long business definition', () => {
    const longDefinitionFramework: FrameworkSpecificationWithParsedSchema = {
      ...mockFramework,
      businessDefinition: 'This is a very long business definition that spans multiple lines and contains a lot of detailed information about the framework including its purpose, scope, requirements, and regulatory background. It may include references to specific regulations, directives, and compliance requirements that organizations must follow.',
    };

    cy.mountWithPlugins(FrameworkMetadataPanel, {
      props: { framework: longDefinitionFramework },
    });

    cy.get('.metadata-value').first().should('be.visible');
    cy.get('.metadata-value').first().invoke('text').should('have.length.at.least', 100);
  });

  it('Should display framework metadata in correct visual hierarchy', () => {
    cy.mountWithPlugins(FrameworkMetadataPanel, {
      props: { framework: mockFramework },
    });

    // Title should be at the top
    cy.get('.title-section').should('exist');
    cy.get('.title-section .framework-name').should('exist');
    cy.get('.title-section .framework-id').should('exist');
    
    // Metadata list should follow
    cy.get('.metadata-list').should('exist');
  });

  it('Should style framework ID as code element', () => {
    cy.mountWithPlugins(FrameworkMetadataPanel, {
      props: { framework: mockFramework },
    });

    cy.get('.metadata-value-code')
      .should('have.css', 'font-family')
      .and('match', /monospace/i);
  });

  it('Should use PrimeVue Card component', () => {
    cy.mountWithPlugins(FrameworkMetadataPanel, {
      props: { framework: mockFramework },
    });

    // PrimeVue Card renders with specific structure
    cy.get('.framework-metadata-panel').should('exist');
    cy.get('.framework-metadata-panel').should('have.class', 'p-card');
  });

  it('Should have data-test attribute for testing', () => {
    cy.mountWithPlugins(FrameworkMetadataPanel, {
      props: { framework: mockFramework },
    });

    cy.get('[data-test="framework-metadata"]').should('exist');
  });
});
