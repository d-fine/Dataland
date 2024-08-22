import { formatBytesUserFriendly } from '@/utils/NumberConversionUtils';
import {
  formatNumberToReadableFormat,
  formatPercentageNumberAsString,
  formatAmountWithCurrency,
} from '@/utils/Formatter';

describe('Unit tests for formating data', () => {
  it('Check if file size display in more readable format', () => {
    const bytesValues = [1024 * 1024, 1.5 * 1024 * 1024, 500 * 1024, 30 * 1024, 0];
    const outputArray = bytesValues.map((el) => {
      return formatBytesUserFriendly(el, 1);
    });
    console.log(outputArray);
    expect(outputArray).to.deep.equal(['1 MB', '1.5 MB', '500 KB', '30 KB', '0 Bytes']);
  });
  it('Check if numbers are displayed in more readable format', () => {
    const sampleNumbers = [123456, 654321.123, 987654321, 123, 0];
    const outputArray = sampleNumbers.map((el) => {
      return formatNumberToReadableFormat(el);
    });
    expect(outputArray).to.deep.equal(['123,456', '654,321.12', '987,654,321', '123', '0']);
  });
  it('Check if percentage numbers is displayed in correct format (%)', () => {
    const sampleNumbers = [12, 65.123, 1200, 100, 0.1, 0, -1];
    const outputArray = sampleNumbers.map((el) => {
      return formatPercentageNumberAsString(el);
    });
    expect(outputArray).to.deep.equal(['12 %', '65.12 %', '1200 %', '100 %', '0.1 %', '0 %', '-1 %']);
  });
  it('Check if amount with currency is displayed in correct format', () => {
    const sampleObjects = [
      { amount: 12, currency: 'USD' },
      { amount: 132.123, currency: 'CHF' },
      { amount: undefined, currency: 'USD' },
      { amount: 12, currency: undefined },
    ];
    const outputArray = sampleObjects.map((el) => {
      return formatAmountWithCurrency(el);
    });
    expect(outputArray).to.deep.equal(['12.00 USD', '132.12 CHF', '', '12.00']);
  });
});
