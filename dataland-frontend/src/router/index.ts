import { createWebHistory, createRouter, RouteComponent } from "vue-router";
const UploadEuTaxonomyDataForNonFinancials = (): Promise<RouteComponent> =>
  import("@/components/pages/UploadEuTaxonomyDataForNonFinancials.vue");
const WelcomeDataland = (): Promise<RouteComponent> => import("@/components/pages/WelcomeDataland.vue");
const CompanyInformation = (): Promise<RouteComponent> => import("@/components/pages/CompanyInformation.vue");
const SearchCompaniesForFrameworkData = (): Promise<RouteComponent> =>
  import("@/components/pages/SearchCompaniesForFrameworkData.vue");
const ViewEuTaxonomyNonFinancialsSample = (): Promise<RouteComponent> =>
  import("@/components/pages/ViewEuTaxonomyNonFinancialsSample.vue");
const TheImprint = (): Promise<RouteComponent> => import("@/components/pages/TheImprint.vue");
const DataPrivacy = (): Promise<RouteComponent> => import("@/components/pages/DataPrivacy.vue");
const NoContentFound = (): Promise<RouteComponent> => import("@/components/pages/NoContentFound.vue");
const UploadEuTaxonomyDataForFinancials = (): Promise<RouteComponent> =>
  import("@/components/pages/UploadEuTaxonomyDataForFinancials.vue");
const UploadCompany = (): Promise<RouteComponent> => import("@/components/pages/UploadCompany.vue");
const SearchCompanies = (): Promise<RouteComponent> => import("@/components/pages/SearchCompanies.vue");
const ViewEuTaxonomyFinancials = (): Promise<RouteComponent> =>
  import("@/components/pages/ViewEuTaxonomyFinancials.vue");
const ViewEuTaxonomyNonFinancials = (): Promise<RouteComponent> =>
  import("@/components/pages/ViewEuTaxonomyNonFinancials.vue");
const ApiKeysPage = (): Promise<RouteComponent> => import("@/components/pages/ApiKeysPage.vue");
const RequestData = (): Promise<RouteComponent> => import("@/components/pages/RequestData.vue");
const ViewLksg = (): Promise<RouteComponent> => import("@/components/pages/ViewLksg.vue");
const UploadLkSG = (): Promise<RouteComponent> => import("@/components/pages/UploadLkSG.vue");
const DatasetOverview = (): Promise<RouteComponent> => import("@/components/pages/DatasetOverview.vue");
const ChooseCompanyForFrameworkDataUpload = (): Promise<RouteComponent> =>
  import("@/components/pages/ChooseCompanyForFrameworkDataUpload.vue");
const ChooseFrameworkForDataUpload = (): Promise<RouteComponent> =>
  import("@/components/pages/ChooseFrameworkForDataUpload.vue");
import { DataTypeEnum} from "@clients/backend";


const routes = [
  {
    path: "/",
    name: "Welcome to Dataland",
    component: WelcomeDataland,
  },
  {
    path: `/samples/${DataTypeEnum.EutaxonomyNonFinancials}`,

    name: "Eu Taxonomy For Non-Financials Sample",
    component: ViewEuTaxonomyNonFinancialsSample,
  },
  {
    path: "/companies/upload",
    name: "Upload Company",
    component: UploadCompany,
  },
  {
    path: "/companies/choose",
    name: "Choose Company",
    component: ChooseCompanyForFrameworkDataUpload,
  },
  {
    path: "/companies/:companyID/frameworks/upload",
    props: true,
    name: "Choose Framework",
    component: ChooseFrameworkForDataUpload,
  },
  {
    path: `/companies/:companyID/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload`,
    props: true,
    name: "Upload Eu Taxonomy Data For Non-Financials",
    component: UploadEuTaxonomyDataForNonFinancials,
  },
  {
    path: `/companies/:companyID/frameworks/${DataTypeEnum.EutaxonomyFinancials}/upload`,
    props: true,
    name: "Upload Eu Taxonomy Data For Financials",
    component: UploadEuTaxonomyDataForFinancials,
  },
  {
    path: `/companies/:companyID/frameworks/${DataTypeEnum.Lksg}/upload`,
    props: true,
    name: "Upload lkSG Data",
    component: UploadLkSG,
  },
  {
    path: "/companies",
    name: "Search Companies for Framework Data",
    component: SearchCompaniesForFrameworkData,
  },
  {
    path: "/companies/:companyID",
    props: true,
    name: "Company Info",
    component: CompanyInformation,
  },
  {
    path: `/companies/:companyID/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`,
    props: true,
    name: "Company EU Taxonomy for non financials",
    component: ViewEuTaxonomyNonFinancials,
  },
  {
    path: `/companies/:companyID/frameworks/${DataTypeEnum.EutaxonomyFinancials}`,
    props: true,
    name: "Company EU Taxonomy for financials",
    component: ViewEuTaxonomyFinancials,
  },
  {
    path: `/companies/:companyID/frameworks/${DataTypeEnum.Lksg}`,
    props: true,
    name: "Company lksg",
    component: ViewLksg,
  },
  {
    path: "/companies-only-search",
    name: "Search Companies",
    component: SearchCompanies,
  },
  {
    path: "/datasets",
    name: "Dataset Overview",
    component: DatasetOverview,
  },
  {
    path: "/requests",
    name: "Request Data",
    component: RequestData,
  },
  {
    path: "/api-key",
    name: "ApiKeysPage",
    component: ApiKeysPage,
  },
  {
    path: "/dataprivacy",
    name: "DataPrivacy",
    component: DataPrivacy,
  },
  {
    path: "/imprint",
    name: "LandingImprint",
    component: TheImprint,
  },
  {
    path: "/nocontent",
    name: "NoContentFound",
    component: NoContentFound,
  },
  {
    path: "/:notFound(.*)",
    redirect: "/nocontent",
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    window.scrollTo(0, 0);
  },
});

export { routes };
export default router;
