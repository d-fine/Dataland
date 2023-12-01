import { type AxiosInstance } from "axios";
import { type FrameworkDefinition, type FrameworkViewConfiguration } from "@/frameworks/FrameworkDefinition";
import { type Configuration, type ${frameworkIdentifier?cap_first}Data } from "@clients/backend";
import { ${frameworkIdentifier?cap_first}ViewConfiguration } from "@/frameworks/${frameworkIdentifier}/ViewConfig";
import { type FrameworkDataApi } from "@/utils/api/UnifiedFrameworkDataApi";
import { ${frameworkIdentifier?cap_first}ApiClient } from "@/frameworks/${frameworkIdentifier}/ApiClient";

const ${frameworkIdentifier?cap_first}FrameworkDefinition: FrameworkDefinition<${frameworkIdentifier?cap_first}Data> = {
  identifier: "${frameworkIdentifier}",
  explanation: "${frameworkExplanation?js_string}",
  label: "${frameworkLabel?js_string}",
  getFrameworkViewConfiguration(): FrameworkViewConfiguration<${frameworkIdentifier?cap_first}Data> {
    return {
      type: "MultiLayerDataTable",
      configuration: ${frameworkIdentifier?cap_first}ViewConfiguration,
    };
  },
  getFrameworkApiClient(
    configuration?: Configuration,
    axiosInstance?: AxiosInstance,
  ): FrameworkDataApi<${frameworkIdentifier?cap_first}Data> {
    return new ${frameworkIdentifier?cap_first}ApiClient(configuration, axiosInstance);
  },
};

export default ${frameworkIdentifier?cap_first}FrameworkDefinition;
