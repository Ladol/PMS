<template>
  <v-container>
    <div class="d-flex align-center mb-6">
      <v-btn icon class="mr-4" @click="router.back()">
        <v-icon>mdi-arrow-left</v-icon>
      </v-btn>
      <h1 class="text-h4">Detalhes do Aluno</h1>
    </div>

    <v-alert v-if="loading" type="info" class="mb-4">
      Carregando informações do aluno...
    </v-alert>

    <v-alert v-else-if="error" type="error" class="mb-4">
      {{ error }}
    </v-alert>

    <template v-else>
      <!-- Personal Information Section -->
      <v-card class="mb-6">
        <v-card-title class="text-h5 bg-primary text-white">
          <v-icon class="mr-2">mdi-account</v-icon>
          Informações Pessoais
        </v-card-title>
        <v-card-text class="pa-4">
          <v-row>
            <v-col cols="12" md="4">
              <strong>Nome:</strong> {{ student.name }}
            </v-col>
            <v-col cols="12" md="4">
              <strong>IST ID:</strong> {{ student.istId }}
            </v-col>
            <v-col cols="12" md="4">
              <strong>Email:</strong> {{ student.email }}
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <!-- Thesis Workflow Section -->
      <v-card class="mb-6">
        <v-card-title class="text-h5 bg-primary text-white">
          <v-icon class="mr-2">mdi-file-document</v-icon>
          Workflow de Tese
        </v-card-title>
        <v-card-text class="pa-4">
          <v-timeline align="start" direction="vertical" line-thickness="2">
            <v-timeline-item
              v-for="(state, index) in filteredThesisWorkflowStates"
              :key="index"
              :dot-color="getThesisStateColor(state.state, student.thesisState)"
              :icon="getThesisStateIcon(state.state, student.thesisState)"
              :size="getThesisStateSize(state.state, student.thesisState)"
            >
              <div class="d-flex align-center">
                <div>
                  <div class="text-h6">{{ state.title }}</div>
                  <div>{{ state.description }}</div>
                </div>
                <v-chip
                  v-if="student.thesisState === state.state"
                  color="primary"
                  class="ml-4"
                >
                  Estado Atual da Tese
                </v-chip>
              </div>
            </v-timeline-item>
          </v-timeline>
        </v-card-text>
      </v-card>

      <!-- Defense Workflow Section -->
      <v-card>
        <v-card-title class="text-h5 bg-primary text-white">
          <v-icon class="mr-2">mdi-school</v-icon>
          Workflow de Defesa
        </v-card-title>
        <v-card-text class="pa-4">
          <v-timeline align="start" direction="vertical" line-thickness="2">
            <v-timeline-item
              v-for="(state, index) in defenseWorkflowStates"
              :key="index"
              :dot-color="getDefenseStateColor(state.state, student.defenseState)"
              :icon="getDefenseStateIcon(state.state, student.defenseState)"
              :size="getDefenseStateSize(state.state, student.defenseState)"
            >
              <div class="d-flex align-center">
                <div>
                  <div class="text-h6">{{ state.title }}</div>
                  <div>{{ state.description }}</div>
                </div>
                <v-chip
                  v-if="student.defenseState === state.state"
                  color="primary"
                  class="ml-4"
                >
                  Estado Atual da Defesa
                </v-chip>
              </div>
            </v-timeline-item>
          </v-timeline>
        </v-card-text>
      </v-card>
    </template>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import RemoteServices from '@/services/RemoteService'

const router = useRouter()
const route = useRoute()
const studentId = computed(() => Number(route.params.id))

interface Student {
  id: number;
  name: string;
  istId: string;
  email: string;
  thesisState: string | null;
  defenseState: string | null;
}

const student = ref<Student>({
  id: 0,
  name: '',
  istId: '',
  email: '',
  thesisState: null,
  defenseState: null
})
const loading = ref(true)
const error = ref('')

// Thesis workflow states
const thesisWorkflowStates = [
  {
    state: 'PROPOSTA_JURI_SUBMETIDA',
    title: 'Proposta Submetida',
    description: 'A proposta de júri foi submetida pelo orientador.'
  },
  {
    state: 'APROVADO_PELO_SC',
    title: 'Aprovada pelo SC',
    description: 'A proposta foi aprovada pelo SC.'
  },
  {
    state: 'REJEITADO_PELO_SC',
    title: 'Rejeitada pelo SC',
    description: 'A proposta foi rejeitada pelo SC.'
  },
  {
    state: 'PRESIDENTE_JURI_ATRIBUIDO',
    title: 'Presidente Atribuído',
    description: 'O presidente do júri foi atribuído pelo coordenador.'
  },
  {
    state: 'DOCUMENTO_ASSINADO',
    title: 'Documento Assinado',
    description: 'O documento foi assinado pelo presidente do júri.'
  },
  {
    state: 'TESE_SUBMETIDO_AO_FENIX',
    title: 'Submetida ao Fénix',
    description: 'A tese foi submetida ao Fenix.'
  }
]

