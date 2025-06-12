package com.example.gym4house // Asegúrate de que este sea tu paquete correcto

import androidx.fragment.app.Fragment // Asegúrate de que esta importación exista
import androidx.viewpager2.adapter.FragmentStateAdapter

// EL CAMBIO ESTÁ AQUÍ: Ahora el constructor espera un 'fragment: Fragment'
class RoutinePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        // Tenemos 2 pestañas: Rutina Actual e Historial
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CurrentRoutineFragment() // Primera pestaña: Rutina Actual
            1 -> RoutineHistoryFragment()  // Segunda pestaña: Historial
            else -> throw IllegalArgumentException("Posición de fragmento inválida")
        }
    }
}