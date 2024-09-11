import RenderSanitizedMarkdownInput from '@/components/general/RenderSanitizedMarkdownInput.vue';
import { getMountingFunction } from '@ct/testUtils/Mount';
import DOMPurify from 'dompurify';

describe('Component test for RenderSanitizedMarkdownInput', () => {
  it('renders regular Markdown input correctly', () => {
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

  it('handles empty input correctly', () => {
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
  it('handles error during sanitization correctly', () => {
    cy.stub(DOMPurify, 'sanitize').throws(new Error('Sanitization Error'));

    const markdownInput = '## Sample Text';

    getMountingFunction()(RenderSanitizedMarkdownInput, {
      props: {
        text: markdownInput,
      },
    });

    cy.get('div').should('be.empty');

    cy.spy(console, 'error');
    cy.get('div').should('be.empty');
    cy.then(() => {
      expect(console.error).to.be.calledWith('Error processing markdown:', Cypress.sinon.match.any);
    });
  });
});
