<template>
  <!-- Modal creat/update mentor-->
  <fd-modal
          name="createOrUpdateMentorModal"
          ref="createOrUpdateMentorModal"
          title="Please fill the following details for the new mentor"
        >
          <div>
            <template>
              <fd-alert
                type="error"
                v-model="alertStatus"
                :dismissible="false"
              >Please fill the input fields below.
              </fd-alert>
              <fd-field-set>
                <fd-form-item>
                  <fd-form-label>Name</fd-form-label>
                  <fd-input v-model="tempMentor.name" required />
                </fd-form-item>
                <fd-form-item>
                  <fd-form-label>Email address</fd-form-label>
                  <fd-input v-model="tempMentor.email" />
                </fd-form-item>
                <fd-form-item>
                  <fd-form-label>Phone</fd-form-label>
                  <fd-input v-model="tempMentor.phone" />
                </fd-form-item>
                <fd-form-item>
                  <fd-form-label>Experience</fd-form-label>
                  <fd-input v-model="tempMentor.experience" />
                </fd-form-item>
              </fd-field-set>
            </template>
          </div>
          <template #actions="{close}">
            <fd-button @click="createOrUpdateMentor()" styling="emphasized">Save</fd-button>
            <fd-button @click="close" styling="light">Cancel</fd-button>
          </template>
        </fd-modal>
</template>

<script>
export default {
  name: "AdminCornerModalMentorCreateOrUpdate",
  props: ["tempMentor"],
  data: function() {
    return {
      alertStatus: false
    }
  },
  methods: {
    // Calling backend API to create/update a mentor
    createOrUpdateMentor() {
      if (!this.tempMentor.name || !this.tempMentor.email || !this.tempMentor.phone || !this.tempMentor.experience) {
        this.alertStatus = true;
      } else {
        this.alertStatus = false;
        const apiUrl = this.$backendApi + "/mentor";
        fetch(
          apiUrl,
          {
            method: "PUT",
            headers: {
              "Content-Type": "application/json"
            },
            body: JSON.stringify({
              id: this.tempMentor.id,
              name: this.tempMentor.name,
              email: this.tempMentor.email,
              phone: this.tempMentor.phone,
              experience: this.tempMentor.experience,
              capacity: this.tempMentor.capacity,
            })
          }
        )
          .then(response => {
            console.log(response);
            // Loading updated backend data
            this.$emit('reloadAllMentors');
            // Closing modal
            this.$fdModal.close("createOrUpdateMentorModal");
          })
          .catch(err => {
            console.log(err);
          });
      }
    }   
  }
};
</script>

<style>
</style>
