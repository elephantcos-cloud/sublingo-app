package com.sublingo.app.ui.video

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.sublingo.app.databinding.FragmentVideoBinding

class VideoFragment : Fragment() {

    private var _binding: FragmentVideoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VideoViewModel by viewModels()
    private var player: ExoPlayer? = null
    private var sourceLang = "en"
    private var targetLang = "bn"

    private val videoPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            loadVideo(uri)
        }
    }

    private val subtitlePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            loadSubtitleFile(uri)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPlayer()
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnPickVideo.setOnClickListener {
            videoPicker.launch(Intent(Intent.ACTION_GET_CONTENT).apply { type = "video/*" })
        }
        binding.btnPickSubtitle.setOnClickListener {
            subtitlePicker.launch(Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" })
        }

        viewModel.currentSubtitle.observe(viewLifecycleOwner) { text ->
            binding.tvSubtitle.text = text
            binding.tvSubtitle.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isTranslatorReady.observe(viewLifecycleOwner) { ready ->
            binding.tvSubtitleStatus.text = if (ready) "Translator ready" else "Downloading model..."
        }
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(requireContext()).build().also { exo ->
            binding.playerView.player = exo
            exo.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) startSubtitleUpdater()
                }
            })
        }
    }

    private fun startSubtitleUpdater() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                player?.let { p ->
                    viewModel.updateSubtitleForPosition(p.currentPosition)
                    if (p.isPlaying) handler.postDelayed(this, 200)
                }
            }
        }
        handler.post(runnable)
    }

    private fun loadVideo(uri: Uri) {
        player?.setMediaItem(MediaItem.fromUri(uri))
        player?.prepare()
        binding.btnPickSubtitle.visibility = View.VISIBLE
        binding.tvVideoHint.visibility = View.GONE
    }

    private fun loadSubtitleFile(uri: Uri) {
        val content = requireContext().contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: return
        val name = uri.lastPathSegment ?: ""
        val isVtt = name.endsWith(".vtt", ignoreCase = true)
        viewModel.loadSubtitle(content, isVtt, sourceLang, targetLang)
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player?.release()
        player = null
        _binding = null
    }
}
