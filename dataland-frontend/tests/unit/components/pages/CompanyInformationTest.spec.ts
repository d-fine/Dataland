import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import { shallowMount } from "@vue/test-utils";
import { expect } from "@jest/globals";
import { getInjectedKeycloakObjectsForTest, getRequiredPlugins } from "../../TestUtils";

describe("CompanyInformation", () => {
  const companyID = "e4f68bbb-96c0-42c1-ad92-05cf3df972c2";
  const wrapper = shallowMount(CompanyInformation, {
    props: { companyID },
    global: {
      plugins: getRequiredPlugins(),
      provide: getInjectedKeycloakObjectsForTest(),
    },
  });

  it("checks initial data", () => {
    expect(wrapper.vm.getCompanyResponse).toBeDefined();
    expect(wrapper.vm.companyInformation).toBeDefined();
  });
});
