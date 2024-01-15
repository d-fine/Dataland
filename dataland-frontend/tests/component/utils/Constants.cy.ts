import { putEsgQuestionnaireAtTheEndOfList } from "@/utils/Constants";
import { DataTypeEnum } from "@clients/backend";

describe("Unit test for the data type sorting in the Constants", () => {
  it("Check if esg questionnaire is put at the end of a list of data type enums", () => {
    const testDataTypeEnums = [DataTypeEnum.P2p, DataTypeEnum.Esgquestionnaire, DataTypeEnum.Lksg, DataTypeEnum.Sme];
    const sortedTestDataTypeEnums = putEsgQuestionnaireAtTheEndOfList(testDataTypeEnums);
    expect(sortedTestDataTypeEnums.length).to.equal(testDataTypeEnums.length);
    expect(sortedTestDataTypeEnums[0]).to.equal(DataTypeEnum.P2p);
    expect(sortedTestDataTypeEnums[1]).to.equal(DataTypeEnum.Lksg);
    expect(sortedTestDataTypeEnums[2]).to.equal(DataTypeEnum.Sme);
    expect(sortedTestDataTypeEnums[3]).to.equal(DataTypeEnum.Esgquestionnaire);
  });

  it("Check if the order stays the same if a list of data type enums does not contain esgquestionnaire", () => {
    const allDataTypeEnumsButEsgquestionnaire = Object.values(DataTypeEnum).filter((it) => it !== DataTypeEnum.Esgquestionnaire);
    const sortedAllDataTypeEnumsButEsgquestionnaire = putEsgQuestionnaireAtTheEndOfList(allDataTypeEnumsButEsgquestionnaire);
    expect(sortedAllDataTypeEnumsButEsgquestionnaire.length).to.equal(allDataTypeEnumsButEsgquestionnaire.length);
    for (let i = 0; i < allDataTypeEnumsButEsgquestionnaire.length; i++) {
      expect(allDataTypeEnumsButEsgquestionnaire[i]).to.equal(sortedAllDataTypeEnumsButEsgquestionnaire[i]);
    }
  });
});
