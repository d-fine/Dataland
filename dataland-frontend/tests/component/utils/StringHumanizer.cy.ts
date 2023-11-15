import { humanizeStringOrNumber } from "@/utils/StringHumanizer";

describe("Unit test for StringHumanizer", () => {
  it("Check if specific keywords are converted correctly", () => {
    expect(humanizeStringOrNumber("alignedcapex")).to.equal("Aligned CapEx");
  });

  it("Check if strings other than specific keywords are converted from camel case to sentence case", () => {
    expect(humanizeStringOrNumber("ThisIsACamelCaseString")).to.equal("This Is A Camel Case String");
    expect(humanizeStringOrNumber("companyFullName")).to.equal("Company Full Name");
    expect(humanizeStringOrNumber("PrimeStandards")).to.equal("Prime Standards");
  });

  it("Check that entering null or undefined is possible", () => {
    expect(humanizeStringOrNumber(null)).to.equal("");
    expect(humanizeStringOrNumber(undefined)).to.equal("");
  });

  it("Check that numbers are humanized correctly", () => {
    expect(humanizeStringOrNumber(220)).to.equal("220");
    expect(humanizeStringOrNumber(2.523)).to.equal("2.523");
  });
});
