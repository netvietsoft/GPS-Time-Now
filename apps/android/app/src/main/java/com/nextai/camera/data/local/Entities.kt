package com.nextai.camera.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// ============================================================================
// GROUP A: WATERMARK TEMPLATES & STORE (8 TABLES)
// ============================================================================

@Entity(tableName = "new_watermarks_table")
data class NewWatermarkEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "base_id") val baseId: String?,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "update_time") val updateTime: String?,
    @ColumnInfo(name = "version") val version: String?,
    @ColumnInfo(name = "water_mark_content") val waterMarkContent: String?,
    @ColumnInfo(name = "is_pgc") val isPgc: Int,
    @ColumnInfo(name = "pgc_Order") val pgcOrder: Int
)

@Entity(tableName = "cloud_water_mark_table")
data class CloudWatermarkEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "base_id") val baseId: String?,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "update_time") val updateTime: String?,
    @ColumnInfo(name = "version") val version: String?,
    @ColumnInfo(name = "cover_image_url") val coverImageUrl: String?,
    @ColumnInfo(name = "water_mark_content") val waterMarkContent: String?,
    @ColumnInfo(name = "vip_type") val vipType: Int
)

@Entity(tableName = "share_water_mark_table")
data class ShareWatermarkEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "base_id") val baseId: String?,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "update_time") val updateTime: String?,
    @ColumnInfo(name = "version") val version: String?,
    @ColumnInfo(name = "cover_image_url") val coverImageUrl: String?,
    @ColumnInfo(name = "water_mark_content") val waterMarkContent: String?,
    @ColumnInfo(name = "share_code") val shareCode: String?
)

@Entity(tableName = "cloud_category_table")
data class CloudCategoryEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "icon_url") val iconUrl: String?,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "status") val status: Int
)

@Entity(tableName = "use_recent_water_mark_table")
data class UseRecentWatermarkEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "base_id") val baseId: String?,
    @ColumnInfo(name = "update_time") val updateTime: Long,
    @ColumnInfo(name = "use_count") val useCount: Int,
    @ColumnInfo(name = "from_type") val fromType: Int
)

@Entity(tableName = "recently_edited_table")
data class RecentlyEditedEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "base_id") val baseId: String?,
    @ColumnInfo(name = "watermark_id") val watermarkId: String?,
    @ColumnInfo(name = "item_id") val itemId: Int,
    @ColumnInfo(name = "content") val content: String?,
    @ColumnInfo(name = "edit_time") val editTime: Long
)

@Entity(tableName = "watermark_template_edit_guide_table")
data class WatermarkTemplateEditGuideEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "shown_count") val shownCount: Int,
    @ColumnInfo(name = "last_shown_time") val lastShownTime: Long
)

@Entity(tableName = "history_grouping_table")
data class HistoryGroupingEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "watermark_id") val watermarkId: String?,
    @ColumnInfo(name = "base_id") val baseId: String?,
    @ColumnInfo(name = "item_id") val itemId: Int,
    @ColumnInfo(name = "group_name") val groupName: String?,
    @ColumnInfo(name = "item_content") val itemContent: String?,
    @ColumnInfo(name = "update_time") val updateTime: Long
)

// ============================================================================
// GROUP B: PHOTOS, ATTENDANCE, ANTI-FAKE CODE & CLOUD SYNC (7 TABLES)
// ============================================================================

@Entity(tableName = "stand_alone_photo_table")
data class StandAlonePhotoEntity(
    @PrimaryKey @ColumnInfo(name = "path") val path: String,
    @ColumnInfo(name = "time") val time: Long,
    @ColumnInfo(name = "lat") val lat: Double,
    @ColumnInfo(name = "lng") val lng: Double,
    @ColumnInfo(name = "address") val address: String?,
    @ColumnInfo(name = "watermark_id") val watermarkId: String?,
    @ColumnInfo(name = "base_id") val baseId: String?,
    @ColumnInfo(name = "media_type") val mediaType: Int,
    @ColumnInfo(name = "photo_code") val photoCode: String?,
    @ColumnInfo(name = "synced") val synced: Int
)

