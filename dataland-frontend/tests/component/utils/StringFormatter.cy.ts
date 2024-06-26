import { convertKebabCaseToPascalCase, humanizeStringOrNumber } from '@/utils/StringFormatter';
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
    expect(humanizeStringOrNumber(DataTypeEnum.Heimathafen)).to.equal(
      PublicFrameworkDefinitions[DataTypeEnum.Heimathafen].label
    );
    expect(humanizeStringOrNumber(DataTypeEnum.Vsme)).to.equal(PrivateFrameworkDefinitions[DataTypeEnum.Vsme].label);
  });

  it('Check that kebab case is converted correctly to camel case', () => {
    expect(convertKebabCaseToPascalCase('this-is-kebab-case')).to.equal('ThisIsKebabCase');
    expect(convertKebabCaseToPascalCase('ThisIsAlreadyPascalCase')).to.equal('ThisIsAlreadyPascalCase');
  });
});
