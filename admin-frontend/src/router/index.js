import Vue from 'vue'
import Router from 'vue-router'
import Tools from '@/components/Tools'
import Overview from '@/components/Overview'
import ConfigureGateways from '@/components/ConfigureGateways'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      redirect: '/overview'
    },
    {
      path: '/overview',
      name: 'Overview',
      component: Overview
    }
  ]
})
