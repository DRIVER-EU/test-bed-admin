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
        <list-component :data=solutions></list-component>
      </v-flex>
      <v-flex xs4>
        <h3 class="text-xs-center primary--text">Topics</h3>
        <v-btn @click="openConfigureTopicForm" flat small block class="mx-auto">
          Configure new topic
          <v-icon>create</v-icon>
        </v-btn>
        <list-component :data=topics></list-component>
      </v-flex>
      <v-flex xs4>
        <h3 class="text-xs-center primary--text">Gateways</h3>
        <v-btn @click="openConfigureGatewayForm" flat small block class="mx-auto">
          Configure new gateway
          <v-icon>create</v-icon>
        </v-btn>
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
  import {eventBus} from "../main";
  export default {
    name: "Overview",
    data: () => ({
        snackbar: false,
        snackbarText: '',
        snackbarType: ''
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
    computed:
      mapGetters(['solutions', 'topics', 'gateways'])
    ,
    methods: {
      openConfigureSolutionForm: function() {
        eventBus.$emit('openConfigureSolutionForm')
      },
      openConfigureTopicForm: function() {
        eventBus.$emit('openConfigureTopicForm')
      },
      openConfigureGatewayForm: function() {
        eventBus.$emit('openConfigureGatewayForm')
      },

    }
  }
</script>
