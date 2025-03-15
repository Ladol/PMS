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
      <v-text-field
        v-model="search"
        label="Pesquisar"
        prepend-icon="mdi-magnify"
        single-line
        variant="outlined"
        hide-details
        class="mb-4"
      ></v-text-field>
      
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
                :color="getStateColor(student.thesisState)"
                size="small"
              >
                {{ formatThesisState(student.thesisState) }}
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
}

const students = ref<Student[]>([]);
const loading = ref(true);
const search = ref('');

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
  if (!search.value) return students.value;
  
  const searchLower = search.value.toLowerCase();
  return students.value.filter(student => 
    student.name.toLowerCase().includes(searchLower) ||
    student.istId.toLowerCase().includes(searchLower) ||
    student.email.toLowerCase().includes(searchLower) ||
    (student.thesisState && formatThesisState(student.thesisState).toLowerCase().includes(searchLower))
  );
});

const formatThesisState = (state: string | null): string => {
  if (!state) return 'Sem Tese';
  
  const stateMap: Record<string, string> = {
    'PROPOSTA_JURI_SUBMETIDA': 'Proposta Submetida',
    'APROVADO_PELO_SC': 'Aprovada pelo SC',
    'REJEITADO_PELO_SC': 'Rejeitada pelo SC',
    'PRESIDENTE_JURI_ATRIBUIDO': 'Presidente Atribuído',
    'DOCUMENTO_ASSINADO': 'Documento Assinado',
    'SUBMETIDO_AO_FENIX': 'Submetida ao Fénix'
  };
  
  return stateMap[state] || state;
};

const getStateColor = (state: string | null): string => {
  if (!state) return 'grey';
  
  const colorMap: Record<string, string> = {
    'PROPOSTA_JURI_SUBMETIDA': 'blue',
    'APROVADO_PELO_SC': 'green',
    'REJEITADO_PELO_SC': 'red',
    'PRESIDENTE_JURI_ATRIBUIDO': 'purple',
    'DOCUMENTO_ASSINADO': 'teal',
    'SUBMETIDO_AO_FENIX': 'deep-purple'
  };
  
  return colorMap[state] || 'grey';
};
</script>