// @ts-nocheck
import FreeTextDisplayComponent from '@/components/resources/dataTable/cells/FreeTextDisplayComponent.vue';

const shortTestString = 'Short String';

it('Should entirely display a short string without an expand option', () => {
  cy.mountWithPlugins(FreeTextDisplayComponent, {
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    props: {
      content: {
        displayValue: shortTestString,
      },
    },
  });

  cy.get('span[data-test=freetext-full]').contains(shortTestString);
  cy.get('span[data-test=freetext-toggle]').should('not.exist');
});

const longText = 'long'.repeat(100);

it('Should initially show the collapsed string but allow expanding for longer texts', () => {
  cy.mountWithPlugins(FreeTextDisplayComponent, {
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    props: {
      content: {
        displayValue: longText,
      },
    },
  });

  cy.get('span[data-test=freetext-collapsed]').should('exist');

  cy.get('span[data-test=freetext-toggle]').contains('Show more').click();
  cy.get('span[data-test=freetext-full]').should('exist');

  cy.get('span[data-test=freetext-toggle]').contains('Show less').click();
  cy.get('span[data-test=freetext-collapsed]').should('exist');
});
