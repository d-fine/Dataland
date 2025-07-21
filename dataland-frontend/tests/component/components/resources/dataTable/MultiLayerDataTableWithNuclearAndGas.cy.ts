import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { DataTypeEnum, type NuclearAndGasData } from '@clients/backend';

import {
  getCellValueContainer,
  getSectionHead,
} from '@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils';
import { mountMLDTFrameworkPanelFromFakeFixture } from '@ct/testUtils/MultiLayerDataTableComponentTestUtils';
import { nuclearAndGasViewConfiguration } from '@/frameworks/nuclear-and-gas/ViewConfig';
import { ONLY_AUXILIARY_DATA_PROVIDED } from '@/utils/Constants.ts';

/**
 * Tests whether values in Nuclear and Gas tables are equal to their expected value
 * @param fixture the fake fixture that is used for the expected value
 * @param sectionHead the section header for the tested table
 * @param cellValueContainer the name of the tested table
 * @param activity the activity in the tested table
 * @param expectedValue the value that is expected in the test
 */
function testTableValueNuclearAndGas(
  fixture: FixtureData<NuclearAndGasData>,
  sectionHead: string,
  cellValueContainer: string,
  activity: string,
  expectedValue: string
): void {
  mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.NuclearAndGas, nuclearAndGasViewConfiguration, [fixture]);
  getSectionHead(sectionHead).should('exist');
  getCellValueContainer(cellValueContainer).contains('Show').click();

  cy.get('td')
    .contains(activity)
    .then(($td) => {
      cy.wrap($td)
        .next()
        .invoke('text')
        .then((nextText) => {
          expect(nextText.trim()).to.equal(expectedValue);
        });
    });
}

describe('Component Test for the nuclear and gas view Page with its components', () => {
  let preparedFixturesNuG: Array<FixtureData<NuclearAndGasData>>;

  before(function () {
    cy.fixture('CompanyInformationWithNuclearAndGasPreparedFixtures.json').then(function (jsonContent) {
      preparedFixturesNuG = jsonContent as Array<FixtureData<NuclearAndGasData>>;
    });
  });

  it('Check that on the nuclear and gas view page the extended nuclearAndGas components work properly for aligned activities', () => {
    const preparedFixture = getPreparedFixture(
      'All-fields-defined-for-EU-NuclearAndGas-Framework',
      preparedFixturesNuG
    );
    testTableValueNuclearAndGas(
      preparedFixture,
      'Taxonomy-aligned (numerator)',
      'Nuclear and Gas Taxonomy-aligned Revenue (numerator)',
      'Proportion of taxonomy-aligned economic activity referred to in Section 4.26 of Annexes I and II to Delegated Regulation 2021/2139 in the numerator of the applicable KPI.',
      preparedFixture.t.general?.taxonomyAlignedNumerator?.nuclearAndGasTaxonomyAlignedRevenueNumerator?.value
        ?.taxonomyAlignedShareNumeratorNAndG426?.mitigationAndAdaptation + ' %'
    );
  });

  it('Check that on the nuclear and gas view page the extended nuclearAndGas components work properly for non aligned activities', () => {
    const preparedFixture = getPreparedFixture(
      'All-fields-defined-for-EU-NuclearAndGas-Framework',
      preparedFixturesNuG
    );
    testTableValueNuclearAndGas(
      preparedFixture,
      'Taxonomy-non-eligible',
      'Nuclear and Gas Taxonomy-non-eligible Revenue',
      'Proportion of economic activity referred to in row 1 of Template 1 that is taxonomy-non-eligible in accordance with Section 4.26 of Annexes I and II to Delegated Regulation 2021/2139 in the denominator of the applicable KPI.',
      preparedFixture.t.general?.taxonomyNonEligible?.nuclearAndGasTaxonomyNonEligibleRevenue?.value
        ?.taxonomyNonEligibleShareNAndG426 + ' %'
    );
  });

  it('Check that on the nuclear and gas view page the auxiliary data works as expected', () => {
    const preparedFixture = getPreparedFixture(
      'All-fields-defined-for-EU-NuclearAndGas-Framework',
      preparedFixturesNuG
    );

    if (preparedFixture.t.general?.taxonomyNonEligible?.nuclearAndGasTaxonomyNonEligibleCapex) {
      preparedFixture.t.general.taxonomyNonEligible.nuclearAndGasTaxonomyNonEligibleCapex.value = null;
    }

    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.NuclearAndGas, nuclearAndGasViewConfiguration, [
      preparedFixture,
    ]);
    getSectionHead('Taxonomy-non-eligible').should('exist');
    getCellValueContainer('Nuclear and Gas Taxonomy-non-eligible CapEx')
      .contains(ONLY_AUXILIARY_DATA_PROVIDED)
      .should('exist');
  });
});
