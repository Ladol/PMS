<template>
  <v-container>
    <h1 class="text-h4 mb-6">Agendar Defesa de Tese</h1>
    
    <v-alert v-if="loading" type="info" class="mb-4">
      Carregando teses...
    </v-alert>
    
    <v-alert v-else-if="propostas.length === 0" type="info" class="mb-4">
      Não existem teses pendentes para agendamento de defesa.
    </v-alert>
    
    <v-card v-else class="pa-6">
      <v-table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Aluno</th>
            <th>Presidente do Júri</th>
            <th>Data de Submissão ao Fenix</th>
            <th>Data de Defesa</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="proposta in propostas" :key="proposta.id">
            <td>{{ proposta.id }}</td>
            <td>{{ proposta.student.name }}</td>
            <td>{{ proposta.juryPresident?.name }}</td>
            <td>{{ formatDate(proposta.fenixSubmissionDate) }}</td>
            <td>
              <v-text-field
                type="date"
                v-model="selectedDates[proposta.id]"
                label="Data de Defesa"
                density="compact"
                style="width: 200px;"
              ></v-text-field>
            </td>
            <td>
              <v-btn 
                color="primary" 
                size="small"
                @click="agendarDefesa(proposta.id)"
                :loading="scheduling === proposta.id"
                :disabled="!selectedDates[proposta.id]"
              >
                <v-icon start>mdi-calendar-check</v-icon>
                Agendar
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
          Defesa agendada com sucesso!
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
const scheduling = ref<number | null>(null);
const selectedDates = reactive<Record<number, string>>({});
const successDialog = ref(false);
const errorDialog = ref(false);
const errorMessage = ref('');

// Initialize the date selections
onMounted(async () => {
  try {
    const response = await RemoteServices.getSubmittedProposals();
    
    // Filter proposals that match our criteria
    let filteredProposals;
    if (Array.isArray(response)) {
      filteredProposals = response.filter((p: ThesisProposal) => 
        p.thesisState === 'TESE_SUBMETIDO_AO_FENIX' && 
        (p.defenseState === null || p.defenseState === '')
      );
    } else if (response.data) {
      filteredProposals = response.data.filter((p: ThesisProposal) => 
        p.thesisState === 'TESE_SUBMETIDO_AO_FENIX' && 
        (p.defenseState === null || p.defenseState === '')
      );
    } else {
      filteredProposals = [];
    }
    
    propostas.value = filteredProposals;
    
    // Initialize date selections with today's date in YYYY-MM-DD format
    const today = new Date();
    const formattedDate = today.toISOString().split('T')[0]; // Format as YYYY-MM-DD
    
    propostas.value.forEach(proposta => {
      selectedDates[proposta.id] = formattedDate;
    });
  } catch (error) {
    console.error('Erro ao carregar propostas para agendamento:', error);
    propostas.value = [];
  } finally {
    loading.value = false;
  }
});

const formatDate = (dateString: string | undefined | null) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('pt-PT');
};

const agendarDefesa = async (id: number) => {
  try {
    const currentCoordinatorId = roleStore.currentPerson?.id;
    
    if (!currentCoordinatorId) {
      errorMessage.value = 'Erro: Coordenador não identificado. Por favor, selecione um coordenador.';
      errorDialog.value = true;
      return;
    }

    if (!selectedDates[id]) {
      errorMessage.value = 'Por favor, selecione uma data para a defesa.';
      errorDialog.value = true;
      return;
    }

    scheduling.value = id;
    
    // Convert the date string to a datetime string (noon on the selected day)
    const selectedDate = new Date(selectedDates[id] + 'T12:00:00');
    
    await RemoteServices.scheduleDefense(id, currentCoordinatorId, selectedDate.toISOString());
    
    // Log the defense scheduling action
    try {
      const proposal = propostas.value.find(p => p.id === id);
      if (proposal) {
        await RemoteServices.logAction({
          action: 'SCHEDULED_DEFENSE',
          person: roleStore.currentPerson?.name || 'Unknown Coordinator',
          details: `Scheduled defense for student: ${proposal.student.name}, Date: ${selectedDates[id]}`
        });
      }
    } catch (logError) {
      console.error('Failed to log defense scheduling action:', logError);
    }
    
    // Remove the scheduled proposal from the list
    propostas.value = propostas.value.filter(p => p.id !== id);
    
    // Show success dialog
    successDialog.value = true;
  } catch (error) {
    console.error('Erro ao agendar defesa:', error);
    errorMessage.value = 'Erro ao agendar defesa';
    errorDialog.value = true;
  } finally {
    scheduling.value = null;
  }
};
</script>