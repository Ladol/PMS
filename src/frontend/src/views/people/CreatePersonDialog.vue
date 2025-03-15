<template>
  <div class="pa-4 text-center">
    <v-dialog v-model="dialog" max-width="400">
      <template v-slot:activator="{ props: activatorProps }">
        <v-btn
          class="text-none font-weight-regular"
          prepend-icon="mdi-plus"
          text="Adicionar Pessoa"
          v-bind="activatorProps"
          color="primary"
        ></v-btn>
      </template>

      <v-card prepend-icon="mdi-account" title="Nova">
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

          <v-btn text="Close" variant="plain" @click="dialog = false"></v-btn>

          <v-btn
            color="primary"
            text="Save"
            variant="tonal"
            @click="savePerson"
            :disabled="!isValidForm"
          ></v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type PersonDto from '@/models/people/PersonDto'
import RemoteService from '@/services/RemoteService'

const dialog = ref(false)

const emit = defineEmits(['person-created'])

const typeMappings = {
  Coordenador: 'COORDINATOR',
  Staff: 'STAFF',
  Aluno: 'STUDENT',
  Professor: 'TEACHER',
  SC: 'SC'
}

const newPerson = ref<PersonDto>({
  name: '',
  type: '',
  email: ''
})

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

const savePerson = async () => {
  if (!isValidForm.value) return

  newPerson.value.type = typeMappings[newPerson.value.type as keyof typeof typeMappings]
  await RemoteService.createPerson(newPerson.value)
  newPerson.value = {
    name: '',
    type: '',
    email: ''
  }
  dialog.value = false
  emit('person-created')
}
</script>
