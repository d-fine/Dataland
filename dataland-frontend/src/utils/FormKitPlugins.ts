import { type FormKitNode } from '@formkit/core';

/**
 * FormKit Plugin that applied ability to deselect a single selection from the list
 * @param node the node this plugin is applied to
 */
export function disabledOnMoreThanOne(node: FormKitNode): void {
  if (node.props.type !== 'checkbox' || !node.props.options) {
    return;
  }
  node.hook.commit((payload, next) => next((payload as Option[])?.slice(-1)) as Option[]);
}

interface Option {
  value: string;
  label: string;
}
