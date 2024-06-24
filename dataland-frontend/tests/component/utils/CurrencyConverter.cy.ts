import { convertCurrencyNumbersToNotationWithLetters } from '@/utils/CurrencyConverter';

describe('Unit test for CurrencyConverter', () => {
  it('verifies whether the implementation works correctly', () => {
    expect(convertCurrencyNumbersToNotationWithLetters(0, 1)).to.equal('0');
    expect(convertCurrencyNumbersToNotationWithLetters(1234, 1)).to.equal('1.2 K');
    expect(convertCurrencyNumbersToNotationWithLetters(100000000, 1)).to.equal('100 M');
    expect(convertCurrencyNumbersToNotationWithLetters(299792458, 2)).to.equal('299.79 M');
    expect(convertCurrencyNumbersToNotationWithLetters(3299792458, 2)).to.equal('3.3 B');
    expect(convertCurrencyNumbersToNotationWithLetters(3293792458, 2)).to.equal('3.29 B');
  });
});
