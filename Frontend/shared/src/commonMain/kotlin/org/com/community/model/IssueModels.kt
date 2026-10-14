package org.com.community.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.longOrNull

fun parseIsoToEpochMillis(isoString: String): Long {
    val trimmed = isoString.trim()
    if (trimmed.isEmpty()) return 0L
    trimmed.toLongOrNull()?.let { return it }

    return try {
        val tIdx = trimmed.indexOf('T')
        if (tIdx < 10) return 0L

        val datePart = trimmed.substring(0, tIdx)
        val dateComponents = datePart.split("-")
        if (dateComponents.size < 3) return 0L

        val year = dateComponents[0].toIntOrNull() ?: return 0L
        val month = dateComponents[1].toIntOrNull() ?: return 0L
        val day = dateComponents[2].toIntOrNull() ?: return 0L

        var timePart = trimmed.substring(tIdx + 1)
        val zIdx = timePart.indexOf('Z')
        if (zIdx != -1) timePart = timePart.substring(0, zIdx)
        val plusIdx = timePart.indexOf('+')
        if (plusIdx != -1) timePart = timePart.substring(0, plusIdx)

        val timeComponents = timePart.split(":")
        val hour = if (timeComponents.isNotEmpty()) timeComponents[0].toIntOrNull() ?: 0 else 0
        val minute = if (timeComponents.size > 1) timeComponents[1].toIntOrNull() ?: 0 else 0

        var second = 0
        var millis = 0
        if (timeComponents.size > 2) {
            val secPart = timeComponents[2]
            val dotIdx = secPart.indexOf('.')
            if (dotIdx != -1) {
                second = secPart.substring(0, dotIdx).toIntOrNull() ?: 0
                val frac = secPart.substring(dotIdx + 1)
                val padded = (frac + "000000000").substring(0, 3)
                millis = padded.toIntOrNull() ?: 0
            } else {
                second = secPart.toIntOrNull() ?: 0
            }
        }

        computeEpochMillis(year, month, day, hour, minute, second, millis)
    } catch (e: Exception) {
        0L
    }
}

private fun computeEpochMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int, millis: Int): Long {
    val y = year - 1
    val leapDays = (y / 4) - (y / 100) + (y / 400) - ((1969 / 4) - (1969 / 100) + (1969 / 400))
    val daysBeforeYear = (year - 1970) * 365L + leapDays

    val isLeap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    val daysInMonths = if (isLeap) {
        intArrayOf(0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335)
    } else {
        intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
    }

    val mIdx = (month - 1).coerceIn(0, 11)
    val totalDays = daysBeforeYear + daysInMonths[mIdx] + (day - 1)

    val totalSeconds = totalDays * 86400L + hour * 3600L + minute * 60L + second
    return totalSeconds * 1000L + millis
}

fun formatEpochToIso(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    val totalSec = epochMillis / 1000L
    val millis = (epochMillis % 1000L).toInt()

    val days = (totalSec / 86400L).toInt()
    var remSec = (totalSec % 86400L).toInt()
    if (remSec < 0) remSec += 86400

    val hours = remSec / 3600
    remSec %= 3600
    val minutes = remSec / 60
    val seconds = remSec % 60

    val z = days + 719468
    val era = (if (z >= 0) z else z - 146096) / 146097
    val doe = z - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    val m = mp + (if (mp < 10) 3 else -9)
    val year = y + (if (m <= 2) 1 else 0)

    val yStr = year.toString().padStart(4, '0')
    val mStr = m.toString().padStart(2, '0')
    val dStr = d.toString().padStart(2, '0')
    val hStr = hours.toString().padStart(2, '0')
    val minStr = minutes.toString().padStart(2, '0')
    val sStr = seconds.toString().padStart(2, '0')
    val msStr = millis.toString().padStart(3, '0')

    return "${yStr}-${mStr}-${dStr}T${hStr}:${minStr}:${sStr}.${msStr}Z"
}

object IsoTimestampSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IsoTimestampSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Long) {
        if (value == 0L) {
            encoder.encodeString("")
        } else {
            encoder.encodeString(formatEpochToIso(value))
        }
    }

    override fun deserialize(decoder: Decoder): Long {
        if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()
            if (element is JsonPrimitive) {
                if (element.isString) {
                    return parseIsoToEpochMillis(element.content)
                } else {
                    return element.longOrNull ?: 0L
                }
            }
        }
        return try {
            val str = decoder.decodeString()
            parseIsoToEpochMillis(str)
        } catch (e: Exception) {
            0L
        }
    }
}

@Serializable
enum class IssueCategory(val displayName: String, val iconName: String) {
    WATER("Water and Sanitation", "water_drop"),
    ELECTRICITY("Electricity and Power", "electric_bolt"),
    ROADS("Roads and Transport", "add_road"),
    SANITATION("Sanitation and Hygiene", "clean_hands"),
    WASTE("Waste and Environment", "delete"),
    FACILITIES("Public Facilities", "domain"),
    SAFETY("Safety and Security", "security"),
    INTERNET("Communication and Internet", "wifi"),
    BUILDINGS("Buildings and Public Property", "domain"),
    OTHER("Other", "more_horiz")
}

fun parseIssueCategory(catStr: String?): IssueCategory {
    if (catStr.isNullOrBlank()) return IssueCategory.OTHER
    val upper = catStr.trim().uppercase()
    return try {
        IssueCategory.valueOf(upper)
    } catch (e: Exception) {
        when {
            upper.contains("WATER") -> IssueCategory.WATER
            upper.contains("ELECTRIC") || upper.contains("POWER") -> IssueCategory.ELECTRICITY
            upper.contains("ROAD") || upper.contains("TRANSPORT") -> IssueCategory.ROADS
            upper.contains("SANITA") || upper.contains("HYGIENE") -> IssueCategory.SANITATION
            upper.contains("WASTE") || upper.contains("ENVIRON") -> IssueCategory.WASTE
            upper.contains("FACILITY") || upper.contains("FACILITIES") -> IssueCategory.FACILITIES
            upper.contains("SAFETY") || upper.contains("SECURITY") -> IssueCategory.SAFETY
            upper.contains("INTERNET") || upper.contains("COMMUNICAT") || upper.contains("WIFI") -> IssueCategory.INTERNET
            upper.contains("BUILDING") -> IssueCategory.BUILDINGS
            else -> IssueCategory.OTHER
        }
    }
}

@Serializable
enum class IssueStatus {
    DRAFT,
    SUBMITTED,
    RECEIVED,
    UNDER_REVIEW,
    VERIFIED,
    ASSIGNED,
    IN_PROGRESS,
    WAITING_FOR_ACTION,
    RESOLVED,
    COMMUNITY_VERIFIED,
    CLOSED,
    REJECTED,
    DUPLICATE
}

fun parseIssueStatus(statusStr: String?): IssueStatus {
    if (statusStr.isNullOrBlank()) return IssueStatus.SUBMITTED
    return try {
        IssueStatus.valueOf(statusStr.trim().uppercase())
    } catch (e: Exception) {
        IssueStatus.SUBMITTED
    }
}

@Serializable
enum class IssuePriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

fun parseIssuePriority(priorityStr: String?): IssuePriority {
    if (priorityStr.isNullOrBlank()) return IssuePriority.MEDIUM
    return try {
        IssuePriority.valueOf(priorityStr.trim().uppercase())
    } catch (e: Exception) {
        IssuePriority.MEDIUM
    }
}

@Serializable
data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val street: String? = null,
    val ward: String? = null,
    val district: String? = null,
    val region: String? = null,
    val nearbyLandmark: String? = null,
    val accuracy: Float? = null
)

