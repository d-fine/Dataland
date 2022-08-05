import { Suite } from "mocha";

export interface ExecutionConfig {
  executionEnvironments: { [key in ExecutionEnvironments]: boolean };
  dataEnvironments: { [key in DataEnvironments]: boolean };
}
export type ExecutionEnvironments = "development" | "preview";
export type DataEnvironments = "fakeFixtures" | "realData";

export function describeIf(name: string, execConfig: ExecutionConfig, fn: (this: Suite) => void): Suite {
  return describe(name, fn);
}
