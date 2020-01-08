<template>
  <v-app>
    <toolbar class="primary">
      <v-btn @click="editOrganisations()">
        <v-icon left>business_center</v-icon>
        Organisations
      </v-btn>
      <v-btn @click="openOverviewDiagramPage()">
        <v-icon left>insert_chart_outlined</v-icon>
        Overview
      </v-btn>
      <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition">
        <v-btn slot="activator">
          <v-icon left>settings</v-icon>
          {{ currentConfiguration.configName ? currentConfiguration.configName : 'Configurations' }}
        </v-btn>
        <v-card>
          <v-list>
            <v-list-tile v-bind:key="configuration.id" v-for="configuration in configurations" @click="switchToConfiguration(configuration.name)">
              <v-icon left v-if="configuration.name === currentConfiguration.configName">radio_button_checked</v-icon>
              <v-icon left v-else>radio_button_unchecked</v-icon>
              {{ configuration.name }}
            </v-list-tile>
          </v-list>
        </v-card>
      </v-menu>
      <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition">
        <v-btn slot="activator">
          <v-icon left>settings_ethernet</v-icon>
          {{ currentConfiguration.testbedMode ? currentConfiguration.testbedMode : 'Modes' }}
        </v-btn>
        <v-card>
          <v-list>
            <v-list-tile v-bind:key="mode" v-for="mode in modes" @click="switchToMode(mode)">
              <v-icon left v-if="mode === currentConfiguration.testbedMode">radio_button_checked</v-icon>
              <v-icon left v-else>radio_button_unchecked</v-icon>
              {{ mode }}
            </v-list-tile>
          </v-list>
        </v-card>
      </v-menu>
      <fetch-button :disabled="isTestbedInitialized" icon="rotate_right" url="/initTestbed" method="POST" :onSuccess="handleInitTestbedSuccess" :onError="handleInitTestbedError">
        Initialize test-bed
      </fetch-button>
      <fetch-button :disabled="!isTestbedInitialized" icon="play_arrow" url="/startTrialConfig" method="POST" :onSuccess="handleStartTrialSuccess" :onError="handleStartTrialError">
        Start trial
      </fetch-button>
    </toolbar>
    <main>
      <Overview></Overview>
    </main>
    <organisation-popup/>
  </v-app>
</template>
<script>
  import {mapGetters} from 'vuex'
  import Overview from '../components/Overview'
  import {configurationService} from '../service/ConfigurationService'
  import FetchButton from '../components/FetchButton'
  import {eventBus} from "../main"
  import EventName from '../constants/EventName';

  export default {
    name: 'App',
    components: {Overview, FetchButton},
    methods:
      {
        editOrganisations: function() {
          eventBus.$emit(EventName.ORGANISATION_POPUP, {open: true});
        },
        handleInitTestbedSuccess: function() {
          eventBus.$emit('showSnackbar', 'Testbed initialized.', 'success')
          this.$store.dispatch('initTestbedSuccess')
        },
        handleInitTestbedError: function(e) {
          eventBus.$emit('showSnackbar', 'Testbed could not be initialized (' + e + ').', 'error')
        },
        handleStartTrialSuccess: function() {
          eventBus.$emit('showSnackbar', 'Trial started.', 'success')
          this.$store.dispatch('startTrialSuccess')
        },
        handleStartTrialError: function(e) {
          eventBus.$emit('showSnackbar', 'Trial could not be started (' + e + ').', 'error')
        },
        switchToConfiguration: function (name) {
          configurationService.switchToConfiguration(name, this.currentConfiguration, this.modes);
        },
        switchToMode: function (name) {
          configurationService.switchToMode(name, this.currentConfiguration, this.configurations);
        },
        openOverviewDiagramPage() {
          console.log("Opening overview image");
          let routeData = this.$router.resolve({name: 'overviewDiagram'});
          window.open(routeData.href, '_blank');
        }
      },
    computed: mapGetters(['isTestbedInitialized', 'isTrialStarted', 'loading', 'configurations', 'modes', 'currentConfiguration']),
    created: function () {
      this.$store.dispatch('getPageCount');
      this.$store.dispatch('getCurrentConfiguration');
      this.$store.dispatch('getConfigurations');
      this.$store.dispatch('getModes');
    }
  }
</script>
