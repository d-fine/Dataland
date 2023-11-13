import { type FrameworkDefinition } from "@/frameworks/FrameworkDefinition";

const frameworkModules: Record<string, FrameworkDefinition<unknown>> = import.meta.glob("./*/index.ts", {
  import: "default",
  eager: true,
});

/**
 * Retrieve the framework definition for the provided framework identifier if it exists
 * @param identifier the identifier of the framework to retrieve
 * @returns the framework definition if it exists
 */
export function getFrameworkDefinition(identifier: string): FrameworkDefinition<unknown> | undefined {
  return frameworkModules[`./${identifier}/index.ts`];
}

/**
 * Retrieve a list of all available framework identifiers
 * @returns a list of all available framework identifiers
 */
export function getAllFrameworkIdentifiers(): string[] {
  return Object.values(frameworkModules).map((it) => it.identifier);
}
