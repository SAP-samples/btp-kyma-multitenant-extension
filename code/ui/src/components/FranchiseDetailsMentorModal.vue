<template>
  <fd-modal name="assignMentorModal" ref="assignMentorModal" title="Please select a franchise mentor">
      <template>
        <fd-container fluid>
          <fd-alert
                type="error"
                v-model="alertStatus"
                :dismissible="false"
              >Please select a mentor to continue with the assignement
          </fd-alert>
          <fd-alert type="information" :dismissible="false" class="mb-3">
            Note: 1 mentor can only be assigned to maximum 10 franchises companies
          </fd-alert>
          <fd-table
            :headers="['index', 'name', 'experience', 'capacity']"
            :selectionMode="selectionMode"
            :selectedIds.sync="selectedIds"
            :items="allMentors"
          >
            <template #row="{ canSelect, item, toggle, selected, setSelected }">
              <fd-table-row>
                <template #index>
                  <fd-table-cell>
                    <fd-checkbox
                      :disabled="!canSelect"
                      @update="setSelected"
                      @click="selectMentor(item)"
                      :model-value="selected"
                    />
                  </fd-table-cell>
                </template>
                <template #name>
                  <fd-table-cell>{{ item.name }}</fd-table-cell>
                </template>
                <template #experience>
                  <fd-table-cell>{{ item.experience }}</fd-table-cell>
                </template>
                <template #capacity>
                  <fd-table-cell>
                    <mentor-capacity :mentorId="item.id" :allFranchises="allFranchises" />
                  </fd-table-cell>
                </template>
              </fd-table-row>
            </template>
          </fd-table>
        </fd-container>
      </template>
      <template #actions="{close}">
        <fd-button @click="assignMentor()" styling="emphasized">Assign and inform mentor</fd-button>
        <fd-button @click="close" styling="light">Cancel</fd-button>
      </template>
    </fd-modal>
</template>

<script>
import MentorCapacity from "@/components/MentorCapacity.vue"

export default {
  name: "assignMentorModal",
  props: ["allMentors", "allFranchises", "assignedMentorId"],
  components: {
    MentorCapacity
  },
  data: () => ({
    alertStatus: false,
    selectionMode: "single",
    selectedMentor: "",
  }),
  computed: {
    selectedIds: function(){
      const ids = [];
      console.log('[DEBUG] Checking already assigned mentor Id: ', this.assignedMentorId)
      if(this.assignedMentorId){
        ids.push(this.assignedMentorId);
      }
      return ids;
    }
  },
  methods: {
    selectMentor(item){
      console.log("[DEBUG] Selected mentor: ", item);
      this.selectedMentor = item;
    },
    assignMentor(){
      if(this.selectedMentor){
        this.alertStatus = false;
        this.$emit('assignMentor', this.selectedMentor);
        this.$fdModal.close("assignMentorModal");
      } else {
        console.log("[INFO] No mentor selected")
        this.alertStatus = true;
      }
    }
  }
};
</script>

<style>
.fd-modal.fd-modal--overrides{
  max-width: 700px !important;
}
.fd-modal__body {
  padding: 30px !important;
}
</style>
