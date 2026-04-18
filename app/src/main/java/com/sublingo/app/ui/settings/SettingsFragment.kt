package com.sublingo.app.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sublingo.app.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get version from PackageManager (no BuildConfig needed)
        val version = try {
            val pInfo = requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
        binding.tvVersion.text = "Version $version"

        binding.rowAbout.setOnClickListener {
            startActivity(
                Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/elephantcos-cloud"))
            )
        }

        binding.rowGithub.setOnClickListener {
            startActivity(
                Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/elephantcos-cloud/sublingo-app"))
            )
        }

        binding.rowOutputDir.setOnClickListener {
            val dir = requireContext()
                .getExternalFilesDir(null)
                ?.resolve("SubLingo")
            android.widget.Toast.makeText(
                requireContext(),
                "Output: ${dir?.absolutePath}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
