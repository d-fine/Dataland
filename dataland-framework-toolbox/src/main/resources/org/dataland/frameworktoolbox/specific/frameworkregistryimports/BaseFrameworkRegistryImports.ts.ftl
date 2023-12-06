import { type BaseFrameworkDefinition } from "@/frameworks/FrameworkDefinition";
<#list frameworks as framework>
import ${framework} from "@/frameworks/${framework}/BaseFrameworkDefinition";
</#list>

export const FrameworkDefinitions: Record<string, BaseFrameworkDefinition<object>> = {
  <#list frameworks as framework>
    ${framework?js_string}: ${framework},
  </#list>
};
