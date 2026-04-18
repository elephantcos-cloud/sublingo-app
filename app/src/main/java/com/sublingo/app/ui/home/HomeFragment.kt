package com.sublingo.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sublingo.app.R
import com.sublingo.app.databinding.FragmentHomeBinding
import com.sublingo.app.ui.history.HistoryAdapter
import java.io.File

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = HistoryAdapter(
            onShare = { entity ->
                val file = File(entity.outputPath)
                if (file.exists()) {
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        requireContext(), "${requireContext().packageName}.fileprovider", file)
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(android.content.Intent.createChooser(intent, "Share subtitle"))
                }
            },
            onDelete = {}
        )
        binding.rvRecent.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecent.adapter = adapter

        viewModel.recentHistory.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvNoRecent.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            binding.rvRecent.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
        }

        binding.cardFileTranslate.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_fileTranslate)
        }
        binding.cardVideoTranslate.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_video)
        }
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
