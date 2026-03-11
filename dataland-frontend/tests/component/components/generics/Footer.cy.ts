import TheFooter from '@/components/generics/TheFooter.vue';

describe('Component test for the footer', () => {
  it('Check if essential elements are present', () => {
    //@ts-ignore
    cy.mountWithPlugins(TheFooter, {});

    cy.get('footer').should('exist');
    cy.get('.footer__logo').should('exist');

    const currentYear = new Date().getFullYear();
    const expectedCopyrightText = `Copyright © ${currentYear} Dataland`;

    cy.get('.footer__copyright').should('contain.text', expectedCopyrightText);

    const essentialLinks = [
      { href: '/imprint', text: 'IMPRINT' },
      { href: '/dataprivacy', text: 'DATA PRIVACY' },
      { href: '/terms', text: 'LEGAL' },
    ];

    for (const link of essentialLinks) {
      cy.get(`footer a[href='${link.href}']`).should('contain.text', link.text);
    }
  });

  it('Toggles accordion on small screens', () => {
    cy.viewport(375, 667);
    //@ts-ignore
    cy.mountWithPlugins(TheFooter, {});

    cy.get('.footer__column--techhub .footer__toggle-icon').should('contain.text', '+');
    cy.get('.footer__column--techhub').click();
    cy.get('.footer__column--techhub .footer__toggle-icon').should('contain.text', '-');
    cy.get('.footer__column--techhub').click();
    cy.get('.footer__column--techhub .footer__toggle-icon').should('contain.text', '+');
  });
});
