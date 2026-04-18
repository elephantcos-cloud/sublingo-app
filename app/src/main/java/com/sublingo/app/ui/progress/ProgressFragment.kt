package com.sublingo.app.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sublingo.app.databinding.FragmentProgressBinding
import com.sublingo.app.data.repository.TranslationState
import com.sublingo.app.widget.SpiralProgressView
import kotlinx.coroutines.launch

class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProgressViewModel by viewModels()
    private val args: ProgressFragmentArgs by navArgs()
    private var translationStarted = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvFileName.text = args.fileName

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                handleState(state)
            }
        }

        if (!translationStarted) {
            translationStarted = true
            viewModel.startTranslation(
                fileUri = args.fileUri,
                fileName = args.fileName,
                fileType = args.fileType,
                sourceLang = args.sourceLang,
                targetLang = args.targetLang
            )
        }
    }

    private fun handleState(state: TranslationState) {
        when (state) {
            is TranslationState.Idle -> {}

            is TranslationState.Parsing -> {
                binding.spiralProgress.setProgress(
                    progress = state.progress * 0.1f,
                    phase = SpiralProgressView.Phase.UPLOAD,
                    phaseName = "Parsing",
                    currentLine = "Reading file..."
                )
                binding.tvDetail.text = "Reading subtitle file..."
            }

            is TranslationState.Translating -> {
                val overallProgress = 0.1f + state.progress * 0.8f
                binding.spiralProgress.setProgress(
                    progress = overallProgress,
                    phase = SpiralProgressView.Phase.TRANSLATE,
                    phaseName = "Translating",
                    currentLine = state.currentLine
                )
                binding.tvDetail.text = "${state.current} / ${state.total} lines"
                binding.tvOriginal.text = state.currentLine
                binding.tvTranslated.text = state.translatedLine
                binding.tvOriginal.visibility = View.VISIBLE
                binding.tvTranslated.visibility = View.VISIBLE
                binding.divTranslation.visibility = View.VISIBLE
            }

            is TranslationState.Saving -> {
                val overallProgress = 0.9f + state.progress * 0.1f
                binding.spiralProgress.setProgress(
                    progress = overallProgress,
                    phase = SpiralProgressView.Phase.SAVE,
                    phaseName = "Saving",
                    currentLine = "Writing output file..."
                )
                binding.tvDetail.text = "Saving translated file..."
            }

            is TranslationState.Completed -> {
                binding.spiralProgress.setProgress(1f, SpiralProgressView.Phase.SAVE, "Done")
                val action = ProgressFragmentDirections.actionProgressToResult(
                    outputPath = state.outputPath,
                    lineCount = state.lineCount,
                    targetLang = args.targetLang
                )
                findNavController().navigate(action)
            }

            is TranslationState.Error -> {
                binding.tvDetail.text = "Error: ${state.message}"
                binding.tvDetail.setTextColor(0xFFFF4444.toInt())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
