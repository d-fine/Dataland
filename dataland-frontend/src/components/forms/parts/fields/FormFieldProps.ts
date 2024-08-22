import { type DropdownOption } from '@/utils/PremadeDropdownDatasets';

export const BaseFormFieldProps = {
  name: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    default: '',
  },
  label: {
    type: String,
    default: '',
  },
  validation: {
    type: String,
    default: '',
  },
  validationLabel: {
    type: String,
  },
  required: {
    type: Boolean,
    default: false,
  },
  inputClass: {
    type: String,
    default: 'col-4',
  },
};

export const FormFieldPropsWithPlaceholder = {
  ...BaseFormFieldProps,
  placeholder: {
    type: String,
    default: '',
  },
};

export const DateFormFieldProps = {
  ...FormFieldPropsWithPlaceholder,
  todayAsMax: {
    type: Boolean,
    default: false,
  },
};

export const DropdownOptionFormFieldProps = {
  ...FormFieldPropsWithPlaceholder,
  options: {
    type: Array as () => Array<DropdownOption> | undefined,
    required: true,
  },
  placeholder: {
    default: 'Please Select',
  },
};

export const OptionsFormFieldProps = {
  ...FormFieldPropsWithPlaceholder,
  options: {
    type: Object,
    required: true,
  },
};

export const MultiSelectFormProps = {
  ...FormFieldPropsWithPlaceholder,
  options: {
    type: Array as () => Array<DropdownOption>,
    required: true,
  },
  optionValue: {
    type: String,
    required: false,
  },
  optionLabel: {
    type: String,
    required: false,
  },
};
