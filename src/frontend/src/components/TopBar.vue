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
  { name: 'Alunos', path: '/alunos', icon: 'mdi-school-outline' },
  ...(roleStore.isStudent ? [
    { name: 'Propor Júri', path: '/propor-juri', icon: 'mdi-account-multiple-plus' }
  ] : []),
  ...(roleStore.isSC ? [
    { name: 'Aprovar Propostas', path: '/aprovar-propostas', icon: 'mdi-check-decagram' }
  ] : []),
  ...(roleStore.isCoordinator ? [
    { name: 'Atribuir Presidente Júri', path: '/atribuir-presidente', icon: 'mdi-account-tie' },
    { name: 'Assinar Documento', path: '/assinar-documento', icon: 'mdi-file-sign' },
    { name: 'Agendar Defesa', path: '/agendar-defesa', icon: 'mdi-calendar-clock' },
    { name: 'Submeter Nota', path: '/submeter-nota', icon: 'mdi-school' }
  ] : []),
  ...(roleStore.isStaff ? [
    { name: 'Submeter ao Fenix', path: '/submeter-fenix', icon: 'mdi-send' }
  ] : [])
])
</script>