@Entity(tableName = "picture_up_table")
data class PictureUpEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "local_file_path") val localFilePath: String?,
    @ColumnInfo(name = "oss_file_path") val ossFilePath: String?,
    @ColumnInfo(name = "upload_status") val uploadStatus: Int,
    @ColumnInfo(name = "take_photo_time") val takePhotoTime: Long,
    @ColumnInfo(name = "lat") val lat: Double,
    @ColumnInfo(name = "lng") val lng: Double,
    @ColumnInfo(name = "location_address") val locationAddress: String?,
    @ColumnInfo(name = "watermark_base_id") val watermarkBaseId: String?,
    @ColumnInfo(name = "watermark_id") val watermarkId: String?,
    @ColumnInfo(name = "photo_info_json") val photoInfoJson: String?,
    @ColumnInfo(name = "retry_count") val retryCount: Int,
    @ColumnInfo(name = "media_type") val mediaType: Int
)

@Entity(tableName = "album_number_table_new")
data class AlbumNumberNewEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "take_date") val takeDate: String?,
    @ColumnInfo(name = "photo_num") val photoNum: Int,
    @ColumnInfo(name = "video_num") val videoNum: Int,
    @ColumnInfo(name = "watermark_id") val watermarkId: String?
)

@Entity(tableName = "water_check_in_take_history_table")
data class WaterCheckInTakeHistoryEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "take_time") val takeTime: Long,
    @ColumnInfo(name = "date_str") val dateStr: String?,
    @ColumnInfo(name = "month_str") val monthStr: String?,
    @ColumnInfo(name = "lat") val lat: Double,
    @ColumnInfo(name = "lng") val lng: Double,
    @ColumnInfo(name = "address") val address: String?,
    @ColumnInfo(name = "photo_path") val photoPath: String?,
    @ColumnInfo(name = "watermark_id") val watermarkId: String?,
    @ColumnInfo(name = "shift_type") val shiftType: Int,
    @ColumnInfo(name = "remark") val remark: String?
)

@Entity(tableName = "photo_code_entity")
data class PhotoCodeEntity(
    @PrimaryKey @ColumnInfo(name = "photo_code") val photoCode: String,
    @ColumnInfo(name = "local_path") val localPath: String?,
    @ColumnInfo(name = "oss_url") val ossUrl: String?,
    @ColumnInfo(name = "capture_time") val captureTime: Long,
    @ColumnInfo(name = "lat") val lat: Double,
    @ColumnInfo(name = "lng") val lng: Double,
    @ColumnInfo(name = "address") val address: String?,
    @ColumnInfo(name = "watermark_json") val watermarkJson: String?,
    @ColumnInfo(name = "verify_status") val verifyStatus: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(tableName = "anti_code_entry")
data class AntiCodeEntryEntity(
    @PrimaryKey @ColumnInfo(name = "code") val code: String,
    @ColumnInfo(name = "status") val status: Int,
    @ColumnInfo(name = "create_time") val createTime: Long,
    @ColumnInfo(name = "use_time") val useTime: Long,
    @ColumnInfo(name = "photo_path") val photoPath: String?,
    @ColumnInfo(name = "extra_json") val extraJson: String?
)

@Entity(tableName = "oss_data_table")
data class OssDataEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "access_key_id") val accessKeyId: String?,
    @ColumnInfo(name = "access_key_secret") val accessKeySecret: String?,
    @ColumnInfo(name = "security_token") val securityToken: String?,
    @ColumnInfo(name = "expiration") val expiration: Long,
    @ColumnInfo(name = "bucket_name") val bucketName: String?,
    @ColumnInfo(name = "endpoint") val endpoint: String?,
    @ColumnInfo(name = "region") val region: String?
)

