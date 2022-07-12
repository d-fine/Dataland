import { mount } from "@vue/test-utils";
import UserProfileDropDown from "@/components/general/UserProfileDropDown.vue";
import { expect } from "@jest/globals";

describe("UserProfileDropDownTest", () => {
  const WrapperComponent = {
    template: '<UserProfileDropDown ref="pd"/>',
    components: { UserProfileDropDown },
    provide: {
      authenticated: true,
      getKeycloakInitPromise() {
        return Promise.resolve({
          authenticated: true,
          idTokenParsed: {
            picture: "http://localhost/testimg",
          },
        });
      },
    },
  };

  it("Should display a profile picture if the keycloak authenticator provides one", (done) => {
    const wrapper = mount(WrapperComponent);
    const propic = wrapper.vm.$refs.pd;
    wrapper.vm.$nextTick(() => {
      expect(propic.$refs["profile-picture"].src).toBe("http://localhost/testimg");
      done();
    });
  });
});
