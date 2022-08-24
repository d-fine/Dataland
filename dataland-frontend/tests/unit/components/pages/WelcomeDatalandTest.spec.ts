import { shallowMount } from "@vue/test-utils";
import WelcomeDataland from "@/components/pages/WelcomeDataland.vue";
import { expect } from "@jest/globals";
import { getInjectedKeycloakObjectsForTest, getRequiredPlugins } from "../../TestUtils";

describe("WelcomeDatalandTest", () => {
  it("checks if it is defined", () => {
    const wrapper = shallowMount(WelcomeDataland, {
      global: {
        plugins: getRequiredPlugins(),
        provide: getInjectedKeycloakObjectsForTest(),
      },
    });
    expect(wrapper.text()).toBeDefined();
  });
});
