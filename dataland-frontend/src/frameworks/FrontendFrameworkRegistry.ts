import { FrontendFrameworkDefinitions } from '@/frameworks/FrontendFrameworkRegistryImports';
import { type FrontendFrameworkDefinition } from '@/frameworks/BaseFrameworkDefinition';

/**
 * Retrieve the framework definition for the provided framework identifier if it exists
 * @param identifier the identifier of the framework to retrieve
 * @returns the framework definition if it exists
 */
export function getFrontendFrameworkDefinition(identifier: string): FrontendFrameworkDefinition<object> | undefined {
  for (const key in FrontendFrameworkDefinitions) {
    const frameworkDefinition = FrontendFrameworkDefinitions[key];
    if (frameworkDefinition.identifier === identifier) {
      return frameworkDefinition;
    }
  }
  return undefined;
}

/**
 * Retrieve a list of all available framework identifiers
 * @returns a list of all available framework identifiers
 */
export function getAllFrameworkIdentifiers(): string[] {
  return Object.values(FrontendFrameworkDefinitions).map((it) => it.identifier);
}
