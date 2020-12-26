package org.commonvoice.saverio.ui.login

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.FragmentNoconnectionBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio_lib.api.network.ConnectionManager
import org.koin.android.ext.android.inject

class NoConnectionFragment : ViewBoundFragment<FragmentNoconnectionBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNoconnectionBinding {
        return FragmentNoconnectionBinding.inflate(layoutInflater, container, false)
    }

    private val connectionManager by inject<ConnectionManager>()

    override fun onStart() {
        super.onStart()

        checkConnection()

        binding.btnCheckAgain.setOnClickListener {
            checkConnection()
        }

        setTheme()
    }

    private fun setTheme() {
        theme.setElement(binding.layoutNoConnection)
        theme.setElement(requireContext(), binding.btnCheckAgain)
        theme.setElement(
            requireContext(),
            binding.txtNoInternetConnection,
            R.color.colorAlertMessage,
            R.color.colorAlertMessageDT
        )
    }

    private fun checkConnection() {
        if (connectionManager.isInternetAvailable) {
            findNavController().navigateUp()
        }
    }

}