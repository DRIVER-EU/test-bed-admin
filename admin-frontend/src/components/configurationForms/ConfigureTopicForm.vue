<template>
  <v-dialog v-model="open" max-width="700px" persistent>
    <v-card>
      <v-card-title class="primary--text">Configure topic
      </v-card-title>
      <v-card-text>
        <v-form ref="form" v-model="valid">
          <v-text-field
            v-model="clientId"
            :rules="clientIdRules"
            label="ID"
            :counter="25"
            required
          ></v-text-field>
          <v-text-field
            v-model="name"
            :rules="nameRules"
            label="Name"
            :counter="25"
            required
          ></v-text-field>
          <v-select
            :items=topicTypes
            v-model="type"
            label="Select type"
            required
            :rules="[v => !!v || 'Type is required']"
          ></v-select>
          <v-select
            :items=standardNames
            v-model="standard"
            label="Select standard"
          ></v-select>
          <v-select
            :items=items.standardVersions
            v-model="standardVersion"
            label="Select standard version"
            :disabled=!standard
          ></v-select>
          <v-select
            :items=items.solutionIds
            v-model="publishIDs"
            multiple
            chips
            deletable-chips
            label="Select solution(s) allowed to publish to this topic"
          ></v-select>
          <v-select
            :items=items.solutionIds
            v-model="subscribeIDs"
            multiple
            chips
            deletable-chips
            label="Select solution(s) allowed to subscribe to this topic"
          ></v-select>
          <v-text-field
            v-model="description"
            label="Description"
            multi-line
          ></v-text-field>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="open=false">Close</v-btn>
        <v-btn @click="submit" class="primary--text">Submit</v-btn>
      </v-card-actions>
    </v-card>

  </v-dialog>
</template>

<script>
  import {eventBus} from "../../main";
  import {store} from '../../store'

  export default {
    name: "ConfigureTopicForm",
    data: () => ({
      open: false,
      valid: false,
      clientId: '',
      clientIdRules: [
        v => !!v || 'ID is required',
        v => (v && v.length <= 25) || 'Max. 25 characters allowed.'
      ],
      name: '',
      nameRules: [
        v => !!v || 'Name is required',
        v => (v && v.length <= 25) || 'Max. 25 characters allowed.'
      ],
      type: null,
      standard: null,
      standardVersion: null,
      publishIDs: [],
      subscribeIDs: [],
      items: {
        standardVersions: [],
        solutionIds: []
      },
      description: ''
    }),
    computed: {
      standards: function () {
        return this.$store.getters.standards
      },
      standardNames: function () {
        return this.$store.getters.standardNames
      },
      topicTypes: function () {
        return this.$store.getters.topicTypes
      },

    },
    created() {
      eventBus.$on('openConfigureTopicForm', () => {
        this.open = true
      })
      eventBus.$on('updateSolutionIds', (solutionId) => {
        this.items.solutionIds.push(solutionId)
      })

    },
    methods: {
      submit() {
        const self = this;
        if (self.$refs.form.validate()) {
          let topic = {
            clientId: self.clientId,
            name: self.name,
            type: self.type,
            msgType: self.standard,
            msgTypeVersion: self.standardVersion,
            publishIDs: self.publishIDs,
            subscribeIDs: self.subscribeIDs,
            description: self.description
          }
          store.dispatch('addTopic', topic)
          self.clear()
          self.open = false
        }
      },
      clear() {
        this.$refs.form.reset()
      }
    },
    watch: {
      standard: function (selectedStandardName) {
        var obj = this.standards.find(obj => {
          return obj.name === selectedStandardName
        })
        if (obj && obj.versions)
          this.items.standardVersions = obj.versions
      }
    }
  }
</script>

<style scoped>

</style>
