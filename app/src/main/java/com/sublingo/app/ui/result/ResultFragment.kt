package com.sublingo.app.ui.result

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sublingo.app.R
import com.sublingo.app.databinding.FragmentResultBinding
import com.sublingo.app.translator.MLKitTranslator
import java.io.File

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private val args: ResultFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val outputFile = File(args.outputPath)
        val langName = MLKitTranslator.LANGUAGES
            .find { it.first == args.targetLang }?.second ?: args.targetLang

        binding.tvLineCount.text = args.lineCount.toString() + " lines translated"
        binding.tvLangInfo.text = "Language: " + langName
        binding.tvFileName.text = outputFile.name

        if (outputFile.exists()) {
            val lines = outputFile.readLines().take(10)
            val preview = lines.joinToString(separator = System.lineSeparator())
            binding.tvPreview.text = preview
        }

        binding.btnDone.setOnClickListener {
            findNavController().navigate(R.id.action_result_to_home)
        }

        binding.btnShare.setOnClickListener { shareFile(outputFile) }
        binding.btnOpen.setOnClickListener { openFile(outputFile) }
    }

    private fun shareFile(file: File) {
        if (!file.exists()) return
        val uri = FileProvider.getUriForFile(
            requireContext(),
            requireContext().packageName + ".fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share Subtitle"))
    }

    private fun openFile(file: File) {
        if (!file.exists()) return
        val uri = FileProvider.getUriForFile(
            requireContext(),
            requireContext().packageName + ".fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "text/plain")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
