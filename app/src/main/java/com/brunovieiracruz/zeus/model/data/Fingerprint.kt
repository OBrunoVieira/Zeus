package com.brunovieiracruz.zeus.model.data

class Fingerprint(val status: Int, var stringError: String = "") {
    companion object {
        const val AUTHENTICATED = 1111
        const val NOT_SUPPORTED = 3333
        const val ERROR = 4444
    }
}