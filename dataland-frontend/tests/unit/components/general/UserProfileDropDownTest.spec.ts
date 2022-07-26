import { mount } from "@vue/test-utils";
import UserProfileDropDown from "@/components/general/UserProfileDropDown.vue";
import { expect } from "@jest/globals";

describe("UserProfileDropDownTest", () => {
  const TestImagePath = "https://url.to/testImage";
  const WrapperComponent = {
    template: '<UserProfileDropDown ref="profileDropdown"/>',
    components: { UserProfileDropDown },
    provide: {
      authenticated: true,
      getKeycloakPromise() {
        return Promise.resolve({
          authenticated: true,
          idTokenParsed: {
            picture: TestImagePath,
          },
        });
      },
    },
  };

  it("Should display a profile picture if the keycloak authenticator provides one", (done) => {
    const wrapper: any = mount(WrapperComponent);
    const profileDropdown = wrapper.vm.$refs.profileDropdown;
    wrapper.vm.$nextTick(() => {
      expect(profileDropdown.$refs["profile-picture"].src).toBe(TestImagePath);
      done();
    });
  });
});
