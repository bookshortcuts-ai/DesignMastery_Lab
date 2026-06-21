package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String,
    val fullName: String,
    val role: String, // "Super Admin", "Admin", "Team Member"
    val position: String, // e.g. "Owner / Founder", "Admin", "Brand Designer"
    val bio: String = "",
    val skills: String = "", // Comma-separated skills
    val portfolioLinks: String = "", // Comma-separated links
    val contactInfo: String = "", // Email or outreach info
    val instagram: String = "",
    val facebook: String = "",
    val whatsapp: String = "", // Direct WhatsApp number (without + or 00, e.g. "12345678")
    val youtube: String = "",
    val linkedin: String = "",
    val website: String = "",
    val avatarId: Int = 1, // 1 to 5 to map to beautiful drawable colors / initials
    val earnedBadges: String = "", // Comma-separated list of badges (e.g., "Founder Crown Badge", "Elite Admin Badge", etc.)
    val completedProjectsCount: Int = 0,
    val performanceScore: Int = 85, // out of 100
    val status: String = "Active", // "Active" or "Suspended"
    val coverBannerColor: String = "Slate", // Slate, NeonPink, CosmicBlue, GoldGlow, Emerald
    val missionStatement: String = "", // Super Admin feature
    val featuredProjects: String = "", // Super Admin featured portfolio list (comma separated)
    val customSocialLinks: String = "", // Custom key-value representation, e.g., "Behance:link,Dribbble:link"
    val joinedDate: String = "Jun 2026"
)

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val createdBy: String,
    val assignedTo: String = "", // Blank if unassigned (available to claim)
    val status: String = "Pending", // "Pending", "In Progress", "Review", "Completed"
    val creationDate: Long = System.currentTimeMillis(),
    val completionDate: Long = 0L,
    val fileUrls: String = "", // Comma-separated simulation of uploaded files (e.g. "poster_revised.psd, banner.png")
    val submissionNote: String = "", // Text uploaded with mock-file submission
    val feedback: String = "" // Reject / Approve admin comments
)

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val author: String,
    val authorRole: String = "Admin",
    val timestamp: Long = System.currentTimeMillis(),
    val priority: String = "Normal" // "Normal", "High"
)

@Entity(tableName = "discussion_messages")
data class DiscussionMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val author: String,
    val authorRole: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val recipient: String = "All"
)

@Entity(tableName = "resource_shares")
data class ResourceShare(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "PSD Assets", "Vector Fonts", "Mockups", "Inspiration"
    val link: String,
    val sharedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val targetUsername: String = "All", // "All" or a specific username or roles like "Admin" or "Team Member"
    val read: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val priority: String = "Normal", // "Low", "Normal", "High", "Urgent"
    val type: String = "Announcement", // "Announcement", "Project Assigned", "Project Deadline", "Project Approved", "Revision Required", "Badge Awarded", "Team Update", "Important Alert", "System Update", "Custom"
    val bannerPreset: String = "", // Simulated design banners, e.g. "Sunset Neon", "Classic Gold", "Sleek Charcoal"
    val badgeIcon: String = "Comment", // Selected icon graphic identifier, e.g. "Comment", "Shield", "Crown", "Star", "Alert", "Check"
    val isScheduled: Boolean = false,
    val scheduledTime: Long = 0L,
    val readCountSimulated: Int = 0, // Analytic read simulation statistics
    val showAsPushOverlay: Boolean = true // Flag to show real-time on-screen banner in the client
)

@Entity(tableName = "project_comments")
data class ProjectComment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val author: String,
    val authorRole: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

