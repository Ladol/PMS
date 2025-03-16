<template>
  <v-container>
    <h1 class="text-h4 mb-6">System Activity Logs</h1>
    
    <v-card>
      <v-card-title class="d-flex align-center">
        <v-icon class="mr-2">mdi-clipboard-text-clock</v-icon>
        System Actions
        <v-spacer></v-spacer>
        <v-btn
          color="primary"
          variant="outlined"
          prepend-icon="mdi-refresh"
          @click="fetchLogs"
          :loading="loading"
        >
          Refresh
        </v-btn>
      </v-card-title>
      
      <v-card-text>
        <v-alert v-if="loading" type="info" class="mb-4">
          Loading logs...
        </v-alert>
        
        <v-alert v-else-if="error" type="error" class="mb-4">
          {{ error }}
        </v-alert>
        
        <v-alert v-else-if="logs.length === 0" type="info" class="mb-4">
          No logs found.
        </v-alert>
        
        <template v-else>
          <!-- Filters -->
          <v-row class="mb-4">
            <v-col cols="12" md="4">
              <v-select
                v-model="actionFilter"
                :items="actionTypes"
                label="Filter by Action"
                clearable
                prepend-icon="mdi-filter-variant"
              ></v-select>
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model="personFilter"
                label="Filter by Person"
                clearable
                prepend-icon="mdi-account-search"
              ></v-text-field>
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                type="date"
                v-model="dateFilter"
                label="Filter by Date"
                clearable
                prepend-icon="mdi-calendar"
              ></v-text-field>
            </v-col>
          </v-row>
          
          <!-- Logs Table -->
          <v-table>
            <thead>
              <tr>
                <th>Date</th>
                <th>Action</th>
                <th>Person</th>
                <th>Details</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(log, index) in paginatedLogs" :key="index" :class="getLogClass(log.action)">
                <td>{{ formatDate(log.timestamp) }}</td>
                <td>{{ log.action }}</td>
                <td>{{ log.person }}</td>
                <td>{{ log.details }}</td>
              </tr>
            </tbody>
          </v-table>
          
          <!-- Pagination -->
          <div class="d-flex justify-center mt-4">
            <v-pagination
              v-model="page"
              :length="Math.ceil(filteredLogs.length / itemsPerPage)"
              :total-visible="7"
            ></v-pagination>
          </div>
        </template>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import RemoteServices from '@/services/RemoteService'

interface Log {
  id: number
  timestamp: string
  action: string
  person: string
  details: string
}

// State variables
const logs = ref<Log[]>([])
const loading = ref(true)
const error = ref('')
const page = ref(1)
const itemsPerPage = 10
const actionFilter = ref('')
const personFilter = ref('')
const dateFilter = ref('')

// Action types for filter dropdown
const actionTypes = [
  'PROPOSED_JURY',
  'APPROVED_JURY',
  'REJECTED_JURY',
  'ASSIGNED_PRESIDENT',
  'SIGNED_DOCUMENT',
  'SUBMITTED_TO_FENIX',
  'SCHEDULED_DEFENSE'
]

// Format date for display
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('pt-PT', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

// Get CSS class based on action type
const getLogClass = (action: string) => {
  switch (action) {
    case 'PROPOSED_JURY':
      return 'log-proposed'
    case 'APPROVED_JURY':
      return 'log-approved'
    case 'REJECTED_JURY':
      return 'log-rejected'
    case 'ASSIGNED_PRESIDENT':
      return 'log-assigned'
    case 'SIGNED_DOCUMENT':
      return 'log-signed'
    case 'SUBMITTED_TO_FENIX':
      return 'log-submitted'
    case 'SCHEDULED_DEFENSE':
      return 'log-scheduled'
    default:
      return ''
  }
}

// Filter logs based on selected filters
const filteredLogs = computed(() => {
  let filtered = [...logs.value]
  
  if (actionFilter.value) {
    filtered = filtered.filter(log => log.action === actionFilter.value)
  }
  
  if (personFilter.value) {
    const searchTerm = personFilter.value.toLowerCase()
    filtered = filtered.filter(log => log.person.toLowerCase().includes(searchTerm))
  }
  
  if (dateFilter.value) {
    const filterDate = new Date(dateFilter.value).toISOString().split('T')[0]
    filtered = filtered.filter(log => {
      const logDate = new Date(log.timestamp).toISOString().split('T')[0]
      return logDate === filterDate
    })
  }
  
  return filtered
})

// Paginated logs
const paginatedLogs = computed(() => {
  const start = (page.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return filteredLogs.value.slice(start, end)
})

// Make sure onMounted is calling fetchLogs
onMounted(() => {
  fetchLogs()
})

// Fetch logs from backend
const fetchLogs = async () => {
  try {
    loading.value = true
    error.value = ''
    
    const response = await RemoteServices.getLogs()
    logs.value = response || []
    
    loading.value = false
  } catch (err) {
    console.error('Error fetching logs:', err)
    error.value = 'Failed to load logs. Please try again.'
    loading.value = false
  }
}
</script>

<style scoped>
.log-proposed {
  background-color: rgba(33, 150, 243, 0.1);
}

.log-approved {
  background-color: rgba(76, 175, 80, 0.1);
}

.log-rejected {
  background-color: rgba(244, 67, 54, 0.1);
}

.log-assigned {
  background-color: rgba(156, 39, 176, 0.1);
}

.log-signed {
  background-color: rgba(121, 85, 72, 0.1);
}

.log-submitted {
  background-color: rgba(0, 188, 212, 0.1);
}

.log-scheduled {
  background-color: rgba(255, 193, 7, 0.1);
}

.v-table {
  border-radius: 8px;
  overflow: hidden;
}

.v-table th {
  background-color: #f5f5f5;
  font-weight: bold;
}

.v-table tr {
  cursor: pointer;
  transition: background-color 0.2s;
}

.v-table tr:hover {
  background-color: rgba(0, 0, 0, 0.05);
}
</style>