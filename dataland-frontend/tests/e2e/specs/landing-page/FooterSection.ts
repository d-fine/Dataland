import { describeIf } from '@e2e/support/TestUtility';
import { searchBasicCompanyInformationForDataType } from '@e2e/utils/GeneralApiUtils';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { reader_name, reader_pw } from '@e2e/utils/Cypress';
import { checkFooter } from '@sharedUtils/ElementChecks';
import { PUBLIC_FRAMEWORKS, PRIVATE_FRAMEWORKS } from '@/utils/Constants';
import { type DataTypeEnum } from '@clients/backend';

describeIf(
  'As a user, I expect the footer section to be present and contain relevant legal links for public frameworks',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
    onlyExecuteWhenEurodatIsLive: false,
  },
  () => {
    describe('Checks that the footer section is present on many pages', () => {
      beforeEach(() => {
        cy.ensureLoggedIn();
      });

      it(`Checks that the footer is present on /companies`, () => {
        cy.visitAndCheckAppMount('/companies');
        checkFooter();
      });
      PUBLIC_FRAMEWORKS.forEach((framework) => {
        checkFooterComponent(framework);
      });
    });
  }
);
describeIf(
  'As a user, I expect the footer section to be present and contain relevant legal links for private frameworks',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
    onlyExecuteWhenEurodatIsLive: true,
  },
  () => {
    describe('Checks that the footer section is present on many pages', () => {
      beforeEach(() => {
        cy.ensureLoggedIn();
      });

      it(`Checks that the footer is present on /companies`, () => {
        cy.visitAndCheckAppMount('/companies');
        checkFooter();
      });
      PRIVATE_FRAMEWORKS.forEach((framework) => {
        checkFooterComponent(framework);
      });
    });
  }
);

/**
 * This method visits a dataset page for a given framework and checks if the footer is visible
 * @param framework of the dataset for which the footer should be checked
 */
function checkFooterComponent(framework: DataTypeEnum): void {
  it(`Checks that the footer is present on ${framework}`, () => {
    getKeycloakToken(reader_name, reader_pw).then((token) => {
      cy.browserThen(searchBasicCompanyInformationForDataType(token, framework)).then((basicCompanyInformations) => {
        const companyId = basicCompanyInformations[0].companyId;
        cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/${framework}`);
        checkFooter();
      });
    });
  });
}
