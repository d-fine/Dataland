import { type FormKitNode } from "@formkit/core";

/**
 * FormKit Plugin that applied to a select input with changing options selects the first option in the list
 * if the input gets invalid (by e.g. the current selection is removed from the options)
 * @param node the node this plugin is applied to
 */
export function selectNothingIfNotExistsFormKitPlugin(node: FormKitNode): void {
  if (node.props["type"] !== "select") return;
  node.on("prop:options", ({ payload }) => {
    if (!(payload as Option[]).map((option) => option.value).includes(node.value as string)) {
      node.input("").catch(() => undefined);
    }
  });
}

/**
 * FormKit Plugin that applied ability to deselect a single selection from the list
 * @param node the node this plugin is applied to
 */
export function disabledOnMoreThanOne(node: FormKitNode): void {
  if (node.props.type !== "checkbox" || !node.props.options) {
    return;
  }
  node.hook.commit((payload, next) => next((payload as Option[])?.slice(-1)) as Option[]);
}

interface Option {
  value: string;
  label: string;
}
