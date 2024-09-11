import RenderSanitizedMarkdownInput from '@/components/general/RenderSanitizedMarkdownInput.vue';
import { getMountingFunction } from '@ct/testUtils/Mount';

describe('Component test for RenderSanitizedMarkdownInput', () => {
  it('renders Markdown text correctly', () => {
    const markdownInput = '# Hello World\nThis is **bold** text.' + '\n- List item 1\n- List item 2';

    getMountingFunction()(RenderSanitizedMarkdownInput, {
      props: {
        text: markdownInput,
      },
    });

    cy.get('div').should('contain', 'Hello World');
    cy.get('div').should('contain', 'This is bold text.');
    cy.get('div').should('contain', 'List item 1');
    cy.get('div').should('contain', 'List item 2');
  });

  it('handles empty markdown input correctly', () => {
    getMountingFunction()(RenderSanitizedMarkdownInput, {
      props: {
        text: '',
      },
    });

    cy.get('div').should('be.empty');
  });

  it('handles special characters and sanitizes them', () => {
    const markdownInput = '<script>alert("XSS")</script>';

    getMountingFunction()(RenderSanitizedMarkdownInput, {
      props: {
        text: markdownInput,
      },
    });

    cy.get('div').should('not.contain', 'alert("XSS")');
  });
});
