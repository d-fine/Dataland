import { type FrameworkDefinition } from "@/frameworks/FrameworkDefinition";
<#list frameworks as framework>
import ${framework} from "@/frameworks/${framework}";
</#list>

export const FrameworkDefinitions: Record<string, FrameworkDefinition<object>> = {
  <#list frameworks as framework>
    "${framework?js_string}": ${framework},
  </#list>
};
