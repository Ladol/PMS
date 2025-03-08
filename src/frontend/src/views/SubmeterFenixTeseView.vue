<template>
  <v-container>
    <h1 class="text-h4 mb-6">Submeter Teses ao Fenix</h1>
    
    <v-alert v-if="loading" type="info" class="mb-4">
      Carregando propostas...
    </v-alert>
    
    <v-alert v-else-if="propostas.length === 0" type="info" class="mb-4">
      Não existem propostas pendentes para submissão ao Fenix.
    </v-alert>
    
    <v-card v-else class="pa-6">
      <v-table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Aluno</th>
            <th>Presidente do Júri</th>
            <th>Documento Assinado</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="proposta in propostas" :key="proposta.id">
            <td>{{ proposta.id }}</td>
            <td>{{ proposta.student.name }}</td>
            <td>{{ proposta.juryPresident?.name }}</td>
            <td>
              <v-chip color="success" v-if="proposta.signedDocumentPath">
                <v-icon start>mdi-check</v-icon>
                Documento Assinado
              </v-chip>
            </td>
            <td>
              <v-btn 
                color="primary" 
                size="small"
                @click="submeterAoFenix(proposta.id)"
                :loading="submitting === proposta.id"
              >
                <v-icon start>mdi-send</v-icon>
                Submeter ao Fenix
              </v-btn>
            </td>
          </tr>
        </tbody>
      </v-table>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
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

onMounted(async () => {
  try {
    const response = await RemoteServices.getSignedProposals();
    propostas.value = Array.isArray(response) ? response : response.data;
    console.log('Loaded signed proposals:', propostas.value);
  } catch (error) {
    console.error('Erro ao carregar propostas assinadas:', error);
  } finally {
    loading.value = false;
  }
});

const submeterAoFenix = async (id: number) => {
  try {
    const currentStaffId = roleStore.currentPerson?.id;
    
    if (!currentStaffId) {
      alert('Erro: Staff não identificado. Por favor, selecione um utilizador staff.');
      return;
    }

    submitting.value = id;
    await RemoteServices.submitToFenix(id, currentStaffId);
    
    // Remove the submitted proposal from the list
    propostas.value = propostas.value.filter(p => p.id !== id);
    
    // Show success message
    alert('Proposta submetida ao Fenix com sucesso!');
  } catch (error) {
    console.error('Erro ao submeter proposta ao Fenix:', error);
    alert('Erro ao submeter proposta ao Fenix');
  } finally {
    submitting.value = null;
  }
};
</script>