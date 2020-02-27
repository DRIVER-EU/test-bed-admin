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
            :disabled=!type
          ></v-select>
          <v-select
            :items=items.standardVersions
            v-model="standardVersion"
            label="Select standard version"
            :disabled=!standard
          ></v-select>
          <v-select
            :items=items.solutionIds
            v-model="publishSolutionIDs"
            multiple
            chips
            deletable-chips
            label="Select solution(s) allowed to publish to this topic"
          ></v-select>
          <v-select
            :items=items.solutionIds
            v-model="subscribedSolutionIDs"
            multiple
            chips
            deletable-chips
            label="Select solution(s) allowed to subscribe to this topic"
          ></v-select>
          <v-textarea
            v-model="description"
            label="Description"
          ></v-textarea>
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
  import EventName from '../../constants/EventName'

  export default {
    name: "ConfigureTopicForm",
    data: () => ({
      open: false,
      editedItem: null,
      valid: false,
      clientId: '',
      clientIdRules: [
        v => !!v || 'ID is required',
        v => (v && v.length <= 255) || 'Max. 255 characters allowed.'
      ],
      name: '',
      nameRules: [
        v => !!v || 'Name is required',
        v => (v && v.length <= 255) || 'Max. 255 characters allowed.'
      ],
      type: null,
      standard: null,
      standardName: null,
      standardNamespace: null,
      standardVersion: null,
      publishSolutionIDs: [],
      subscribedSolutionIDs: [],
      items: {
        standardVersions: [],
        solutionIds: []
      },
      description: ''
    }),
    computed: {
      topicTypes: function () {
        return this.$store.getters.topicTypes
      },
    },
    created() {
      const me = this;
      this.standardNames = [];
      this.allStandards = [];
      this.clear = this.clear.bind(this);
      eventBus.$on(EventName.OPEN_TOPIC_FORM, (item) => {
        me.editedItem = item;
        me.clear();
        if (item) {
          me.clientId = item.clientId;
          me.name = item.name;
          me.type = item.type;
          me.standard = item.msgTypeNamespace + '/' + item.msgType;
          me.standardName = item.msgType;
          me.standardNamespace = item.msgTypeNamespace;
          me.standardVersion = item.msgTypeVersion;
          me.publishSolutionIDs = item.publishSolutionIDs;
          me.subscribedSolutionIDs = item.subscribedSolutionIDs;
          me.description = item.description;
        }
        me.open = true
      })
      eventBus.$on(EventName.ADD_SOLUTION_ID, (solutionId) => {
        this.items.solutionIds.push(solutionId)
      })
      eventBus.$on(EventName.REMOVE_SOLUTION_ID, (solutionId) => {
        const index = array.indexOf(solutionId);
        if (index !== -1) array.splice(index, 1);
      })
    },
    methods: {
      submit() {
        const me = this;
        if (me.$refs.form.validate()) {
          let topic = {
            id: this.editedItem ? this.editedItem.id : null,
            clientId: me.clientId,
            name: me.name,
            type: me.type,
            msgType: me.standardName,
            msgTypeNamespace: me.standardNamespace,
            msgTypeVersion: me.standardVersion,
            publishSolutionIDs: me.publishSolutionIDs,
            subscribedSolutionIDs: me.subscribedSolutionIDs,
            description: me.description
          }
          if (this.editedItem) {
            store.dispatch('updateTopic', topic);
          } else {
            store.dispatch('addTopic', topic);
          }
          me.clear();
          me.open = false;
        }
      },
      clear() {
        const me = this;
        // me.$refs.form.reset(); // leads to empty v-radio selection
        this.$refs.form.resetValidation();
        me.clientId = "";
        me.name = "";
        me.type = null;
        me.standard = null;
        me.standardName = null;
        me.standardNamespace  = null;
        me.standardVersion = null;
        me.publishSolutionIDs = [];
        me.subscribedSolutionIDs = [];
        me.description = "";
      }
    },
    watch: {
      type: function (selectedType) {
        this.standardNames = [];
        this.allStandards = this.$store.getters.standards;
        this.allStandards.forEach(function(obj) {
        	this.standardNames.push(obj.namespace + '/' + obj.name)
        }, this)
      },
      standard: function (selectedStandardName) {
        var fields = selectedStandardName.split('/');
        this.standardNamespace = fields[0];
        this.standardName = fields[1];
        var obj = this.allStandards.find(obj => {
          return obj.name === fields[1] && obj.namespace === fields[0]
        })
        if (obj && obj.versions)
          this.items.standardVersions = obj.versions
      }
    }
  }
</script>

<style scoped>

</style>
