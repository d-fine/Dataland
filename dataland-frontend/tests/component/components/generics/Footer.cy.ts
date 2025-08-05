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

    essentialLinks.forEach((link) => {
      cy.get(`footer a[href='${link.href}']`).should('contain.text', link.text);
    });
  });
});
