package org.commonvoice.saverio.ui.viewBinding

import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import org.commonvoice.saverio.ui.VariableLanguageActivity

abstract class ViewBoundActivity<T: ViewBinding> : VariableLanguageActivity() {

    private var _binding: T? = null

    protected val binding: T get() = _binding!!

    abstract fun inflateView(layoutInflater: LayoutInflater): T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = inflateView(layoutInflater)
        setContentView(_binding!!.root)
    }

    override fun onDestroy() {
        _binding = null

        super.onDestroy()
    }

    protected inline fun withBinding(body: T.() -> Unit) {
        binding.body()
    }

}