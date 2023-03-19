import { defaultConfig } from "@formkit/vue";
import { createNode, FormKitNode } from "@formkit/core";

/**
 * Plugin that allows nesting inputs by
 * using dot notation of the name.
 *
 * @param node FormKitNode
 */
function babushkaPlugin(node: FormKitNode): void {
  if (node.name.indexOf(".") !== -1) {
    const address = node.name.split(".");
    node._c.name = address.pop() as string;
    const parentAddress = address.reduce((currentAddress, parentName) => {
      const parent = node.at(currentAddress);
      if (!parent && currentAddress === "$root") {
        throw new Error("Dot-notation names bust be children of a form or group");
      } else if (!parent) {
        throw new Error("This shouldn’t happen");
      } else {
        createNode({ name: parentName, parent, type: "group", props: { type: "group" } });
      }
      return `${currentAddress}.${parentName}`;
    }, "$root");
    const parentNode = node.at(parentAddress);
    if (parentNode) {
      parentNode.add(node);
    }
  }
}

const config = defaultConfig({
  plugins: [babushkaPlugin],
});
export default config;
