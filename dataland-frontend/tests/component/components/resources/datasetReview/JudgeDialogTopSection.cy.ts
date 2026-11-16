import JudgeDialogTopSection from '@/components/resources/datasetReview/JudgeDialogTopSection.vue';
import { getMountingFunction } from '@ct/testUtils/Mount.ts';
import type { ParsedSingleDataPoint } from '@/utils/DataPoint.ts';

type TopSectionProps = {
  data: ParsedSingleDataPoint | null;
  isCurrencyDataPoint?: boolean;
  isAssuranceDataPoint?: boolean;
};

/**
 * Mounts the JudgeDialogTopSection with the given props.
 * @param props the props that differ from the defaults
 */
function mountTopSection(props: TopSectionProps): void {
  getMountingFunction()(JudgeDialogTopSection, {
    props: { title: 'Original', acceptLabel: 'Accept', sectionType: 'original', ...props },
  });
}

/**
 * @param label the row header to look for
 * @returns the table row with the given header
 */
function row(label: string): Cypress.Chainable {
  return cy.contains('th', label).parent('tr');
}

describe('Component tests for the table rows of JudgeDialogTopSection', () => {
  it('shows provider but no quality row for assurance data points', () => {
    mountTopSection({
      data: { value: 'LimitedAssurance', provider: 'ACME Auditors', comment: 'A real comment' },
      isAssuranceDataPoint: true,
    });

    row('Provider').should('contain.text', 'ACME Auditors');
    row('Comment').should('contain.text', 'A real comment');
    cy.contains('th', 'Quality').should('not.exist');
  });

  it('shows quality but no provider row for other data points', () => {
    mountTopSection({ data: { value: '1', quality: 'Audited' } });

    row('Quality').should('contain.text', 'Audited');
    cy.contains('th', 'Provider').should('not.exist');
  });

  it('shows the currency row only for currency data points', () => {
    mountTopSection({ data: { value: '1', currency: 'EUR' }, isCurrencyDataPoint: true });
    row('Currency').should('contain.text', 'EUR');
  });

  it('does not show a currency row for non-currency data points', () => {
    mountTopSection({ data: { value: '1', currency: 'EUR' } });
    cy.contains('th', 'Currency').should('not.exist');
  });

  it('shows the file name of the document if available', () => {
    mountTopSection({ data: { value: '1', dataSource: { fileName: 'report.pdf', fileReference: 'abc123' } } });
    row('Document').should('contain.text', 'report.pdf').and('not.contain.text', 'abc123');
  });

  it('falls back to the file reference if the file name is missing', () => {
    mountTopSection({ data: { value: '1', dataSource: { fileReference: 'abc123' } } });
    row('Document').should('contain.text', 'abc123');
  });

  it('shows a placeholder for missing values', () => {
    mountTopSection({ data: { value: 'LimitedAssurance' }, isAssuranceDataPoint: true });
    row('Provider').should('contain.text', '—');
    row('Comment').should('contain.text', '—');
  });
});
