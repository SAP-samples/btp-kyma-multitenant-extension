<!-- Start page that shows all franchises in two groups (new & existing) -->
<template>
<div>   
    <div class="container fluid">
      <h1 class="mt-5 mb-5 text-center">Franchises Overview</h1>
      <!-- Overview of new franchisees-->
      <h2 v-fd-margin:small.top.bottom>{{newFranchisesNumber}} new franchise(s)</h2>
      <fd-table :headers="['Name', 'Location', 'Date', 'Details']" :items="newFranchises" class="mb-5">
        <template #row="{ item }">
          <fd-table-row @click="goToDetails(item)">
            <template #Name>
              <fd-table-cell>
                {{ item.fullName }}
              </fd-table-cell>
            </template>
            <template #Location>
              <fd-table-cell>
                {{ item.cityName }}
              </fd-table-cell>
            </template>
            <template #Date>
              <fd-table-cell>
                {{item.creationDate }}
              </fd-table-cell>
            </template>
            <template #Details>
              <fd-table-cell class="iconCell">
                <fd-icon name="navigation-right-arrow" />
              </fd-table-cell>
            </template>
          </fd-table-row>
        </template>
      </fd-table>
    <!-- Overview of existing franchisees-->
      <h2 v-fd-margin:small.top.bottom>{{existingFranchisesNumber}} existing franchise(s)</h2>
      <fd-table :headers="['Name', 'Location', 'Date', 'Mentor', 'Details']" :items="existingFranchises" class="mb-5">
        <template #row="{ item }">
          <fd-table-row @click="goToDetails(item)">
            <template #Name>
              <fd-table-cell>
                {{ item.fullName }}
              </fd-table-cell>
            </template>
            <template #Location>
              <fd-table-cell>
                {{ item.cityName }}
              </fd-table-cell>
            </template>
            <template #Date>
              <fd-table-cell>
                {{item.creationDate }}
              </fd-table-cell>
            </template>
            <template #Mentor>
              <fd-table-cell>
                {{item.mentorName }}
              </fd-table-cell>
            </template>
            <template #Details>
              <fd-table-cell class="iconCell">
                <fd-icon name="navigation-right-arrow" />
              </fd-table-cell>
            </template>
          </fd-table-row>
        </template>
      </fd-table>

    </div>
</div>    
</template>

<script>


export default {
  name: 'FranchiseOverview',
  props: ["allFranchises"],
  data: function(){
    return {
      allMentors:[],
      showData: false
    }
  },
  computed: {
    // Get array of new franchisees (without assignement)
    newFranchises: function(){
      let franchisesWithoutMentor = this.allFranchises.filter(franchise => !franchise.mentorName);
      return franchisesWithoutMentor;
    },
    // Get number of new franchisees
    newFranchisesNumber: function(){
      return this.newFranchises.length;
    },
    // Get array of existing franchisees (with mentor assignement)
    existingFranchises: function(){
      let franchisesWithMentor = this.allFranchises.filter(franchise => franchise.mentorName);
      return franchisesWithMentor;
    },
    // Get number of existing franchisees
    existingFranchisesNumber: function(){
      return this.existingFranchises.length;
    }
  },
  methods: {
    // Navigate to franchisee details page 
    goToDetails(item){
      const apiUrl = this.$backendApi + "/franchisee/" + item.businessPartner;
      fetch(apiUrl, {
        method: "PUT"})
      .then(response => {
        console.log("[DEBUG] Response by creating businessPartner in db", response)
        console.log("[DEBUG] New selected Id: ", item.businessPartner)
      })
      .then(this.$emit('changeSelectedFranchiseeId', item.businessPartner))
      .then(this.$emit('updateNavButtonStatus', true))
      .then(this.$router.push("/franchise-details"))
      .catch(err => {
        console.log(err);
      });
    }
  },
 mounted: function(){
   this.$emit('updateNavButtonStatus', false);
  },
}
</script>

<style>
  .iconCell{
    width: 5%;
  }
  .fd-shellbar__title{
    position:relative;
    top:3px;
  }
  .table th:last-child{
    opacity: 0;
  }
  .showData{
    list-style-type: none !important;
  }
</style>