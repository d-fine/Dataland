import CreateCompany from "@/components/forms/CreateCompany.vue";
import { shallowMount } from "@vue/test-utils";

describe("Component test for CreateCompany", () => {
  const wrapper = shallowMount(CreateCompany);

  it("Check that the initial values are correct", () => {
    expect(wrapper.vm.postCompanyProcessed).to.equal(false);
    expect(wrapper.vm.companyAlternativeNames).to.be.an("array").that.is.empty;
    expect(wrapper.vm.identifiers).to.be.an("array").that.is.empty;
    expect(wrapper.vm.allCountryCodes).to.be.an("array").that.is.not.empty;
    expect(wrapper.vm.companyDataExplanations).to.be.an("object").that.is.not.empty;
    expect(wrapper.vm.companyDataNames).to.be.an("object").that.is.not.empty;
    expect(wrapper.vm.gicsSectors).to.be.an("array").that.is.not.empty;
    expect(wrapper.vm.messageCounter).to.equal(0);
  });
});
