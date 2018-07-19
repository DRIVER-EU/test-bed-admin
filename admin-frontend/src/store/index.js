import Vue from 'vue'
import Vuex from 'vuex'
import moment from 'moment'
import {eventBus} from "../main";
import {heartbeatController} from '../heartbeatController'
import {LogEntry} from '../objects/logEntry'
import {Solution} from '../objects/solution'
import {Topic} from '../objects/topic'
import {Gateway} from '../objects/gateway'

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
    standards: [],
    topicTypes: [],
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
      return state.logEntries.sort((a, b) => {
        return b.id - a.id
      })
    },
    standards(state) {
      return state.standards
    },
    standardNames(state) {
      let standardNames = []
      state.standards.forEach(standard => standardNames.push(standard.name))
      return standardNames
    },
    topicTypes(state) {
      return state.topicTypes
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
      console.log(payload.id)
      var obj = state.solutions.find(obj => {
        console.log(obj.clientId)
        return obj.clientId === payload.id
      });
      obj.state = payload.state
    },
    UPDATE_TOPIC(state, payload) {
      console.log(payload.id)
      var obj = state.topics.find(obj => {
        return obj.clientId === payload.id
      });
      obj.state = payload.state
    },
    UPDATE_GATEWAY(state, payload) {
      var obj = state.gateways.find(obj => {
        return obj.clientId === payload.id
      });
      obj.state = payload.state
    },
    SET_SOLUTION(state, solution) {
      state.solutions.push(new Solution(solution))
      eventBus.$emit('updateSolutionIds', solution.clientId)
    },
    SET_TOPIC(state, topic) {
      state.topics.push(new Topic(topic))
    },
    SET_GATEWAY(state, gateway) {
      state.gateways.push(new Gateway(gateway))

    },
    SET_LOG(state, data) {
      data.logs.forEach(logEntry => {
        logEntry.sendDate = moment.utc(logEntry.sendDate).format('YYYY-MM-DD HH:mm:ss.SSS');
        state.logEntries.push(new LogEntry(logEntry));
      })

    },
    SET_STANDARDS(state, standards) {
      state.standards = standards
    },
    SET_TOPIC_TYPES(state, topicTypes) {
      state.topicTypes = topicTypes
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
        response.data.solutions.forEach(solution => context.commit('SET_SOLUTION', (solution)))
      }).catch(e => {
        eventBus.$emit('showSnackbar', 'Data could not be loaded. (' + e + ')', 'error')
      });
    },
    getTopics(context) {
      this.axios.get('getAllTrialTopics').then(response => {
        response.data.topics.forEach(topic => context.commit('SET_TOPIC', (topic)))
      }).catch(e => {
        eventBus.$emit('showSnackbar', 'Data could not be loaded. (' + e + ')', 'error')
      });
    },
    getGateways(context) {
      this.axios.get('getAllTrialGateways').then(response => {
        response.data.gateways.forEach(gateway => context.commit('SET_GATEWAY', (gateway)))
      }).catch(e => {
        eventBus.$emit('showSnackbar', 'Data could not be loaded. (' + e + ')', 'error')
      });
    },
    getAllLogs(context) {
      this.axios.get('getAllLogs').then(response => {
        context.commit('SET_LOG', (response.data));
      }).catch(e => {
        eventBus.$emit('showSnackbar', 'Data could not be loaded. (' + e + ')', 'error')
      });
    },
    startTrial(context) {
      this.axios.post('startTrialConfig').then(function () {
        eventBus.$emit('showSnackbar', 'Trial started.', 'success')
        context.commit('TRIAL_STATE_CHANGE', true);
      }).catch(e => {
        eventBus.$emit('showSnackbar', 'Trial could not be started. (' + e + ')', 'error')
      });
    },
    initTestbed(context) {
      this.axios.post('initTestbed').then(function () {
        eventBus.$emit('showSnackbar', 'Testbed initialized.', 'success')
        context.commit('TESTBED_STATE_CHANGE', true)
        context.commit('LOADING', false)
      }).catch(e => {
        eventBus.$emit('showSnackbar', 'Testbed could not be initialized. (' + e + ')', 'error')
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
    },
    addSolution(context, solution) {
      this.axios.post('addSolution', solution).then(response => {
        eventBus.$emit('showSnackbar', 'Data was successfully submitted.', 'success')
        context.commit('SET_SOLUTION', (response.data));
      })
        .catch(e => {
          eventBus.$emit('showSnackbar', 'Data was not submitted. (' + e + ')', 'error')
        })
    },
    addGateway(context, gateway) {
      this.axios.post('addGateway', gateway).then(response => {
        eventBus.$emit('showSnackbar', 'Data was successfully submitted.', 'success')
        context.commit('SET_GATEWAY', (response.data))
      })
        .catch(e => {
          eventBus.$emit('showSnackbar', 'Data was not submitted. (' + e + ')', 'error')
        })
    },
    addTopic(context, topic) {
      this.axios.post('addTopic', topic).then(response => {
        eventBus.$emit('showSnackbar', 'Data was successfully submitted.', 'success')
        context.commit('SET_TOPIC', (response.data))
      })
        .catch(e => {
          eventBus.$emit('showSnackbar', 'Data was not submitted. (' + e + ')', 'error')
        })
    },
    getAllStandards(context) {
      this.axios.get('getAllStandards').then(response => {
        context.commit('SET_STANDARDS', (response.data));
      })
    },
    getAllTopicTypes(context) {
      this.axios.get('getAllTopicTypes').then(response => {
        context.commit('SET_TOPIC_TYPES', (response.data));
      })
    }
  }
})