// ============================================================================
// GROUP C: GPS LOCATION, POI & OFFLINE GEO-CACHE (5 TABLES)
// ============================================================================

@Entity(tableName = "place_list_entity")
data class PlaceListEntity(
    @PrimaryKey @ColumnInfo(name = "place_id") val placeId: String,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "address") val address: String?,
    @ColumnInfo(name = "lat") val lat: Double,
    @ColumnInfo(name = "lng") val lng: Double,
    @ColumnInfo(name = "distance") val distance: Double,
    @ColumnInfo(name = "type_code") val typeCode: String?,
    @ColumnInfo(name = "city") val city: String?,
    @ColumnInfo(name = "district") val district: String?,
    @ColumnInfo(name = "update_time") val updateTime: Long
)

@Entity(tableName = "search_place_entity")
data class SearchPlaceEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "keyword") val keyword: String?,
    @ColumnInfo(name = "place_id") val placeId: String?,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "address") val address: String?,
    @ColumnInfo(name = "lat") val lat: Double,
    @ColumnInfo(name = "lng") val lng: Double,
    @ColumnInfo(name = "search_time") val searchTime: Long
)

@Entity(tableName = "prefer_place_entity")
data class PreferPlaceEntity(
    @PrimaryKey @ColumnInfo(name = "place_id") val placeId: String,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "address") val address: String?,
    @ColumnInfo(name = "lat") val lat: Double,
    @ColumnInfo(name = "lng") val lng: Double,
    @ColumnInfo(name = "use_count") val useCount: Int,
    @ColumnInfo(name = "last_use_time") val lastUseTime: Long,
    @ColumnInfo(name = "custom_alias") val customAlias: String?
)

@Entity(tableName = "custom_poi_table")
data class CustomPoiEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "poi_name") val poiName: String?,
    @ColumnInfo(name = "address") val address: String?,
    @ColumnInfo(name = "lat") val lat: Double,
    @ColumnInfo(name = "lng") val lng: Double,
    @ColumnInfo(name = "create_time") val createTime: Long,
    @ColumnInfo(name = "group_id") val groupId: String?
)

@Entity(tableName = "lat_long_poi_entity")
data class LatLongPoiEntity(
    @PrimaryKey @ColumnInfo(name = "grid_key") val gridKey: String,
    @ColumnInfo(name = "lat") val lat: Double,
    @ColumnInfo(name = "lng") val lng: Double,
    @ColumnInfo(name = "poi_list_json") val poiListJson: String?,
    @ColumnInfo(name = "weather_json") val weatherJson: String?,
    @ColumnInfo(name = "cache_time") val cacheTime: Long
)

// ============================================================================
// GROUP D: TEAMSPACE SAAS, PROJECTS, CHECKLISTS & PATROL (8 TABLES)
// ============================================================================

@Entity(tableName = "team_space_entry")
data class TeamSpaceEntryEntity(
    @PrimaryKey @ColumnInfo(name = "team_id") val teamId: String,
    @ColumnInfo(name = "team_name") val teamName: String?,
    @ColumnInfo(name = "role") val role: Int,
    @ColumnInfo(name = "member_count") val memberCount: Int,
    @ColumnInfo(name = "photo_count") val photoCount: Int,
    @ColumnInfo(name = "plan_type") val planType: Int,
    @ColumnInfo(name = "storage_used_bytes") val storageUsedBytes: Long,
    @ColumnInfo(name = "storage_limit_bytes") val storageLimitBytes: Long,
    @ColumnInfo(name = "avatar_url") val avatarUrl: String?,
    @ColumnInfo(name = "settings_json") val settingsJson: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)

