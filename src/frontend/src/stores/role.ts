import { defineStore } from 'pinia'
import type PersonDto from '@/models/PersonDto'

export const useRoleStore = defineStore('role', {
  state: () => ({
    currentRole: '',
    currentPerson: null as PersonDto | null
  }),
  getters: {
    isStudent(): boolean {
      return this.currentRole === 'student'
    },
    isCoordinator(): boolean {
      return this.currentRole === 'coordinator'
    },
    isStaff(): boolean {
      return this.currentRole === 'staff'
    },
    isAdmin(): boolean {
      return this.currentRole === 'admin'
    },
    isTeacher(): boolean {
      return this.currentRole === 'teacher'
    },
    getCurrentPersonId(): number | null {
      return this.currentPerson && this.currentPerson.id !== undefined 
        ? this.currentPerson.id 
        : null
    }
  },
  actions: {
    setCurrentPerson(person: PersonDto | null) {
      this.currentPerson = person
    }
  }
})