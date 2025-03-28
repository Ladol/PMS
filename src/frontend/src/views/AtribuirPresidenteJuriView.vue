<template>
  <v-container>
    <h1 class="text-h4 mb-6">Atribuir Presidente de Júri</h1>
    
    <v-alert v-if="loading" type="info" class="mb-4">
      Carregando propostas...
    </v-alert>
    
    <v-alert v-else-if="propostas.length === 0" type="info" class="mb-4">
      Não existem propostas aprovadas pelo SC pendentes de atribuição de presidente.
    </v-alert>
    
    <v-card v-else class="pa-6">
      <v-table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Aluno</th>
            <th>Data de Submissão</th>
            <th>Membros do Júri</th>
            <th>Presidente</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="proposta in propostas" :key="proposta.id">
            <td>{{ proposta.id }}</td>
            <td>{{ proposta.student.name }}</td>
            <td>{{ formatDate(proposta.submissionDate) }}</td>
            <td>
              <v-chip-group>
                <v-chip v-for="membro in proposta.juryMembers" :key="membro.id">
                  {{ membro.name }}
                </v-chip>
              </v-chip-group>
            </td>
            <td>
              <v-select
                v-model="selectedPresidents[proposta.id]"
                :items="proposta.juryMembers"
                item-title="name"
                item-value="id"
                label="Selecione o presidente"
                return-object
                variant="outlined"
                density="compact"
              ></v-select>
            </td>
            <td>
              <v-btn 
                color="primary" 
                size="small"
                @click="atribuirPresidente(proposta.id)"
                :loading="assigning === proposta.id"
                :disabled="!selectedPresidents[proposta.id]"
              >
                Atribuir
              </v-btn>
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
          Presidente de júri atribuído com sucesso!
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" variant="text" @click="successDialog = false">
            Fechar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
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
const assigning = ref<number | null>(null);
const selectedPresidents = reactive<Record<number, JuryMember>>({});
const successDialog = ref(false);

onMounted(async () => {
  try {
    const response = await RemoteServices.getApprovedProposals();
    propostas.value = Array.isArray(response) ? response : response.data;
    console.log('Loaded approved proposals:', propostas.value);
  } catch (error) {
    console.error('Erro ao carregar propostas aprovadas:', error);
  } finally {
    loading.value = false;
  }
});

const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('pt-PT');
};

const atribuirPresidente = async (id: number) => {
  try {
    const currentCoordinatorId = roleStore.currentPerson?.id;
    
    if (!currentCoordinatorId) {
      alert('Erro: Coordenador não identificado. Por favor, selecione um coordenador.');
      return;
    }

    const selectedPresident = selectedPresidents[id];
    if (!selectedPresident) {
      alert('Por favor, selecione um presidente para o júri.');
      return;
    }

    assigning.value = id;
    await RemoteServices.assignJuryPresident(id, currentCoordinatorId, selectedPresident.id);
    
    // Log the president assignment action
    try {
      const proposal = propostas.value.find(p => p.id === id);
      if (proposal) {
        await RemoteServices.logAction({
          action: 'ASSIGNED_PRESIDENT',
          person: roleStore.currentPerson?.name || 'Unknown Coordinator',
          details: `Assigned ${selectedPresident.name} as jury president for student: ${proposal.student.name}`
        });
      }
    } catch (logError) {
      console.error('Failed to log president assignment action:', logError);
    }
    
    // Remove the assigned proposal from the list
    propostas.value = propostas.value.filter(p => p.id !== id);
    
    // Show success dialog
    successDialog.value = true;
  } catch (error) {
    console.error('Erro ao atribuir presidente de júri:', error);
    alert('Erro ao atribuir presidente de júri');
  } finally {
    assigning.value = null;
  }
};
</script>