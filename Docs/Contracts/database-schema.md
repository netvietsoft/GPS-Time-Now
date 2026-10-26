# Database Schema Contract — 42-Table Relational OLTP & 8-Table OLAP Star Schema

> **Owner**: `Agent 1 - ARCHITECT` & `Agent 2 - BACKEND`  
> **Room Schema Version**: `61` (`NextAICameraDB`, 100% column & PK parity with `TodayCameraDB_Impl`)  
> **Related ADR**: [`ADR-003`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/ADR/ADR-003-relational-and-olap-schema.md)

---

## 1. Plane 1: All 42 Relational OLTP Tables (`NextAICameraDB` v61 Parity)

### Group A: Watermark Templates & Skins (Tables 1–8)
1. `new_watermarks_table` (`id TEXT PRIMARY KEY, name TEXT, base_id TEXT, category_id TEXT, type INTEGER NOT NULL, update_time TEXT, version TEXT, water_mark_content TEXT, is_pgc INTEGER NOT NULL, pgc_Order INTEGER NOT NULL`)
2. `cloud_water_mark_table` (`id TEXT PRIMARY KEY, name TEXT, base_id TEXT, category_id TEXT, type INTEGER NOT NULL, update_time TEXT, version TEXT, cover_image_url TEXT, water_mark_content TEXT, vip_type INTEGER NOT NULL`)
3. `share_water_mark_table` (`id TEXT PRIMARY KEY, name TEXT, base_id TEXT, category_id TEXT, type INTEGER NOT NULL, update_time TEXT, version TEXT, cover_image_url TEXT, water_mark_content TEXT, share_code TEXT`)
4. `cloud_category_table` (`id TEXT PRIMARY KEY, name TEXT, icon_url TEXT, sort_order INTEGER NOT NULL, status INTEGER NOT NULL`)
5. `use_recent_water_mark_table` (`id TEXT PRIMARY KEY, base_id TEXT, update_time INTEGER NOT NULL, use_count INTEGER NOT NULL, from_type INTEGER NOT NULL`)
6. `recently_edited_table` (`id TEXT PRIMARY KEY, base_id TEXT, watermark_id TEXT, item_id INTEGER NOT NULL, content TEXT, edit_time INTEGER NOT NULL`)
7. `watermark_template_edit_guide_table` (`id TEXT PRIMARY KEY, shown_count INTEGER NOT NULL, last_shown_time INTEGER NOT NULL`)
8. `history_grouping_table` (`id INTEGER PRIMARY KEY AUTOINCREMENT, watermark_id TEXT, base_id TEXT, item_id INTEGER NOT NULL, group_name TEXT, item_content TEXT, update_time INTEGER NOT NULL`)

### Group B: Photos, Personal Attendance, Anti-Fake & Cloud Sync (Tables 9–15)
9. `stand_alone_photo_table` (`path TEXT PRIMARY KEY, time INTEGER NOT NULL, lat REAL NOT NULL, lng REAL NOT NULL, address TEXT, watermark_id TEXT, base_id TEXT, media_type INTEGER NOT NULL, photo_code TEXT, synced INTEGER NOT NULL`)
10. `picture_up_table` (`id INTEGER PRIMARY KEY AUTOINCREMENT, local_file_path TEXT, oss_file_path TEXT, upload_status INTEGER NOT NULL, take_photo_time INTEGER NOT NULL, lat REAL NOT NULL, lng REAL NOT NULL, location_address TEXT, watermark_base_id TEXT, watermark_id TEXT, photo_info_json TEXT, retry_count INTEGER NOT NULL, media_type INTEGER NOT NULL`)
11. `album_number_table_new` (`id INTEGER PRIMARY KEY AUTOINCREMENT, take_date TEXT, photo_num INTEGER NOT NULL, video_num INTEGER NOT NULL, watermark_id TEXT`)
12. `water_check_in_take_history_table` (`id INTEGER PRIMARY KEY AUTOINCREMENT, take_time INTEGER NOT NULL, date_str TEXT, month_str TEXT, lat REAL NOT NULL, lng REAL NOT NULL, address TEXT, photo_path TEXT, watermark_id TEXT, shift_type INTEGER NOT NULL, remark TEXT`)
13. `photo_code_entity` (`photo_code TEXT PRIMARY KEY, local_path TEXT, oss_url TEXT, capture_time INTEGER NOT NULL, lat REAL NOT NULL, lng REAL NOT NULL, address TEXT, watermark_json TEXT, verify_status INTEGER NOT NULL, created_at INTEGER NOT NULL`)
14. `anti_code_entry` (`code TEXT PRIMARY KEY, status INTEGER NOT NULL, create_time INTEGER NOT NULL, use_time INTEGER NOT NULL, photo_path TEXT, extra_json TEXT`)
15. `oss_data_table` (`id TEXT PRIMARY KEY, access_key_id TEXT, access_key_secret TEXT, security_token TEXT, expiration INTEGER NOT NULL, bucket_name TEXT, endpoint TEXT, region TEXT`)

