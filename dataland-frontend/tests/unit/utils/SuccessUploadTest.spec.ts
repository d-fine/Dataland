import { expect } from "@jest/globals";
import successUpload from "@/components/messages/SuccessUpload.vue";
import { shallowMount } from "@vue/test-utils";

describe("Negative test for Success message", () => {
  const wrapper = shallowMount(successUpload);

  it("verify that non string input yields null", () => {
    expect(wrapper.vm.humanize(5)).toBeNull();
  });
});
