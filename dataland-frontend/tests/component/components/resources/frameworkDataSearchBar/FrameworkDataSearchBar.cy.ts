import FrameworkDataSearchBar from "@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue";
import { shallowMount } from "@vue/test-utils";
import { createRouter, createMemoryHistory } from "vue-router";
import { routes } from "@/router";
import {
  getInjectedKeycloakObjectsForTest,
  getRequiredPlugins,
} from "../../../TestUtils";

describe("Component test for FrameworkDataSearchBar", () => {
  let wrapper: any;
  it("checks field properties", async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes,
    });
    router.push("/companies");
    await router.isReady();
    wrapper = shallowMount(FrameworkDataSearchBar, {
      global: {
        plugins: [router, ...getRequiredPlugins()],
        provide: getInjectedKeycloakObjectsForTest(),
      },
    });
  });

  it("Check that the initial values are correct", () => {
    expect(wrapper.vm.autocompleteArray).to.be.an("array").that.is.empty;
    expect(wrapper.vm.autocompleteArrayDisplayed).to.be.null;
    expect(wrapper.vm.loading).to.equal(false);
    expect(wrapper.vm.currentInput).to.be.null;
  });
});
