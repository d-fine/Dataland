import { convertKebabCaseToPascalCase, humanizeStringOrNumber, truncateText, humanizeDataPointBaseType } from '@/utils/StringFormatter';
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
    expect(humanizeStringOrNumber(DataTypeEnum.Lksg)).to.equal(PublicFrameworkDefinitions[DataTypeEnum.Lksg]!.label);
    expect(humanizeStringOrNumber(DataTypeEnum.Vsme)).to.equal(PrivateFrameworkDefinitions[DataTypeEnum.Vsme]!.label);
  });

  it('Check that kebab case is converted correctly to camel case', () => {
    expect(convertKebabCaseToPascalCase('this-is-kebab-case')).to.equal('ThisIsKebabCase');
    expect(convertKebabCaseToPascalCase('ThisIsAlreadyPascalCase')).to.equal('ThisIsAlreadyPascalCase');
  });

  describe('truncateText', () => {
    it('Should not truncate text shorter than maxLength', () => {
      const result = truncateText('Short text', 50);
      expect(result.truncated).to.equal('Short text');
      expect(result.needsTruncation).to.be.false;
    });

    it('Should not truncate text exactly at maxLength', () => {
      const result = truncateText('Exactly20Characters!', 20);
      expect(result.truncated).to.equal('Exactly20Characters!');
      expect(result.needsTruncation).to.be.false;
    });

    it('Should truncate at last space before limit with word boundaries', () => {
      const text = 'This is a long text that needs to be truncated properly';
      const result = truncateText(text, 30);
      expect(result.truncated).to.equal('This is a long text that...');
      expect(result.needsTruncation).to.be.true;
    });

    it('Should hard truncate at maxLength when no spaces exist', () => {
      const text = 'ThisIsAVeryLongWordWithoutAnySpaces';
      const result = truncateText(text, 20);
      expect(result.truncated).to.equal('ThisIsAVeryLongW...');
      expect(result.needsTruncation).to.be.true;
    });

    it('Should handle empty string', () => {
      const result = truncateText('', 50);
      expect(result.truncated).to.equal('');
      expect(result.needsTruncation).to.be.false;
    });

    it('Should handle single word longer than maxLength', () => {
      const result = truncateText('Supercalifragilisticexpialidocious', 15);
      expect(result.truncated).to.equal('Supercalifr...');
      expect(result.needsTruncation).to.be.true;
    });
    
    it('Should handle text with multiple spaces', () => {
      const text = 'This has    multiple   spaces in it and should truncate';
      const result = truncateText(text, 35);
      expect(result.truncated).to.equal('This has    multiple   spaces...');
      expect(result.needsTruncation).to.be.true;
    });
  });

  describe('humanizeDataPointBaseType', () => {
    it('Should map plainString to Text', () => {
      expect(humanizeDataPointBaseType('plainString')).to.equal('Text');
    });

    it('Should map plainDate to Date', () => {
      expect(humanizeDataPointBaseType('plainDate')).to.equal('Date');
    });

    it('Should map plainInteger to Number', () => {
      expect(humanizeDataPointBaseType('plainInteger')).to.equal('Number');
    });

    it('Should map plainDecimal to Number', () => {
      expect(humanizeDataPointBaseType('plainDecimal')).to.equal('Number');
    });

    it('Should map extendedDecimal to Number', () => {
      expect(humanizeDataPointBaseType('extendedDecimal')).to.equal('Number');
    });

    it('Should map plainBoolean to Yes/No', () => {
      expect(humanizeDataPointBaseType('plainBoolean')).to.equal('Yes/No');
    });

    it('Should map plainEnum to Selection', () => {
      expect(humanizeDataPointBaseType('plainEnum')).to.equal('Selection');
    });

    it('Should map extendedEnum to Selection', () => {
      expect(humanizeDataPointBaseType('extendedEnum')).to.equal('Selection');
    });

    it('Should map extendedArray to List', () => {
      expect(humanizeDataPointBaseType('extendedArray')).to.equal('List');
    });

    it('Should detect enum pattern in unknown type', () => {
      expect(humanizeDataPointBaseType('extendedEnumPcafMainSector')).to.equal('Selection');
    });

    it('Should detect date pattern in unknown type', () => {
      expect(humanizeDataPointBaseType('someDateField')).to.equal('Date');
    });

    it('Should detect number patterns in unknown types', () => {
      expect(humanizeDataPointBaseType('someDecimalValue')).to.equal('Number');
      expect(humanizeDataPointBaseType('someIntegerValue')).to.equal('Number');
    });

    it('Should detect boolean pattern in unknown type', () => {
      expect(humanizeDataPointBaseType('someBooleanFlag')).to.equal('Yes/No');
    });

    it('Should detect array pattern in unknown type', () => {
      expect(humanizeDataPointBaseType('someArrayOfItems')).to.equal('List');
      expect(humanizeDataPointBaseType('someListOfValues')).to.equal('List');
    });

    it('Should detect string pattern in unknown type', () => {
      expect(humanizeDataPointBaseType('someStringValue')).to.equal('Text');
      expect(humanizeDataPointBaseType('someTextField')).to.equal('Text');
    });

    it('Should return Data for completely unknown types', () => {
      expect(humanizeDataPointBaseType('customUnknownType')).to.equal('Data');
    });

    it('Should handle empty string', () => {
      expect(humanizeDataPointBaseType('')).to.equal('Data');
    });

    it('Should be case insensitive for pattern detection', () => {
      expect(humanizeDataPointBaseType('PLAINSTRING')).to.equal('Data'); // No direct mapping (case sensitive)
      expect(humanizeDataPointBaseType('SomeEnumType')).to.equal('Selection'); // Pattern match (case insensitive)
    });
  });
});
