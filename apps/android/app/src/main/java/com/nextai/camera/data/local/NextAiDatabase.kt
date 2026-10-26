package com.nextai.camera.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * NextAI Camera Room SQLite Database (`NextAiDatabase` v61).
 * Models all 42 relational tables for Watermarks, Photos, Anti-Fake Codes,
 * Geo/POI Caching, Teamspace SaaS, Geofence Attendance, Patrols, and Batch Reports.
 */
@Database(
    entities = [
        // Group A (8)
        NewWatermarkEntity::class,
        CloudWatermarkEntity::class,
        ShareWatermarkEntity::class,
        CloudCategoryEntity::class,
        UseRecentWatermarkEntity::class,
        RecentlyEditedEntity::class,
        WatermarkTemplateEditGuideEntity::class,
        HistoryGroupingEntity::class,
        // Group B (7)
        StandAlonePhotoEntity::class,
        PictureUpEntity::class,
        AlbumNumberNewEntity::class,
        WaterCheckInTakeHistoryEntity::class,
        PhotoCodeEntity::class,
        AntiCodeEntryEntity::class,
        OssDataEntity::class,
        // Group C (5)
        PlaceListEntity::class,
        SearchPlaceEntity::class,
        PreferPlaceEntity::class,
        CustomPoiEntity::class,
        LatLongPoiEntity::class,
        // Group D (8)
        TeamSpaceEntryEntity::class,
        TeamWatermarkTemplateEntity::class,
        UploadProgressTaskEntryEntity::class,
        DocUploadTaskEntryEntity::class,
        ChecklistTemplateEntity::class,
        ChecklistSubmissionEntity::class,
        PatrolEntity::class,
        PatrolTaskEntity::class,
        // Group E (10)
        WorkGroupEntity::class,
        WorkGroupPictureEntity::class,
        WorkGroupWatermarkEntity::class,
        WorkGroupReportTemplateTypeEntity::class,
        WorkGroupReportOneTemplateEntity::class,
        WorkGroupReportHistoryEntity::class,
        WorkGroupReportTemplateIdMapEntity::class,
        WorkReportRecommendEntity::class,
        ProjectItemEntity::class,
        InputTabEntity::class,
        // Group F (4)
        NotificationEntity::class,
        HomeNoticeEntity::class,
        HomeNoticeNewEntity::class,
        BabyInfoEntity::class
    ],
    version = 61,
    exportSchema = false
)
abstract class NextAiDatabase : RoomDatabase() {
    abstract fun watermarkDao(): WatermarkDao
    abstract fun photoAndAntiCodeDao(): PhotoAndAntiCodeDao
    abstract fun geoPoiDao(): GeoPoiDao
    abstract fun teamspaceAndReportDao(): TeamspaceAndReportDao

    companion object {
        private const val DATABASE_NAME = "nextai_camera_v61.db"

        @Volatile
        private var INSTANCE: NextAiDatabase? = null

        fun getInstance(context: Context): NextAiDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NextAiDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
