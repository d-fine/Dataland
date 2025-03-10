import { type DataMetaInformation, DataTypeEnum, QaStatus } from '@clients/backend';
import ChangeFrameworkDropdown from '@/components/generics/ChangeFrameworkDropdown.vue';
import { mount } from 'cypress/vue';

describe('Component test for ChangeFrameworkDropdown', () => {
  const companyId: string = 'dummy-companyId';
  const dataType: string = 'Documents';
  const listOfDataMetaInfo: Array<DataMetaInformation> = [
    {
      dataId: 'dummy-dataId-1',
      companyId: companyId,
      dataType: DataTypeEnum.Lksg,
      uploadTime: 123,
      reportingPeriod: '2022',
      currentlyActive: true,
      qaStatus: QaStatus.Accepted,
    },
    {
      dataId: 'dummy-dataId-2',
      companyId: companyId,
      dataType: DataTypeEnum.Sfdr,
      uploadTime: 123,
      reportingPeriod: '2022',
      currentlyActive: true,
      qaStatus: QaStatus.Accepted,
    },
    {
      dataId: 'dummy-dataId-3',
      companyId: companyId,
      dataType: DataTypeEnum.Lksg,
      uploadTime: 123,
      reportingPeriod: '2022',
      currentlyActive: true,
      qaStatus: QaStatus.Accepted,
    },
    {
      dataId: 'dummy-dataId-4',
      companyId: companyId,
      dataType: DataTypeEnum.AdditionalCompanyInformation,
      uploadTime: 123,
      reportingPeriod: '2022',
      currentlyActive: true,
      qaStatus: QaStatus.Accepted,
    },
  ];

  /**
   * counts the entries of different DataTypeEnum plus 1 for the additional documents entry
   * @param dataMetaInfoArray list of metadata information to be counted
   */
  function countDataTypesInArray(dataMetaInfoArray: Array<DataMetaInformation>): number {
    const uniqueDataTypes = new Set<string>();

    dataMetaInfoArray.forEach((item) => {
      uniqueDataTypes.add(item.dataType);
    });
    return uniqueDataTypes.size + 1; // for the added 'documents' entry
  }

  it('Check if dropdown data is set properly, all options displayed with no duplicates', () => {
    const numberOfEntries: number = countDataTypesInArray(listOfDataMetaInfo);

    mount(ChangeFrameworkDropdown, {
      props: {
        companyID: companyId,
        dataType: dataType,
        listOfMetaInfo: listOfDataMetaInfo,
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
        .contains('Additional Company Information')
        .should('have.attr', 'href', '/companies/dummy-companyId/frameworks/additional-company-information');
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
