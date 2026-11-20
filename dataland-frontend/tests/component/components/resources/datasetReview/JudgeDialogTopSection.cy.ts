import JudgeDialogTopSection from '@/components/resources/datasetReview/JudgeDialogTopSection.vue';
import { getMountingFunction } from '@ct/testUtils/Mount.ts';

const data = { value: 'v1', currency: 'EUR', provider: 'ProviderX', quality: 'Audited', comment: 'c1' };

const scenarios: { name: string; props: object; shown: string[]; hidden: string[] }[] = [
  {
    name: 'default data point shows Quality only',
    props: {},
    shown: ['Value', 'Quality', 'Document', 'Page(s)', 'Comment'],
    hidden: ['Currency', 'Provider'],
  },
  {
    name: 'currency data point additionally shows Currency',
    props: { isCurrencyDataPoint: true },
    shown: ['Currency', 'Quality'],
    hidden: ['Provider'],
  },
  {
    name: 'assurance data point shows Provider instead of Quality',
    props: { isAssuranceDataPoint: true },
    shown: ['Provider'],
    hidden: ['Quality', 'Currency'],
  },
];

describe('Component tests for JudgeDialogTopSection table rows', () => {
  scenarios.forEach(({ name, props, shown, hidden }) => {
    it(name, () => {
      getMountingFunction()(JudgeDialogTopSection, {
        props: { title: 'Original', acceptLabel: 'Accept', sectionType: 'original', data, ...props },
      });
      shown.forEach((label) => cy.contains('th', label).should('exist'));
      hidden.forEach((label) => cy.contains('th', label).should('not.exist'));
    });
  });
});
