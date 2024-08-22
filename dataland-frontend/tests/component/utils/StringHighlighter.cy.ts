import { splitStringBySearchMatch } from '@/utils/StringHighlighter';

describe('Unit test for StringHighlighter', () => {
  it('Should find no matches on an empty searchString', () => {
    expect(splitStringBySearchMatch('ThisISCool', '')).to.eql([{ text: 'ThisISCool', highlight: false }]);
  });

  it('Should return a correct result on a non-empty searchstring', () => {
    expect(splitStringBySearchMatch('Hello there', 'ello')).to.eql([
      { text: 'H', highlight: false },
      { text: 'ello', highlight: true },
      { text: ' there', highlight: false },
    ]);

    expect(splitStringBySearchMatch('A A', 'A')).to.eql([
      { text: 'A', highlight: true },
      { text: ' ', highlight: false },
      { text: 'A', highlight: true },
    ]);
  });
});
