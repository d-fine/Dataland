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

  it("checks initial data", () => {
    expect(wrapper.vm.skyminderSearchParams).toEqual({
      countryCode: "SomeCountryCode",
      name: "SomeCountryName",
    });
    expect(wrapper.vm.skyminderSearchResponse).toBeNull();
  });
});
