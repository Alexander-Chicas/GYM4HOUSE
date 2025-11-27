package com.example.gym4house

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.gym4house.databinding.FragmentProgressiveHistoryBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class ProgessiveFragment : Fragment() {

    private var _binding: FragmentProgressiveHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressiveHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupChartStyle()
        loadProgressData()
    }

    private fun setupChartStyle() {
        binding.chartProgress.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDragEnabled(true)
            setScaleEnabled(false)
            setDrawGridBackground(false)
            legend.isEnabled = false // Ocultar leyenda para look limpio

            // Ejes
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.textColor = Color.parseColor("#80FFFFFF")
            xAxis.setDrawLabels(false) // Ocultar fechas por espacio, o formatear luego

            axisLeft.textColor = Color.parseColor("#80FFFFFF")
            axisLeft.setDrawGridLines(true)
            axisLeft.gridColor = Color.parseColor("#1AFFFFFF")

            axisRight.isEnabled = false // Ocultar eje derecho

            setNoDataText("Cargando datos...")
            setNoDataTextColor(Color.WHITE)
        }
    }

    private fun loadProgressData() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(uid)
            .collection("progreso")
            .orderBy("fechaCompletado", Query.Direction.ASCENDING)
            .limit(10) // Últimos 10 entrenamientos para la gráfica
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    binding.chartProgress.setNoDataText("No hay datos de entrenamiento aún")
                    return@addOnSuccessListener
                }

                val entries = ArrayList<Entry>()
                var totalMinutes = 0L
                var totalWorkouts = 0
                var lastRoutineName = ""
                var lastRoutineDate = ""

                // Procesar datos
                documents.forEachIndexed { index, doc ->
                    val historial = doc.toObject(HistorialRutina::class.java)
                    val duracion = historial.duracionMinutos.toFloat()

                    entries.add(Entry(index.toFloat(), duracion))

                    totalMinutes += historial.duracionMinutos
                    totalWorkouts++

                    // Guardar último para la tarjeta
                    lastRoutineName = historial.nombreRutina
                    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                    historial.fechaCompletado?.let {
                        lastRoutineDate = sdf.format(it.toDate())
                    }
                }

                // Actualizar UI de resumen
                binding.tvTotalWorkouts.text = "$totalWorkouts"
                binding.tvTotalMinutes.text = "$totalMinutes"

                if (lastRoutineName.isNotEmpty()) {
                    binding.tvLastRoutineName.text = lastRoutineName
                    binding.tvLastRoutineDate.text = "Completado el $lastRoutineDate"
                }

                // Dibujar Gráfica
                val dataSet = LineDataSet(entries, "Duración (min)")
                dataSet.color = Color.parseColor("#FF9800") // Color de la línea
                dataSet.lineWidth = 3f
                dataSet.setCircleColor(Color.WHITE)
                dataSet.circleRadius = 4f
                dataSet.setDrawCircleHole(false)
                dataSet.valueTextColor = Color.WHITE
                dataSet.valueTextSize = 10f
                dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // Línea curva suave
                dataSet.setDrawFilled(true)
                dataSet.fillColor = Color.parseColor("#FF9800")
                dataSet.fillAlpha = 50

                val lineData = LineData(dataSet)
                binding.chartProgress.data = lineData
                binding.chartProgress.invalidate() // Refrescar
                binding.chartProgress.animateY(1000) // Animación
            }
            .addOnFailureListener {
                binding.chartProgress.setNoDataText("Error cargando datos")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}