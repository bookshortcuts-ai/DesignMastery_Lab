import React, { useState, useEffect, useMemo } from 'react';
import Head from 'next/head';
import { 
  User, Project, Announcement, DiscussionMessage, ResourceShare, Notification, ProjectComment 
} from '../types';
import { 
  INITIAL_USERS, INITIAL_PROJECTS, INITIAL_ANNOUNCEMENTS, 
  INITIAL_RESOURCES, INITIAL_DISCUSSIONS, INITIAL_NOTIFICATIONS 
} from '../data/initialData';
import { ClickableAvatar } from '../components/ClickableAvatar';
import { BadgeDisplay } from '../components/BadgeDisplay';
import { PublicProfileOverlayDialog } from '../components/PublicProfileOverlayDialog';
import { BroadcastPanel } from '../components/BroadcastPanel';
import { 
  Briefcase, MessageSquare, FolderGit, LogOut, Bell, Compass, Trophy, Send, Plus, 
  Search, FileText, CheckCircle2, AlertCircle, ThumbsUp, ThumbsDown, Flame, Link2, 
  User as UserIcon, Lock, ChevronRight, Bookmark, BadgeAlert, CheckCircle, Info, RefreshCw
} from 'lucide-react';

export default function DMHubWeb() {
  // App-wide Mock DB state
  const [users, setUsers] = useState<User[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [announcements, setAnnouncements] = useState<Announcement[]>([]);
  const [discussionMessages, setDiscussionMessages] = useState<DiscussionMessage[]>([]);
  const [resources, setResources] = useState<ResourceShare[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [projectComments, setProjectComments] = useState<ProjectComment[]>([]);

  // Auth States
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [loginUsername, setLoginUsername] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [loginError, setLoginError] = useState('');

  // Navigation states: 'briefs' | 'leaderboard' | 'discussions' | 'resources' | 'notificationCenter'
  const [activeTab, setActiveTab] = useState<'briefs' | 'leaderboard' | 'discussions' | 'resources'>('briefs');

  // UI Intermediary states
  const [selectedProfileUser, setSelectedProfileUser] = useState<User | null>(null);
  const [showNotificationPanel, setShowNotificationPanel] = useState(false);
  const [activeNotificationSlide, setActiveNotificationSlide] = useState<Notification | null>(null);

  // Search / Filters
  const [projectFilterStatus, setProjectFilterStatus] = useState<string>('All');
  const [projectSearchQuery, setProjectSearchQuery] = useState('');
  const [resourceCategoryFilter, setResourceCategoryFilter] = useState<string>('All');

  // Creation Sheets / Modals
  const [showCreateBriefModal, setShowCreateBriefModal] = useState(false);
  const [newBriefTitle, setNewBriefTitle] = useState('');
  const [newBriefDesc, setNewBriefDesc] = useState('');
  const [newBriefAssignee, setNewBriefAssignee] = useState('');

  const [showSubmitWorkModal, setShowSubmitWorkModal] = useState<Project | null>(null);
  const [simulatedFileUrl, setSimulatedFileUrl] = useState('');
  const [submissionNotes, setSubmissionNotes] = useState('');

  const [showReviewModal, setShowReviewModal] = useState<Project | null>(null);
  const [reviewComment, setReviewComment] = useState('');

  // Discussion Board Inputs
  const [newChatMessage, setNewChatMessage] = useState('');
  const [chatRecipient, setChatRecipient] = useState('All');

  // Resource Share Inputs
  const [showAddResourceModal, setShowAddResourceModal] = useState(false);
  const [newResourceTitle, setNewResourceTitle] = useState('');
  const [newResourceCategory, setNewResourceCategory] = useState<'PSD Assets' | 'Vector Fonts' | 'Mockups' | 'Inspiration'>('PSD Assets');
  const [newResourceLink, setNewResourceLink] = useState('');

  // Load and hydrate database state
  useEffect(() => {
    if (typeof window !== 'undefined') {
      const getOrSet = (key: string, initial: any) => {
        const local = localStorage.getItem(`dm_flow_${key}`);
        if (local) {
          try {
            return JSON.parse(local);
          } catch (e) {
            return initial;
          }
        }
        localStorage.setItem(`dm_flow_${key}`, JSON.stringify(initial));
        return initial;
      };

      setUsers(getOrSet('users', INITIAL_USERS));
      setProjects(getOrSet('projects', INITIAL_PROJECTS));
      setAnnouncements(getOrSet('announcements', INITIAL_ANNOUNCEMENTS));
      setDiscussionMessages(getOrSet('discussions', INITIAL_DISCUSSIONS));
      setResources(getOrSet('resources', INITIAL_RESOURCES));
      setNotifications(getOrSet('notifications', INITIAL_NOTIFICATIONS));
      setProjectComments(getOrSet('project_comments', []));

      // Attempt to auto login with previous session if exists
      const savedUser = localStorage.getItem('dm_flow_logged_in_user');
      if (savedUser) {
        try {
          const uStr = JSON.parse(savedUser);
          const freshUsers = getOrSet('users', INITIAL_USERS) as User[];
          const match = freshUsers.find(u => u.username === uStr.username);
          if (match) {
            setCurrentUser(match);
          }
        } catch (e) {
          // ignore
        }
      }
    }
  }, []);

  // Save changes helper
  const saveState = (key: string, data: any) => {
    if (typeof window !== 'undefined') {
      localStorage.setItem(`dm_flow_${key}`, JSON.stringify(data));
    }
  };

  // Live Alert Trigger Engine
  const triggerLivePushOverlay = (title: string, message: string, bannerPreset = 'Sunset Neon') => {
    const newAlert: Notification = {
      id: Date.now(),
      title,
      message,
      targetUsername: 'All',
      read: false,
      timestamp: Date.now(),
      priority: 'High',
      type: 'System Broadcast',
      bannerPreset,
      badgeIcon: 'Star',
      isScheduled: false,
      scheduledTime: 0,
      readCountSimulated: 0,
      showAsPushOverlay: true
    };

    const updated = [newAlert, ...notifications];
    setNotifications(updated);
    saveState('notifications', updated);

    // Active Slide overlay toast
    setActiveNotificationSlide(newAlert);
    setTimeout(() => {
      setActiveNotificationSlide(null);
    }, 6000);
  };

  // Auth Handlers
  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    setLoginError('');
    const match = users.find(u => u.username.toLowerCase() === loginUsername.trim().toLowerCase());
    
    if (!match) {
      setLoginError('Staff credentials match failure.');
      return;
    }

    if (match.password !== loginPassword) {
      setLoginError('Incorrect password key.');
      return;
    }

    if (match.status === 'Suspended') {
      setLoginError('Your access is currently suspended by administration.');
      return;
    }

    setCurrentUser(match);
    localStorage.setItem('dm_flow_logged_in_user', JSON.stringify(match));
    triggerLivePushOverlay(`Welcome, ${match.fullName}`, `Connected securely to visual agency boards.`);
  };

  const handleLogout = () => {
    setCurrentUser(null);
    localStorage.removeItem('dm_flow_logged_in_user');
  };

  const quickSwitch = (u: User) => {
    setCurrentUser(u);
    localStorage.setItem('dm_flow_logged_in_user', JSON.stringify(u));
    triggerLivePushOverlay(`Switched to: ${u.fullName}`, `Operating role: ${u.role}`);
  };

  // Badge Logic
  const handleAwardBadge = (targetUsernameStr: string, badgeName: string) => {
    const updatedUsers = users.map(u => {
      if (u.username === targetUsernameStr) {
        const currentArr = u.earnedBadges ? u.earnedBadges.split(',').map(b => b.trim()).filter(Boolean) : [];
        if (!currentArr.includes(badgeName)) {
          const nextArr = [...currentArr, badgeName];
          const updatedUser = { 
            ...u, 
            earnedBadges: nextArr.join(','),
            performanceScore: Math.min(100, u.performanceScore + 4)
          };
          
          // Let's update the active profile viewer as well if viewed
          if (selectedProfileUser?.username === targetUsernameStr) {
            setSelectedProfileUser(updatedUser);
          }
          return updatedUser;
        }
      }
      return u;
    });

    setUsers(updatedUsers);
    saveState('users', updatedUsers);

    triggerLivePushOverlay(
      "Designer Credential Awarded", 
      `@${targetUsernameStr} earned the "${badgeName}" award!`
    );
  };

  const handleRemoveBadge = (targetUsernameStr: string, badgeName: string) => {
    const updatedUsers = users.map(u => {
      if (u.username === targetUsernameStr) {
        const currentArr = u.earnedBadges ? u.earnedBadges.split(',').map(b => b.trim()).filter(Boolean) : [];
        const nextArr = currentArr.filter(b => b !== badgeName);
        const updatedUser = { 
          ...u, 
          earnedBadges: nextArr.join(','),
          performanceScore: Math.max(0, u.performanceScore - 3)
        };
        
        if (selectedProfileUser?.username === targetUsernameStr) {
          setSelectedProfileUser(updatedUser);
        }
        return updatedUser;
      }
      return u;
    });

    setUsers(updatedUsers);
    saveState('users', updatedUsers);

    triggerLivePushOverlay(
      "Credential Revoked", 
      `"${badgeName}" badge has been removed from @${targetUsernameStr}.`
    );
  };

  // Project Brief Creators
  const handleCreateBrief = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newBriefTitle.trim() || !newBriefDesc.trim()) return;

    const newProject: Project = {
      id: Date.now(),
      title: newBriefTitle.trim(),
      description: newBriefDesc.trim(),
      createdBy: currentUser?.username || 'admin',
      assignedTo: newBriefAssignee,
      status: newBriefAssignee ? 'In Progress' : 'Pending',
      creationDate: Date.now(),
      completionDate: 0,
      fileUrls: '',
      submissionNote: '',
      feedback: ''
    };

    const nextProjects = [newProject, ...projects];
    setProjects(nextProjects);
    saveState('projects', nextProjects);

    // Notification
    const labelDetail = newBriefAssignee 
      ? `Task assigned directly to @${newBriefAssignee}` 
      : `New unassigned briefing is open to the designer pool.`;

    triggerLivePushOverlay("New brief published!", newBriefTitle);

    const alertNotif: Notification = {
      id: Date.now() + 1,
      title: "New Agency Project Briefing",
      message: `${newBriefTitle}. ${labelDetail}`,
      targetUsername: newBriefAssignee || "All",
      read: false,
      timestamp: Date.now(),
      priority: "Normal",
      type: "Announcement",
      bannerPreset: "Deep Sapphire",
      badgeIcon: "Crown",
      isScheduled: false,
      scheduledTime: 0,
      readCountSimulated: 0,
      showAsPushOverlay: false
    };

    const nextNotifs = [alertNotif, ...notifications];
    setNotifications(nextNotifs);
    saveState('notifications', nextNotifs);

    // Reset Form
    setNewBriefTitle('');
    setNewBriefDesc('');
    setNewBriefAssignee('');
    setShowCreateBriefModal(false);
  };

  // Job Claims
  const handleClaimProject = (project: Project) => {
    if (!currentUser) return;
    
    const updated = projects.map(p => {
      if (p.id === project.id) {
        return { 
          ...p, 
          assignedTo: currentUser.username, 
          status: 'In Progress' as const 
        };
      }
      return p;
    });

    setProjects(updated);
    saveState('projects', updated);

    triggerLivePushOverlay(
      "Brief Claim Successful", 
      `You claimed "${project.title}"! File delivery is expected shortly.`
    );
  };

  // Submit Proposal
  const handleSubmitBriefProposal = (e: React.FormEvent) => {
    e.preventDefault();
    if (!showSubmitWorkModal) return;

    const updated = projects.map(p => {
      if (p.id === showSubmitWorkModal.id) {
        return {
          ...p,
          status: 'Review' as const,
          fileUrls: simulatedFileUrl.trim(),
          submissionNote: submissionNotes.trim()
        };
      }
      return p;
    });

    setProjects(updated);
    saveState('projects', updated);

    triggerLivePushOverlay(
      "Proposal Submitted", 
      `"${showSubmitWorkModal.title}" delivered to quality control admins.`
    );

    // Alert Administrators
    const adminAlert: Notification = {
      id: Date.now(),
      title: "Submission Pending Assessment",
      message: `@${currentUser?.username} uploaded simulated designs for "${showSubmitWorkModal.title}"`,
      targetUsername: "Admin",
      read: false,
      timestamp: Date.now(),
      priority: "High",
      type: "Review Required",
      bannerPreset: "Classic Gold",
      badgeIcon: "Alert",
      isScheduled: false,
      scheduledTime: 0,
      readCountSimulated: 0,
      showAsPushOverlay: false
    };

    const nextNotifs = [adminAlert, ...notifications];
    setNotifications(nextNotifs);
    saveState('notifications', nextNotifs);

    setSimulatedFileUrl('');
    setSubmissionNotes('');
    setShowSubmitWorkModal(null);
  };

  // Admin Reviews
  const handleApproveBrief = (project: Project) => {
    const updated = projects.map(p => {
      if (p.id === project.id) {
        return {
          ...p,
          status: 'Completed' as const,
          completionDate: Date.now(),
          feedback: reviewComment.trim() || 'Visual layout conforms perfectly. Approved!'
        };
      }
      return p;
    });

    setProjects(updated);
    saveState('projects', updated);

    // Update User score
    const targetDesigner = project.assignedTo;
    const updatedUsers = users.map(u => {
      if (u.username === targetDesigner) {
        return {
          ...u,
          completedProjectsCount: u.completedProjectsCount + 1,
          performanceScore: Math.min(100, u.performanceScore + 3)
        };
      }
      return u;
    });
    setUsers(updatedUsers);
    saveState('users', updatedUsers);

    triggerLivePushOverlay(
      "Submission Approved", 
      `Project Briefing "${project.title}" was approved & finalized!`
    );

    setReviewComment('');
    setShowReviewModal(null);
  };

  const handleRejectBrief = (project: Project) => {
    const updated = projects.map(p => {
      if (p.id === project.id) {
        return {
          ...p,
          status: 'In Progress' as const,
          feedback: reviewComment.trim() || 'Adjust hierarchy and color calibration. Revisions required.'
        };
      }
      return p;
    });

    setProjects(updated);
    saveState('projects', updated);

    // Decrease designer score slightly as reflection of revision request
    const targetDesigner = project.assignedTo;
    const updatedUsers = users.map(u => {
      if (u.username === targetDesigner) {
        return {
          ...u,
          performanceScore: Math.max(50, u.performanceScore - 2)
        };
      }
      return u;
    });
    setUsers(updatedUsers);
    saveState('users', updatedUsers);

    triggerLivePushOverlay(
      "Revision Requested", 
      `Feedback notes published on "${project.title}".`
    );

    setReviewComment('');
    setShowReviewModal(null);
  };

  // Chat message send
  const handleChatSend = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newChatMessage.trim() || !currentUser) return;

    const newMessage: DiscussionMessage = {
      id: Date.now(),
      author: currentUser.username,
      authorRole: currentUser.role,
      messageText: newChatMessage.trim(),
      timestamp: Date.now(),
      recipient: chatRecipient
    };

    const nextMessages = [...discussionMessages, newMessage];
    setDiscussionMessages(nextMessages);
    saveState('discussions', nextMessages);

    setNewChatMessage('');
  };

  // Resources Addition
  const handleAddResource = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newResourceTitle.trim() || !newResourceLink.trim()) return;

    const res: ResourceShare = {
      id: Date.now(),
      title: newResourceTitle.trim(),
      category: newResourceCategory,
      link: newResourceLink.trim(),
      sharedBy: currentUser?.username || 'admin',
      timestamp: Date.now()
    };

    const nextResources = [res, ...resources];
    setResources(nextResources);
    saveState('resources', nextResources);

    triggerLivePushOverlay(
      "Asset Published", 
      `"${newResourceTitle}" shared in Download templates!`
    );

    setNewResourceTitle('');
    setNewResourceLink('');
    setShowAddResourceModal(false);
  };

  // Admin Broadcast Dispatches
  const handleAddBroadcast = (notif: Omit<Notification, 'id'>) => {
    const fullNotif: Notification = {
      ...notif,
      id: Date.now()
    };

    const updated = [fullNotif, ...notifications];
    setNotifications(updated);
    saveState('notifications', updated);

    if (fullNotif.showAsPushOverlay) {
      triggerLivePushOverlay(fullNotif.title, fullNotif.message, fullNotif.bannerPreset);
    }
  };

  // Interactive filtering variables
  const filteredProjects = useMemo(() => {
    return projects.filter(p => {
      const matchStatus = projectFilterStatus === 'All' || p.status === projectFilterStatus;
      const matchSearch = p.title.toLowerCase().includes(projectSearchQuery.toLowerCase()) || 
                          p.description.toLowerCase().includes(projectSearchQuery.toLowerCase()) ||
                          p.assignedTo.toLowerCase().includes(projectSearchQuery.toLowerCase());
      return matchStatus && matchSearch;
    });
  }, [projects, projectFilterStatus, projectSearchQuery]);

  const filteredResources = useMemo(() => {
    if (resourceCategoryFilter === 'All') return resources;
    return resources.filter(r => r.category === resourceCategoryFilter);
  }, [resources, resourceCategoryFilter]);

  // Leaders rankings
  const leaderboardUsers = useMemo(() => {
    return [...users]
      .filter(u => u.role === 'Team Member' || u.username === 'Jatin')
      .sort((a, b) => {
        if (b.completedProjectsCount !== a.completedProjectsCount) {
          return b.completedProjectsCount - a.completedProjectsCount;
        }
        return b.performanceScore - a.performanceScore;
      });
  }, [users]);

  // Unread Alerts counter
  const unreadAlertsCount = useMemo(() => {
    return notifications.filter(n => !n.read).length;
  }, [notifications]);

  // Display User Badge details
  const triggerProfileOverlay = (username: string) => {
    const match = users.find(u => u.username === username);
    if (match) {
      setSelectedProfileUser(match);
    }
  };

  // Convert Cover Banner Colors to CSS
  const getBannerGradient = (color: string) => {
    switch (color) {
      case 'NeonPink': return 'from-pink-500 via-rose-500 to-red-500';
      case 'CosmicBlue': return 'from-cyan-400 via-blue-500 to-indigo-600';
      case 'GoldGlow': return 'from-amber-400 via-yellow-500 to-orange-500';
      case 'Emerald': return 'from-emerald-400 via-teal-500 to-green-600';
      default: return 'from-slate-700 via-slate-800 to-slate-900';
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-slate-100 text-slate-800 select-none antialiased">
      <Head>
        <title>DM Flow — Premium Creative Agency Dashboard</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
      </Head>

      {/* TOP PUSH FLOATING BANNER NOTIFICATION (Android-like simulation) */}
      {activeNotificationSlide && (
        <div className="fixed top-4 right-4 z-[9999] max-w-sm w-full bg-slate-900 text-white p-4.5 rounded-2xl shadow-2xl border-l-[6px] border-emerald-500 animate-bounce cursor-pointer flex gap-3.5 items-center">
          <div className="w-9 h-9 shrink-0 rounded-xl bg-gradient-to-tr from-pink-500 to-violet-600 flex items-center justify-center font-black text-sm">
            ⭐
          </div>
          <div className="flex-1 min-w-0">
            <h4 className="font-extrabold text-[12px] truncate leading-tight tracking-wide">{activeNotificationSlide.title}</h4>
            <p className="text-[10px] text-slate-300 line-clamp-2 mt-0.5">{activeNotificationSlide.message}</p>
          </div>
          <button 
            onClick={() => setActiveNotificationSlide(null)}
            className="text-slate-400 hover:text-white font-bold p-1"
          >
            ✕
          </button>
        </div>
      )}

      {/* LOGIN OVERLAY PANEL */}
      {!currentUser ? (
        <div className="flex-1 flex flex-col justify-center items-center px-4 py-12 bg-gradient-to-tr from-slate-900 via-slate-800 to-zinc-900 text-white">
          <div className="max-w-md w-full bg-white/5 backdrop-blur-md rounded-3xl p-8 border border-white/10 shadow-2xl">
            <div className="text-center mb-8">
              <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-emerald-400 to-teal-600 mx-auto flex items-center justify-center shadow-lg transform rotate-6 mb-4">
                <Flame size={32} className="text-white transform -rotate-6" />
              </div>
              <h2 className="text-2xl font-black tracking-wide bg-clip-text text-transparent bg-gradient-to-r from-emerald-400 to-teal-200">
                DM_Flow Portal
              </h2>
              <p className="text-xs text-slate-300 font-semibold mt-1">
                DesignMastery_Lab Creative Operations System
              </p>
            </div>

            {loginError && (
              <div className="bg-rose-500/10 border-l-4 border-rose-500 p-3.5 rounded-xl text-xs font-bold text-rose-300 mb-6 flex items-center gap-2">
                <AlertCircle size={16} />
                <span>{loginError}</span>
              </div>
            )}

            <form onSubmit={handleLogin} className="flex flex-col gap-4">
              <div className="flex flex-col gap-1">
                <label className="text-[9px] font-black uppercase text-slate-400 tracking-widest px-1">
                  Enter Staff Username
                </label>
                <div className="relative">
                  <UserIcon size={14} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
                  <input
                    type="text"
                    required
                    value={loginUsername}
                    onChange={(e) => setLoginUsername(e.target.value)}
                    placeholder="john_vector"
                    className="w-full bg-white/5 border border-white/15 rounded-xl py-3.5 pl-10 pr-4 text-xs font-bold outline-none focus:border-emerald-400 focus:bg-white/10 transition-colors"
                  />
                </div>
              </div>

              <div className="flex flex-col gap-1">
                <label className="text-[9px] font-black uppercase text-slate-400 tracking-widest px-1">
                  Workspace Authentication Key
                </label>
                <div className="relative">
                  <Lock size={14} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
                  <input
                    type="password"
                    required
                    value={loginPassword}
                    onChange={(e) => setLoginPassword(e.target.value)}
                    placeholder="••••••••••••••"
                    className="w-full bg-white/5 border border-white/15 rounded-xl py-3.5 pl-10 pr-4 text-xs font-bold outline-none focus:border-emerald-400 focus:bg-white/10 transition-colors"
                  />
                </div>
              </div>

              <button
                type="submit"
                className="w-full py-4 mt-2 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 font-extrabold text-white text-xs uppercase tracking-wider shadow-lg hover:from-emerald-600 hover:to-teal-700 active:scale-98 transition-all"
              >
                Connect to Workspace Hub
              </button>
            </form>

            <div className="mt-8 border-t border-white/10 pt-6">
              <span className="text-[9px] font-black tracking-widest text-slate-400 uppercase block mb-3 text-center">
                Quick Access Staff Demo Profiles
              </span>
              <div className="grid grid-cols-2 gap-2 max-h-48 overflow-y-auto pr-1">
                {INITIAL_USERS.map((u, idx) => (
                  <button
                    key={idx}
                    type="button"
                    onClick={() => {
                      setLoginUsername(u.username);
                      setLoginPassword(u.password || '');
                    }}
                    className="p-2 sm:p-2.5 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 hover:border-emerald-500/50 flex items-center gap-2 text-left transition-all"
                  >
                    <div className="w-6 h-6 shrink-0 rounded-full bg-slate-500 flex items-center justify-center font-bold text-[9px]">
                      {u.fullName.charAt(0)}
                    </div>
                    <div className="truncate">
                      <p className="text-[10px] sm:text-[11px] font-black truncate text-white leading-tight">{u.fullName}</p>
                      <p className="text-[8px] font-bold text-slate-400 tracking-wider truncate uppercase">{u.role}</p>
                    </div>
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>
      ) : (
        /* CORE APPLICATION LAYOUT */
        <div className="flex-1 flex flex-col md:flex-row">
          
          {/* PERSISTENT HUB SIDEBAR CONTROLLERS */}
          <aside className="w-full md:w-64 bg-slate-900 text-white shrink-0 flex flex-col justify-between border-r border-slate-800 shadow-xl">
            <div>
              {/* Agency Logo */}
              <div className="p-6 border-b border-rose-500/20 bg-slate-950 flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-pink-500 to-violet-600 flex items-center justify-center font-black text-sm text-white shadow transform rotate-3">
                    DM
                  </div>
                  <div>
                    <h2 className="font-black text-sm tracking-wide bg-gradient-to-r from-pink-500 via-purple-400 to-emerald-400 bg-clip-text text-transparent">
                      DM FLOW
                    </h2>
                    <p className="text-[8px] font-bold text-slate-400 uppercase tracking-widest leading-none mt-0.5">
                      Production Agency
                    </p>
                  </div>
                </div>

                <div className="text-[10px] font-bold py-0.5 px-2 bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded-full uppercase tracking-wider select-none">
                  Active
                </div>
              </div>

              {/* User Identity widget */}
              <div 
                onClick={() => triggerProfileOverlay(currentUser.username)}
                className="p-4 mx-3 my-4 bg-slate-800/60 border border-slate-700/50 hover:border-emerald-500/40 rounded-2xl flex items-center gap-3 cursor-pointer transition-all hover:scale-102 active:scale-98 relative group"
              >
                <ClickableAvatar
                  avatarId={currentUser.avatarId}
                  fullName={currentUser.fullName}
                  size={42}
                  className="ring-2 ring-emerald-500/50 ring-offset-2 ring-offset-slate-900 group-hover:ring-emerald-400"
                />
                <div className="flex-1 min-w-0">
                  <h3 className="font-extrabold text-xs truncate group-hover:text-emerald-400 transition-colors">
                    {currentUser.fullName}
                  </h3>
                  <p className="text-[9px] font-black text-rose-400 uppercase tracking-wider truncate mt-0.5">
                    {currentUser.position}
                  </p>
                </div>
                <ChevronRight size={14} className="text-slate-500 group-hover:text-emerald-400 transition-colors" />
              </div>

              {/* Screen Nav List */}
              <nav className="px-3 flex flex-col gap-1.5 pt-1">
                <span className="text-[9px] font-black tracking-widest text-slate-500 uppercase px-3 pb-1 block">
                  Dashboard Channels
                </span>
                <button
                  onClick={() => setActiveTab('briefs')}
                  className={`w-full flex items-center justify-between px-3.5 py-3 rounded-xl text-xs font-black uppercase transition-all ${
                    activeTab === 'briefs'
                      ? 'bg-rose-600 text-white shadow-md shadow-rose-600/10'
                      : 'text-slate-400 hover:text-white hover:bg-slate-800/40'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <Briefcase size={15} />
                    <span>Project Briefings</span>
                  </div>
                  {projects.filter(p => p.status === 'Pending').length > 0 && (
                    <span className="bg-emerald-500 text-slate-900 text-[9px] font-black px-1.5 py-0.5 rounded-full">
                      {projects.filter(p => p.status === 'Pending').length} Available
                    </span>
                  )}
                </button>

                <button
                  onClick={() => setActiveTab('discussions')}
                  className={`w-full flex items-center justify-between px-3.5 py-3 rounded-xl text-xs font-black uppercase transition-all ${
                    activeTab === 'discussions'
                      ? 'bg-rose-600 text-white shadow-md shadow-rose-600/10'
                      : 'text-slate-400 hover:text-white hover:bg-slate-800/40'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <MessageSquare size={15} />
                    <span>Discussion Room</span>
                  </div>
                </button>

                <button
                  onClick={() => setActiveTab('resources')}
                  className={`w-full flex items-center justify-between px-3.5 py-3 rounded-xl text-xs font-black uppercase transition-all ${
                    activeTab === 'resources'
                      ? 'bg-rose-600 text-white shadow-md shadow-rose-600/10'
                      : 'text-slate-400 hover:text-white hover:bg-slate-800/40'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <FolderGit size={15} />
                    <span>Template Sharing</span>
                  </div>
                </button>

                <button
                  onClick={() => setActiveTab('leaderboard')}
                  className={`w-full flex items-center justify-between px-3.5 py-3 rounded-xl text-xs font-black uppercase transition-all ${
                    activeTab === 'leaderboard'
                      ? 'bg-rose-600 text-white shadow-md shadow-rose-600/10'
                      : 'text-slate-400 hover:text-white hover:bg-slate-800/40'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <Trophy size={15} />
                    <span>Designer Board</span>
                  </div>
                </button>
              </nav>
            </div>

            {/* Quick switcher panel visible to developer and managers in testing */}
            <div className="p-4 border-t border-slate-800 bg-slate-950">
              <span className="text-[9px] font-black tracking-widest text-slate-500 uppercase block mb-3 text-center">
                System Account Quick Switcher
              </span>
              <div className="grid grid-cols-3 gap-1 mb-4 select-none">
                {users.map((u, idx) => {
                  const isCur = u.username === currentUser.username;
                  return (
                    <button
                      key={idx}
                      title={u.fullName}
                      onClick={() => quickSwitch(u)}
                      className={`text-[9px] font-black py-1 px-1 rounded truncate border transition-all ${
                        isCur 
                          ? 'bg-rose-600 border-rose-600 text-white' 
                          : 'bg-slate-800/50 hover:bg-slate-800 border-slate-700 text-slate-300 hover:text-white'
                      }`}
                    >
                      {u.username}
                    </button>
                  );
                })}
              </div>

              <button
                onClick={handleLogout}
                className="w-full flex items-center justify-center gap-2 py-3.5 rounded-xl border border-rose-500/20 bg-rose-500/5 hover:bg-rose-500/10 text-rose-400 font-extrabold text-[10px] uppercase tracking-wider transition-colors"
              >
                <LogOut size={12} />
                <span>Sign Out Securely</span>
              </button>
            </div>
          </aside>

          {/* MAIN PLATFORM WORKSPACE */}
          <main className="flex-1 flex flex-col min-w-0 bg-slate-50">
            
            {/* WORKSPACE HEADER */}
            <header className="px-6 py-4 bg-white border-b border-slate-200 flex items-center justify-between sticky top-0 z-40 shadow-sm">
              <div className="flex items-center gap-3">
                <h1 className="font-extrabold text-slate-800 text-base flex items-center gap-2">
                  <span className="w-1.5 h-6 bg-rose-600 rounded-full"></span>
                  {activeTab === 'briefs' && 'Operational Agency Briefings'}
                  {activeTab === 'discussions' && 'Central Team Discussion Platform'}
                  {activeTab === 'resources' && 'Downloadable Brand Assets'}
                  {activeTab === 'leaderboard' && 'Designer Performance Leaderboard'}
                </h1>
              </div>

              {/* Alert Bell Center */}
              <div className="flex items-center gap-3 relative">
                <button
                  onClick={() => setShowNotificationPanel(!showNotificationPanel)}
                  className="w-10 h-10 rounded-xl hover:bg-slate-100 flex items-center justify-center relative border border-slate-200 transition-colors"
                >
                  <Bell size={18} className="text-slate-600" />
                  {unreadAlertsCount > 0 && (
                    <span className="absolute -top-1 -right-1 bg-rose-500 text-white text-[8px] font-black w-4.5 h-4.5 rounded-full flex items-center justify-center ring-2 ring-white">
                      {unreadAlertsCount}
                    </span>
                  )}
                </button>

                {/* NOTIFICATIONS SLIDE SHEET */}
                {showNotificationPanel && (
                  <div className="absolute right-0 top-12 w-80 bg-white shadow-2xl rounded-2xl border border-slate-200 overflow-hidden z-[9999]">
                    <div className="p-4 bg-slate-900 text-white flex justify-between items-center">
                      <h4 className="font-black text-xs uppercase tracking-wider">Alert Center ({notifications.length})</h4>
                      <button 
                        onClick={() => {
                          const updated = notifications.map(n => ({...n, read: true}));
                          setNotifications(updated);
                          saveState('notifications', updated);
                        }}
                        className="text-[10px] bg-white/10 hover:bg-white/20 text-white font-extrabold px-2 py-1 rounded"
                      >
                        Mark all read
                      </button>
                    </div>

                    <div className="max-h-96 overflow-y-auto divide-y divide-slate-150">
                      {notifications.length === 0 ? (
                        <div className="p-6 text-center text-slate-400 text-xs italic">
                          No alerts published yet.
                        </div>
                      ) : (
                        notifications.map((n, idx) => {
                          const coverMap: Record<string, string> = {
                            "Sunset Neon": "from-pink-500 to-rose-500",
                            "Classic Gold": "from-amber-500 to-yellow-500",
                            "Sleek Charcoal": "from-slate-700 to-slate-900",
                            "Deep Sapphire": "from-blue-600 to-indigo-800",
                            "Emerald Forest": "from-emerald-500 to-teal-700"
                          };
                          const gradient = coverMap[n.bannerPreset] || 'from-slate-600 to-slate-800';
                          return (
                            <div key={idx} className={`p-4 hover:bg-slate-50 transition-colors ${!n.read ? 'bg-emerald-50/10' : ''}`}>
                              <div className={`h-1.5 bg-gradient-to-r ${gradient} rounded-full mb-1 sm:mb-2`}></div>
                              <div className="flex justify-between items-start gap-2">
                                <h5 className="font-black text-xs text-slate-800 truncate leading-tight">{n.title}</h5>
                                <span className={`shrink-0 text-[8px] font-black px-1 rounded uppercase tracking-wider ${
                                  n.priority === 'Urgent' ? 'bg-red-100 text-red-700' :
                                  n.priority === 'High' ? 'bg-orange-100 text-orange-700' :
                                  'bg-slate-100 text-slate-700'
                                }`}>
                                  {n.priority}
                                </span>
                              </div>
                              <p className="text-[10px] text-slate-500 mt-1 leading-relaxed">{n.message}</p>
                              <div className="flex justify-between items-center mt-2 pt-1.5 border-t border-slate-100/50">
                                <span className="text-[8px] text-slate-400 font-bold">
                                  {new Date(n.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                </span>
                                {!n.read && (
                                  <button
                                    onClick={() => {
                                      const updated = notifications.map(no => no.id === n.id ? {...no, read: true} : no);
                                      setNotifications(updated);
                                      saveState('notifications', updated);
                                    }}
                                    className="text-[8px] text-emerald-600 font-extrabold hover:underline"
                                  >
                                    Acknowledge
                                  </button>
                                )}
                              </div>
                            </div>
                          );
                        })
                      )}
                    </div>
                  </div>
                )}
              </div>
            </header>

            {/* CORE CHANNELS FLOW CONTENT */}
            <div className="flex-1 p-6 overflow-y-auto space-y-6">
              
              {/* BRAND GLOBAL ANNOUNCEMENT BANNER */}
              {announcements.length > 0 && (
                <div className="bg-slate-900 rounded-3xl p-6 text-white relative overflow-hidden shadow-lg border border-slate-800">
                  <div className="absolute right-0 bottom-0 top-0 w-1/3 bg-gradient-to-l from-emerald-500/10 to-transparent pointer-events-none"></div>
                  <div className="flex flex-col sm:flex-row gap-4 items-start relative z-10">
                    <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-pink-500 to-violet-600 flex items-center justify-center font-black shadow-lg">
                      🔥
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="bg-rose-500 text-[8px] font-black uppercase tracking-wider px-2 py-0.5 rounded-full">
                          LATEST AGENCY COMMANDMENT
                        </span>
                        <span className="text-slate-400 text-[10px] font-semibold">
                          Posted by {announcements[0].author} • {announcements[0].priority} priority
                        </span>
                      </div>
                      <h2 className="text-lg font-black tracking-wide text-white mt-1.5 leading-tight">
                        {announcements[0].title}
                      </h2>
                      <p className="text-slate-300 text-xs leading-relaxed mt-1.5">
                        {announcements[0].content}
                      </p>
                    </div>
                  </div>
                </div>
              )}

              {/* ADMINISTRATIVE BROADCAST DESK TOOL */}
              {(currentUser.role === 'Super Admin' || currentUser.role === 'Admin') && (
                <BroadcastPanel
                  currentUser={currentUser}
                  users={users}
                  onAddBroadcast={handleAddBroadcast}
                />
              )}

              {/* TAB 1: OPERATIONAL AGENCY BRIEFINGS (THE CORE OF DM_FLOW) */}
              {activeTab === 'briefs' && (
                <div className="space-y-4">
                  
                  {/* SEARCH, STATUS TAB FILTERS */}
                  <div className="bg-white p-4.5 rounded-2xl shadow-sm border border-slate-200 flex flex-col md:flex-row justify-between items-center gap-4">
                    <div className="flex gap-1 overflow-x-auto w-full md:w-auto scrollbar-none py-1">
                      {['All', 'Pending', 'In Progress', 'Review', 'Completed'].map((st, idx) => {
                        const isCurState = projectFilterStatus === st;
                        return (
                          <button
                            key={idx}
                            onClick={() => setProjectFilterStatus(st)}
                            className={`px-3.5 py-1.5 text-[10px] font-black uppercase tracking-wider rounded-xl transition-all ${
                              isCurState
                                ? 'bg-slate-900 border-slate-900 text-white'
                                : 'bg-slate-50 hover:bg-slate-100 text-slate-600 border border-slate-200'
                            }`}
                          >
                            {st} ({st === 'All' ? projects.length : projects.filter(p => p.status === st).length})
                          </button>
                        );
                      })}
                    </div>

                    <div className="flex items-center gap-3 w-full md:w-auto">
                      <div className="relative w-full md:w-64 bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 flex items-center gap-2">
                        <Search size={14} className="text-slate-400 shrink-0" />
                        <input
                          type="text"
                          value={projectSearchQuery}
                          onChange={(e) => setProjectSearchQuery(e.target.value)}
                          placeholder="Search briefing parameters..."
                          className="w-full text-xs font-bold outline-none bg-transparent text-slate-700"
                        />
                      </div>

                      {(currentUser.role === 'Super Admin' || currentUser.role === 'Admin') && (
                        <button
                          onClick={() => setShowCreateBriefModal(true)}
                          className="flex items-center gap-1.5 py-2.5 px-4 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-600 hover:to-teal-700 text-white font-extrabold text-xs uppercase tracking-wide shadow transition-colors shrink-0"
                        >
                          <Plus size={14} /> Publish Brief
                        </button>
                      )}
                    </div>
                  </div>

                  {/* ACTIVE BRIEFS FEED LIST */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {filteredProjects.length === 0 ? (
                      <div className="col-span-1 md:col-span-2 bg-white py-12 rounded-3xl border border-slate-100 shadow-sm text-center">
                        <Bookmark size={40} className="text-slate-200 mx-auto mb-3" />
                        <p className="font-extrabold text-xs text-slate-500">
                          No creative brief parameters match.
                        </p>
                        <p className="text-[10px] text-slate-400 mt-1">
                          Admins can publish new design deliverables from the button above.
                        </p>
                      </div>
                    ) : (
                      filteredProjects.map((p, idx) => {
                        const isAssignedToSelf = p.assignedTo === currentUser.username;
                        const isUnassigned = !p.assignedTo;
                        const cardColors: Record<string, string> = {
                          'Pending': 'border-blue-500/25 bg-blue-50/50',
                          'In Progress': 'border-purple-500/25 bg-purple-50/50',
                          'Review': 'border-amber-500/25 bg-amber-50/50',
                          'Completed': 'border-emerald-500/25 bg-emerald-50/50'
                        };

                        return (
                          <div 
                            key={idx} 
                            className={`p-5 rounded-2xl hover:scale-101 border transition-all flex flex-col justify-between ${
                              cardColors[p.status] || 'border-slate-200 bg-white'
                            } shadow-sm`}
                          >
                            <div>
                              {/* Status Badges header */}
                              <div className="flex justify-between items-start gap-2 mb-3">
                                <span className={`px-2.5 py-1 rounded-full text-[9px] font-black uppercase tracking-wider ${
                                  p.status === 'Completed' ? 'bg-emerald-100 text-emerald-800' :
                                  p.status === 'Review' ? 'bg-amber-100 text-amber-800' :
                                  p.status === 'In Progress' ? 'bg-purple-100 text-purple-800' :
                                  'bg-blue-100 text-blue-800'
                                }`}>
                                  {p.status}
                                </span>
                                
                                {p.assignedTo ? (
                                  <div 
                                    onClick={() => triggerProfileOverlay(p.assignedTo)} 
                                    className="flex items-center gap-1.5 bg-white border border-slate-200 rounded-full px-2.5 py-0.5 cursor-pointer shadow-sm text-[10px] font-bold text-slate-600 hover:text-emerald-600 active:scale-95 transition-transform"
                                  >
                                    <div className="w-4 h-4 rounded-full bg-slate-500 flex items-center justify-center text-white text-[8px] font-black">
                                      {p.assignedTo.charAt(0).toUpperCase()}
                                    </div>
                                    <span>@{p.assignedTo}</span>
                                  </div>
                                ) : (
                                  <span className="text-[9px] font-black text-rose-500 uppercase tracking-widest bg-rose-50 px-2 py-0.5 rounded">
                                    AVAILABLE CLAIM
                                  </span>
                                )}
                              </div>

                              <h3 className="font-extrabold text-slate-800 text-sm leading-snug line-clamp-1 mb-1.5">
                                {p.title}
                              </h3>
                              <p className="text-xs text-slate-500 leading-relaxed font-medium line-clamp-3 mb-4">
                                {p.description}
                              </p>
                            </div>

                            {/* Submissions feedback or action items bar */}
                            <div className="border-t border-slate-150 pt-3 flex flex-col gap-2">
                              {/* Action details if review exists */}
                              {p.feedback && (
                                <div className="p-2.5 rounded-lg bg-white border border-slate-150 text-[10px] mb-2 leading-relaxed">
                                  <span className="font-black text-slate-700 block mb-0.5 uppercase tracking-wide">
                                    Quality Control Feedback:
                                  </span>
                                  <p className="text-slate-500 italic font-semibold">{p.feedback}</p>
                                </div>
                              )}

                              {p.fileUrls && (
                                <div className="flex gap-2 items-center text-[10px] font-bold text-slate-500 mb-1 leading-none">
                                  <FileText size={11} className="text-slate-400" />
                                  <span className="max-w-[150px] truncate">{p.fileUrls}</span>
                                  <span className="text-[8px] px-1 bg-slate-100 border border-slate-200 text-slate-400 rounded">Simulation</span>
                                </div>
                              )}

                              {/* Operations trigger logic based on user role */}
                              <div className="flex justify-end gap-2 mt-1">
                                
                                {/* Claim option */}
                                {isUnassigned && currentUser.role === 'Team Member' && (
                                  <button
                                    onClick={() => handleClaimProject(p)}
                                    className="px-3.5 py-2 hover:scale-102 rounded-xl text-[10px] font-black bg-rose-600 hover:bg-rose-700 active:scale-95 text-white uppercase tracking-wider transition-all"
                                  >
                                    Claim Briefing
                                  </button>
                                )}

                                {/* Delivery upload files option */}
                                {isAssignedToSelf && p.status === 'In Progress' && (
                                  <button
                                    onClick={() => setShowSubmitWorkModal(p)}
                                    className="px-3.5 py-2 hover:scale-102 rounded-xl text-[10px] font-black bg-emerald-600 hover:bg-emerald-700 active:scale-95 text-white uppercase tracking-wider transition-all"
                                  >
                                    Deliver Proposals PSD/AI
                                  </button>
                                )}

                                {/* Admin Audit/Review Option */}
                                {p.status === 'Review' && (currentUser.role === 'Super Admin' || currentUser.role === 'Admin') && (
                                  <button
                                    onClick={() => setShowReviewModal(p)}
                                    className="px-3.5 py-2 hover:scale-102 rounded-xl text-[10px] font-black bg-amber-500 hover:bg-amber-600 active:scale-95 text-white uppercase tracking-wider transition-all"
                                  >
                                    Assess Proposal
                                  </button>
                                )}

                                {/* Completed visual placeholder */}
                                {p.status === 'Completed' && (
                                  <div className="flex items-center gap-1.5 text-[10px] font-black text-emerald-600 uppercase tracking-widest leading-none py-1">
                                    <CheckCircle size={12} /> Passed Quality Audits
                                  </div>
                                )}
                              </div>
                            </div>
                          </div>
                        );
                      })
                    )}
                  </div>
                </div>
              )}

              {/* TAB 2: CENTRAL TEAM DISCUSSION PLATFORM (CHAT BOARD) */}
              {activeTab === 'discussions' && (
                <div className="bg-white rounded-3xl border border-slate-200 shadow-sm flex flex-col h-[520px] overflow-hidden">
                  
                  {/* Chat board info bar */}
                  <div className="p-4 bg-slate-900 text-white flex justify-between items-center shrink-0">
                    <div className="flex items-center gap-2">
                      <MessageSquare size={16} />
                      <h3 className="font-extrabold text-sm uppercase tracking-wide">Agency Command HQ Discussions</h3>
                    </div>
                    <span className="text-[10px] font-bold bg-white/10 px-2 py-0.5 rounded text-white tracking-widest uppercase">
                      Live Communications Feed
                    </span>
                  </div>

                  {/* Message displays body */}
                  <div className="flex-1 p-5 overflow-y-auto space-y-4 bg-slate-50/50">
                    {discussionMessages.length === 0 ? (
                      <div className="flex flex-col justify-center items-center h-full text-slate-400 text-xs italic">
                        <span>No visual agency ideas posted yet.</span>
                      </div>
                    ) : (
                      discussionMessages.map((m, idx) => {
                        const isSelf = m.author === currentUser.username;
                        return (
                          <div key={idx} className={`flex gap-3 max-w-[85%] ${isSelf ? 'ml-auto flex-row-reverse' : ''}`}>
                            <div 
                              onClick={() => triggerProfileOverlay(m.author)}
                              className="shrink-0 cursor-pointer self-end"
                            >
                              <div className="w-8 h-8 rounded-full bg-slate-800 text-white font-black text-[10px] flex items-center justify-center transform hover:scale-105 active:scale-95 transition-transform">
                                {m.author.charAt(0).toUpperCase()}
                              </div>
                            </div>

                            <div className="flex flex-col gap-1">
                              <span className={`text-[9px] font-black tracking-wide text-slate-400 uppercase ${isSelf ? 'text-right' : ''}`}>
                                @{m.author} ({m.authorRole}) • {new Date(m.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                              </span>
                              <div className={`p-3 rounded-2xl text-xs leading-relaxed font-medium shadow-sm border ${
                                isSelf
                                  ? 'bg-rose-600 text-white border-rose-500'
                                  : 'bg-white text-slate-700 border-slate-150'
                              }`}>
                                {m.messageText}
                              </div>
                            </div>
                          </div>
                        );
                      })
                    )}
                  </div>

                  {/* Chat composer tray */}
                  <form onSubmit={handleChatSend} className="p-4.5 bg-white border-t border-slate-200 shrink-0 flex items-center gap-3">
                    <select
                      value={chatRecipient}
                      onChange={(e) => setChatRecipient(e.target.value)}
                      className="text-[10px] font-black uppercase text-slate-600 border border-slate-200 p-2.5 rounded-xl bg-slate-50 outline-none"
                    >
                      <option value="All">Global Board</option>
                      {users.map((u, idx) => (
                        <option key={idx} value={u.username}>Private @{u.username}</option>
                      ))}
                    </select>

                    <input
                      type="text"
                      required
                      value={newChatMessage}
                      onChange={(e) => setNewChatMessage(e.target.value)}
                      placeholder="Type design ideas, coordination remarks..."
                      className="flex-1 text-xs font-bold bg-slate-50 border border-slate-200 p-3.5 rounded-xl outline-none focus:border-rose-600 focus:bg-white transition-colors"
                    />

                    <button
                      type="submit"
                      className="p-3.5 rounded-xl bg-rose-602 bg-rose-600 hover:bg-rose-700 active:scale-95 text-white flex items-center justify-center shadow-md transition-all"
                    >
                      <Send size={15} />
                    </button>
                  </form>

                </div>
              )}

              {/* TAB 3: DOWNLOADABLE BRAND ASSETS (TEMPLATES) */}
              {activeTab === 'resources' && (
                <div className="space-y-4">
                  
                  {/* Category filters bar */}
                  <div className="bg-white p-4 rounded-2xl shadow-sm border border-slate-200 flex flex-col sm:flex-row justify-between items-center gap-4">
                    <div className="flex gap-1 overflow-x-auto w-full sm:w-auto scrollbar-none py-1">
                      {['All', 'PSD Assets', 'Vector Fonts', 'Mockups', 'Inspiration'].map((cat, idx) => {
                        return (
                          <button
                            key={idx}
                            onClick={() => setResourceCategoryFilter(cat)}
                            className={`px-3.5 py-1.5 text-[10px] font-black uppercase tracking-wider rounded-xl transition-all ${
                              resourceCategoryFilter === cat
                                ? 'bg-slate-900 text-white'
                                : 'bg-slate-50 hover:bg-slate-100 text-slate-500 border border-slate-200'
                            }`}
                          >
                            {cat}
                          </button>
                        );
                      })}
                    </div>

                    <button
                      onClick={() => setShowAddResourceModal(true)}
                      className="w-full sm:w-auto flex items-center justify-center gap-1.5 py-2.5 px-4 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-600 hover:to-teal-700 text-white font-extrabold text-xs uppercase tracking-wide shadow transition-colors"
                    >
                      <Plus size={14} /> Upload Asset Link
                    </button>
                  </div>

                  {/* Resource shares grid */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {filteredResources.map((r, idx) => (
                      <div 
                        key={idx} 
                        className="p-5 rounded-2xl bg-white border border-slate-200 shadow-sm flex items-center justify-between gap-4 hover:scale-101 hover:border-emerald-500/30 transition-all"
                      >
                        <div className="flex items-center gap-3.5 min-w-0">
                          <div className="w-11 h-11 rounded-xl bg-emerald-500/10 text-emerald-600 border border-emerald-500/15 flex items-center justify-center shrink-0">
                            <Link2 size={18} />
                          </div>
                          <div className="min-w-0">
                            <span className="text-[8px] font-black px-2 py-0.5 bg-slate-100 border border-slate-200 text-slate-500 rounded uppercase tracking-wider">
                              {r.category}
                            </span>
                            <h4 className="font-extrabold text-slate-800 text-xs sm:text-sm mt-1.5 truncate leading-snug">
                              {r.title}
                            </h4>
                            <p className="text-[10px] text-slate-400 font-bold mt-0.5 truncate">
                              Shared by @{r.sharedBy}
                            </p>
                          </div>
                        </div>

                        <a
                          href={r.link}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="px-3.5 py-2 rounded-xl border border-emerald-500 text-emerald-600 hover:bg-emerald-500 hover:text-white font-black text-[10px] uppercase tracking-wider transition-colors shrink-0"
                        >
                          Download (ZIP)
                        </a>
                      </div>
                    ))}
                  </div>

                </div>
              )}

              {/* TAB 4: LEADERBOARD ARENA */}
              {activeTab === 'leaderboard' && (
                <div className="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden">
                  
                  {/* Leaderboard info bar */}
                  <div className="p-6 bg-slate-900 text-white flex justify-between items-center flex-wrap gap-4 border-b border-rose-500/20">
                    <div>
                      <h3 className="font-extrabold text-base uppercase tracking-wide">Agency Designer Leaderboards</h3>
                      <p className="text-[10px] text-slate-400 mt-1">
                        Rankings aggregated based on finalized client visual briefings & diagnostic parameters.
                      </p>
                    </div>
                    <span className="text-xs bg-emerald-500 text-slate-900 font-black px-3.5 py-1.5 rounded-full select-none">
                      Active Cycle: June 2026
                    </span>
                  </div>

                  {/* Leaderboards checklist */}
                  <div className="divide-y divide-slate-150">
                    {leaderboardUsers.map((member, idx) => {
                      const rank = idx + 1;
                      
                      const podiumColors = 
                        rank === 1 ? 'bg-yellow-50 border-yellow-200' :
                        rank === 2 ? 'bg-slate-50 border-slate-250' :
                        rank === 3 ? 'bg-orange-50 border-orange-200' :
                        'bg-white border-slate-100';

                      const medalIcon = 
                        rank === 1 ? '🥇' :
                        rank === 2 ? '🥈' :
                        rank === 3 ? '🥉' :
                        String(rank).padStart(2, '0');

                      return (
                        <div 
                          key={idx} 
                          onClick={() => triggerProfileOverlay(member.username)}
                          className={`p-4 sm:p-5 flex items-center justify-between gap-4 cursor-pointer hover:bg-slate-50/85 transition-colors border-l-4 ${
                            rank === 1 ? 'border-l-yellow-400' :
                            rank === 2 ? 'border-l-slate-400' :
                            rank === 3 ? 'border-l-orange-400' :
                            'border-l-slate-200'
                          } ${podiumColors}`}
                        >
                          <div className="flex items-center gap-3 sm:gap-4.5 min-w-0">
                            {/* Medals ranking column */}
                            <div className="w-8 shrink-0 flex justify-center text-sm font-black text-slate-800">
                              {medalIcon}
                            </div>

                            <ClickableAvatar
                              avatarId={member.avatarId}
                              fullName={member.fullName}
                              size={44}
                              className="ring-2 ring-white"
                            />

                            <div className="min-w-0">
                              <h4 className="font-extrabold text-xs sm:text-sm text-slate-800 leading-tight truncate">
                                {member.fullName}
                              </h4>
                              <p className="text-[10px] font-black text-emerald-600 uppercase tracking-wide truncate mt-0.5">
                                {member.position}
                              </p>
                            </div>
                          </div>

                          {/* Completed and score info details columns */}
                          <div className="flex items-center gap-6 text-right shrink-0">
                            <div>
                              <p className="font-extrabold text-xs sm:text-sm text-slate-800 leading-none">
                                {String(member.completedProjectsCount || 0).padStart(2, '0')}
                              </p>
                              <p className="text-[8px] font-bold text-slate-400 tracking-wider uppercase mt-1">Briefs Done</p>
                            </div>

                            <div>
                              <p className="font-extrabold text-xs sm:text-sm text-emerald-600 leading-none">
                                {member.performanceScore}%
                              </p>
                              <p className="text-[8px] font-bold text-slate-400 tracking-wider uppercase mt-1">Accuracy</p>
                            </div>
                          </div>

                        </div>
                      );
                    })}
                  </div>

                </div>
              )}

            </div>
          </main>

        </div>
      )}

      {/* CREATE NEW BRIEF MODAL */}
      {showCreateBriefModal && (
        <div className="fixed inset-0 bg-black/55 z-50 flex items-center justify-center p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full overflow-hidden border border-slate-150">
            <div className="p-4 bg-slate-900 text-white flex justify-between items-center">
              <h3 className="font-black text-xs uppercase tracking-widest flex items-center gap-2">
                <Bookmark size={14} /> New Creative brief Designer Spec Sheet
              </h3>
              <button 
                onClick={() => setShowCreateBriefModal(false)}
                className="text-slate-400 hover:text-white font-bold"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleCreateBrief} className="p-5 flex flex-col gap-4">
              <div className="flex flex-col gap-1">
                <label className="text-[10px] font-black tracking-wider text-slate-400 bg-white px-1 uppercase">
                  Deliverable Title Label
                </label>
                <input
                  type="text"
                  required
                  value={newBriefTitle}
                  onChange={(e) => setNewBriefTitle(e.target.value)}
                  placeholder="Fintech App Vector Redesign..."
                  className="p-3 bg-slate-50 border border-slate-200 rounded-xl outline-none text-xs font-bold focus:bg-white focus:border-rose-600 transition-colors"
                />
              </div>

              <div className="flex flex-col gap-1">
                <label className="text-[10px] font-black tracking-wider text-slate-400 bg-white px-1 uppercase">
                  Deliverable Guidelines Description
                </label>
                <textarea
                  required
                  rows={4}
                  value={newBriefDesc}
                  onChange={(e) => setNewBriefDesc(e.target.value)}
                  placeholder="Create dynamic vector outlines. Avoid standard stock illustrations. Source PSD/SVG delivery required..."
                  className="p-3 bg-slate-50 border border-slate-200 rounded-xl outline-none text-xs font-semibold focus:bg-white focus:border-rose-600 transition-colors"
                />
              </div>

              <div className="flex flex-col gap-1">
                <label className="text-[10px] font-black tracking-wider text-slate-400 bg-white px-1 uppercase">
                  Assignee Staff (Optional)
                </label>
                <select
                  value={newBriefAssignee}
                  onChange={(e) => setNewBriefAssignee(e.target.value)}
                  className="p-3 bg-slate-50 border border-slate-200 rounded-xl outline-none text-xs font-bold focus:bg-white focus:border-rose-600 transition-colors"
                >
                  <option value="">Unassigned (Available to Claim)</option>
                  {users.filter(u => u.role === 'Team Member').map((u, idx) => (
                    <option key={idx} value={u.username}>{u.fullName}</option>
                  ))}
                </select>
              </div>

              <button
                type="submit"
                className="w-full py-4 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 text-white text-xs font-black uppercase tracking-wider hover:from-emerald-600 hover:to-teal-700 active:scale-98 transition-transform shadow-md"
              >
                Publish Campaign Proposal Brief
              </button>
            </form>
          </div>
        </div>
      )}

      {/* SUBMIT WORK PROPOSALS MODAL */}
      {showSubmitWorkModal && (
        <div className="fixed inset-0 bg-black/55 z-50 flex items-center justify-center p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full overflow-hidden border border-slate-150">
            <div className="p-4 bg-slate-900 text-white flex justify-between items-center">
              <h3 className="font-black text-xs uppercase tracking-widest">
                Deliver Simulated Proposal
              </h3>
              <button 
                onClick={() => setShowSubmitWorkModal(null)}
                className="text-slate-400 hover:text-white font-bold"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleSubmitBriefProposal} className="p-5 flex flex-col gap-4">
              <p className="text-slate-500 text-xs font-medium leading-relaxed bg-slate-50 border border-slate-150 p-3 rounded-lg">
                Delivering work proposal for: <strong className="text-slate-700">{showSubmitWorkModal.title}</strong>
              </p>

              <div className="flex flex-col gap-1">
                <label className="text-[10px] font-black tracking-wider text-slate-400 bg-white px-1 uppercase">
                  Simulated Delivery Resource File URLs (PSDs / SVGs)
                </label>
                <input
                  type="text"
                  required
                  value={simulatedFileUrl}
                  onChange={(e) => setSimulatedFileUrl(e.target.value)}
                  placeholder="fintech_conceptual_revised.psd, showcase_draft.svg"
                  className="p-3 bg-slate-50 border border-slate-200 rounded-xl outline-none text-xs font-bold focus:bg-white focus:border-rose-600 transition-colors"
                />
              </div>

              <div className="flex flex-col gap-1">
                <label className="text-[10px] font-black tracking-wider text-slate-400 bg-white px-1 uppercase">
                  Submission Cover notes
                </label>
                <textarea
                  required
                  rows={3}
                  value={submissionNotes}
                  onChange={(e) => setSubmissionNotes(e.target.value)}
                  placeholder="Completed vector concepts in source folders. Highly responsive Material 3 outline themes used..."
                  className="p-3 bg-slate-50 border border-slate-200 rounded-xl outline-none text-xs font-semibold focus:bg-white focus:border-rose-600 transition-colors"
                />
              </div>

              <button
                type="submit"
                className="w-full py-4 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 text-white text-xs font-black uppercase tracking-wider hover:from-emerald-600 hover:to-teal-700 active:scale-98 transition-transform shadow-md"
              >
                Deliver Portfolio Proposal Assets
              </button>
            </form>
          </div>
        </div>
      )}

      {/* ASSESS SUBMISSION REVIEWS PROPOSAL MODAL */}
      {showReviewModal && (
        <div className="fixed inset-0 bg-black/55 z-50 flex items-center justify-center p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full overflow-hidden border border-slate-150">
            <div className="p-4 bg-slate-900 text-white flex justify-between items-center">
              <h3 className="font-black text-xs uppercase tracking-widest">
                Assess Proposal Draft Deliveries
              </h3>
              <button 
                onClick={() => setShowReviewModal(null)}
                className="text-slate-400 hover:text-white font-bold"
              >
                ✕
              </button>
            </div>

            <div className="p-5 flex flex-col gap-4">
              <div className="text-slate-500 text-xs font-medium leading-relaxed bg-slate-50 border border-slate-150 p-4 rounded-xl">
                <p className="font-extrabold text-slate-700">Audit Deliverables Checklist:</p>
                <p className="mt-1.5 font-bold">Files: <strong className="text-slate-800">{showReviewModal.fileUrls}</strong></p>
                <p className="mt-1 font-semibold text-slate-500 italic">&ldquo;{showReviewModal.submissionNote}&rdquo;</p>
              </div>

              <div className="flex flex-col gap-1">
                <label className="text-[10px] font-black tracking-wider text-slate-400 bg-white px-1 uppercase">
                  Quality Control Directives / Revision Guidelines
                </label>
                <textarea
                  rows={3}
                  value={reviewComment}
                  onChange={(e) => setReviewComment(e.target.value)}
                  placeholder="Excellent colors. Typography kerning can be rounded slightly for high symmetry. Approve design!"
                  className="p-3 bg-slate-50 border border-slate-200 rounded-xl outline-none text-xs font-semibold focus:bg-white focus:border-rose-600 transition-colors"
                />
              </div>

              <div className="grid grid-cols-2 gap-3 mt-1">
                <button
                  onClick={() => handleRejectBrief(showReviewModal)}
                  className="flex items-center justify-center gap-1.5 py-4 rounded-xl border border-red-500 text-red-600 font-extrabold text-xs uppercase tracking-wider hover:bg-red-50 active:scale-98 transition-all"
                >
                  <ThumbsDown size={14} /> Request Revision
                </button>
                <button
                  onClick={() => handleApproveBrief(showReviewModal)}
                  className="flex items-center justify-center gap-1.5 py-4 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-600 hover:to-teal-700 text-white font-black text-xs uppercase tracking-wider active:scale-98 transition-all shadow-md"
                >
                  <ThumbsUp size={14} /> Pass & Finalize
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ADD RESOURCE MANUAL MODAL */}
      {showAddResourceModal && (
        <div className="fixed inset-0 bg-black/55 z-50 flex items-center justify-center p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl max-w-sm w-full overflow-hidden border border-slate-150">
            <div className="p-4 bg-slate-900 text-white flex justify-between items-center">
              <h3 className="font-black text-xs uppercase tracking-widest">
                Upload Custom Template Resource
              </h3>
              <button 
                onClick={() => setShowAddResourceModal(false)}
                className="text-slate-400 hover:text-white font-bold"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleAddResource} className="p-5 flex flex-col gap-4">
              <div className="flex flex-col gap-1">
                <label className="text-[10px] font-black tracking-wider text-slate-400 bg-white px-1 uppercase">
                  Asset Title
                </label>
                <input
                  type="text"
                  required
                  value={newResourceTitle}
                  onChange={(e) => setNewResourceTitle(e.target.value)}
                  placeholder="Luxury Modern Box Packaging vector file..."
                  className="p-3 bg-slate-50 border border-slate-200 rounded-xl outline-none text-xs font-bold focus:bg-white focus:border-rose-600 transition-colors"
                />
              </div>

              <div className="flex flex-col gap-1">
                <label className="text-[10px] font-black tracking-wider text-slate-400 bg-white px-1 uppercase">
                  Resource Category
                </label>
                <select
                  value={newResourceCategory}
                  onChange={(e) => setNewResourceCategory(e.target.value as any)}
                  className="p-3 bg-slate-50 border border-slate-200 rounded-xl outline-none text-xs font-bold focus:bg-white focus:border-rose-600 transition-colors"
                >
                  <option value="PSD Assets">PSD Assets</option>
                  <option value="Vector Fonts">Vector Fonts</option>
                  <option value="Mockups">Mockups</option>
                  <option value="Inspiration">Inspiration</option>
                </select>
              </div>

              <div className="flex flex-col gap-1">
                <label className="text-[10px] font-black tracking-wider text-slate-400 bg-white px-1 uppercase">
                  External Share Link
                </label>
                <input
                  type="url"
                  required
                  value={newResourceLink}
                  onChange={(e) => setNewResourceLink(e.target.value)}
                  placeholder="https://dml-storage.s3.amazonaws.com/vip-fonts.zip..."
                  className="p-3 bg-slate-50 border border-slate-200 rounded-xl outline-none text-xs font-semibold focus:bg-white focus:border-rose-600 transition-colors"
                />
              </div>

              <button
                type="submit"
                className="w-full py-4 mt-2 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 text-white text-xs font-black uppercase tracking-wider hover:from-emerald-600 hover:to-teal-700 active:scale-98 transition-transform shadow-md"
              >
                Publish Link Segment
              </button>
            </form>
          </div>
        </div>
      )}

      {/* PUBLIC PROFILE MODALS PLATFORMS */}
      {selectedProfileUser && (
        <PublicProfileOverlayDialog
          user={selectedProfileUser}
          currentUser={currentUser}
          onClose={() => setSelectedProfileUser(null)}
          onAwardBadge={handleAwardBadge}
          onRemoveBadge={handleRemoveBadge}
        />
      )}
    </div>
  );
}
