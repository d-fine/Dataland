import { type BaseFrameworkDefinition } from "@/frameworks/FrameworkDefinition";
import { FrameworkDefinitions } from "@/frameworks/BaseFrameworkRegistryImports";

/**
 * Retrieve the framework definition for the provided framework identifier if it exists
 * It is critical that no files in the base-framework-definition dependency tree import .vue files
 * as this code is shared with the tests that do not know how to handle such files
 * @param identifier the identifier of the framework to retrieve
 * @returns the framework definition if it exists
 */
export function getBaseFrameworkDefinition(identifier: string): BaseFrameworkDefinition<object> | undefined {
  return FrameworkDefinitions[identifier];
}

/**
 * Retrieve a list of all available framework identifiers
 * @returns a list of all available framework identifiers
 */
export function getAllFrameworkIdentifiers(): string[] {
  return Object.values(FrameworkDefinitions).map((it) => it.identifier);
}
