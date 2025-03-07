<template>
  <UtilBar />
  <NavBar :navbarItems="navbarItems" />
</template>

<script setup lang="ts">
import UtilBar from '@/components/UtilBar.vue'
import NavBar from '@/components/NavBar.vue'
import { useRoleStore } from '@/stores/role'
import { computed } from 'vue'

const roleStore = useRoleStore()

// Use computed property to make navbarItems reactive to role changes
const navbarItems = computed(() => [
  { name: 'Pessoal', path: '/people', icon: 'mdi-account-group' },
  { name: 'Estatísticas', path: '/statistics', icon: 'mdi-chart-bar' },
  ...(roleStore.isStudent ? [
    { name: 'Propor Júri', path: '/propor-juri', icon: 'mdi-account-multiple-plus' }
  ] : [])
])
</script>
