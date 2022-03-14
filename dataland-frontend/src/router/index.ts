import { createWebHistory, createRouter } from "vue-router";
import CreateData from "@/components/forms/CreateData.vue";
import SearchData from "@/components/forms/SearchData.vue";
import WelcomeDataland from "@/components/WelcomeDataland.vue";

const routes = [
    {
        path: "/",
        name: "Welcome to Dataland",
        component: WelcomeDataland,
    },
    {
        path: "/upload",
        name: "Create Data",
        component: CreateData,
    },
    {
        path: "/search",
        name: "Search Data",
        component: SearchData,
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;