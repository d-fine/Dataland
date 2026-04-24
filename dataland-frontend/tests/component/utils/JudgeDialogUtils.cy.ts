import {
  DEFAULT_CUSTOM_JSON,
  parseFormDataToDataPointJson,
  parseDataPointJsonToFormData,
  transformDataPointDetailToFormData,
  unwrapDataPointJson,
  wrapDataPointJson,
} from '@/utils/JudgeDialogUtils';
import type { CustomFormData, ParsedSingleDataPoint, DocumentOption } from '@/types/JudgeDialogTypes.ts';

describe('parseFormDataToDataPointJson', () => {
  const emptyForm: CustomFormData = {
    value: '',
    quality: '',
    document: '',
    pages: '',
    comment: '',
  };

  // Realistic base example: numeric value, quality, pages, and comment
  const baseFormValue = '123.45';
  const baseFormQuality = 'Reported';
  const baseFormPages = '121–125';
  const baseFormComment = 'Taken from Annual Report 2023, pages 121–125.';

  const baseForm: CustomFormData = {
    value: baseFormValue,
    quality: baseFormQuality,
    document: '', // no document selected in this scenario
    pages: baseFormPages,
    comment: baseFormComment,
  };

  const documentOption: DocumentOption = {
    label: 'Annual Report 2023',
    value: 'annual-report-2023',
    dataSource: {
      fileName: 'AnnualReport2023.pdf',
      fileReference: 'ref-123',
      publicationDate: '2024-01-01',
    },
  };

  it('returns DEFAULT_CUSTOM_JSON when the form is effectively empty', () => {
    const json = parseFormDataToDataPointJson(emptyForm, null);
    expect(json).to.equal(DEFAULT_CUSTOM_JSON);
  });

  it('builds JSON with value/quality/comment and a page-only dataSource when only pages are provided', () => {
    const json = parseFormDataToDataPointJson(baseForm, null);
    const parsed = JSON.parse(json) as ParsedSingleDataPoint;

    expect(parsed).to.deep.equal({
      value: baseFormValue,
      quality: baseFormQuality,
      comment: baseFormComment,
      dataSource: {
        page: baseFormPages,
      },
    });
  });

  it('builds a page-only dataSource when no document is selected', () => {
    const form: CustomFormData = {
      value: '2465.12',
      quality: 'Estimated',
      document: '',
      pages: '10–12',
      comment: '',
    };

    const json = parseFormDataToDataPointJson(form, null);
    const parsed = JSON.parse(json) as ParsedSingleDataPoint;

    expect(parsed).to.deep.equal({
      value: '2465.12',
      quality: 'Estimated',
      dataSource: { page: '10–12' },
    });
  });

  it('merges selected document dataSource and pages into the dataSource object', () => {
    const form: CustomFormData = {
      value: '987.65',
      quality: 'Audited',
      document: documentOption.value,
      pages: '5',
      comment: 'Verified against Annual Report 2023.',
    };

    const json = parseFormDataToDataPointJson(form, documentOption);
    const parsed = JSON.parse(json) as ParsedSingleDataPoint;

    expect(parsed.value).to.equal('987.65');
    expect(parsed.quality).to.equal('Audited');
    expect(parsed.comment).to.equal('Verified against Annual Report 2023.');
    expect(parsed.dataSource).to.deep.equal({
      fileName: 'AnnualReport2023.pdf',
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
    const parsed = JSON.parse(json) as ParsedSingleDataPoint;

    expect(parsed).to.deep.equal({
      dataSource: {
        fileName: 'AnnualReport2023.pdf',
        fileReference: 'ref-123',
        publicationDate: '2024-01-01',
      },
    });
  });
});

describe('transformDataPointDetailToFormData', () => {
  it('maps a full ParsedSingleDataPoint to CustomFormData', () => {
    const detail: ParsedSingleDataPoint = {
      value: '123.45',
      quality: 'Reported',
      comment: 'Taken from Sustainability Report 2023, page 12.',
      dataSource: {
        fileName: 'SustainabilityReport2023.pdf',
        fileReference: 'ref-999',
        page: '12',
      },
    };

    const form = transformDataPointDetailToFormData(detail);

    expect(form).to.deep.equal({
      value: '123.45',
      quality: 'Reported',
      document: 'SustainabilityReport2023.pdf', // fileName preferred
      pages: '12',
      comment: 'Taken from Sustainability Report 2023, page 12.',
    });
  });

  it('falls back to fileReference when fileName is not present', () => {
    const detail: ParsedSingleDataPoint = {
      value: '50.0',
      quality: 'Estimated',
      comment: 'No fileName available, only reference.',
      dataSource: {
        fileName: null,
        fileReference: 'ref-123',
        page: '7',
      },
    };

    const form = transformDataPointDetailToFormData(detail);

    expect(form.document).to.equal('ref-123');
    expect(form.pages).to.equal('7');
  });

  it('returns empty strings for missing fields', () => {
    const detail: ParsedSingleDataPoint = {
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
    const detail: ParsedSingleDataPoint = {
      value: '321.00',
      quality: 'Audited',
      comment: 'Verified against ParsedReport.pdf.',
      dataSource: {
        fileName: 'ParsedReport.pdf',
        page: '9',
      },
    };

    const json = JSON.stringify(detail);
    const form = parseDataPointJsonToFormData(json);

    expect(form).to.deep.equal({
      value: '321.00',
      quality: 'Audited',
      document: 'ParsedReport.pdf',
      pages: '9',
      comment: 'Verified against ParsedReport.pdf.',
    });
  });

  it('returns null when JSON is invalid', () => {
    const invalidJson = '{ this is not valid json }';

    const form = parseDataPointJsonToFormData(invalidJson);

    expect(form).to.equal(null);
  });
});

describe('unwrapDataPointJson', () => {
  it('unwraps to a plain primitive when original data point was a primitive and custom JSON is an object with value', () => {
    const rawDataPoint = JSON.stringify('2024-01-01'); // original backend value: "2024-01-01"
    const customDetail: ParsedSingleDataPoint = {
      value: '2024-01-01',
      quality: 'Audited',
      comment: 'Manually confirmed by reviewer.',
    };
    const customJson = JSON.stringify(customDetail);

    const result = unwrapDataPointJson(customJson, rawDataPoint);

    expect(result).to.equal(JSON.stringify('2024-01-01'));
  });

  it('unwraps to a plain primitive when both original data point and custom JSON are primitives', () => {
    const rawDataPoint = JSON.stringify(123); // original: 123
    const customJson = JSON.stringify(456); // custom: 456

    const result = unwrapDataPointJson(customJson, rawDataPoint);
    expect(result).to.equal(JSON.stringify(456));
  });

  it('returns original custom JSON unchanged when original data point is an object', () => {
    const rawDetail: ParsedSingleDataPoint = {
      value: 'initial value',
      quality: 'Reported',
      comment: 'Some comment',
      dataSource: { fileName: 'A file', page: '1' },
    };
    const rawDataPoint = JSON.stringify(rawDetail);

    const customDetail: ParsedSingleDataPoint = {
      value: 'new-v',
      quality: 'Audited',
      comment: 'new comment',
      dataSource: { fileName: 'A new file', page: '2' },
    };
    const customJson = JSON.stringify(customDetail);

    const result = unwrapDataPointJson(customJson, rawDataPoint);
    expect(result).to.equal(customJson);
  });

  it('returns original custom JSON when rawDataPoint is invalid JSON', () => {
    const rawDataPoint = '{ not valid json }';
    const customJson = JSON.stringify({ value: 'v' });

    const result = unwrapDataPointJson(customJson, rawDataPoint);
    expect(result).to.equal(customJson);
  });
});

describe('wrapDataPointJson', () => {
  it('returns a ParsedSingleDataPoint object unchanged when JSON represents an object', () => {
    const detail: ParsedSingleDataPoint = {
      value: '123.45',
      quality: 'Reported',
      comment: 'From WrappedReport.pdf, page 5.',
      dataSource: {
        fileName: 'WrappedReport.pdf',
        page: '5',
      },
    };
    const json = JSON.stringify(detail);
    const wrapped = wrapDataPointJson(json);
    expect(wrapped).to.deep.equal(detail);
  });

  it('wraps a primitive string JSON into a ParsedSingleDataPoint with value', () => {
    const json = JSON.stringify('2024-01-01');
    const wrapped = wrapDataPointJson(json);
    expect(wrapped).to.deep.equal({ value: '2024-01-01' });
  });

  it('wraps a primitive number JSON into a ParsedSingleDataPoint with value', () => {
    const json = JSON.stringify(123);
    const wrapped = wrapDataPointJson(json);
    expect(wrapped).to.deep.equal({ value: 123 });
  });

  it('returns null when JSON is invalid', () => {
    const invalidJson = '{ this is not valid json }';
    const wrapped = wrapDataPointJson(invalidJson);
    expect(wrapped).to.equal(null);
  });

  it('returns null when JSON value is null', () => {
    const nullJson = JSON.stringify(null);
    const wrapped = wrapDataPointJson(nullJson);
    expect(wrapped).to.deep.equal({ value: null });
  });
});
