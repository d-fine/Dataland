import { describeIf } from '@e2e/support/TestUtility.ts';

describeIf(
  'As a user, I expect to be able to log in',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function () {
    it('Check whether login can be completed', () => {});
  }
);
