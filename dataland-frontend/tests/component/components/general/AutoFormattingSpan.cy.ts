import { getMountingFunction } from '@ct/testUtils/Mount';
import AutoFormattingTextSpan from '@/components/general/AutoFormattingTextSpan.vue';

describe('Component test for AutoFormattingSpan', () => {
  it('Should format text with links', () => {
    getMountingFunction()(AutoFormattingTextSpan, {
      props: {
        text:
          'No evidence found in the analyzed documents. (Scope: Sustainability Report 2023 and all publicly ' +
          'available information on www.lifeatspotify.com, accessed on June 24, 2024) ',
      },
    });
    cy.get('a').should('have.attr', 'href', 'https://www.lifeatspotify.com');
  });
  it('Should not format text without links', () => {
    getMountingFunction()(AutoFormattingTextSpan, {
      props: {
        text: 'No link here, or is there?',
      },
    });
    cy.get('a').should('not.exist');
  });
});