@Entity(tableName = "team_watermark_template")
data class TeamWatermarkTemplateEntity(
    @PrimaryKey @ColumnInfo(name = "template_id") val templateId: String,
    @ColumnInfo(name = "team_id") val teamId: String,
    @ColumnInfo(name = "project_id") val projectId: String?,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "base_id") val baseId: String?,
    @ColumnInfo(name = "content_json") val contentJson: String?,
    @ColumnInfo(name = "force_apply") val forceApply: Int,
    @ColumnInfo(name = "lock_time_location") val lockTimeLocation: Int,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)

@Entity(tableName = "upload_progress_task_entry")
data class UploadProgressTaskEntryEntity(
    @PrimaryKey @ColumnInfo(name = "task_id") val taskId: String,
    @ColumnInfo(name = "team_id") val teamId: String,
    @ColumnInfo(name = "project_id") val projectId: String?,
    @ColumnInfo(name = "local_path") val localPath: String,
    @ColumnInfo(name = "remote_url") val remoteUrl: String?,
    @ColumnInfo(name = "media_type") val mediaType: Int,
    @ColumnInfo(name = "file_size") val fileSize: Long,
    @ColumnInfo(name = "uploaded_bytes") val uploadedBytes: Long,
    @ColumnInfo(name = "status") val status: Int,
    @ColumnInfo(name = "error_msg") val errorMsg: String?,
    @ColumnInfo(name = "meta_json") val metaJson: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(tableName = "doc_upload_task_entry")
data class DocUploadTaskEntryEntity(
    @PrimaryKey @ColumnInfo(name = "doc_id") val docId: String,
    @ColumnInfo(name = "team_id") val teamId: String,
    @ColumnInfo(name = "project_id") val projectId: String,
    @ColumnInfo(name = "file_name") val fileName: String?,
    @ColumnInfo(name = "local_path") val localPath: String?,
    @ColumnInfo(name = "remote_url") val remoteUrl: String?,
    @ColumnInfo(name = "mime_type") val mimeType: String?,
    @ColumnInfo(name = "file_size") val fileSize: Long,
    @ColumnInfo(name = "status") val status: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(tableName = "checklist_template")
data class ChecklistTemplateEntity(
    @PrimaryKey @ColumnInfo(name = "checklist_id") val checklistId: String,
    @ColumnInfo(name = "team_id") val teamId: String,
    @ColumnInfo(name = "project_id") val projectId: String?,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "items_json") val itemsJson: String?,
    @ColumnInfo(name = "require_photo") val requirePhoto: Int,
    @ColumnInfo(name = "status") val status: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)

@Entity(tableName = "checklist_submission")
data class ChecklistSubmissionEntity(
    @PrimaryKey @ColumnInfo(name = "submission_id") val submissionId: String,
    @ColumnInfo(name = "checklist_id") val checklistId: String,
    @ColumnInfo(name = "team_id") val teamId: String,
    @ColumnInfo(name = "project_id") val projectId: String?,
    @ColumnInfo(name = "user_id") val userId: String?,
    @ColumnInfo(name = "answers_json") val answersJson: String?,
    @ColumnInfo(name = "photo_urls_json") val photoUrlsJson: String?,
    @ColumnInfo(name = "status") val status: Int,
    @ColumnInfo(name = "submitted_at") val submittedAt: Long
)

@Entity(tableName = "patrol_table")
data class PatrolEntity(
    @PrimaryKey @ColumnInfo(name = "patrol_id") val patrolId: String,
    @ColumnInfo(name = "route_name") val routeName: String?,
    @ColumnInfo(name = "group_id") val groupId: String?,
    @ColumnInfo(name = "total_points") val totalPoints: Int,
    @ColumnInfo(name = "completed_points") val completedPoints: Int,
    @ColumnInfo(name = "start_time") val startTime: Long,
    @ColumnInfo(name = "end_time") val endTime: Long,
    @ColumnInfo(name = "status") val status: Int
)

@Entity(tableName = "patrol_task_table")
data class PatrolTaskEntity(
    @PrimaryKey @ColumnInfo(name = "task_id") val taskId: String,
    @ColumnInfo(name = "patrol_id") val patrolId: String,
    @ColumnInfo(name = "point_name") val pointName: String?,
    @ColumnInfo(name = "qr_code") val qrCode: String?,
    @ColumnInfo(name = "lat") val lat: Double,
    @ColumnInfo(name = "lng") val lng: Double,
    @ColumnInfo(name = "radius_meters") val radiusMeters: Int,
    @ColumnInfo(name = "check_time") val checkTime: Long,
    @ColumnInfo(name = "photo_path") val photoPath: String?,
    @ColumnInfo(name = "status") val status: Int
)

// ============================================================================
// GROUP E: WORKGROUPS & BATCH REPORT EXPORT (10 TABLES)
// ============================================================================

@Entity(tableName = "work_group_table")
data class WorkGroupEntity(
    @PrimaryKey @ColumnInfo(name = "group_id") val groupId: String,
    @ColumnInfo(name = "group_name") val groupName: String?,
    @ColumnInfo(name = "owner_id") val ownerId: String?,
    @ColumnInfo(name = "role") val role: Int,
    @ColumnInfo(name = "member_num") val memberNum: Int,
    @ColumnInfo(name = "avatar") val avatar: String?,
    @ColumnInfo(name = "invite_code") val inviteCode: String?,
    @ColumnInfo(name = "create_time") val createTime: Long
)

@Entity(tableName = "work_group_picture_table")
data class WorkGroupPictureEntity(
    @PrimaryKey @ColumnInfo(name = "pic_id") val picId: String,
    @ColumnInfo(name = "group_id") val groupId: String,
    @ColumnInfo(name = "user_id") val userId: String?,
    @ColumnInfo(name = "user_name") val userName: String?,
    @ColumnInfo(name = "local_path") val localPath: String?,
    @ColumnInfo(name = "oss_url") val ossUrl: String?,
    @ColumnInfo(name = "thumb_url") val thumbUrl: String?,
    @ColumnInfo(name = "take_time") val takeTime: Long,
    @ColumnInfo(name = "lat") val lat: Double,
    @ColumnInfo(name = "lng") val lng: Double,
    @ColumnInfo(name = "address") val address: String?,
    @ColumnInfo(name = "watermark_id") val watermarkId: String?,
    @ColumnInfo(name = "Sync_status") val syncStatus: Int
)

@Entity(tableName = "work_group_water_mark_table")
data class WorkGroupWatermarkEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "group_id") val groupId: String,
    @ColumnInfo(name = "watermark_id") val watermarkId: String,
    @ColumnInfo(name = "base_id") val baseId: String?,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "content_json") val contentJson: String?,
    @ColumnInfo(name = "update_time") val updateTime: Long
)

