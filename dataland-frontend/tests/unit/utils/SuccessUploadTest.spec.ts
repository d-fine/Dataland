import { expect } from "@jest/globals";
import successUpload from "@/components/messages/SuccessUpload.vue";

describe("Negative test for Success message", () => {
  it("verify that non string input yields null", () => {
    expect(successUpload.methods.humanize(5)).toBeNull();
  });
});
