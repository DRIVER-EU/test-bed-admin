<template>
  <v-form ref="form" lazy-validation v-model="valid" class="organisationForm">
    <v-layout row wrap>
      <v-flex xs12>
        <v-text-field
          v-model=orgName
          :rules=orgNameRules
          label="Organisation Name"
          :counter="25"
          required
        ></v-text-field>
      </v-flex>
      <v-flex xs6>
        <v-text-field
          v-model=userName
          :rules=userNameRules
          label="User Name"
          required
        ></v-text-field>
      </v-flex>
      <v-flex xs6>
        <v-text-field
          v-model=userPwd
          :rules=userPwdRules
          label="User Password"
          required
          type="text"
        ></v-text-field>
      </v-flex>
      <v-flex xs6>
        <v-text-field
          v-model=certPwd
          :rules=certPwdRules
          label="Certificate Password"
          required
          type="text"
        ></v-text-field>
      </v-flex>
    </v-layout>
    <v-layout row wrap>
      <v-flex xs6>
        <v-text-field
          v-model=email
          label="E-Mail"
        ></v-text-field>
      </v-flex>
      <v-flex xs6>
        <v-text-field
          v-model=phone
          label="Phone"
        ></v-text-field>
      </v-flex>
      <v-flex xs3>
        <v-text-field
          v-model=postcode
          :rules=postcodeRules
          label="Post Code"
        ></v-text-field>
      </v-flex>
      <v-flex xs9>
        <v-text-field
          v-model=city
          label="City"
        ></v-text-field>
      </v-flex>
      <v-flex xs9>
        <v-text-field
          v-model=street
          label="Street"
        ></v-text-field>
      </v-flex>
      <v-flex xs3>
        <v-text-field
          v-model=nr
          :rules=nrRules
          label="Number"
        ></v-text-field>
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
    name: "OrganisationForm",
    props: ['organisation'],
    data: () => ({
      valid: false,
      orgName: '',
      orgNameRules: [
        v => !!v || 'Name is required',
        v => (v && v.length <= 25) || 'Max. 25 characters allowed.'
      ],
      userName: '',
      userNameRules: [
        v => !!v || 'User name is required',
      ],
      userPwd: '',
      userPwdRules: [
        v => !!v || 'User password is required',
      ],
      certPwd: '',
      certPwdRules: [
        v => !!v || 'Certificate password is required',
      ],
      email: '',
      phone: '',
      city: '',
      postcode: '',
      postcodeRules: [
        v => (!v || new RegExp("^[0-9]*$").test(v)) || 'Please enter a numeric value', // Number.isInteger(parseInt(v))
      ],
      street: '',
      nr: '',
      nrRules: [
        v => (!v || new RegExp("^[0-9]*$").test(v)) || 'Please enter a numeric value', // Number.isInteger(parseInt(v))
      ],
      description: '',
    }),
    computed: {
    },
    created() {
      const me = this;
      this.clear = this.clear.bind(this);
      // me.clear();
      if (this.organisation) {
        me.orgName = this.organisation.orgName;
        me.userName = this.organisation.userName;
        me.userPwd = this.organisation.userPwd;
        me.certPwd = this.organisation.certPwd;
        me.email = this.organisation.email;
        me.phone = this.organisation.phone;
        me.city = this.organisation.city;
        me.postcode = this.organisation.postcode;
        me.street = this.organisation.street;
        me.nr = this.organisation.nr;
        me.description = this.organisation.description;
      }
    },
    methods: {
      clear() {
        const me = this;
        // me.$refs.form.reset(); // leads to empty v-radio selection
        me.$refs.form.resetValidation();
        me.orgName = "";
      },
      save() {
        const me = this;
        if (me.$refs.form.validate()) {
          const entity = {
            id: this.organisation ? this.organisation.id : null,
            orgName: me.orgName,
            userName: me.userName,
            userPwd: me.userPwd,
            certPwd: me.certPwd,
            email: me.email,
            phone: me.phone,
            city: me.city,
            postcode: me.postcode,
            street: me.street,
            nr: me.nr,
            description: me.description,
        };
          if (entity.id) {
            store.dispatch('updateOrganisation', entity);
          } else {
            store.dispatch('addOrganisation', entity);
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
