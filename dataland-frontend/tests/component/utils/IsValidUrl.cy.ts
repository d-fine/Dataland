import { isValidHttpUrl } from "@/utils/UrlValid";

describe("Test for isValidHttpUrl", () => {
  it("checks if the url validation function is working correctly", () => {
    const validUrl = "https://dataland.com/";
    const invalidUrl = "dataland.com";
    expect(isValidHttpUrl(validUrl)).to.be.true;
    expect(isValidHttpUrl(invalidUrl)).to.be.false;
  });
});
