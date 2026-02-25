import UserProfileDropDown from '@/components/general/UserProfileDropDown.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import router from '@/router';
import { KEYCLOAK_ROLE_JUDGE, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakRoles';

describe('Component test for UserProfileDropDown', () => {
  it('Checks QA menu item is visible for the judge role', () => {
    const reviewerKeycloakMock = minimalKeycloakMock({
      roles: [KEYCLOAK_ROLE_JUDGE],
    });
    cy.spy(router, 'push').as('routerPush');
    cy.mountWithPlugins(UserProfileDropDown, {
      keycloak: reviewerKeycloakMock,
      router: router,
    }).then(() => {
      cy.get('[data-test="user-profile-toggle"]').click();
      cy.contains('.p-menu-item-label', 'QUALITY ASSURANCE').should('be.visible').click();
      cy.get('@routerPush').should('have.been.calledWith', '/qualityassurance');
    });
  });

  it('Checks QA menu item is invisible for a regular user', () => {
    const reviewerKeycloakMock = minimalKeycloakMock({
      roles: [KEYCLOAK_ROLE_USER],
    });
    cy.mountWithPlugins(UserProfileDropDown, {
      keycloak: reviewerKeycloakMock,
    }).then(() => {
      cy.get('[data-test="user-profile-toggle"]').click();
      cy.contains('.p-menu-item-label', 'QUALITY ASSURANCE').should('exist').should('not.be.visible');
    });
  });
});
