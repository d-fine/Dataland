import { ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER, PRIVATE_FRAMEWORKS } from '@/utils/Constants';
import { DataTypeEnum } from '@clients/backend';
import { getAllPublicFrameworkIdentifiers } from '@/frameworks/BasePublicFrameworkRegistry';
import { getAllPrivateFrameworkIdentifiers } from '@/frameworks/BasePrivateFrameworkRegistry';

describe('Unit test for the data type sorting in the Constants', () => {
  it('Check framework order of esg datenkatalog, and additional company information', () => {
    expect(ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER[ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER.length - 3]).to.equal(
      DataTypeEnum.Vsme
    );
    expect(ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER[ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER.length - 2]).to.equal(
      DataTypeEnum.EsgDatenkatalog
    );
  });

  it('Check if the frameworks in the Constants are in sync with the framework registries', () => {
    /**
     * Removes frameworks without toolbox support from the list of framework identifiers, that is passed to it.
     * @param frameworkIdentifiers as input to filter
     * @returns the filtered list of framework identifiers
     */
    function removeFrameworksWithNoToolboxSupport(frameworkIdentifiers: string[]): string[] {
      const nonToolboxSupportedFrameworkIdentifiers = [DataTypeEnum.P2p.toString()];
      return frameworkIdentifiers.filter(
        (frameworkIdentifier) => !nonToolboxSupportedFrameworkIdentifiers.includes(frameworkIdentifier)
      );
    }

    const allToolboxSupportedFrameworksConstant = removeFrameworksWithNoToolboxSupport(
      ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER
    );

    const frameworkIdentifiersFromPrivateRegistry = getAllPrivateFrameworkIdentifiers();
    const frameworkIdentifiersFromPublicAndPrivateRegistries = [
      ...frameworkIdentifiersFromPrivateRegistry,
      ...getAllPublicFrameworkIdentifiers(),
    ];

    expect(allToolboxSupportedFrameworksConstant).to.have.members(frameworkIdentifiersFromPublicAndPrivateRegistries);
    expect(frameworkIdentifiersFromPublicAndPrivateRegistries).to.have.members(allToolboxSupportedFrameworksConstant);

    expect(PRIVATE_FRAMEWORKS).to.have.members(frameworkIdentifiersFromPrivateRegistry);
    expect(frameworkIdentifiersFromPrivateRegistry).to.have.members(PRIVATE_FRAMEWORKS);
  });
});
