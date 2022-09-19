import UserProfileDropDown from "@/components/general/UserProfileDropDown.vue";
import { mount } from "@vue/test-utils";

describe("Component test for UserProfileDropDown", () => {
  const testImagePath = "https://url.to/testImage";

  it("Should display a profile picture if the keycloak authenticator provides one", (done) => {
    const wrapper: any = mount(UserProfileDropDown, {
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
    });
    wrapper.vm.$nextTick(() => {
      expect(wrapper.vm.$refs["profile-picture"].src).to.be.equal(testImagePath);
      done();
    });
  });
});
