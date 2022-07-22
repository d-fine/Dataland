import RetrieveSkyminder from "@/components/forms/RetrieveSkyminder.vue";
import { shallowMount } from "@vue/test-utils";
import { expect } from "@jest/globals";
import { getInjectedKeycloakObjectsForTest } from "../../TestUtils";

describe("RetrieveSkyminderTest", () => {
  const wrapper = shallowMount(RetrieveSkyminder, {
    data() {
      return {
        skyminderSearchParams: {
          code: "SomeCountryCode",
          name: "SomeCountryName",
        },
      };
    },
    global: {
      provide: getInjectedKeycloakObjectsForTest(),
    },
  });

  it("checks field properties", () => {
    expect(wrapper.vm.skyminderSearchParams).toBeDefined();
    expect(wrapper.vm.skyminderSearchResponse).toBeDefined();
  });

  it("checks postCompanyData()", async () => {
    expect(wrapper.vm.getSkyminderByName()).toBeDefined();
  });
});
