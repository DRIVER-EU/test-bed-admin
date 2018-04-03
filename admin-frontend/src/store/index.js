import Vue from 'vue'
import Vuex from 'vuex'
import {_} from 'vue-underscore'
import moment from 'moment'
import {heartbeatController} from '../heartbeatController'
import {LogEntry} from '../objects/logEntry'
import {Solution} from '../objects/solution'
import {Topic} from '../objects/topic'
import {Gateway} from '../objects/gateway'
import {Alert} from '../objects/alert'

Vue.use(Vuex)

export const store = new Vuex.Store({
  state: {
    socket: {
      isConnected: false,
      message: '',
      reconnectError: false,
      pingTimer: null,
      pingTimeOutTimer: null,
      messageAccepted: false
    },
    solutions: [],
    topics: [],
    gateways: [],
    logEntries: [],
    alerts: [],
    loading: false,
    isTestbedInitialized: false,
    isTrialStarted: false
  },
  getters: {
    solutions(state) {
      return state.solutions
    },
    topics(state) {
      return state.topics
    },
    gateways(state) {
      return state.gateways
    },
    logEntries(state) {
      var copy = state.logEntries.slice(0);
      return copy.reverse()
    },
    alerts(state) {
      return state.alerts
    },
    loading(state) {
      return state.loading
    },
    isTestbedInitialized(state) {
      return state.isTestbedInitialized
    },
    isTrialStarted(state) {
      return state.isTrialStarted
    }
  }
  ,
  mutations: {
    SOCKET_ONOPEN(state) {
      console.log('connection open')
      state.socket.isConnected = true
      heartbeatController()
    },
    SOCKET_ONCLOSE(state) {
      console.log('connection closed')
      state.socket.isConnected = false
      clearInterval(state.socket.pingTimer);
      clearInterval(state.socket.pingTimeOutTimer);
    },
    SOCKET_RECONNECT() {
      console.log('reconnect')
    },
    HBRESPONSE(state) {
      state.socket.messageAccepted = true
    },
    LOG_NOTIFICATION(state, log) {
      log.id = Number(log.id);
      state.logEntries.push(new LogEntry(log))
    },
    UPDATE_SOLUTION(state, payload) {
      var obj = state.solutions.find(obj => {
        return obj.id === payload.id
      });
      obj.state = payload.state
    },
    UPDATE_TOPIC(state, payload) {
      var obj = state.topics.find(obj => {
        return obj.id === payload.id
      });
      obj.state = payload.state
    },
    UPDATE_GATEWAY(state, payload) {
      var obj = state.gateways.find(obj => {
        return obj.id === payload.id
      });
      obj.state = payload.state
    },
    GET_SOLUTIONS(state, data) {
      data.solutions.forEach(solution => state.solutions.push(new Solution(solution)))
    },
    GET_TOPICS(state, data) {
      data.topics.forEach(topic => state.topics.push(new Topic(topic)))
    },
    GET_GATEWAYS(state, data) {
      data.gateways.forEach(gateway => state.gateways.push(new Gateway(gateway)))
    },
    GET_LOGS(state, data) {
      data.logs.forEach(logEntry => {
        logEntry.sendDate = moment.utc(logEntry.sendDate).format('YYYY-MM-DD HH:mm:ss.SSS');
        state.logEntries.push(new LogEntry(logEntry));
      })

    },
    ADD_ALERT(state, alert) {
      state.alerts.push(alert)
    },
    TRIAL_STATE_CHANGE(state, isStarted) {
      state.isTrialStarted = isStarted
    },
    TESTBED_STATE_CHANGE(state, isInitialized) {
      state.isTestbedInitialized = isInitialized
    },
    LOADING(state, isTrue) {
      state.loading = isTrue
    }
  }
  ,
  actions: {
    getSolutions(context) {
      this.axios.get('getAllTrialSolutions').then(response => {
        context.commit('GET_SOLUTIONS', (response.data));
      }).catch(function () {
        var alert = new Alert(_.uniqueId(), 'error', 'Solutions could not be loaded. Check that backend is available.', true)
        context.commit('ADD_ALERT', (alert));
      });
    },
    getTopics(context) {
      this.axios.get('getAllTrialTopics').then(response => {
        context.commit('GET_TOPICS', (response.data));
      }).catch(function () {
        var alert = new Alert(_.uniqueId(), 'error', 'Topics could not be loaded. Check if the backend is available.', true)
        context.commit('ADD_ALERT', (alert));
      });
    },
    getGateways(context) {
      this.axios.get('getAllTrialGateways').then(response => {
        context.commit('GET_GATEWAYS', (response.data));
      }).catch(function () {
        var alert = new Alert(_.uniqueId(), 'error', 'Gateways could not be loaded. Check if the backend is available.', true)
        context.commit('ADD_ALERT', (alert));
      });
    },
    getAllLogs(context) {
      this.axios.get('getAllLogs').then(response => {
        context.commit('GET_LOGS', (response.data));
      }).catch(function () {
        var alert = new Alert(_.uniqueId(), 'error', 'Logs could not be loaded. Check if the backend is available.', true)
        context.commit('ADD_ALERT', (alert));
      });
    },
    startTrial(context) {
      this.axios.post('startTrialConfig').then(function () {
        var alert = new Alert(_.uniqueId(), 'success', 'Trial was successfully started.', true)
        context.commit('ADD_ALERT', (alert));
        context.commit('TRIAL_STATE_CHANGE', true);
      }).catch(function () {
        var alert = new Alert(_.uniqueId(), 'error', 'Trial could not be started.', true)
        context.commit('ADD_ALERT', (alert));
      });
    },
    initTestbed(context) {
      this.axios.post('initTestbed').then(function () {
        var alert = new Alert(_.uniqueId(), 'success', 'Testbed was successfully initialized.', true)
        context.commit('ADD_ALERT', (alert));
        context.commit('TESTBED_STATE_CHANGE', true)
        context.commit('LOADING', false)
      }).catch(function () {
        var alert = new Alert(_.uniqueId(), 'error', 'Testbed could not be initialized.', true)
        context.commit('ADD_ALERT', (alert));
        context.commit('LOADING', false)
      });
    },
    isTrialStarted(context) {
      this.axios.get('isTrialStarted').then(response => {
        context.commit('TRIAL_STATE_CHANGE', (response.data));
      }).catch();
    },
    isTestbedInitialized(context) {
      this.axios.get('isTestbedInitialized').then(response => {
        context.commit('TESTBED_STATE_CHANGE', (response.data));
      }).catch();
    }
  }
})
