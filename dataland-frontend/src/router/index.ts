import { createWebHistory, createRouter, type RouteComponent } from "vue-router";
const NewLandingPage = (): Promise<RouteComponent> => import("@/components/pages/NewLandingPage.vue");

const QualityAssurance = (): Promise<RouteComponent> => import("@/components/pages/QualityAssurance.vue");
const SearchCompaniesForFrameworkData = (): Promise<RouteComponent> =>
  import("@/components/pages/SearchCompaniesForFrameworkData.vue");
const TermsAndConditions = (): Promise<RouteComponent> => import("@/components/pages/TermsAndConditions.vue");
const TokenTerms = (): Promise<RouteComponent> => import("@/components/pages/TokenTerms.vue");
const PricingAndRemuneration = (): Promise<RouteComponent> => import("@/components/pages/PricingAndRemuneration.vue");
const TheImprint = (): Promise<RouteComponent> => import("@/components/pages/TheImprint.vue");
const DataPrivacy = (): Promise<RouteComponent> => import("@/components/pages/DataPrivacy.vue");
const NoContentFound = (): Promise<RouteComponent> => import("@/components/pages/NoContentFound.vue");
const ApiKeysPage = (): Promise<RouteComponent> => import("@/components/pages/ApiKeysPage.vue");
const RequestData = (): Promise<RouteComponent> => import("@/components/pages/RequestData.vue");
const ViewFrameworkData = (): Promise<RouteComponent> => import("@/components/pages/ViewFrameworkData.vue");
const DatasetOverview = (): Promise<RouteComponent> => import("@/components/pages/DatasetOverview.vue");
const UploadFormWrapper = (): Promise<RouteComponent> => import("@/components/pages/UploadFormWrapper.vue");
const ChooseCompanyForFrameworkDataUpload = (): Promise<RouteComponent> =>
  import("@/components/pages/ChooseCompanyForFrameworkDataUpload.vue");

const ViewTeaserCompanyData = (): Promise<RouteComponent> => import("@/components/pages/ViewTeaserCompanyData.vue");
const ChooseFrameworkForDataUpload = (): Promise<RouteComponent> =>
  import("@/components/pages/ChooseFrameworkForDataUpload.vue");

const routes = [
  {
    path: "/",
    name: "Welcome to Dataland",
    component: NewLandingPage,
  },
  {
    path: "/preview",
    name: "View Sample Data in Preview Mode",
    component: ViewTeaserCompanyData,
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
    path: `/companies/:companyID/frameworks/:frameworkType/upload`,
    props: true,
    name: `Upload framework data`,
    component: UploadFormWrapper,
  },
  {
    path: "/companies",
    name: "Search Companies for Framework Data",
    component: SearchCompaniesForFrameworkData,
  },
  {
    path: `/companies/:companyId/frameworks/:dataType`,
    props: true,
    name: "Company framework data view page",
    component: ViewFrameworkData,
  },
  {
    path: `/companies/:companyId/frameworks/:dataType/:dataId`,
    props: true,
    name: "Company framework data view page for specific data ID",
    component: ViewFrameworkData,
  },
  {
    path: `/companies/:companyId/frameworks/:dataType/reportingPeriods/:reportingPeriod`,
    props: true,
    name: "Company framework data for specific reporting period",
    component: ViewFrameworkData,
  },
  {
    path: `/qualityassurance`,
    name: "UI for quality assurance",
    component: QualityAssurance,
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
    path: "/terms",
    name: "TermsAndConditions",
    component: TermsAndConditions,
  },
  {
    path: "/pricing",
    name: "Pricing",
    component: PricingAndRemuneration,
  },
  {
    path: "/token",
    name: "TokenTerms",
    component: TokenTerms,
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
