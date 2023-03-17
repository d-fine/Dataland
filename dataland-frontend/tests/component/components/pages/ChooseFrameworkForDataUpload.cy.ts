import ChooseFrameworkForDataUpload from "@/components/pages/ChooseFrameworkForDataUpload.vue";
import { shallowMount } from "@vue/test-utils";
import { DataMetaInformation, DataTypeEnum } from "@clients/backend";

describe("Component tests for the ChooseFrameworkForDataUpload page", () => {
  const wrapper = shallowMount(ChooseFrameworkForDataUpload, {
    global: {
      provide: {
        authenticated: true,
        getKeycloakPromise() {
          return Promise.resolve({
            authenticated: true,
            updateToken: () => undefined,
          });
        },
      },
    },
  });

  it("Unit tests for getMetaInfoAboutAllDataSetsForCurrentCompany", () => {
    const dataMetaInfo = { dataType: DataTypeEnum.Lksg, uploadTime: 0, reportingPeriod: "2023" } as DataMetaInformation;
    cy.intercept("**/api/metadata*", { statusCode: 200, body: [dataMetaInfo] }).then(() => {
      void (wrapper.vm.getMetaInfoAboutAllDataSetsForCurrentCompany as () => Promise<void>)().then(() => {
        expect(wrapper.vm.mapOfDataTypeToListOfDataMetaInfo).to.deep.equal(
          new Map([[DataTypeEnum.Lksg, [dataMetaInfo]]])
        );
      });
    });
  });

  it("Unit tests for sortListOfDataMetaInfoAlphabeticallyByReportingPeriod", () => {
    const testedMethod = wrapper.vm.sortListOfDataMetaInfoAlphabeticallyByReportingPeriod as (
      listOfDataMetaInfo: DataMetaInformation[]
    ) => DataMetaInformation[];
    const dataMetaInfo2023 = { reportingPeriod: "2023" } as DataMetaInformation;
    const dataMetaInfo2022 = { reportingPeriod: "2022" } as DataMetaInformation;
    expect(testedMethod([])).to.be.empty;
    expect(testedMethod([dataMetaInfo2023])).to.deep.equal([dataMetaInfo2023]);
    expect(testedMethod([dataMetaInfo2023, dataMetaInfo2022])).to.deep.equal([dataMetaInfo2023, dataMetaInfo2022]);
    expect(testedMethod([dataMetaInfo2022, dataMetaInfo2023])).to.deep.equal([dataMetaInfo2023, dataMetaInfo2022]);
    expect(testedMethod([dataMetaInfo2022, dataMetaInfo2023, dataMetaInfo2022])).to.deep.equal([
      dataMetaInfo2023,
      dataMetaInfo2022,
      dataMetaInfo2022,
    ]);
  });

  it("Unit tests for sortListOfDataMetaInfoByUploadTime", () => {
    const testedMethod = wrapper.vm.sortListOfDataMetaInfoByUploadTime as (
      listOfDataMetaInfo: DataMetaInformation[]
    ) => DataMetaInformation[];
    const dataMetaInfo0 = { uploadTime: 0 } as DataMetaInformation;
    const dataMetaInfo1 = { uploadTime: 1 } as DataMetaInformation;
    expect(testedMethod([])).to.be.empty;
    expect(testedMethod([dataMetaInfo1])).to.deep.equal([dataMetaInfo1]);
    expect(testedMethod([dataMetaInfo1, dataMetaInfo0])).to.deep.equal([dataMetaInfo1, dataMetaInfo0]);
    expect(testedMethod([dataMetaInfo0, dataMetaInfo1])).to.deep.equal([dataMetaInfo1, dataMetaInfo0]);
    expect(testedMethod([dataMetaInfo0, dataMetaInfo1, dataMetaInfo0])).to.deep.equal([
      dataMetaInfo1,
      dataMetaInfo0,
      dataMetaInfo0,
    ]);
  });

  it("Unit tests for groupListOfDataMetaInfoAsMapOfReportingPeriodToListOfDataMetaInfo", () => {
    const testedMethod = wrapper.vm.groupListOfDataMetaInfoAsMapOfReportingPeriodToListOfDataMetaInfo as (
      listOfDataMetaInfo: DataMetaInformation[]
    ) => Map<string, DataMetaInformation[]>;
    const dataMetaInfo20230 = { reportingPeriod: "2023", uploadTime: 0 } as DataMetaInformation;
    const dataMetaInfo20231 = { reportingPeriod: "2023", uploadTime: 1 } as DataMetaInformation;
    const dataMetaInfo20220 = { reportingPeriod: "2022", uploadTime: 0 } as DataMetaInformation;
    expect(testedMethod([])).to.deep.equal(new Map<string, DataMetaInformation[]>());
    expect(testedMethod([dataMetaInfo20230])).to.deep.equal(
      new Map<string, DataMetaInformation[]>([["2023", [dataMetaInfo20230]]])
    );
    expect(testedMethod([dataMetaInfo20230, dataMetaInfo20231, dataMetaInfo20220])).to.deep.equal(
      new Map<string, DataMetaInformation[]>([
        ["2023", [dataMetaInfo20230, dataMetaInfo20231]],
        ["2022", [dataMetaInfo20220]],
      ])
    );
    expect(testedMethod([dataMetaInfo20231, dataMetaInfo20230, dataMetaInfo20220])).to.deep.equal(
      new Map<string, DataMetaInformation[]>([
        ["2023", [dataMetaInfo20231, dataMetaInfo20230]],
        ["2022", [dataMetaInfo20220]],
      ])
    );
  });

  it("Unit tests for groupAndSortListOfDataMetaInfo", () => {
    const testedMethod = wrapper.vm.groupAndSortListOfDataMetaInfo as (
      listOfDataMetaInfo: DataMetaInformation[]
    ) => DataMetaInformation[];
    const dataMetaInfo20230 = { reportingPeriod: "2023", uploadTime: 0 } as DataMetaInformation;
    const dataMetaInfo20231 = { reportingPeriod: "2023", uploadTime: 1 } as DataMetaInformation;
    const dataMetaInfo20220 = { reportingPeriod: "2022", uploadTime: 0 } as DataMetaInformation;
    const orderedListOfThree = [dataMetaInfo20231, dataMetaInfo20230, dataMetaInfo20220];

    expect(testedMethod([])).to.be.empty;
    expect(testedMethod([dataMetaInfo20230])).to.deep.equal([dataMetaInfo20230]);
    expect(testedMethod([dataMetaInfo20230, dataMetaInfo20230])).to.deep.equal([dataMetaInfo20230, dataMetaInfo20230]);
    expect(testedMethod([dataMetaInfo20230, dataMetaInfo20231, dataMetaInfo20220])).to.deep.equal(orderedListOfThree);
    expect(testedMethod([dataMetaInfo20230, dataMetaInfo20220, dataMetaInfo20231])).to.deep.equal(orderedListOfThree);
    expect(testedMethod([dataMetaInfo20231, dataMetaInfo20230, dataMetaInfo20220])).to.deep.equal(orderedListOfThree);
    expect(testedMethod([dataMetaInfo20231, dataMetaInfo20220, dataMetaInfo20230])).to.deep.equal(orderedListOfThree);
    expect(testedMethod([dataMetaInfo20220, dataMetaInfo20230, dataMetaInfo20231])).to.deep.equal(orderedListOfThree);
    expect(testedMethod([dataMetaInfo20220, dataMetaInfo20231, dataMetaInfo20230])).to.deep.equal(orderedListOfThree);
  });
});
