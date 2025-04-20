package com.example.evaluation3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.TextView

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return TextView(context).apply {
            text = "Settings Placeholder"
            setTextColor(android.graphics.Color.WHITE)
            textSize = 20f
            setPadding(16, 16, 16, 16)
        }
    }
}