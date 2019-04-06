package com.anwesh.uiprojects.linkedhalfarcfillstepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.halfarcfillstepview.HalfArcFillStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HalfArcFillStepView.create(this)
    }
}
