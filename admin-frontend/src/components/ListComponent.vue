<template>
  <v-flex>
    <v-layout column>
      <v-card v-for="d in data" :key="d.id" class="mb-3 tertiary">
        <v-card-title style="padding-top: 0;
    padding-bottom: 0;">
          {{ d.name }}
          <v-spacer></v-spacer>
          <v-card-actions>
            <v-btn v-if="dataType === 'SOLUTION'" :disabled="!!solutionCertificates[d.clientId] || !d.orgName || isCreateCertificateAllowed()" icon @click.native="createCertificate(d)">
              <v-icon>playlist_add_check</v-icon>
            </v-btn>
            <v-btn v-if="dataType === 'SOLUTION'" :disabled="!solutionCertificates[d.clientId] || isDownloadCertificateAllowed()" icon @click.native="downloadCertificate(d)" >
              <v-icon>playlist_play</v-icon>
            </v-btn>
            <v-btn icon @click.native="editItem(d)" :disabled="isEditAllowed()">
              <v-icon>text_format</v-icon>
            </v-btn>
            <v-btn icon @click.native="deleteItem(d)" :disabled="isDeleteAllowed()">
              <v-icon>delete_outline</v-icon>
            </v-btn>
          </v-card-actions>
          <div v-if="d.hasOwnProperty('state')" style="padding-top: 6px;">
            <v-spacer></v-spacer>
            <img v-if="!d.state" src="../assets/lens-red.png">
            <img v-if="d.state" src="../assets/lens-green.png">
          </div>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn icon @click.native="d.showDescription = !d.showDescription">
              <v-icon>{{ d.showDescription ? 'keyboard_arrow_up' : 'keyboard_arrow_down' }}</v-icon>
            </v-btn>
          </v-card-actions>
        </v-card-title>
        <v-card-text v-show="d.showDescription">
          {{d.description}}
        </v-card-text>
      </v-card>
    </v-layout>
  </v-flex>
</template>

<script>
  import {fetchService} from '../service/FetchService'
  import {eventBus} from "../main"
  import EventName from '../constants/EventName'

  export default {
    name: 'ListComponent',
    props: ['data', 'dataType'],
    computed: {
      solutionCertificates: function () {
        return this.$store.state.solutionCertificates;
      },
    },
    methods: {
      isEditAllowed: function() {
        const me = this;
        switch (this.dataType) {
          case "SOLUTION":
            return !this.$store.getters.rightsMatrix.editSolution;
          case "TOPIC":
            return !this.$store.getters.rightsMatrix.editTopic;
          case "GATEWAY":
            return !this.$store.getters.rightsMatrix.editGateway;
          default:
            return false;
        }
      },
      isDeleteAllowed: function() {
        const me = this;
        switch (this.dataType) {
          case "SOLUTION":
            return !this.$store.getters.rightsMatrix.removeSolution;
          case "TOPIC":
            return !this.$store.getters.rightsMatrix.removeTopic;
          case "GATEWAY":
            return !this.$store.getters.rightsMatrix.removeGateway;
          default:
            return false;
        }
      },
      isCreateCertificateAllowed: function() {
        return !this.$store.getters.rightsMatrix.createCertificate;
      },
      isDownloadCertificateAllowed: function() {
        return !this.$store.getters.rightsMatrix.downloadCertificate;
      },
      createCertificate(solution) {
        const me = this;
        fetchService.performGet('getAllOrganisations').then(response => {
          const organisation = response.data.find(o => o.orgName === solution.orgName);
          if (organisation) {
            const url = "createSolutionCertificate?clientID=" + encodeURIComponent(solution.clientId) +
              "&organisationName=" + encodeURIComponent(solution.orgName) +
              "&password=" + encodeURIComponent(solution.certPwd) +
              "&email=" + encodeURIComponent(solution.email);
            console.log("Creating certificate for solution ", solution.id);
            fetchService.performGet(url).then(response => {
              const fileName = response.data;
              me.$store.dispatch('addSolutionCertificate', {solution: solution.clientId, fileName: fileName});
            })
          } else {
            console.log("No organisation found for name " + solution.orgName);
          }
        });
      },
      downloadCertificate(solution) {
        const fileName = this.solutionCertificates[solution.clientId];
        console.log("Downloading certificate for solution", solution.id, fileName);
        fetchService.performSimpleDownload("downloadCertificate/" + fileName);
      },
      deleteItem(item) {
        const me = this;
        switch (this.dataType) {
          case "SOLUTION":
            me.$store.dispatch("removeSolution", {item: item});
            break;
          case "TOPIC":
            me.$store.dispatch("removeTopic", {item: item});
            break;
          case "GATEWAY":
            me.$store.dispatch("removeGateway", {item: item});
            break;
          default:
            throw "Unsupported data type for removal: " + this.dataType;
        }
      },
      editItem(item) {
        const me = this;
        switch (this.dataType) {
          case "SOLUTION":
            eventBus.$emit(EventName.OPEN_SOLUTION_FORM, item);
            break;
          case "TOPIC":
            eventBus.$emit(EventName.OPEN_TOPIC_FORM, item);
            break;
          case "GATEWAY":
            eventBus.$emit(EventName.OPEN_GATEWAY_FORM, item);
            break;
          default:
            throw "Unsupported data type for removal: " + this.dataType;
        }
      },
    }
  }
</script>
