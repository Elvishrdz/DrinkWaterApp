import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity
@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
data class HistoryEntity(
    @PrimaryKey val id: String = Uuid.random().toString(),
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?
)
