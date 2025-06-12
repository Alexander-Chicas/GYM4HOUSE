package com.example.gym4house

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class RutinasFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rutinas, container, false)

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        val viewPager2 = view.findViewById<ViewPager2>(R.id.viewPager2)

        val adapter = RutinasPagerAdapter(this)
        viewPager2.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.text = when (position) {
                0 -> "Rutina Actual"
                1 -> "Historial"
                2 -> "Guardadas"
                3 -> "Recomendadas" // ¡NUEVA PESTAÑA!
                else -> "Desconocido"
            }
        }.attach()

        return view
    }

    inner class RutinasPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 4 // ¡Ahora hay 4 pestañas!

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> CurrentRoutineFragment()
            1 -> RoutineHistoryFragment()
            2 -> SavedRoutinesFragment()
            3 -> RecommendationsFragment() // ¡Nuevo Fragment de Recomendaciones!
            else -> Fragment()
        }
    }
}