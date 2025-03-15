<template>
  <div class="pa-4 text-center">
    <v-dialog v-model="dialog" max-width="400">
      <template v-slot:activator="{ props: activatorProps }" v-if="!editMode">
        <v-btn
          class="text-none font-weight-regular"
          prepend-icon="mdi-plus"
          text="Adicionar Pessoa"
          v-bind="activatorProps"
          color="primary"
        ></v-btn>
      </template>

      <v-card prepend-icon="mdi-account" :title="editMode ? 'Editar Pessoa' : 'Nova Pessoa'">
        <v-card-text>
          <v-text-field label="Nome*" required v-model="newPerson.name"></v-text-field>
          <v-text-field label="IST ID*" required v-model="newPerson.istId"></v-text-field>
          <v-text-field 
            label="Email*" 
            required 
            v-model="newPerson.email"
            :rules="[emailRules]"
            hint="example@example.com"
            persistent-hint
          ></v-text-field>

          <v-select
            :items="['Coordenador', 'Staff', 'Aluno', 'Professor', 'SC']"
            label="Categoria*"
            required
            v-model="newPerson.type"
          ></v-select>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>

          <v-btn text="Close" variant="plain" @click="closeDialog">Cancelar</v-btn>

          <v-btn
            color="primary"
            text="Save"
            variant="tonal"
            @click="savePerson"
            :disabled="!isValidForm"
          >
            {{ editMode ? 'Atualizar' : 'Salvar' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type PersonDto from '@/models/people/PersonDto'
import RemoteService from '@/services/RemoteService'

const props = defineProps({
  personToEdit: {
    type: Object as () => PersonDto | null,
    default: null
  }
})

const dialog = ref(false)
const editMode = computed(() => !!props.personToEdit)

const emit = defineEmits(['person-created', 'person-updated'])

const typeMappings = {
  Coordenador: 'COORDINATOR',
  Staff: 'STAFF',
  Aluno: 'STUDENT',
  Professor: 'TEACHER',
  SC: 'SC'
}

const reverseTypeMappings = {
  COORDINATOR: 'Coordenador',
  STAFF: 'Staff',
  STUDENT: 'Aluno',
  TEACHER: 'Professor',
  SC: 'SC'
}

const newPerson = ref<PersonDto>({
  name: '',
  type: '',
  email: ''
})

// Watch for changes in personToEdit and update the form
watch(() => props.personToEdit, (person) => {
  if (person) {
    dialog.value = true
    newPerson.value = { ...person }
    
    // Convert the type to display format
    if (newPerson.value.type) {
      newPerson.value.type = reverseTypeMappings[newPerson.value.type as keyof typeof reverseTypeMappings] || newPerson.value.type
    }
  }
}, { immediate: true })

const emailRules = (value: string) => {
  const pattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return pattern.test(value) || 'Email inválido'
}

const isValidForm = computed(() => {
  return (
    newPerson.value.name && 
    newPerson.value.istId && 
    newPerson.value.type && 
    newPerson.value.email && 
    emailRules(newPerson.value.email) === true
  )
})

const closeDialog = () => {
  dialog.value = false
  if (!editMode.value) {
    resetForm()
  }
}

const resetForm = () => {
  newPerson.value = {
    name: '',
    type: '',
    email: ''
  }
}

const savePerson = async () => {
  if (!isValidForm.value) return

  // Convert display type to backend type
  const personToSave = { ...newPerson.value }
  personToSave.type = typeMappings[personToSave.type as keyof typeof typeMappings] || personToSave.type

  try {
    if (editMode.value && personToSave.id) {
      // Update existing person
      await RemoteService.updatePerson(personToSave)
      emit('person-updated')
    } else {
      // Create new person
      await RemoteService.createPerson(personToSave)
      emit('person-created')
    }
    
    dialog.value = false
    if (!editMode.value) {
      resetForm()
    }
  } catch (error) {
    console.error('Error saving person:', error)
  }
}

// Method to open the dialog from outside
const openDialog = () => {
  dialog.value = true
}

// Expose methods to parent component
defineExpose({
  openDialog
})
</script>
