import { assertDefined } from "@/utils/TypeScriptUtils";

describe("Unit test for assertDefined", () => {
  it("verifies whether an error is thrown when invalid input is provided", () => {
    expect(function () {
      assertDefined(null);
    }).to.throw("Assertion error: Input was supposed to be non-null but is");
    expect(function () {
      assertDefined(undefined);
    }).to.throw("Assertion error: Input was supposed to be non-null but is");
  });

  it("verifies that the input is returned when it is non-null", () => {
    expect(assertDefined("Test")).to.equal("Test");
    expect(assertDefined(5)).to.equal(5);
  });
});
