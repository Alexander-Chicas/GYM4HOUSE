package com.example.gym4house

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gym4house.databinding.ActivityEquipmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.gym4house.Equipment // Asegúrate de que esta importación sea correcta

class EquipmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEquipmentBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: EquipmentAdapter

    // Constantes para el modo de lanzamiento (registro vs. edición)
    companion object {
        const val LAUNCH_MODE_EXTRA = "launch_mode"
        const val MODE_REGISTER = "register"
        const val MODE_EDIT_PROFILE = "edit_profile"
    }

    private var launchMode: String = MODE_EDIT_PROFILE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEquipmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore

        // Obtener el modo de lanzamiento del Intent
        launchMode = intent.getStringExtra(LAUNCH_MODE_EXTRA) ?: MODE_EDIT_PROFILE
        Log.d("EquipmentActivity", "onCreate - Launch Mode: $launchMode")

        setupRecyclerView()
        Log.d("EquipmentActivity", "onCreate - setupRecyclerView() completado.")
        setupListeners()
        Log.d("EquipmentActivity", "onCreate - setupListeners() completado.")
        loadEquipmentList()
        Log.d("EquipmentActivity", "onCreate - loadEquipmentList() iniciado.")
    }

    private fun setupRecyclerView() {
        Log.d("EquipmentActivity", "setupRecyclerView - Iniciando.")
        // Inicializar el adaptador con una lista vacía y los listeners
        adapter = EquipmentAdapter(
            mutableListOf(),
            onItemCheckChanged = { position, isChecked ->
                Log.d("EquipmentActivity", "setupRecyclerView - Item at $position checked: $isChecked")
                // No necesitamos hacer nada aquí porque los datos ya están en el modelo
            },
            onItemDelete = { equipment ->
                // Lógica de eliminación
                deleteEquipment(equipment)
                Log.d("EquipmentActivity", "setupRecyclerView - deleteEquipment llamado para ${equipment.name}.")
            }
        )
        binding.rvEquipment.layoutManager = LinearLayoutManager(this)
        binding.rvEquipment.adapter = adapter
        Log.d("EquipmentActivity", "setupRecyclerView - Completado.")
    }

    private fun setupListeners() {
        Log.d("EquipmentActivity", "setupListeners - Iniciando.")
        binding.btnAddEquipment.setOnClickListener {
            Log.d("EquipmentActivity", "setupListeners - Botón AddEquipment clicado.")
            addEquipment()
        }
        binding.btnSaveEquipment.setOnClickListener {
            Log.d("EquipmentActivity", "setupListeners - Botón SaveEquipment clicado.")
            saveEquipment()
        }
        Log.d("EquipmentActivity", "setupListeners - Completado.")
    }

    private fun addEquipment() {
        Log.d("EquipmentActivity", "addEquipment - Iniciando.")
        val equipmentName = binding.editTextNewEquipment.text?.toString()?.trim() ?: ""
        if (equipmentName.isNotEmpty()) {
            val newEquipment = Equipment(name = equipmentName, isSelected = false)
            adapter.addItem(newEquipment)
            binding.editTextNewEquipment.text?.clear()
            Toast.makeText(this, "Equipo añadido: $equipmentName", Toast.LENGTH_SHORT).show()
            Log.d("EquipmentActivity", "addEquipment - Equipo añadido: $equipmentName.")
        } else {
            Toast.makeText(this, "Por favor, ingresa el nombre del equipo.", Toast.LENGTH_SHORT).show()
            Log.d("EquipmentActivity", "addEquipment - Nombre de equipo vacío.")
        }
        Log.d("EquipmentActivity", "addEquipment - Completado.")
    }

    private fun deleteEquipment(equipment: Equipment) {
        Log.d("EquipmentActivity", "deleteEquipment - Iniciando para ${equipment.name}.")
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userEquipmentRef = db.collection("usuarios").document(userId)
                .collection("equipamiento")
            // Buscar el documento con el nombre del equipamiento
            userEquipmentRef.whereEqualTo("nombre", equipment.name)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // Eliminar el documento encontrado
                        val docId = querySnapshot.documents[0].id
                        userEquipmentRef.document(docId).delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "${equipment.name} eliminado.", Toast.LENGTH_SHORT).show()
                                adapter.removeItem(equipment)
                                Log.d("EquipmentActivity", "deleteEquipment - ${equipment.name} eliminado de Firestore y localmente.")
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.e("EquipmentActivity", "deleteEquipment - Error al eliminar ${equipment.name} de Firestore: ${e.message}", e)
                            }
                    } else {
                        // Si el equipamiento no está en Firestore, simplemente lo quitamos de la lista local
                        adapter.removeItem(equipment)
                        Log.d("EquipmentActivity", "deleteEquipment - ${equipment.name} no encontrado en Firestore, eliminado localmente.")
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al buscar equipamiento: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("EquipmentActivity", "deleteEquipment - Error al buscar ${equipment.name} en Firestore: ${e.message}", e)
                }
        } else {
            Log.w("EquipmentActivity", "deleteEquipment - Usuario no autenticado.")
        }
        Log.d("EquipmentActivity", "deleteEquipment - Finalizado.")
    }

    private fun loadEquipmentList() {
        Log.d("EquipmentActivity", "loadEquipmentList - Iniciando.")
        binding.progressBar.visibility = View.VISIBLE
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
            Log.e("EquipmentActivity", "loadEquipmentList - Usuario no autenticado, abortando carga.")
            return
        }

        db.collection("usuarios").document(userId)
            .collection("equipamiento")
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("EquipmentActivity", "loadEquipmentList - Carga de Firestore exitosa. Documentos: ${querySnapshot.size()}")
                binding.progressBar.visibility = View.GONE
                val equipmentList = mutableListOf<Equipment>()
                for (document in querySnapshot.documents) {
                    val name = document.getString("nombre") ?: ""
                    val isSelected = document.getBoolean("estaSeleccionado") ?: false
                    if (name.isNotEmpty()) {
                        equipmentList.add(Equipment(name, isSelected))
                    }
                }
                // Si la lista está vacía, añade los elementos por defecto
                if (equipmentList.isEmpty()) {
                    Log.d("EquipmentActivity", "loadEquipmentList - Lista del usuario vacía, cargando lista por defecto.")
                    val defaultEquipment = getDefaultEquipmentList()
                    adapter.setItems(defaultEquipment)
                } else {
                    Log.d("EquipmentActivity", "loadEquipmentList - Cargando lista del usuario (${equipmentList.size} elementos).")
                    adapter.setItems(equipmentList)
                }
                Log.d("EquipmentActivity", "loadEquipmentList - Adaptador actualizado con la lista de equipamiento.")
            }
            .addOnFailureListener { e ->
                Log.e("EquipmentActivity", "loadEquipmentList - Error al cargar equipamiento de Firestore: ${e.message}", e)
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Error al cargar equipamiento: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        Log.d("EquipmentActivity", "loadEquipmentList - Solicitud de Firestore enviada.")
    }

    // Método para obtener la lista de equipamiento por defecto
    private fun getDefaultEquipmentList(): List<Equipment> {
        Log.d("EquipmentActivity", "getDefaultEquipmentList - Generando lista por defecto.")
        val defaultList = mutableListOf<Equipment>()
        defaultList.add(Equipment("Mancuernas", false))
        defaultList.add(Equipment("Barra con disco", false))
        defaultList.add(Equipment("Banda elástica", false))
        defaultList.add(Equipment("Cuerda para saltar", false))
        defaultList.add(Equipment("Silla o banco", false))
        defaultList.add(Equipment("Colchoneta", false))
        defaultList.add(Equipment("Kettlebell", false))
        defaultList.add(Equipment("Bicicleta estática", false))
        defaultList.add(Equipment("Caminadora", false))
        Log.d("EquipmentActivity", "getDefaultEquipmentList - Lista por defecto generada (${defaultList.size} elementos).")
        return defaultList
    }

    private fun saveEquipment() {
        Log.d("EquipmentActivity", "saveEquipment - Iniciando.")
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            Log.e("EquipmentActivity", "saveEquipment - Usuario no autenticado, abortando guardado.")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        val batch = db.batch()
        val userEquipmentRef = db.collection("usuarios").document(userId).collection("equipamiento")

        // Primero, limpia la colección actual para evitar duplicados
        userEquipmentRef.get().addOnSuccessListener { querySnapshot ->
            Log.d("EquipmentActivity", "saveEquipment - Limpiando colección existente (${querySnapshot.size()} documentos).")
            for (document in querySnapshot.documents) {
                batch.delete(document.reference)
            }

            // Luego, añade los nuevos documentos del adaptador
            val equipmentToSave = adapter.getEquipmentList()
            Log.d("EquipmentActivity", "saveEquipment - Añadiendo ${equipmentToSave.size} nuevos documentos.")
            for (equipment in equipmentToSave) {
                val newDocRef = userEquipmentRef.document()
                val data = hashMapOf(
                    "nombre" to equipment.name,
                    "estaSeleccionado" to equipment.isSelected
                )
                batch.set(newDocRef, data)
            }

            batch.commit()
                .addOnSuccessListener {
                    Log.d("EquipmentActivity", "saveEquipment - Equipamiento guardado correctamente en Firestore.")
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Equipamiento guardado correctamente.", Toast.LENGTH_SHORT).show()
                    navigateToNextScreen()
                }
                .addOnFailureListener { e ->
                    Log.e("EquipmentActivity", "saveEquipment - Error al guardar equipamiento en Firestore: ${e.message}", e)
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error al guardar equipamiento: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
        .addOnFailureListener { e ->
            Log.e("EquipmentActivity", "saveEquipment - Error al obtener la colección para limpieza: ${e.message}", e)
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Error al preparar el guardado: ${e.message}", Toast.LENGTH_LONG).show()
        }
        Log.d("EquipmentActivity", "saveEquipment - Solicitud de guardado iniciada.")
    }

    private fun navigateToNextScreen() {
        if (launchMode == MODE_REGISTER) {
            // TEMPORALMENTE: Solo finaliza esta actividad para ver si se muestra EquipmentActivity
            // Si después de este cambio, AÚN te envía a WelcomeActivity, el problema está antes.
            // Si ahora se queda en EquipmentActivity (después de guardar), el problema era la navegación inmediata.
            Log.d("EquipmentActivity", "navigateToNextScreen - Modo Registro: Finalizando EquipmentActivity para depuración.")
            finish()
            // REMOVIDO TEMPORALMENTE:
            // val intent = Intent(this, WelcomeActivity::class.java)
            // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // startActivity(intent)
        } else {
            // Si es el modo de edición, simplemente finaliza la actividad para volver al PerfilFragment
            Log.d("EquipmentActivity", "navigateToNextScreen - Modo Edición: Finalizando EquipmentActivity.")
            finish()
        }
        Log.d("EquipmentActivity", "navigateToNextScreen - Completado. Activity finalizada.")
    }
}
