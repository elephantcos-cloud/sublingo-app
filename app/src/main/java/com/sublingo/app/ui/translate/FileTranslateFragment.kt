package com.sublingo.app.ui.translate

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.sublingo.app.R
import com.sublingo.app.data.model.FileType
import com.sublingo.app.databinding.FragmentFileTranslateBinding
import com.sublingo.app.translator.MLKitTranslator

class FileTranslateFragment : Fragment() {

    private var _binding: FragmentFileTranslateBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FileTranslateViewModel by viewModels()

    private val langNames by lazy { MLKitTranslator.LANGUAGES.map { it.second } }
    private val langCodes by lazy { MLKitTranslator.LANGUAGES.map { it.first } }

    private val filePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            val name = getFileName(uri) ?: "subtitle.srt"
            val ext = name.substringAfterLast(".").lowercase()
            if (ext != "srt" && ext != "vtt") {
                Toast.makeText(requireContext(), "Please select SRT or VTT file", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            viewModel.selectedUri.value = uri
            viewModel.selectedFileName.value = name
            viewModel.selectedFileType.value = if (ext == "vtt") FileType.VTT else FileType.SRT
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFileTranslateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, langNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSource.adapter = adapter
        binding.spinnerTarget.adapter = adapter

        val srcIdx = langCodes.indexOf("en").takeIf { it >= 0 } ?: 0
        val tgtIdx = langCodes.indexOf("bn").takeIf { it >= 0 } ?: 2
        binding.spinnerSource.setSelection(srcIdx)
        binding.spinnerTarget.setSelection(tgtIdx)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.uploadZone.setOnClickListener { openFilePicker() }
        binding.btnPickFile.setOnClickListener { openFilePicker() }

        binding.btnSwap.setOnClickListener {
            val srcPos = binding.spinnerSource.selectedItemPosition
            val tgtPos = binding.spinnerTarget.selectedItemPosition
            binding.spinnerSource.setSelection(tgtPos)
            binding.spinnerTarget.setSelection(srcPos)
        }

        viewModel.selectedFileName.observe(viewLifecycleOwner) { name ->
            binding.tvFileName.text = name
            binding.tvFileName.visibility = View.VISIBLE
            binding.tvUploadHint.visibility = View.GONE
            binding.ivUploadIcon.setImageResource(R.drawable.ic_file_selected)
            updateTranslateButton()
        }

        binding.btnTranslate.setOnClickListener {
            val uri = viewModel.selectedUri.value ?: return@setOnClickListener
            val srcLang = langCodes[binding.spinnerSource.selectedItemPosition]
            val tgtLang = langCodes[binding.spinnerTarget.selectedItemPosition]
            if (srcLang == tgtLang) {
                Toast.makeText(requireContext(), "Source and target language must be different", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val action = FileTranslateFragmentDirections.actionFileTranslateToProgress(
                fileUri = uri.toString(),
                fileName = viewModel.selectedFileName.value ?: "subtitle.srt",
                fileType = viewModel.selectedFileType.value?.name ?: "SRT",
                sourceLang = srcLang,
                targetLang = tgtLang
            )
            findNavController().navigate(action)
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/plain", "application/x-subrip", "*/*"))
        }
        filePicker.launch(intent)
    }

    private fun getFileName(uri: android.net.Uri): String? {
        var name: String? = null
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && idx >= 0) name = cursor.getString(idx)
        }
        return name ?: uri.lastPathSegment
    }

    private fun updateTranslateButton() {
        binding.btnTranslate.isEnabled = viewModel.selectedUri.value != null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
