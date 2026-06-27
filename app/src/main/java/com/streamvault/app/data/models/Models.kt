package com.streamvault.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// ─── Server Configuration ──────────────────────────────────────
data class ServerConfig(
    val name: String = "",
    val url: String = "",
    val username: String = "",
    val password: String = ""
) {
    val baseUrl: String get() = url.trimEnd('/')
    val apiUrl: String get() = "$baseUrl/player_api.php?username=$username&password=$password"
    fun streamUrl(type: String, extension: String, streamId: Any): String =
        "$baseUrl/$type/$username/$password/$streamId.$extension"
}

// ─── Authentication ────────────────────────────────────────────
data class UserAuthResponse(
    @SerializedName("user_info") val userInfo: UserInfo?,
    @SerializedName("server_info") val serverInfo: ServerInfo?
)

data class UserInfo(
    val username: String?,
    val password: String?,
    val message: String?,
    val auth: Int?,
    val status: String?,
    @SerializedName("exp_date") val expDate: String?,
    @SerializedName("is_trial") val isTrial: String?,
    @SerializedName("active_cons") val activeCons: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("max_connections") val maxConnections: String?,
    @SerializedName("allowed_output_formats") val allowedOutputFormats: List<String>?
)

data class ServerInfo(
    @SerializedName("url") val url: String?,
    val port: String?,
    @SerializedName("https_port") val httpsPort: String?,
    @SerializedName("server_protocol") val serverProtocol: String?,
    @SerializedName("rtmp_port") val rtmpPort: String?,
    val timezone: String?,
    @SerializedName("timestamp_now") val timestampNow: Long?,
    @SerializedName("process_active") val processActive: Int?
)

// ─── Categories ────────────────────────────────────────────────
data class Category(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("parent_id") val parentId: Int = 0
)

// ─── Live Streams ──────────────────────────────────────────────
data class LiveStream(
    @SerializedName("num") val num: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("stream_type") val streamType: String = "",
    @SerializedName("stream_id") val streamId: Int = 0,
    @SerializedName("stream_icon") val streamIcon: String? = null,
    @SerializedName("epg_channel_id") val epgChannelId: String? = null,
    @SerializedName("added") val added: String? = null,
    @SerializedName("is_adult") val isAdult: String? = null,
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("category_ids") val categoryIds: List<Int>? = null,
    @SerializedName("custom_sid") val customSid: String? = null,
    @SerializedName("tv_archive") val tvArchive: Int = 0,
    @SerializedName("direct_source") val directSource: String? = null,
    @SerializedName("tv_archive_duration") val tvArchiveDuration: Int = 0
)

// ─── VOD (Movies) ──────────────────────────────────────────────
data class VodStream(
    @SerializedName("num") val num: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("stream_type") val streamType: String = "",
    @SerializedName("stream_id") val streamId: Int = 0,
    @SerializedName("stream_icon") val streamIcon: String? = null,
    @SerializedName("rating") val rating: String? = null,
    @SerializedName("rating_5based") val rating5based: Double = 0.0,
    @SerializedName("added") val added: String? = null,
    @SerializedName("is_adult") val isAdult: String? = null,
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("category_ids") val categoryIds: List<Int>? = null,
    @SerializedName("container_extension") val containerExtension: String? = null,
    @SerializedName("custom_sid") val customSid: String? = null,
    @SerializedName("direct_source") val directSource: String? = null
)

data class VodInfo(
    @SerializedName("info") val info: VodDetail?,
    @SerializedName("movie_data") val movieData: MovieData?
)

data class VodDetail(
    @SerializedName("movie_image") val movieImage: String? = null,
    @SerializedName("tmdb_id") val tmdbId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("o_name") val originalName: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("plot") val plot: String? = null,
    @SerializedName("cast") val cast: String? = null,
    @SerializedName("director") val director: String? = null,
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("releaseDate") val releaseDate: String? = null,
    @SerializedName("release_date") val releaseDateAlt: String? = null,
    @SerializedName("duration_secs") val durationSecs: Int = 0,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("rating") val rating: String? = null,
    @SerializedName("backdrop_path") val backdropPath: List<String>? = null,
    @SerializedName("youtube_trailer") val youtubeTrailer: String? = null,
    @SerializedName("episode_run_time") val episodeRunTime: String? = null,
    @SerializedName("country") val country: String? = null
)

data class MovieData(
    @SerializedName("stream_id") val streamId: Int = 0,
    @SerializedName("name") val name: String? = null,
    @SerializedName("added") val added: String? = null,
    @SerializedName("category_id") val categoryId: String? = null,
    @SerializedName("container_extension") val containerExtension: String? = null,
    @SerializedName("custom_sid") val customSid: String? = null,
    @SerializedName("direct_source") val directSource: String? = null
)

