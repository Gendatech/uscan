package genda.uscan.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import genda.uscan.App
import genda.uscan.R
import genda.uscan.databinding.FragmentUscanBinding
import genda.uscan.utils.Logger

class UscanFragment : Fragment() {

    // region Data Members

    private var _binding: FragmentUscanBinding? = null

    // endregion

    // region Life Cycle

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment.
        _binding = FragmentUscanBinding.inflate(inflater)

        return _binding?.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize fragment views.
        setupViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // endregion

    // region Private Methods

    /**
     * Initialize the fragment views.
     */
    private fun setupViews() {

        if (!App.get().isAllNeededPermissionsGranted()) {

            Logger.d("why this not working")
            findNavController().navigate(R.id.PermissionFragment)
        }

        _binding?.buttonFirst?.setOnClickListener { view ->
            App.get().startUscanService()
        }

    }

    // endregion
}