import WelcomeDataland from "@/components/pages/WelcomeDataland.vue";
import { shallowMount } from "@vue/test-utils";

describe("Component test for WelcomeDataland", () => {
  it("Check that the initial values are correct", () => {
    const wrapper = shallowMount(WelcomeDataland);
    expect(wrapper.text()).to.exist;
  });
});
