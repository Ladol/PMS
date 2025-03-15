<template>
  <v-row align="center">
    <v-col>
      <h2 class="text-left ml-1">Listagem de Pessoas</h2>
    </v-col>
    <v-col cols="auto">
      <v-btn 
        color="secondary" 
        class="mr-2" 
        prepend-icon="mdi-database-import" 
        @click="createSamplePeople"
        :loading="creatingPeople"
      >
        Criar Pessoas de Exemplo
      </v-btn>
      <CreatePersonDialog @person-created="getPeople" />
    </v-col>
  </v-row>


  <v-text-field
    v-model="search"
    label="Search"
    prepend-inner-icon="mdi-magnify"
    variant="outlined"
    hide-details
    single-line
  ></v-text-field>

  <v-data-table
    :headers="headers"
    :items="people"
    :search="search"
    :loading="loading"
    :custom-filter="fuzzySearch"
    item-key="id"
    class="text-left"
    no-data-text="Sem pessoas a apresentar."
  >
    <template v-slot:[`item.type`]="{ item }">
      <v-chip v-if="item.type === 'COORDINATOR'" color="purple" text-color="white">
        Coordenador
      </v-chip>
      <v-chip v-else-if="item.type === 'STAFF'" color="red" text-color="white">
        Staff
      </v-chip>
      <v-chip v-else-if="item.type === 'TEACHER'" color="blue" text-color="white">
        Professor
      </v-chip>
      <v-chip v-else-if="item.type === 'SC'" color="orange" text-color="white">
        SC
      </v-chip>
      <v-chip v-else color="green" text-color="white">
        Aluno
      </v-chip>
    </template>
    <template v-slot:[`item.actions`]="{ item }">
      <v-icon @click="editPerson(item)" class="mr-2">mdi-pencil</v-icon>
      <v-icon @click="deletePerson(item)" color="error">mdi-delete</v-icon>
    </template>
  </v-data-table>

  <!-- Add confirmation dialog -->
  <v-dialog v-model="deleteDialog" max-width="400">
    <v-card>
      <v-card-title class="text-h5">Confirmar</v-card-title>
      <v-card-text>
        Tem certeza que deseja apagar <strong>{{ personToDelete?.name }}</strong>?
        <div class="text-caption mt-2">Esta ação não pode ser desfeita.</div>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="grey-darken-1" variant="text" @click="deleteDialog = false">Cancelar</v-btn>
        <v-btn 
          color="error" 
          variant="tonal" 
          @click="confirmDelete"
          :loading="deleting"
        >
          Apagar
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <!-- Add edit dialog -->
  <CreatePersonDialog 
    ref="editDialog" 
    :personToEdit="personToEdit" 
    @person-updated="getPeople" 
  />
</template>

<script setup lang="ts">
import type PeopleDto from '@/models/PeopleDto'
import RemoteService from '@/services/RemoteService'
import CreatePersonDialog from './CreatePersonDialog.vue'
import { reactive, ref } from 'vue'

let search = ref('')
let loading = ref(true)
let creatingPeople = ref(false)
let deleteDialog = ref(false)
let deleting = ref(false)
let personToDelete = ref<PeopleDto | null>(null)
let personToEdit = ref<PeopleDto | null>(null)
const editDialog = ref<InstanceType<typeof CreatePersonDialog> | null>(null)

const headers = [
  { title: 'ID', key: 'id', value: 'id', sortable: true, filterable: false },
  {
    title: 'Nome',
    key: 'name',
    value: 'name',
    sortable: true,
    filterable: true
  },
  {
    title: 'IST ID',
    key: 'istId',
    value: 'istId',
    sortable: true,
    filterable: true
  },
  {
    title: 'Email',
    key: 'email',
    value: 'email',
    sortable: true,
    filterable: true
  },
  {
    title: 'Tipo',
    key: 'type',
    value: 'type',
    sortable: true,
    filterable: true
  },
  {
    title: 'Ações',
    key: 'actions',
    value: 'actions',
    sortable: false,
    filterable: false
  }
  // TODO: maybe add another column with possible actions? (edit / delete)
]

const people: PeopleDto[] = reactive([])

getPeople()
async function getPeople() {
  people.splice(0, people.length)
  people.push(...(await RemoteService.getPeople()))
  loading.value = false
  console.log(people)
}

const editPerson = (person: PeopleDto) => {
  personToEdit.value = { ...person }
}

const deletePerson = (person: PeopleDto) => {
  personToDelete.value = person
  deleteDialog.value = true
}

const confirmDelete = async () => {
  if (!personToDelete.value || !personToDelete.value.id) return
  
  deleting.value = true
  try {
    await RemoteService.deletePerson(personToDelete.value.id)
    // Remove the person from the local array
    const index = people.findIndex(p => p.id === personToDelete.value?.id)
    if (index !== -1) {
      people.splice(index, 1)
    }
    // Show success notification (you might want to add a notification system)
    console.log(`Person ${personToDelete.value.name} deleted successfully`)
  } catch (error) {
    console.error(`Failed to delete person:`, error)
    // Show error notification
  } finally {
    deleting.value = false
    deleteDialog.value = false
    personToDelete.value = null
  }
}

const fuzzySearch = (value: string, search: string) => {
  // Regex to match any character in between the search characters
  let searchRegex = new RegExp(search.split('').join('.*'), 'i')
  return searchRegex.test(value)
}

const createSamplePeople = async () => {
  creatingPeople.value = true
  try {
    const samplePeople = [
      // Students
      { istId: 'ist123456', name: 'Alice Johnson', type: 'STUDENT', email: 'alice.johnson@example.com' },
      { istId: 'ist234567', name: 'Bob Smith', type: 'STUDENT', email: 'bob.smith@example.com' },
      
      // Teachers
      { istId: 'ist345678', name: 'Dr. Emily Davis', type: 'TEACHER', email: 'emily.davis@example.com' },
      { istId: 'ist456789', name: 'Prof. Michael Brown', type: 'TEACHER', email: 'michael.brown@example.com' },
      
      // Staff
      { istId: 'ist567890', name: 'Sarah Wilson', type: 'STAFF', email: 'sarah.wilson@example.com' },
      { istId: 'ist678901', name: 'David Martinez', type: 'STAFF', email: 'david.martinez@example.com' },
      
      // SC (Student Committee)
      { istId: 'ist789012', name: 'Laura Garcia', type: 'SC', email: 'laura.garcia@example.com' },
      { istId: 'ist890123', name: 'James Rodriguez', type: 'SC', email: 'james.rodriguez@example.com' },
      
      // Coordinators
      { istId: 'ist901234', name: 'Dr. Olivia Taylor', type: 'COORDINATOR', email: 'olivia.taylor@example.com' },
      { istId: 'ist012345', name: 'Prof. William Anderson', type: 'COORDINATOR', email: 'william.anderson@example.com' }
    ]
    
    // Create each person sequentially
    for (const person of samplePeople) {
      try {
        await RemoteService.createPerson(person)
      } catch (error) {
        console.error(`Failed to create person ${person.name}:`, error)
      }
    }
    
    // Refresh the people list
    await getPeople()
    
  } catch (error) {
    console.error('Error creating sample people:', error)
  } finally {
    creatingPeople.value = false
  }
}
</script>
