package org.commonvoice.saverio_lib.dataClasses

sealed class BadgeDialogMediator {
    object Speak: BadgeDialogMediator()
    object Listen: BadgeDialogMediator()
    object Level: BadgeDialogMediator()
}