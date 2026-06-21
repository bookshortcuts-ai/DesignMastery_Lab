import { User, Project, Announcement, DiscussionMessage, ResourceShare, Notification } from '../types';

export const INITIAL_USERS: User[] = [
  {
    username: "superadmin",
    password: "DML@Toli@2008",
    fullName: "DesignMastery_Lab Founder",
    role: "Super Admin",
    position: "Owner / Founder",
    bio: "Founder & Creative Director of DesignMastery_Lab. Building high fidelity branding and state of the art visual experiences worldwide.",
    skills: "Creative Direction, Branding, Identity Design, Photoshop, Illustrator, UI/UX Strategy",
    portfolioLinks: "https://designmasterylab.com/portfolio, https://behance.net/dml_design",
    contactInfo: "founder@designmasterylab.com",
    instagram: "designmastery_lab",
    facebook: "designmasterylab",
    whatsapp: "9012345678",
    youtube: "designmasterylab",
    linkedin: "designmastery-lab",
    website: "https://designmasterylab.com",
    avatarId: 1,
    earnedBadges: "Team Leader, Creative Expert",
    completedProjectsCount: 5,
    performanceScore: 95,
    status: "Active",
    coverBannerColor: "Slate",
    joinedDate: "Jun 2026"
  },
  {
    username: "admin",
    password: "DML@2008#2026",
    fullName: "DesignMastery_Lab Admin",
    role: "Admin",
    position: "Lead Operations & Quality",
    bio: "Quality reviewer and coordinator. Directing creative assets to meet client expectations.",
    skills: "Typography, Client Pitching, QA Review, Layout Optimization",
    portfolioLinks: "https://behance.net/dml_admin",
    contactInfo: "admin@designmasterylab.com",
    instagram: "dml_admin",
    facebook: "dml_admin_page",
    whatsapp: "9543210987",
    avatarId: 2,
    earnedBadges: "Team Leader, Fast Delivery",
    completedProjectsCount: 2,
    performanceScore: 88,
    status: "Active",
    coverBannerColor: "CosmicBlue",
    joinedDate: "Jun 2026"
  },
  {
    username: "john_vector",
    password: "DML@john_vector2026#",
    fullName: "John Vector",
    role: "Team Member",
    position: "Senior Vector Illustrator",
    bio: "Illustrator specialist focusing on branding typography and geometry vectors.",
    skills: "Illustrator, Identity Design, SVG, SVG Art",
    portfolioLinks: "https://behance.net/john_vector",
    contactInfo: "john.vector@dml.com",
    instagram: "john_vector",
    whatsapp: "12341234123",
    avatarId: 3,
    earnedBadges: "Logo Specialist, Fast Delivery",
    completedProjectsCount: 12,
    performanceScore: 92,
    status: "Active",
    coverBannerColor: "Emerald",
    joinedDate: "Jun 2026"
  },
  {
    username: "sarah_pixels",
    password: "DML@sarah_pixels2026#",
    fullName: "Sarah Pixels",
    role: "Team Member",
    position: "Lead Thumbnail & Packaging Designer",
    bio: "Crafting high click-through-rate YouTube thumbnails and agency kit assets.",
    skills: "Photoshop, YouTube Thumbnails, Media Kits, Color Layout",
    portfolioLinks: "https://behance.net/sarah_pixels",
    contactInfo: "sarah.pixels@dml.com",
    instagram: "sarah_pixels",
    whatsapp: "5551234567",
    avatarId: 4,
    earnedBadges: "Thumbnail Master, Top Designer, Creative Expert",
    completedProjectsCount: 28,
    performanceScore: 98,
    status: "Active",
    coverBannerColor: "NeonPink",
    joinedDate: "Jun 2026"
  },
  {
    username: "mike_frame",
    password: "DML@mike_frame2026#",
    fullName: "Mike Frame",
    role: "Team Member",
    position: "Social Media Designer",
    bio: "Video content visual coordinator. Creating promotional slides, dynamic posts, and banners.",
    skills: "After Effects, Instagram Reels, Banner Design, Motion Layout",
    portfolioLinks: "https://vimeo.com/mike_frame",
    contactInfo: "mike.frame@dml.com",
    avatarId: 5,
    earnedBadges: "Social Media Expert, Fast Delivery",
    completedProjectsCount: 15,
    performanceScore: 89,
    status: "Active",
    coverBannerColor: "Slate",
    joinedDate: "Jun 2026"
  },
  {
    username: "Jatin",
    password: "DML@Jatin2026#",
    fullName: "Jatin Yogi",
    role: "Super Admin",
    position: "Owner / Founder",
    bio: "Sleek brand architecture and visual styling curation. Transforming creative ideas into premium design layouts with a focus on Material Design 3 and responsive aesthetics.",
    skills: "Branding, Typography, vector styling, Material 3 layouts, UI/UX Strategy",
    portfolioLinks: "https://behance.net/jatin_dml, https://github.com/jatin-dml",
    contactInfo: "jatin@dml.com",
    avatarId: 6,
    earnedBadges: "Founder Crown Badge, Creative Expert, Top Designer, Team Leader",
    completedProjectsCount: 42,
    performanceScore: 100,
    status: "Active",
    coverBannerColor: "GoldGlow",
    missionStatement: "To convert beautiful creative ideas into digital realities and empower team members to reach peak execution mastery.",
    featuredProjects: "Interactive UI Kit, Cosmic Design System, Vector Logo Deck",
    joinedDate: "Jun 2026"
  }
];

