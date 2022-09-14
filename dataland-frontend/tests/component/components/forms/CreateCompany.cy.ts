import CreateCompany from "@/components/forms/CreateCompany.vue";
import { shallowMount } from "@vue/test-utils";

describe("Component test for CreateCompany", () => {
  const wrapper = shallowMount(CreateCompany);

  it("Check that the initial values are correct", () => {
    expect(wrapper.vm.postCompanyProcessed).to.equal(false);
    expect(wrapper.vm.model).to.be.an("object").that.is.empty;
    expect(wrapper.vm.companyInformationSchema).to.be.an("array").that.is.not
      .empty;
    expect(wrapper.vm.companyIdentifierSchema).to.be.an("array").that.is.not
      .empty;
    expect(wrapper.vm.postCompanyResponse).to.be.null;
    expect(wrapper.vm.messageCount).to.equal(0);
    expect(wrapper.vm.identifierListSize).to.equal(1);
  });
});