// Defense workflow states
const defenseWorkflowStates = [
  {
    state: 'DEFESA_AGENDADA',
    title: 'Defesa Agendada',
    description: 'A defesa da tese foi agendada.'
  },
  {
    state: 'EM_REVISAO',
    title: 'Em Revisão',
    description: 'A tese está em processo de revisão após a defesa.'
  },
  {
    state: 'DEFESA_SUBMETIDO_AO_FENIX',
    title: 'Defesa Submetida ao Fénix',
    description: 'A defesa foi concluída e submetida ao Fenix.'
  }
]

onMounted(async () => {
  try {
    const response = await RemoteServices.getStudent(studentId.value);
    // TODO: create StudentDTO
    student.value = {
      id: response.id,
      name: response.name,
      istId: response.istId,
      email: response.email,
      thesisState: response.thesisState,
      defenseState: response.defenseState
    };
    loading.value = false;
  } catch (err) {
    error.value = 'Erro ao carregar informações do aluno. Por favor, tente novamente.';
    loading.value = false;
    console.error('Error loading student:', err);
  }
});

// Helper functions for thesis timeline
const getThesisStateColor = (state: string, currentState: string | null) => {
  if (!currentState) return 'grey'
  
  // Special case for rejected state
  if (state === 'REJEITADO_PELO_SC') {
    return currentState === 'REJEITADO_PELO_SC' ? 'error' : 'grey'
  }
  
  // Get index of current state in workflow
  const currentIndex = thesisWorkflowStates.findIndex(s => s.state === currentState)
  const stateIndex = thesisWorkflowStates.findIndex(s => s.state === state)
  
  // If current state is rejected, only show that as active
  if (currentState === 'REJEITADO_PELO_SC') {
    return state === 'REJEITADO_PELO_SC' ? 'error' : 'grey'
  }
  
  if (stateIndex <= currentIndex) return 'success' // Completed
  //if (stateIndex === currentIndex) return 'primary' // Current
  return 'grey' // Not reached yet
}

const getThesisStateIcon = (state: string, currentState: string | null) => {
  if (!currentState) return 'mdi-circle-outline'
  
  // Special case for rejected state
  if (state === 'REJEITADO_PELO_SC') {
    return 'mdi-close-circle'
  }
  
  const currentIndex = thesisWorkflowStates.findIndex(s => s.state === currentState)
  const stateIndex = thesisWorkflowStates.findIndex(s => s.state === state)
  
  // If current state is rejected, only show that as active
  if (currentState === 'REJEITADO_PELO_SC') {
    return state === 'REJEITADO_PELO_SC' ? 'mdi-close-circle' : 'mdi-circle-outline'
  }
  
  // For current and previous states, show check circle
  if (stateIndex <= currentIndex) return 'mdi-check-circle'
  
  // For future states
  return 'mdi-circle-outline'
}

const getThesisStateSize = (state: string, currentState: string | null) => {
  if (!currentState) return 'small'
  
  const currentIndex = thesisWorkflowStates.findIndex(s => s.state === currentState)
  const stateIndex = thesisWorkflowStates.findIndex(s => s.state === state)
  
  // Make the current state larger
  if (state === currentState) return 'large'
  return 'small'
}

// Helper functions for defense timeline
const getDefenseStateColor = (state: string, currentState: string | null) => {
  if (!currentState) return 'grey'
  
  const currentIndex = defenseWorkflowStates.findIndex(s => s.state === currentState)
  const stateIndex = defenseWorkflowStates.findIndex(s => s.state === state)
  
  if (stateIndex <= currentIndex) return 'success'
  //if (stateIndex === currentIndex) return 'primary'
  return 'grey'
}

const getDefenseStateIcon = (state: string, currentState: string | null) => {
  if (!currentState) return 'mdi-circle-outline'
  
  const currentIndex = defenseWorkflowStates.findIndex(s => s.state === currentState)
  const stateIndex = defenseWorkflowStates.findIndex(s => s.state === state)
  
  if (stateIndex <= currentIndex) return 'mdi-check-circle'
  return 'mdi-circle-outline'
}

const getDefenseStateSize = (state: string, currentState: string | null) => {
  if (!currentState) return 'small'
  
  const currentIndex = defenseWorkflowStates.findIndex(s => s.state === currentState)
  const stateIndex = defenseWorkflowStates.findIndex(s => s.state === state)
  
  if (stateIndex === currentIndex) return 'large'
  return 'small'
}

const filteredThesisWorkflowStates = computed(() => {
  // Create a copy of the workflow states
  const states = [...thesisWorkflowStates];
  
  // Find the indices of approval and rejection states
  const approvalIndex = states.findIndex(s => s.state === 'APROVADO_PELO_SC');
  const rejectionIndex = states.findIndex(s => s.state === 'REJEITADO_PELO_SC');
  
  // If the current state is rejected, show the rejection state
  if (student.value.thesisState === 'REJEITADO_PELO_SC') {
    // Remove the approval state
    if (approvalIndex !== -1) {
      states.splice(approvalIndex, 1);
    }
  } else {
    // Otherwise, show the approval state and remove the rejection state
    if (rejectionIndex !== -1) {
      states.splice(rejectionIndex, 1);
    }
  }
  
  return states;
});
</script>