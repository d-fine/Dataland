import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import { shallowMount } from "@vue/test-utils";
import { expect } from "@jest/globals";

describe("CompanyInformation", () => {
  const wrapper = shallowMount(CompanyInformation);

  it("checks field properties", () => {
    expect(wrapper.vm.company).toBeDefined();
    expect(wrapper.vm.response).toBeDefined();
    expect(wrapper.vm.companyInformation).toBeDefined();
  });

  it("checks getCompanyInformation()", async () => {
    jest.spyOn(console, "error");
    expect(wrapper.vm.getCompanyInformation()).toBeDefined();
    await wrapper.vm.getCompanyInformation();
    expect(console.error).toHaveBeenCalled();
  });
});
