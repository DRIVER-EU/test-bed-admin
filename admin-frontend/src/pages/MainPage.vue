<template>
  <v-app>
    <toolbar class="primary">
      <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition">
        <v-btn slot="activator">
          <v-icon left>bar_chart</v-icon>Administration</v-btn>
        <v-card>
          <v-list>
            <v-list-tile @click="uploadSchema()" :disabled="isUploadAllowed()">
              <v-icon left>business_center</v-icon>
              Upload Schema
            </v-list-tile>
            <v-list-tile @click="editConfigurations()" :disabled="isConfigViewAllowed()">
              <v-icon left>settings</v-icon>
              Configurations
            </v-list-tile>
            <v-list-tile @click="editOrganisations()" :disabled="isOrgViewAllowed()">
              <v-icon left>business_center</v-icon>
              Organisations
            </v-list-tile>
          </v-list>
        </v-card>
      </v-menu>
      <v-btn @click="openOverviewDiagramPage()">
        <v-icon left>insert_chart_outlined</v-icon>
        Overview
      </v-btn>
      <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition" :disabled="isChangeConfigAllowed()">
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
      <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition" :disabled="isChangeSecurityAllowed()">
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
      <fetch-button :disabled="isTestbedInitialized || isInitAllowed()" icon="rotate_right" url="/initTestbed" method="POST" :onSuccess="handleInitTestbedSuccess" :onError="handleInitTestbedError" >
        Initialize test-bed
      </fetch-button>
      <fetch-button :disabled="!isTestbedInitialized || isStartAllowed()" icon="play_arrow" url="/startTrialConfig" method="POST" :onSuccess="handleStartTrialSuccess" :onError="handleStartTrialError" >
        Start trial
      </fetch-button>
      <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition">
        <v-btn slot="activator">
          <v-icon left>account_circle</v-icon>
          {{ currentRole ? currentRole : 'Roles' }}
        </v-btn>
        <v-card>
          <v-list>
            <v-list-tile v-bind:key="name" v-for="name in roles" @click="switchToRole(name)">
              <v-icon left v-if="name === currentRole">radio_button_checked</v-icon>
              <v-icon left v-else>radio_button_unchecked</v-icon>
              {{ name }}
            </v-list-tile>
          </v-list>
        </v-card>
      </v-menu>
    </toolbar>
    <main>
      <Overview></Overview>
    </main>
    <configuration-popup/>
    <organisation-popup/>
    <schema-popup/>
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
        isUploadAllowed: function () {
          return !this.$store.getters.rightsMatrix.uploadSchemaFile;
        },
        isOrgViewAllowed: function () {
          return !this.$store.getters.rightsMatrix.viewOrganisations;
        },
        isConfigViewAllowed: function () {
          return !this.$store.getters.rightsMatrix.viewConfigurations;
        },
        isChangeConfigAllowed: function () {
          return !this.$store.getters.rightsMatrix.changeConfiguration;
        },
        isChangeSecurityAllowed: function () {
          return !this.$store.getters.rightsMatrix.changesecurity;
        },
        isInitAllowed: function () {
          return !this.$store.getters.rightsMatrix.initTestbed;
        },
        isStartAllowed: function () {
          return !this.$store.getters.rightsMatrix.startTrial;
        },
        uploadSchema: function() {
          eventBus.$emit(EventName.SCHEMA_POPUP, {open: true});
        },
        editConfigurations: function() {
          eventBus.$emit(EventName.CONFIGURATION_POPUP, {open: true});
        },
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
        switchToRole: function (name) {
          this.$store.dispatch('setRole',name);
          this.$store.dispatch('getRightsMatrix');
        },
        openOverviewDiagramPage() {
          console.log("Opening overview image");
          let routeData = this.$router.resolve({name: 'overviewDiagram'});
          window.open(routeData.href, '_blank');
        }
      },
    computed: mapGetters(['isTestbedInitialized', 'isTrialStarted', 'loading', 'configurations', 'modes','roles', 'currentConfiguration', 'currentRole']),
    created: function () {
      this.$store.dispatch('getRightsMatrix');
      this.$store.dispatch('getPageCount');
      this.$store.dispatch('getCurrentConfiguration');
      this.$store.dispatch('getConfigurations');
      this.$store.dispatch('getModes');
      eventBus.$on(EventName.RIGHTS_RELOADED, (text, type) => {
        this.$forceUpdate();
      })
    }
  }
</script>
