package com.example.gym4house

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnUpdatePassword.setOnClickListener {
            validateAndUpdatePassword()
        }
    }

    private fun validateAndUpdatePassword() {
        val currentPass = binding.etCurrentPassword.text.toString().trim()
        val newPass = binding.etNewPassword.text.toString().trim()
        val confirmPass = binding.etConfirmPassword.text.toString().trim()

        // 1. Validaciones básicas
        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(context, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPass != confirmPass) {
            binding.etConfirmPassword.error = "Las contraseñas no coinciden"
            return
        }

        if (newPass.length < 6) {
            binding.etNewPassword.error = "Mínimo 6 caracteres"
            return
        }

        // 2. Proceso de actualización
        val user = auth.currentUser
        if (user != null && user.email != null) {
            binding.btnUpdatePassword.isEnabled = false
            binding.btnUpdatePassword.text = "Verificando..."

            // Credencial para re-autenticar (Seguridad)
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)

            user.reauthenticate(credential)
                .addOnSuccessListener {
                    // La contraseña actual es correcta, procedemos a cambiarla
                    binding.btnUpdatePassword.text = "Actualizando..."

                    user.updatePassword(newPass)
                        .addOnSuccessListener {
                            Toast.makeText(context, "¡Contraseña actualizada con éxito!", Toast.LENGTH_LONG).show()
                            parentFragmentManager.popBackStack() // Volver atrás
                        }
                        .addOnFailureListener { e ->
                            handleError("Error al actualizar", e)
                        }
                }
                .addOnFailureListener { e ->
                    // La contraseña actual era incorrecta
                    binding.etCurrentPassword.error = "Contraseña actual incorrecta"
                    handleError("Verificación fallida", e)
                }
        }
    }

    private fun handleError(msg: String, e: Exception) {
        if (context != null) {
            Toast.makeText(context, "$msg: ${e.message}", Toast.LENGTH_SHORT).show()
            binding.btnUpdatePassword.isEnabled = true
            binding.btnUpdatePassword.text = "Actualizar Contraseña"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}