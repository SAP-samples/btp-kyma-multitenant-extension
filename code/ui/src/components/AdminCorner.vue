<template>
  <div id="adminCorner">
    <h1 class="mt-5 mb-5 text-center">Administration Corner</h1>

    <!-- Manage settings section -->
    <div class="container">
        <div class="row">
          <div class="col">
            <h2>General settings</h2>
            <p>Update the logo and the name of your company by changing the settings below.</p>
          </div>
          <div class="col col-lg-2">
            <fd-button class="float-right cta" styling="emphasized" @click="saveCompanyDetails">Update settings</fd-button>
          </div>
        </div>
        <div class="row">  
          <fd-alert
                type="error"
                v-model="alertStatus"
                :dismissible="false"
              >Please fill the input fields below.
          </fd-alert>
          <fd-field-set>
            <fd-form-item>
              <fd-form-label>Name of the company</fd-form-label>
              <fd-input v-model="companyName" />
            </fd-form-item>
            <fd-form-item>
              <fd-form-label>Logo of the company</fd-form-label>
              <fd-input v-model="logoUrl" />
            </fd-form-item>
          </fd-field-set>
        </div>
    </div>

    <!-- Manage Mentors section -->
    <div class="container">
        <div class="row">
          <div class="col">
            <h2>Manage mentors</h2>
            <p>This section aims to manage the mentors that can be assigned to the business partners (franchisees). By buying the the pro version of the app, you can import your mentors directly from your SAP Succesfactors tenant. Please contact us for more details.</p>
          </div>
          <div class="col col-lg-4 float-right">
            <fd-button class="float-right cta" styling="emphasized" @click="createMockData">Add mockdata</fd-button>
            <fd-button class="float-right cta" styling="emphasized" @click="openCreateOrUpdateMentorModal(item)">Add mentor</fd-button>
          </div>
        </div>
        <div class="row">
          <fd-table
        selection-mode="single"
        :headers="['Name', 'Email', 'Phone', 'Experience', 'Capacity', 'Edit']"
        :items="allMentors"
        :selectedIds.sync="selectedIds"
        class="mb-5"
      >
        <template #row="{ toggle,item }">
          <fd-table-row @click="toggle">
            <template #Name>
              <fd-table-cell class="text-uppercase">{{ item.name }}</fd-table-cell>
            </template>
            <template #Email>
              <fd-table-cell>{{ item.email }}</fd-table-cell>
            </template>
            <template #Phone>
              <fd-table-cell>{{ item.phone }}</fd-table-cell>
            </template>
            <template #Experience>
              <fd-table-cell>{{ item.experience }}</fd-table-cell>
            </template>
            <template #Capacity>
              <fd-table-cell>
                <mentor-capacity :mentorId="item.id" :allFranchises="allFranchises" />
              </fd-table-cell>
            </template>
            <template #Edit>
              <fd-table-cell class="iconCell">
                <fd-buttonGroup 
                  selectionMode="multiple"
                  aria-label="Favorite Color"
                >
                  <fd-button styling="light" 
                             icon="edit" 
                             @click="openCreateOrUpdateMentorModal(item)" />
                   <fd-button styling="light" 
                              icon="delete"
                              @click="openDeleteMentorModal(item)" />
                </fd-buttonGroup>
              </fd-table-cell>
            </template>
          </fd-table-row>
        </template>
          </fd-table>
        </div>
    </div>

    <!-- Manage coordinators section -->
    <div class="container">
        <div class="row">
          <div class="col">
            <h2>Notification overview</h2>
            <p>This section aims to list all the coordinators that have subscribed to the notification.</p>
          </div>
        </div>
        <div class="row">
          <fd-table
        selection-mode="single"
        :headers="['Name', 'Email']"
        :items="allCoordinators"
        :selectedIds.sync="selectedIds"
        class="mb-5"
      >
        <template #row="{ toggle,item }">
          <fd-table-row @click="toggle">
            <template #Name>
              <fd-table-cell class="text-uppercase">{{ item.name }}</fd-table-cell>
            </template>
            <template #Email>
              <fd-table-cell>{{ item.email }}</fd-table-cell>
            </template>
          </fd-table-row>
        </template>
          </fd-table>
        </div>
    </div>

    <!-- Modal create/update Mentor -->
    <admin-corner-modal-mentor-create-or-update :tempMentor="tempMentor" v-on="$listeners" />

    <!-- Modal delete Mentor-->
    <admin-corner-modal-mentor-delete :tempMentor="tempMentor" v-on="$listeners" />

  </div>
