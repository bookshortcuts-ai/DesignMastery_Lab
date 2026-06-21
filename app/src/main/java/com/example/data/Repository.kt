package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(private val database: AppDatabase) {

    private val userDao = database.userDao()
    private val projectDao = database.projectDao()
    private val announcementDao = database.announcementDao()
    private val discussionDao = database.discussionDao()
    private val resourceDao = database.resourceDao()
    private val notificationDao = database.notificationDao()
    private val projectCommentDao = database.projectCommentDao()

    // Flows
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()
    val allAnnouncements: Flow<List<Announcement>> = announcementDao.getAllAnnouncements()
    val recentDiscussionMessages: Flow<List<DiscussionMessage>> = discussionDao.getRecentMessages()
    val allResources: Flow<List<ResourceShare>> = resourceDao.getAllResources()

    fun getCommentsForProject(projectId: Int): Flow<List<ProjectComment>> {
        return projectCommentDao.getCommentsForProject(projectId)
    }

    fun getNotificationsForUser(username: String): Flow<List<Notification>> {
        return notificationDao.getNotificationsForUser(username)
    }

    // User Operations
    suspend fun getUserByUsername(username: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserByUsername(username)
    }

    suspend fun insertUser(user: User): Long = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: User) = withContext(Dispatchers.IO) {
        userDao.deleteUser(user)
    }

    suspend fun deleteUserById(id: Int) = withContext(Dispatchers.IO) {
        userDao.deleteUserById(id)
    }

    // Project Operations
    suspend fun getProjectById(id: Int): Project? = withContext(Dispatchers.IO) {
        projectDao.getProjectById(id)
    }

    suspend fun insertProject(project: Project): Long = withContext(Dispatchers.IO) {
        val id = projectDao.insertProject(project)
        createNotification(
            title = "New Project Created",
            message = "Project \"${project.title}\" has been published.",
            targetUsername = if (project.assignedTo.isNotEmpty()) project.assignedTo else "All"
        )
        id
    }

    suspend fun updateProject(project: Project) = withContext(Dispatchers.IO) {
        val oldProject = projectDao.getProjectById(project.id)
        projectDao.updateProject(project)
        
        // Notify of changes
        if (oldProject != null && oldProject.status != project.status) {
            val notifyDest = if (project.assignedTo.isNotEmpty()) project.assignedTo else "All"
            createNotification(
                title = "Project Status Updated",
                message = "\"${project.title}\" is now in status ${project.status}.",
                targetUsername = notifyDest
            )
        }
    }

    suspend fun deleteProject(project: Project) = withContext(Dispatchers.IO) {
        projectDao.deleteProject(project)
    }

    // Announcement Operations
    suspend fun insertAnnouncement(announcement: Announcement): Long = withContext(Dispatchers.IO) {
        val id = announcementDao.insertAnnouncement(announcement)
        createNotification(
            title = "New Announcement",
            message = announcement.title,
            targetUsername = "All"
        )
        id
    }

    suspend fun deleteAnnouncementById(id: Int) = withContext(Dispatchers.IO) {
        announcementDao.deleteAnnouncementById(id)
    }

    // Discussion Board Operations
    suspend fun insertMessage(message: DiscussionMessage): Long = withContext(Dispatchers.IO) {
        discussionDao.insertMessage(message)
    }

    // Resource Sharing Operations
    suspend fun insertResource(resource: ResourceShare): Long = withContext(Dispatchers.IO) {
        resourceDao.insertResource(resource)
    }

    suspend fun deleteResourceById(id: Int) = withContext(Dispatchers.IO) {
        resourceDao.deleteResourceById(id)
    }

    // Notification Operations
    suspend fun createNotification(title: String, message: String, targetUsername: String) = withContext(Dispatchers.IO) {
        notificationDao.insertNotification(
            Notification(
                title = title,
                message = message,
                targetUsername = targetUsername
            )
        )
    }

    suspend fun createDetailedNotification(
        title: String,
        message: String,
        targetUsername: String,
        priority: String,
        type: String,
        bannerPreset: String,
        badgeIcon: String,
        isScheduled: Boolean,
        scheduledTime: Long
    ) = withContext(Dispatchers.IO) {
        notificationDao.insertNotification(
            Notification(
                title = title,
                message = message,
                targetUsername = targetUsername,
                priority = priority,
                type = type,
                bannerPreset = bannerPreset,
                badgeIcon = badgeIcon,
                isScheduled = isScheduled,
                scheduledTime = scheduledTime,
                readCountSimulated = if (isScheduled) 0 else (1..5).random()
            )
        )
    }

    suspend fun markNotificationAsRead(id: Int) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    // Comment Operations
    suspend fun insertProjectComment(comment: ProjectComment): Long = withContext(Dispatchers.IO) {
        projectCommentDao.insertComment(comment)
    }

    suspend fun deleteProjectCommentById(id: Int) = withContext(Dispatchers.IO) {
        projectCommentDao.deleteCommentById(id)
    }

    // Populate initial default data if database is empty
    suspend fun populateDefaultsIfNeeded() = withContext(Dispatchers.IO) {
        val existingUsers = userDao.getAllUsers().firstOrNull()
        if (existingUsers.isNullOrEmpty()) {
            // 1. Super Admin
            userDao.insertUser(User(
                username = "superadmin",
                password = "DML@Toli@2008",
                fullName = "DesignMastery_Lab Founder",
                role = "Super Admin",
                position = "Owner / Founder",
                bio = "Founder & Creative Director of DesignMastery_Lab. Building high fidelity branding and state of the art visual experiences worldwide.",
                skills = "Creative Direction, Branding, Identity Design, Photoshop, Illustrator, UI/UX Strategy",
                portfolioLinks = "https://designmasterylab.com/portfolio, https://behance.net/dml_design",
                contactInfo = "founder@designmasterylab.com",
                instagram = "designmastery_lab",
                facebook = "designmasterylab",
                whatsapp = "9012345678",
                youtube = "designmasterylab",
                linkedin = "designmastery-lab",
                website = "https://designmasterylab.com",
                avatarId = 1,
                earnedBadges = "Team Leader, Creative Expert"
            ))

            // 2. Admin
            userDao.insertUser(User(
                username = "admin",
                password = "DML@2008#2026",
                fullName = "DesignMastery_Lab Admin",
                role = "Admin",
                position = "Lead Operations & Quality",
                bio = "Quality reviewer and coordinator. Directing creative assets to meet client expectations.",
                skills = "Typography, Client Pitching, QA Review, Layout Optimization",
                portfolioLinks = "https://behance.net/dml_admin",
                contactInfo = "admin@designmasterylab.com",
                instagram = "dml_admin",
                facebook = "dml_admin_page",
                whatsapp = "9543210987",
                avatarId = 2,
                earnedBadges = "Team Leader, Fast Delivery"
            ))

            // 3. Team Member 1 (John Vector)
            userDao.insertUser(User(
                username = "john_vector",
                password = "DML@john_vector2026#",
                fullName = "John Vector",
                role = "Team Member",
                position = "Senior Vector Illustrator",
                bio = "Illustrator specialist focusing on branding typography and geometry vectors.",
                skills = "Illustrator, Identity Design, SVG, SVG Art",
                portfolioLinks = "https://behance.net/john_vector",
                contactInfo = "john.vector@dml.com",
                instagram = "john_vector",
                whatsapp = "12341234123",
                avatarId = 3,
                earnedBadges = "Logo Specialist, Fast Delivery",
                completedProjectsCount = 12,
                performanceScore = 92
            ))

            // 4. Team Member 2 (Sarah Pixels)
            userDao.insertUser(User(
                username = "sarah_pixels",
                password = "DML@sarah_pixels2026#",
                fullName = "Sarah Pixels",
                role = "Team Member",
                position = "Lead Thumbnail & Packaging Designer",
                bio = "Crafting high click-through-rate YouTube thumbnails and agency kit assets.",
                skills = "Photoshop, YouTube Thumbnails, Media Kits, Color Layout",
                portfolioLinks = "https://behance.net/sarah_pixels",
                contactInfo = "sarah.pixels@dml.com",
                instagram = "sarah_pixels",
                whatsapp = "5551234567",
                avatarId = 4,
                earnedBadges = "Thumbnail Master, Top Designer, Creative Expert",
                completedProjectsCount = 28,
                performanceScore = 98
            ))

            // 5. Team Member 3 (Mike Frame)
            userDao.insertUser(User(
                username = "mike_frame",
                password = "DML@mike_frame2026#",
                fullName = "Mike Frame",
                role = "Team Member",
                position = "Social Media Designer",
                bio = "Video content visual coordinator. Creating promotional slides, dynamic posts, and banners.",
                skills = "After Effects, Instagram Reels, Banner Design, Motion Layout",
                portfolioLinks = "https://vimeo.com/mike_frame",
                contactInfo = "mike.frame@dml.com",
                avatarId = 5,
                earnedBadges = "Social Media Expert, Fast Delivery",
                completedProjectsCount = 15,
                performanceScore = 89
            ))

            // 6. Dynamic/Named Team Member 4 (Jatin)
            userDao.insertUser(User(
                username = "Jatin",
                password = "DML@Jatin2026#",
                fullName = "Jatin Yogi",
                role = "Super Admin",
                position = "Owner / Founder",
                bio = "Sleek brand architecture and visual styling curation. Transforming creative ideas into premium design layouts with a focus on Material Design 3 and responsive aesthetics.",
                skills = "Branding, Typography, vector styling, Material 3 layouts, UI/UX Strategy",
                portfolioLinks = "https://behance.net/jatin_dml, https://github.com/jatin-dml",
                contactInfo = "jatin@dml.com",
                avatarId = 6,
                earnedBadges = "Founder Crown Badge, Creative Expert, Top Designer, Team Leader",
                completedProjectsCount = 42,
                performanceScore = 100,
                coverBannerColor = "GoldGlow",
                missionStatement = "To convert beautiful creative ideas into digital realities and empower team members to reach peak execution mastery.",
                featuredProjects = "Interactive UI Kit, Cosmic Design System, Vector Logo Deck"
            ))

            // Admin initial projects
            projectDao.insertProject(Project(
                title = "Fintech App Logo Redesign",
                description = "Create a premium vector brandmark for high scalability. Delivery must be strictly in pristine AI and SVG source files, representing secure growth.",
                createdBy = "admin",
                assignedTo = "john_vector",
                status = "In Progress",
                creationDate = System.currentTimeMillis() - 86400000 * 3, // 3 days ago
                fileUrls = "fintech_logo_draft1.ai",
                submissionNote = "Attached is the first rough concept centered on ascending bars."
            ))

            projectDao.insertProject(Project(
                title = "DM_Lab High-CTR YouTube Thumbnail",
                description = "Generate 3 high-impact concept design options for the new tech review series. Large text hierarchy, strong contrast background and high sharpness. Provide PSD files.",
                createdBy = "admin",
                assignedTo = "sarah_pixels",
                status = "Review",
                creationDate = System.currentTimeMillis() - 86400000 * 2, // 2 days ago
                fileUrls = "youtube_tech_v1.psd, preview_draft.jpg",
                submissionNote = "Three versions finalized. Highly prominent neon text accents used!"
            ))

            projectDao.insertProject(Project(
                title = "Minimalist Packaging Box Graphics",
                description = "Modern visual graphics for a cosmetic box packaging design. Light pastel theme background with refined typography. Clean modern layout required.",
                createdBy = "admin",
                assignedTo = "", // available to claim!
                status = "Pending",
                creationDate = System.currentTimeMillis() - 3600000 // 1 hour ago
            ))

            // Initial announcements
            announcementDao.insertAnnouncement(Announcement(
                title = "Welcome to DesignMastery_Lab Team Hub!",
                content = "We have launched our brand-new operational portal! All designers can now claim available brand briefings, review assigned work, post announcements, resource links, and showcase earned performance badges on our leaderboard. Keep your profiles updated!",
                author = "Super Admin (Founder)",
                authorRole = "Super Admin",
                priority = "High"
            ))

            announcementDao.insertAnnouncement(Announcement(
                title = "New Client Requirement: Branding Kits",
                content = "Multiple boutique packaging clients are joining this month. Keep your Adobe Illustrator toolkits ready. Ensure all typography licenses are cleared.",
                author = "Lead Operations",
                authorRole = "Admin",
                priority = "Normal"
            ))

            // Initial Resources
            resourceDao.insertResource(ResourceShare(
                title = "Ultra High-Res Showcase Mockups PSD",
                category = "PSD Assets",
                link = "https://dml-storage.s3.amazonaws.com/mockups/ultra-box-bundle.zip",
                sharedBy = "superadmin"
            ))

            resourceDao.insertResource(ResourceShare(
                title = "Elite Elegant Fonts (Google Sans & Space Grotesk Custom Alternates)",
                category = "Vector Fonts",
                link = "https://dml-storage.s3.amazonaws.com/fonts/luxury-headings-v2.zip",
                sharedBy = "admin"
            ))

            // Initial Discussion Messages
            discussionDao.insertMessage(DiscussionMessage(
                author = "john_vector",
                authorRole = "Team Member",
                messageText = "Hello guys! Super excited to use this new dashboard. The Light Pink theme looks amazing!"
            ))
            discussionDao.insertMessage(DiscussionMessage(
                author = "sarah_pixels",
                authorRole = "Team Member",
                messageText = "Just submitted the YouTube thumbnail options for review. Let me know what you think, admin!"
            ))
            discussionDao.insertMessage(DiscussionMessage(
                author = "admin",
                authorRole = "Admin",
                messageText = "Great work Sarah! Reviewing them right now. I'll post the feedback in the project sheet."
            ))
        }
    }
}
