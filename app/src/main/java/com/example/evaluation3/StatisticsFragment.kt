package com.example.evaluation3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.TextView

class StatisticsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return TextView(context).apply {
            text = "Statistics Placeholder"
            setTextColor(android.graphics.Color.WHITE)
            textSize = 20f
            setPadding(16, 16, 16, 16)
        }
    }
}