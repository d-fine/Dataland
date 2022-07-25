import { convertCurrencyNumbersToNotationWithLetters } from "@/utils/CurrencyConverter";
import { expect } from "@jest/globals";

describe("Test CurrencyConverter", () => {
  it("verifies whether the implementation works correctly", () => {
    expect(convertCurrencyNumbersToNotationWithLetters(0, 1)).toEqual("0");
    expect(convertCurrencyNumbersToNotationWithLetters(1234, 1)).toEqual("1.2 K");
    expect(convertCurrencyNumbersToNotationWithLetters(100000000, 1)).toEqual("100 M");
    expect(convertCurrencyNumbersToNotationWithLetters(299792458, 2)).toEqual("299.79 M");
    expect(convertCurrencyNumbersToNotationWithLetters(3299792458, 2)).toEqual("3.3 B");
    expect(convertCurrencyNumbersToNotationWithLetters(3293792458, 2)).toEqual("3.29 B");
  });
});
