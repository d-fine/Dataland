import UserProfileDropDown from '@/components/general/UserProfileDropDown.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakUtils';
import router from '@/router';

describe('Component test for UserProfileDropDown', () => {
  it('Should display a profile picture if the keycloak authenticator provides one', () => {
    const testImagePath = 'https://url.doesnotexist/testImage';
    const profilePictureLoadingErrorSpy = cy.spy().as('onProfilePictureLoadingErrorSpy');
    const profilePictureObtainedSpy = cy.spy().as('onProfilePictureObtainedSpy');
    cy.mountWithPlugins(UserProfileDropDown, {
      keycloak: minimalKeycloakMock({
        idTokenParsed: {
          picture: testImagePath,
        },
      }),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        onProfilePictureLoadingError: profilePictureLoadingErrorSpy,
        onProfilePictureObtained: profilePictureObtainedSpy,
      },
    });

    cy.get('@onProfilePictureObtainedSpy').should('have.been.calledWith', testImagePath);
    cy.get('@onProfilePictureLoadingErrorSpy').should('have.been.called');
  });

  const profileDropdownToggleSelector = "div[id='profile-picture-dropdown-toggle']";
  const qaAnchorSelector = "a[id='profile-picture-dropdown-qa-services-anchor']";
  it('Checks QA menu item is visible for the reviewer role', () => {
    const reviewerKeycloakMock = minimalKeycloakMock({
      roles: [KEYCLOAK_ROLE_REVIEWER],
    });
    cy.spy(router, 'push').as('routerPush');

    cy.mountWithPlugins(UserProfileDropDown, {
      keycloak: reviewerKeycloakMock,
      router: router,
    }).then(() => {
      cy.get(profileDropdownToggleSelector).click().get(qaAnchorSelector).should('exist').should('be.visible');
      cy.get(qaAnchorSelector).click();
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
      cy.get(profileDropdownToggleSelector).click().get(qaAnchorSelector).should('not.exist');
    });
  });
});
