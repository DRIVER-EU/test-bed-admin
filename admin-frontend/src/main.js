import Vue from 'vue'
import App from './App'
import router from './router'
import Vuetify from 'vuetify'
import {store} from './store'
import 'vuetify/dist/vuetify.min.css'
import VueNativeSock from 'vue-native-websocket'
import axios from 'axios'
import VueAxios from 'vue-axios'
import underscore from 'vue-underscore'
import ListComponent from './components/ListComponent'
import LogsTable from './components/LogsTable'
import ConfigureSolutionForm from './components/configurationForms/ConfigureSolutionForm'
import ConfigureTopicForm from './components/configurationForms/ConfigureTopicForm'
import ConfigureGatewayForm from './components/configurationForms/ConfigureGatewayForm'

export const eventBus = new Vue()
const host = window.location.host
/*const httpUrl = 'http://' + host + '/AdminService'
const wsUrl = 'ws://'+ host + '/AdminServiceWSEndpoint'*/
const httpUrl = 'http://localhost:8090/AdminService'
const wsUrl = 'ws://localhost:8090/AdminServiceWSEndpoint'

Vue.use(underscore)

Vue.use(VueAxios, axios.create({
  baseURL: httpUrl
}))
store.axios = Vue.prototype.axios

Vue.use(VueNativeSock, wsUrl, {
  store: store,
  format: 'json',
  reconnection: true, // (Boolean) whether to reconnect automatically (false)
  reconnectionDelay: 2000
})
store.$socket = Vue.prototype.$socket

Vue.use(Vuetify, {
  theme: {
    primary: '#FDB836',
    secondary: '#b0bec5',
    tertiary: '#fff8dc7a',
    accent: '#8c9eff',
    error: '#b71c1c'
  }
})

Vue.component('list-component', ListComponent)
Vue.component('logs-table', LogsTable)
Vue.component('configure-solution-form',ConfigureSolutionForm)
Vue.component('configure-topic-form',ConfigureTopicForm)
Vue.component('configure-gateway-form',ConfigureGatewayForm)

Vue.config.productionTip = false

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  render: h => h(App),
  created() {
    this.$store.dispatch('getSolutions'),
      this.$store.dispatch('getTopics'),
      this.$store.dispatch('getGateways'),
      this.$store.dispatch('getAllLogs'),
      this.$store.dispatch('isTestbedInitialized'),
      this.$store.dispatch('isTrialStarted'),
      this.$store.dispatch('getAllStandards'),
      this.$store.dispatch('getAllTopicTypes')
  }
})
