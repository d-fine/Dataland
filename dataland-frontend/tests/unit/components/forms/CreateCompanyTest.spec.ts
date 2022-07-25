import CreateCompany from "@/components/forms/CreateCompany.vue";
import { shallowMount } from "@vue/test-utils";
import { expect } from "@jest/globals";
import { getInjectedKeycloakObjectsForTest } from "../../TestUtils";

describe("CreateCompanyTest", () => {
  const wrapper = shallowMount(CreateCompany, {
    global: {
      provide: getInjectedKeycloakObjectsForTest(),
    },
  });

  it("checks field properties", () => {
    expect(wrapper.vm.postCompanyCallProcessed).toBeDefined();
    expect(wrapper.vm.model).toEqual({});
    expect(wrapper.vm.companyInformationSchema).toBeDefined();
    expect(wrapper.vm.companyIdentifierSchema).toBeDefined();
    expect(wrapper.vm.postCompanyResponse).toBeNull()
    expect(wrapper.vm.messageCount).toEqual(0)
    expect(wrapper.vm.identifierListSize).toEqual(1)
  });
});
