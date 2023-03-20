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

  it("Check that an error message is displayed when trying to upload a company without any identifiers", () => {
    void wrapper.setData({ identifiers: [] });
    void (wrapper.vm.postCompanyInformation as () => Promise<void>)();
    expect(wrapper.vm.uploadSucceded).to.be.false;
    expect(wrapper.vm.postCompanyProcessed).to.be.true;
  });

  it("Check if removing an alternative company name works", () => {
    void wrapper.setData({
      companyAlternativeNames: ["No1", "No2", "No3"],
    });
    (wrapper.vm.removeAlternativeName as (index: number) => void)(2);
    const expectedValues = ["No1", "No3"];
    (wrapper.vm.companyAlternativeNames as string[]).forEach((name: string, index: number) => {
      expect(name).to.equal(expectedValues[index]);
    });
  });
});
