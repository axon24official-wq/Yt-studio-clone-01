package com.example.model

enum class ContentType {
    VIDEO, SHORT, LIVE
}

data class VideoItem(
    val id: String,
    val title: String,
    val type: ContentType,
    val duration: String,
    val publishedTimeAgo: String,
    val views: Long,
    val likes: Long,
    val comments: Long = 0,
    val rankingText: String = "",
    val avgPercentageViewed: String = "",
    val realTimeViews: Long = 0,
    val thumbnailTitle: String = "",
    val isExpanded: Boolean = false,
    val category: String = "Entertainment",
    val description: String = "",
    val thumbnailUri: String? = null
)

data class CommentItem(
    val id: String,
    val authorHandle: String,
    val timeAgo: String,
    val content: String,
    val videoTitle: String,
    val likesCount: Int = 0
)

data class ChannelProfile(
    val id: String,
    val name: String,
    val handle: String,
    val subscribersCount: String,
    val email: String = "creator@gmail.com",
    val isMonetized: Boolean = true,
    val description: String = "",
    val avatarUri: String? = null
)

data class MetricCardData(
    val title: String,
    val value: String,
    val subtitle: String,
    val isPositive: Boolean = true,
    val showArrow: Boolean = true,
    val isCheckmark: Boolean = false,
    val trendData: List<Float> = emptyList(),
    val yMinLabel: String = "0",
    val yMidLabel: String = "",
    val yMaxLabel: String = "",
    val startDateLabel: String = "24 Sept",
    val endDateLabel: String = "21 Oct",
    val isSimulatedDemo: Boolean = false
)

data class DrilldownContentItem(
    val title: String,
    val valueText: String,
    val sharePercent: Float = 0f,
    val thumbnailTitle: String = "",
    val isShort: Boolean = false
)

data class RealtimeVideo(
    val title: String,
    val views: Int,
    val bars: List<Float>
)

data class TrafficSourceItem(
    val source: String,
    val percentage: Float
)

data class DeviceTypeItem(
    val name: String,
    val percentage: Float
)

data class SubtitleItem(
    val language: String,
    val percentage: Float
)

data class ExploreTopic(
    val title: String,
    val savedCount: Int
)

data class TrendVideo(
    val title: String,
    val channelName: String,
    val viewsText: String,
    val timeAgo: String,
    val duration: String
)
