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
        :loading="submitting"
      >
        Submeter Proposta
      </v-btn>
    </v-card>
    
    <!-- Success Dialog -->
    <v-dialog v-model="successDialog" max-width="400">
      <v-card>
        <v-card-title class="text-h5 bg-success text-white">
          Sucesso
        </v-card-title>
        <v-card-text class="pt-4">
          Proposta de júri submetida com sucesso!
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" variant="text" @click="successDialog = false">
            Fechar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    
    <!-- Error Dialog -->
    <v-dialog v-model="errorDialog" max-width="400">
      <v-card>
        <v-card-title class="text-h5 bg-error text-white">
          Erro
        </v-card-title>
        <v-card-text class="pt-4">
          {{ errorMessage }}
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" variant="text" @click="errorDialog = false">
            Fechar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
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
const submitting = ref(false)
const successDialog = ref(false)
const errorDialog = ref(false)
const errorMessage = ref('Ocorreu um erro ao submeter a proposta.')

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
      errorMessage.value = 'Por favor, selecione um estudante primeiro.'
      errorDialog.value = true
      return
    }
    
    submitting.value = true
    const professorIds = selectedProfessores.value
      .map(p => p.id)
      .filter((id): id is number => id !== undefined)
    
    await RemoteServices.submeterPropostaJuri(professorIds)
    
    // Clear selected professors after successful submission
    selectedProfessores.value = []
    
    // Show success dialog
    successDialog.value = true
  } catch (error) {
    console.error('Erro ao submeter proposta:', error)
    errorMessage.value = 'Erro ao submeter proposta. Por favor, tente novamente.'
    errorDialog.value = true
  } finally {
    submitting.value = false
  }
}
</script>