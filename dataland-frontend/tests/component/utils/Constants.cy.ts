import { putGermanFrameworksAtTheEndOfList } from "@/utils/Constants";
import { DataTypeEnum } from "@clients/backend";

describe("Unit test for the data type sorting in the Constants", () => {
  it("Check if Gdv and Heimathafen are put at the end of a list of data type enums", () => {
    const testDataTypeEnums = [
      DataTypeEnum.P2p,
      DataTypeEnum.Gdv,
      DataTypeEnum.Lksg,
      DataTypeEnum.Sme,
      DataTypeEnum.Heimathafen,
    ];
    const sortedTestDataTypeEnums = putGermanFrameworksAtTheEndOfList(testDataTypeEnums);
    expect(sortedTestDataTypeEnums.length).to.equal(testDataTypeEnums.length);
    expect(sortedTestDataTypeEnums[0]).to.equal(DataTypeEnum.P2p);
    expect(sortedTestDataTypeEnums[1]).to.equal(DataTypeEnum.Lksg);
    expect(sortedTestDataTypeEnums[2]).to.equal(DataTypeEnum.Sme);
    expect(sortedTestDataTypeEnums[3]).to.equal(DataTypeEnum.Gdv);
    expect(sortedTestDataTypeEnums[4]).to.equal(DataTypeEnum.Heimathafen);
  });

  it("Check if the order stays the same if a list of data type enums does not contain gdv", () => {
    const allDataTypeEnumsButGdv = Object.values(DataTypeEnum).filter((it) => it !== DataTypeEnum.Gdv);
    const sortedAllDataTypeEnumsButGdv = putGermanFrameworksAtTheEndOfList(allDataTypeEnumsButGdv);
    expect(sortedAllDataTypeEnumsButGdv.length).to.equal(allDataTypeEnumsButGdv.length);
    for (let i = 0; i < allDataTypeEnumsButGdv.length; i++) {
      expect(allDataTypeEnumsButGdv[i]).to.equal(sortedAllDataTypeEnumsButGdv[i]);
    }
  });
});
