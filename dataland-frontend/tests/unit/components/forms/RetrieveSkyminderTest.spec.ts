import RetrieveSkyminder from "@/components/forms/RetrieveSkyminder.vue";
import { shallowMount } from "@vue/test-utils";
import { expect } from "@jest/globals";
import { getInjectedKeycloakObjectsForTest, getRequiredPlugins } from "../../TestUtils";

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
      plugins: getRequiredPlugins(),
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
