<template>
  <!-- Modal delete mentor-->
  <fd-modal
    name="deleteMentorModal"
    ref="deleteMentorModal"
    title="Delete mentor"
  >
    <div>
      <p> 
        Do you really want to delete the following mentor:<br>
        <span class="font-weight-bolder text-uppercase">- {{ tempMentor.name }}</span>
      </p>
    </div>
    <template #actions="{close}">
      <fd-button @click="deleteMentor" styling="emphasized">Delete</fd-button>
      <fd-button @click="close" styling="light">Cancel</fd-button>
    </template>
  </fd-modal>
</template>

<script>
export default {
  name: "AdminCornerModalMentorDelete",
  props: ["tempMentor"],
  methods: {
    // Calling API backend to delete mentor
    deleteMentor(){
      const apiUrl = this.$backendApi + "/mentor/" + this.tempMentor.id;
      fetch(
        apiUrl,
        {
          method: "DELETE"
        }
      )
      .then(response => {
        console.log(response);
        // Loading updated backend data
        this.$emit('reloadAllMentors');
        // Closing modal
        this.$fdModal.close("deleteMentorModal");
      })
      .catch(err => {
        console.log(err);
      });
    }    
  }
};
</script>

<style>
</style>
