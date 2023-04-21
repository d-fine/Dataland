import { formatBytesUserFriendly } from "@/utils/NumberConversionUtils";

describe("Unit test for file size display", () => {
  it("Check if file size display in more readable format", () => {
    const bytesValues = [1024 * 1024, 1.5 * 1024 * 1024, 500 * 1024, 30 * 1024, 0];
    const outputArray = bytesValues.map((el) => {
      return formatBytesUserFriendly(el, 1);
    });
    console.log(outputArray);
    expect(outputArray).to.deep.equal(["1 MB", "1.5 MB", "500 KB", "30 KB", "0 Bytes"]);
  });
});
