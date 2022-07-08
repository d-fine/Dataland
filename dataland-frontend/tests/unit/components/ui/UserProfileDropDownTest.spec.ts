import { mount } from '@vue/test-utils'
import UserProfileDropDown from "@/components/general/UserProfileDropDown.vue";
import { expect } from '@jest/globals';

describe("UserProfileDropDownTest", () => {
    //expect(wrapper.vm.$route.path).eq($route.path)
    const WrappComponent = {
        template: '<UserProfileDropDown ref="pd"/>',
        components: {UserProfileDropDown},
        provide: {
            authenticated: true,
            getKeycloakInitPromise() {
                return Promise.resolve({
                    authenticated: true,
                    idTokenParsed: {
                        picture: "http://localhost/testimg"
                    }
                });
            }
        }
    }

    it('Should display a profile picture if the keycloak autenticator provides one', (done) => {
        const wrapper = mount(WrappComponent);
        const propic = wrapper.vm.$refs.pd;
        wrapper.vm.$nextTick(() => {
            expect(propic.$refs["profile-picture"].src).toBe("http://localhost/testimg");
            done();
        });

    });

});