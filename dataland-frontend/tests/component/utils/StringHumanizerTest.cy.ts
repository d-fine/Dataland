import { humanizeString } from "@/utils/StringHumanizer";

describe("Unit test for StringHumanizer", () => {
  it("Check if stock index abbreviations and specific keywords are converted correctly", () => {
    expect(humanizeString("Hdax")).to.equal("HDAX");
    expect(humanizeString("DaX50ESg")).to.equal("DAX 50 ESG");
    expect(humanizeString("alignedcapex")).to.equal("Aligned CapEx");
  });

  it("Check if strings other than stock indices and specific keywords are converted from camel case to sentence case", () => {
    expect(humanizeString("ThisIsACamelCaseString")).to.equal("This Is A Camel Case String");
    expect(humanizeString("companyFullName")).to.equal("Company Full Name");
    expect(humanizeString("PrimeStandards")).to.equal("Prime Standards");
  });
});
