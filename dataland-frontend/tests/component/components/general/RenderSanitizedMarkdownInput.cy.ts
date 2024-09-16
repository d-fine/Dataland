import RenderSanitizedMarkdownInput from '@/components/general/RenderSanitizedMarkdownInput.vue';
import { getMountingFunction } from '@ct/testUtils/Mount';

describe('Component test for RenderSanitizedMarkdownInput', () => {
  it('renders regular Markdown input correctly', () => {
    const markdownInput =
      '# Hello World\nThis is **bold** text.' +
      '\n- List item 1' +
      '\n- [Dataland](https://dataland.com) \n- https://test.dataland.com';

    getMountingFunction()(RenderSanitizedMarkdownInput, {
      props: {
        text: markdownInput,
      },
    });
    cy.get('div').should('not.contain', '#');
    cy.get('div').should('not.contain', '*');
    cy.get('div').should('not.contain', '-');
    cy.get('div').should('contain', 'Hello World');
    cy.get('div').should('contain', 'This is bold text.');
    cy.get('div').should('contain', 'List item 1');
    cy.get('a').eq(0).should('contain', 'Dataland').should('be.visible').should('not.be.disabled');
    cy.get('a').eq(0).should('not.contain', 'https://dataland.com');
    cy.get('a').eq(1).should('contain', 'https://test.dataland.com').should('be.visible').should('not.be.disabled');
  });

  it('handles special characters and sanitizes them', () => {
    const dangerousInputs = ['<script>alert("XSS")</script>', '[Click me](javascript:alert("XSS"))'];
    for (const dangerousInput of dangerousInputs) {
      getMountingFunction()(RenderSanitizedMarkdownInput, {
        props: {
          text: dangerousInput,
        },
      });
      cy.get('a').should('not.exist');
    }
  });
});
