import { getKeysFromMapAndReturnAsAlphabeticallySortedArray } from "@/utils/GenericUtils";

describe("Unit test for StringHumanizer", () => {
  it("Check if specific keywords are converted correctly", () => {
    const inputMap = new Map<string, string>([
      ["X", "someValue_123"],
      ["A", "someValue_124"],
      ["2022", "someValue_125"],
      ["C", "someValue_126"],
      ["D", "someValue_127"],
      ["2020", "someValue_128"],
      ["B", "someValue_129"],
      ["2023", "someValue_130"],
    ]);
    const outputArray = getKeysFromMapAndReturnAsAlphabeticallySortedArray(inputMap);
    expect(outputArray.toString()).to.equal(["X", "D", "C", "A", "B", "2023", "2022", "2020"].toString());
  });
});
