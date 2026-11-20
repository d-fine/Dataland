import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import type Keycloak from 'keycloak-js';
import { defineComponent, h, onMounted, ref, type Ref, type VNode } from 'vue';

const mobileWarningText = 'We are working on making the website display properly on mobile.';

/**
 * Mounts the AuthenticationWrapper with a child that reports every mount, as App.vue does with header and page.
 * @param keycloak the Keycloak mock describing the current user
 * @param isPublicRoute controls the disableAuthenticationWrapper prop, like the route meta in App.vue
 */
function mountAuthenticationWrapper(keycloak: Keycloak, isPublicRoute: Ref<boolean>): void {
  const onContentMounted = cy.spy().as('contentMounted');
  const Content = defineComponent({
    setup(): () => VNode {
      onMounted(onContentMounted);
      return () => h('div', { 'data-test': 'wrapped-content' }, 'Wrapped content');
    },
  });
  const Host = defineComponent({
    setup(): () => VNode {
      return () =>
        h(AuthenticationWrapper, { disableAuthenticationWrapper: isPublicRoute.value }, { default: () => h(Content) });
    },
  });
  cy.mountWithPlugins(Host, { keycloak });
}

/**
 * Creates a Keycloak mock whose login function is stubbed
 * @param authenticated whether the user is logged in
 * @returns the Keycloak mock
 */
function keycloakWithLoginStub(authenticated: boolean): Keycloak {
  const keycloak = minimalKeycloakMock({ authenticated });
  keycloak.login = cy.stub().as('login').resolves();
  return keycloak;
}

describe('Component tests for the AuthenticationWrapper', () => {
  it('Keeps its content mounted when a logged-in user switches between public and protected routes', () => {
    const isPublicRoute = ref(true);
    mountAuthenticationWrapper(keycloakWithLoginStub(true), isPublicRoute);
    cy.get('[data-test="wrapped-content"]').should('be.visible');

    cy.then(() => (isPublicRoute.value = false));
    cy.get('[data-test="wrapped-content"]').should('be.visible');
    cy.then(() => (isPublicRoute.value = true));
    cy.get('[data-test="wrapped-content"]').should('be.visible');

    cy.get('@contentMounted').should('have.been.calledOnce');
    cy.get('@login').should('not.have.been.called');
  });

  it('Shows public content to anonymous users and redirects them to the login once the route is protected', () => {
    const isPublicRoute = ref(true);
    mountAuthenticationWrapper(keycloakWithLoginStub(false), isPublicRoute);
    cy.get('[data-test="wrapped-content"]').should('be.visible');
    cy.get('@login').should('not.have.been.called');

    cy.then(() => (isPublicRoute.value = false));
    cy.contains('Checking Log-In status.').should('be.visible');
    cy.get('[data-test="wrapped-content"]').should('not.exist');
    cy.get('@login').should('have.been.calledOnce');
  });

  it('Shows the mobile warning on protected routes only', () => {
    cy.stub(globalThis.navigator, 'userAgent').value('Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)');
    const isPublicRoute = ref(false);
    mountAuthenticationWrapper(keycloakWithLoginStub(true), isPublicRoute);
    cy.contains(mobileWarningText).should('be.visible');
    cy.get('[data-test="wrapped-content"]').should('not.exist');

    cy.then(() => (isPublicRoute.value = true));
    cy.get('[data-test="wrapped-content"]').should('be.visible');
    cy.contains(mobileWarningText).should('not.exist');
  });
});
