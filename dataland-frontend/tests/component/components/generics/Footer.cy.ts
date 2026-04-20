import TheFooter from '@shared-footer/TheFooter.vue';

describe('Component test for the footer', () => {
  it('Check if essential elements are present', () => {
    //@ts-ignore
    cy.mountWithPlugins(TheFooter, {});

    cy.get('footer').should('exist');

    const currentYear = new Date().getFullYear();
    cy.get('.footer__copyright').should('contain.text', `${currentYear} Dataland`);

    const essentialLinks = [
      { href: '/imprint', text: 'Imprint' },
      { href: '/dataprivacy', text: 'Data Privacy' },
      { href: '/legal', text: 'Legal' },
    ];

    for (const link of essentialLinks) {
      cy.get(`footer a[href='${link.href}']`).should('contain.text', link.text);
    }

    cy.get('img[alt="Dataland"]').should('exist');
    cy.get('img[alt="Werte-Stiftung Company"]').should('exist');

    cy.get('.footer__col-heading').contains('About').should('exist');
    cy.get('.footer__col-heading').contains('Product').should('exist');
    cy.get('.footer__col-heading').contains('Connect and explore').should('exist');
  });

  it('Renders footer columns on desktop', () => {
    //@ts-ignore
    cy.mountWithPlugins(TheFooter, {});

    cy.get('.footer__col').should('have.length.at.least', 4);
  });
});
