import Vue from 'vue';
// Activating vue extension for chrome in production mode
// This code should be right after importing the vue component to work properly
// See https://stackoverflow.com/questions/43781351/vue-js-is-detected-on-this-page-devtools-inspection-is-not-available-because-it
Vue.config.devtools = true
import App from './App.vue';
import "bootstrap";
import "bootstrap/dist/css/bootstrap.css";
import FundamentalVue from 'fundamental-vue';
import VueRouter from "vue-router";
import moment from 'moment';
import FranchisesOverview from "./components/FranchisesOverview.vue";
import FranchiseDetails from "./components/FranchiseDetails.vue";
import AdminCorner from "./components/AdminCorner.vue";
import InfoPage from "./components/InfoPage.vue";
import UserProfile from "./components/UserProfile.vue";

Vue.use(FundamentalVue);
Vue.config.productionTip = false;
Vue.use(VueRouter);

const originalPush = VueRouter.prototype.push
// Rewrite the push method on the prototype and handle the error message uniformly
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}

// Adding the different pages to the router
const router = new VueRouter({
  routes: [
    {
      path: "*",
      component: FranchisesOverview
    },
    {
      path: "/franchise-details",
      component: FranchiseDetails
    },
    {
      path: "/admin-corner",
      component: AdminCorner
    },
    {
      path: "/info-page",
      component: InfoPage
    },
    {
      path: "/user-profile",
      component: UserProfile
    }
  ]
});

// Filter to format date in the app
Vue.filter('formatDate', function(value) {
  if (value) {
    return moment(String(value)).format('MM/DD/YYYY')
  }
})

// Defining the url of the backend apis
Vue.prototype.$backendApi = "/backend/easyfranchise/rest/efservice/v1";
//Vue.prototype.$backendApi = "http://localhost:8080/easyfranchise/rest/efservice/v1";
// Change $backendApi to below url for consuming backend without tenant id in header (only deployed on dev landscape)
// Vue.prototype.$backendApi = "https://efservice.c-97d8b1a.kyma.shoot.live.k8s-hana.ondemand.com/easyfranchise/rest/efservice/v1";

// Rendering the main vue component and adding the router to it.
new Vue({
  render: h => h(App),
  router
}).$mount('#app')
