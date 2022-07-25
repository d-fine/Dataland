import { splitStringBySearchMatch } from "@/utils/StringHighlighter";
import { expect } from "@jest/globals";

describe("Test StringHighlighter", () => {
  it("Should find no matches on an empty searchString", () => {
    expect(splitStringBySearchMatch("ThisISCool", "")).toEqual([{ text: "ThisISCool", highlight: false }]);
  });

  it("Should return a correct result on a non-empty searchstring", () => {
    expect(splitStringBySearchMatch("Hello there", "ello")).toEqual([
      { text: "H", highlight: false },
      { text: "ello", highlight: true },
      { text: " there", highlight: false },
    ]);

    expect(splitStringBySearchMatch("A A", "A")).toEqual([
      { text: "A", highlight: true },
      { text: " ", highlight: false },
      { text: "A", highlight: true },
    ]);
  });
});
