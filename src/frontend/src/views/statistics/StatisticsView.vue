<template>
  <v-container>
    <v-row class="mb-6">
      <v-col cols="12">
        <h1 class="text-h4 text-center mb-4">PhD Thesis and Defenses Statistics</h1>
        <p class="text-center text-body-1">Dashboard showing the current status of all PhD students</p>
      </v-col>
    </v-row>

    <!-- Refresh button and last updated info -->
    <v-row class="mb-4">
      <v-col cols="12" class="d-flex justify-end align-center">
        <span class="text-caption mr-4" v-if="lastUpdated">
          Last updated: {{ lastUpdated }}
        </span>
        <v-btn 
          color="primary" 
          variant="outlined" 
          prepend-icon="mdi-refresh" 
          @click="refreshData"
          :loading="loading"
        >
          Refresh Data
        </v-btn>
      </v-col>
    </v-row>

    <v-alert v-if="loading" type="info" class="mb-4">
      Carregando estatísticas...
    </v-alert>

    <v-alert v-else-if="error" type="error" class="mb-4">
      {{ error }}
    </v-alert>

    <template v-else>
      <!-- Summary Cards -->
      <v-row class="mb-6">
        <v-col cols="12" md="4">
          <v-card class="summary-card primary-gradient">
            <v-card-text class="text-center text-white">
              <v-icon size="48" class="mb-2">mdi-account-group</v-icon>
              <h3 class="text-h5 mb-1">Total Students</h3>
              <p class="text-h3 font-weight-bold">{{ totalStudents }}</p>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" md="4">
          <v-card class="summary-card success-gradient">
            <v-card-text class="text-center text-white">
              <v-icon size="48" class="mb-2">mdi-file-document</v-icon>
              <h3 class="text-h5 mb-1">Completed Theses</h3>
              <p class="text-h3 font-weight-bold">{{ completedTheses }}</p>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" md="4">
          <v-card class="summary-card info-gradient">
            <v-card-text class="text-center text-white">
              <v-icon size="48" class="mb-2">mdi-school</v-icon>
              <h3 class="text-h5 mb-1">Completed Defenses</h3>
              <p class="text-h3 font-weight-bold">{{ defenseProgress['Completed'] || 0 }}</p>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Thesis Progress Section -->
      <v-row class="mb-6">
        <v-col cols="12" md="6">
          <v-card elevation="2" class="pa-4">
            <v-card-title class="d-flex align-center">
              <v-icon class="mr-2">mdi-file-document</v-icon>
              Thesis Progress
            </v-card-title>
            <v-card-text>
              <canvas ref="thesisChart"></canvas>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" md="6">
          <v-card elevation="2">
            <v-card-title class="d-flex align-center pa-4">
              <v-icon class="mr-2">mdi-view-list</v-icon>
              Thesis Status Breakdown
            </v-card-title>
            <v-list>
              <v-list-item
                v-for="(count, step) in thesisProgress"
                :key="step"
                :prepend-icon="getThesisIcon(step)"
                :title="step"
                :subtitle="`${count} students`"
              >
                <template v-slot:append>
                  <v-chip
                    :color="getThesisColor(step)"
                    size="small"
                    class="ml-2"
                  >
                    {{ count }}
                  </v-chip>
                </template>
              </v-list-item>
            </v-list>
          </v-card>
        </v-col>
      </v-row>

      <!-- Defense Progress Section -->
      <v-row class="mb-6">
        <v-col cols="12" md="6">
          <v-card elevation="2" class="pa-4">
            <v-card-title class="d-flex align-center">
              <v-icon class="mr-2">mdi-school</v-icon>
              Defense Progress
            </v-card-title>
            <v-card-text>
              <canvas ref="defenseChart"></canvas>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" md="6">
          <v-card elevation="2">
            <v-card-title class="d-flex align-center pa-4">
              <v-icon class="mr-2">mdi-view-list</v-icon>
              Defense Status Breakdown
            </v-card-title>
            <v-list>
              <v-list-item
                v-for="(count, step) in defenseProgress"
                :key="step"
                :prepend-icon="getDefenseIcon(step)"
                :title="step"
                :subtitle="`${count} students`"
              >
                <template v-slot:append>
                  <v-chip
                    :color="getDefenseColor(step)"
                    size="small"
                    class="ml-2"
                  >
                    {{ count }}
                  </v-chip>
                </template>
              </v-list-item>
            </v-list>
          </v-card>
        </v-col>
      </v-row>

      <!-- Timeline Section -->
      <v-row>
        <v-col cols="12">
          <v-card elevation="2">
            <v-card-title class="d-flex align-center pa-4">
              <v-icon class="mr-2">mdi-timeline</v-icon>
              Thesis Workflow Timeline
            </v-card-title>
            <v-card-text>
              <v-timeline align="start" direction="horizontal" line-thickness="2">
                <v-timeline-item
                  v-for="(count, step) in thesisProgress"
                  :key="step"
                  :dot-color="getThesisColor(step)"
                  :icon="getThesisIcon(step)"
                  size="small"
                >
                  <div class="text-center">
                    <div class="text-subtitle-1 font-weight-bold">{{ step }}</div>
                    <div class="text-h6">{{ count }}</div>
                  </div>
                </v-timeline-item>
              </v-timeline>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </template>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import Chart from 'chart.js/auto'
