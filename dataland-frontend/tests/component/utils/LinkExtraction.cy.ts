import { segmentTextIncludingLinks } from '@/utils/LinkExtraction';

describe('Unit tests for link extraction', () => {
  it('HTTPS-Links should be extracted', () => {
    const outputArray = segmentTextIncludingLinks('This is a text with a link https://www.dataland.com/testing.');
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
    const outputArray = segmentTextIncludingLinks('Insecure link alert http://www.google.com');
    expect(outputArray).to.deep.equal([
      {
        type: 'text',
        text: 'Insecure link alert ',
      },
      {
        type: 'link',
        text: 'http://www.google.com',
        href: 'https://www.google.com',
      },
    ]);
  });
  it('Links with query parameters should be extracted', () => {
    const outputArray = segmentTextIncludingLinks('Test dataland.com/companies?input=hello, does it work?');
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
    const outputArray = segmentTextIncludingLinks('Weired Link alert https://www.dataland.com:332.');
    expect(outputArray).to.deep.equal([
      {
        type: 'text',
        text: 'Weired Link alert https://www.dataland.com:332.',
      },
    ]);
  });
  it('Links with username and password should not be extracted', () => {
    const outputArray = segmentTextIncludingLinks('https://user:pass@dataland.com');
    expect(outputArray).to.deep.equal([
      {
        type: 'text',
        text: 'https://user:pass@dataland.com',
      },
    ]);
  });
  it('Links without protocol should be extracted', () => {
    const outputArray = segmentTextIncludingLinks('This is a text with a link www.dataland.com/path.');
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
    const outputArray = segmentTextIncludingLinks('Without www dataland.com/companies.');
    expect(outputArray).to.deep.equal([
      {
        type: 'text',
        text: 'Without www ',
      },
      {
        type: 'link',
        text: 'dataland.com/companies',
        href: 'https://dataland.com/companies',
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
