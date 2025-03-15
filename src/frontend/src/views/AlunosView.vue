<template>
  <v-container>
    <h1 class="text-h4 mb-6">Lista de Alunos</h1>
    
    <v-alert v-if="loading" type="info" class="mb-4">
      Carregando alunos...
    </v-alert>
    
    <v-alert v-else-if="students.length === 0" type="info" class="mb-4">
      Não existem alunos registrados no sistema.
    </v-alert>
    
    <v-card v-else class="pa-6">
      <div class="d-flex flex-wrap gap-4 mb-4">
        <v-text-field
          v-model="search"
          label="Pesquisar"
          prepend-icon="mdi-magnify"
          single-line
          variant="outlined"
          hide-details
          class="flex-grow-1"
        ></v-text-field>
        
        <v-select
          v-model="stateFilter"
          :items="thesisStateOptions"
          label="Filtrar por Estado"
          prepend-icon="mdi-filter-variant"
          variant="outlined"
          hide-details
          class="flex-grow-0"
          style="min-width: 250px;"
          clearable
        ></v-select>
      </div>
      
      <v-table>
        <thead>
          <tr>
            <th class="text-left" width="30%">Nome</th>
            <th class="text-left" width="15%">IST ID</th>
            <th class="text-left" width="30%">Email</th>
            <th class="text-left" width="25%">Estado da Tese</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="student in filteredStudents" :key="student.id">
            <td class="text-left" width="30%">{{ student.name }}</td>
            <td class="text-left" width="15%">{{ student.istId }}</td>
            <td class="text-left" width="30%">{{ student.email }}</td>
            <td class="text-left" width="25%">
              <v-chip
                v-if="student.defenseState"
                :color="getStateColor(student.defenseState)"
                size="small"
              >
                {{ formatState(student.defenseState) }}
              </v-chip>
              <v-chip
                v-else
                :color="getStateColor(student.thesisState)"
                size="small"
              >
                {{ formatState(student.thesisState) }}
              </v-chip>
            </td>
          </tr>
        </tbody>
      </v-table>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import RemoteServices from '@/services/RemoteService'

interface Student {
  id: number;
  name: string;
  istId: string;
  email: string;
  thesisState: string | null;
  defenseState: string | null;
}

const students = ref<Student[]>([]);
const loading = ref(true);
const search = ref('');
const stateFilter = ref('');

// Define thesis state options for the dropdown
const thesisStateOptions = [
  { title: 'Sem Tese', value: 'null' },
  // Thesis States
  { title: 'Proposta Submetida', value: 'thesis:PROPOSTA_JURI_SUBMETIDA' },
  { title: 'Aprovada pelo SC', value: 'thesis:APROVADO_PELO_SC' },
  { title: 'Rejeitada pelo SC', value: 'thesis:REJEITADO_PELO_SC' },
  { title: 'Presidente Atribuído', value: 'thesis:PRESIDENTE_JURI_ATRIBUIDO' },
  { title: 'Documento Assinado', value: 'thesis:DOCUMENTO_ASSINADO' },
  { title: 'Tese Submetida ao Fenix', value: 'thesis:TESE_SUBMETIDO_AO_FENIX' },
  // Defense States
  { title: 'Defesa Agendada', value: 'defense:DEFESA_AGENDADA' },
  { title: 'Em Revisão', value: 'defense:EM_REVISAO' },
  { title: 'Defesa Submetida ao Fenix', value: 'defense:DEFESA_SUBMETIDO_AO_FENIX' }
];

onMounted(async () => {
  try {
    const response = await RemoteServices.getStudents();
    students.value = Array.isArray(response) ? response : response.data;
    console.log('Loaded students:', students.value);
  } catch (error) {
    console.error('Erro ao carregar alunos:', error);
  } finally {
    loading.value = false;
  }
});

const filteredStudents = computed(() => {
  let result = students.value;
  
  // Apply thesis state filter if selected
  if (stateFilter.value) {
    if (stateFilter.value === 'null') {
      result = result.filter(student => !student.thesisState && !student.defenseState);
    } else if (stateFilter.value.startsWith('thesis:')) {
      const thesisStateValue = stateFilter.value.replace('thesis:', '');
      result = result.filter(student => student.thesisState === thesisStateValue);
    } else if (stateFilter.value.startsWith('defense:')) {
      const defenseStateValue = stateFilter.value.replace('defense:', '');
      result = result.filter(student => student.defenseState === defenseStateValue);
    }
  }
  
  // Apply text search filter
  if (search.value) {
    const searchLower = search.value.toLowerCase();
    result = result.filter(student => 
      student.name.toLowerCase().includes(searchLower) ||
      student.istId.toLowerCase().includes(searchLower) ||
      student.email.toLowerCase().includes(searchLower) ||
      (getDisplayState(student).toLowerCase().includes(searchLower))
    );
  }
  
  return result;
});

// Helper function to determine which state to display
const getDisplayState = (student: Student): string => {
  // If there's a defense state, prioritize it
  if (student.defenseState) {
    return formatState(student.defenseState);
  }
  // Otherwise use thesis state
  return formatState(student.thesisState);
};

const getStateColor = (state: string | null): string => {
  if (!state) return 'grey';
  
  const colorMap: Record<string, string> = {
    // Thesis States
    'PROPOSTA_JURI_SUBMETIDA': 'blue',
    'APROVADO_PELO_SC': 'green',
    'REJEITADO_PELO_SC': 'red',
    'PRESIDENTE_JURI_ATRIBUIDO': 'purple',
    'DOCUMENTO_ASSINADO': 'teal',
    'TESE_SUBMETIDO_AO_FENIX': 'deep-purple',
    // Defense States
    'DEFESA_AGENDADA': 'orange',
    'EM_REVISAO': 'amber-darken-2',
    'DEFESA_SUBMETIDO_AO_FENIX': 'light-blue'
  };
  
  return colorMap[state] || 'grey';
};

const formatState = (state: string | null): string => {
  if (!state) return 'Sem Tese';
  
  const stateMap: Record<string, string> = {
    // Defense States
    'DEFESA_AGENDADA': 'Defesa Agendada',
    'EM_REVISAO': 'Em Revisão',
    'DEFESA_SUBMETIDO_AO_FENIX': 'Defesa Submetida ao Fenix',
    // Thesis States
    'PROPOSTA_JURI_SUBMETIDA': 'Proposta Submetida',
    'APROVADO_PELO_SC': 'Aprovada pelo SC',
    'REJEITADO_PELO_SC': 'Rejeitada pelo SC',
    'PRESIDENTE_JURI_ATRIBUIDO': 'Presidente Atribuído',
    'DOCUMENTO_ASSINADO': 'Documento Assinado',
    'TESE_SUBMETIDO_AO_FENIX': 'Tese Submetida ao Fenix'
  };
  
  return stateMap[state] || state;
};
</script>