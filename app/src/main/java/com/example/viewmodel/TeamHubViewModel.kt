package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.Announcement
import com.example.data.DiscussionMessage
import com.example.data.Notification
import com.example.data.Project
import com.example.data.ProjectComment
import com.example.data.Repository
import com.example.data.ResourceShare
import com.example.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TeamHubViewModel(application: Application) : AndroidViewModel(application) {

    private val database: AppDatabase = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "dml_team_hub_db"
    ).fallbackToDestructiveMigration().build()

    private val repository = Repository(database)

    // Users
    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Projects
    val allProjects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Announcements
    val allAnnouncements: StateFlow<List<Announcement>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Discussion Board Messages
    val recentDiscussionMessages: StateFlow<List<DiscussionMessage>> = repository.recentDiscussionMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Shared Resources
    val allResources: StateFlow<List<ResourceShare>> = repository.allResources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Authentication State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Viewed Profile State (Universal Viewer)
    private val _viewedUserProfile = MutableStateFlow<User?>(null)
    val viewedUserProfile: StateFlow<User?> = _viewedUserProfile.asStateFlow()

    fun showProfile(user: User) {
        _viewedUserProfile.value = user
    }

    fun showProfileByUsername(username: String) {
        viewModelScope.launch {
            val user = repository.getUserByUsername(username)
            if (user != null) {
                _viewedUserProfile.value = user
            }
        }
    }

    fun closeProfile() {
        _viewedUserProfile.value = null
    }

    // Advanced Notification dispatch
    fun createDetailedNotification(
        title: String,
        message: String,
        targetUsername: String,
        priority: String,
        type: String,
        bannerPreset: String,
        badgeIcon: String,
        isScheduled: Boolean,
        scheduledTime: Long
    ) {
        val current = _currentUser.value ?: return
        if (current.role != "Admin" && current.role != "Super Admin") return

        viewModelScope.launch {
            repository.createDetailedNotification(
                title = title,
                message = message,
                targetUsername = targetUsername,
                priority = priority,
                type = type,
                bannerPreset = bannerPreset,
                badgeIcon = badgeIcon,
                isScheduled = isScheduled,
                scheduledTime = scheduledTime
            )
        }
    }

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // Active Notifications
    val userNotifications: StateFlow<List<Notification>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getNotificationsForUser(user.username)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Pre-populate data and auto-login superadmin for a slick initial view (or wait for user login)
        viewModelScope.launch {
            repository.populateDefaultsIfNeeded()
            // Optional: Auto-login superadmin, but let's keep it clean on login screen unless session details are found
        }
    }

    // AUTH ACTIONS
    fun login(username: String, pword: String) {
        viewModelScope.launch {
            val trimmedUsername = username.trim()
            var user = repository.getUserByUsername(trimmedUsername)
            
            // Auto register dynamic member if password matches the dynamic pattern
            if (user == null && trimmedUsername.isNotEmpty()) {
                val expectedPassword = "DML@${trimmedUsername}2026#"
                if (pword == expectedPassword) {
                    val formattedName = trimmedUsername.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
                    val autoCreated = User(
                        username = trimmedUsername,
                        password = expectedPassword,
                        fullName = "$formattedName Designer",
                        role = "Team Member",
                        position = "UI & Graphic Specialist",
                        bio = "Team member of DM Flow by DesignMastery_Lab. Converting creative ideas into professional designs.",
                        skills = "Figma, Illustrator, Photoshop, Branding",
                        avatarId = (3..6).random()
                    )
                    repository.insertUser(autoCreated)
                    user = autoCreated
                }
            }

            if (user == null) {
                _loginError.value = "Username not found"
            } else if (user.status == "Suspended") {
                _loginError.value = "Account suspended. Reach contact outreach."
            } else if (user.password != pword) {
                _loginError.value = "Incorrect password"
            } else {
                _currentUser.value = user
                _loginError.value = null
                // Trigger login notification
                repository.createNotification(
                    title = "Successful Login",
                    message = "${user.fullName} authenticated securely.",
                    targetUsername = user.username
                )
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _loginError.value = null
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    fun changePassword(oldP: String, newP: String, onResult: (Boolean, String) -> Unit) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false, "No active login session")
            return
        }

        if (user.password != oldP) {
            onResult(false, "Current password does not match")
            return
        }

        if (newP.length < 4) {
            onResult(false, "Password must be at least 4 characters")
            return
        }

        viewModelScope.launch {
            val updatedUser = user.copy(password = newP)
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
            onResult(true, "Password successfully updated!")
            repository.createNotification(
                title = "Security Alert",
                message = "Your password has been changed successfully.",
                targetUsername = user.username
            )
        }
    }

    // SUPER ADMIN ADMIN MANAGEMENT
    fun createAdminAccount(adminUsername: String, adminP: String, fullName: String, position: String, contactInfo: String) {
        val current = _currentUser.value
        if (current == null || current.role != "Super Admin") return

        viewModelScope.launch {
            val existing = repository.getUserByUsername(adminUsername.trim())
            if (existing != null) {
                repository.createNotification(
                    title = "Admin Account Creation Failed",
                    message = "Username \"${adminUsername}\" already exists.",
                    targetUsername = current.username
                )
                return@launch
            }

            val newAdmin = User(
                username = adminUsername.trim(),
                password = adminP,
                fullName = fullName,
                role = "Admin",
                position = position.ifEmpty { "Admin" },
                contactInfo = contactInfo,
                earnedBadges = "Logo Specialist, Team Leader"
            )
            repository.insertUser(newAdmin)
            repository.createNotification(
                title = "Admin Created",
                message = "Admin account \"${adminUsername}\" has been successfully created by Super Admin.",
                targetUsername = "All"
            )
        }
    }

    fun resetAdminPassword(adminUsername: String, adminNewP: String) {
        val current = _currentUser.value
        if (current == null || current.role != "Super Admin") return

        viewModelScope.launch {
            val adminUser = repository.getUserByUsername(adminUsername)
            if (adminUser != null) {
                val updated = adminUser.copy(password = adminNewP)
                repository.updateUser(updated)
                repository.createNotification(
                    title = "Password Reset",
                    message = "Password for Admin \"${adminUsername}\" was reset by Super Admin.",
                    targetUsername = adminUsername
                )
            }
        }
    }

    fun deleteAdminAccount(adminUser: User) {
        val current = _currentUser.value
        if (current == null || current.role != "Super Admin") return
        if (adminUser.role != "Admin") return

        viewModelScope.launch {
            repository.deleteUser(adminUser)
            repository.createNotification(
                title = "Admin Account Deleted",
                message = "Admin account \"${adminUser.username}\" has been removed.",
                targetUsername = current.username
            )
        }
    }

    // PROJECT ACTIONS
    fun createProject(title: String, description: String, assignedTo: String) {
        val current = _currentUser.value ?: return
        if (current.role != "Admin" && current.role != "Super Admin") return

        viewModelScope.launch {
            val project = Project(
                title = title,
                description = description,
                createdBy = current.username,
                assignedTo = assignedTo,
                status = if (assignedTo.isEmpty()) "Pending" else "In Progress"
            )
            repository.insertProject(project)
        }
    }

    fun updateProjectStatus(projectId: Int, status: String) {
        viewModelScope.launch {
            val project = repository.getProjectById(projectId) ?: return@launch
            val updated = project.copy(status = status)
            repository.updateProject(updated)
        }
    }

    fun claimProject(projectId: Int) {
        val current = _currentUser.value ?: return
        if (current.role != "Team Member") return

        viewModelScope.launch {
            val project = repository.getProjectById(projectId) ?: return@launch
            if (project.assignedTo.isEmpty()) {
                val updated = project.copy(assignedTo = current.username, status = "In Progress")
                repository.updateProject(updated)
                repository.createNotification(
                    title = "Project Claimed",
                    message = "Designer ${current.fullName} claimed project \"${project.title}\".",
                    targetUsername = "All"
                )
            }
        }
    }

    fun submitProjectWork(projectId: Int, fileNames: String, notes: String) {
        val current = _currentUser.value ?: return
        viewModelScope.launch {
            val project = repository.getProjectById(projectId) ?: return@launch
            val updated = project.copy(
                fileUrls = fileNames,
                submissionNote = notes,
                status = "Review"
            )
            repository.updateProject(updated)
            repository.createNotification(
                title = "Work For Review",
                message = "${current.fullName} uploaded files for project \"${project.title}\".",
                targetUsername = "All" // will alert Admins
            )
        }
    }

    fun approveSubmission(projectId: Int, approvalComments: String) {
        val current = _currentUser.value ?: return
        if (current.role != "Admin" && current.role != "Super Admin") return

        viewModelScope.launch {
            val project = repository.getProjectById(projectId) ?: return@launch
            val updated = project.copy(
                status = "Completed",
                feedback = approvalComments,
                completionDate = System.currentTimeMillis()
            )
            repository.updateProject(updated)

            // Award stats to user
            if (project.assignedTo.isNotEmpty()) {
                val assignedUser = repository.getUserByUsername(project.assignedTo)
                if (assignedUser != null) {
                    val revisedStats = assignedUser.copy(
                        completedProjectsCount = assignedUser.completedProjectsCount + 1,
                        performanceScore = (assignedUser.performanceScore + 3).coerceAtMost(100)
                    )
                    repository.updateUser(revisedStats)
                }
            }
        }
    }

    fun rejectSubmission(projectId: Int, rejectionComments: String) {
        val current = _currentUser.value ?: return
        if (current.role != "Admin" && current.role != "Super Admin") return

        viewModelScope.launch {
            val project = repository.getProjectById(projectId) ?: return@launch
            val updated = project.copy(
                status = "In Progress",
                feedback = rejectionComments
            )
            repository.updateProject(updated)
            
            // Adjust performance slightly for rework
            if (project.assignedTo.isNotEmpty()) {
                val assignedUser = repository.getUserByUsername(project.assignedTo)
                if (assignedUser != null) {
                    val revisedStats = assignedUser.copy(
                        performanceScore = (assignedUser.performanceScore - 2).coerceAtLeast(60)
                    )
                    repository.updateUser(revisedStats)
                }
            }
        }
    }

    fun removeProject(project: Project) {
        val current = _currentUser.value ?: return
        if (current.role != "Admin" && current.role != "Super Admin") return

        viewModelScope.launch {
            repository.deleteProject(project)
            repository.createNotification(
                title = "Project Removed",
                message = "Project \"${project.title}\" has been deleted.",
                targetUsername = current.username
            )
        }
    }

    // BADGE SYSTEM RULES
    fun awardBadge(targetUsername: String, badgeName: String) {
        val current = _currentUser.value ?: return
        if (current.role != "Admin" && current.role != "Super Admin") return

        viewModelScope.launch {
            val target = repository.getUserByUsername(targetUsername) ?: return@launch
            val currentBadges = target.earnedBadges.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            
            if (!currentBadges.contains(badgeName)) {
                currentBadges.add(badgeName)
                val updated = target.copy(
                    earnedBadges = currentBadges.joinToString(", "),
                    performanceScore = (target.performanceScore + 5).coerceAtMost(100)
                )
                repository.updateUser(updated)
                repository.createNotification(
                    title = "Badge Awarded!",
                    message = "${target.fullName} was awarded the [${badgeName}] badge by ${current.fullName}.",
                    targetUsername = targetUsername
                )
            }
        }
    }

    fun removeBadge(targetUsername: String, badgeName: String) {
        val current = _currentUser.value ?: return
        if (current.role != "Admin" && current.role != "Super Admin") return

        viewModelScope.launch {
            val target = repository.getUserByUsername(targetUsername) ?: return@launch
            val currentBadges = target.earnedBadges.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            
            if (currentBadges.contains(badgeName)) {
                currentBadges.remove(badgeName)
                val updated = target.copy(
                    earnedBadges = currentBadges.joinToString(", ")
                )
                repository.updateUser(updated)
                repository.createNotification(
                    title = "Badge Revoked",
                    message = "[${badgeName}] badge was removed from your profile.",
                    targetUsername = targetUsername
                )
            }
        }
    }

    // MEMBER CONTROLS
    fun toggleSuspendMember(targetUser: User) {
        val current = _currentUser.value ?: return
        if (current.role != "Admin" && current.role != "Super Admin") return

        viewModelScope.launch {
            val newStatus = if (targetUser.status == "Active") "Suspended" else "Active"
            val updated = targetUser.copy(status = newStatus)
            repository.updateUser(updated)
            repository.createNotification(
                title = if (newStatus == "Suspended") "Account Suspended" else "Account Restored",
                message = "Member account \"${targetUser.username}\" is now ${newStatus.lowercase()} by ${current.fullName}.",
                targetUsername = "All"
            )
        }
    }

    // PROFILE MANAGEMENT
    fun updateProfile(
        fullName: String,
        bio: String,
        skills: String,
        portfolioLinks: String,
        instagram: String,
        facebook: String,
        whatsapp: String,
        youtube: String,
        linkedin: String,
        website: String,
        contactInfo: String,
        avatarId: Int,
        targetUsername: String? = null // Super Admin can edit anyone, other can only edit themselves
    ) {
        val sessionUser = _currentUser.value ?: return
        val effectiveUsername = targetUsername ?: sessionUser.username
        
        // Validation check for authorization
        if (sessionUser.username != effectiveUsername && sessionUser.role != "Super Admin") {
            // Member/Admin trying to edit somebody else
            return
        }

        viewModelScope.launch {
            val dbUser = repository.getUserByUsername(effectiveUsername) ?: return@launch
            val updated = dbUser.copy(
                fullName = fullName,
                bio = bio,
                skills = skills,
                portfolioLinks = portfolioLinks,
                instagram = instagram,
                facebook = facebook,
                whatsapp = whatsapp,
                youtube = youtube,
                linkedin = linkedin,
                website = website,
                contactInfo = contactInfo,
                avatarId = avatarId
            )
            repository.updateUser(updated)
            
            // If editing own profile, also update currentUser stateflow
            if (sessionUser.username == effectiveUsername) {
                _currentUser.value = updated
            }
            
            repository.createNotification(
                title = "Profile Updated",
                message = "Your hub portfolio cards are updated successfully.",
                targetUsername = effectiveUsername
            )
        }
    }

    // TEAM AREA ACTIONS
    fun postAnnouncement(title: String, content: String, priority: String) {
        val current = _currentUser.value ?: return
        // Admind/SuperAdmin only
        if (current.role != "Admin" && current.role != "Super Admin") return

        viewModelScope.launch {
            val announcement = Announcement(
                title = title,
                content = content,
                author = current.fullName,
                authorRole = current.role,
                priority = priority
            )
            repository.insertAnnouncement(announcement)
        }
    }

    fun removeAnnouncement(announcementId: Int) {
        val current = _currentUser.value ?: return
        if (current.role != "Admin" && current.role != "Super Admin") return

        viewModelScope.launch {
            repository.deleteAnnouncementById(announcementId)
        }
    }

    fun postDiscussionMessage(text: String, recipient: String = "All") {
        val current = _currentUser.value ?: return
        if (text.trim().isEmpty()) return

        viewModelScope.launch {
            val msg = DiscussionMessage(
                author = current.username,
                authorRole = current.role,
                messageText = text.trim(),
                recipient = recipient
            )
            repository.insertMessage(msg)
        }
    }

    fun shareResource(title: String, category: String, link: String) {
        val current = _currentUser.value ?: return
        if (title.trim().isEmpty() || link.trim().isEmpty()) return

        viewModelScope.launch {
            val res = ResourceShare(
                title = title.trim(),
                category = category,
                link = link.trim(),
                sharedBy = current.username
            )
            repository.insertResource(res)
            repository.createNotification(
                title = "New Resource Shared",
                message = "${current.fullName} shared visual resource: \"${title}\".",
                targetUsername = "All"
            )
        }
    }

    fun deleteResource(id: Int) {
        viewModelScope.launch {
            repository.deleteResourceById(id)
        }
    }

    fun editProjectDetails(projectId: Int, title: String, description: String, assignedTo: String) {
        viewModelScope.launch {
            val project = repository.getProjectById(projectId) ?: return@launch
            val updated = project.copy(
                title = title.trim(),
                description = description.trim(),
                assignedTo = assignedTo
            )
            repository.updateProject(updated)
            repository.createNotification(
                title = "Project Briefing Updated",
                message = "The project brief of \"${updated.title}\" has been modified by the administrator.",
                targetUsername = if (assignedTo.isNotEmpty()) assignedTo else "All"
            )
        }
    }

    fun editAnnouncementDetails(annId: Int, title: String, content: String, priority: String) {
        viewModelScope.launch {
            val current = _currentUser.value ?: return@launch
            val updated = Announcement(
                id = annId,
                title = title.trim(),
                content = content.trim(),
                author = current.fullName,
                authorRole = current.role,
                timestamp = System.currentTimeMillis(),
                priority = priority
            )
            repository.insertAnnouncement(updated)
        }
    }

    fun sendDirectNotification(title: String, message: String, targetUsername: String) {
        viewModelScope.launch {
            repository.createNotification(
                title = title.trim(),
                message = message.trim(),
                targetUsername = targetUsername
            )
        }
    }

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    // PROJECT COMMENTS
    fun getCommentsForProject(projectId: Int): kotlinx.coroutines.flow.Flow<List<ProjectComment>> {
        return repository.getCommentsForProject(projectId)
    }

    fun postProjectComment(projectId: Int, text: String) {
        viewModelScope.launch {
            val current = _currentUser.value ?: return@launch
            if (text.trim().isEmpty()) return@launch

            val comment = ProjectComment(
                projectId = projectId,
                author = current.username,
                authorRole = current.role,
                text = text.trim()
            )

            repository.insertProjectComment(comment)

            // Dynamic notification triggers
            val project = repository.getProjectById(projectId)
            if (project != null) {
                // If it was posted by a team member, notify creator/admin
                // If by admin, notify the assigned member
                val notifyTarget = if (current.role == "Team Member") {
                    if (project.createdBy.isNotEmpty()) project.createdBy else "All"
                } else {
                    if (project.assignedTo.isNotEmpty()) project.assignedTo else "All"
                }

                if (notifyTarget.isNotEmpty() && notifyTarget != current.username) {
                    repository.createNotification(
                        title = "New Project Feedback/Comment",
                        message = "${current.fullName} added comment in project \"${project.title}\": \"${text.trim().take(35)}...\"",
                        targetUsername = notifyTarget
                    )
                }
            }
        }
    }

    fun deleteProjectComment(commentId: Int) {
        viewModelScope.launch {
            repository.deleteProjectCommentById(commentId)
        }
    }
}
