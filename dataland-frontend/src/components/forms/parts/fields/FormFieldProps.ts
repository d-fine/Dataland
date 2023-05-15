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

export const YesNoFormFieldProps = Object.assign(JSON.parse(JSON.stringify(FormFieldProps)) as object, {
  certificateRequiredIfYes: {
    type: Boolean,
    default: false,
  },
});
export const FormFieldPropsWithPlaceholder = Object.assign(JSON.parse(JSON.stringify(FormFieldProps)) as object, {
  placeholder: {
    type: String,
    default: "",
  },
});
export const DateFormFieldProps = Object.assign(JSON.parse(JSON.stringify(FormFieldPropsWithPlaceholder)) as object, {
  todayAsMax: {
    type: Boolean,
    default: false,
  },
});

export const DropdownOptionFormFieldProps = Object.assign(
  JSON.parse(JSON.stringify(FormFieldPropsWithPlaceholder)) as object,
  {
    options: {
      type: Array as () => Array<DropdownOption> | undefined,
      required: true,
    },
  }
);
export const OptionsFormFieldProps = Object.assign(
  JSON.parse(JSON.stringify(FormFieldPropsWithPlaceholder)) as object,
  {
    options: {
      type: Array as () => Array<typeof Option> | undefined,
      required: true,
    },
  }
);
