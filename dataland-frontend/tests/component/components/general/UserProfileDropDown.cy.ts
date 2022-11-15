import UserProfileDropDown from "@/components/general/UserProfileDropDown.vue";
import { mount } from "@vue/test-utils";

describe("Component test for UserProfileDropDown", () => {
  it("Should display a profile picture if the keycloak authenticator provides one", () => {
    const testImagePath = "https://url.doesnotexit/testImage";
    const profilePictureLoadingErrorSpy = cy.spy().as("onProfilePictureLoadingErrorSpy");
    const profilePictureObtainedSpy = cy.spy().as("onProfilePictureObtainedSpy");
    mount(UserProfileDropDown, {
      global: {
        provide: {
          authenticated: true,
          getKeycloakPromise() {
            return Promise.resolve({
              authenticated: true,
              idTokenParsed: {
                picture: testImagePath,
              },
            });
          },
        },
      },
      props: {
        onProfilePictureLoadingError: profilePictureLoadingErrorSpy,
        onProfilePictureObtained: profilePictureObtainedSpy,
      },
    });

    cy.get("@onProfilePictureObtainedSpy").should("have.been.calledWith", testImagePath);

    cy.get("@onProfilePictureLoadingErrorSpy").should("have.been.called");
  });
});
