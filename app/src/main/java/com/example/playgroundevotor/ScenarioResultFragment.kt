package com.example.playgroundevotor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.playgroundevotor.databinding.FragmentScenarioResultBinding

class ScenarioResultFragment : Fragment() {

    private var _binding: FragmentScenarioResultBinding? = null
    private val binding: FragmentScenarioResultBinding
        get() = _binding ?: error("Binding is only valid between onCreateView and onDestroyView.")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScenarioResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.resultTitle.text = requireArguments().getString(ARG_TITLE).orEmpty()
        binding.resultMessage.text = requireArguments().getString(ARG_MESSAGE).orEmpty()
        binding.closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ScenarioResultFragment"

        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"

        fun newInstance(title: String, message: String) = ScenarioResultFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_MESSAGE, message)
            }
        }
    }
}