### Group C: GPS Positioning, POI & Offline Geocoding Grid (Tables 16–20)
16. `place_list_entity` (`place_id TEXT PRIMARY KEY, name TEXT, address TEXT, lat REAL NOT NULL, lng REAL NOT NULL, distance REAL NOT NULL, type_code TEXT, city TEXT, district TEXT, update_time INTEGER NOT NULL`)
17. `search_place_entity` (`id INTEGER PRIMARY KEY AUTOINCREMENT, keyword TEXT, place_id TEXT, name TEXT, address TEXT, lat REAL NOT NULL, lng REAL NOT NULL, search_time INTEGER NOT NULL`)
18. `prefer_place_entity` (`place_id TEXT PRIMARY KEY, name TEXT, address TEXT, lat REAL NOT NULL, lng REAL NOT NULL, use_count INTEGER NOT NULL, last_use_time INTEGER NOT NULL, custom_alias TEXT`)
19. `custom_poi_table` (`id INTEGER PRIMARY KEY AUTOINCREMENT, poi_name TEXT, address TEXT, lat REAL NOT NULL, lng REAL NOT NULL, create_time INTEGER NOT NULL, group_id TEXT`)
20. `lat_long_poi_entity` (`grid_key TEXT PRIMARY KEY, lat REAL NOT NULL, lng REAL NOT NULL, poi_list_json TEXT, weather_json TEXT, cache_time INTEGER NOT NULL`)

### Group D: Teamspace SaaS, Projects, Checklists & Patrol (Tables 21–28)
21. `team_space_entry` (`team_id TEXT PRIMARY KEY, team_name TEXT, role INTEGER NOT NULL, member_count INTEGER NOT NULL, photo_count INTEGER NOT NULL, plan_type INTEGER NOT NULL, storage_used_bytes INTEGER NOT NULL, storage_limit_bytes INTEGER NOT NULL, avatar_url TEXT, settings_json TEXT, updated_at INTEGER NOT NULL`)
22. `team_watermark_template` (`template_id TEXT PRIMARY KEY, team_id TEXT NOT NULL, project_id TEXT, name TEXT, base_id TEXT, content_json TEXT, force_apply INTEGER NOT NULL, lock_time_location INTEGER NOT NULL, sort_order INTEGER NOT NULL, updated_at INTEGER NOT NULL`)
23. `upload_progress_task_entry` (`task_id TEXT PRIMARY KEY, team_id TEXT NOT NULL, project_id TEXT, local_path TEXT NOT NULL, remote_url TEXT, media_type INTEGER NOT NULL, file_size INTEGER NOT NULL, uploaded_bytes INTEGER NOT NULL, status INTEGER NOT NULL, error_msg TEXT, meta_json TEXT, created_at INTEGER NOT NULL`)
24. `doc_upload_task_entry` (`doc_id TEXT PRIMARY KEY, team_id TEXT NOT NULL, project_id TEXT NOT NULL, file_name TEXT, local_path TEXT, remote_url TEXT, mime_type TEXT, file_size INTEGER NOT NULL, status INTEGER NOT NULL, created_at INTEGER NOT NULL`)
25. `checklist_template` (`checklist_id TEXT PRIMARY KEY, team_id TEXT NOT NULL, project_id TEXT, title TEXT, description TEXT, items_json TEXT, require_photo INTEGER NOT NULL, status INTEGER NOT NULL, updated_at INTEGER NOT NULL`)
26. `checklist_submission` (`submission_id TEXT PRIMARY KEY, checklist_id TEXT NOT NULL, team_id TEXT NOT NULL, project_id TEXT, user_id TEXT, answers_json TEXT, photo_urls_json TEXT, status INTEGER NOT NULL, submitted_at INTEGER NOT NULL`)
27. `patrol_table` (`patrol_id TEXT PRIMARY KEY, route_name TEXT, group_id TEXT, total_points INTEGER NOT NULL, completed_points INTEGER NOT NULL, start_time INTEGER NOT NULL, end_time INTEGER NOT NULL, status INTEGER NOT NULL`)
28. `patrol_task_table` (`task_id TEXT PRIMARY KEY, patrol_id TEXT NOT NULL, point_name TEXT, qr_code TEXT, lat REAL NOT NULL, lng REAL NOT NULL, radius_meters INTEGER NOT NULL, check_time INTEGER NOT NULL, photo_path TEXT, status INTEGER NOT NULL`)

