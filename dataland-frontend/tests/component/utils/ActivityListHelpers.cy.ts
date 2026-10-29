import { kpiTypeFromDataPointTypeId } from '@/utils/ActivityListHelpers';
import type { MLDTConfig } from '@/components/resources/dataTable/MultiLayerDataTableConfiguration';
import { eutaxonomyNonFinancialsViewConfiguration } from '@/frameworks/custom/EuTaxoNonFinancialsStaticViewConfig';
import { eutaxonomyNonFinancials202673ViewConfiguration } from '@/frameworks/eutaxonomy-non-financials-2026-73/ViewConfig';

const activityComponentNames = new Set([
  'EligibleOrAlignedActivitiesFormField',
  'AlignedActivitiesFormField',
  'NonAlignedActivitiesFormField',
]);

/**
 * Collects the data point type ids of all activity list cells in a view configuration.
 * @param config the view configuration
 * @returns the data point type ids
 */
function collectActivityDataPointTypeIds<T>(config: MLDTConfig<T>): string[] {
  return config.flatMap((element) => {
    if (element.type === 'section') return collectActivityDataPointTypeIds(element.children);
    return activityComponentNames.has(element.uploadComponentName ?? '') && element.dataPointTypeId
      ? [element.dataPointTypeId]
      : [];
  });
}

describe('Unit test for ActivityListHelpers', () => {
  it('derives the KPI type from activity list data point type ids', () => {
    expect(kpiTypeFromDataPointTypeId('extendedEuTaxonomyAlignedActivitiesComponentRevenueAlignedActivities')).to.equal(
      'revenue'
    );
    expect(
      kpiTypeFromDataPointTypeId('extendedEuTaxonomyNonAlignedActivitiesComponentCapexNonAlignedActivities')
    ).to.equal('capex');
    expect(
      kpiTypeFromDataPointTypeId(
        'extendedEuTaxonomyEligibleOrAlignedActivitiesComponentOpexEligibleOrAlignedActivities'
      )
    ).to.equal('opex');
  });

  it('returns undefined for ids that are not the ones of activity lists', () => {
    const results = ['extendedDecimalCapexRelativeShareOfNuclearActivitiesInPercentAligned', '', undefined].map((id) =>
      kpiTypeFromDataPointTypeId(id)
    );
    expect(results).to.deep.equal([undefined, undefined, undefined]);
  });

  it('derives a KPI type for every activity list cell of the EU taxonomy non-financials view configurations', () => {
    const ids = [
      ...collectActivityDataPointTypeIds(eutaxonomyNonFinancialsViewConfiguration),
      ...collectActivityDataPointTypeIds(eutaxonomyNonFinancials202673ViewConfiguration),
    ];
    expect(ids).to.have.length(9);
    const kpiTypes = ids.map((id) => kpiTypeFromDataPointTypeId(id));
    expect(kpiTypes).not.to.include(undefined);
    expect(new Set(kpiTypes)).to.deep.equal(new Set(['revenue', 'capex', 'opex']));
  });
});