@Entity(tableName = "work_group_report_template_type_table")
data class WorkGroupReportTemplateTypeEntity(
    @PrimaryKey @ColumnInfo(name = "type_id") val typeId: String,
    @ColumnInfo(name = "type_name") val typeName: String?,
    @ColumnInfo(name = "icon_url") val iconUrl: String?,
    @ColumnInfo(name = "sort_order") val sortOrder: Int
)

@Entity(tableName = "work_group_report_one_template_table")
data class WorkGroupReportOneTemplateEntity(
    @PrimaryKey @ColumnInfo(name = "template_id") val templateId: String,
    @ColumnInfo(name = "type_id") val typeId: String?,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "preview_url") val previewUrl: String?,
    @ColumnInfo(name = "format_type") val formatType: String?,
    @ColumnInfo(name = "config_json") val configJson: String?,
    @ColumnInfo(name = "vip_required") val vipRequired: Int
)

@Entity(tableName = "work_group_report_history_table")
data class WorkGroupReportHistoryEntity(
    @PrimaryKey @ColumnInfo(name = "report_id") val reportId: String,
    @ColumnInfo(name = "template_id") val templateId: String?,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "local_file_path") val localFilePath: String?,
    @ColumnInfo(name = "remote_share_url") val remoteShareUrl: String?,
    @ColumnInfo(name = "photo_count") val photoCount: Int,
    @ColumnInfo(name = "format_type") val formatType: String?,
    @ColumnInfo(name = "create_time") val createTime: Long
)

