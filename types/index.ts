export interface User {
  id?: number;
  username: string;
  password?: string;
  fullName: string;
  role: 'Super Admin' | 'Admin' | 'Team Member';
  position: string;
  bio: string;
  skills: string; // Comma-separated
  portfolioLinks: string; // Comma-separated
  contactInfo: string;
  instagram?: string;
  facebook?: string;
  whatsapp?: string;
  youtube?: string;
  linkedin?: string;
  website?: string;
  avatarId: number;
  earnedBadges: string; // Comma-separated list
  completedProjectsCount: number;
  performanceScore: number;
  status: 'Active' | 'Suspended';
  coverBannerColor: 'Slate' | 'NeonPink' | 'CosmicBlue' | 'GoldGlow' | 'Emerald';
  missionStatement?: string;
  featuredProjects?: string; // Comma-separated list
  joinedDate: string;
}

export interface Project {
  id: number;
  title: string;
  description: string;
  createdBy: string;
  assignedTo: string; // Blank if unassigned
  status: 'Pending' | 'In Progress' | 'Review' | 'Completed';
  creationDate: number;
  completionDate: number;
  fileUrls: string; // Comma-separated list
  submissionNote: string;
  feedback: string;
}

export interface Announcement {
  id: number;
  title: string;
  content: string;
  author: string;
  authorRole: string;
  timestamp: number;
  priority: 'Normal' | 'High';
}

export interface DiscussionMessage {
  id: number;
  author: string;
  authorRole: string;
  messageText: string;
  timestamp: number;
  recipient: string; // "All" or a specific username
}

export interface ResourceShare {
  id: number;
  title: string;
  category: 'PSD Assets' | 'Vector Fonts' | 'Mockups' | 'Inspiration';
  link: string;
  sharedBy: string;
  timestamp: number;
}

export interface Notification {
  id: number;
  title: string;
  message: string;
  targetUsername: string; // "All", "Admin", a specific username
  read: boolean;
  timestamp: number;
  priority: 'Low' | 'Normal' | 'High' | 'Urgent';
  type: string;
  bannerPreset: string;
  badgeIcon: string;
  isScheduled: boolean;
  scheduledTime: number;
  readCountSimulated: number;
  showAsPushOverlay: boolean;
}

export interface ProjectComment {
  id: number;
  projectId: number;
  author: string;
  authorRole: string;
  text: string;
  timestamp: number;
}
