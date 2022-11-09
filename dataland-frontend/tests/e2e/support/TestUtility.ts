import { Suite } from "mocha";

export interface ExecutionConfig {
  executionEnvironments: Array<ExecutionEnvironment>;
  dataEnvironments: Array<DataEnvironment>;
}
export type ExecutionEnvironment = "developmentLocal" | "ci" | "developmentCd" | "previewCd";
export type DataEnvironment = "fakeFixtures" | "realData";

export function describeIf(name: string, execConfig: ExecutionConfig, fn: (this: Suite) => void): Suite {
  const executionEnvironment = Cypress.env("EXECUTION_ENVIRONMENT") as ExecutionEnvironment;
  const dataEnvironment = Cypress.env("DATA_ENVIRONMENT") as DataEnvironment;

  if (execConfig.executionEnvironments.indexOf(executionEnvironment) === -1) {
    return describe(`${name} - Disabled`, () => {
      it(`Has been disabled because the execution environment ${executionEnvironment} has not been allowed`, () => {
        // Stub-Test just so its displayed why test suit wasn't executed
      });
    });
  }

  if (execConfig.dataEnvironments.indexOf(dataEnvironment) === -1) {
    return describe(`${name} - Disabled`, () => {
      it(`Has been disabled because the data environment ${dataEnvironment} has not been allowed`, () => {
        // Stub-Test just so its displayed why test suit wasn't executed
      });
    });
  }

  return describe(name, fn);
}
