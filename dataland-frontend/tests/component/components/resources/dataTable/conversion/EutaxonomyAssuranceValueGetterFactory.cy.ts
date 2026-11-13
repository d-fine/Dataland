import {
  MLDTDisplayComponentName,
  MLDTDisplayObjectForEmptyString,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { formatAssuranceForDataTable } from '@/components/resources/dataTable/conversion/EutaxonomyAssuranceValueGetterFactory';
import { wrapDisplayValueWithDatapointInformationByDataPoint } from '@/components/resources/dataTable/conversion/DataPoints';
import { AssuranceDataPointValueEnum } from '@clients/backend';

describe('Unit test for formatAssuranceForDataTable', () => {
  it('returns an empty string display object if the assurance is undefined', () => {
    expect(formatAssuranceForDataTable(undefined, 'Assurance')).to.deep.equal(MLDTDisplayObjectForEmptyString);
  });

  it('keeps the provider and the real comment for the modal and does not generate any text', () => {
    const result = formatAssuranceForDataTable(
      { value: AssuranceDataPointValueEnum.LimitedAssurance, provider: 'Some Provider', comment: 'A real comment' },
      'Assurance'
    );
    expect(result.displayComponentName).to.equal(MLDTDisplayComponentName.DataPointWrapperDisplayComponent);
    const displayValue = result.displayValue as Record<string, unknown>;
    expect(displayValue.provider).to.equal('Some Provider');
    expect(displayValue.comment).to.equal('A real comment');
    expect(displayValue.quality).to.equal(undefined);
    expect(displayValue.innerContents).to.deep.equal({
      displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: 'Limited Assurance',
    });
    expect(JSON.stringify(result)).not.to.contain('Assurance provided by');
  });

  it('keeps the provider even if neither comment nor data source exist', () => {
    const result = formatAssuranceForDataTable(
      { value: AssuranceDataPointValueEnum.None, provider: 'Only Provider' },
      'Assurance'
    );
    expect(result.displayComponentName).to.equal(MLDTDisplayComponentName.DataPointWrapperDisplayComponent);
    expect((result.displayValue as Record<string, unknown>).provider).to.equal('Only Provider');
  });

  it('handles legacy assurance data without provider and comment', () => {
    const result = formatAssuranceForDataTable({ value: AssuranceDataPointValueEnum.ReasonableAssurance }, 'Assurance');
    expect(result.displayComponentName).to.equal(MLDTDisplayComponentName.DataPointWrapperDisplayComponent);
    const displayValue = result.displayValue as Record<string, unknown>;
    expect(displayValue.provider).to.equal(undefined);
    expect(displayValue.comment).to.equal(undefined);
  });
});

describe('Unit test for wrapDisplayValueWithDatapointInformationByDataPoint with a provider', () => {
  it('keeps the provider of a stored data point JSON', () => {
    const inner = {
      displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
      displayValue: 'Limited Assurance',
    } as const;
    const result = wrapDisplayValueWithDatapointInformationByDataPoint(
      inner,
      'Assurance',
      JSON.stringify({ value: 'LimitedAssurance', provider: 'Some Provider', comment: 'A real comment' })
    );
    const displayValue = result.displayValue as Record<string, unknown>;
    expect(displayValue.provider).to.equal('Some Provider');
    expect(displayValue.comment).to.equal('A real comment');
  });
});
