<template>
  <v-app-bar color="secondary" :height="36" class="px-2">
    <v-toolbar-items>
      <v-btn
        href="https://dei.tecnico.ulisboa.pt/"
        selected-class="no-active"
        class="dei-title"
        size="small"
      >
        Departamento de Engenharia Informática
      </v-btn>
    </v-toolbar-items>
    <v-spacer />
    <span>Utilizador Atual: {{ currentPerson ? currentPerson.name : 'Nenhum' }} ({{ currentRole }})</span>
    <v-spacer />
    <v-toolbar-items class="align-center">
      <DarkModeSwitch />
    </v-toolbar-items>

    <v-toolbar-items class="ms-2">
      <v-btn size="small" @click="openRoleDialog('student')">Aluno</v-btn>
      <v-btn size="small" @click="openRoleDialog('coordinator')">Coordenador</v-btn>
      <v-btn size="small" @click="openRoleDialog('staff')">Staff</v-btn>
      <v-btn size="small" @click="openRoleDialog('admin')">SC</v-btn>
      <v-btn size="small" @click="openRoleDialog('teacher')">Professor</v-btn>
    </v-toolbar-items>
    <v-toolbar-items class="ms-2">
      <v-btn size="small" variant="text" @click="logout">
        Terminar sessão
        <v-icon size="small" class="ms-1" icon="mdi-logout"></v-icon>
      </v-btn>
    </v-toolbar-items>
  </v-app-bar>

  <!-- Person Selection Dialog -->
  <v-dialog v-model="showDialog" max-width="500px">
    <v-card>
      <v-card-title>Selecionar {{ getRoleDisplayName(selectedRole) }}</v-card-title>
      <v-card-text>
        <v-select
          v-model="selectedPerson"
          :items="filteredPeople"
          item-title="name"
          item-value="id"
          label="Selecione uma pessoa"
          return-object
        ></v-select>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="primary" @click="confirmSelection">Confirmar</v-btn>
        <v-btn color="error" @click="showDialog = false">Cancelar</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import DarkModeSwitch from './DarkModeSwitch.vue'
import { useRoleStore } from '@/stores/role'
import { ref, watch } from 'vue'
import RemoteServices from '@/services/RemoteService'
import type PersonDto from '@/models/PersonDto'

const roleStore = useRoleStore()
const currentRole = ref(roleStore.currentRole)
const currentPerson = ref(roleStore.currentPerson)

// Dialog control
const showDialog = ref(false)
const selectedRole = ref('')
const selectedPerson = ref<PersonDto | null>(null)
const people = ref<PersonDto[]>([])
const filteredPeople = ref<PersonDto[]>([])

// Fetch all people when component is mounted
RemoteServices.getPeople().then(data => {
  people.value = data
})

// Get display name for role
const getRoleDisplayName = (role: string) => {
  switch(role) {
    case 'student': return 'Aluno'
    case 'coordinator': return 'Coordenador'
    case 'staff': return 'Staff'
    case 'admin': return 'SC'
    case 'teacher': return 'Professor'
    default: return role
  }
}

// Open dialog for role selection
const openRoleDialog = (role: string) => {
  selectedRole.value = role
  selectedPerson.value = null  // Reset selected person when opening dialog
  
  // Refresh the people list before showing the dialog
  RemoteServices.getPeople().then(data => {
    people.value = data
    
    // Filter people by the selected role, safely handling undefined types
    filteredPeople.value = people.value.filter(person => 
      person.type && person.type.toLowerCase() === role.toLowerCase()
    )
    
    showDialog.value = true
  })
}

// Confirm person selection
const confirmSelection = () => {
  if (selectedPerson.value) {
    roleStore.setCurrentPerson(selectedPerson.value)
    roleStore.currentRole = selectedRole.value
    showDialog.value = false
  }
}

// Logout function
const logout = () => {
  roleStore.setCurrentPerson(null)
  roleStore.currentRole = ''
}

// Watch for role changes
watch(
  () => roleStore.currentRole,
  (newRole) => {
    currentRole.value = newRole
  }
)

// Watch for person changes
watch(
  () => roleStore.currentPerson,
  (newPerson) => {
    currentPerson.value = newPerson
  }
)
</script>

<style scoped>
.dei-title:hover {
  background-color: transparent !important;
}
</style>
