import { faker } from '@faker-js/faker';
import { activityTree } from '@/components/forms/parts/elements/derived/ActivityTree';
import { type TreeNode } from 'primevue/treenode';
import { pickOneElement, pickSubsetOfElements } from '@e2e/fixtures/FixtureUtils';
import { naceCodeMap } from '@/components/forms/parts/elements/derived/NaceCodeTree';

/**
 * Generates a list of sorted NACE codes
 * @param min minimum number of NACE codes to generate
 * @param max maximum number of NACE codes to generate
 * @returns random list of NACE codes
 */
export function generateNaceCodes(min = 0, max = 5): string[] {
  return pickSubsetOfElements<string>(Array.from(naceCodeMap.keys()), min, max).sort((a, b) => a.localeCompare(b));
}

/**
 * Gets a random number of valid NACE codes for a specific activity by parsing the activities tree.
 * It throws an error if the activity cannot be found in the activity tree.
 * @param activityName name of the activity to return NACE codes for
 * @returns a random number of valid NACE codes
 */
export function getRandomNumberOfNaceCodesForSpecificActivity(activityName: string): string[] | null {
  for (const node of activityTree) {
    if (node.type === 'header' && node.children) {
      for (const childNode of node.children) {
        if (childNode.type === 'child' && childNode.value === activityName) {
          return getRandomNumberOfNaceCodes(childNode);
        }
      }
    }
  }
  throw new Error(`Activity not found in activity tree: ${activityName}`);
}

/**
 * Gets a random number of NACE codes for one specific childNode if it actually contains NACE codes.
 * @param childNode node in the activity tree to get potential NACE codes from
 * @returns a random number of valid NACE codes or null
 */
function getRandomNumberOfNaceCodes(childNode: TreeNode): string[] | null {
  let naceCodesToReturn: string[] | null = null;
  if (Array.isArray(childNode.naceCodes) && childNode.naceCodes.every((item) => typeof item === 'string')) {
    const allNaceCodesForActivity = childNode.naceCodes as string[];
    const listWithRandomNumberOfNaceCodes = Array.from(
      { length: faker.number.int({ min: 1, max: allNaceCodesForActivity.length }) },
      () => {
        return pickOneElement(allNaceCodesForActivity);
      }
    );
    naceCodesToReturn = [...new Set(listWithRandomNumberOfNaceCodes)];
  }
  return naceCodesToReturn;
}
