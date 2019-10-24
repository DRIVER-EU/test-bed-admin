<template>
  <v-flex>
    <v-layout column>
      <v-card v-for="d in data" :key="d.id" class="mb-3 tertiary">
        <v-card-title style="padding-top: 0;
    padding-bottom: 0;">
          {{ d.name }}
          <v-spacer></v-spacer>
          <v-card-actions>
            <v-btn v-if="dataType === 'solution'" :disabled="!!solutionCertificateNames[d.id] || !d.orgName" icon @click.native="createCertificate(d)">
              <v-icon>playlist_add_check</v-icon>
            </v-btn>
            <v-btn v-if="dataType === 'solution'" :disabled="!solutionCertificateNames[d.id]" icon @click.native="downloadCertificate(d.id)">
              <v-icon>playlist_play</v-icon>
            </v-btn>
            <!--
            <v-btn icon @click.native="d.showDescription = !d.showDescription">
              <v-icon>text_format</v-icon>
            </v-btn>
            <v-btn icon @click.native="d.showDescription = !d.showDescription">
              <v-icon>delete_outline</v-icon>
            </v-btn>
            -->
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

  export default {
    name: 'ListComponent',
    props: ['data', 'dataType'],
    data: () => ({
      solutionCertificateNames: {}
    }),
    methods: {
      createCertificate(solution) {
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
              this.$set(this.solutionCertificateNames, solution.id, fileName);
            })
          } else {
            console.log("No organisation found for name " + solution.orgName);
          }
        });
      },
      downloadCertificate(id) {
        const fileName = this.solutionCertificateNames[id];
        console.log("Downloading certificate for solution", id, fileName);
        fetchService.performSimpleDownload("downloadCertificate/" + fileName);
      }
    }
  }
</script>
