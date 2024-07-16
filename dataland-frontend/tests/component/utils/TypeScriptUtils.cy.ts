import { assertDefined, isString } from '@/utils/TypeScriptUtils';

describe('Unit test for assertDefined', () => {
  it('verifies whether an error is thrown when invalid input is provided', () => {
    const error_message = 'Assertion error: Input was supposed to be non-null but is.';
    expect(function () {
      assertDefined(null);
    }).to.throw(error_message);
    expect(function () {
      assertDefined(undefined);
    }).to.throw(error_message);
  });

  it('verifies that the input is returned when it is non-null', () => {
    expect(assertDefined('Test')).to.equal('Test');
    expect(assertDefined(5)).to.equal(5);
  });

  it('verifies that the isString function works', () => {
    expect(isString('Test')).to.be.true;

    const someRandomNumberThatIsNotAString = 5;
    expect(isString(someRandomNumberThatIsNotAString)).to.be.false;
  });
});