import RemoteServices from '@/services/RemoteService'

// State variables
const thesisProgress = ref({})
const defenseProgress = ref({})
const loading = ref(true)
const error = ref('')
const thesisChart = ref(null)
const defenseChart = ref(null)
const lastUpdated = ref('')
let thesisChartInstance = null
let defenseChartInstance = null

// Computed properties
const totalStudents = computed(() => {
  return Object.values(thesisProgress.value).reduce((sum: number, count: number) => sum + count, 0) + 
         Object.values(defenseProgress.value).reduce((sum: number, count: number) => sum + count, 0)
})

// Add a new computed property for completed theses
const completedTheses = computed(() => {
  // Count students who have submitted to Fenix plus all students in any defense state
  return (thesisProgress.value['Submitted to Fenix'] || 0) + 
         Object.values(defenseProgress.value).reduce((sum: number, count: number) => sum + count, 0)
})

// Format date for last updated
const formatDate = (date: Date) => {
  return new Intl.DateTimeFormat('pt-PT', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

// Refresh data function
const refreshData = async () => {
  await fetchStatistics()
}

// Fetch data from backend
const fetchStatistics = async () => {
  try {
    loading.value = true
    error.value = ''
    
    // Get all students
    const studentsResponse = await RemoteServices.getStudents()
    const students = Array.isArray(studentsResponse) ? studentsResponse : studentsResponse.data
    
    // Process thesis states
    const thesisStates = {
      'Jury Proposal Submitted': 0,
      'Approved by SC': 0,
      'Rejected by SC': 0,
      'Jury President Assigned': 0,
      'Document Signed': 0,
      'Submitted to Fenix': 0
    }
    
    // Process defense states
    const defenseStates = {
      'Scheduled': 0,
      'In Review': 0,
      'Completed': 0
    }
    
    // Count students in each state
    students.forEach(student => {
      if (student.defenseState) {
        switch (student.defenseState) {
          case 'DEFESA_AGENDADA':
            defenseStates['Scheduled']++
            break
          case 'EM_REVISAO':
            defenseStates['In Review']++
            break
          case 'DEFESA_SUBMETIDO_AO_FENIX':
            defenseStates['Completed']++
            break
        }
      } else if (student.thesisState) {
        switch (student.thesisState) {
          case 'PROPOSTA_JURI_SUBMETIDA':
            thesisStates['Jury Proposal Submitted']++
            break
          case 'APROVADO_PELO_SC':
            thesisStates['Approved by SC']++
            break
          case 'REJEITADO_PELO_SC':
            thesisStates['Rejected by SC']++
            break
          case 'PRESIDENTE_JURI_ATRIBUIDO':
            thesisStates['Jury President Assigned']++
            break
          case 'DOCUMENTO_ASSINADO':
            thesisStates['Document Signed']++
            break
          case 'TESE_SUBMETIDO_AO_FENIX':
            thesisStates['Submitted to Fenix']++
            break
        }
      }
    })
    
    thesisProgress.value = thesisStates
    defenseProgress.value = defenseStates
    lastUpdated.value = formatDate(new Date())
    
    loading.value = false
  } catch (err) {
    console.error('Error fetching statistics:', err)
    error.value = 'Erro ao carregar estatísticas. Por favor, tente novamente.'
    loading.value = false
  }
}

// Initialize charts
const initCharts = () => {
  if (thesisChartInstance) {
    thesisChartInstance.destroy()
  }
  if (defenseChartInstance) {
    defenseChartInstance.destroy()
  }
  
  // Thesis chart
  const thesisCtx = thesisChart.value.getContext('2d')
  thesisChartInstance = new Chart(thesisCtx, {
    type: 'doughnut',
    data: {
      labels: Object.keys(thesisProgress.value),
      datasets: [{
        data: Object.values(thesisProgress.value),
        backgroundColor: [
          '#42A5F5', // Blue
          '#66BB6A', // Green
          '#EF5350', // Red
          '#AB47BC', // Purple
          '#7E57C2', // Deep Purple
          '#26A69A'  // Teal
        ],
        borderWidth: 1
      }]
    },
    options: {
      responsive: true,
      plugins: {
        legend: {
          position: 'right'
        }
      }
    }
  })
  
  // Defense chart
  const defenseCtx = defenseChart.value.getContext('2d')
  defenseChartInstance = new Chart(defenseCtx, {
    type: 'pie',
    data: {
      labels: Object.keys(defenseProgress.value),
      datasets: [{
        data: Object.values(defenseProgress.value),
        backgroundColor: [
          '#FF7043', // Deep Orange
          '#FFCA28', // Amber
          '#26C6DA'  // Cyan
        ],
        borderWidth: 1
      }]
    },
    options: {
      responsive: true,
      plugins: {
        legend: {
          position: 'right'
        }
      }
    }
  })
}

// Helper functions for icons and colors
const getThesisIcon = (step) => {
  switch (step) {
    case 'Jury Proposal Submitted': return 'mdi-file-document-plus'
    case 'Approved by SC': return 'mdi-check-circle'
    case 'Rejected by SC': return 'mdi-close-circle'
    case 'Jury President Assigned': return 'mdi-account-tie'
    case 'Document Signed': return 'mdi-file-sign'
    case 'Submitted to Fenix': return 'mdi-cloud-upload'
    default: return 'mdi-help-circle'
  }
}

const getThesisColor = (step) => {
  switch (step) {
    case 'Jury Proposal Submitted': return 'blue'
    case 'Approved by SC': return 'green'
    case 'Rejected by SC': return 'red'
    case 'Jury President Assigned': return 'purple'
    case 'Document Signed': return 'deep-purple'
    case 'Submitted to Fenix': return 'teal'
    default: return 'grey'
  }
}

const getDefenseIcon = (step) => {
  switch (step) {
    case 'Scheduled': return 'mdi-calendar-clock'
    case 'In Review': return 'mdi-clipboard-text'
    case 'Completed': return 'mdi-school'
    default: return 'mdi-help-circle'
  }
}

const getDefenseColor = (step) => {
  switch (step) {
    case 'Scheduled': return 'deep-orange'
    case 'In Review': return 'amber'
    case 'Completed': return 'cyan'
    default: return 'grey'
  }
}

// Watch for changes in data to update charts
watch([thesisProgress, defenseProgress], () => {
  if (!loading.value && !error.value) {
    // Use nextTick to ensure DOM is updated
    setTimeout(() => {
      if (thesisChart.value && defenseChart.value) {
        initCharts()
      }
    }, 100)
  }
}, { deep: true })

// Lifecycle hooks
onMounted(async () => {
  await fetchStatistics()
  // Initialize charts after data is loaded
  if (!loading.value && !error.value) {
    setTimeout(initCharts, 100)
  }
})
</script>

<style scoped>
.stat-card {
  transition: transform 0.2s ease-in-out;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.summary-card {
  border-radius: 12px;
  transition: transform 0.2s ease-in-out;
  height: 100%;
}

.summary-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.primary-gradient {
  background: linear-gradient(135deg, #1976D2 0%, #64B5F6 100%);
}

.success-gradient {
  background: linear-gradient(135deg, #388E3C 0%, #81C784 100%);
}

.info-gradient {
  background: linear-gradient(135deg, #0097A7 0%, #4DD0E1 100%);
}

/* Add responsive styling for timeline */
.v-timeline {
  overflow-x: auto;
  padding-bottom: 16px;
}

/* Add card hover effects */
.v-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.v-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

/* Improve list item styling */
.v-list-item {
  border-left: 3px solid transparent;
  transition: background-color 0.2s ease;
}

.v-list-item:hover {
  background-color: rgba(0, 0, 0, 0.03);
}

/* Style for each thesis state in list */
.v-list-item[data-state="Jury Proposal Submitted"] {
  border-left-color: #42A5F5;
}

.v-list-item[data-state="Approved by SC"] {
  border-left-color: #66BB6A;
}

.v-list-item[data-state="Rejected by SC"] {
  border-left-color: #EF5350;
}

.v-list-item[data-state="Jury President Assigned"] {
  border-left-color: #AB47BC;
}

.v-list-item[data-state="Document Signed"] {
  border-left-color: #7E57C2;
}

.v-list-item[data-state="Submitted to Fenix"] {
  border-left-color: #26A69A;
}

/* Style for each defense state in list */
.v-list-item[data-state="Scheduled"] {
  border-left-color: #FF7043;
}

.v-list-item[data-state="In Review"] {
  border-left-color: #FFCA28;
}

.v-list-item[data-state="Completed"] {
  border-left-color: #26C6DA;
}
</style>