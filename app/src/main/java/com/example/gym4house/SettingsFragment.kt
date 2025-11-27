package com.example.gym4house

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gym4house.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botón Atrás (Vuelve al Perfil)
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Ir a Salud (Modo Edición)
        binding.btnHealth.setOnClickListener {
            val intent = Intent(requireContext(), HealthRestrictionsActivity::class.java)
            intent.putExtra(HealthRestrictionsActivity.LAUNCH_MODE_EXTRA, HealthRestrictionsActivity.MODE_EDIT)
            startActivity(intent)
        }

        // Ir a Equipamiento (Modo Edición)
        binding.btnEquipment.setOnClickListener {
            val intent = Intent(requireContext(), EquipmentActivity::class.java)
            intent.putExtra(EquipmentActivity.LAUNCH_MODE_EXTRA, EquipmentActivity.MODE_EDIT)
            startActivity(intent)
        }

        // Ir a Cambiar Contraseña
        binding.btnPassword.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(ChangePasswordFragment())
        }

        // Ir a Recordatorios
        binding.btnReminders.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(RemindersSettingsFragment())
        }

        // Cerrar Sesión
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}