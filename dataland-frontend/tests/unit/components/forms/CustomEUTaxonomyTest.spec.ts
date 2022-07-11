import CustomEUTaxonomy from "@/components/forms/CreateEUTaxonomy.vue";
import { shallowMount } from "@vue/test-utils";
import { expect } from "@jest/globals";

describe("CreateCompanyTest", () => {
  const wrapper = shallowMount(CustomEUTaxonomy);

  it("checks field properties", () => {
    expect(wrapper.vm.model).toBeDefined();
  });

  it("checks postCompanyData()", async () => {
    expect(wrapper.vm.postEUData()).toBeDefined();
  });
});
