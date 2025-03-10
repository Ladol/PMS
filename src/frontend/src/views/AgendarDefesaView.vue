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
              <div class="d-flex">
                <!-- Day dropdown -->
                <v-select
                  v-model="selectedDays[proposta.id]"
                  :items="days"
                  label="Dia"
                  density="compact"
                  class="mr-2"
                  style="width: 80px;"
                  @update:model-value="updateSelectedDate(proposta.id)"
                ></v-select>
                
                <!-- Month dropdown -->
                <v-select
                  v-model="selectedMonths[proposta.id]"
                  :items="months"
                  label="Mês"
                  density="compact"
                  class="mr-2"
                  style="width: 120px;"
                  @update:model-value="updateSelectedDate(proposta.id)"
                ></v-select>
                
                <!-- Year dropdown -->
                <v-select
                  v-model="selectedYears[proposta.id]"
                  :items="years"
                  label="Ano"
                  density="compact"
                  style="width: 100px;"
                  @update:model-value="updateSelectedDate(proposta.id)"
                ></v-select>
              </div>
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
const dateMenus = reactive<Record<number, boolean>>({});
const selectedDays = reactive<Record<number, number>>({});
const selectedMonths = reactive<Record<number, number>>({});
const selectedYears = reactive<Record<number, number>>({});

// Generate arrays for days, months, and years
const days = Array.from({ length: 31 }, (_, i) => i + 1);
const months = Array.from({ length: 12 }, (_, i) => i + 1);
const currentYear = new Date().getFullYear();
const years = Array.from({ length: 5 }, (_, i) => currentYear + i);

// Initialize the date selections
onMounted(async () => {
  try {
    const response = await RemoteServices.getSubmittedProposals();
    
    // Filter proposals that match our criteria
    let filteredProposals;
    if (Array.isArray(response)) {
      filteredProposals = response.filter((p: ThesisProposal) => 
        p.thesisState === 'SUBMETIDO_AO_FENIX' && 
        (p.defenseState === null || p.defenseState === '')
      );
    } else if (response.data) {
      filteredProposals = response.data.filter((p: ThesisProposal) => 
        p.thesisState === 'SUBMETIDO_AO_FENIX' && 
        (p.defenseState === null || p.defenseState === '')
      );
    } else {
      filteredProposals = [];
    }
    
    propostas.value = filteredProposals;
    console.log('Loaded proposals for defense scheduling:', propostas.value);
    
    // Initialize date selections
    propostas.value.forEach(proposta => {
      selectedDays[proposta.id] = new Date().getDate();
      selectedMonths[proposta.id] = new Date().getMonth() + 1;
      selectedYears[proposta.id] = currentYear;
      dateMenus[proposta.id] = false;
      selectedDates[proposta.id] = '';
      updateSelectedDate(proposta.id);
    });
  } catch (error) {
    console.error('Erro ao carregar propostas para agendamento:', error);
    propostas.value = [];
  } finally {
    loading.value = false;
  }
});

// Update the selected date string when day, month, or year changes
const updateSelectedDate = (id: number) => {
  const day = selectedDays[id];
  const month = selectedMonths[id];
  const year = selectedYears[id];
  
  if (day && month && year) {
    // Format as YYYY-MM-DD for ISO format
    const monthStr = month.toString().padStart(2, '0');
    const dayStr = day.toString().padStart(2, '0');
    selectedDates[id] = `${year}-${monthStr}-${dayStr}`;
  }
};

const formatDate = (dateString: string | undefined | null) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('pt-PT');
};

const agendarDefesa = async (id: number) => {
  try {
    const currentCoordinatorId = roleStore.currentPerson?.id;
    
    if (!currentCoordinatorId) {
      alert('Erro: Coordenador não identificado. Por favor, selecione um coordenador.');
      return;
    }

    if (!selectedDates[id]) {
      alert('Por favor, selecione uma data para a defesa.');
      return;
    }

    scheduling.value = id;
    
    // Convert the date string to a datetime string (noon on the selected day)
    const selectedDate = new Date(selectedDates[id] + 'T12:00:00');
    
    await RemoteServices.scheduleDefense(id, currentCoordinatorId, selectedDate.toISOString());
    
    // Remove the scheduled proposal from the list
    propostas.value = propostas.value.filter(p => p.id !== id);
    
    // Show success message
    alert('Defesa agendada com sucesso!');
  } catch (error) {
    console.error('Erro ao agendar defesa:', error);
    alert('Erro ao agendar defesa');
  } finally {
    scheduling.value = null;
  }
};
</script>