</template>


<script>
import AdminCornerModalMentorDelete from "@/components/AdminCornerModalMentorDelete.vue"
import AdminCornerModalMentorCreateOrUpdate from "@/components/AdminCornerModalMentorCreateOrUpdate.vue"
import MentorCapacity from "@/components/MentorCapacity.vue"

export default {
  name: "FranchiseDetails",
  props: ["allMentors", "allCoordinators", "allFranchises", "companyName", "logoUrl"],
  data: function() {
    return {
      alertStatus: false,
      selectedIds: [],
      tempMentor:{
        id: "",
        name: "",
        email: "",
        phone: "",
        experience: "",
        capacity: "",
      }
    };
  },
  components: {
    MentorCapacity,
    AdminCornerModalMentorDelete,
    AdminCornerModalMentorCreateOrUpdate
  },
  methods: {
    // Updating name and logo of company
    saveCompanyDetails(){
      if (!this.companyName || !this.logoUrl) {
        this.alertStatus = true;
        console.log("[DEBUG] Company details not available:", this.companyName + " " + this.logoUrl)
      } else {
        this.alertStatus = false;
        console.log("[DEBUG] Company details available:", this.companyName + this.logoUrl)
        this.$emit('updateCompanyDetails', {name: this.companyName, logo: this.logoUrl});
      }
    },
    // Updating tempMentor and opening modal to create/update mentor
    openCreateOrUpdateMentorModal(item){
      console.log("[INFO] Open createOrUpdateMentorModal");
      this.updateTempMentor(item)
      this.$fdModal.open("createOrUpdateMentorModal");
    },
    // Updating tempMentor and opening modal to delete mentor
    openDeleteMentorModal(item){
      console.log("[INFO] Open deleteMentorModal");
      this.updateTempMentor(item)
      this.$fdModal.open("deleteMentorModal");
    },
    // Updating details of temporaly mentor details object when mentor is selected in table
    updateTempMentor(item){
      console.log("[INFO] Updating tempMentor details");
      if (item){
        // If item, user wants to update details of existing mentor
        this.tempMentor.id = item.id;
        this.tempMentor.name = item.name;
        this.tempMentor.email = item.email;
        this.tempMentor.phone = item.phone;
        this.tempMentor.experience = item.experience;
        this.tempMentor.capacity = item.capacity;
        console.log("[DEBUG] tempMentor details: ", this.tempMentor);

      } else {
        // If no item, user wants to create a new mentor
        this.tempMentor.id = "0";
        this.tempMentor.name = "";
        this.tempMentor.email = "";
        this.tempMentor.phone = "";
        this.tempMentor.experience = "";
        this.tempMentor.capacity = "0";
        console.log("[DEBUG] tempMentor details: ", this.tempMentor);
      }
    },
    // Creating mentors mock data
    async createMockData(){
      const apiUrl = this.$backendApi + "/mentor";
      const responseM1 = await fetch(apiUrl,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({
            id: "",
            name: "Justin Case",
            email: "justin.case@demo.com",
            phone: "01234-56789",
            experience: "Beginner",
            capacity: "0"
          })
        }
      )
      const jsonM1 = await responseM1.json();
      console.log(jsonM1);
      
      const responseM2 = await fetch(apiUrl,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({
            id: "",
            name: "Tony Stark",
            email: "tony.stark@demo.com",
            phone: "01234-56789",
            experience: "Expert",
            capacity: "0"
          })
        }
      )
      const jsonM2 = await responseM2.json();
      console.log(jsonM2);

      const responseM3 = await fetch(apiUrl,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({
            id: "",
            name: "Mile Stone",
            email: "mile.stone@demo.com",
            phone: "01234-56789",
            experience: "Intermediate",
            capacity: "0"
          })
        }
      )
      const jsonM3 = await responseM3.json();
      console.log(jsonM3);

      if (jsonM1 && jsonM2 && jsonM3) {
        this.$emit('reloadAllMentors');
      }
    }
  }
};
</script>

<style>
  #adminCorner .container{
    margin-bottom: 5em;
  }
  #adminCorner .cta{
    margin-left: 20px;
    top: 25px;
    position: relative;
  }
  .float-right {
    float: right;
  }
</style>