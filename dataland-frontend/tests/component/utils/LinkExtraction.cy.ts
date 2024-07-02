import { segmentTextIncludingLinks } from '@/utils/LinkExtraction';

describe('Unit tests for link extraction', () => {
  it('HTTPS-Links should be extracted', () => {
    const text = 'This is a text with a link https://www.dataland.com/testing.';
    const outputArray = segmentTextIncludingLinks(text);
    expect(outputArray).to.deep.equal([
      {
        type: 'text',
        text: 'This is a text with a link ',
      },
      {
        type: 'link',
        text: 'https://www.dataland.com/testing',
        href: 'https://www.dataland.com/testing',
      },
      {
        type: 'text',
        text: '.',
      },
    ]);
  });
  it('HTTP-Links should be converted to https', () => {
    const text = 'Insecure link alert http://www.dataland.com.';
    const outputArray = segmentTextIncludingLinks(text);
    expect(outputArray).to.deep.equal([
      {
        type: 'text',
        text: 'Insecure link alert ',
      },
      {
        type: 'link',
        text: 'http://www.dataland.com',
        href: 'https://www.dataland.com',
      },
      {
        type: 'text',
        text: '.',
      },
    ]);
  });
  it('Links with query parameters should be extracted', () => {
    const text = 'Test dataland.com/companies?input=hello, does it work?';
    const outputArray = segmentTextIncludingLinks(text);
    expect(outputArray).to.deep.equal([
      {
        type: 'text',
        text: 'Test ',
      },
      {
        type: 'link',
        text: 'dataland.com/companies?input=hello',
        href: 'https://dataland.com/companies?input=hello',
      },
      {
        type: 'text',
        text: ', does it work?',
      },
    ]);
  });
  it('Links with non-standard ports should not be extracted', () => {
    const text = 'Weired Link alert https://www.dataland.com:332.';
    const outputArray = segmentTextIncludingLinks(text);
    expect(outputArray).to.deep.equal([
      {
        type: 'text',
        text: 'Weired Link alert https://www.dataland.com:332.',
      },
    ]);
  });
  it('Links with username and password should not be extracted', () => {
    const text = 'https://user:pass@dataland.com';
    const outputArray = segmentTextIncludingLinks(text);
    expect(outputArray).to.deep.equal([
      {
        type: 'text',
        text: 'https://user:pass@dataland.com',
      },
    ]);
  });
  it('Links without protocol should be extracted', () => {
    const text = 'This is a text with a link www.dataland.com/path.';
    const outputArray = segmentTextIncludingLinks(text);
    expect(outputArray).to.deep.equal([
      {
        type: 'text',
        text: 'This is a text with a link ',
      },
      {
        type: 'link',
        text: 'www.dataland.com/path',
        href: 'https://www.dataland.com/path',
      },
      {
        type: 'text',
        text: '.',
      },
    ]);
  });
  it('Links without www should be extracted', () => {
    const text = 'Without www dataland.com/path.';
    const outputArray = segmentTextIncludingLinks(text);
    expect(outputArray).to.deep.equal([
      {
        type: 'text',
        text: 'Without www ',
      },
      {
        type: 'link',
        text: 'dataland.com/path',
        href: 'https://dataland.com/path',
      },
      {
        type: 'text',
        text: '.',
      },
    ]);
  });
  it('The ticket example url should be extracted correctly', () => {
    const text =
      'No evidence found in the analyzed documents. ' +
      '(Scope: Sustainability Report 2023 and all publicly available information on ' +
      'www.lifeatspotify.com, accessed on June 24, 2024)';
    const outputArray = segmentTextIncludingLinks(text);
    expect(outputArray).to.deep.equal([
      {
        type: 'text',
        text:
          'No evidence found in the analyzed documents. ' +
          '(Scope: Sustainability Report 2023 and all publicly available information on ',
      },
      {
        type: 'link',
        text: 'www.lifeatspotify.com',
        href: 'https://www.lifeatspotify.com',
      },
      {
        type: 'text',
        text: ', accessed on June 24, 2024)',
      },
    ]);
  });
});
