package org.commonvoice.saverio_lib.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

fun <T1, T2> combineLiveData(
    f1: LiveData<T1>,
    f2: LiveData<T2>,
    default1: T1,
    default2: T2
): LiveData<Pair<T1, T2>> = MediatorLiveData<Pair<T1, T2>>().also { mediator ->
    mediator.value = Pair(f1.value ?: default1, f2.value ?: default2)

    mediator.addSource(f1) { t1: T1 ->
        val (_, t2) = mediator.value!!
        mediator.value = Pair(t1, t2)
    }

    mediator.addSource(f2) { t2: T2 ->
        val (t1, _) = mediator.value!!
        mediator.value = Pair(t1, t2)
    }
}