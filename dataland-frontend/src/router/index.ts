import { createWebHistory, createRouter, type RouteComponent } from 'vue-router';

const CompanyCockpitPage = (): Promise<RouteComponent> => import('@/components/pages/CompanyCockpitPage.vue');
const LandingPage = (): Promise<RouteComponent> => import('@/components/pages/LandingPage.vue');
import AboutPage from '@/components/pages/AboutPage.vue';

const QualityAssurance = (): Promise<RouteComponent> => import('@/components/pages/QualityAssurance.vue');
const SearchCompaniesForFrameworkData = (): Promise<RouteComponent> =>
  import('@/components/pages/SearchCompaniesForFrameworkData.vue');
const TermsAndConditions = (): Promise<RouteComponent> => import('@/components/pages/TermsAndConditions.vue');
const TokenTerms = (): Promise<RouteComponent> => import('@/components/pages/TokenTerms.vue');
const PricingAndRemuneration = (): Promise<RouteComponent> => import('@/components/pages/PricingAndRemuneration.vue');
const TheImprint = (): Promise<RouteComponent> => import('@/components/pages/TheImprint.vue');
const DataPrivacy = (): Promise<RouteComponent> => import('@/components/pages/DataPrivacy.vue');
const NoContentFound = (): Promise<RouteComponent> => import('@/components/pages/NoContentFound.vue');
const ApiKeysPage = (): Promise<RouteComponent> => import('@/components/pages/ApiKeysPage.vue');
const BulkDataRequest = (): Promise<RouteComponent> => import('@/components/pages/BulkDataRequest.vue');
const SingleDataRequest = (): Promise<RouteComponent> => import('@/components/pages/SingleDataRequest.vue');
const ViewFrameworkData = (): Promise<RouteComponent> => import('@/components/pages/ViewFrameworkData.vue');
const DocumentOverview = (): Promise<RouteComponent> => import('@/components/pages/DocumentOverview.vue');
const DatasetOverview = (): Promise<RouteComponent> => import('@/components/pages/DatasetOverview.vue');
const MyDataRequestsOverview = (): Promise<RouteComponent> => import('@/components/pages/MyDataRequestsOverview.vue');
const PortfolioOverview = (): Promise<RouteComponent> => import('@/components/pages/PortfolioOverview.vue');
const ViewDataRequestPage = (): Promise<RouteComponent> => import('@/components/pages/ViewDataRequestPage.vue');
const UnsubscribeFromMailsPage = (): Promise<RouteComponent> =>
  import('@/components/pages/UnsubscribeFromMailsPage.vue');
const CompanyDataRequestsOverview = (): Promise<RouteComponent> =>
  import('@/components/pages/CompanyDataRequestsOverview.vue');
const UploadFormWrapper = (): Promise<RouteComponent> => import('@/components/pages/UploadFormWrapper.vue');
const ChooseCompanyForFrameworkDataUpload = (): Promise<RouteComponent> =>
  import('@/components/pages/ChooseCompanyForFrameworkDataUpload.vue');
const AdminRequestsOverview = (): Promise<RouteComponent> => import('@/components/pages/AdminAllRequestsOverview.vue');
const ViewTeaserCompanyData = (): Promise<RouteComponent> => import('@/components/pages/ViewTeaserCompanyData.vue');
const ChooseFrameworkForDataUpload = (): Promise<RouteComponent> =>
  import('@/components/pages/ChooseFrameworkForDataUpload.vue');

