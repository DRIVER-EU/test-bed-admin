import Vue from 'vue'
import Router from 'vue-router'
import MainPage from '../pages/MainPage';
import OverviewDiagramPage from '../pages/OverviewDiagramPage';

Vue.use(Router);

export default new Router({
  routes: [
    { path: '/', component: MainPage },
    { path: '/overviewDiagram', component: OverviewDiagramPage, name: 'overviewDiagram' },
  ]
})
