import { formatSize } from "@/utils/DataFormatUtils";

describe("Unit test for file size display", () => {
  it("Check if file size display in more readable format", () => {
    const bytesValues = [1000000, 1500000, 500000, 30000, 0];
    const outputArray = bytesValues.map((el) => {
      return formatSize(el);
    });
    expect(outputArray).to.deep.equal(["1 MB", "1.5 MB", "500 KB", "30 KB", "0 B"]);
  });
});
