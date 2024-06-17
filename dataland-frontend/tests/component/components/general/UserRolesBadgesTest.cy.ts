import UserRolesBadges from '@/components/resources/apiKey/UserRolesBadges.vue';
import { mount } from 'cypress/vue';
import {
  KEYCLOAK_ROLE_ADMIN,
  KEYCLOAK_ROLE_REVIEWER,
  KEYCLOAK_ROLE_UPLOADER,
  KEYCLOAK_ROLE_USER,
} from '@/utils/KeycloakUtils';

describe('Component test for UserRolesBadges', () => {
  it('Should display the user roles for an uploader (READ/WRITE)', () => {
    mount(UserRolesBadges, {
      props: {
        userRoles: [KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_UPLOADER],
      },
    });
    cy.get('[data-test=userRoleUser]').should('have.text', 'READ');
    cy.get('[data-test=userRoleUploader]').should('have.text', 'WRITE');
    cy.get('[data-test=userRoleAdmin]').should('not.exist');
    cy.get('[data-test=userRoleReviewer]').should('not.exist');
  });

  it('Should display the user roles for a user (READ)', () => {
    mount(UserRolesBadges, {
      props: {
        userRoles: [KEYCLOAK_ROLE_USER],
      },
    });
    cy.get('[data-test=userRoleUser]').should('have.text', 'READ');
    cy.get('[data-test=userRoleUploader]').should('not.exist');
    cy.get('[data-test=userRoleAdmin]').should('not.exist');
    cy.get('[data-test=userRoleReviewer]').should('not.exist');
  });

  it('Should display the user roles for an admin (READ/WRITE/ADMIN/ROLE_REVIEWER)', () => {
    mount(UserRolesBadges, {
      props: {
        userRoles: [KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_UPLOADER, KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_ADMIN],
      },
    });
    cy.get('[data-test=userRoleUser]').should('have.text', 'READ');
    cy.get('[data-test=userRoleUploader]').should('have.text', 'WRITE');
    cy.get('[data-test=userRoleAdmin]').should('have.text', 'ADMIN');
    cy.get('[data-test=userRoleReviewer]').should('have.text', 'REVIEWER');
  });
});