@Entity(tableName = "work_group_report_template_id_map_table")
data class WorkGroupReportTemplateIdMapEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "watermark_base_id") val watermarkBaseId: String?,
    @ColumnInfo(name = "report_template_id") val reportTemplateId: String?,
    @ColumnInfo(name = "priority") val priority: Int
)

@Entity(tableName = "work_report_recommend_table")
data class WorkReportRecommendEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "scene_tag") val sceneTag: String?,
    @ColumnInfo(name = "template_id") val templateId: String?,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "score") val score: Double,
    @ColumnInfo(name = "update_time") val updateTime: Long
)

@Entity(tableName = "project_item_table")
data class ProjectItemEntity(
    @PrimaryKey @ColumnInfo(name = "project_id") val projectId: String,
    @ColumnInfo(name = "team_id") val teamId: String?,
    @ColumnInfo(name = "project_name") val projectName: String?,
    @ColumnInfo(name = "address") val address: String?,
    @ColumnInfo(name = "lat") val lat: Double,
    @ColumnInfo(name = "lng") val lng: Double,
    @ColumnInfo(name = "radius") val radius: Int,
    @ColumnInfo(name = "photo_count") val photoCount: Int,
    @ColumnInfo(name = "status") val status: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)

@Entity(tableName = "input_tab_table")
data class InputTabEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "watermark_id") val watermarkId: String?,
    @ColumnInfo(name = "item_id") val itemId: Int,
    @ColumnInfo(name = "tab_name") val tabName: String?,
    @ColumnInfo(name = "input_history_json") val inputHistoryJson: String?,
    @ColumnInfo(name = "update_time") val updateTime: Long
)

// ============================================================================
// GROUP F: SYSTEM NOTIFICATIONS & PERSONAL UTILITIES (4 TABLES)
// ============================================================================

@Entity(tableName = "notification_entity")
data class NotificationEntity(
    @PrimaryKey @ColumnInfo(name = "msg_id") val msgId: String,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "body") val body: String?,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "deep_link") val deepLink: String?,
    @ColumnInfo(name = "team_id") val teamId: String?,
    @ColumnInfo(name = "is_read") val isRead: Int,
    @ColumnInfo(name = "received_at") val receivedAt: Long
)

@Entity(tableName = "home_notice_table")
data class HomeNoticeEntity(
    @PrimaryKey @ColumnInfo(name = "notice_id") val noticeId: String,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "content") val content: String?,
    @ColumnInfo(name = "action_url") val actionUrl: String?,
    @ColumnInfo(name = "priority") val priority: Int,
    @ColumnInfo(name = "is_dismissed") val isDismissed: Int,
    @ColumnInfo(name = "expire_time") val expireTime: Long
)

@Entity(tableName = "home_notice_new_table")
data class HomeNoticeNewEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "scene") val scene: String?,
    @ColumnInfo(name = "payload_json") val payloadJson: String?,
    @ColumnInfo(name = "show_count") val showCount: Int,
    @ColumnInfo(name = "max_show_count") val maxShowCount: Int,
    @ColumnInfo(name = "last_show_time") val lastShowTime: Long
)

@Entity(tableName = "baby_info_table")
data class BabyInfoEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "baby_name") val babyName: String?,
    @ColumnInfo(name = "birthday_timestamp") val birthdayTimestamp: Long,
    @ColumnInfo(name = "gender") val gender: Int,
    @ColumnInfo(name = "avatar_path") val avatarPath: String?,
    @ColumnInfo(name = "watermark_id") val watermarkId: String?
)
