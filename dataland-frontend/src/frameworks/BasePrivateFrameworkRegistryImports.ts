import vsmeBaseFrameworkDefinition from "@/frameworks/vsme/BaseFrameworkDefinition";
import { type BasePrivateFrameworkDefinition } from "@/frameworks/BasePrivateFrameworkDefinition";

export const PrivateFrameworkDefinitions: Record<string, BasePrivateFrameworkDefinition<object>> = {
  vsme: vsmeBaseFrameworkDefinition,
};
