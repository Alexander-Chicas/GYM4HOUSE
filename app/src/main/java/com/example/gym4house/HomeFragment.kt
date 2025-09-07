package com.example.gym4house

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gym4house.databinding.FragmentHomeBinding // Make sure to create this binding in build.gradle.kts and fragment_home.xml

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Example: Handle button click to navigate to RoutineSelectionFragment
        // If you have a button in fragment_home.xml with id btnStartWorkout
        binding.btnStartWorkout.setOnClickListener {
            (activity as MainActivity).replaceFragment(RoutineSelectionFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}