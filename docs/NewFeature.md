WIP 
Alarm Notification


An alarm that displays a notification to the user.


Use cases
1. For testing we will start the alarm flow using a dedicated button. in the future an onboarding will setup everything
including the first alarm.
2. User taps "drink" button, and no alarm setup is active:
2.1. if the user is drinking with the initial scheduled plan, we track drinks as usual and we setup the next alarm time based 
on the initial schedule. we need a threshold, if the user drinks too early or too late, we need to reschedule new drink times.
2.2. Reschedule: We calculate a new schedule starting from the time the user has drank water. We check the remaining amount of
water to drink and use it as our goal (for the new calculation) we adjust to the current endTime 
3. User taps "drink" button, and an alarm is active: alarms calculate the next drink time
3. When a new day starts, we calculate the drink times in the active hours. DrinkAlarmCalculator2.kt contains the logic to
generate the alarm schedule. 
