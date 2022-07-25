import RetrieveSkyminder from "@/components/forms/RetrieveSkyminder.vue";
import { shallowMount } from "@vue/test-utils";
import { expect } from "@jest/globals";
import { getInjectedKeycloakObjectsForTest } from "../../TestUtils";

describe("RetrieveSkyminderTest", () => {
  const wrapper = shallowMount(RetrieveSkyminder, {
    data() {
      return {
        skyminderSearchParams: {
          countryCode: "SomeCountryCode",
          name: "SomeCountryName",
        },
      };
    },
    global: {
      provide: getInjectedKeycloakObjectsForTest(),
    },
  });

  it("checks field properties", () => {
    expect(wrapper.vm.skyminderSearchParams).toEqual({
      countryCode: "SomeCountryCode",
      name: "SomeCountryName",
    });
    expect(wrapper.vm.skyminderSearchResponse).toBeNull();
  });

  it("checks existence of required methods to communicate with backend", () => {
    expect(wrapper.vm.executeSkyminderSearch()).toBeDefined();
  });
});
