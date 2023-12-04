import { type FrontendFrameworkDefinition } from "@/frameworks/FrameworkDefinition";
<#list frameworks as framework>
import ${framework} from "@/frameworks/${framework}/FrontendFrameworkDefinition";
</#list>

export const FrameworkDefinitions: Record<string, FrontendFrameworkDefinition<object>> = {
  <#list frameworks as framework>
    "${framework?js_string}": ${framework},
  </#list>
};
