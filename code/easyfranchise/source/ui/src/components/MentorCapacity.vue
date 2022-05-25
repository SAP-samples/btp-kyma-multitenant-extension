<template>
    <span :class="['capacity-' + actualMentorCapacity]">
        {{ actualMentorCapacity}}/10
    </span>
</template>

<script>
    export default {
        name: "MentorCapacity",
        props:["mentorId", "allFranchises"],
        computed: {
            actualMentorCapacity: function(){
                return this.calculateMentorCapacity(this.mentorId);
            }
        },
        methods: {
            // Calcultate how many times the mentor is assigned in all franchises
            calculateMentorCapacity(mentorId){
                console.log("[INFO] Calculating the mentor capacity.");
                const allFranchisesWithTheActualMentor = this.allFranchises.filter(
                franchise => franchise.mentorId === mentorId
                );
                return allFranchisesWithTheActualMentor.length;
            }
        }
    }
</script>

<style>

</style>