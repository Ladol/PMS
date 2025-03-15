import axios from 'axios'
import type { AxiosResponse } from 'axios'
import { useAppearanceStore } from '@/stores/appearance'
import DeiError from '@/models/DeiError'
import type PersonDto from '@/models/PersonDto'
import { useRoleStore } from '@/stores/role'

const httpClient = axios.create()
httpClient.defaults.timeout = 50000
httpClient.defaults.baseURL = import.meta.env.VITE_ROOT_API
httpClient.defaults.headers.post['Content-Type'] = 'application/json'

export default class RemoteServices {
  static async getPeople(): Promise<PersonDto[]> {
    return httpClient.get('/people')
  }

  static async createPerson(person: PersonDto): Promise<PersonDto> {
    return httpClient.post('/people', person)
  }
  
  static async deletePerson(id: number): Promise<void> {
    return httpClient.delete(`/people/${id}`)
  }

  static async errorMessage(error: any): Promise<string> {
    if (error.message === 'Network Error') {
      return 'Unable to connect to the server'
    } else if (error.message.split(' ')[0] === 'timeout') {
      return 'Request timeout - Server took too long to respond'
    } else {
      return error.response?.data?.message ?? 'Unknown Error'
    }
  }

  static async handleError(error: any): Promise<never> {
    const deiErr = new DeiError(
      await RemoteServices.errorMessage(error),
      error.response?.data?.code ?? -1
    )
    const appearance = useAppearanceStore()
    appearance.pushError(deiErr)
    appearance.loading = false
    throw deiErr
  }
  
  static async getPeopleByType(type: string): Promise<PersonDto[]> {
    return httpClient.get(`/api/workflows/professors`)
  }

  static async submeterPropostaJuri(professorIds: number[]): Promise<void> {
    const roleStore = useRoleStore()
    const studentId = roleStore.getCurrentPersonId
    
    if (!studentId) {
      throw new Error('Nenhum estudante selecionado')
    }
    
    return httpClient.post(`/api/workflows/proposta-juri`, {
      studentId: studentId,
      professorIds: professorIds
    })
  }
  static async getPendingProposals() {
    return httpClient.get('/api/workflows/proposals/pending')
  }
  static async getApprovedProposals() {
    return httpClient.get('/api/workflows/proposals/approved')
  }
  static async approveProposal(id: number, scId: number) {
    return httpClient.post(`/api/workflows/proposals/${id}/approve`, scId)
  }
  
  static async rejectProposal(id: number, scId: number) {
    return httpClient.post(`/api/workflows/proposals/${id}/reject`, scId)
  }
  static async assignJuryPresident(id: number, coordinatorId: number, presidentId: number) {
    return httpClient.post(`/api/workflows/proposals/${id}/assign-president`, {
      coordinatorId,
      presidentId
    })
  }
  static async signDocument(id: number, coordinatorId: number, documentPath: string) {
    return httpClient.post(`/api/workflows/proposals/${id}/sign-document`, {
      coordinatorId,
      documentPath
    })
  }

  static async getSignedProposals() {
    return httpClient.get('/api/workflows/proposals/signed')
  }

  static async getSubmittedProposals() {
    return httpClient.get('/api/workflows/proposals/submitted')
  }

  static async getProposalsForDefenseScheduling() {
    return httpClient.get('/api/workflows/proposals/for-defense-scheduling')
  }

  static async submitToFenix(id: number, staffId: number) {
    return httpClient.post(`/api/workflows/proposals/${id}/submit-fenix`, {
      staffId
    })
  }
  static async scheduleDefense(id: number, coordinatorId: number, defenseDate: string) {
    return httpClient.post(`/api/workflows/proposals/${id}/schedule-defense`, {
      coordinatorId,
      defenseDate
    })
  }
  
  static async getScheduledDefenses() {
    return httpClient.get('/api/workflows/proposals/scheduled-defenses')
  }
  
  static async updateDefenseState(id: number, defenseState: string) {
    return httpClient.post(`/api/workflows/proposals/${id}/update-defense-state`, {
      defenseState
    })
  }
  
  static async gradeThesis(id: number, coordinatorId: number, grade: number) {
    return httpClient.post(`/api/workflows/proposals/${id}/grade`, {
      coordinatorId,
      grade
    })
  }
  
  static async getProposalsWithPresident() {
    return httpClient.get('/api/workflows/proposals/with-president')
  }
  
  static async updatePerson(person: PersonDto): Promise<PersonDto> {
    return httpClient.put(`/people/${person.id}`, person)
  }
  
  static async getStudents() {
    return httpClient.get('/people/students')
  }
}

httpClient.interceptors.request.use((request) => request, RemoteServices.handleError)
httpClient.interceptors.response.use((response) => response.data, RemoteServices.handleError)
