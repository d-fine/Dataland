import UserProfileDropDown from "@/components/general/UserProfileDropDown.vue";
import { mount, VueWrapper } from "@vue/test-utils";

describe("Component test for UserProfileDropDown", () => {
  it("Should display a profile picture if the keycloak authenticator provides one", (done) => {
    const testImagePath = "https://url.to/testImage";
    const wrapper: VueWrapper = mount(UserProfileDropDown, {
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
    void wrapper.vm.$nextTick(() => {
      /* eslint-disable @typescript-eslint/ban-ts-comment, @typescript-eslint/no-unsafe-call */
      // @ts-ignore
      expect(wrapper.vm.$refs["profile-picture"].src).to.be.equal(testImagePath);
      // @ts-ignore
      wrapper.vm.handleProfilePicError();
      void wrapper.vm.$nextTick(() => {
        // @ts-ignore
        expect(wrapper.vm.$refs["profile-picture"].src).not.equal(testImagePath);
        done();
      });
    });
  });
});
