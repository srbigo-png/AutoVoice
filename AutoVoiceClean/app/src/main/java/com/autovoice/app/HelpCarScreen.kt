package com.autovoice.app

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*

class HelpCarScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val items = ItemList.Builder()
            .addItem(Row.Builder().setTitle("Navigation").addText("Say: Navigate to [address]").build())
            .addItem(Row.Builder().setTitle("Call").addText("Say: Call [name]").build())
            .addItem(Row.Builder().setTitle("Search").addText("Say: Search [place]").build())
            .addItem(Row.Builder().setTitle("Open Waze").addText("Say: Open Waze").build())
            .build()

        return ListTemplate.Builder()
            .setTitle("Voice Commands")
            .setHeaderAction(Action.BACK)
            .setSingleList(items)
            .build()
    }
}