@Serializable
data class IssueReport(
    val id: String = "",
    val universityId: Long? = null,
    val universityName: String? = null,
    val campusName: String? = null,
    val category: IssueCategory = IssueCategory.OTHER,
    val subCategory: String? = null,
    val description: String = "",
    val voiceTranscription: String? = null,
    val issueLocation: GeoLocation = GeoLocation(0.0, 0.0),
    val reporterLocation: GeoLocation = GeoLocation(0.0, 0.0),
    val distanceToIssue: Double = 0.0,
    val distanceText: String = "",
    val evidenceUris: List<String> = emptyList(),
    val videoUri: String? = null,
    val status: IssueStatus = IssueStatus.SUBMITTED,
    val routingStatus: String = "UNASSIGNED",
    val urgency: String = "NORMAL",
    val locationSource: String = "CURRENT_LOCATION",
    val userPriority: IssuePriority = IssuePriority.MEDIUM,
    val calculatedPriority: IssuePriority = IssuePriority.MEDIUM,
    val impactScore: Int = 0,
    val reporterId: String = "",
    val reporterName: String? = null,
    val reporterEmail: String? = null,
    val reporterPhone: String? = null,
    @Serializable(with = IsoTimestampSerializer::class)
    val timestamp: Long = 0,
    @Serializable(with = IsoTimestampSerializer::class)
    val lastUpdated: Long = 0,
    val assignedDepartment: String? = null,
    val assignedOfficerId: String? = null,
    val assignedLeaderName: String? = null,
    val supportsCount: Int = 0,
    val verificationCount: Int = 0,
    val isEmergency: Boolean = false,
    val timeline: List<IssueTimelineEvent> = emptyList(),
    val beforeImageUri: String? = null,
    val afterImageUri: String? = null,
    val publicResponse: String? = null,
    val internalNotes: String? = null
)

@Serializable
data class IssueReportResponse(
    val id: String = "",
    val dbId: Long? = null,
    val universityId: Long? = null,
    val universityName: String? = null,
    val campusName: String? = null,
    val category: String? = "OTHER",
    val subCategory: String? = null,
    val description: String = "",
    val voiceTranscription: String? = null,
    val status: String? = "SUBMITTED",
    val routingStatus: String? = "UNASSIGNED",
    val urgency: String? = "NORMAL",
    val userSeverity: String? = "MEDIUM",
    val calculatedPriority: String? = "MEDIUM",
    val impactScore: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String? = null,
    val street: String? = null,
    val ward: String? = null,
    val district: String? = null,
    val region: String? = null,
    val nearbyLandmark: String? = null,
    val issueLocation: GeoLocation? = null,
    val reporterLatitude: Double? = null,
    val reporterLongitude: Double? = null,
    val locationAccuracy: Float? = null,
    val reporterLocation: GeoLocation? = null,
    val evidenceUrls: List<String> = emptyList(),
    val evidenceUris: List<String> = emptyList(),
    val videoUri: String? = null,
    val locationSource: String? = "CURRENT_LOCATION",
    val reporterId: String? = null,
    val reporterName: String? = null,
    val reporterEmail: String? = null,
    val reporterPhone: String? = null,
    val supportsCount: Int = 0,
    val verificationCount: Int = 0,
    val isEmergency: Boolean = false,
    @Serializable(with = IsoTimestampSerializer::class)
    val timestamp: Long = 0,
    @Serializable(with = IsoTimestampSerializer::class)
    val lastUpdated: Long = 0,
    @Serializable(with = IsoTimestampSerializer::class)
    val updatedAt: Long = 0,
    val assignedDepartment: String? = null,
    val assignedOfficerId: String? = null,
    val assignedLeaderName: String? = null,
    val timeline: List<IssueTimelineEvent> = emptyList(),
    val beforeImageUri: String? = null,
    val afterImageUri: String? = null,
    val publicResponse: String? = null,
    val internalNotes: String? = null
)

