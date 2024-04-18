import { ALL_FRAMEWORKS_ORDERED } from "@/utils/Constants";
import { DataTypeEnum } from "@clients/backend";

describe("Unit test for the data type sorting in the Constants", () => {
  it("Check if esg questionnaire and heimathafen are put at the end of a list of data type enums", () => {
    expect(ALL_FRAMEWORKS_ORDERED[ALL_FRAMEWORKS_ORDERED.length - 2]).to.equal(DataTypeEnum.EsgQuestionnaire);
    expect(ALL_FRAMEWORKS_ORDERED[ALL_FRAMEWORKS_ORDERED.length - 1]).to.equal(DataTypeEnum.Heimathafen);
  });
});
