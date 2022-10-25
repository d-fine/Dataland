import { humanizeString } from "@/utils/StringHumanizer";

describe("Unit test for StringHumanizer", () => {
  it("Check if specific keywords are converted correctly", () => {
    expect(humanizeString("alignedcapex")).to.equal("Aligned CapEx");
  });

  it("Check if strings other than specific keywords are converted from camel case to sentence case", () => {
    expect(humanizeString("ThisIsACamelCaseString")).to.equal("This Is A Camel Case String");
    expect(humanizeString("companyFullName")).to.equal("Company Full Name");
    expect(humanizeString("PrimeStandards")).to.equal("Prime Standards");
  });

  it("Check that entering null or undefined is possible", () => {
    expect(humanizeString(null)).to.equal("");
    expect(humanizeString(undefined)).to.equal("");
  });
});
