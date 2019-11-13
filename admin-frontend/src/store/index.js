import Vue from 'vue'
import Vuex from 'vuex'
import moment from 'moment'
import {eventBus} from '../main'
import {heartbeatController} from '../heartbeatController'
import {LogEntry} from '../objects/logEntry'
import {Solution} from '../objects/solution'
import {Topic} from '../objects/topic'
import {Gateway} from '../objects/gateway'
import Settings from '../constants/Settings'
import {fetchService} from '../service/FetchService'
import EventName from '../constants/EventName'

Vue.use(Vuex);

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
    logsPageCount: 1,
    standards: [],
    topicTypes: [],
    isTestbedInitialized: false,
    isTrialStarted: false,
    configurations: [],
    modes: [],
    currentConfiguration: {},
    organisations: [],
    solutionCertificates: {},
  },
  getters: {
    solutions (state) {
      return state.solutions
    },
    topics (state) {
      return state.topics
    },
    gateways (state) {
      return state.gateways
    },
    logEntries (state) {
      return state.logEntries.sort((a, b) => {
        return b.id - a.id
      })
    },
    standards (state) {
      return state.standards
    },
    standardNames (state) {
      let standardNames = [];
      state.standards.forEach(standard => standardNames.push(standard.name));
      return standardNames
    },
    topicTypes (state) {
      return state.topicTypes
    },
    isTestbedInitialized (state) {
      return state.isTestbedInitialized
    },
    isTrialStarted (state) {
      return state.isTrialStarted
    },
    configurations (state) {
      return state.configurations
    },
    modes (state) {
      return state.modes
    },
    currentConfiguration (state) {
      return state.currentConfiguration
    },
    organisations (state) {
      return state.organisations
    },
    solutionCertificates (state) {
      return state.solutionCertificates
    }
  },
  mutations: {
    SOCKET_ONOPEN (state) {
      console.log('Web socket connection opened.');
      state.socket.isConnected = true;
      heartbeatController()
    },
    SOCKET_ONCLOSE (state) {
      console.log('Web socket connection closed.');
      state.socket.isConnected = false;
      if (state.socket.pingTimer) {
        console.log("Stopped heartbeat timer", state.socket.pingTimer);
        clearInterval(state.socket.pingTimer);
        state.socket.pingTimer = null
      }
      if (state.socket.pingTimeOutTimer) {
        console.log("Stopped heartbeat timeout timer", state.socket.pingTimeOutTimer);
        clearInterval(state.socket.pingTimeOutTimer);
        state.socket.pingTimeOutTimer = null
      }
    },
    SOCKET_RECONNECT (state) {
      console.log('Web socket reconnected.');
      state.socket.isConnected = true;
      heartbeatController()
    },
    SOCKET_ONMESSAGE(state, msg) {
      console.log('Received unhandled message', msg)
    },
    HBRESPONSE (state) {
      state.socket.messageAccepted = true
    },
    LOG_NOTIFICATION (state, log) {
      log.id = Number(log.id);
      state.logEntries.push(new LogEntry(log))
    },
    UPDATE_SOLUTION (state, payload) {
      const obj = state.solutions.find(obj => obj.clientId === payload.id);
      if (obj) {
        obj.state = payload.state
      }
    },
    UPDATE_TOPIC (state, payload) {
      const obj = state.topics.find(obj => obj.clientId === payload.id);
      if (obj) {
        obj.state = payload.state
      }
    },
    UPDATE_GATEWAY (state, payload) {
      const obj = state.gateways.find(obj => obj.clientId === payload.id);
      if (obj) {
        obj.state = payload.state
      }
    },
    SET_SOLUTIONS (state, solutions) {
      state.solutions = [];
      solutions.forEach(solution => {
        state.solutions.push(new Solution(solution));
        eventBus.$emit(EventName.ADD_SOLUTION_ID, solution.clientId)
      })
    },
    ADD_SOLUTION (state, solution) {
      state.solutions.push(new Solution(solution));
      eventBus.$emit(EventName.ADD_SOLUTION_ID, solution.clientId)
    },
    REMOVE_SOLUTION (state, solution) {
      const id = solution.id;
      state.solutions = [...state.solutions.filter(s => s.id !== id)];
      eventBus.$emit(EventName.REMOVE_SOLUTION_ID, solution.clientId)
    },
    SET_TOPICS (state, topics) {
      state.topics = [];
      topics.forEach(topic => state.topics.push(new Topic(topic)))
    },
    ADD_TOPIC (state, topic) {
      state.topics.push(new Topic(topic))
    },
    REMOVE_TOPIC (state, topic) {
      const id = topic.id;
      state.topics = [...state.topics.filter(s => s.id !== id)]
    },
    SET_GATEWAYS (state, gateways) {
      state.gateways = [];
      gateways.forEach(gateway => state.gateways.push(new Gateway(gateway)))
    },
    ADD_GATEWAY (state, gateway) {
      state.gateways.push(new Gateway(gateway))
    },
    REMOVE_GATEWAY (state, gateway) {
      const id = gateway.id;
      state.gateways = [...state.gateways.filter(s => s.id !== id)]
    },
    GET_LOGS (state, data) {
      state.logEntries = [];
      data.logs.forEach(logEntry => {
        logEntry.sendDate = moment.utc(logEntry.sendDate).format('YYYY-MM-DD HH:mm:ss.SSS');
        state.logEntries.push(new LogEntry(logEntry))
      })
    },
    GET_LOGS_PAGE_COUNT (state, count) {
      // console.log("Received records page count", count);
      state.logsPageCount = count
    },
    SET_STANDARDS (state, standards) {
      state.standards = standards
    },
    SET_TOPIC_TYPES (state, topicTypes) {
      state.topicTypes = topicTypes
    },
    TRIAL_STATE_CHANGE (state, isStarted) {
      state.isTrialStarted = isStarted
    },
    TESTBED_STATE_CHANGE (state, isInitialized) {
      state.isTestbedInitialized = isInitialized
    },
    SET_CONFIGURATIONS (state, configurations) {
      state.configurations = configurations
    },
    SET_MODES (state, modes) {
      state.modes = modes
    },
    SET_CURRENT_CONFIGURATION (state, currentConfiguration) {
      state.currentConfiguration = currentConfiguration
    },
    SET_ORGANISATIONS (state, organisations) {
      state.organisations = organisations
    },
    SET_SOLUTION_CERTIFICATES (state, solutionCertificates) {
      state.solutionCertificates = solutionCertificates
    },
    ADD_SOLUTION_CERTIFICATE (state, payload) {
      const solution = payload.solution;
      const fileName = payload.fileName;
      const additionalItems = {};
      additionalItems[solution] = fileName;
      state.solutionCertificates = {...state.solutionCertificates, ...additionalItems}
    },
  },
  actions: {
    getSolutions (context) {
      fetchService.performGet('getAllTrialSolutions').then(response => {
        context.commit('SET_SOLUTIONS', response.data.solutions)
      }).catch(e => {
        eventBus.$emit('showSnackbar', 'Data could not be loaded. (' + e + ')', 'error')
      })
    },
    getTopics (context) {
      fetchService.performGet('getAllTrialTopics').then(response => {
        context.commit('SET_TOPICS', response.data.topics)
      }).catch(e => {
        eventBus.$emit('showSnackbar', 'Data could not be loaded. (' + e + ')', 'error')
      })
    },
    getGateways (context) {
      fetchService.performGet('getAllTrialGateways').then(response => {
        context.commit('SET_GATEWAYS', response.data.gateways)
      }).catch(e => {
        eventBus.$emit('showSnackbar', 'Data could not be loaded. (' + e + ')', 'error')
      })
    },
    getLogs (context, payload) {
      const page = payload ? payload.page : null;
      const url = page ? 'getAllLogs?size=' + Settings.PAGE_SIZE + '&page=' + page : 'getAllLogs';
      fetchService.performGet(url).then(response => {
        console.log('/getAllLogs returned count', response.data.logs.length);
        context.commit('GET_LOGS', (response.data))
      }).catch(e => {
        eventBus.$emit('showSnackbar', 'Data could not be loaded. (' + e + ')', 'error')
      })
    },
    getPageCount (context) {
      fetchService.performGet('getPageCount').then(response => {
        context.commit('GET_LOGS_PAGE_COUNT', (response.data))
      }).catch(ex => console.log(ex))
    },
    startTrialSuccess(context) {
      context.commit('TRIAL_STATE_CHANGE', true);
    },
    initTestbedSuccess(context) {
      context.commit('TESTBED_STATE_CHANGE', true);
    },
    isTrialStarted (context) {
      fetchService.performGet('isTrialStarted').then(response => {
        context.commit('TRIAL_STATE_CHANGE', (response.data))
      }).catch()
    },
    isTestbedInitialized (context) {
      fetchService.performGet('isTestbedInitialized').then(response => {
        context.commit('TESTBED_STATE_CHANGE', (response.data))
      }).catch()
    },
    addSolution (context, solution) {
      fetchService.performPost('addSolution', solution).then(response => {
        eventBus.$emit('showSnackbar', 'Data was successfully submitted.', 'success');
        context.commit('ADD_SOLUTION', (response.data))
      }).catch(e => {
        eventBus.$emit('showSnackbar', 'Data was not submitted. (' + e + ')', 'error')
      })
    },
    addGateway (context, gateway) {
      fetchService.performPost('addGateway', gateway).then(response => {
        eventBus.$emit('showSnackbar', 'Data was successfully submitted.', 'success');
        context.commit('ADD_GATEWAY', (response.data))
      }).catch(e => {
        eventBus.$emit('showSnackbar', 'Data was not submitted. (' + e + ')', 'error')
      })
    },
    addTopic (context, topic) {
      fetchService.performPost('addTopic', topic).then(response => {
        eventBus.$emit('showSnackbar', 'Data was successfully submitted.', 'success');
        context.commit('ADD_TOPIC', (response.data))
      }).catch(e => {
        eventBus.$emit('showSnackbar', 'Data was not submitted. (' + e + ')', 'error')
      })
    },
    updateSolution (context, solution) {
      fetchService.performPut('updateSolution', solution).then(response => {
        eventBus.$emit('showSnackbar', 'Data was successfully submitted.', 'success');
        context.commit('UPDATE_SOLUTION', response.data);
      }).catch(e => {
        eventBus.$emit('showSnackbar', 'Data was not submitted. (' + e + ')', 'error')
      })
    },
    updateGateway (context, gateway) {
      fetchService.performPut('updateGateway', gateway).then(response => {
        eventBus.$emit('showSnackbar', 'Data was successfully submitted.', 'success');
        context.commit('UPDATE_GATEWAY', response.data);
      }).catch(e => {
        eventBus.$emit('showSnackbar', 'Data was not submitted. (' + e + ')', 'error')
      })
    },
    updateTopic (context, topic) {
      fetchService.performPut('updateTopic', topic).then(response => {
        eventBus.$emit('showSnackbar', 'Data was successfully submitted.', 'success');
        context.commit('UPDATE_TOPIC', response.data);
      }).catch(e => {
        eventBus.$emit('showSnackbar', 'Data was not submitted. (' + e + ')', 'error')
      })
    },
    removeSolution (context, payload) {
      const item = payload.item;
      fetchService.performDelete('removeSolution/' + item.id).then(response => {
        eventBus.$emit('showSnackbar', 'Solution was deleted.', 'success');
        context.commit('REMOVE_SOLUTION', item)
      }).catch(e => {
        eventBus.$emit('showSnackbar', 'Solution was not deleted. (' + e + ')', 'error')
      })
    },
    removeGateway (context, payload) {
      const item = payload.item;
      fetchService.performDelete('removeGateway/' + item.id).then(response => {
        eventBus.$emit('showSnackbar', 'Gateway was deleted.', 'success');
        context.commit('REMOVE_GATEWAY', item)
      }).catch(e => {
        eventBus.$emit('showSnackbar', 'Gateway was not deleted. (' + e + ')', 'error')
      })
    },
    removeTopic (context, payload) {
      const item = payload.item;
      fetchService.performDelete('removeTopic/' + item.id).then(response => {
        eventBus.$emit('showSnackbar', 'Topic was deleted.', 'success');
        context.commit('REMOVE_TOPIC', item)
      }).catch(e => {
        eventBus.$emit('showSnackbar', 'Topic was not deleted. (' + e + ')', 'error')
      })
    },
    getAllStandards (context) {
      fetchService.performGet('getAllStandards').then(response => {
        context.commit('SET_STANDARDS', (response.data))
      })
    },
    getAllTopicTypes (context) {
      fetchService.performGet('getAllTopicTypes').then(response => {
        context.commit('SET_TOPIC_TYPES', (response.data))
      })
    },
    getConfigurations (context) {
      fetchService.performGet('getAllConfigurations').then(response => {
        context.commit('SET_CONFIGURATIONS', (response.data))
      })
    },
    getModes (context) {
      fetchService.performGet('getAllTestbedModes').then(response => {
        context.commit('SET_MODES', (response.data))
      })
    },
    getCurrentConfiguration (context) {
      fetchService.performGet('getActTestbedConfig').then(response => {
        context.commit('SET_CURRENT_CONFIGURATION', (response.data))
      })
    },
    getAllOrganisations (context) {
      fetchService.performGet('getAllOrganisations').then(response => {
        context.commit('SET_ORGANISATIONS', (response.data))
      })
    },
    getSolutionCertificates (context) {
      fetchService.performGet('getSolutionsCertMap').then(response => {
        context.commit('SET_SOLUTION_CERTIFICATES', (response.data))
      })
    },
    addSolutionCertificate (context, payload) {
      context.commit('ADD_SOLUTION_CERTIFICATE', payload);
    },
  }
});
