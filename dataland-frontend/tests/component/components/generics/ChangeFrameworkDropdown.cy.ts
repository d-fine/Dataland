import { type DataMetaInformation, DataTypeEnum, QaStatus } from '@clients/backend';
import ChangeFrameworkDropdown from '@/components/generics/ChangeFrameworkDropdown.vue';
import { mount } from 'cypress/vue';
import { type DataTypeEnumAndDocumentsEntry } from '@/types/DataTypeEnumAndDocumentsEntry.ts';

describe('Component test for ChangeFrameworkDropdown', () => {
  const companyId: string = 'dummy-companyId';
  const dataType: DataTypeEnumAndDocumentsEntry = 'Documents';
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
        listOfDataMetaInfo: listOfDataMetaInfo,
      },
    }).then(() => {
      cy.get('[data-test="chooseFrameworkDropdown"]').should('exist').click();
      cy.get('[data-test="chooseFrameworkDropdown"]').children().should('have.length', numberOfEntries);
    });

    // check for children
  });
  /*it('Check if clicking dropdown option results in correct path', () => {

    })*/
});
