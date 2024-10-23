import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { DataTypeEnum, type NuclearAndGasData } from '@clients/backend';

import {
  getCellValueContainer,
  getSectionHead,
} from '@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils';
import { mountMLDTFrameworkPanelFromFakeFixture } from '@ct/testUtils/MultiLayerDataTableComponentTestUtils';
import { nuclearAndGasViewConfiguration } from '../../../../../src/frameworks/nuclear-and-gas/ViewConfig';

describe('Component Test for the nuclear and gas view Page with its components', () => {
  let preparedFixtures: Array<FixtureData<NuclearAndGasData>>;
  before(function () {
    cy.fixture('CompanyInformationWithNuclearAndGasPreparedFixtures.json').then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<NuclearAndGasData>>;
    });
  });

  it('Check that on the nuclear and gas view Page the list base data point component works properly', () => {
    const preparedFixture = getPreparedFixture('All-fields-defined-for-EU-NuclearAndGas-Framework', preparedFixtures);
    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.NuclearAndGas, nuclearAndGasViewConfiguration, [
      preparedFixture,
    ]);
    getSectionHead('Taxonomy-aligned (numerator)').should('exist');
    getCellValueContainer('Nuclear and Gas Taxonomy-aligned Revenue (numerator)').contains('Show').click();

    cy.get('td')
      .contains(
        'Proportion of taxonomy-aligned economic activity referred to in Section 4.26 of Annexes I and II to Delegated Regulation 2021/2139 in the numerator of the applicable KPI.'
      )
      .then(($td) => {
        cy.wrap($td)
          .next()
          .invoke('text')
          .then((nextText) => {
            expect(nextText.trim()).to.equal(
              preparedFixture.t.general?.taxonomyAlignedNumerator?.nuclearAndGasTaxonomyAlignedRevenueNumerator?.value
                ?.taxonomyAlignedShareNumeratorNAndG426?.mitigation + ' %'
            );
          });
      });
  });
});
