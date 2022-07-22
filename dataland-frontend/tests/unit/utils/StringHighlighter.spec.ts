import { highlightSearchMatches } from "@/utils/StringHighlighter";
import { expect } from "@jest/globals";

describe("Test string highlighter", () => {
  it("verifies if the implementation works correctly", () => {
    expect(highlightSearchMatches("This Is A Test", "Te", "test")).toEqual('This Is A <span class="test">Te</span>st');
    expect(highlightSearchMatches("this is awesome", "mtm", "test")).toEqual("this is awesome");
  });
});
