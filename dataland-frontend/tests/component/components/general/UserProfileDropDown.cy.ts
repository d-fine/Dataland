import UserProfileDropDown from "@/components/general/UserProfileDropDown.vue";
import { mount } from "@vue/test-utils";
import {
  getInjectedKeycloakObjectsForTest,
  getRequiredPlugins,
} from "../../TestUtils";

describe("Component test for UserProfileDropDown", () => {
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
    const wrapper: any = mount(WrapperComponent, {
      global: {
        plugins: getRequiredPlugins(),
        provide: getInjectedKeycloakObjectsForTest(),
      },
    });
    const profileDropdown = wrapper.vm.$refs.profileDropdown;
    wrapper.vm.$nextTick(() => {
      expect(profileDropdown.$refs["profile-picture"].src).to.be(TestImagePath);
      done();
    });
  });
});
