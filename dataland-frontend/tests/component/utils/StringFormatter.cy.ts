import { convertKebabCaseToPascalCase, humanizeStringOrNumber, toSafeDisplayString } from '@/utils/StringFormatter';
import { DataTypeEnum } from '@clients/backend';
import { PublicFrameworkDefinitions } from '@/frameworks/BasePublicFrameworkRegistryImports';
import { PrivateFrameworkDefinitions } from '@/frameworks/BasePrivateFrameworkRegistryImports';

describe('Unit test for StringFormatter', () => {
  it('Check if specific keywords are converted correctly', () => {
    expect(humanizeStringOrNumber('alignedcapex')).to.equal('Aligned CapEx');
  });

  it('Check if strings other than specific keywords are converted from camel case to sentence case', () => {
    expect(humanizeStringOrNumber('ThisIsACamelCaseString')).to.equal('This Is A Camel Case String');
    expect(humanizeStringOrNumber('companyFullName')).to.equal('Company Full Name');
    expect(humanizeStringOrNumber('PrimeStandards')).to.equal('Prime Standards');
  });

  it('Check that entering null or undefined is possible', () => {
    expect(humanizeStringOrNumber(null)).to.equal('');
    expect(humanizeStringOrNumber(undefined)).to.equal('');
  });

  it('Check that numbers are humanized correctly', () => {
    expect(humanizeStringOrNumber(220)).to.equal('220');
    expect(humanizeStringOrNumber(2.523)).to.equal('2.523');
  });

  it('Check that framework identifiers are being formatted correctly', () => {
    expect(humanizeStringOrNumber(DataTypeEnum.Lksg)).to.equal(PublicFrameworkDefinitions[DataTypeEnum.Lksg].label);
    expect(humanizeStringOrNumber(DataTypeEnum.Vsme)).to.equal(PrivateFrameworkDefinitions[DataTypeEnum.Vsme].label);
  });

  it('Check that kebab case is converted correctly to camel case', () => {
    expect(convertKebabCaseToPascalCase('this-is-kebab-case')).to.equal('ThisIsKebabCase');
    expect(convertKebabCaseToPascalCase('ThisIsAlreadyPascalCase')).to.equal('ThisIsAlreadyPascalCase');
  });
});

describe('toSafeDisplayString', () => {
  it('returns empty string for null and undefined', () => {
    expect(toSafeDisplayString(null)).to.equal('');
    expect(toSafeDisplayString(undefined)).to.equal('');
  });

  it('returns the same string for string values', () => {
    expect(toSafeDisplayString('hello')).to.equal('hello');
    expect(toSafeDisplayString('')).to.equal('');
  });

  it('converts numbers to strings', () => {
    expect(toSafeDisplayString(0)).to.equal('0');
    expect(toSafeDisplayString(42)).to.equal('42');
  });

  it('converts booleans to "true" or "false"', () => {
    expect(toSafeDisplayString(true)).to.equal('true');
    expect(toSafeDisplayString(false)).to.equal('false');
  });

  it('stringifies objects', () => {
    expect(toSafeDisplayString({ a: 1, b: 'x' })).to.equal('{"a":1,"b":"x"}');
    expect(toSafeDisplayString([1, 2, 3])).to.equal('[1,2,3]');
  });
});
