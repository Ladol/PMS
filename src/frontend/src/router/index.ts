import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import PeopleView from '@/views/people/PeopleView.vue'
import StatisticsView from '@/views/statistics/StatisticsView.vue'
import ProporJuriView from '@/views/ProporJuriView.vue'
import AprovarPropostasView from '@/views/AprovarPropostasView.vue'
import AtribuirPresidenteJuriView from '@/views/AtribuirPresidenteJuriView.vue'
import AssinarDocumentoView from '@/views/AssinarDocumentoView.vue'
import { useRoleStore } from '@/stores/role'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/people',
      name: 'people',
      component: PeopleView
    },
    {
      path: '/statistics',
      name: 'statistics',
      component: StatisticsView
    },
    {
      path: '/propor-juri',
      name: 'propor-juri',
      component: ProporJuriView,
      meta: { requiresRole: 'student' }
    },
    {
      path: '/aprovar-propostas',
      name: 'aprovar-propostas',
      component: AprovarPropostasView,
      meta: { requiresRole: 'sc' }
    },
    {
      path: '/atribuir-presidente',
      name: 'atribuir-presidente',
      component: AtribuirPresidenteJuriView
    },
    {
      path:'/assinar-documento',
      name:'assinar-documento',
      component: AssinarDocumentoView
    }
  ]
})

// Add navigation guard to check roles
router.beforeEach((to, from) => {
  if (to.meta.requiresRole) {
    const roleStore = useRoleStore()
    if (to.meta.requiresRole === 'student' && !roleStore.isStudent) {
      return '/'
    }
    if (to.meta.requiresRole === 'sc' && !roleStore.isSC) {
      return '/'
    }
  }
})

export default router
