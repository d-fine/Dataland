import { nextTick, ComponentOptions } from "vue";

export const waitForComponent = async (component: ComponentOptions): Promise<void> => {
  // Wait for the next tick of the event loop to ensure that the component has been rendered
  await nextTick();
  // Check if the component's element is available in the DOM
  if (component.vm.$el) {
    // If the element is not available, call the function recursively until it is
    return;
  }
  return waitForComponent(component);
};
