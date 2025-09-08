package com.example.gym4house

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.example.gym4house.databinding.FragmentHealthRestrictionsBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HealthRestrictionsFragment : Fragment() {

    private var _binding: FragmentHealthRestrictionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var progressBar: ProgressBar
    private var launchMode: String = HealthRestrictionsActivity.MODE_EDIT_PROFILE // Para almacenar el modo de lanzamiento

    // Checkboxes de Alergias
    private lateinit var checkBoxGluten: CheckBox
    private lateinit var checkBoxLactosa: CheckBox
    private lateinit var checkBoxNueces: CheckBox
    private lateinit var checkBoxMariscos: CheckBox
    private lateinit var checkBoxOtraAlergia: CheckBox
    private lateinit var editTextOtraAlergia: EditText
    private lateinit var checkBoxNingunaAlergia: CheckBox

    // Checkboxes de Condiciones Médicas
    private lateinit var checkBoxHipertension: CheckBox
    private lateinit var checkBoxDiabetes: CheckBox
    private lateinit var checkBoxAsma: CheckBox
    private lateinit var checkBoxProblemasCardiacos: CheckBox
    private lateinit var checkBoxOtraCondicion: CheckBox
    private lateinit var editTextOtraCondicion: EditText
    private lateinit var checkBoxNingunaCondicion: CheckBox

    // Checkboxes de Restricciones Físicas
    private lateinit var checkBoxDolorRodilla: CheckBox
    private lateinit var checkBoxDolorEspalda: CheckBox
    private lateinit var checkBoxLesionMuscular: CheckBox
    private lateinit var checkBoxOtraRestriccion: CheckBox
    private lateinit var editTextOtraRestriccion: EditText
    private lateinit var checkBoxNingunaRestriccion: CheckBox

    // EditText de Notas Médicas
    private lateinit var editTextNotasMedicas: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Recuperar el modo de lanzamiento de los argumentos pasados al fragmento
        arguments?.let {
            launchMode = it.getString(HealthRestrictionsActivity.LAUNCH_MODE_EXTRA) ?: HealthRestrictionsActivity.MODE_EDIT_PROFILE
            Log.d("HealthRestrictionsFragment", "onCreate - Launch Mode: $launchMode") // LOG para depuración
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthRestrictionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inicializar vistas
        progressBar = binding.restrictionsProgressBar

        // Alergias
        checkBoxGluten = binding.checkBoxGluten
        checkBoxLactosa = binding.checkBoxLactosa
        checkBoxNueces = binding.checkBoxNueces
        checkBoxMariscos = binding.checkBoxMariscos
        checkBoxOtraAlergia = binding.checkBoxOtraAlergia
        editTextOtraAlergia = binding.editTextOtraAlergia
        checkBoxNingunaAlergia = binding.checkBoxNingunaAlergia

        // Condiciones Médicas
        checkBoxHipertension = binding.checkBoxHipertension
        checkBoxDiabetes = binding.checkBoxDiabetes
        checkBoxAsma = binding.checkBoxAsma
        checkBoxProblemasCardiacos = binding.checkBoxProblemasCardiacos
        checkBoxOtraCondicion = binding.checkBoxOtraCondicion
        editTextOtraCondicion = binding.editTextOtraCondicion
        checkBoxNingunaCondicion = binding.checkBoxNingunaCondicion

        // Restricciones Físicas
        checkBoxDolorRodilla = binding.checkBoxDolorRodilla
        checkBoxDolorEspalda = binding.checkBoxDolorEspalda
        checkBoxLesionMuscular = binding.checkBoxLesionMusDificil // Corregido: ID del XML
        checkBoxOtraRestriccion = binding.checkBoxOtraRestriccion
        editTextOtraRestriccion = binding.editTextOtraRestriccion
        checkBoxNingunaRestriccion = binding.checkBoxNingunaRestriccion // Corregido: ID del XML

        // Notas Médicas
        editTextNotasMedicas = binding.editTextNotasMedicas

        setupCheckboxListeners()
        loadHealthRestrictions()
        setupButtonListeners()
    }

    private fun setupCheckboxListeners() {
        // --- Lógica para Alergias ---
        val alergiaCheckBoxes = listOf(checkBoxGluten, checkBoxLactosa, checkBoxNueces, checkBoxMariscos, checkBoxOtraAlergia)

        // Listener para el checkbox "Otra Alergia"
        checkBoxOtraAlergia.setOnCheckedChangeListener { _, isChecked ->
            editTextOtraAlergia.isEnabled = isChecked
            if (!isChecked) {
                editTextOtraAlergia.text.clear()
            }
            // Si "Otro" se marca, desmarcar "Ninguna"
            if (isChecked) {
                checkBoxNingunaAlergia.isChecked = false
            }
        }

        // Listener para los checkboxes de alergias individuales
        alergiaCheckBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    checkBoxNingunaAlergia.isChecked = false // Si se marca una alergia, desmarcar "Ninguna"
                }
            }
        }

        // Listener para "Ninguna Alergia"
        checkBoxNingunaAlergia.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Si "Ninguna Alergia" se marca, desmarcar y deshabilitar las demás alergias
                alergiaCheckBoxes.forEach { it.isChecked = false; it.isEnabled = false }
                editTextOtraAlergia.isEnabled = false
                editTextOtraAlergia.text.clear()
            } else {
                // Si "Ninguna Alergia" se desmarca, volver a habilitar las demás alergias
                alergiaCheckBoxes.forEach { it.isEnabled = true }
                // La habilitación del editTextOtraAlergia depende del estado de checkBoxOtraAlergia, no de aquí.
                editTextOtraAlergia.isEnabled = checkBoxOtraAlergia.isChecked
            }
        }

        // --- Lógica para Condiciones Médicas ---
        val condicionCheckBoxes = listOf(checkBoxHipertension, checkBoxDiabetes, checkBoxAsma, checkBoxProblemasCardiacos, checkBoxOtraCondicion)

        checkBoxOtraCondicion.setOnCheckedChangeListener { _, isChecked ->
            editTextOtraCondicion.isEnabled = isChecked
            if (!isChecked) {
                editTextOtraCondicion.text.clear()
            }
            if (isChecked) {
                checkBoxNingunaCondicion.isChecked = false
            }
        }

        condicionCheckBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    checkBoxNingunaCondicion.isChecked = false
                }
            }
        }

        checkBoxNingunaCondicion.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                condicionCheckBoxes.forEach { it.isChecked = false; it.isEnabled = false }
                editTextOtraCondicion.isEnabled = false
                editTextOtraCondicion.text.clear()
            } else {
                condicionCheckBoxes.forEach { it.isEnabled = true }
                editTextOtraCondicion.isEnabled = checkBoxOtraCondicion.isChecked
            }
        }

        // --- Lógica para Restricciones Físicas ---
        val restriccionCheckBoxes = listOf(checkBoxDolorRodilla, checkBoxDolorEspalda, checkBoxLesionMuscular, checkBoxOtraRestriccion)

        checkBoxOtraRestriccion.setOnCheckedChangeListener { _, isChecked ->
            editTextOtraRestriccion.isEnabled = isChecked
            if (!isChecked) {
                editTextOtraRestriccion.text.clear()
            }
            if (isChecked) {
                checkBoxNingunaRestriccion.isChecked = false
            }
        }

        restriccionCheckBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    checkBoxNingunaRestriccion.isChecked = false
                }
            }
        }

        checkBoxNingunaRestriccion.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                restriccionCheckBoxes.forEach { it.isChecked = false; it.isEnabled = false }
                editTextOtraRestriccion.isEnabled = false
                editTextOtraRestriccion.text.clear()
            } else {
                restriccionCheckBoxes.forEach { it.isEnabled = true }
                editTextOtraRestriccion.isEnabled = checkBoxOtraRestriccion.isChecked
            }
        }
    }

    private fun loadHealthRestrictions() {
        Log.d("HealthRestrictionsFragment", "loadHealthRestrictions - Iniciando carga.") // LOG
        progressBar.visibility = View.VISIBLE
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Snackbar.make(binding.root, "Usuario no autenticado", Snackbar.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            Log.e("HealthRestrictionsFragment", "loadHealthRestrictions - Usuario no autenticado, abortando.") // LOG
            return
        }

        firestore.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener { document ->
                Log.d("HealthRestrictionsFragment", "loadHealthRestrictions - Carga de Firestore exitosa. Documento existe: ${document.exists()}") // LOG
                progressBar.visibility = View.GONE
                if (document.exists()) {
                    val restriccionesSalud = document.get("restriccionesSalud") as? Map<String, Any>

                    restriccionesSalud?.let {
                        // Alergias
                        val alergias = it["alergias"] as? Map<String, Any>
                        alergias?.let { al ->
                            checkBoxGluten.isChecked = al["gluten"] as? Boolean ?: false
                            checkBoxLactosa.isChecked = al["lactosa"] as? Boolean ?: false
                            checkBoxNueces.isChecked = al["nueces"] as? Boolean ?: false
                            checkBoxMariscos.isChecked = al["mariscos"] as? Boolean ?: false
                            checkBoxOtraAlergia.isChecked = al["otraAlergia"] as? Boolean ?: false
                            editTextOtraAlergia.setText(al["otraAlergiaTexto"] as? String ?: "")
                            checkBoxNingunaAlergia.isChecked = al["ningunaAlergia"] as? Boolean ?: false
                        }

                        // Condiciones Médicas
                        val condicionesMedicas = it["condicionesMedicas"] as? Map<String, Any>
                        condicionesMedicas?.let { cm ->
                            checkBoxHipertension.isChecked = cm["hipertension"] as? Boolean ?: false
                            checkBoxDiabetes.isChecked = cm["diabetes"] as? Boolean ?: false
                            checkBoxAsma.isChecked = cm["asma"] as? Boolean ?: false
                            checkBoxProblemasCardiacos.isChecked = cm["problemasCardiacos"] as? Boolean ?: false
                            checkBoxOtraCondicion.isChecked = cm["otraCondicion"] as? Boolean ?: false
                            editTextOtraCondicion.setText(cm["otraCondicionTexto"] as? String ?: "")
                            checkBoxNingunaCondicion.isChecked = cm["ningunaCondicion"] as? Boolean ?: false
                        }

                        // Restricciones Físicas
                        val restriccionesFisicas = it["restriccionesFisicas"] as? Map<String, Any>
                        restriccionesFisicas?.let { rf ->
                            checkBoxDolorRodilla.isChecked = rf["dolorRodilla"] as? Boolean ?: false
                            checkBoxDolorEspalda.isChecked = rf["dolorEspalda"] as? Boolean ?: false
                            // Se corrige el ID del checkbox
                            checkBoxLesionMuscular.isChecked = rf["lesionMuscular"] as? Boolean ?: false
                            checkBoxOtraRestriccion.isChecked = rf["otraRestriccion"] as? Boolean ?: false
                            editTextOtraRestriccion.setText(rf["otraRestriccionTexto"] as? String ?: "")
                            checkBoxNingunaRestriccion.isChecked = rf["ningunaRestriccion"] as? Boolean ?: false
                        }

                        // Notas Médicas
                        editTextNotasMedicas.setText(it["notasMedicas"] as? String ?: "")

                        // Ajustar el estado inicial de los EditText "Otro" y las habilitaciones de checkboxes
                        applyInitialUiStateBasedOnLoadedData()
                    }
                } else {
                    // Si no existen restricciones previas, asegura el estado UI por defecto
                    Log.d("HealthRestrictionsFragment", "loadHealthRestrictions - No existen restricciones previas, aplicando estado UI por defecto.") // LOG
                    applyInitialUiStateBasedOnLoadedData()
                }
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
                Snackbar.make(binding.root, "Error al cargar restricciones: ${exception.message}", Snackbar.LENGTH_SHORT).show()
                Log.e("HealthRestrictionsFragment", "loadHealthRestrictions - Error al cargar restricciones: ${exception.message}", exception) // LOG de error
                applyInitialUiStateBasedOnLoadedData() // Asegurar estado UI incluso con error de carga
            }
        Log.d("HealthRestrictionsFragment", "loadHealthRestrictions - Solicitud de Firestore enviada.") // LOG

    }

    // Función para aplicar el estado UI inicial basado en datos cargados o por defecto
    private fun applyInitialUiStateBasedOnLoadedData() {
        val alergiaCheckBoxes = listOf(checkBoxGluten, checkBoxLactosa, checkBoxNueces, checkBoxMariscos, checkBoxOtraAlergia)
        if (checkBoxNingunaAlergia.isChecked) {
            alergiaCheckBoxes.forEach { it.isChecked = false; it.isEnabled = false }
            editTextOtraAlergia.isEnabled = false
            editTextOtraAlergia.text.clear()
        } else {
            alergiaCheckBoxes.forEach { it.isEnabled = true }
            editTextOtraAlergia.isEnabled = checkBoxOtraAlergia.isChecked
        }

        val condicionCheckBoxes = listOf(checkBoxHipertension, checkBoxDiabetes, checkBoxAsma, checkBoxProblemasCardiacos, checkBoxOtraCondicion)
        if (checkBoxNingunaCondicion.isChecked) {
            condicionCheckBoxes.forEach { it.isChecked = false; it.isEnabled = false }
            editTextOtraCondicion.isEnabled = false
            editTextOtraCondicion.text.clear()
        } else {
            condicionCheckBoxes.forEach { it.isEnabled = true }
            editTextOtraCondicion.isEnabled = checkBoxOtraCondicion.isChecked
        }

        val restriccionCheckBoxes = listOf(checkBoxDolorRodilla, checkBoxDolorEspalda, checkBoxLesionMuscular, checkBoxOtraRestriccion)
        if (checkBoxNingunaRestriccion.isChecked) {
            restriccionCheckBoxes.forEach { it.isChecked = false; it.isEnabled = false }
            editTextOtraRestriccion.isEnabled = false
            editTextOtraRestriccion.text.clear()
        } else {
            restriccionCheckBoxes.forEach { it.isEnabled = true }
            editTextOtraRestriccion.isEnabled = checkBoxOtraRestriccion.isChecked
        }
    }

    private fun saveHealthRestrictions() {
        Log.d("HealthRestrictionsFragment", "saveHealthRestrictions - Iniciando el proceso de guardado.") // LOG
        // Añadir validación antes de guardar
        if (!validateHealthRestrictions()) {
            Snackbar.make(binding.root, "Por favor, selecciona una opción en cada categoría (o 'Ninguna').", Snackbar.LENGTH_LONG).show()
            Log.d("HealthRestrictionsFragment", "saveHealthRestrictions - Validación fallida.") // LOG
            return
        }

        progressBar.visibility = View.VISIBLE
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Snackbar.make(binding.root, "Usuario no autenticado", Snackbar.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            Log.e("HealthRestrictionsFragment", "saveHealthRestrictions - Usuario no autenticado, abortando.") // LOG
            return
        }

        val alergiasMap = hashMapOf(
            "gluten" to checkBoxGluten.isChecked,
            "lactosa" to checkBoxLactosa.isChecked,
            "nueces" to checkBoxNueces.isChecked,
            "mariscos" to checkBoxMariscos.isChecked,
            "otraAlergia" to checkBoxOtraAlergia.isChecked,
            "otraAlergiaTexto" to editTextOtraAlergia.text.toString().trim(),
            "ningunaAlergia" to checkBoxNingunaAlergia.isChecked
        )

        val condicionesMedicasMap = hashMapOf(
            "hipertension" to checkBoxHipertension.isChecked,
            "diabetes" to checkBoxDiabetes.isChecked,
            "asma" to checkBoxAsma.isChecked,
            "problemasCardiacos" to checkBoxProblemasCardiacos.isChecked,
            "otraCondicion" to checkBoxOtraCondicion.isChecked,
            "otraCondicionTexto" to editTextOtraCondicion.text.toString().trim(),
            "ningunaCondicion" to checkBoxNingunaCondicion.isChecked
        )

        val restriccionesFisicasMap = hashMapOf(
            "dolorRodilla" to checkBoxDolorRodilla.isChecked,
            "dolorEspalda" to checkBoxDolorEspalda.isChecked,
            "lesionMuscular" to checkBoxLesionMuscular.isChecked,
            "otraRestriccion" to checkBoxOtraRestriccion.isChecked,
            "otraRestriccionTexto" to editTextOtraRestriccion.text.toString().trim(),
            "ningunaRestriccion" to checkBoxNingunaRestriccion.isChecked
        )

        val restriccionesSaludData = hashMapOf(
            "alergias" to alergiasMap,
            "condicionesMedicas" to condicionesMedicasMap,
            "restriccionesFisicas" to restriccionesFisicasMap,
            "notasMedicas" to editTextNotasMedicas.text.toString().trim()
        )

        Log.d("HealthRestrictionsFragment", "saveHealthRestrictions - Datos a guardar: $restriccionesSaludData") // LOG para ver los datos

        firestore.collection("usuarios").document(userId)
            .update("restriccionesSalud", restriccionesSaludData)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Snackbar.make(binding.root, "Restricciones de salud guardadas correctamente.", Snackbar.LENGTH_SHORT).show()
                Log.d("HealthRestrictionsFragment", "saveHealthRestrictions - Datos guardados exitosamente. Navegando a la siguiente pantalla.") // LOG
                navigateToNextScreen()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Snackbar.make(binding.root, "Error al guardar restricciones.", Snackbar.LENGTH_LONG).show()
                Log.e("HealthRestrictionsFragment", "saveHealthRestrictions - Error al guardar restricciones en Firestore.") // LOG de error
            }
        Log.d("HealthRestrictionsFragment", "saveHealthRestrictions - Solicitud de guardado de Firestore enviada.") // LOG
    }

    // Función de validación
    private fun validateHealthRestrictions(): Boolean {
        // Validar Alergias
        val hasSelectedAlergia = listOf(
            checkBoxGluten.isChecked,
            checkBoxLactosa.isChecked,
            checkBoxNueces.isChecked,
            checkBoxMariscos.isChecked,
            (checkBoxOtraAlergia.isChecked && editTextOtraAlergia.text.isNotBlank())
        ).any { it } || checkBoxNingunaAlergia.isChecked
        if (!hasSelectedAlergia) {
            Log.d("HealthRestrictionsFragment", "Validación: No se seleccionó ninguna opción de Alergias.") // LOG
            return false
        }

        // Validar Condiciones Médicas
        val hasSelectedCondicion = listOf(
            checkBoxHipertension.isChecked,
            checkBoxDiabetes.isChecked,
            checkBoxAsma.isChecked,
            checkBoxProblemasCardiacos.isChecked,
            (checkBoxOtraCondicion.isChecked && editTextOtraCondicion.text.isNotBlank())
        ).any { it } || checkBoxNingunaCondicion.isChecked
        if (!hasSelectedCondicion) {
            Log.d("HealthRestrictionsFragment", "Validación: No se seleccionó ninguna opción de Condiciones Médicas.") // LOG
            return false
        }

        // Validar Restricciones Físicas
        val hasSelectedRestriccion = listOf(
            checkBoxDolorRodilla.isChecked,
            checkBoxDolorEspalda.isChecked,
            checkBoxLesionMuscular.isChecked,
            (checkBoxOtraRestriccion.isChecked && editTextOtraRestriccion.text.isNotBlank())
        ).any { it } || checkBoxNingunaRestriccion.isChecked
        if (!hasSelectedRestriccion) {
            Log.d("HealthRestrictionsFragment", "Validación: No se seleccionó ninguna opción de Restricciones Físicas.") // LOG
            return false
        }

        Log.d("HealthRestrictionsFragment", "Validación: Todas las categorías tienen al menos una opción seleccionada.") // LOG
        return true
    }

    private fun setupButtonListeners() {
        binding.buttonGuardar.setOnClickListener {
            Log.d("HealthRestrictionsFragment", "setupButtonListeners - Botón Guardar clicado. Llamando a saveHealthRestrictions().") // LOG
            saveHealthRestrictions()
        }

        binding.buttonOmitir.setOnClickListener {
            Log.d("HealthRestrictionsFragment", "setupButtonListeners - Botón Omitir clicado. Marcando 'Ninguna' y guardando.") // LOG
            Snackbar.make(binding.root, "Restricciones omitidas por ahora.", Snackbar.LENGTH_SHORT).show()
            // Al omitir, marcamos las opciones "Ninguna" por defecto y guardamos.
            checkBoxNingunaAlergia.isChecked = true
            checkBoxNingunaCondicion.isChecked = true
            checkBoxNingunaRestriccion.isChecked = true // Asegurarse de que el nuevo checkbox también se marca
            // Importante: llamar a applyInitialUiStateBasedOnLoadedData() para reflejar el cambio en la UI
            applyInitialUiStateBasedOnLoadedData()
            saveHealthRestrictions() // Esto guardará el estado de "Ninguna" en todas las categorías
        }
    }

    private fun navigateToNextScreen() {
        if (launchMode == HealthRestrictionsActivity.MODE_REGISTER) {
            // CAMBIO: Navegar a la nueva EquipmentActivity
            Log.d("HealthRestrictionsFragment", "navigateToNextScreen - Navegando a EquipmentActivity (flujo de registro).")
            val intent = Intent(requireActivity(), EquipmentActivity::class.java)
            intent.putExtra(EquipmentActivity.LAUNCH_MODE_EXTRA, EquipmentActivity.MODE_REGISTER)
            startActivity(intent)
            requireActivity().finish()
        } else {
            // Si es modo de edición, simplemente cerramos esta Activity para volver a la anterior.
            Log.d("HealthRestrictionsFragment", "navigateToNextScreen - Finalizando HealthRestrictionsActivity (flujo de edición, regresa a la actividad que la lanzó).") // LOG
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
