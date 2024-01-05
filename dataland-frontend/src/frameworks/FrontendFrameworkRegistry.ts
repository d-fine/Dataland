import { type FrontendFrameworkDefinition } from "@/frameworks/FrameworkDefinition";
import { FrameworkDefinitions } from "@/frameworks/FrontendFrameworkRegistryImports";

/**
 * Retrieve the framework definition for the provided framework identifier if it exists
 * @param identifier the identifier of the framework to retrieve
 * @returns the framework definition if it exists
 */
export function getFrontendFrameworkDefinition(identifier: string): FrontendFrameworkDefinition<object> | undefined {
  return FrameworkDefinitions[identifier];
}

/**
 * Retrieve a list of all available framework identifiers
 * @returns a list of all available framework identifiers
 */
export function getAllFrameworkIdentifiers(): string[] {
  return Object.values(FrameworkDefinitions).map((it) => it.identifier);
}
