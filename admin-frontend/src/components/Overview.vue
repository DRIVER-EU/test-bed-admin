<template>
  <v-container fluid grid-list-xl class="mt-0">
    <v-layout row wrap justify-space-around style="height: 55vh"
              class="scroll-y">
      <v-flex xs4>
        <h3 class="text-xs-center primary--text">Solutions</h3>
        <v-btn @click="openConfigureSolutionForm" flat small block class="mx-auto">
          Configure new solution
          <v-icon>create</v-icon>
        </v-btn>
        <v-select
          label="Select which solutions to show"
          :items=solutionTypes
          v-model="solutionSelection"
        ></v-select>
        <list-component :data=solutionsToShow></list-component>
      </v-flex>
      <v-flex xs4>
        <h3 class="text-xs-center primary--text">Topics</h3>
        <v-btn @click="openConfigureTopicForm" flat small block class="mx-auto">
          Configure new topic
          <v-icon>create</v-icon>
        </v-btn>
        <v-select
          label="Select which topics to show"
          :items=topicTypes
          v-model="topicSelection"
        ></v-select>
        <list-component :data=topicsToShow></list-component>
      </v-flex>
      <v-flex xs4>
        <h3 class="text-xs-center primary--text">Gateways</h3>
        <v-btn @click="openConfigureGatewayForm" flat small block class="mx-auto">
          Configure new gateway
          <v-icon>create</v-icon>
        </v-btn>
        <v-select
          label="There is currently no selection available."
          disabled
        ></v-select>
        <list-component :data=gateways></list-component>
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

  export default {
    name: "Overview",
    data: () => ({
        snackbar: false,
        snackbarText: '',
        snackbarType: '',
        solutionTypes: ['All', 'Testbed services', 'Solutions'],
        topicTypes: ['All', 'Standard topics', 'Core topics'],
        solutionSelection: 'All',
        topicSelection: 'All',
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
        return this.solutions.filter((solution) => {
          let showSolution = true
          // exclude solutions that don't meet the filter criteria
          if ((this.solutionSelection === "Solutions" && solution.isService) || (this.solutionSelection === "Testbed services" && !solution.isService) ) {
            showSolution = false
          }
          return showSolution
        })
      },
      topicsToShow: function() {
        return this.topics.filter((topic) => {
          let showTopic = true
          // exclude topics that don't meet the filter criteria
          if ((this.topicSelection === "Standard topics" && topic.type.indexOf("core") != -1) || (this.solutionSelection === "Core topics" && topic.type.indexOf("standard") != -1) ) {
            showTopic = false
          }
          return showTopic
        })
      }
    }
    ,
    methods: {
      openConfigureSolutionForm: function () {
        eventBus.$emit('openConfigureSolutionForm')
      },
      openConfigureTopicForm: function () {
        eventBus.$emit('openConfigureTopicForm')
      },
      openConfigureGatewayForm: function () {
        eventBus.$emit('openConfigureGatewayForm')
      },

    }
  }
</script>
