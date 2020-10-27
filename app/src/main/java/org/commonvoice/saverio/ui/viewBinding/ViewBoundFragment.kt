package org.commonvoice.saverio.ui.viewBinding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import org.commonvoice.saverio.DarkLightTheme
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.koin.android.ext.android.inject

abstract class ViewBoundFragment<T: ViewBinding> : Fragment() {

    private var _binding: T? = null

    protected val binding: T get() = _binding!!

    protected val theme: DarkLightTheme by inject()

    private val _mainPrefManager by inject<MainPrefManager>()

    abstract fun inflate(layoutInflater: LayoutInflater, container: ViewGroup?): T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = inflate(inflater, container)
        return _binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    protected inline fun withBinding(body: T.() -> Unit) {
        binding.body()
    }

    protected fun startAnimation(view: View, @AnimRes res: Int) {
        if (_mainPrefManager.areAnimationsEnabled) {
            AnimationUtils.loadAnimation(requireContext(), res).let {
                view.startAnimation(it)
            }
        }
    }

    protected fun stopAnimation(view: View) {
        view.clearAnimation()
    }

}