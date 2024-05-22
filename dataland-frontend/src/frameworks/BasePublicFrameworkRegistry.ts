import { type BasePublicFrameworkDefinition } from "@/frameworks/BasePublicFrameworkDefinition";
import { PublicFrameworkDefinitions } from "@/frameworks/BasePublicFrameworkRegistryImports";

/**
 * Gets the base framework definition for a specific public framework identified by its identifier string
 * @param identifier of the public framework
 * @returns the base framework definition
 */
export function getBasePublicFrameworkDefinition(
  identifier: string,
): BasePublicFrameworkDefinition<object> | undefined {
  for (const key in PublicFrameworkDefinitions) {
    const frameworkDefinition = PublicFrameworkDefinitions[key];
    if (frameworkDefinition.identifier === identifier) {
      return frameworkDefinition;
    }
  }
  return undefined;
}

/**
 * Gets all the identifier strings for public frameworks
 * @returns the identifier strings
 */
export function getAllPublicFrameworkIdentifiers(): string[] {
  return Object.values(PublicFrameworkDefinitions).map((it) => it.identifier);
}
