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
    expect(wrapper.vm.model).toBeDefined();
    expect(wrapper.vm.companyInformationSchema).toBeDefined();
    expect(wrapper.vm.processed).toBeDefined();
    expect(wrapper.vm.response).toBeDefined();
    expect(wrapper.vm.messageCount).toBeDefined();
  });

  it("checks postCompanyData()", async () => {
    jest.spyOn(console, "error");
    expect(wrapper.vm.postCompanyData()).toBeDefined();
    await wrapper.vm.postCompanyData();
    expect(console.error).toHaveBeenCalled();
  });
});
