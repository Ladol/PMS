<template>
  <v-container>
    <h1 class="text-h4 mb-6">Submeter Nota de Defesa</h1>
    
    <v-alert v-if="loading" type="info" class="mb-4">
      Carregando teses...
    </v-alert>
    
    <v-alert v-else-if="propostas.length === 0" type="info" class="mb-4">
      Não existem teses pendentes para avaliação.
    </v-alert>
    
    <v-card v-else class="pa-6">
      <v-table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Aluno</th>
            <th>Presidente do Júri</th>
            <th>Data de Defesa</th>
            <th>Nota (0-20)</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="proposta in propostas" :key="proposta.id">
            <td>{{ proposta.id }}</td>
            <td>{{ proposta.student.name }}</td>
            <td>{{ proposta.juryPresident?.name }}</td>
            <td>{{ formatDate(proposta.plannedDefenseDate) }}</td>
            <td>
              <v-text-field
                v-model="grades[proposta.id]"
                type="number"
                min="0"
                max="20"
                step="1"
                density="compact"
                style="width: 80px;"
                hint="0-20"
                persistent-hint
                @input="validateGrade(proposta.id)"
              ></v-text-field>
            </td>
            <td>
              <v-btn 
                color="primary" 
                size="small"
                @click="submeterNota(proposta.id)"
                :loading="submitting === proposta.id"
                :disabled="!isValidGrade(grades[proposta.id])"
              >
                <v-icon start>mdi-school</v-icon>
                Submeter Nota
              </v-btn>
            </td>
          </tr>
        </tbody>
      </v-table>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import RemoteServices from '@/services/RemoteService'
import { useRoleStore } from '@/stores/role'

interface JuryMember {
  id: number;
  name: string;
}

interface Student {
  id: number;
  name: string;
}

interface ThesisProposal {
  id: number;
  student: Student;
  juryMembers: JuryMember[];
  submissionDate: string;
  thesisState: string;
  defenseState: string | null;
  scApprover: { id: number; name: string } | null;
  scApprovalDate: string | null;
  juryPresident: { id: number; name: string } | null;
  coordinatorAssigner: { id: number; name: string } | null;
  presidentAssignmentDate: string | null;
  documentSigner: { id: number; name: string } | null;
  documentSignDate: string | null;
  signedDocumentPath: string | null;
  fenixSubmitter: { id: number; name: string } | null;
  fenixSubmissionDate: string | null;
  defenseScheduler: { id: number; name: string } | null;
  defenseScheduleDate: string | null;
  plannedDefenseDate: string | null;
  grader: { id: number; name: string } | null;
  gradingDate: string | null;
  grade: number | null;
}

const roleStore = useRoleStore();
const propostas = ref<ThesisProposal[]>([]);
const loading = ref(true);
const submitting = ref<number | null>(null);
const grades = reactive<Record<number, string>>({});

onMounted(async () => {
  try {
    // Get all theses with defense scheduled
    const response = await RemoteServices.getScheduledDefenses();
    
    let theses = Array.isArray(response) ? response : response.data;
    
    // Update defense state for theses with past defense dates
    const currentDate = new Date();
    const updatedTheses = [];
    
    for (const thesis of theses) {
      if (thesis.defenseState === 'DEFESA_AGENDADA' && thesis.plannedDefenseDate) {
        const defenseDate = new Date(thesis.plannedDefenseDate);
        
        if (defenseDate <= currentDate) {
          // Update defense state to EM_REVISAO
          try {
            await RemoteServices.updateDefenseState(thesis.id, 'EM_REVISAO');
            thesis.defenseState = 'EM_REVISAO';
          } catch (error) {
            console.error(`Error updating defense state for thesis ${thesis.id}:`, error);
          }
        }
      }
      
      // Only include theses in EM_REVISAO state
      if (thesis.defenseState === 'EM_REVISAO') {
        updatedTheses.push(thesis);
        grades[thesis.id] = thesis.grade?.toString() || '';
      }
    }
    
    propostas.value = updatedTheses;
    console.log('Loaded theses for grading:', propostas.value);
  } catch (error) {
    console.error('Erro ao carregar teses para avaliação:', error);
  } finally {
    loading.value = false;
  }
});

const formatDate = (dateString: string | undefined | null) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('pt-PT');
};

const validateGrade = (id: number) => {
  const gradeValue = parseInt(grades[id]);
  if (isNaN(gradeValue) || gradeValue < 0 || gradeValue > 20) {
    grades[id] = '';
  }
};

const isValidGrade = (grade: string | undefined) => {
  if (!grade) return false;
  const gradeValue = parseInt(grade);
  return !isNaN(gradeValue) && gradeValue >= 0 && gradeValue <= 20;
};

const submeterNota = async (id: number) => {
  try {
    const currentCoordinatorId = roleStore.currentPerson?.id;
    
    if (!currentCoordinatorId) {
      alert('Erro: Coordenador não identificado. Por favor, selecione um coordenador.');
      return;
    }

    const grade = parseInt(grades[id]);
    if (!isValidGrade(grades[id])) {
      alert('Por favor, insira uma nota válida entre 0 e 20.');
      return;
    }

    submitting.value = id;
    
    await RemoteServices.gradeThesis(id, currentCoordinatorId, grade);
    
    // Remove the graded proposal from the list
    propostas.value = propostas.value.filter(p => p.id !== id);
    
    // Show success message
    alert('Nota submetida com sucesso!');
  } catch (error) {
    console.error('Erro ao submeter nota:', error);
    alert('Erro ao submeter nota');
  } finally {
    submitting.value = null;
  }
};
</script>