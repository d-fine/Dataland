import { humanizeString } from "@/utils/StringHumanizer";
import { expect } from "@jest/globals";

describe("Test StringHumanizer", () => {
  it("verifies if the implementation works correctly", () => {
    expect(humanizeString("companyFullName")).toEqual("Company Full Name");
    expect(humanizeString("Hdax")).toEqual("HDAX");
    expect(humanizeString("Dax50Esg")).toEqual("DAX 50 ESG");
    expect(humanizeString("PrimeStandards")).toEqual("Prime Standards");
  });
});
