package com.nextai.camera.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Teamspace SaaS, Construction Projects, Photo Map Clusters, Geofence Timeclock,
 * Field Inspection Checklists, and Cloud Integration Service (`NextAiClusterApiService`).
 */
interface NextAiClusterApiService {
    @POST("app/workgroup/home")
    suspend fun getTeamspaceHome(@Body body: Map<String, String>): NextAiBaseResponse<TeamspaceSummaryDto>

    @POST("app/photo/search")
    suspend fun searchTeamPhotos(@Body body: Map<String, Any>): NextAiBaseResponse<List<Map<String, Any>>>

    @POST("app/group/project/list")
    suspend fun getTeamProjects(@Body body: Map<String, String>): NextAiBaseResponse<List<Map<String, Any>>>

    @POST("app/photo/map/markers")
    suspend fun getPhotoMapClusters(@Body body: Map<String, Any>): NextAiBaseResponse<List<Map<String, Any>>>

    @POST("app/attendance/punch")
    suspend fun punchGeofenceAttendance(@Body request: GeofencePunchRequest): NextAiBaseResponse<GeofencePunchResult>

    @POST("app/group/timeclock/geofence/check")
    suspend fun checkGeofenceBoundary(@Body body: Map<String, Any>): NextAiBaseResponse<GeofencePunchResult>

    @POST("app/checklist/list")
    suspend fun listInspectionChecklists(@Body body: Map<String, String>): NextAiBaseResponse<List<Map<String, Any>>>

    @POST("app/photo/export/task/create")
    suspend fun createCloudExportTask(@Body body: Map<String, Any>): NextAiBaseResponse<Map<String, String>>

    @POST("web/teamspace/app/integration/list")
    suspend fun getCloudIntegrations(@Body body: Map<String, String>): NextAiBaseResponse<List<Map<String, Any>>>
}
