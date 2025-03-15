<template>
  <v-container>
    <h1 class="text-h4 mb-6">Aprovar Propostas de Júri</h1>
    
    <v-alert v-if="loading" type="info" class="mb-4">
      Carregando propostas...
    </v-alert>
    
    <v-alert v-else-if="propostas.length === 0" type="info" class="mb-4">
      Não existem propostas pendentes para aprovação.
    </v-alert>
    
    <v-card v-else class="pa-6">
      <v-table>
        <thead>
          <tr>
            <th class="text-left" width="5%">ID</th>
            <th class="text-left" width="20%">Aluno</th>
            <th class="text-left" width="20%">Data de Submissão</th>
            <th class="text-left" width="40%">Membros do Júri</th>
            <th class="text-center" width="15%">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="proposta in propostas" :key="proposta.id">
            <td class="text-left">{{ proposta.id }}</td>
            <td class="text-left">{{ proposta.student.name }}</td>
            <td class="text-left">{{ formatDate(proposta.submissionDate) }}</td>
            <td class="text-left">
              <v-chip-group>
                <v-chip v-for="membro in proposta.juryMembers" :key="membro.id">
                  {{ membro.name }}
                </v-chip>
              </v-chip-group>
            </td>
            <td class="text-center">
              <v-btn 
                color="success" 
                size="small"
                @click="aprovarProposta(proposta.id)"
                :loading="approving === proposta.id"
              >
                Aprovar
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
import { useUserStore } from '@/stores/user'
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

const userStore = useUserStore();
const roleStore = useRoleStore();
const propostas = ref<ThesisProposal[]>([]);
const loading = ref(true);
const approving = ref<number | null>(null);

onMounted(async () => {
  try {
    const response = await RemoteServices.getPendingProposals();
    propostas.value = Array.isArray(response) ? response : response.data;
    console.log('Loaded proposals:', propostas.value);
  } catch (error) {
    console.error('Erro ao carregar propostas:', error);
  } finally {
    loading.value = false;
  }
});

const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('pt-PT');
};

const aprovarProposta = async (id: number) => {
  try {
    // Use user store ID if available, otherwise fall back to role store
    const currentUserId = userStore.user.id || (roleStore.currentPerson?.id || 0);
    
    if (!currentUserId) {
      alert('Erro: Usuário não identificado. Por favor, selecione um usuário SC.');
      return;
    }

    approving.value = id;
    // Pass the current SC user's ID to the backend
    await RemoteServices.approveProposal(id, currentUserId);
    
    // Remove the approved proposal from the list
    propostas.value = propostas.value.filter(p => p.id !== id);
    
    // Show success message
    alert('Proposta aprovada com sucesso!');
  } catch (error) {
    console.error('Erro ao aprovar proposta:', error);
    alert('Erro ao aprovar proposta');
  } finally {
    approving.value = null;
  }
};
</script>