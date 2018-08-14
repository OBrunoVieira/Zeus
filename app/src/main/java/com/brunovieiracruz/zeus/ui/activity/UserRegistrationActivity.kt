package com.brunovieiracruz.zeus.ui.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.brunovieiracruz.zeus.R
import com.brunovieiracruz.zeus.model.data.Fingerprint
import com.brunovieiracruz.zeus.model.data.Fingerprint.Companion.AUTHENTICATED
import com.brunovieiracruz.zeus.model.data.Fingerprint.Companion.ERROR
import com.brunovieiracruz.zeus.model.data.Fingerprint.Companion.NOT_SUPPORTED
import com.brunovieiracruz.zeus.ui.view_model.UserRegistrationViewModel


class UserRegistrationActivity : BaseActivity() {
    private lateinit var viewContentRoot: View
    private lateinit var textViewFingerPrintHint: TextView
    private lateinit var textViewFingerPrintError: TextView
    private lateinit var viewModel: UserRegistrationViewModel

    private val observer = Observer<Fingerprint> {
        when (it?.status) {
            AUTHENTICATED -> {
                startActivity(Intent(this, AuthUserActivity::class.java))
            }

            NOT_SUPPORTED -> {
                Snackbar.make(viewContentRoot, "Fingerprint authentication is not supported.", Snackbar.LENGTH_SHORT).show()
            }

            ERROR -> {
                textViewFingerPrintError.text = it.stringError
                textViewFingerPrintError.visibility = VISIBLE

                val objectAnimatorAngle = ObjectAnimator
                        .ofFloat(textViewFingerPrintHint, "rotation", -3f, 3f, 0f).apply {
                            repeatCount = 3
                            duration = 100
                        }

                textViewFingerPrintError.postDelayed({
                    textViewFingerPrintError.visibility = GONE

                }, 2000)

                AnimatorSet().apply {
                    play(objectAnimatorAngle)
                    start()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_registration)

        viewContentRoot = findViewById(R.id.activity_user_registration_content_root)
        textViewFingerPrintHint = findViewById(R.id.activity_user_registration_text_view_fingerprint_hint)
        textViewFingerPrintError = findViewById(R.id.activity_user_registration_text_view_fingerprint_error)

        val factory = UserRegistrationViewModel.Factory()
        viewModel = ViewModelProviders.of(this, factory).get(UserRegistrationViewModel::class.java)
        viewModel.observableFingerPrint.observe(this, observer)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.observableFingerPrint.removeObserver(observer)
    }

    override fun onResume() {
        super.onResume()
        viewModel.startListeningFingerprint()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopListeningFingerprint()
    }
}
