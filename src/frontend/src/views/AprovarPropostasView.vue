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
            <th class="text-left" width="35%">Membros do Júri</th>
            <th class="text-center" width="20%">Ações</th>
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
              <div class="d-flex justify-center">
                <v-btn 
                  color="success" 
                  size="small"
                  @click="aprovarProposta(proposta.id)"
                  :loading="approving === proposta.id"
                  class="mr-2"
                >
                  Aprovar
                </v-btn>
                <v-btn 
                  color="error" 
                  size="small"
                  @click="rejeitarProposta(proposta.id)"
                  :loading="rejecting === proposta.id"
                >
                  Rejeitar
                </v-btn>
              </div>
            </td>
          </tr>
        </tbody>
      </v-table>
    </v-card>
    
    <!-- Success Dialog -->
    <v-dialog v-model="successDialog" max-width="400">
      <v-card>
        <v-card-title class="text-h5 bg-success text-white">
          Sucesso
        </v-card-title>
        <v-card-text class="pt-4">
          {{ successMessage }}
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
const rejecting = ref<number | null>(null);
const successDialog = ref(false);
const errorDialog = ref(false);
const successMessage = ref('');
const errorMessage = ref('');

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
      errorMessage.value = 'Erro: Usuário não identificado. Por favor, selecione um usuário SC.';
      errorDialog.value = true;
      return;
    }

    approving.value = id;
    // Pass the current SC user's ID to the backend
    await RemoteServices.approveProposal(id, currentUserId);
    
    // Remove the approved proposal from the list
    propostas.value = propostas.value.filter(p => p.id !== id);
    
    // Show success message
    successMessage.value = 'Proposta aprovada com sucesso!';
    successDialog.value = true;
  } catch (error) {
    console.error('Erro ao aprovar proposta:', error);
    errorMessage.value = 'Erro ao aprovar proposta';
    errorDialog.value = true;
  } finally {
    approving.value = null;
  }
};

const rejeitarProposta = async (id: number) => {
  try {
    // Use user store ID if available, otherwise fall back to role store
    const currentUserId = userStore.user.id || (roleStore.currentPerson?.id || 0);
    
    if (!currentUserId) {
      errorMessage.value = 'Erro: Usuário não identificado. Por favor, selecione um usuário SC.';
      errorDialog.value = true;
      return;
    }

    rejecting.value = id;
    // Pass the current SC user's ID to the backend
    await RemoteServices.rejectProposal(id, currentUserId);
    
    // Remove the rejected proposal from the list
    propostas.value = propostas.value.filter(p => p.id !== id);
    
    // Show success message
    successMessage.value = 'Proposta rejeitada com sucesso!';
    successDialog.value = true;
  } catch (error) {
    console.error('Erro ao rejeitar proposta:', error);
    errorMessage.value = 'Erro ao rejeitar proposta';
    errorDialog.value = true;
  } finally {
    rejecting.value = null;
  }
};
</script>