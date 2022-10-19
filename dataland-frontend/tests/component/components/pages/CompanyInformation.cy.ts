import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import { shallowMount } from "@vue/test-utils";

describe("Component test for CompanyInformation", () => {
  const wrapper = shallowMount(CompanyInformation);

  it("Check that the initial values are correct", () => {
    expect(wrapper.vm.companyInformation).to.be.null;
  });
});
