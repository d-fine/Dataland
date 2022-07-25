import EuTaxoSearchBar from "@/components/resources/taxonomy/search/EuTaxoSearchBar.vue";
import { shallowMount } from "@vue/test-utils";
import { createRouter, createMemoryHistory } from "vue-router";
import { routes } from "@/router";
import { expect } from "@jest/globals";
import { getInjectedKeycloakObjectsForTest } from "../../../../TestUtils";

describe("EuTaxoSearchBarTest", () => {
  let wrapper: any;
  it("checks field properties", async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes,
    });
    router.push("/searchtaxonomy");
    await router.isReady();
    wrapper = shallowMount(EuTaxoSearchBar, {
      global: {
        plugins: [router],
        provide: getInjectedKeycloakObjectsForTest(),
      },
    });
  });

  it("checks initial data", () => {
    expect(wrapper.vm.autocompleteArray).toBeDefined();
    expect(wrapper.vm.autocompleteArrayDisplayed).toBeDefined();
    expect(wrapper.vm.loading).toBeDefined();
    expect(wrapper.vm.currentInput).toBeDefined();
  });

  it("checks existence of required methods to communicate with backend", () => {
    expect(wrapper.vm.queryCompany("someCompanyName")).toBeDefined();
    expect(wrapper.vm.searchCompanyName("someCompanyName")).toBeDefined();
  });
});
