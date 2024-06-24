// @ts-nocheck
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import {
  type DataAndMetaInformationLksgData,
  type DataMetaInformation,
  DataTypeEnum,
  type LksgData,
  type LksgProductionSite,
  QaStatus,
} from '@clients/backend';
import { type ReportingPeriodOfDataSetWithId, sortReportingPeriodsToDisplayAsColumns } from '@/utils/DataTableDisplay';
import { lksgViewConfiguration } from '@/frameworks/lksg/ViewConfig';
import {
  mountMLDTFrameworkPanelFromFakeFixture,
  mountMLDTFrameworkPanel,
} from '@ct/testUtils/MultiLayerDataTableComponentTestUtils';

import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation';
import {
  getCellValueContainerAndCheckIconForHiddenDisplay,
  getCellValueContainer,
  getSectionHead,
} from '@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils';

describe('Component test for the LksgPanel', () => {
  let preparedFixtures: Array<FixtureData<LksgData>>;

  before(function () {
    cy.fixture('CompanyInformationWithLksgPreparedFixtures').then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
    });
  });

  it('Should be able to handle null values in a Lksg dataset and display rows for those values', () => {
    const preparedFixture = getPreparedFixture('lksg-almost-only-nulls', preparedFixtures);
    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.Lksg, lksgViewConfiguration, [preparedFixture]);
    getCellValueContainer('Data Date').should('contain.text', '1999-12-24');
    getCellValueContainer('Industry').should('exist');
  });

  it('Check that the Master Data section is auto-expanded on page load and is decollapsed', () => {
    const preparedFixture = getPreparedFixture('one-lksg-data-set-with-two-production-sites', preparedFixtures);
    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.Lksg, lksgViewConfiguration, [preparedFixture]);
    const lksgData = preparedFixture.t;

    getCellValueContainer('Data Date')
      .should('contain.text', lksgData.general.masterData.dataDate)
      .should('be.visible');
    getSectionHead('Master Data').should('have.attr', 'data-section-expanded', 'true');
    getCellValueContainer('Data Date', 0, false).should('be.visible');
    getSectionHead('Master Data').should('have.attr', 'data-section-expanded', 'true');
    getCellValueContainer('Data Date')
      .should('contain.text', lksgData.general.masterData.dataDate)
      .should('be.visible');
  });

  it('Validate that certificate links are displayed correctly', () => {
    const preparedFixture = getPreparedFixture('one-lksg-data-set-with-two-production-sites', preparedFixtures);
    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.Lksg, lksgViewConfiguration, [preparedFixture]);

    getSectionHead('Governance').should('have.attr', 'data-section-expanded', 'true');
    getSectionHead('Certifications, policies and responsibilities')
      .should('have.attr', 'data-section-expanded', 'true')
      .click();
  });

  it('Validate that the list of production sites is displayed modal is displayed correctly', () => {
    const preparedFixture = getPreparedFixture('one-lksg-data-set-with-two-production-sites', preparedFixtures);
    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.Lksg, lksgViewConfiguration, [preparedFixture]);
    const lksgData = preparedFixture.t;
    const reportingPeriod = preparedFixture.reportingPeriod;
    getCellValueContainer('Data Date')
      .should('contain.text', lksgData.general.masterData.dataDate)
      .should('be.visible');
    cy.get(`span.p-column-title`).should('contain.text', reportingPeriod.substring(0, 4));
    getSectionHead('Production-specific').should('have.attr', 'data-section-expanded', 'true');
    getCellValueContainer('List Of Production Sites').contains('a').should('be.visible').click();
    lksgData.general.productionSpecific!.listOfProductionSites!.forEach((productionSite: LksgProductionSite) => {
      if (productionSite.addressOfProductionSite?.streetAndHouseNumber) {
        cy.get('tbody.p-datatable-tbody p').contains(productionSite.addressOfProductionSite.streetAndHouseNumber);
      }
    });
    cy.get('div.p-dialog-mask').click({ force: true });
  });

  it('Validate that the procurement category modal is displayed and contains the correct headers', () => {
    const preparedFixture = getPreparedFixture('lksg-with-procurement-categories', preparedFixtures);
    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.Lksg, lksgViewConfiguration, [preparedFixture]);

    getSectionHead('Production-specific - Own Operations').should('have.attr', 'data-section-expanded', 'true');
    getCellValueContainer('Procurement Categories').find('a').should('be.visible').click();

    cy.get('div.p-dialog').within(() => {
      cy.get('th').eq(0).should('have.text', 'Procurement Category');
      cy.get('th').eq(1).should('have.text', 'Procured Products/Services');
      cy.get('th').eq(2).should('have.text', 'Number of Direct Suppliers and Countries');
      cy.get('th').eq(3).should('have.text', 'Order Volume');
    });
  });

  it('Validate that the subcategories countries modal is displayed correctly and contains the correct headers', () => {
    const preparedFixture = getPreparedFixture('lksg-with-subcontracting-countries', preparedFixtures);
    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.Lksg, lksgViewConfiguration, [preparedFixture]);

    getSectionHead('Production-specific').should('have.attr', 'data-section-expanded', 'true');
    getCellValueContainer('Subcontracting Companies Countries').find('a').should('be.visible').click();

    cy.get('div.p-dialog').within(() => {
      cy.get('th').eq(0).should('have.text', 'Country');
      cy.get('th').eq(1).should('have.text', 'Industries');
      cy.get("td:contains('United Kingdom')").should('exist');
      cy.get("td:contains('Germany')")
        .siblings('td')
        .find('li')
        .should('have.length', 2)
        .first()
        .should('contain.text', 'A - AGRICULTURE');
    });
  });

  it('Validate that show-if hidden fields are displayed and highlighted in review mode', () => {
    const preparedFixture = getPreparedFixture(
      'lksg-not-a-manufacturing-company-but-has-production-sites',
      preparedFixtures
    );
    mountMLDTFrameworkPanelFromFakeFixture(
      DataTypeEnum.Lksg,
      lksgViewConfiguration,
      [preparedFixture],
      'mock-company-id',
      true
    );

    getSectionHead('Production-specific').should('have.attr', 'data-section-expanded', 'true');

    getCellValueContainer('Manufacturing Company').should('have.text', 'No');
    getCellValueContainer('List Of Production Sites').should('be.visible');
    getCellValueContainerAndCheckIconForHiddenDisplay('List Of Production Sites', true);
  });

  /**
   * This functions imitates an api response of the /data/lksg/companies/mock-company-id endpoint
   * to include 6 active Lksg datasets from different years to test the simultaneous display of multiple Lksg
   * datasets (constructed datasets range from 2023 to 2028)
   * @param baseDataset the lksg dataset used as a basis for constructing the 6 mocked ones
   * @returns a mocked api response
   */
  function constructCompanyApiResponseForLksgForSixYears(baseDataset: LksgData): DataAndMetaInformation<LksgData>[] {
    const lksgDatasets: DataAndMetaInformationLksgData[] = [];
    for (let i = 0; i < 6; i++) {
      const reportingYear = 2023 + i;
      const reportingDate = `${reportingYear}-01-01`;
      const lksgData = structuredClone(baseDataset);
      lksgData.general.masterData.dataDate = reportingDate;
      const metaData: DataMetaInformation = {
        dataId: `dataset-${i}`,
        reportingPeriod: reportingYear.toString(),
        qaStatus: QaStatus.Accepted,
        currentlyActive: true,
        dataType: DataTypeEnum.Lksg,
        companyId: 'mock-company-id',
        uploadTime: 0,
        uploaderUserId: 'mock-uploader-id',
      };

      lksgDatasets.push({
        metaInfo: metaData,
        data: lksgData,
      });
    }
    return lksgDatasets;
  }

  it('Check Lksg view page for company with six Lksg data sets reported in different years ', () => {
    const preparedFixture = getPreparedFixture('six-lksg-data-sets-in-different-years', preparedFixtures);
    const mockedData = constructCompanyApiResponseForLksgForSixYears(preparedFixture.t);
    mountMLDTFrameworkPanel(DataTypeEnum.Lksg, lksgViewConfiguration, mockedData);

    cy.get('table').find(`tr:contains("Data Date")`).find(`span`).eq(6).get('span').contains('2023');

    for (let indexOfColumn = 1; indexOfColumn <= 6; indexOfColumn++) {
      cy.get(`span.p-column-title`)
        .eq(indexOfColumn)
        .should('contain.text', (2029 - indexOfColumn).toString());
    }
  });

  it('Unit test for sortReportingPeriodsToDisplayAsColumns', () => {
    const firstYearObject = { dataId: '5', reportingPeriod: '2022' };
    const secondYearObject = { dataId: '2', reportingPeriod: '2020' };
    const firstOtherObject = { dataId: '3', reportingPeriod: 'Q2-2020' };
    const secondOtherObject = { dataId: '6', reportingPeriod: 'Q3-2020' };
    const shouldSwapList = [false, true]; //Apparently Typescript doesn't like type conversions, so input is direct.
    for (let i = 0; i < 2; i++) {
      expect(
        swapAndSortReportingPeriodsToDisplayAsColumns([secondYearObject, firstYearObject], shouldSwapList[i])
      ).to.deep.equal([firstYearObject, secondYearObject]);

      expect(
        swapAndSortReportingPeriodsToDisplayAsColumns([secondOtherObject, firstOtherObject], shouldSwapList[i])
      ).to.deep.equal([firstOtherObject, secondOtherObject]);
    }
    expect(
      sortReportingPeriodsToDisplayAsColumns([firstYearObject, secondOtherObject, firstOtherObject])
    ).to.deep.equal([firstYearObject, firstOtherObject, secondOtherObject]);
  });
});

/**
 * The function swaps and sorts the data date columns to display
 * @param listOfDataDateToDisplayAsColumns is a list of data dates to display
 * @param shouldSwap boolean which determines if the list should be swapped or not
 * @returns the sorted reporting periods to display based on the given list of data dates
 */
function swapAndSortReportingPeriodsToDisplayAsColumns(
  listOfDataDateToDisplayAsColumns: ReportingPeriodOfDataSetWithId[],
  shouldSwap = false
): ReportingPeriodOfDataSetWithId[] {
  let swappedList: ReportingPeriodOfDataSetWithId[];
  if (shouldSwap && listOfDataDateToDisplayAsColumns.length == 2) {
    swappedList = listOfDataDateToDisplayAsColumns.slice();
    swappedList[0] = listOfDataDateToDisplayAsColumns[1];
    swappedList[1] = listOfDataDateToDisplayAsColumns[0];
    listOfDataDateToDisplayAsColumns = swappedList.slice();
  }
  return sortReportingPeriodsToDisplayAsColumns(listOfDataDateToDisplayAsColumns);
}
