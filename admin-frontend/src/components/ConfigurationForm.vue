<template>
  <v-form ref="form" lazy-validation v-model="valid" class="configurationForm">
    <v-layout row wrap>
      <v-flex xs12>
        <v-text-field
          v-model=name
          :rules=nameRules
          label="Configuration Name"
          :counter="255"
          required
        ></v-text-field>
      </v-flex>
      <v-flex xs12>
      	<v-select
            :items=items.availableSolutionIDs
            v-model="items.selectedSolutionIDs"
            multiple
            chips
            deletable-chips
            label="Select solution(s) that are part of the configuration"
          ></v-select>
      </v-flex>
      <v-flex xs12>
      	<v-select
            :items=items.availableTopicIDs
            v-model="items.selectedTopicIDs"
            multiple
            chips
            deletable-chips
            label="Select topic(s) that are part of the configuration"
          ></v-select>
      </v-flex>
      <v-flex xs12>
        <v-textarea
          v-model="description"
          label="Description"
        ></v-textarea>
      </v-flex>
    </v-layout>
  </v-form>
</template>
<script>
  import {store} from '../store';

  export default {
    name: "ConfigurationForm",
    props: ['configuration'],
    data: () => ({
      valid: false,
      name: '',
      nameRules: [
        v => !!v || 'Name is required',
        v => (v && v.length <= 255) || 'Max. 255 characters allowed.'
      ],
      description: '',
      selectedSolutions: [],
      selectedTopics: [],
      items: {
        availableSolutions: [],
        availableSolutionIDs: [],
        selectedSolutionIDs: [],
        availableTopics: [],
        availableTopicIDs: [],
        selectedTopicIDs: []
      },
    }),
    computed: {
    },
    created() {
      const me = this;
      me.items.availableSolutionIDs = [];
      me.items.availableTopicIDs = [];
      me.items.selectedSolutionIDs = [];
      me.items.selectedTopicIDs = [];
      me.clear = this.clear.bind(this);
      me.items.availableSolutions =  this.$store.getters.allSolutions;
      me.items.availableSolutions.forEach(function(obj) {
        	me.items.availableSolutionIDs.push(obj.name)
        }, me);
      me.items.availableTopics =  this.$store.getters.allTopics;
      me.items.availableTopics.forEach(function(obj) {
        	me.items.availableTopicIDs.push(obj.name)
        }, me);
      // me.clear();
      if (this.configuration) {
        me.name = this.configuration.name;
        me.description = me.configuration.discription;
        me.selectedSolutions = me.configuration.solutions;
        me.selectedSolutions.forEach(function(obj) {
        	me.items.selectedSolutionIDs.push(obj.name)
        }, me);
        me.selectedTopics = me.configuration.topics;
        me.selectedTopics.forEach(function(obj) {
        	me.items.selectedTopicIDs.push(obj.name)
        }, me);
      }
    },
    methods: {
      clear() {
        const me = this;
        // me.$refs.form.reset(); // leads to empty v-radio selection
        me.$refs.form.resetValidation();
        me.name = "";
      },
      save() {
        const me = this;
        if (me.$refs.form.validate()) {
          me.selectedSolutions = [];
          me.selectedTopics = [];
          me.items.selectedSolutionIDs.forEach(function(obj) {
          	me.items.availableSolutions.forEach(function(sol) {
          		if (sol.name === obj) {
          			me.selectedSolutions.push(sol);
          		}
          	}, me)
          }, me)
          me.items.selectedTopicIDs.forEach(function(obj) {
          	me.items.availableTopics.forEach(function(topic) {
          		if (topic.name === obj) {
          			me.selectedTopics.push(topic);
          		}
          	}, me)
          }, me)
          const entity = {
            id: this.configuration ? this.configuration.id : null,
            name: me.name,
            discription: me.description,
            solutions: me.selectedSolutions,
            topics: me.selectedTopics
          };
          if (entity.id) {
            store.dispatch('updateConfiguration', entity);
          } else {
            store.dispatch('addConfiguration', entity);
          }
          me.clear();
          return true;
        } else {
          return false;
        }
      }
    },
    watch: {
    }
  };
</script>
