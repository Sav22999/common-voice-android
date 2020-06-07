package org.commonvoice.saverio.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.commonvoice.saverio.utils.sharedStateViewModel
import org.commonvoice.saverio_lib.viewmodels.ListenViewModel

class ListenDialogFragment: BottomSheetDialogFragment() {

    private val listenViewModel: ListenViewModel by sharedStateViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState) //TODO set view here
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO add here button listeners
    }

}