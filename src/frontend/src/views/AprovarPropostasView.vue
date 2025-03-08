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
            <th>ID</th>
            <th>Aluno</th>
            <th>Data de Submissão</th>
            <th>Membros do Júri</th>
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
  state: string;
}

const propostas = ref<ThesisProposal[]>([]);
const loading = ref(true);
const approving = ref<number | null>(null);

onMounted(async () => {
  try {
    const response = await RemoteServices.getPendingProposals();
    // Check if we need to access response.data
    propostas.value = Array.isArray(response) ? response : response.data;
    console.log('Loaded proposals:', propostas.value); // Add logging for debugging
  } catch (error) {
    console.error('Erro ao carregar propostas:', error);
  } finally {
    loading.value = false;
  }
});

const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleString('pt-PT');
};

const aprovarProposta = async (id: number) => {
  try {
    approving.value = id;
    await RemoteServices.approveProposal(id);
    
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