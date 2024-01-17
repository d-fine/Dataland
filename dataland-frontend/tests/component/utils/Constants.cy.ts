import { putEsgQuestionnaireAtTheEndOfList } from "@/utils/Constants";
import { DataTypeEnum } from "@clients/backend";

describe("Unit test for the data type sorting in the Constants", () => {
  it("Check if esg questionnaire is put at the end of a list of data type enums", () => {
    const testDataTypeEnums = [DataTypeEnum.P2p, DataTypeEnum.EsgQuestionnaire, DataTypeEnum.Lksg, DataTypeEnum.Sme];
    const sortedTestDataTypeEnums = putEsgQuestionnaireAtTheEndOfList(testDataTypeEnums);
    expect(sortedTestDataTypeEnums.length).to.equal(testDataTypeEnums.length);
    expect(sortedTestDataTypeEnums[0]).to.equal(DataTypeEnum.P2p);
    expect(sortedTestDataTypeEnums[1]).to.equal(DataTypeEnum.Lksg);
    expect(sortedTestDataTypeEnums[2]).to.equal(DataTypeEnum.Sme);
    expect(sortedTestDataTypeEnums[3]).to.equal(DataTypeEnum.EsgQuestionnaire);
  });

  it("Check if the order stays the same if a list of data type enums does not contain esgquestionnaire", () => {
    const allDataTypeEnumsButEsgQuestionnaire = Object.values(DataTypeEnum).filter(
      (it) => it !== DataTypeEnum.EsgQuestionnaire,
    );
    const sortedAllDataTypeEnumsButEsgQuestionnaire = putEsgQuestionnaireAtTheEndOfList(
      allDataTypeEnumsButEsgQuestionnaire,
    );
    expect(sortedAllDataTypeEnumsButEsgQuestionnaire.length).to.equal(allDataTypeEnumsButEsgQuestionnaire.length);
    for (let i = 0; i < allDataTypeEnumsButEsgQuestionnaire.length; i++) {
      expect(allDataTypeEnumsButEsgQuestionnaire[i]).to.equal(sortedAllDataTypeEnumsButEsgQuestionnaire[i]);
    }
  });
});
