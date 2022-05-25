<template>
  <!-- Introduction modal - displays information when basic information are not maintained -->
        <fd-modal
          name="Introduction"
          ref="Introduction"
          title="Welcome to Easy Franchise"
        >
          <div>
            <template>
              <fd-form-label>Thanks for using our partner app Easy Franchise. Before you start managing your franchisees, you need first to configure the application.</fd-form-label>
              <fd-button @click="goToAdminCorner" styling="emphasized">Go to admin corner</fd-button>
            </template>
          </div>
        </fd-modal>
</template>

<script>
export default {
  name: "modalIntro",
  methods: {
    // Closing modal and redirecting to admin corner page
    goToAdminCorner() {
      this.$fdModal.close("Introduction");
      this.$router.push('/admin-corner');
    }
  }
};
</script>

<style>
</style>