fun IssueReportResponse.toDomain(): IssueReport {
    val effLat = if (issueLocation != null && (issueLocation.latitude != 0.0 || issueLocation.longitude != 0.0)) {
        issueLocation.latitude
    } else {
        latitude
    }
    val effLng = if (issueLocation != null && (issueLocation.latitude != 0.0 || issueLocation.longitude != 0.0)) {
        issueLocation.longitude
    } else {
        longitude
    }

    val effIssueLoc = GeoLocation(
        latitude = effLat,
        longitude = effLng,
        address = address ?: issueLocation?.address,
        street = street ?: issueLocation?.street,
        ward = ward ?: issueLocation?.ward,
        district = district ?: issueLocation?.district,
        region = region ?: issueLocation?.region,
        nearbyLandmark = nearbyLandmark ?: issueLocation?.nearbyLandmark,
        accuracy = locationAccuracy ?: issueLocation?.accuracy
    )

    val effRepLat = reporterLocation?.latitude ?: reporterLatitude ?: 0.0
    val effRepLng = reporterLocation?.longitude ?: reporterLongitude ?: 0.0
    val effRepLoc = GeoLocation(
        latitude = effRepLat,
        longitude = effRepLng
    )

    val effCat = parseIssueCategory(category)
    val effStatus = parseIssueStatus(status)
    val effUserPriority = parseIssuePriority(userSeverity)
    val effCalcPriority = parseIssuePriority(calculatedPriority)
    val effEvidence = if (evidenceUrls.isNotEmpty()) evidenceUrls else evidenceUris
    val effLastUpdated = if (lastUpdated != 0L) lastUpdated else updatedAt

    return IssueReport(
        id = id,
        universityId = universityId,
        universityName = universityName,
        campusName = campusName,
        category = effCat,
        subCategory = subCategory,
        description = description,
        voiceTranscription = voiceTranscription,
        issueLocation = effIssueLoc,
        reporterLocation = effRepLoc,
        evidenceUris = effEvidence,
        videoUri = videoUri,
        status = effStatus,
        routingStatus = routingStatus ?: "UNASSIGNED",
        urgency = urgency ?: "NORMAL",
        locationSource = locationSource ?: "CURRENT_LOCATION",
        userPriority = effUserPriority,
        calculatedPriority = effCalcPriority,
        impactScore = impactScore,
        reporterId = reporterId ?: "",
        reporterName = reporterName,
        reporterEmail = reporterEmail,
        reporterPhone = reporterPhone,
        timestamp = timestamp,
        lastUpdated = effLastUpdated,
        assignedDepartment = assignedDepartment,
        assignedOfficerId = assignedOfficerId,
        assignedLeaderName = assignedLeaderName,
        supportsCount = supportsCount,
        verificationCount = verificationCount,
        isEmergency = isEmergency,
        timeline = timeline,
        beforeImageUri = beforeImageUri,
        afterImageUri = afterImageUri,
        publicResponse = publicResponse,
        internalNotes = internalNotes
    )
}

@Serializable
data class IssueComment(
    val id: String,
    val issueId: String,
    val userId: String,
    val text: String,
    @Serializable(with = IsoTimestampSerializer::class)
    val timestamp: Long = 0,
    val attachments: List<String> = emptyList(),
    val isOfficial: Boolean = false
)

@Serializable
enum class UserRole {
    CITIZEN,
    FIELD_OFFICER,
    DEPT_OFFICER,
    STUDENT_LEADER,
    ADMIN
}

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val role: UserRole = UserRole.CITIZEN,
    val department: String? = null,
    val reputationScore: Int = 0
)

@Serializable
data class IssueTimelineEvent(
    @Serializable(with = IsoTimestampSerializer::class)
    val timestamp: Long = 0,
    val status: IssueStatus = IssueStatus.SUBMITTED,
    val message: String = "",
    val actorId: String? = null
)

@Serializable
data class CreateIssueReportRequest(
    val universityId: Long? = null,
    val campusName: String? = null,
    val category: String,
    val subCategory: String? = null,
    val description: String,
    val urgency: String = "NORMAL",
    val userSeverity: String = "MEDIUM",
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val street: String? = null,
    val ward: String? = null,
    val district: String? = null,
    val region: String? = null,
    val nearbyLandmark: String? = null,
    val reporterLatitude: Double? = null,
    val reporterLongitude: Double? = null,
    val locationAccuracy: Float? = null,
    val evidenceUrls: List<String> = emptyList(),
    val locationSource: String = "CURRENT_LOCATION",
    val reporterName: String,
    val reporterEmail: String? = null,
    val reporterPhone: String? = null,
    val isEmergency: Boolean = false
)
