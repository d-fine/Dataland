import { humanizeString } from "@/utils/StringHumanizer";
import { expect } from "@jest/globals";

describe("Test StringHumanizer", () => {
  it("checks if stock index abbreviations and specific keywords are converted correctly", () => {
    expect(humanizeString("Hdax")).toEqual("HDAX");
    expect(humanizeString("DaX50ESg")).toEqual("DAX 50 ESG");
    expect(humanizeString("alignedcapex")).toEqual("Aligned CapEx");
  });

  it("checks if strings other than stock indices and specific keywords are converted from camle case to sentence case", () => {
    expect(humanizeString("ThisIsACamelCaseString")).toEqual("This Is A Camel Case String");
    expect(humanizeString("companyFullName")).toEqual("Company Full Name");
    expect(humanizeString("PrimeStandards")).toEqual("Prime Standards");
  });
});
