import { FormKitNode } from "@formkit/core";

/**
 * Checks which inputs are not filled correctly
 * @param node - single form field
 */
export function checkCustomInputs(node: FormKitNode): void {
  const invalidElements: HTMLElement[] = [];
  node.walk((child: FormKitNode) => {
    // Check if this child has errors
    if ((child.ledger.value("blocking") || child.ledger.value("errors")) && child.type !== "group") {
      // We found an input with validation errors
      if (typeof child.props.id === "string") {
        const invalidElement = document.getElementById(child.props.id);
        if (invalidElement) {
          invalidElements.push(invalidElement);
        }
      }
    }
  }, true);
  invalidElements.find((el) => el !== null)?.scrollIntoView({ behavior: "smooth", block: "center" });
}

/**
 * Checks if for a given validation the corresponding formkit field requires some input
 * @param validation the formkit validation string
 * @returns true if the validation string contains required else false
 */
export function isInputRequired(validation?: string): boolean {
  return validation?.includes("required") ?? false;
}
