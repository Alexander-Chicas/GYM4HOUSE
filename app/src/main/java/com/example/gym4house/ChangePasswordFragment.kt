package com.example.gym4house

import android.os.Bundle
import android.util.Log // Importa Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.gym4house.databinding.FragmentChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding.toolbarChangePassword.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonChangePassword.setOnClickListener {
            val oldPassword = binding.editTextOldPassword.text.toString()
            val newPassword = binding.editTextNewPassword.text.toString()
            val confirmNewPassword = binding.editTextConfirmPassword.text.toString()

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmNewPassword) {
                Toast.makeText(requireContext(), "Las nuevas contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = auth.currentUser
            if (user != null) {
                val userEmail = user.email
                if (userEmail == null) {
                    Toast.makeText(requireContext(), "El usuario no tiene un email asociado.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val credential = EmailAuthProvider.getCredential(userEmail, oldPassword)

                user.reauthenticate(credential)
                    .addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            Log.d("ChangePassword", "Reautenticación exitosa.")
                            user.updatePassword(newPassword)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        Log.d("ChangePassword", "Contraseña actualizada exitosamente en Firebase.")
                                        Toast.makeText(requireContext(), "Contraseña cambiada exitosamente", Toast.LENGTH_SHORT).show()
                                        findNavController().navigateUp()
                                    } else {
                                        Log.e("ChangePassword", "Error al actualizar contraseña: ${updateTask.exception?.message}", updateTask.exception)
                                        Toast.makeText(requireContext(), "Error al cambiar la contraseña: ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                        } else {
                            Log.e("ChangePassword", "Error de reautenticación: ${reauthTask.exception?.message}", reauthTask.exception)
                            Toast.makeText(requireContext(), "Error de reautenticación: Contraseña actual incorrecta", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                Log.w("ChangePassword", "Intento de cambio de contraseña sin usuario autenticado.")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}