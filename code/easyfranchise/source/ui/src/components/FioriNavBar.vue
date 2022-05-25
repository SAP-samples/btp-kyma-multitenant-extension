<template>
    <div>
      <fd-shell-bar>
          <fd-shell-bar-group position="start">
              <fd-shell-bar-action v-show="showNavButtonStatus"> 
                    <fd-shell-bar-action-button icon="nav-back" @click="$router.go(-1)" />
                </fd-shell-bar-action>
              <fd-shell-bar-logo 
                v-if="logoUrl"
                :alt="companyName" 
                :src="logoUrl"  
                width="48"
                height="24" 
                @click="goHome" />
              <fd-shell-bar-product :productTitle="companyName" @click="goHome" />
              <fd-shell-bar-subtitle>Easy Franchise</fd-shell-bar-subtitle>
          </fd-shell-bar-group>
          <fd-shell-bar-group position="end">
              <fd-shell-bar-actions>
                <fd-shell-bar-action>
                    <fd-shell-bar-action-button icon="sys-help" @click="goToInfoPage" />
                </fd-shell-bar-action>
                <fd-shell-bar-action>
                    <fd-shell-bar-action-button icon="settings" @click="goToAdminCorner" />
                </fd-shell-bar-action>
                <fd-shell-bar-action>
                    <fd-shell-bar-user-menu ref="userMenu">
                        <fd-menu>
                            <fd-menu-list>
                                <fd-menu-item @click="goToUserProfile">
                                    User Profile
                                </fd-menu-item>
                                <fd-menu-item @click="signOut">
                                    Sign out
                                </fd-menu-item>
                            </fd-menu-list>
                        </fd-menu>
                    </fd-shell-bar-user-menu>
                </fd-shell-bar-action>
              </fd-shell-bar-actions>
          </fd-shell-bar-group>
      </fd-shell-bar>
  </div>
</template>

<script>


export default {
    name: 'FioriNavBar',
    props: ['companyName', 'logoUrl', 'showNavButtonStatus'],
    methods: {
        goHome(){
            this.$emit('updateNavButtonStatus', false);
            this.$router.push('/home');
        },
        goToAdminCorner(){
            this.$emit('updateNavButtonStatus', true);
            this.$router.push('/admin-corner');
        },
        goToUserProfile(){
            this.$emit('updateNavButtonStatus', true);
            this.$router.push('/user-profile');
        },
        goToInfoPage(){
            this.$emit('updateNavButtonStatus', true);
            this.$router.push('/info-page');
        },
        signOut(){
            alert('Not yet implemented');
        }
    }
}
</script>

<style>
.fd-button--shell, a.fd-button--shell{
    color: #d1e8ff;
}
.fd-identifier--s{
    color: #ffffff;
}
</style>