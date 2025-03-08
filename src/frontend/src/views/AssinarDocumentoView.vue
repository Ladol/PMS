<template>
  <v-container>
    <h1 class="text-h4 mb-6">Assinar Documento</h1>
    
    <v-alert v-if="loading" type="info" class="mb-4">
      Carregando propostas...
    </v-alert>
    
    <v-alert v-else-if="propostas.length === 0" type="info" class="mb-4">
      Não existem propostas pendentes para assinatura.
    </v-alert>
    
    <v-card v-else class="pa-6">
      <v-table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Aluno</th>
            <th>Presidente do Júri</th>
            <th>Membros do Júri</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="proposta in propostas" :key="proposta.id">
            <td>{{ proposta.id }}</td>
            <td>{{ proposta.student.name }}</td>
            <td>{{ proposta.juryPresident?.name }}</td>
            <td>
              <v-chip-group>
                <v-chip v-for="membro in proposta.juryMembers" :key="membro.id">
                  {{ membro.name }}
                </v-chip>
              </v-chip-group>
            </td>
            <td>
              <v-btn 
                color="info" 
                size="small"
                class="mr-2"
                @click="visualizarDocumento(proposta)"
              >
                <v-icon left>mdi-download</v-icon>
                Visualizar
              </v-btn>
              <v-btn 
                color="success" 
                size="small"
                @click="assinarDocumento(proposta.id)"
                :loading="signing === proposta.id"
              >
                <v-icon left>mdi-upload</v-icon>
                Assinar
              </v-btn>
            </td>
          </tr>
        </tbody>
      </v-table>
    </v-card>

    <!-- PDF Visualization Dialog -->
    <v-dialog v-model="showPdfDialog" max-width="800px">
      <v-card>
        <v-card-title class="text-h5 bg-primary text-white">
          Documento da Proposta
          <v-spacer></v-spacer>
          <v-btn icon @click="showPdfDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-card-title>
        <v-card-text class="pa-6">
          <div class="pdf-container">
            <div class="pdf-header">
              <h2>Instituto Superior Técnico</h2>
              <h3>Departamento de Engenharia Informática</h3>
              <h4>Proposta de Júri para Dissertação</h4>
            </div>
            
            <div class="pdf-content mt-6">
              <p><strong>ID da Proposta:</strong> {{ selectedProposal?.id }}</p>
              <p><strong>Aluno:</strong> {{ selectedProposal?.student?.name }}</p>
              <p><strong>IST ID:</strong> {{ selectedProposal?.student?.istId }}</p>
              <p><strong>Data de Submissão:</strong> {{ formatDate(selectedProposal?.submissionDate) }}</p>
              
              <h4 class="mt-4">Composição do Júri</h4>
              <p><strong>Presidente:</strong> {{ selectedProposal?.juryPresident?.name }}</p>
              
              <h5 class="mt-2">Membros:</h5>
              <ul>
                <li v-for="membro in selectedProposal?.juryMembers" :key="membro.id">
                  {{ membro.name }}
                </li>
              </ul>
              
              <div class="signature-area mt-6">
                <p>Assinatura do Coordenador:</p>
                <div class="signature-line"></div>
                <p class="text-caption">Data: {{ new Date().toLocaleDateString('pt-PT') }}</p>
              </div>
            </div>
          </div>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" @click="showPdfDialog = false">Fechar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import RemoteServices from '@/services/RemoteService'
import { useRoleStore } from '@/stores/role'

interface JuryMember {
  id: number;
  name: string;
  istId?: string;
}

interface Student {
  id: number;
  name: string;
  istId?: string;
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
const signing = ref<number | null>(null);
const showPdfDialog = ref(false);
const selectedProposal = ref<ThesisProposal | null>(null);

onMounted(async () => {
  try {
    const response = await RemoteServices.getProposalsWithPresident();
    propostas.value = Array.isArray(response) ? response : response.data;
    console.log('Loaded proposals with president:', propostas.value);
  } catch (error) {
    console.error('Erro ao carregar propostas com presidente:', error);
  } finally {
    loading.value = false;
  }
});

const formatDate = (dateString: string | undefined) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('pt-PT');
};

const visualizarDocumento = (proposta: ThesisProposal) => {
  selectedProposal.value = proposta;
  showPdfDialog.value = true;
};

const assinarDocumento = async (id: number) => {
  try {
    const currentCoordinatorId = roleStore.currentPerson?.id;
    
    if (!currentCoordinatorId) {
      alert('Erro: Coordenador não identificado. Por favor, selecione um coordenador.');
      return;
    }

    signing.value = id;
    // Simulate document path - in a real app, this would be a path to the uploaded file
    const documentPath = `signed_document_${id}_${Date.now()}.pdf`;
    
    await RemoteServices.signDocument(id, currentCoordinatorId, documentPath);
    
    // Remove the signed proposal from the list
    propostas.value = propostas.value.filter(p => p.id !== id);
    
    // Show success message
    alert('Documento assinado com sucesso!');
  } catch (error) {
    console.error('Erro ao assinar documento:', error);
    alert('Erro ao assinar documento');
  } finally {
    signing.value = null;
  }
};
</script>

<style scoped>
.pdf-container {
  background-color: white;
  padding: 30px;
  font-family: 'Times New Roman', Times, serif;
  color: black;
  border: 1px solid #ddd;
  min-height: 500px;
}

.pdf-header {
  text-align: center;
  margin-bottom: 30px;
  border-bottom: 2px solid #000;
  padding-bottom: 20px;
}

.pdf-content {
  line-height: 1.6;
}

.signature-area {
  margin-top: 60px;
}

.signature-line {
  border-bottom: 1px solid #000;
  width: 250px;
  margin: 10px 0;
}
</style>