const routes = [
  {
    path: '/',
    name: 'Welcome to Dataland',
    component: LandingPage,
    meta: {
      requiresAuthentication: false,
    },
  },
  {
    path: '/about',
    name: 'About Page',
    component: AboutPage,
    meta: {
      requiresAuthentication: false,
    },
  },
  {
    path: '/preview',
    name: 'View Sample Data in Preview Mode',
    component: ViewTeaserCompanyData,
    meta: {
      requiresAuthentication: false,
    },
  },
  {
    path: '/companies/choose',
    name: 'Choose Company',
    component: ChooseCompanyForFrameworkDataUpload,
  },
  {
    path: '/companies/:companyID/frameworks/upload',
    props: true,
    name: 'Choose Framework',
    component: ChooseFrameworkForDataUpload,
  },
  {
    path: `/companies/:companyID/frameworks/:frameworkType/upload`,
    props: true,
    name: `Upload framework data`,
    component: UploadFormWrapper,
  },
  {
    path: '/companies',
    name: 'Search Companies for Framework Data',
    component: SearchCompaniesForFrameworkData,
    meta: { initialTabIndex: 0 },
  },
  {
    path: `/companies/:companyId`,
    props: true,
    name: 'Company Cockpit',
    component: CompanyCockpitPage,
    meta: {
      requiresAuthentication: false,
      useLandingPageHeader: false,
    },
  },
  {
    path: `/companies/:companyId/frameworks/:dataType`,
    props: true,
    name: 'Company framework data view page',
    component: ViewFrameworkData,
  },
  {
    path: `/companies/:companyId/frameworks/:dataType/:dataId`,
    props: true,
    name: 'Company framework data view page for specific data ID',
    component: ViewFrameworkData,
  },
  {
    path: `/companies/:companyId/frameworks/:dataType/reportingPeriods/:reportingPeriod`,
    props: true,
    name: 'Company framework data for specific reporting period',
    component: ViewFrameworkData,
  },
  {
    path: `/companies/:companyId/documents`,
    props: true,
    name: 'Document Overview',
    component: DocumentOverview,
  },
  {
    path: `/companies/:companyId/users`,
    props: true,
    name: 'CompanyCockpitPage',
    component: CompanyCockpitPage,
  },
  {
    path: '/requestoverview',
    name: 'Admin overview for all requests',
    component: AdminRequestsOverview,
    meta: { initialTabIndex: 6 },
  },
  {
    path: `/qualityassurance`,
    name: 'UI for quality assurance',
    component: QualityAssurance,
    meta: { initialTabIndex: 3 },
  },
  {
    path: '/datasets',
    name: 'Dataset Overview',
    component: DatasetOverview,
    meta: { initialTabIndex: 1 },
  },
  {
    path: '/requests',
    name: 'MyDataRequestsOverview',
    component: MyDataRequestsOverview,
    meta: { initialTabIndex: 4 },
  },
  {
    path: `/requests/:requestId`,
    name: 'Data Request View Page',
    props: true,
    component: ViewDataRequestPage,
  },
  {
    path: `/companyrequests`,
    name: 'CompanyDataRequestsOverview',
    component: CompanyDataRequestsOverview,
    meta: { initialTabIndex: 5 },
  },
  {
    path: '/bulkdatarequest',
    name: 'Bulk Data Request',
    component: BulkDataRequest,
  },
  {
    path: '/singledatarequest/:companyId',
    name: 'Single Data Request',
    props: true,
    component: SingleDataRequest,
  },
  {
    path: '/portfolios',
    name: 'Portfolio Overview',
    component: PortfolioOverview,
    meta: { initialTabIndex: 2 },
  },
  {
    path: '/api-key',
    name: 'ApiKeysPage',
    component: ApiKeysPage,
  },
  {
    path: '/unsubscribe/:subscriptionId',
    name: 'Unsubscribe from mails',
    props: true,
    component: UnsubscribeFromMailsPage,
  },
  {
    path: '/dataprivacy',
    name: 'DataPrivacy',
    component: DataPrivacy,
    meta: {
      requiresAuthentication: false,
    },
  },
  {
    path: '/terms',
    name: 'TermsAndConditions',
    component: TermsAndConditions,
    meta: {
      requiresAuthentication: false,
    },
  },
  {
    path: '/pricing',
    name: 'Pricing',
    component: PricingAndRemuneration,
    meta: {
      requiresAuthentication: false,
    },
  },
  {
    path: '/token',
    name: 'TokenTerms',
    component: TokenTerms,
    meta: {
      requiresAuthentication: false,
    },
  },
  {
    path: '/imprint',
    name: 'LandingImprint',
    component: TheImprint,
    meta: {
      requiresAuthentication: false,
    },
  },
  {
    path: '/nocontent',
    name: 'NoContentFound',
    component: NoContentFound,
    meta: {
      requiresAuthentication: false,
    },
  },
  {
    path: '/:notFound(.*)',
    redirect: '/nocontent',
    meta: {
      requiresAuthentication: false,
    },
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
