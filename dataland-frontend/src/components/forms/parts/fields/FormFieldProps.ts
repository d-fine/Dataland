import { DropdownOption } from "@/utils/PremadeDropdownDatasets";
import { deepCopyObject, ObjectType } from "@/utils/UpdateObjectUtils";
import { ComponentPropsOptions } from "vue";

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
  innerClass: {
    type: String,
    default: "short",
  },
};

export const YesNoFormFieldProps = Object.assign(deepCopyObject(FormFieldProps), {
  certificateRequiredIfYes: {
    type: Boolean,
    default: false,
  },
}) as Readonly<ComponentPropsOptions>;
export const FormFieldPropsWithPlaceholder = Object.assign(deepCopyObject(FormFieldProps), {
  placeholder: {
    type: String,
    default: "",
  },
}) as Readonly<ComponentPropsOptions>;
export const DateFormFieldProps = Object.assign(deepCopyObject(FormFieldPropsWithPlaceholder as ObjectType), {
  todayAsMax: {
    type: Boolean,
    default: false,
  },
}) as Readonly<ComponentPropsOptions>;
export const DropdownOptionFormFieldProps = Object.assign(deepCopyObject(FormFieldPropsWithPlaceholder as ObjectType), {
  options: {
    type: Array as () => Array<DropdownOption> | undefined,
    required: true,
  },
}) as Readonly<ComponentPropsOptions>;
export const OptionsFormFieldProps = Object.assign(deepCopyObject(FormFieldPropsWithPlaceholder as ObjectType), {
  options: {
    type: Array as () => Array<typeof Option> | undefined,
    required: true,
  },
}) as Readonly<ComponentPropsOptions>;
