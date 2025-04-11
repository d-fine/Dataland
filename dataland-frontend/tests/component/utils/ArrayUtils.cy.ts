import { arraySetEquals, groupBy, range } from '@/utils/ArrayUtils';

describe('Unit test for ArrayUtils', () => {
  it('verifies arraySetEquals', () => {
    expect(arraySetEquals([1], [1])).to.be.true;
    expect(arraySetEquals([1], [2])).to.be.false;
    expect(arraySetEquals([1], [1, 2])).to.be.false;
    expect(arraySetEquals([1, 2], [1])).to.be.false;
    expect(arraySetEquals([1, 2], [2, 1])).to.be.true;
    expect(arraySetEquals([1, 1], [1])).to.be.true;
    expect(arraySetEquals([], [])).to.be.true;
    expect(arraySetEquals([null], ['null'])).to.be.false;
  });

  it('verifies range', () => {
    expect(range(0)).to.deep.equal([]);
    expect(range(1)).to.deep.equal([0]);
    expect(range(3)).to.deep.equal([0, 1, 2]);
  });

  it('verifies groupBy', () => {
    expect(groupBy([1, 2, 3], (x) => x).get(1)).to.deep.equal([1]);
    expect(groupBy([1, 2, 3], (x) => x).get(2)).to.deep.equal([2]);
    expect(groupBy([1, 2, 3], (x) => x).get(3)).to.deep.equal([3]);

    expect(groupBy([1, 2, 3], () => 'a').get('a')).to.deep.equal([1, 2, 3]);

    expect(groupBy([1, 2, 3], (x) => x % 2).get(0)).to.deep.equal([2]);
    expect(groupBy([1, 2, 3], (x) => x % 2).get(1)).to.deep.equal([1, 3]);
  });
});
