<!-- This page shows the details of the selected franchise -->
<template>
  <div>
    <fd-section>
       <!--Show contact information of franchise-->
        <div class="container mt-4 mb-4" id="companyinformation">
          <fd-section-header class="mb-3">
            <fd-section-title>Franchise Company Information</fd-section-title>
          </fd-section-header>
          <fd-container fluid v-fd-padding:medium>
            <div class="fd-col fd-col--6">
              <p>Name: {{ actualFranchiseDetails.fullName }}</p>
              <p>Address: {{ actualFranchiseDetails.streetName }} {{ actualFranchiseDetails.houseNumber }}</p>
              <p>Postal Code: {{ actualFranchiseDetails.cityCode }}</p>
              <p>City: {{ actualFranchiseDetails.cityName }}</p>
            </div>
            <div class="fd-col fd-col--6">
              <p>Email: {{ actualFranchiseDetails.emailAddress }}</p>
              <p>Date of Partnership: {{ actualFranchiseDetails.creationDate }}</p>
              <p>Profil: {{ actualFranchiseDetails.businessPartnerGrouping }}</p>
            </div>
          </fd-container>
        </div>
    </fd-section>
 
    <!-- Mentor section -->
    <fd-section class="grey-section">
      <div class="container fluid mt-4 mb-4" id="mentor">
        <fd-section-header class="mb-3">
          <fd-section-title>Mentor assignement</fd-section-title>
        </fd-section-header>
        <fd-container fluid v-fd-padding:medium>
          <!-- Show button if no mentor is selected -->
          <fd-col :span="12" v-if="!assignedMentorDetails">
            <p>
              So far, there is no mentor assigned to franchise
              <b>{{ actualFranchiseDetails.fullName }}</b>.
            </p>
            <p>Please click on the button below and select a mentor from the list.</p>
            <!-- Open Modal to assign new mentor -->
            <fd-button
              icon="popup-window"
              @click="$fdModal.open('assignMentorModal')"
              :allMentors="allMentors"
            >Assign a mentor</fd-button>
          </fd-col>
        
          <!-- Show mentor details after mentor is assigned -->
          <fd-col :span="6" v-if="assignedMentorDetails">
            <p>Name: {{ assignedMentorDetails.name }}</p>
            <p>Email address: {{ assignedMentorDetails.email }}</p>
            <p>Phone number: {{ assignedMentorDetails.phone }}</p>
            <p>Mentor Experience: {{ assignedMentorDetails.experience }}</p>
          </fd-col>
          <fd-col :span="6" v-if="assignedMentorDetails">
                  
            <radial-progress-bar 
              id="radialMentorCapacity"
              :diameter="150"
              :completed-steps="actualMentorCapacity"
              :total-steps="totalSteps"
              :startColor="circleColor"
              :stopColor="circleColor"
              :innerStrokeColor="innerStrokeColor">
              <p>Mentor capacity</p> 
              <span :class="['capacity-' + actualMentorCapacity]">
                  {{ actualMentorCapacity }}/10
              </span>
              <!-- <p>Total steps: {{ totalSteps }}</p>
              <p>Completed steps: {{ completedSteps }}</p> -->
            </radial-progress-bar>

          </fd-col>
        </fd-container>
        <fd-container>
          <fd-col :span="12" v-if="assignedMentorDetails">
            <fd-panel class="pt-3 mb-5">
              <p>You can change the assigned mentor by clicking the following button.</p>
              <fd-button
                id="update-mentor-button"
                v-if="assignedMentorDetails"
                icon="popup-window"
                @click="$fdModal.open('assignMentorModal')"
                :allMentors="allMentors">
                Update mentor
              </fd-button>
            </fd-panel>
          </fd-col>
        </fd-container>
      </div>
    </fd-section>

    <!-- Modal to assign a mentor -->
    <assign-mentor-modal
      v-show="isMentorModalVisible"
      @assignMentor="assignSelectedMentor($event)"
      :allMentors="allMentors"
      :allFranchises="allFranchises"
      :assignedMentorId ="assignedMentorDetails.id"
    />
    
  </div>
</template>

<script>
import AssignMentorModal from "@/components/FranchiseDetailsMentorModal.vue";
// See https://github.com/wyzantinc/vue-radial-progress for more info about RadialProgressBar
import RadialProgressBar from 'vue-radial-progress'

