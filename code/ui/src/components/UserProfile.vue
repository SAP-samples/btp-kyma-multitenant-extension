<template>
  <div>
    <h1 class="mt-5 mb-5 text-center">User Profile</h1>
    <!-- Manage coordinator details section -->
    <div class="container">
      <div class="fd-row">
        <fd-field-set>
          <fd-form-item>
            <fd-form-label>User name</fd-form-label>
            <fd-input v-model="coordinatorName" required />
          </fd-form-item>
          <fd-form-item>
            <fd-form-label>User email</fd-form-label>
            <fd-input v-model="userInfo.email" required />
          </fd-form-item>
        </fd-field-set>
      </div>
      <div class="fd-row mt-5">
        <h2>Notifications subscription</h2>
        <p>As coordinator, you can subscribe to events to get notified, once a new businnes partner (franchisee) is added. Switch the button below to activate the subscription.</p>
        <fd-field-set>
          <fd-form-item-toggle :label="subcriptionStatusLabel">
            <fd-toggle v-model="subcriptionStatus" size="s" />
          </fd-form-item-toggle>
        </fd-field-set>
      </div>
    </div>

  </div>
</template>


<script>
export default {
  name: "UserProfile",
  props: ['allCoordinators'],
  data: function() {
    return {
      subcriptionStatus: "false",
      subcriptionStatusLabel: "Notifications deactivated",
      userInfo: "",
      userAlreadyinDb: ""
    };
  },
  computed: {
    // Merging coordinator lastname and firstname
    coordinatorName: function() {
      let name = this.userInfo.firstname + ' ' + this.userInfo.lastname;
      return name;
    },
    // Extracting Id of coordinator in the backend as it is needed to activate notification
    backendCoordinatorId: function(){
      const BackendCoordinatorObject = this.allCoordinators.filter(
          coordinator => coordinator.email === this.userInfo.email
      );
      return BackendCoordinatorObject[0].id;
    }
  },
  methods: {
    // Checking if actual user has activated notifications
    async getCoordinatorStatus(){
      if(!this.userInfo){
        // Calling API to extract user info from the JWT token
        const response = await fetch('/userinfo');
        this.userInfo = await response.json();
        console.log("[DEBUG] Loading user info: ", this.userInfo);
      }
      // Check if user email is already save as coordinator email in db
      const allCoordinatorsWithTheActualCoordinator = this.allCoordinators.filter(
          coordinator => coordinator.email === this.userInfo.email
        );
      if (allCoordinatorsWithTheActualCoordinator.length == 0 ){
        this.userAlreadyinDb = false;
      } else{
        this.userAlreadyinDb = true;
        this.updateSubscriptionStatus();
      }
    },
    // Updating UI to show notification activated
    updateSubscriptionStatus(){
      this.subcriptionStatus = true;
      this.subcriptionStatusLabel= "Notifications activated";
    },
    // Activating notifications for actual user
    activateSubscription(){
      if(!this.userAlreadyinDb){
        const apiUrl = this.$backendApi + "/coordinator";
        fetch(
          apiUrl,
          {
            method: "PUT",
            headers: {
              "Content-Type": "application/json"
            },
            body: JSON.stringify({
              name: this.coordinatorName,
              email: this.userInfo.email
            })
          }
        )
        .then(response => {
          console.log("[DEBUG] Notifications activated: ", response);
          // Updating UI notification 
          this.updateSubscriptionStatus();
          // Updating UI data
          this.$emit('reloadAllCoordinators');
        })
        .catch(err => {
          console.log(err);
        });
      }
    },
    deactivateSubscription(){
      if(this.userAlreadyinDb){
        this.subcriptionStatus = false;
        this.subcriptionStatusLabel = "Notifications deactivated";
        const apiUrl = this.$backendApi + "/coordinator/" + this.backendCoordinatorId;
        fetch(
          apiUrl,
          {
            method: "DELETE"
          }
        )
        .then(response => {
          console.log("[DEBUG] Notifications deactivated: ", response);
          // Reloading UI data for coordinator details
          this.$emit('reloadAllCoordinators');
        })
        .catch(err => {
          console.log(err);
        });
      }
    }
  },
  watch: {
    // Watching if the toggle button is trigered
    subcriptionStatus(on) {
      on ? this.activateSubscription() : this.deactivateSubscription();
    }
  },
  mounted: function() {
    // Loading the coodinator status when component is mounted
    this.getCoordinatorStatus();
  }
};
</script>
<style>
</style>