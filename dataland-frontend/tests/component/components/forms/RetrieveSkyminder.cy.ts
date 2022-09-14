import RetrieveSkyminder from "@/components/forms/RetrieveSkyminder.vue";
import { shallowMount } from "@vue/test-utils";

describe("Component test for RetrieveSkyminder", () => {
  const wrapper = shallowMount(RetrieveSkyminder);

  it("Check that the initial values are correct", () => {
    expect(wrapper.vm.skyminderSearchParams).to.be.an("object").that.is.empty;
    expect(wrapper.vm.skyminderSearchResponse).to.be.null;
  });
});
