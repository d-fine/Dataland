import { DropdownOption } from "@/utils/PremadeDropdownDatasets";
import { deepCopyObject } from "@/utils/UpdateObjectUtils";

export const FormFieldProps = {
  name: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    default: "",
  },
  label: {
    type: String,
    default: "",
  },
  validation: {
    type: String,
    default: "",
  },
  validationLabel: {
    type: String,
  },
  required: {
    type: Boolean,
    default: false,
  },
};

export const FormFieldPropsWithPlaceholder = Object.assign(deepCopyObject(FormFieldProps), {
  placeholder: {
    type: String,
    default: "",
  },
});
export const DateFormFieldProps = Object.assign(deepCopyObject(FormFieldPropsWithPlaceholder), {
  todayAsMax: {
    type: Boolean,
    default: false,
  },
});

export const DropdownOptionFormFieldProps = Object.assign(deepCopyObject(FormFieldPropsWithPlaceholder), {
  options: {
    type: Array as () => Array<DropdownOption> | undefined,
    required: true,
  },
});
export const OptionsFormFieldProps = Object.assign(deepCopyObject(FormFieldPropsWithPlaceholder), {
  options: {
    type: Array as () => Array<typeof Option> | undefined,
    required: true,
  },
});
