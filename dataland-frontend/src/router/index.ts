import { createWebHistory, createRouter, type RouteComponent } from 'vue-router';

const CompanyCockpitPage = (): Promise<RouteComponent> => import('@/components/pages/CompanyCockpitPage.vue');
const NewLandingPage = (): Promise<RouteComponent> => import('@/components/pages/NewLandingPage.vue');
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
const DatasetOverview = (): Promise<RouteComponent> => import('@/components/pages/DatasetOverview.vue');
const MyDataRequestsOverview = (): Promise<RouteComponent> => import('@/components/pages/MyDataRequestsOverview.vue');
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
    component: NewLandingPage,
  },
  {
    path: '/about',
    name: 'About Page',
    component: AboutPage,
  },
  {
    path: '/preview',
    name: 'View Sample Data in Preview Mode',
    component: ViewTeaserCompanyData,
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
  },
  {
    path: `/companies/:companyId`,
    props: true,
    name: 'Company Cockpit',
    component: CompanyCockpitPage,
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
    path: '/requestoverview',
    name: 'Admin overview for all requests',
    component: AdminRequestsOverview,
  },
  {
    path: `/qualityassurance`,
    name: 'UI for quality assurance',
    component: QualityAssurance,
  },
  {
    path: '/datasets',
    name: 'Dataset Overview',
    component: DatasetOverview,
  },
  {
    path: '/requests',
    name: 'MyDataRequestsOverview',
    component: MyDataRequestsOverview,
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
  },
  {
    path: '/terms',
    name: 'TermsAndConditions',
    component: TermsAndConditions,
  },
  {
    path: '/pricing',
    name: 'Pricing',
    component: PricingAndRemuneration,
  },
  {
    path: '/token',
    name: 'TokenTerms',
    component: TokenTerms,
  },
  {
    path: '/imprint',
    name: 'LandingImprint',
    component: TheImprint,
  },
  {
    path: '/nocontent',
    name: 'NoContentFound',
    component: NoContentFound,
  },
  {
    path: '/:notFound(.*)',
    redirect: '/nocontent',
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
