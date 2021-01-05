package org.commonvoice.saverio.ui.viewBinding

import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import org.commonvoice.saverio.ui.VariableLanguageActivity

abstract class ViewBoundActivity<T: ViewBinding>(
    private val inflate: (LayoutInflater) -> T
) : VariableLanguageActivity() {

    private var _binding: T? = null

    protected val binding: T get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = inflate(layoutInflater)
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