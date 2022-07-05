import { expect } from "@jest/globals";
import failedUpload from "@/components/messages/FailedUpload.vue";
import { shallowMount } from "@vue/test-utils";

describe("Test for failed message", () => {
  const wrapper = shallowMount(failedUpload);

  it("verify that non string input yields null", () => {
    expect(wrapper.vm.humanize(5)).toBeNull();
  });

  it("verify that string input yields correct output", () => {
    expect(wrapper.vm.humanize("hallo")).toEqual("Hallo");
  });
});
