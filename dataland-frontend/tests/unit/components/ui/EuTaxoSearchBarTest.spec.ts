import EuTaxoSearchBar from "@/components/resources/taxonomy/search/EuTaxoSearchBar.vue";
import { shallowMount } from "@vue/test-utils";
import { createRouter, createMemoryHistory } from "vue-router";
import { routes } from "@/router";
import { expect } from "@jest/globals";

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
        provide: {
          getKeycloakPromise() {
            return Promise.resolve("I should be a Keycloak Object");
          }
        },
      },
    });
  });

  it("checks field properties", () => {
    expect(wrapper.vm.autocompleteArray).toBeDefined();
    expect(wrapper.vm.autocompleteArrayDisplayed).toBeDefined();
    expect(wrapper.vm.loading).toBeDefined();
    expect(wrapper.vm.modelValue).toBeDefined();
  });

  it("checks queryCompany()", async () => {
    jest.spyOn(console, "error");
    expect(wrapper.vm.queryCompany("someCompanyToSearchFor")).toBeDefined();
    await wrapper.vm.queryCompany("someCompanyToSearchFor");
    expect(console.error).toHaveBeenCalled();
  });
});
