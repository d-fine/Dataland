import CreateEUTaxonomy from "@/components/forms/CreateEUTaxonomy.vue";
import { shallowMount } from "@vue/test-utils";
import { expect } from "@jest/globals";
import { getInjectedKeycloakObjectsForTest } from "../../TestUtils";

describe("CreateCompanyTest", () => {
  const wrapper = shallowMount(CreateEUTaxonomy, {
    global: {
      provide: getInjectedKeycloakObjectsForTest(),
    },
  });

  it("checks initial data", () => {
    expect(wrapper.vm.innerClass).toBeDefined();
    expect(wrapper.vm.inputClass).toBeDefined();
    expect(wrapper.vm.postEUDataProcessed).toEqual(false);
    expect(wrapper.vm.messageCount).toEqual(0);
    expect(wrapper.vm.model).toEqual({});
    expect(wrapper.vm.postEUDataResponse).toBeNull();
    expect(wrapper.vm.allExistingCompanyIDs.length).toEqual(0);
  });
});
