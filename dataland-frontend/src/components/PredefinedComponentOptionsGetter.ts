import Card from 'primevue/card';
import Tooltip from 'primevue/tooltip';
import { type ComponentOptions, type PropType } from 'vue';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';

/**
 * Contains getter methods to provide pre-defined options to standardize multiple Vue-components that should behave in
 * a similar way
 * @param componentName the name of the vue component to generate standard options for
 * @returns a set of pre-defined options for the defineCopmonent method
 */
export function getComponentOptionsForDatalandInfoCard(componentName: string): ComponentOptions {
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
        default: '',
      },
    },
    computed: {
      humanizedValue() {
        const humanizedValue = humanizeStringOrNumber(this.value as string);
        return humanizedValue !== '' ? humanizedValue : 'No data has been reported';
      },
    },
  } as ComponentOptions;
}
