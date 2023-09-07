import { createWebHistory, createRouter, type RouteComponent, type RouteLocationNormalized } from "vue-router";
const WelcomeDataland = (): Promise<RouteComponent> => import("@/components/pages/WelcomeDataland.vue");
const QualityAssurance = (): Promise<RouteComponent> => import("@/components/pages/QualityAssurance.vue");
const SearchCompaniesForFrameworkData = (): Promise<RouteComponent> =>
  import("@/components/pages/SearchCompaniesForFrameworkData.vue");
const TheImprint = (): Promise<RouteComponent> => import("@/components/pages/TheImprint.vue");
const DataPrivacy = (): Promise<RouteComponent> => import("@/components/pages/DataPrivacy.vue");
const NoContentFound = (): Promise<RouteComponent> => import("@/components/pages/NoContentFound.vue");
const UploadEuTaxonomyDataForFinancials = (): Promise<RouteComponent> =>
  import("@/components/pages/UploadEuTaxonomyDataForFinancials.vue");
const ApiKeysPage = (): Promise<RouteComponent> => import("@/components/pages/ApiKeysPage.vue");
const RequestData = (): Promise<RouteComponent> => import("@/components/pages/RequestData.vue");
const ViewFrameworkData = (): Promise<RouteComponent> => import("@/components/pages/ViewFrameworkData.vue");
const DatasetOverview = (): Promise<RouteComponent> => import("@/components/pages/DatasetOverview.vue");
const UploadFormWrapper = (): Promise<RouteComponent> => import("@/components/pages/UploadFormWrapper.vue");
const ChooseCompanyForFrameworkDataUpload = (): Promise<RouteComponent> =>
  import("@/components/pages/ChooseCompanyForFrameworkDataUpload.vue");

const ViewTeaserCompanyData = (): Promise<RouteComponent> =>
    import("@/components/pages/ViewTeaserCompanyData.vue");
const ChooseFrameworkForDataUpload = (): Promise<RouteComponent> =>
  import("@/components/pages/ChooseFrameworkForDataUpload.vue");
import { DataTypeEnum } from "@clients/backend";
// TODO Upload page von P2p importen
const routes = [
  {
    path: "/",
    name: "Welcome to Dataland",
    component: WelcomeDataland,
    props: {
      isMobile: /Android|webOS|iPhone|iPad|iPod|BlackBerry|Windows Phone|IEMobile|OperaMini/i.test(navigator.userAgent),
    },
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
    path: `/companies/:companyID/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/upload/`,
    props: (route: RouteLocationNormalized): object => ({
      companyID: route.params.companyID,
      frameworkType: DataTypeEnum.EutaxonomyNonFinancials,
    }),
    name: "Upload new Eu Taxonomy Data For Non-Financials",
    component: UploadFormWrapper,
  },
  {
    path: `/companies/:companyID/frameworks/${DataTypeEnum.EutaxonomyFinancials}/upload`,
    props: true,
    name: "Upload Eu Taxonomy Data For Financials",
    component: UploadEuTaxonomyDataForFinancials,
  },
  {
    path: `/companies/:companyID/frameworks/${DataTypeEnum.Lksg}/upload`, // TODO Emanuel: code duplication
    props: (route: RouteLocationNormalized): object => ({
      companyID: route.params.companyID,
      frameworkType: DataTypeEnum.Lksg,
    }),
    name: "Upload lkSG Data",
    component: UploadFormWrapper,
  },
  {
    path: `/companies/:companyID/frameworks/${DataTypeEnum.Sfdr}/upload`,
    props: (route: RouteLocationNormalized): object => ({
      companyID: route.params.companyID,
      frameworkType: DataTypeEnum.Sfdr,
    }),
    name: "Upload SFDR Data",
    component: UploadFormWrapper,
  },
  {
    path: `/companies/:companyID/frameworks/${DataTypeEnum.P2p}/upload`,
    props: (route: RouteLocationNormalized): object => ({
      companyID: route.params.companyID,
      frameworkType: DataTypeEnum.P2p,
    }),
    name: "Upload P2p Data",
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
    name: "Company EU Taxonomy for specific reporting period",
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
