// @ts-nocheck
import CreateP2pDataset from '@/components/forms/CreateP2pDataset.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { type CompanyAssociatedDataPathwaysToParisData } from '@clients/backend';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { submitButton } from '@sharedUtils/components/SubmitButton';

describe('Component tests for the CreateP2pDataset that test dependent fields', () => {
  /**
   * Picks the 13th day of the next month in the datepicker
   */
  function pickDate(): void {
    cy.get('div[data-test="dataDate"]').find('button').click();
    cy.get('div.p-datepicker').find('button[aria-label="Next Month"]').click();
    cy.get('div.p-datepicker').find('span:contains("13")').click();
  }

  /**
   * Opens the sector-multiselect and clicks on "Automotive"
   * @param sector is the label of the sector in the dropdown
   */
  function clickOnSectorInSectorsDropdown(sector: string): void {
    cy.get('div[data-test="sectors"] div.p-multiselect').should('exist').click();
    cy.contains('span', sector).should('exist').click();
    cy.get('div[data-test="sectors"] div.p-multiselect').should('exist').click();
  }

  /**
   * Assures that when scrolling down on the upload page the side bar sticks at the top of the viewport.
   */
  function checkStickinessOfSideBar(): void {
    cy.scrollTo('bottom');
    cy.get("[data-test='submitSideBar']").should('have.css', 'position', 'fixed').and('have.css', 'top', '60px');
    cy.scrollTo('top');
    cy.get("[data-test='submitSideBar']").should('have.css', 'position', 'relative').and('have.css', 'top', '0px');
  }

  it('On the upload page, ensure that sectors can be selected and deselected and the submit looks as expected', () => {
    cy.mountWithPlugins(CreateP2pDataset, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      submitButton.buttonAppearsDisabled();
      checkStickinessOfSideBar();

      pickDate();

      cy.get('button[data-test="submitButton"]').should('have.class', 'button-disabled').click();
      cy.get('div[data-test="productionSiteEnergyConsumptionInMWh"]').should('not.exist');
      cy.contains('span', 'AUTOMOTIVE').should('not.exist');

      clickOnSectorInSectorsDropdown('Automotive');

      cy.get('button[data-test="submitButton"]').should('not.have.class', 'button-disabled');
      cy.contains('span', 'AUTOMOTIVE').should('exist');
      cy.get('div[data-test="productionSiteEnergyConsumptionInMWh"]').should('exist');

      clickOnSectorInSectorsDropdown('Automotive');

      cy.get('button[data-test="submitButton"]').should('have.class', 'button-disabled').click();
      cy.get('div[data-test="productionSiteEnergyConsumptionInMWh"]').should('not.exist');
      cy.contains('span', 'AUTOMOTIVE').should('not.exist');

      clickOnSectorInSectorsDropdown('Steel');

      submitButton.buttonAppearsEnabled();
      cy.contains('span', 'STEEL').should('exist');

      cy.get('div[data-test="emissionIntensityOfElectricityInCorrespondingUnit"] input').type('222');

      cy.intercept('POST', '**/api/data/p2p*', (request) => {
        request.reply(200, {});
      }).as('postP2pData');
      submitButton.clickButton();
      cy.wait('@postP2pData').then((interception) => {
        const postedObject = interception.request.body as CompanyAssociatedDataPathwaysToParisData;
        const postedP2pDataset = postedObject.data;
        const emissionIntensityOfElectricityInCorrespondingUnit =
          assertDefined(postedP2pDataset).steel?.energy?.emissionIntensityOfElectricityInCorrespondingUnit;
        expect(emissionIntensityOfElectricityInCorrespondingUnit).to.equal('222');
        const automotive = postedP2pDataset.automotive;
        expect(automotive).to.be.undefined;
      });
    });
  });

  it('Open upload page prefilled and assure that only the sections that the dataset holds are displayed', () => {
    cy.mountWithPlugins(CreateP2pDataset, {
      keycloak: minimalKeycloakMock({}),
      data() {
        return {
          companyAssociatedP2pData: {
            reportingPeriod: '2020',
            data: { general: { general: { dataDate: '2021-04-30', sectors: ['Automotive', 'Steel'] } } },
          },
        };
      },
    }).then(() => {
      cy.contains('span', 'AUTOMOTIVE').should('exist');
      cy.get('div[data-test="productionSiteEnergyConsumptionInMWh"]').should('exist');

      cy.contains('span', 'STEEL').should('exist');
      cy.get('div[data-test="emissionIntensityOfElectricityInCorrespondingUnit"]').should('exist');

      submitButton.buttonAppearsEnabled();
    });
  });

  it('In the freight transport by road sector, ensure that the driveMixPerFleetSegment field works as expected', () => {
    cy.mountWithPlugins(CreateP2pDataset, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      clickOnSectorInSectorsDropdown('Freight Transport by Road');
      cy.get('div[data-test="driveMixPerFleetSegment"]')
        .should('exist')
        .find('div[data-test="dataPointToggleButton"]')
        .eq(0)
        .click();
      cy.get('[name="driveMixPerFleetSegmentInPercent"]').type('133').blur();
      cy.get('.formkit-message').should('contain.text', 'must be between 0 and 100');
      cy.get('[name="driveMixPerFleetSegmentInPercent"]').clear().type('22');
      cy.get('[name="totalAmountOfVehicles"]').type('5000');
      cy.get('div[data-test="dataPointToggleButton"]').eq(1).click();
      cy.get('div[data-test="dataPointToggleButton"]').eq(1).click();
    });
  });
});
