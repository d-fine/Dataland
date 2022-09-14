import CreateEUTaxonomyForNonFinancials from "@/components/forms/CreateEUTaxonomyForNonFinancials.vue";
import { shallowMount } from "@vue/test-utils";

describe("Component test for CreateEUTaxonomyForNonFinancials", () => {
  const wrapper = shallowMount(CreateEUTaxonomyForNonFinancials);

  it("Check that the initial values are correct", () => {
    expect(wrapper.vm.innerClass).to.exist;
    expect(wrapper.vm.inputClass).to.exist;
    expect(wrapper.vm.postEuTaxonomyDataForNonFinancialsProcessed).to.equal(false);
    expect(wrapper.vm.messageCount).to.equal(0);
    expect(wrapper.vm.formInputsModel).to.be.an("object").that.is.empty;
    expect(wrapper.vm.postEuTaxonomyDataForNonFinancialsResponse).to.be.null;
  });
});
