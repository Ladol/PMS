<template>
  <v-container>
    <h1 class="text-h4 mb-6">Proposta de Júri de Tese</h1>
    
    <v-alert v-if="!currentStudent" type="warning" class="mb-4">
      Por favor, selecione um estudante primeiro.
    </v-alert>
    
    <v-card v-else class="pa-6">
      <v-alert type="info" class="mb-4">
        Selecione entre 1 a 5 professores para compor o júri da sua tese
      </v-alert>

      <v-combobox
        v-model="selectedProfessores"
        :items="professores"
        item-title="name"
        item-value="id"
        label="Selecionar Professores"
        multiple
        chips
        :rules="[v => v.length >= 1 && v.length <=5 || 'Selecione entre 1 a 5 professores']"
      ></v-combobox>

      <v-btn 
        color="primary"
        :disabled="!isValid"
        @click="submeterProposta"
        class="mt-4"
      >
        Submeter Proposta
      </v-btn>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import RemoteServices from '@/services/RemoteService'
import type PersonDto from '@/models/PersonDto'
import { useRoleStore } from '@/stores/role'

const roleStore = useRoleStore()
const currentStudent = computed(() => roleStore.currentPerson)

const professores = ref<PersonDto[]>([])
const selectedProfessores = ref<PersonDto[]>([])

// Fetch only teachers
RemoteServices.getPeopleByType('TEACHER').then(data => {
  professores.value = data
})

const isValid = computed(() => {
  return selectedProfessores.value.length >= 1 && 
         selectedProfessores.value.length <= 5
})

const submeterProposta = async () => {
  try {
    if (!currentStudent.value) {
      alert('Por favor, selecione um estudante primeiro')
      return
    }
    
    const professorIds = selectedProfessores.value
      .map(p => p.id)
      .filter((id): id is number => id !== undefined)
    await RemoteServices.submeterPropostaJuri(professorIds)
    alert('Proposta submetida com sucesso!')
  } catch (error) {
    console.error('Erro ao submeter proposta:', error)
    alert('Erro ao submeter proposta')
  }
}
</script>