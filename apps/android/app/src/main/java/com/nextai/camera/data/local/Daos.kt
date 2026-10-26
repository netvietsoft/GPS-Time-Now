package com.nextai.camera.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatermarkDao {
    @Query("SELECT * FROM new_watermarks_table ORDER BY pgc_Order ASC")
    fun observeInstalledWatermarks(): Flow<List<NewWatermarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatermarks(items: List<NewWatermarkEntity>)

    @Query("SELECT * FROM cloud_category_table ORDER BY sort_order ASC")
    fun observeCategories(): Flow<List<CloudCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategories(items: List<CloudCategoryEntity>)

    @Query("SELECT * FROM cloud_water_mark_table WHERE category_id = :categoryId")
    suspend fun getCloudWatermarksByCategory(categoryId: String): List<CloudWatermarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecentWatermark(item: UseRecentWatermarkEntity)

    @Query("SELECT * FROM recently_edited_table WHERE watermark_id = :watermarkId ORDER BY edit_time DESC LIMIT 20")
    suspend fun getRecentFieldEdits(watermarkId: String): List<RecentlyEditedEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFieldEdit(edit: RecentlyEditedEntity)
}

@Dao
interface PhotoAndAntiCodeDao {
    @Query("SELECT * FROM stand_alone_photo_table ORDER BY time DESC")
    fun observeAllPhotos(): Flow<List<StandAlonePhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: StandAlonePhotoEntity)

    @Query("SELECT * FROM photo_code_entity WHERE photo_code = :photoCode LIMIT 1")
    suspend fun findByPhotoCode(photoCode: String): PhotoCodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPhotoCode(entity: PhotoCodeEntity)

    @Query("SELECT * FROM anti_code_entry WHERE status = 0 LIMIT 1")
    suspend fun claimAvailableOfflineAntiCode(): AntiCodeEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAntiCodes(codes: List<AntiCodeEntryEntity>)

    @Query("SELECT * FROM water_check_in_take_history_table ORDER BY take_time DESC")
    fun observeAttendanceHistory(): Flow<List<WaterCheckInTakeHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecord(record: WaterCheckInTakeHistoryEntity)
}

@Dao
interface GeoPoiDao {
    @Query("SELECT * FROM place_list_entity ORDER BY distance ASC")
    fun observeNearbyPlaces(): Flow<List<PlaceListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNearbyPlaces(places: List<PlaceListEntity>)

    @Query("SELECT * FROM prefer_place_entity ORDER BY use_count DESC")
    fun observeFavoritePlaces(): Flow<List<PreferPlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavoritePlace(place: PreferPlaceEntity)

    @Query("SELECT * FROM lat_long_poi_entity WHERE grid_key = :gridKey LIMIT 1")
    suspend fun getCachedGridPoi(gridKey: String): LatLongPoiEntity?
}

@Dao
interface TeamspaceAndReportDao {
    @Query("SELECT * FROM team_space_entry ORDER BY updated_at DESC")
    fun observeTeams(): Flow<List<TeamSpaceEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTeams(teams: List<TeamSpaceEntryEntity>)

    @Query("SELECT * FROM project_item_table WHERE team_id = :teamId ORDER BY updated_at DESC")
    fun observeProjects(teamId: String): Flow<List<ProjectItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProjects(projects: List<ProjectItemEntity>)

    @Query("SELECT * FROM checklist_template WHERE team_id = :teamId")
    fun observeChecklists(teamId: String): Flow<List<ChecklistTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChecklists(items: List<ChecklistTemplateEntity>)

    @Query("SELECT * FROM work_group_report_history_table ORDER BY create_time DESC")
    fun observeReportHistory(): Flow<List<WorkGroupReportHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportHistory(report: WorkGroupReportHistoryEntity)

    @Query("SELECT * FROM notification_entity ORDER BY received_at DESC")
    fun observeNotifications(): Flow<List<NotificationEntity>>
}
