package com.example.gym4house

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.gym4house.databinding.FragmentExerciseDetailBinding
import com.google.firebase.storage.FirebaseStorage

class ExerciseDetailFragment : Fragment() {

    private var _binding: FragmentExerciseDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var ejercicio: Ejercicio

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ejercicio = requireArguments().getParcelable("ejercicio")!!

        binding.tvExerciseName.text = ejercicio.nombreEjercicio
        binding.tvExerciseDescription.text = ejercicio.descripcion

        if (!ejercicio.mediaUrl.isNullOrEmpty()) {
            // 🔹 Referencia a Firebase Storage
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(ejercicio.mediaUrl!!)

            storageRef.downloadUrl.addOnSuccessListener { uri: Uri ->
                val mediaUrl = uri.toString()

                if (mediaUrl.endsWith(".gif")) {
                    // Mostrar GIF
                    Glide.with(this)
                        .asGif()
                        .load(mediaUrl)
                        .into(binding.ivExerciseMedia)

                    binding.ivExerciseMedia.visibility = View.VISIBLE
                    binding.videoExercise.visibility = View.GONE

                } else if (mediaUrl.endsWith(".mp4")) {
                    // Mostrar video en loop
                    val mediaController = MediaController(requireContext())
                    binding.videoExercise.setMediaController(mediaController)
                    mediaController.setAnchorView(binding.videoExercise)

                    binding.videoExercise.setVideoURI(Uri.parse(mediaUrl))
                    binding.videoExercise.setOnPreparedListener { mp ->
                        mp.isLooping = true
                        mp.start()
                    }

                    binding.videoExercise.visibility = View.VISIBLE
                    binding.ivExerciseMedia.visibility = View.GONE
                }
            }.addOnFailureListener {
                // Manejar error si no se puede obtener el URL
                binding.ivExerciseMedia.visibility = View.GONE
                binding.videoExercise.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(ejercicio: Ejercicio): ExerciseDetailFragment {
            val fragment = ExerciseDetailFragment()
            fragment.arguments = Bundle().apply {
                putParcelable("ejercicio", ejercicio)
            }
            return fragment
        }
    }
}
