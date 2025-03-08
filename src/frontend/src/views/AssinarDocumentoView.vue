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
                <v-icon left>mdi-eye</v-icon>
                Visualizar
              </v-btn>
              <v-btn 
                color="primary" 
                size="small"
                class="mr-2"
                @click="downloadPdf(proposta)"
              >
                <v-icon left>mdi-download</v-icon>
                Download
              </v-btn>
              <v-btn 
                color="success" 
                size="small"
                @click="openUploadDialog(proposta)"
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

    <!-- Upload Dialog -->
    <v-dialog v-model="showUploadDialog" max-width="500px">
      <v-card>
        <v-card-title class="text-h5 bg-primary text-white">
          Submeter Documento Assinado
          <v-spacer></v-spacer>
        </v-card-title>
        <v-card-text class="pa-6">
          <p class="mb-4">Por favor, faça upload do documento assinado:</p>
          
          <v-file-input
            v-model="uploadedFile"
            accept=".pdf"
            label="Documento Assinado"
            prepend-icon="mdi-file-pdf-box"
            show-size
            :rules="[v => !!v || 'É necessário fazer upload do documento assinado']"
            counter
          ></v-file-input>
          
          <v-alert v-if="uploadError" type="error" class="mt-4">
            {{ uploadError }}
          </v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="error" @click="showUploadDialog = false">Cancelar</v-btn>
          <v-btn 
            color="success" 
            @click="assinarDocumento()"
            :loading="signing !== null"
            :disabled="!uploadedFile"
          >
            Submeter
          </v-btn>
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
const showUploadDialog = ref(false);
const selectedProposal = ref<ThesisProposal | null>(null);
const uploadedFile = ref<File | null>(null);
const uploadError = ref<string | null>(null);

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

const openUploadDialog = (proposta: ThesisProposal) => {
  selectedProposal.value = proposta;
  uploadedFile.value = null;
  uploadError.value = null;
  showUploadDialog.value = true;
};

const downloadPdf = (proposta: ThesisProposal) => {
  // Create PDF content TODO: it doesn't work, i don't know why
  const pdfContent = generatePdfContent(proposta);
  
  // Create a Blob with the PDF content
  const blob = new Blob([pdfContent], { type: 'application/pdf' });
  
  // Create a URL for the Blob
  const url = URL.createObjectURL(blob);
  
  // Create a link element
  const link = document.createElement('a');
  link.href = url;
  link.download = `proposta_juri_${proposta.id}.pdf`;
  
  // Append the link to the body
  document.body.appendChild(link);
  
  // Click the link to trigger the download
  link.click();
  
  // Remove the link from the body
  document.body.removeChild(link);
  
  // Release the URL object
  URL.revokeObjectURL(url);
};

const generatePdfContent = (proposta: ThesisProposal): string => {
  // In a real application, you would use a library like jsPDF to generate a real PDF
  // For this project, we'll just return a string representation
  return `
    Instituto Superior Técnico
    Departamento de Engenharia Informática
    Proposta de Júri para Dissertação
    
    ID da Proposta: ${proposta.id}
    Aluno: ${proposta.student.name}
    IST ID: ${proposta.student.istId || 'N/A'}
    Data de Submissão: ${formatDate(proposta.submissionDate)}
    
    Composição do Júri
    Presidente: ${proposta.juryPresident?.name || 'N/A'}
    
    Membros:
    ${proposta.juryMembers.map(membro => `- ${membro.name}`).join('\n')}
    
    Assinatura do Coordenador:
    
    Data: ${new Date().toLocaleDateString('pt-PT')}
  `;
};

const assinarDocumento = async () => {
  if (!selectedProposal.value) {
    uploadError.value = 'Nenhuma proposta selecionada.';
    return;
  }
  
  if (!uploadedFile.value) {
    uploadError.value = 'É necessário fazer upload do documento assinado.';
    return;
  }
  
  const currentCoordinatorId = roleStore.currentPerson?.id;
  
  if (!currentCoordinatorId) {
    uploadError.value = 'Coordenador não identificado. Por favor, selecione um coordenador.';
    return;
  }

  try {
    signing.value = selectedProposal.value.id;
    
    // Get the file name
    const fileName = uploadedFile.value.name;
    
    // In a real application, you would upload the file to a server
    // For this simulation, we'll just use the file name as the document path
    const documentPath = `signed_documents/${fileName}`;
    
    await RemoteServices.signDocument(selectedProposal.value.id, currentCoordinatorId, documentPath);
    
    // Remove the signed proposal from the list
    propostas.value = propostas.value.filter(p => p.id !== selectedProposal.value?.id);
    
    // Close the upload dialog
    showUploadDialog.value = false;
    
    // Show success message
    alert('Documento assinado com sucesso!');
  } catch (error) {
    console.error('Erro ao assinar documento:', error);
    uploadError.value = 'Erro ao assinar documento. Por favor, tente novamente.';
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