// ─── Series ────────────────────────────────────────────────────
data class SeriesItem(
    @SerializedName("num") val num: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("series_id") val seriesId: Int = 0,
    @SerializedName("cover") val cover: String? = null,
    @SerializedName("plot") val plot: String? = null,
    @SerializedName("cast") val cast: String? = null,
    @SerializedName("director") val director: String? = null,
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("releaseDate") val releaseDate: String? = null,
    @SerializedName("last_modified") val lastModified: String? = null,
    @SerializedName("rating") val rating: String? = null,
    @SerializedName("rating_5based") val rating5based: Double = 0.0,
    @SerializedName("backdrop_path") val backdropPath: List<String>? = null,
    @SerializedName("youtube_trailer") val youtubeTrailer: String? = null,
    @SerializedName("episode_run_time") val episodeRunTime: String? = null,
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("category_ids") val categoryIds: List<Int>? = null
)

data class SeriesInfo(
    @SerializedName("info") val info: SeriesDetail?,
    @SerializedName("episodes") val episodes: Map<String, List<Episode>>?,
    @SerializedName("seasons") val seasons: List<Season>?
)

data class SeriesDetail(
    @SerializedName("name") val name: String? = null,
    @SerializedName("cover") val cover: String? = null,
    @SerializedName("plot") val plot: String? = null,
    @SerializedName("cast") val cast: String? = null,
    @SerializedName("director") val director: String? = null,
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("releaseDate") val releaseDate: String? = null,
    @SerializedName("rating") val rating: String? = null,
    @SerializedName("backdrop_path") val backdropPath: List<String>? = null,
    @SerializedName("youtube_trailer") val youtubeTrailer: String? = null,
    @SerializedName("episode_run_time") val episodeRunTime: String? = null,
    @SerializedName("seasons") val seasons: List<Season>? = null
)

data class Season(
    @SerializedName("season_number") val seasonNumber: Int = 0,
    @SerializedName("name") val name: String? = null,
    @SerializedName("episode_count") val episodeCount: Int? = null,
    @SerializedName("cover") val cover: String? = null,
    @SerializedName("overview") val overview: String? = null
)

data class Episode(
    @SerializedName("id") val id: String = "",
    @SerializedName("episode_num") val episodeNum: Int = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("container_extension") val containerExtension: String? = null,
    @SerializedName("info") val info: EpisodeInfo? = null,
    @SerializedName("custom_sid") val customSid: String? = null,
    @SerializedName("added") val added: String? = null,
    @SerializedName("season") val season: Int = 0,
    @SerializedName("direct_source") val directSource: String? = null
)

data class EpisodeInfo(
    @SerializedName("tmdb_id") val tmdbId: Int? = null,
    @SerializedName("releasedate") val releaseDate: String? = null,
    @SerializedName("plot") val plot: String? = null,
    @SerializedName("duration_secs") val durationSecs: Int = 0,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("movie_image") val movieImage: String? = null,
    @SerializedName("rating") val rating: Double? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("season") val season: Int = 0
)

// ─── EPG ───────────────────────────────────────────────────────
data class EpgResponse(
    @SerializedName("epg_listings") val epgListings: List<EpgListing>?
)

data class EpgListing(
    @SerializedName("id") val id: String?,
    @SerializedName("epg_id") val epgId: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("start") val start: String?,
    @SerializedName("end") val end: String?,
    @SerializedName("start_timestamp") val startTimestamp: String?,
    @SerializedName("stop_timestamp") val stopTimestamp: String?,
    @SerializedName("now_playing") val nowPlaying: Int = 0,
    @SerializedName("has_archive") val hasArchive: Int = 0
)

// ─── Room Entities ─────────────────────────────────────────────
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val icon: String?,
    val type: String, // "live", "movie", "series"
    val categoryId: String,
    val containerExtension: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Int,
    val name: String,
    val icon: String?,
    val type: String, // "live", "movie", "episode"
    val categoryId: String,
    val containerExtension: String? = null,
    val lastPosition: Long = 0,
    val duration: Long = 0,
    val episodeId: String? = null,
    val seriesId: Int? = null,
    val season: Int? = null,
    val episodeNum: Int? = null,
    val watchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "server_configs")
data class ServerConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val username: String,
    val password: String,
    val isActive: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)

// ─── UI State ──────────────────────────────────────────────────
data class HomeSection<T>(
    val title: String,
    val items: List<T>,
    val type: String
)

enum class ContentType { LIVE, MOVIE, SERIES }

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val title: String = "",
    val subtitle: String = "",
    val quality: String = "",
    val audioTrack: String = "",
    val subtitleTrack: String = ""
)

data class ChannelGroup(
    val category: Category,
    val channels: List<LiveStream>
)
