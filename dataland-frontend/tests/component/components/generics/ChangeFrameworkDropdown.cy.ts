import { type BasicDataDimensions, DataTypeEnum } from '@clients/backend';
import ChangeFrameworkDropdown from '@/components/generics/ChangeFrameworkDropdown.vue';
import { mount } from 'cypress/vue';

describe('Component test for ChangeFrameworkDropdown', () => {
  const companyId: string = 'dummy-companyId';
  const dataType: string = 'Documents';
  const listOfDataMetaInfo: Array<BasicDataDimensions> = [
    {
      companyId: companyId,
      dataType: DataTypeEnum.Lksg,
      reportingPeriod: '2022',
    },
    {
      companyId: companyId,
      dataType: DataTypeEnum.Sfdr,
      reportingPeriod: '2022',
    },
    {
      companyId: companyId,
      dataType: DataTypeEnum.Lksg,
      reportingPeriod: '2022',
    },
    {
      companyId: companyId,
      dataType: DataTypeEnum.Sfdr,
      reportingPeriod: '2022',
    },
  ];

  /**
   * counts the number of different entries in the dropdown component depending on the given array of
   * BasicDataDimensions.
   * @param dataMetaInfoArray list of available data dimensions
   * @returns the number of distinct framework types among the data dimensions plus 1 because
   * the dropdown also has an entry linking to the documents page
   */
  function countDropdownEntries(dataMetaInfoArray: Array<BasicDataDimensions>): number {
    const uniqueDataTypes = new Set<string>();

    for (const item of dataMetaInfoArray) {
      uniqueDataTypes.add(item.dataType);
    }
    return uniqueDataTypes.size + 1;
  }

  it('Check if dropdown data is set properly, all options displayed with no duplicates', () => {
    const numberOfEntries: number = countDropdownEntries(listOfDataMetaInfo);

    mount(ChangeFrameworkDropdown, {
      props: {
        companyId: companyId,
        dataType: dataType,
        availableDataDimensions: listOfDataMetaInfo,
      },
    }).then(() => {
      // Dropdown is closed
      cy.get('[data-test="chooseFrameworkList"]').should('not.exist');

      // Open dropdown
      cy.get('[data-test="chooseFrameworkDropdown"]').click();
      cy.get('[data-test="chooseFrameworkList"]').should('be.visible');
      cy.get('[data-test="chooseFrameworkList"]').children().should('have.length', numberOfEntries);

      // Check hyperlinks
      cy.get('[data-test="chooseFrameworkList"]')
        .contains('LkSG')
        .should('have.attr', 'href', '/companies/dummy-companyId/frameworks/lksg');
      cy.get('[data-test="chooseFrameworkList"]')
        .contains('SFDR')
        .should('have.attr', 'href', '/companies/dummy-companyId/frameworks/sfdr');
      cy.get('[data-test="chooseFrameworkList"]')
        .contains('Documents')
        .should('have.attr', 'href', '/companies/dummy-companyId/documents');

      // Close dropdown
      cy.get('[data-test="chooseFrameworkDropdown"]').click();
      cy.get('[data-test="chooseFrameworkList"]').should('not.exist');
    });
  });
});
