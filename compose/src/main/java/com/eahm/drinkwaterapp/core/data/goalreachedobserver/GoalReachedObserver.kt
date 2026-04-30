package com.eahm.drinkwaterapp.core.data.goalreachedobserver

import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val goalReachedObserverModule: Module = module {
    singleOf(::GoalReachedObserver)
}

class GoalReachedObserver(
    private val prefsStorage: PrefsStorage,
) {

    /**
     * Returns a [Flow] that emits [Unit] exactly once per goal-crossing transition
     * (not-reached → reached).
     *
     * - Does NOT fire on cold subscription if the goal is already met.
     * - Re-fires if the goal is raised above the current intake and then crossed again.
     * - Safe across midnight: when [drinkAmountFlow] resets to 0 for a new day,
     *   the state transitions to not-reached without emitting, and the next crossing emits.
     *
     * @param drinkAmountFlow reactive total intake for the observed period (today).
     */
    fun observe(drinkAmountFlow: Flow<Float>): Flow<Unit> =
        combine(prefsStorage.getGoalAmount(), drinkAmountFlow) { goal, amount ->
            amount >= goal
        }
            .distinctUntilChanged()
            .drop(1)        // skip the initial state so a cold-start with goal already met does not re-fire
            .filter { it }  // only pass not-reached → reached transitions
            .map { }
}
