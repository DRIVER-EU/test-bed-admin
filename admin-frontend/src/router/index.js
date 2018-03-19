import Vue from 'vue'
import Router from 'vue-router'
import Tools from '@/components/Tools'
import Overview from '@/components/Overview'
import ConfigureGateways from '@/components/ConfigureGateways'

Vue.use(Router)

export default new Router({
  routes: [
    {path: '/overview',
      name: 'Overview',
      component: Overview
    },
    {path: '/tools',
      name: 'Tools',
      component: Tools
    },
    {path: '/configureGateways',
      name: 'ConfigureGateways',
      component: ConfigureGateways
    }
  ]
})
