import CreateEUTaxonomyForNonFinancials from "@/components/forms/CreateEUTaxonomyForNonFinancials.vue";
import { shallowMount } from "@vue/test-utils";
import { expect } from "@jest/globals";
import { getInjectedKeycloakObjectsForTest, getRequiredPlugins } from "../../TestUtils";

describe("CreateCompanyTest", () => {
  const wrapper = shallowMount(CreateEUTaxonomyForNonFinancials, {
    global: {
      plugins: getRequiredPlugins(),
      provide: getInjectedKeycloakObjectsForTest(),
    },
  });

  it("checks initial data", () => {
    expect(wrapper.vm.innerClass).toBeDefined();
    expect(wrapper.vm.inputClass).toBeDefined();
    expect(wrapper.vm.postEuTaxonomyDataForNonFinancialsProcessed).toEqual(false);
    expect(wrapper.vm.messageCount).toEqual(0);
    expect(wrapper.vm.formInputsModel).toEqual({});
    expect(wrapper.vm.postEuTaxonomyDataForNonFinancialsResponse).toBeNull();
  });
});