export const INITIAL_PROJECTS: Project[] = [
  {
    id: 1,
    title: "Fintech App Logo Redesign",
    description: "Create a premium vector brandmark for high scalability. Delivery must be strictly in pristine AI and SVG source files, representing secure growth.",
    createdBy: "admin",
    assignedTo: "john_vector",
    status: "In Progress",
    creationDate: Date.now() - 86400000 * 3,
    completionDate: 0,
    fileUrls: "fintech_logo_draft1.ai",
    submissionNote: "Attached is the first rough concept centered on ascending bars.",
    feedback: ""
  },
  {
    id: 2,
    title: "DM_Lab High-CTR YouTube Thumbnail",
    description: "Generate 3 high-impact concept design options for the new tech review series. Large text hierarchy, strong contrast background and high sharpness. Provide PSD files.",
    createdBy: "admin",
    assignedTo: "sarah_pixels",
    status: "Review",
    creationDate: Date.now() - 86400000 * 2,
    completionDate: 0,
    fileUrls: "youtube_tech_v1.psd, preview_draft.jpg",
    submissionNote: "Three versions finalized. Highly prominent neon text accents used!",
    feedback: ""
  },
  {
    id: 3,
    title: "Minimalist Packaging Box Graphics",
    description: "Modern visual graphics for a cosmetic box packaging design. Light pastel theme background with refined typography. Clean modern layout required.",
    createdBy: "admin",
    assignedTo: "",
    status: "Pending",
    creationDate: Date.now() - 3600000,
    completionDate: 0,
    fileUrls: "",
    submissionNote: "",
    feedback: ""
  }
];

export const INITIAL_ANNOUNCEMENTS: Announcement[] = [
  {
    id: 1,
    title: "Welcome to DesignMastery_Lab Team Hub!",
    content: "We have launched our brand-new operational portal! All designers can now claim available brand briefings, review assigned work, post announcements, resource links, and showcase earned performance badges on our leaderboard. Keep your profiles updated!",
    author: "Super Admin (Founder)",
    authorRole: "Super Admin",
    timestamp: Date.now() - 86400000 * 5,
    priority: "High"
  },
  {
    id: 2,
    title: "New Client Requirement: Branding Kits",
    content: "Multiple boutique packaging clients are joining this month. Keep your Adobe Illustrator toolkits ready. Ensure all typography licenses are cleared.",
    author: "Lead Operations",
    authorRole: "Admin",
    timestamp: Date.now() - 36400000 * 4,
    priority: "Normal"
  }
];

export const INITIAL_RESOURCES: ResourceShare[] = [
  {
    id: 1,
    title: "Ultra High-Res Showcase Mockups PSD",
    category: "PSD Assets",
    link: "https://dml-storage.s3.amazonaws.com/mockups/ultra-box-bundle.zip",
    sharedBy: "superadmin",
    timestamp: Date.now() - 86400000 * 3
  },
  {
    id: 2,
    title: "Elite Elegant Fonts (Google Sans & Space Grotesk Custom Alternates)",
    category: "Vector Fonts",
    link: "https://dml-storage.s3.amazonaws.com/fonts/luxury-headings-v2.zip",
    sharedBy: "admin",
    timestamp: Date.now() - 86400000 * 2
  }
];

export const INITIAL_DISCUSSIONS: DiscussionMessage[] = [
  {
    id: 1,
    author: "john_vector",
    authorRole: "Team Member",
    messageText: "Hello guys! Super excited to use this new dashboard. The interface is stunning!",
    timestamp: Date.now() - 3600000 * 3,
    recipient: "All"
  },
  {
    id: 2,
    author: "sarah_pixels",
    authorRole: "Team Member",
    messageText: "Just submitted the YouTube thumbnail options for review. Let me know what you think, admin!",
    timestamp: Date.now() - 3600000 * 2,
    recipient: "All"
  },
  {
    id: 3,
    author: "admin",
    authorRole: "Admin",
    messageText: "Great work Sarah! Reviewing them right now. I'll post feedback shortly.",
    timestamp: Date.now() - 3600000,
    recipient: "All"
  }
];

export const INITIAL_NOTIFICATIONS: Notification[] = [
  {
    id: 1,
    title: "Operational Hub Live",
    message: "Welcome to DM Flow Client Portal.",
    targetUsername: "All",
    read: false,
    timestamp: Date.now() - 86400000,
    priority: "Normal",
    type: "System Update",
    bannerPreset: "Sleek Charcoal",
    badgeIcon: "Shield",
    isScheduled: false,
    scheduledTime: 0,
    readCountSimulated: 5,
    showAsPushOverlay: false
  }
];
