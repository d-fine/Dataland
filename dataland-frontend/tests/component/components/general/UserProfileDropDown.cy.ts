import UserProfileDropDown from "@/components/general/UserProfileDropDown.vue";
import { mount, VueWrapper } from "@vue/test-utils";

describe("Component test for UserProfileDropDown", () => {
  const testImagePath = "https://url.to/testImage";

  it("Should display a profile picture if the keycloak authenticator provides one", (done) => {
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
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      expect(wrapper.vm.$refs["profile-picture"].src).to.be.equal(testImagePath);
      done();
    });
  });
});
