package org.commonvoice.saverio.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.koin.getStateViewModel
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import kotlin.reflect.KClass

//TODO remove this when Koin gets updated to version 2.1.6, as this should get included in the update

inline fun <reified T : ViewModel> Fragment.sharedStateViewModel(
    qualifier: Qualifier? = null,
    bundle: Bundle? = null,
    noinline parameters: ParametersDefinition? = null
): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE) { getSharedStateViewModel<T>(qualifier, bundle, parameters) }

fun <T : ViewModel> Fragment.sharedStateViewModel(
    clazz: KClass<T>,
    qualifier: Qualifier? = null,
    bundle: Bundle? = null,
    parameters: ParametersDefinition? = null
): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE) {
        getSharedStateViewModel(
            clazz,
            qualifier,
            bundle,
            parameters
        )
    }

inline fun <reified T : ViewModel> Fragment.getSharedStateViewModel(
    qualifier: Qualifier? = null,
    bundle: Bundle? = null,
    noinline parameters: ParametersDefinition? = null
): T {
    return getSharedStateViewModel(T::class, qualifier, bundle, parameters)
}

fun <T : ViewModel> Fragment.getSharedStateViewModel(
    clazz: KClass<T>,
    qualifier: Qualifier? = null,
    bundle: Bundle? = null,
    parameters: ParametersDefinition? = null
): T {
    val bundleOrDefault: Bundle = bundle ?: Bundle()
    return getKoin().getStateViewModel(
        requireActivity(),
        clazz,
        qualifier,
        bundleOrDefault,
        parameters
    )
}