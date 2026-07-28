import { createRouter, createWebHistory } from 'vue-router'
import PaymentView from '../views/PaymentView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'payment',
      component: PaymentView,
    },
  ],
})

export default router
