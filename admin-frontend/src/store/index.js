import Vue from 'vue'
import Vuex from 'vuex'
import {_} from 'vue-underscore'
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
    testbedAvailable: false
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
      return state.logEntries
    },
    alerts(state) {
      return state.alerts
    },
    testbedAvailable(state) {
      return state.testbedAvailable
    }
  },
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
    SOCKET_ONERROR(state) {
    },
    SOCKET_RECONNECT() {
      console.log('reconnect')
    },
    HBRESPONSE(state) {
      state.socket.messageAccepted = true
    },
    LOG_NOTIFICATION(state, log) {
      log.date = new Date(log.date).toUTCString()
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
      data.logs.forEach(logEntry => state.logEntries.push(new LogEntry(logEntry)))
    },
    ADD_ALERT(state, alert) {
      state.alerts.push(alert)
    },
    TESTBED_AVAILABLE(state, isAvailable) {
      state.testbedAvailable = isAvailable
    }
  }
  ,
  actions: {
    getSolutions(context) {
      this.axios.get('getAllTrialSolutions').then(response => {
        context.commit('GET_SOLUTIONS', (response.data));
      }).catch(function(){
        var alert = new Alert(_.uniqueId(), 'error', 'Solutions could not be loaded. Check that backend is available.', true)
        context.commit('ADD_ALERT', (alert));
      });
    },
    getTopics(context) {
      this.axios.get('getAllTrialTopics').then(response => {
        context.commit('GET_TOPICS', (response.data));
      }).catch(function(){
        var alert = new Alert(_.uniqueId(), 'error', 'Topics could not be loaded. Check if the backend is available.', true)
        context.commit('ADD_ALERT', (alert));
      });
    },
    getGateways(context) {
      this.axios.get('getAllTrialGateways').then(response => {
        context.commit('GET_GATEWAYS', (response.data));
      }).catch(function(){
        var alert = new Alert(_.uniqueId(), 'error', 'Gateways could not be loaded. Check if the backend is available.', true)
        context.commit('ADD_ALERT', (alert));
      });
    },
    getAllLogs(context) {
      this.axios.get('getAllLogs').then(response => {
        context.commit('GET_LOGS', (response.data));
        console.log(response.data)
      }).catch(function(){
        var alert = new Alert(_.uniqueId(), 'error', 'Logs could not be loaded. Check if the backend is available.', true)
        context.commit('ADD_ALERT', (alert));
      });
    },
    startTrial(context) {
      this.axios.post('startTrialConfig').then(function () {
        var alert = new Alert(_.uniqueId(), 'success', 'Trial was successfully started.', true)
        context.commit('ADD_ALERT', (alert));
      }).catch(function(){
        var alert = new Alert(_.uniqueId(), 'error', 'Trial could not be started.', true)
        context.commit('ADD_ALERT', (alert));
      });
    },
    initTestbed(context) {
      this.axios.post('initTestbed').then(function () {
        var alert = new Alert(_.uniqueId(), 'succes', 'Testbed was successfully initialized.', true)
        context.commit('ADD_ALERT', (alert));
        context.commit('TESTBED_AVAILABLE', true)
      }).catch(function(){
        var alert = new Alert(_.uniqueId(), 'error', 'Testbed could not be initialized.', true)
        context.commit('ADD_ALERT', (alert));
      });
    }
  }
})
