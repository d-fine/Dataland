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

  it("checks field properties", () => {
    expect(wrapper.vm.model).toBeDefined();
  });

  it("checks postCompanyData()", async () => {
    expect(wrapper.vm.postEUData()).toBeDefined();
  });
});
