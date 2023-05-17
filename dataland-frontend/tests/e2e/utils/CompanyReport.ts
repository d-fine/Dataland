import { readFileSync } from "fs";
import { createHash } from "crypto";
import { ReferencedReports } from "@e2e/fixtures/FixtureUtils";
import { faker } from "@faker-js/faker";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import { randomYesNoNa } from "@e2e/fixtures/common/YesNoFixtures";
import { randomPastDateOrUndefined } from "@e2e/fixtures/common/DateFixtures";

const possibleReports = ["AnnualReport", "SustainabilityReport", "IntegratedReport", "ESEFReport"];

/**
 * Generates hash to fixture pdf that is used for all fake fixture references
 * @returns documentId ID of a pdf that is stored in internal storage and can be referenced
 */
export function getReferencedDocumentId(): string {
  const testDocumentPath = "../testing/data/documents/StandardWordExport.pdf";
  const fileContent: Buffer = readFileSync(testDocumentPath);
  return createHash("sha256").update(fileContent).digest("hex");
}

/**
 * Generates a random non-empty set of reports that can be referenced
 * @returns a random non-empty set of reports
 */
export function generateReferencedReports(): ReferencedReports {
  const availableReports = faker.helpers.arrayElements(possibleReports);
  if (availableReports.length == 0) availableReports.push(possibleReports[0]);

  const referencedReports: ReferencedReports = {};
  for (const reportName of availableReports) {
    referencedReports[reportName] = {
      reference: getReferencedDocumentId(),
      isGroupLevel: valueOrUndefined(randomYesNoNa()),
      reportDate: randomPastDateOrUndefined(),
      currency: faker.finance.currencyCode(),
    };
  }
  return referencedReports;
}