export default {
  name: "FranchiseDetails",
  props: ["selectedFranchiseeId", "allFranchises", "allMentors"],
    data: function() {
    return {
      isMentorModalVisible: false,
      //allFranchises: [{"businessPartner":"1000162","businessPartnerGrouping":"BP02","cityCode":"","cityName":"München","creationDate":"2021-07-21","emailAddress":"munich@bike-sharing.com","fullName":"Bike Sharing - München","houseNumber":"102","mentorId":24,"mentorName":"Justin Case","postalCode":"80636","streetName":"Arnulfstraße"},{"businessPartner":"1000163","businessPartnerGrouping":"BP02","cityCode":"","cityName":"London","creationDate":"2021-07-21","emailAddress":"london@bike-sharing.com","fullName":"Bike Sharing - London","houseNumber":"40","mentorId":24,"mentorName":"Justin Case","postalCode":"SE1 8NY","streetName":"Blackfriars Rd"},{"businessPartner":"1000164","businessPartnerGrouping":"BP02","cityCode":"","cityName":"Hamburg","creationDate":"2021-07-21","emailAddress":"hamburg@bike-sharing.com","fullName":"Bike Sharing - Hamburg","houseNumber":"46","mentorId":25,"mentorName":"Tony Stark","postalCode":"20457","streetName":"Am Sandtorkai"},{"businessPartner":"1000165","businessPartnerGrouping":"BP02","cityCode":"","cityName":"Stuttgart","creationDate":"2021-07-21","emailAddress":"stuttgart@bike-sharing.com","fullName":"Bike Sharing - Stuttgart","houseNumber":"3","postalCode":"70174","streetName":"Friedrichstraße"}],
      //allMentors: [{"capacity":0,"email":"justin.case@demo.com","experience":"Beginner","id":24,"lastUpdate":"2021-08-06T08:44:24.0759762","name":"Justin Case","phone":"01234-56789"},{"capacity":0,"email":"tony.stark@demo.com","experience":"Expert","id":25,"lastUpdate":"2021-08-06T08:44:24.1641981","name":"Tony Stark","phone":"01234-56789"},{"capacity":0,"email":"mile.stone@demo.com","experience":"Intermediate","id":26,"lastUpdate":"2021-08-06T08:44:24.2460856","name":"Mile Stone","phone":"01234-56789"}],
      //selectedFranchiseeId: "1000162",
      completedSteps: 1,
      totalSteps: 10,
      circleColor: "#427CAC",
      innerStrokeColor: "#fff"
    };
  },
  components: {
    AssignMentorModal,
    RadialProgressBar
  },
  computed: {
    // Get details of selected franchise
    actualFranchiseDetails: function() {
      var filteredArr = this.allFranchises.find(
        x => x.businessPartner === this.selectedFranchiseeId 
      );
      return filteredArr;
    },
    // Get details of assigned mentor
    assignedMentorDetails: function() {
      if(this.actualFranchiseDetails.mentorName){
        var filteredArr = this.allMentors.find(
          x => x.name === this.actualFranchiseDetails.mentorName
        );
        return filteredArr;
      } else{
        return false;
      }
    },
    // Update mentor capacity after mentor is selected
    actualMentorCapacity: function(){
      if(this.actualFranchiseDetails){
        return this.calculateMentorCapacity(this.actualFranchiseDetails.mentorId);
      } else{
        return false;
      }
    }
  },
  methods: {
    // Calculate mentor capacity by showing how many times the mentor is assigned in franchisees
    calculateMentorCapacity(mentorId){
        const allFranchisesWithTheActualMentor = this.allFranchises.filter(
          franchise => franchise.mentorId === mentorId
        );
        return allFranchisesWithTheActualMentor.length;
    },
    // Update assigned mentor in api and refresh all franchise page
    assignSelectedMentor(selectedMentorDetails){
      console.log("[DEBUG] Details of selected mentor: ", selectedMentorDetails);
      const apiUrl = this.$backendApi + "/franchisee/" + this.selectedFranchiseeId + "/mentor/" + selectedMentorDetails.id;
      fetch(apiUrl, {
          method: "PUT",
          headers: {
            "Content-Type": "application/json"
          }
        }
      )
      .then(response => {
        console.log("[DEBUG] Response of saving assigned mentor: ", response)
      })
      .then(this.$emit('reloadAllFranchises'))
      .then(this.informMentor(selectedMentorDetails))
      .catch(err => {
        console.log(err);
      });
    },
    // Inform mentor by using email service
    informMentor(selectedMentorDetails){
      console.log("[DEBUG] Selected mentor: ", selectedMentorDetails);
      console.log("[DEBUG] Selected franchise: ", this.actualFranchiseDetails);
      const apiUrl = this.$backendApi + "/mentor/notify";
      fetch(
          apiUrl,
          {
            method: "PUT",
            headers: {
              "Content-Type": "application/json"
            },
            body: JSON.stringify({
              mentor: selectedMentorDetails,
              franchise: this.actualFranchiseDetails,
            })
          }
        )
          .then(response => {
            console.log(response);
          })
          .then(this.$fdModal.close('informMentorModal'))
          .catch(err => {
            console.log(err);
          });
    }
  },
  mounted: function(){
  }
};
</script>

<style>
p{
  margin-bottom: 1em !important;
}
#informBtn{
  position: absolute;
  top: 0;
  right: 10px;
}
.fd-tabs {
  box-shadow: none !important;
}
.fd-dynamic-page--xl .fd-dynamic-page__tabs {
  padding: 0rem !important;
}
.mb-4 {
  margin-bottom: 0cm !important;
}
#trainings button.selected {
  background-color: #e5f0fa;
}
.fd-form__label {
  font-size: 1.2em;
}
.fd-form__label {
  font-size: 1.2em !important;
}
.grey-section {
  background-color: #f7f7f7 !important;
  box-shadow: inset 0 0.125rem 0.25rem 0 rgb(0 0 0 / 8%);
}
.notAvailable {
  color: red;
}
#mentor .fd-container {
  position: relative;
}
#update-mentor-button{
  position: absolute;
  top: 1.8em;
  right: 1.8em;
}
.fd-has-type-1{
  font-size: 1.5rem;
  font-weight: 500;
}
.capacity-0,
.capacity-1,
.capacity-2,
.capacity-3,
.capacity-4,
.capacity-5{
  color: green;
}
.capacity-6,
.capacity-7,
.capacity-8 {
  color: orange;
}
.capacity-9,
.capacity-10,
.capacity-11,
.capacity-12 {
  color: red;
}
#radialMentorCapacity{
  position:relative;
  top: -30px;
}
</style>