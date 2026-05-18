package com.eahm.drinkwaterapp.ui.awesometexts

import kotlin.random.Random

// TODO: display messages with the amount of drinks so far, goal and remaining to reach the goal.
// TODO: translate texts
class NotificationTextMessages {

    fun getText(
        category: MessageCategory,
    ): String? {
        return when (category) {
            MessageCategory.Start -> determineByStart()
            MessageCategory.End -> determineByEnd()
            is MessageCategory.ActiveHours -> determineByHoursOfDay(category.hour)
        }
    }

    private fun determineByStart(): String {
        val random = Random.nextInt(until = 4)
        return when (random) {
            1 -> "Listos para empezar un vaso a la vez"
            2 -> "Un nuevo periodo empieza ahora"
            else -> "Empecemos con un refrescante vaso de agua"
        }
    }

    private fun determineByEnd(): String {
        val random = Random.nextInt(until = 4)
        return when (random) {
            1 -> "Este es el ultimo recordatorio"
            2 -> "Terminemos con un vaso de agua mas"
            else -> "Periodo completado, abre la app para ver como van tus registros"
        }
    }

    private fun determineByHoursOfDay(hour: Int): String {
        val random = Random.nextInt(until = 3)
        return when (random) {
            1 -> determineByHoursOfDayGroup1(hour)
            else -> determineByHoursOfDayGroup2(hour)
        }
    }

    private fun determineByHoursOfDayGroup1(hour: Int): String {
        return when (MessageRanges.getByHourOfDay(hour)) {
            MessageRanges.FROM_0_UNTIL_4 -> "Even at night, your body needs water"
            MessageRanges.FROM_4_UNTIL_6 -> "Kick off your morning with a glass of water"
            MessageRanges.FROM_6_UNTIL_8 -> "A glass of water gets your metabolism going"
            MessageRanges.FROM_8_UNTIL_9 -> "Hydrate before your day picks up"
            MessageRanges.FROM_9_UNTIL_10 -> "Keep sipping to stay sharp and focused"
            MessageRanges.FROM_10_UNTIL_12 -> "Don't let the morning pass without hydrating"
            MessageRanges.FROM_12_UNTIL_13 -> "Pair your lunch with a glass of water"
            MessageRanges.FROM_13_UNTIL_15 -> "Stay sharp through the afternoon with water"
            MessageRanges.FROM_15_UNTIL_16 -> "A glass of water can lift your energy right now"
            MessageRanges.FROM_16_UNTIL_18 -> "Finishing up? Drink some water before you head out"
            MessageRanges.FROM_18_UNTIL_20 -> "Relax and rehydrate after a busy day"
            MessageRanges.FROM_20_UNTIL_22 -> "One more glass helps your body wind down"
            MessageRanges.FROM_22_UNTIL_0 -> "A small glass before bed keeps you refreshed overnight"
        }
    }

    private fun determineByHoursOfDayGroup2(hour: Int): String {
        return when (MessageRanges.getByHourOfDay(hour)) {
            MessageRanges.FROM_0_UNTIL_4 -> "Sed a estas horas? Vamos por un sip"
            MessageRanges.FROM_4_UNTIL_6 -> "A estas horas tempranas, un poco de agua puede ser refrescante"
            MessageRanges.FROM_6_UNTIL_8 -> "El cafecito o un vaso de agua? dificil decision"
            MessageRanges.FROM_8_UNTIL_9 -> "Hidratarse a horas tempranas puede ayudarte en tu inicio del dia"
            MessageRanges.FROM_9_UNTIL_10 -> "Recuerda mantenerte hidratado"
            MessageRanges.FROM_10_UNTIL_12 -> "Otro vaso antes del almuerzo?"
            MessageRanges.FROM_12_UNTIL_13 -> "A mitad del dia vamos por mas registros!"
            MessageRanges.FROM_13_UNTIL_15 -> "A estas horas de la tarde recuerda mantenerte hidratado"
            MessageRanges.FROM_15_UNTIL_16 -> "Que tal otro vaso de agua antes que termine la tarde"
            MessageRanges.FROM_16_UNTIL_18 -> "No se trata de alcanzar una meta, se trata de recordar tomar agua"
            MessageRanges.FROM_18_UNTIL_20 -> "Hora de relajarse y de ir por otro vaso"
            MessageRanges.FROM_20_UNTIL_22 -> "En la noche toma agua de forma moderada"
            MessageRanges.FROM_22_UNTIL_0 -> "Ya cenaste? un vaso de agua ayuda a tu digestion."
        }
    }
}
