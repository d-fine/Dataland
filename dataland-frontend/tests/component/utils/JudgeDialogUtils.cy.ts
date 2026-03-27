import {
  DEFAULT_CUSTOM_JSON,
  parseFormDataToDataPointJson,
  parseDataPointJsonToFormData,
  transformDataPointDetailToFormData,
  toSafeDisplayString,
} from '@/utils/JudgeDialogUtils';
import type {
  CustomFormData,
  DataPointDetail,
  DocumentOption,
} from '@/components/resources/datasetReview/JudgeDialogTypes';

describe('parseFormDataToDataPointJson', () => {
  const emptyForm: CustomFormData = {
    value: '',
    quality: '',
    document: '',
    pages: '',
    comment: '',
  };

  const baseForm: CustomFormData = {
    value: 'v',
    quality: 'q',
    document: '',
    pages: '',
    comment: 'c',
  };

  const documentOption: DocumentOption = {
    label: 'Annual Report',
    value: 'annual-report',
    dataSource: {
      fileName: 'AnnualReport.pdf',
      fileReference: 'ref-123',
      publicationDate: '2024-01-01',
    },
  };

  it('returns DEFAULT_CUSTOM_JSON when the form is effectively empty', () => {
    const json = parseFormDataToDataPointJson(emptyForm, null);
    expect(json).to.equal(DEFAULT_CUSTOM_JSON);
  });

  it('builds JSON with only value/quality/comment when no dataSource info is present', () => {
    const json = parseFormDataToDataPointJson(baseForm, null);
    const parsed = JSON.parse(json) as DataPointDetail;

    expect(parsed).to.deep.equal({
      value: 'v',
      quality: 'q',
      comment: 'c',
    });
  });

  it('adds a dataSource with only page when pages are provided but no document is selected', () => {
    const form: CustomFormData = {
      value: 'v',
      quality: '',
      document: '',
      pages: '10-12',
      comment: '',
    };

    const json = parseFormDataToDataPointJson(form, null);
    const parsed = JSON.parse(json) as DataPointDetail;

    expect(parsed).to.deep.equal({
      value: 'v',
      dataSource: { page: '10-12' },
    });
  });

  it('merges selected document dataSource and pages into the dataSource object', () => {
    const form: CustomFormData = {
      value: 'v',
      quality: 'q',
      document: documentOption.value,
      pages: '5',
      comment: 'c',
    };

    const json = parseFormDataToDataPointJson(form, documentOption);
    const parsed = JSON.parse(json) as DataPointDetail;

    expect(parsed.value).to.equal('v');
    expect(parsed.quality).to.equal('q');
    expect(parsed.comment).to.equal('c');
    expect(parsed.dataSource).to.deep.equal({
      fileName: 'AnnualReport.pdf',
      fileReference: 'ref-123',
      publicationDate: '2024-01-01',
      page: '5',
    });
  });

  it('uses only document dataSource when pages is empty', () => {
    const form: CustomFormData = {
      value: '',
      quality: '',
      document: documentOption.value,
      pages: '',
      comment: '',
    };

    const json = parseFormDataToDataPointJson(form, documentOption);
    const parsed = JSON.parse(json) as DataPointDetail;

    expect(parsed).to.deep.equal({
      dataSource: {
        fileName: 'AnnualReport.pdf',
        fileReference: 'ref-123',
        publicationDate: '2024-01-01',
      },
    });
  });
});

describe('transformDataPointDetailToFormData', () => {
  it('maps a full DataPointDetail to CustomFormData', () => {
    const detail: DataPointDetail = {
      value: 'v',
      quality: 'q',
      comment: 'c',
      dataSource: {
        fileName: 'Report.pdf',
        fileReference: 'ref-999',
        page: '12',
      },
    };

    const form = transformDataPointDetailToFormData(detail);

    expect(form).to.deep.equal({
      value: 'v',
      quality: 'q',
      document: 'Report.pdf', // fileName preferred
      pages: '12',
      comment: 'c',
    });
  });

  it('falls back to fileReference when fileName is not present', () => {
    const detail: DataPointDetail = {
      value: 'v',
      quality: 'q',
      comment: 'c',
      dataSource: {
        fileName: null,
        fileReference: 'ref-123',
        page: '7',
      },
    } as DataPointDetail;

    const form = transformDataPointDetailToFormData(detail);

    expect(form.document).to.equal('ref-123');
    expect(form.pages).to.equal('7');
  });

  it('returns empty strings for missing fields', () => {
    const detail: DataPointDetail = {
      value: null,
      quality: undefined,
      comment: undefined,
      dataSource: undefined,
    };

    const form = transformDataPointDetailToFormData(detail);

    expect(form).to.deep.equal({
      value: '',
      quality: '',
      document: '',
      pages: '',
      comment: '',
    });
  });
});

describe('parseDataPointJsonToFormData', () => {
  it('parses valid JSON into CustomFormData', () => {
    const detail: DataPointDetail = {
      value: 'v',
      quality: 'q',
      comment: 'c',
      dataSource: {
        fileName: 'ParsedReport.pdf',
        page: '9',
      },
    };

    const json = JSON.stringify(detail);
    const form = parseDataPointJsonToFormData(json);

    expect(form).to.deep.equal({
      value: 'v',
      quality: 'q',
      document: 'ParsedReport.pdf',
      pages: '9',
      comment: 'c',
    });
  });

  it('returns null when JSON is invalid', () => {
    const invalidJson = '{ this is not valid json }';

    const form = parseDataPointJsonToFormData(invalidJson);

    expect(form).to.equal(null);
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

  it('converts bigint to string', () => {
    expect(toSafeDisplayString(123n)).to.equal('123');
  });

  it('converts symbol to string', () => {
    const sym = Symbol('test');
    expect(toSafeDisplayString(sym)).to.equal(sym.toString());
  });

  it('stringifies objects', () => {
    expect(toSafeDisplayString({ a: 1, b: 'x' })).to.equal('{"a":1,"b":"x"}');
    expect(toSafeDisplayString([1, 2, 3])).to.equal('[1,2,3]');
  });
});
