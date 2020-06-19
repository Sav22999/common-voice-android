package org.commonvoice.saverio.ui.viewBinding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class ViewBoundFragment<T: ViewBinding> : Fragment() {

    private var _binding: T? = null

    protected val binding: T get() = _binding!!

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

}