import { DropdownOption } from "@/utils/PremadeDropdownDatasets";

export const FormFieldProps = {
  name: {
    type: String,
    required: true,
  },
  info: {
    type: String,
    default: "",
  },
  displayName: {
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

export const FormFieldPropsWithPlaceholder = Object.assign(FormFieldProps, {
  required: {
    // TODO this isnt a placeholder per se in FormKit
    type: String,
    default: "",
  },
});
export const DateFormFieldProps = Object.assign(FormFieldPropsWithPlaceholder, {
  todayAsMax: {
    type: Boolean,
    default: false,
  },
});

export const DropdownOptionFormFieldProps = Object.assign(FormFieldPropsWithPlaceholder, {
  options: {
    type: Array as () => Array<DropdownOption>,
    required: true,
  },
});
export const OptionsFormFieldProps = Object.assign(FormFieldPropsWithPlaceholder, {
  options: {
    type: Array as () => Array<typeof Option>,
    required: true,
  },
});
