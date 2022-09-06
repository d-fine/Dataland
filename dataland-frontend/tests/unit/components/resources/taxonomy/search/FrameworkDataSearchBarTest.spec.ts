import FrameworkDataSearchBar from "@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue";
import { mount } from "@vue/test-utils";
import { createRouter, createMemoryHistory } from "vue-router";
import { routes } from "@/router";
import { expect } from "@jest/globals";
import { getInjectedKeycloakObjectsForTest, getRequiredPlugins } from "../../../../TestUtils";

describe("FrameworkDataSearchBarTest", () => {
  const router = createRouter({
    history: createMemoryHistory(),
    routes,
  });
  const wrapper = mount(FrameworkDataSearchBar, {
    global: {
      plugins: [router, ...getRequiredPlugins()],
      provide: getInjectedKeycloakObjectsForTest(),
    },
  });

  it("checks initial data", () => {
    expect(wrapper.vm.autocompleteArray).toBeDefined();
    expect(wrapper.vm.autocompleteArrayDisplayed).toBeDefined();
    expect(wrapper.vm.loading).toBeDefined();
  });
});
