import FrameworkDataSearchBar from "@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue";
import { mount } from "@vue/test-utils";
import { createRouter, createMemoryHistory } from "vue-router";
import { routes } from "@/router";

describe("Component test for FrameworkDataSearchBar", () => {
  let wrapper: any;
  it("checks field properties", async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes,
    });
    await router.push("/companies");
    await router.isReady();
    wrapper = mount(FrameworkDataSearchBar, {
      global: {
        plugins: [router],
      },
    });
  });

  it("Check that the initial values are correct", () => {
    expect(wrapper.vm.autocompleteArray).to.be.an("array").that.is.empty;
    expect(wrapper.vm.autocompleteArrayDisplayed).to.be.an("array").that.is.empty;
    expect(wrapper.vm.loading).to.equal(false);
  });
});
