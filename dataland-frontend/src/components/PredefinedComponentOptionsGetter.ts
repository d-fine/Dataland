import Card from "primevue/card";
import Tooltip from "primevue/tooltip";
import { ComponentOptionsWithObjectProps, PropType } from "vue";
import { humanizeString } from "@/utils/StringHumanizer";

/**
 * Contains getter methods to provide pre-defined options to standardize multiple Vue-components that should behave in
 * a similar way
 */

export function getComponentOptionsForDatalandInfoCard(componentName: string): ComponentOptionsWithObjectProps {
  return {
    name: componentName,
    components: { Card },
    directives: {
      tooltip: Tooltip,
    },
    props: {
      title: {
        type: String,
      },
      value: {
        type: null as unknown as PropType<string | null>,
      },
      tooltipText: {
        type: String,
        default: "",
      },
    },
    computed: {
      humanizedValue() {
        const humanizedValue = humanizeString(this.value as string);
        return humanizedValue !== "" ? humanizedValue : "No data has been reported";
      },
    },
  } as ComponentOptionsWithObjectProps;
}
