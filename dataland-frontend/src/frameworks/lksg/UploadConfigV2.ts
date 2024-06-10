import { type FormKitSchemaDefinition } from "@formkit/core";

interface UploadConfigV2 {
  formKitSchema: FormKitSchemaDefinition;
}
const uploadConfig: UploadConfigV2 = {
  formKitSchema: [
    {
      $formkit: "group",
      name: "data",
      children: [
        {
          $formkit: "group",
          name: "general",
          children: [
            {
              $formkit: "datePicker",
              id: "dataDate",
              name: "dataDate",
              validation: "required",
            },
            {
              $formkit: "dataSource",
              id: "dataSource",
              name: "dataSource",
              validation: "required",
            },
            {
              $formkit: "toggleGroup",
              id: "toggleGroup",
              name: "toggleGroup",
              children: [
                {
                  $formkit: "dataSource",
                  id: "dataSource",
                  name: "dataSource",
                  validation: "required",
                },
              ],
            },
          ],
        },
      ],
    },
  ],
};

export default uploadConfig;
