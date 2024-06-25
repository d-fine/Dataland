import { type BasePrivateFrameworkDefinition } from '@/frameworks/BasePrivateFrameworkDefinition';
import { PrivateFrameworkDefinitions } from '@/frameworks/BasePrivateFrameworkRegistryImports';

/**
 * Gets the base framework definition for a specific private framework identified by its identifier string
 * It is critical that no files in the base-framework-definition dependency tree import .vue files
 * as this code is shared with the tests that do not know how to handle such files
 * @param identifier of the private framework
 * @returns the base framework definition
 */
export function getBasePrivateFrameworkDefinition(
  identifier: string
): BasePrivateFrameworkDefinition<object> | undefined {
  for (const key in PrivateFrameworkDefinitions) {
    const frameworkDefinition = PrivateFrameworkDefinitions[key];
    if (frameworkDefinition.identifier === identifier) {
      return frameworkDefinition;
    }
  }
  return undefined;
}

/**
 * Gets all the identifier strings for private frameworks
 * @returns the identifier strings
 */
export function getAllPrivateFrameworkIdentifiers(): string[] {
  return Object.values(PrivateFrameworkDefinitions).map((it) => it.identifier);
}
