package com.example.gym4house

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class RutinasFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos el XML que acabamos de corregir (el que tiene tabs)
        val view = inflater.inflate(R.layout.fragment_rutinas, container, false)

        // Buscamos las vistas por su ID correcto
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        val viewPager2 = view.findViewById<ViewPager2>(R.id.viewPager2)
        val fabAddRoutine = view.findViewById<ExtendedFloatingActionButton>(R.id.fabAddRoutine)

        // Configuramos el adaptador de páginas
        val adapter = RutinasPagerAdapter(this)
        viewPager2.adapter = adapter

        // Conectamos las pestañas con el ViewPager
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.text = when (position) {
                0 -> "Explorar"       // CurrentRoutineFragment
                1 -> "Historial"      // RoutineHistoryFragment
                2 -> "Guardadas"      // SavedRoutinesFragment
                3 -> "Para Ti"        // RecommendationsFragment
                else -> "Tab $position"
            }
        }.attach()

        // Acción del botón flotante "Crear"
        fabAddRoutine.setOnClickListener {
            // Aquí iría la lógica para crear una nueva rutina
            Toast.makeText(context, "Próximamente: Crear Rutina", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    // Adaptador interno para manejar los fragmentos
    inner class RutinasPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> CurrentRoutineFragment() // Catálogo
            1 -> RoutineHistoryFragment() // Tu historial
            2 -> SavedRoutinesFragment()  // Favoritos (Si aún no existe, crea una clase vacía temporal)
            3 -> RecommendationsFragment() // Recomendaciones IA
            else -> CurrentRoutineFragment()
        }
    }
}