### Group E: WorkGroups & Batch Report Export (Tables 29–38)
29. `work_group_table` (`group_id TEXT PRIMARY KEY, group_name TEXT, owner_id TEXT, role INTEGER NOT NULL, member_num INTEGER NOT NULL, avatar TEXT, invite_code TEXT, create_time INTEGER NOT NULL`)
30. `work_group_picture_table` (`pic_id TEXT PRIMARY KEY, group_id TEXT NOT NULL, user_id TEXT, user_name TEXT, local_path TEXT, oss_url TEXT, thumb_url TEXT, take_time INTEGER NOT NULL, lat REAL NOT NULL, lng REAL NOT NULL, address TEXT, watermark_id TEXT, Sync_status INTEGER NOT NULL`)
31. `work_group_water_mark_table` (`id INTEGER PRIMARY KEY AUTOINCREMENT, group_id TEXT NOT NULL, watermark_id TEXT NOT NULL, base_id TEXT, name TEXT, content_json TEXT, update_time INTEGER NOT NULL`)
32. `work_group_report_template_type_table` (`type_id TEXT PRIMARY KEY, type_name TEXT, icon_url TEXT, sort_order INTEGER NOT NULL`)
33. `work_group_report_one_template_table` (`template_id TEXT PRIMARY KEY, type_id TEXT, title TEXT, preview_url TEXT, format_type TEXT, config_json TEXT, vip_required INTEGER NOT NULL`)
34. `work_group_report_history_table` (`report_id TEXT PRIMARY KEY, template_id TEXT, title TEXT, local_file_path TEXT, remote_share_url TEXT, photo_count INTEGER NOT NULL, format_type TEXT, create_time INTEGER NOT NULL`)
35. `work_group_report_template_id_map_table` (`id INTEGER PRIMARY KEY AUTOINCREMENT, watermark_base_id TEXT, report_template_id TEXT, priority INTEGER NOT NULL`)
36. `work_report_recommend_table` (`id TEXT PRIMARY KEY, scene_tag TEXT, template_id TEXT, title TEXT, score REAL NOT NULL, update_time INTEGER NOT NULL`)
37. `project_item_table` (`project_id TEXT PRIMARY KEY, team_id TEXT, project_name TEXT, address TEXT, lat REAL NOT NULL, lng REAL NOT NULL, radius INTEGER NOT NULL, photo_count INTEGER NOT NULL, status INTEGER NOT NULL, updated_at INTEGER NOT NULL`)
38. `input_tab_table` (`id INTEGER PRIMARY KEY AUTOINCREMENT, watermark_id TEXT, item_id INTEGER NOT NULL, tab_name TEXT, input_history_json TEXT, update_time INTEGER NOT NULL`)

### Group F: System Notifications & Utilities (Tables 39–42)
39. `notification_entity` (`msg_id TEXT PRIMARY KEY, title TEXT, body TEXT, type INTEGER NOT NULL, deep_link TEXT, team_id TEXT, is_read INTEGER NOT NULL, received_at INTEGER NOT NULL`)
40. `home_notice_table` (`notice_id TEXT PRIMARY KEY, title TEXT, content TEXT, action_url TEXT, priority INTEGER NOT NULL, is_dismissed INTEGER NOT NULL, expire_time INTEGER NOT NULL`)
41. `home_notice_new_table` (`id TEXT PRIMARY KEY, scene TEXT, payload_json TEXT, show_count INTEGER NOT NULL, max_show_count INTEGER NOT NULL, last_show_time INTEGER NOT NULL`)
42. `baby_info_table` (`id INTEGER PRIMARY KEY AUTOINCREMENT, baby_name TEXT, birthday_timestamp INTEGER NOT NULL, gender INTEGER NOT NULL, avatar_path TEXT, watermark_id TEXT`)

---

## 2. Plane 2: OLAP Analytical Star Schema (8 Tables)

1. `dim_date` (`date_key TEXT PRIMARY KEY, full_date TEXT, year INTEGER, quarter INTEGER, month INTEGER, iso_week INTEGER, day_of_week TEXT, is_weekend INTEGER`)
2. `dim_geography` (`geo_key TEXT PRIMARY KEY, country_code TEXT, country_name TEXT, city TEXT, district TEXT, grid_key TEXT, center_lat REAL, center_lng REAL`)
3. `dim_teamspace` (`team_key TEXT PRIMARY KEY, team_name TEXT, industry_sector TEXT, plan_tier TEXT, seat_quota INTEGER, storage_quota_gb REAL`)
4. `dim_watermark_template` (`watermark_key TEXT PRIMARY KEY, base_id TEXT, title TEXT, category_name TEXT, has_ai_cv INTEGER, cv_model_name TEXT`)
5. `fact_photo_captures` (`fact_id TEXT PRIMARY KEY, date_key TEXT, geo_key TEXT, team_key TEXT, watermark_key TEXT, photo_count INTEGER, ai_rebar_detected_sum INTEGER, avg_ntp_drift_ms REAL, avg_gps_satellites REAL, aegis_verified_count INTEGER`)
6. `fact_attendance_events` (`fact_id TEXT PRIMARY KEY, date_key TEXT, geo_key TEXT, team_key TEXT, project_id TEXT, checkin_count INTEGER, checkout_count INTEGER, inside_geofence_count INTEGER, avg_distance_meters REAL`)
7. `fact_revenue_transactions` (`fact_id TEXT PRIMARY KEY, date_key TEXT, geo_key TEXT, sku_id TEXT, channel TEXT, gross_amount_usd REAL, net_amount_usd REAL, is_trial_conversion INTEGER`)
8. `fact_robotics_mcap_telemetry` (`fact_id TEXT PRIMARY KEY, date_key TEXT, geo_key TEXT, task_id TEXT, mcap_file_count INTEGER, total_duration_sec REAL, avg_hand_landmark_conf REAL, reward_amount_brl REAL, reward_amount_mxn REAL`)
