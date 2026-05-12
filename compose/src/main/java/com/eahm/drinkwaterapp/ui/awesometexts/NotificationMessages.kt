package com.eahm.drinkwaterapp.ui.awesometexts

import android.content.Context
import com.eahm.drinkwaterapp.R
import kotlin.random.Random

class NotificationMessages {

    sealed interface Category {
        data object Start : Category
        data object End : Category
        data class ActiveHours(val hour: Int) : Category
    }

    fun getTitle(
        category: Category,
        context: Context
    ): String? {
        return when (category) {
            Category.Start -> {
                val randomPos1 = Random.nextInt(9)
                when (randomPos1) {
                    1 -> context.getString(R.string.title_start_01)
                    2 -> context.getString(R.string.title_start_02)
                    3 -> context.getString(R.string.title_start_03)
                    4 -> context.getString(R.string.title_start_04)
                    5 -> context.getString(R.string.title_start_05)
                    6 -> context.getString(R.string.title_start_06)
                    7 -> context.getString(R.string.title_start_07)
                    else -> context.getString(R.string.title_start_default)
                }
            }

            Category.End -> {
                //region TITULOS PARA LAST ALARM
                val randomPos2 = Random.nextInt(4)
                when (randomPos2) {
                    1 -> context.getString(R.string.title_end_01)
                    2 -> context.getString(R.string.title_end_02)
                    3 -> context.getString(R.string.title_end_03)
                    else -> context.getString(R.string.title_end_default)
                }
            }

            is Category.ActiveHours -> {
                determineMessageByHourOfDay(category.hour)
            }
        }
    }


    private fun determineMessageByHourOfDay(hour: Int): String {
        return when (hour) {
            in 0..<4 -> {
                "0-4"
            }

            in 4..<6 -> {
                "4-6"
            }

            in 6..<8 -> {
                "6-8"
            }

            in 8..<9 -> {
                "8-9"
            }

            in 9..<10 -> {
                "9-10"
            }

            in 10..<12 -> {
                "10-12"
            }

            in 12..<13 -> {
                "12-13"
            }

            in 13..<15 -> {
                "13-15"
            }

            in 15..<16 -> {
                "15-16"
            }

            in 16..<18 -> {
                "16-18"
            }

            in 18..<20 -> {
                "18-20"
            }

            in 20..<22 -> {
                "20-22"
            }

            else -> {
                "22-0"
            }
        }
    }

    private fun Context.getRandomMessage() {
        val randomPos3 = Random.nextInt(5)
        when (randomPos3) {
            1 -> getString(R.string.title_drink_time_01)
            2 -> getString(R.string.title_drink_time_03)
            3 -> getString(R.string.title_drink_time_04)
            else -> getString(R.string.title_drink_time_default)
        }
    }
}
