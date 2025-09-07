package com.example.gym4house

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProgessiveFragment : Fragment() {

    private lateinit var progressiveLineChart: LineChart
    private lateinit var progressiveProgressBar: ProgressBar
    private lateinit var progressiveNoDataTextView: TextView

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val TAG = "ProgessiveFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progressive_history, container, false)
        progressiveLineChart = view.findViewById(R.id.progressiveLineChart)
        progressiveProgressBar = view.findViewById(R.id.progressiveProgressBar)
        progressiveNoDataTextView = view.findViewById(R.id.progressiveNoDataTextView)

        progressiveLineChart.visibility = View.VISIBLE
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChart()
        loadRoutineHistory()
    }

    private fun setupChart() {
        val brown = ContextCompat.getColor(requireContext(), R.color.gym_brown_variant)

        progressiveLineChart.description.isEnabled = false
        progressiveLineChart.setTouchEnabled(true)
        progressiveLineChart.isDragEnabled = true
        progressiveLineChart.setScaleEnabled(true)
        progressiveLineChart.setDrawGridBackground(false)
        progressiveLineChart.setPinchZoom(true)
        progressiveLineChart.animateX(1500)

        val xAxis = progressiveLineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = brown
        xAxis.textSize = 10f
        xAxis.valueFormatter = DateValueFormatter()

        val yAxisLeft = progressiveLineChart.axisLeft
        yAxisLeft.textColor = brown
        yAxisLeft.textSize = 10f
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.axisMinimum = 0f

        progressiveLineChart.axisRight.isEnabled = false
        progressiveLineChart.legend.textColor = brown
    }

    private fun loadRoutineHistory() {
        progressiveProgressBar.visibility = View.VISIBLE
        progressiveNoDataTextView.visibility = View.GONE

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            progressiveProgressBar.visibility = View.GONE
            Log.w(TAG, "No hay usuario autenticado")
            return
        }

        Log.d(TAG, "Cargando historial para usuario: $userId")

        firestore.collection("usuarios")
            .document(userId)
            .collection("progreso")
            .orderBy("fechaCompletado", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                progressiveProgressBar.visibility = View.GONE
                val entries = ArrayList<Entry>()

                Log.d(TAG, "Documentos recibidos: ${documents.size()}")

                if (documents.isEmpty) {
                    Log.w(TAG, "Colección progreso vacía")
                    progressiveNoDataTextView.text = "No hay datos de historial para mostrar."
                    progressiveNoDataTextView.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                for (document in documents) {
                    val duracionMinutos = document.getLong("duracionMinutos")?.toFloat()
                    var fechaEnMilisegundos: Long? = null

                    Log.d(TAG, "Documento: ${document.id} → duracion=$duracionMinutos")

                    val fechaCompletadoTimestamp = document.getTimestamp("fechaCompletado")
                    if (fechaCompletadoTimestamp != null) {
                        fechaEnMilisegundos = fechaCompletadoTimestamp.toDate().time
                        Log.d(TAG, "Fecha como Timestamp: $fechaEnMilisegundos")
                    } else {
                        val fechaCompletadoString = document.getString("fechaCompletado")
                        if (fechaCompletadoString != null) {
                            try {
                                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                                fechaEnMilisegundos = dateFormat.parse(fechaCompletadoString)?.time
                                Log.d(TAG, "Fecha como String: $fechaCompletadoString → $fechaEnMilisegundos")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al parsear fecha: $fechaCompletadoString", e)
                            }
                        }
                    }

                    if (fechaEnMilisegundos != null && duracionMinutos != null) {
                        entries.add(Entry(fechaEnMilisegundos.toFloat(), duracionMinutos))
                        Log.d(TAG, "Añadido Entry → x=$fechaEnMilisegundos y=$duracionMinutos")
                    } else {
                        Log.w(TAG, "Documento ignorado por datos inválidos")
                    }
                }

                if (entries.isNotEmpty()) {
                    val brown = ContextCompat.getColor(requireContext(), R.color.gym_brown_variant)

                    val dataSet = LineDataSet(entries, "Duración de Rutinas (min)")
                    dataSet.color = brown
                    dataSet.setCircleColor(brown)
                    dataSet.circleRadius = 4f
                    dataSet.setDrawCircleHole(false)
                    dataSet.valueTextColor = brown
                    dataSet.valueTextSize = 9f
                    dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

                    progressiveLineChart.data = LineData(dataSet)
                    progressiveLineChart.invalidate()

                    Log.i(TAG, "Gráfico actualizado con ${entries.size} puntos")
                } else {
                    progressiveNoDataTextView.text = "No hay datos válidos para graficar."
                    progressiveNoDataTextView.visibility = View.VISIBLE
                    Log.w(TAG, "No se generaron entradas válidas")
                }
            }
            .addOnFailureListener { exception ->
                progressiveProgressBar.visibility = View.GONE
                progressiveNoDataTextView.text = "Error al cargar el historial: ${exception.message}"
                progressiveNoDataTextView.visibility = View.VISIBLE
                Log.e(TAG, "Error al consultar Firestore", exception)
                Toast.makeText(requireContext(), "Error al cargar historial", Toast.LENGTH_SHORT).show()
            }
    }

    private class DateValueFormatter : ValueFormatter() {
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return try {
                dateFormat.format(Date(value.toLong()))
            } catch (e: Exception) {
                ""
            }
        }
    }
}
