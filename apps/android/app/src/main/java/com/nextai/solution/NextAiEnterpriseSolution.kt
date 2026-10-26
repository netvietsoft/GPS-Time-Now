package com.nextai.solution

/**
 * NextAI Enterprise Solution Suite for B2B Teamspace SaaS, Geofence Attendance,
 * Field Inspection Checklists, and Automated Work Report Generation (PDF / XLSX / KMZ / ZIP).
 */
object NextAiEnterpriseSolution {
    const val SUITE_TITLE = "NextAI Teamspace Enterprise Solution"
    const val DEFAULT_GEOFENCE_RADIUS_METERS = 150
    const val MAX_BATCH_EXPORT_PHOTOS = 200

    enum class ExportFormat(val extension: String, val mimeType: String, val label: String) {
        PDF_REPORT("pdf", "application/pdf", "PDF Work Report"),
        EXCEL_SHEET("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Excel Spreadsheet (.xlsx)"),
        GOOGLE_EARTH_KMZ("kmz", "application/vnd.google-earth.kmz", "Google Earth Map (.kmz)"),
        ZIP_ARCHIVE("zip", "application/zip", "ZIP Photo Archive (.zip)")
    }
}
