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

describe('Component tests for the table rows of JudgeDialogTopSection', () => {
  it('shows provider but no quality row for assurance data points', () => {
    mountTopSection({
      data: { value: 'LimitedAssurance', provider: 'ACME Auditors', comment: 'A real comment' },
      isAssuranceDataPoint: true,
    });

    cy.contains('th', 'Provider').parent('tr').should('contain.text', 'ACME Auditors');
    cy.contains('th', 'Comment').parent('tr').should('contain.text', 'A real comment');
    cy.contains('th', 'Quality').should('not.exist');
  });

  it('shows quality but no provider row for other data points', () => {
    mountTopSection({ data: { value: '1', quality: 'Audited' } });

    cy.contains('th', 'Quality').parent('tr').should('contain.text', 'Audited');
    cy.contains('th', 'Provider').should('not.exist');
  });

  it('shows the currency row only for currency data points', () => {
    mountTopSection({ data: { value: '1', currency: 'EUR' }, isCurrencyDataPoint: true });
    cy.contains('th', 'Currency').parent('tr').should('contain.text', 'EUR');
  });

  it('does not show a currency row for non-currency data points', () => {
    mountTopSection({ data: { value: '1', currency: 'EUR' } });
    cy.contains('th', 'Currency').should('not.exist');
  });

  it('shows the file name of the document if available', () => {
    mountTopSection({ data: { value: '1', dataSource: { fileName: 'report.pdf', fileReference: 'abc123' } } });
    cy.contains('th', 'Document').parent('tr').should('contain.text', 'report.pdf').and('not.contain.text', 'abc123');
  });

  it('falls back to the file reference if the file name is missing', () => {
    mountTopSection({ data: { value: '1', dataSource: { fileReference: 'abc123' } } });
    cy.contains('th', 'Document').parent('tr').should('contain.text', 'abc123');
  });

  it('shows a placeholder for missing values', () => {
    mountTopSection({ data: { value: 'LimitedAssurance' }, isAssuranceDataPoint: true });
    cy.contains('th', 'Provider').parent('tr').should('contain.text', '—');
    cy.contains('th', 'Comment').parent('tr').should('contain.text', '—');
  });
});
