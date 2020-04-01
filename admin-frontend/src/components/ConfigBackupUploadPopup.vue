<template>
  <v-layout row justify-center>
    <v-dialog v-model="dialog" max-width="600">
      <v-card class="backupFileUploadPopup" style="height: 200px;">
        <v-card-title class="upload_headline">
          Upload configuration backup file
        </v-card-title>
        <form enctype="multipart/form-data" novalidate v-if="isInitial || isSaving">
          <div class="dropbox dropbox_padding">
            <input type="file" multiple :name="uploadFieldName" :disabled="isSaving" @change="filesChange($event.target.name, $event.target.files); fileCount = $event.target.files.length"
              accept=".zip" class="input-file">
              <p v-if="isInitial">
                Drag your file(s) here to begin<br> or click to browse
              </p>
              <p v-if="isSaving">
                Uploading {{ fileCount }} files...
              </p>
          </div>
        </form>
        <v-card-actions style="position:absolute;bottom:0px;height:60px;left:0px;right:0px;">
          <v-spacer></v-spacer>
          <v-btn flat="flat" @click.native="dialog = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-layout>
</template>

<script>
  import Urls from '../constants/Urls'
  import {eventBus} from '../main';
  import EventName from '../constants/EventName';
  import {fetchService} from '../service/FetchService'
  
  import {store} from '../store';


  const STATUS_INITIAL = 0, STATUS_SAVING = 1, STATUS_SUCCESS = 2, STATUS_FAILED = 3;

  export default {
    name: 'app',
    data() {
      return {
        dialog: false,
        uploadedFiles: [],
        currentStatus: null,
        uploadFieldName: 'file',
        rightsMatrix: {},
      }
    },
    computed: {
      isInitial() {
        return this.currentStatus === STATUS_INITIAL;
      },
      isSaving() {
        return this.currentStatus === STATUS_SAVING;
      },
      isSuccess() {
        return this.currentStatus === STATUS_SUCCESS;
      },
      isFailed() {
        return this.currentStatus === STATUS_FAILED;
      }
    },
    methods: {
      reset() {
        // reset form to initial state
        this.currentStatus = STATUS_INITIAL;
        this.uploadedFiles = [];
      },
      save(formData) {
        // upload data to the server
        this.currentStatus = STATUS_SAVING;

        fetchService.performPost('uploadConfigurationData', formData).then(response => {
          this.currentStatus = STATUS_SUCCESS;
          eventBus.$emit('showSnackbar', 'Backup was successfully uploaded.', 'success');
          this.$store.dispatch('getSolutionCertificates');
          this.$store.dispatch('getAllSolutions');
          this.$store.dispatch('getAllTopics');
          this.$store.dispatch('getSolutions');
          this.$store.dispatch('getTopics');
          this.$store.dispatch('getGateways');
          this.$store.dispatch('isTestbedInitialized');
          this.$store.dispatch('isTrialStarted');
          this.$store.dispatch('getAllStandards');
          this.$store.dispatch('getAllTopicTypes');
          this.$store.dispatch('getAllOrganisations');
        }).catch(e => {
          this.currentStatus = STATUS_FAILED;
          eventBus.$emit('showSnackbar', 'Backup file upload failed. (' + e + ')', 'error')
        })
        this.dialog = false;

      },
      filesChange(fieldName, fileList) {
        // handle file changes
        const formData = new FormData();

        if (!fileList.length) return;

        // append the files to FormData
        Array
          .from(Array(fileList.length).keys())
          .map(x => {
            formData.append(fieldName, fileList[x], fileList[x].name);
          });

        // save it
        this.save(formData);
      }
    },
    mounted() {
      this.reset();
    },
    created () {
      const vm = this;
      eventBus.$on(EventName.BACKUP_POPUP, function (value) {
        vm.dialog = value.open;
        vm.reset();
      });
    }
  }

</script>