<template>
  <v-container fluid grid-list-xl class="mt-0">
    <v-layout row wrap justify-space-around style="height: 55vh"
              class="scroll-y">
      <v-flex xs4>
        <h3 class="text-xs-center primary--text">Solutions
          <img v-if="overallSolutionState===0" src="../assets/lens-red-green.png" style="float:right">
          <img v-if="overallSolutionState===-1" src="../assets/lens-red.png" style="float:right">
          <img v-if="overallSolutionState===1" src="../assets/lens-green.png" style="float:right">
        </h3>
        <v-btn @click="openConfigureSolutionForm" flat small block class="mx-auto">
          Configure new solution
          <v-icon>add</v-icon>
        </v-btn>
        <v-select
          label="Select which solutions to show"
          :items=solutionTypes
          v-model="solutionSelection"
        ></v-select>
        <list-component :data=solutionsToShow data-type="SOLUTION"></list-component>
      </v-flex>
      <v-flex xs4>
        <h3 class="text-xs-center primary--text">Topics
          <img v-if="overallTopicState===0" src="../assets/lens-red-green.png" style="float:right">
          <img v-if="overallTopicState===-1" src="../assets/lens-red.png" style="float:right">
          <img v-if="overallTopicState===1" src="../assets/lens-green.png" style="float:right">
        </h3>
        <v-btn @click="openConfigureTopicForm" flat small block class="mx-auto">
          Configure new topic
          <v-icon>add</v-icon>
        </v-btn>
        <v-select
          label="Select which topics to show"
          :items=topicTypes
          v-model="topicSelection"
        ></v-select>
        <list-component :data=topicsToShow data-type="TOPIC"></list-component>
      </v-flex>
      <v-flex xs4>
        <h3 class="text-xs-center primary--text">Gateways
          <img v-if="overallGatewayState===0" src="../assets/lens-red-green.png" style="float:right">
          <img v-if="overallGatewayState===-1" src="../assets/lens-red.png" style="float:right">
          <img v-if="overallGatewayState===1" src="../assets/lens-green.png" style="float:right">
        </h3>
        <v-btn @click="openConfigureGatewayForm" flat small block class="mx-auto">
          Configure new gateway
          <v-icon>add</v-icon>
        </v-btn>
        <v-select
          label="There is currently no selection available."
          disabled
        ></v-select>
        <list-component :data=gateways data-type="GATEWAY"></list-component>
      </v-flex>
    </v-layout>
    <v-layout column style="height: 30vh" class="scroll-y">
      <v-flex>
        <v-card>
          <logs-table></logs-table>
        </v-card>
      </v-flex>
    </v-layout>
    <configure-solution-form></configure-solution-form>
    <configure-topic-form></configure-topic-form>
    <configure-gateway-form></configure-gateway-form>
    <v-snackbar
      top
      v-model="snackbar"
      :color="snackbarType"
    >
      {{snackbarText}}
      <v-btn flat color="white" @click.native="snackbar = false">Close</v-btn>
    </v-snackbar>
  </v-container>
</template>

<script>
  import {mapGetters} from 'vuex'
  import {eventBus} from "../main"
  import EventName from '../constants/EventName'
  export default {
    name: "Overview",
    data: () => ({
        snackbar: false,
        snackbarText: '',
        snackbarType: '',
        solutionTypes: ['All', 'Testbed services', 'Solutions'],
        topicTypes: ['All', 'Standard topics', 'Core topics'],
        solutionSelection: 'All',
        topicSelection: 'All'
      }
    ),
    created() {
      const self = this
      eventBus.$on('showSnackbar', (text, type) => {
        self.snackbar = true
        self.snackbarText = text
        self.snackbarType = type
      })
    },
    computed: {
      ...mapGetters(['solutions', 'topics', 'gateways']),
      solutionsToShow: function () {
        let self = this
        return self.solutions.filter((solution) => {
          let showSolution = true
          if ((self.solutionSelection === "Solutions" && solution.isService) || (self.solutionSelection === "Testbed services" && !solution.isService)) {
            showSolution = false
          }
          return showSolution
        })
      },
      topicsToShow: function () {
        let self = this
        return self.topics.filter((topic) => {
          let showTopic = true
          if ((self.topicSelection === "Standard topics" && topic.type.indexOf("core") !== -1) || (self.topicSelection === "Core topics" && topic.type.indexOf("standard") !== -1)) {
            showTopic = false
          }
          return showTopic
        })
      },
      overallSolutionState: function () {
        let solutionsStates = 0
       this.solutions.forEach(solution => {
          if (solution.state) solutionsStates++
        })
        switch (solutionsStates) {
          case 0:
            return -1
          case this.solutions.length:
            return 1
          default:
            return 0
        }
      },
      overallTopicState: function () {
        let topicsStates = 0
        this.topics.forEach(topic => {
          if (topic.state) topicsStates++
        })
        switch (topicsStates) {
          case 0:
            return -1
          case this.topics.length:
            return 1
          default:
            return 0
        }
      },
      overallGatewayState: function () {
        let gatewayStates = 0
        this.gateways.forEach(gateway=> {
          if (gateway.state) gatewayStates++
        })
        switch (gatewayStates) {
          case 0:
            return -1
          case this.gateways.length:
            return 1
          default:
            return 0
        }
      }
    },
    methods: {
      openConfigureSolutionForm: function () {
        eventBus.$emit(EventName.OPEN_SOLUTION_FORM);
      },
      openConfigureTopicForm: function () {
        eventBus.$emit(EventName.OPEN_TOPIC_FORM);
      },
      openConfigureGatewayForm: function () {
        eventBus.$emit(EventName.OPEN_GATEWAY_FORM);
      },

    }
  }
</script>
