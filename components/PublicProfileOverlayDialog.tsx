import React, { useState } from 'react';
import { User } from '../types';
import { ClickableAvatar } from './ClickableAvatar';
import { BadgeDisplay } from './BadgeDisplay';
import { X, Mail, Globe, Phone, Award, Shield, Plus, Check } from 'lucide-react';

interface PublicProfileOverlayDialogProps {
  user: User;
  currentUser: User | null;
  onClose: () => void;
  onAwardBadge: (username: string, badgeName: string) => void;
  onRemoveBadge: (username: string, badgeName: string) => void;
}

export const PublicProfileOverlayDialog: React.FC<PublicProfileOverlayDialogProps> = ({
  user,
  currentUser,
  onClose,
  onAwardBadge,
  onRemoveBadge
}) => {
  const [assignBadgeExpanded, setAssignBadgeExpanded] = useState(false);

  const coverBannerColor = user.coverBannerColor || 'Slate';
  const coverBrushMap: Record<string, string> = {
    NeonPink: 'from-pink-500 to-rose-500',
    CosmicBlue: 'from-cyan-400 to-blue-600',
    GoldGlow: 'from-yellow-500 via-orange-500 to-red-500',
    Emerald: 'from-emerald-400 to-teal-600',
    Slate: 'from-slate-600 to-slate-800'
  };

  const coverGradient = coverBrushMap[coverBannerColor] || coverBrushMap.Slate;

  const availableBadges = [
    "Thumbnail Master", "Logo Specialist", "Social Media Expert", 
    "Creative Expert", "Fast Delivery", "Team Leader", 
    "Top Designer", "Employee of the Month", "Rising Talent", "Problem Solver",
    "Founder Crown Badge", "Elite Admin Badge"
  ];

  const earnedList = user.earnedBadges
    ? user.earnedBadges.split(',').map(b => b.trim()).filter(Boolean)
    : [];

  const unearnedList = availableBadges.filter(b => !earnedList.includes(b));

  const isManagement = currentUser?.role === 'Super Admin' || currentUser?.role === 'Admin';
  const isNotSelf = currentUser?.username !== user.username;

  return (
    <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4 backdrop-blur-sm transition-all overflow-y-auto">
      <div className="bg-white rounded-2xl shadow-2xl max-w-lg w-full overflow-hidden border border-slate-150 relative my-8">
        
        {/* Cover Banner */}
        <div className={`h-28 bg-gradient-to-r ${coverGradient} relative flex items-end justify-between p-4`}>
          <button
            onClick={onClose}
            className="absolute top-3 right-3 p-1.5 rounded-full bg-black/25 text-white hover:bg-black/40 transition-colors"
          >
            <X size={16} />
          </button>
          {user.role === 'Super Admin' && (
            <Award size={64} className="text-white/10 absolute bottom-1 right-3 select-none" />
          )}
        </div>

        {/* Profile Details Container */}
        <div className="px-6 pb-6 pt-2">
          {/* Avatar and Info Header */}
          <div className="flex items-end gap-4 -mt-10 mb-4">
            <ClickableAvatar
              avatarId={user.avatarId}
              fullName={user.fullName}
              size={80}
              className="border-[4px] border-white shadow-lg"
            />
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-1.5 mb-0.5">
                {user.role === 'Super Admin' && (
                  <Award size={18} className="text-yellow-500 fill-yellow-500 shrink-0" />
                )}
                {user.role === 'Admin' && (
                  <Shield size={16} className="text-blue-500 fill-blue-500 shrink-0" />
                )}
                <h3 className="font-black text-xl text-slate-800 truncate leading-tight">
                  {user.fullName}
                </h3>
              </div>
              <p className="text-xs font-bold text-emerald-600 truncate uppercase tracking-wide">
                {user.position} • {user.role}
              </p>
            </div>
          </div>

          {/* Mission Statement for Founders */}
          {user.role === 'Super Admin' && user.missionStatement && (
            <div className="bg-emerald-50/50 border border-emerald-100 rounded-xl p-3 mb-4">
              <span className="text-[9px] font-black text-emerald-700 tracking-wider block mb-1">
                FOUNDER VISION STATEMENT:
              </span>
              <p className="text-xs italic font-semibold text-slate-700 leading-normal">
                &ldquo;{user.missionStatement}&rdquo;
              </p>
            </div>
          )}

          {/* Stats Deck */}
          <div className="grid grid-cols-3 bg-slate-50 border border-slate-100 rounded-xl p-3.5 mb-4 text-center divide-x divide-slate-200">
            <div>
              <p className="font-black text-lg text-slate-800">
                {String(user.completedProjectsCount || 0).padStart(2, '0')}
              </p>
              <p className="text-[8px] font-bold text-slate-400 tracking-wider uppercase mt-0.5">
                Completed Briefs
              </p>
            </div>
            <div>
              <p className="font-black text-lg text-emerald-600">
                {user.performanceScore}%
              </p>
              <p className="text-[8px] font-bold text-slate-400 tracking-wider uppercase mt-0.5">
                Accuracy Level
              </p>
            </div>
            <div>
              <p className="font-black text-lg text-rose-500">
                {user.joinedDate || 'Jun 2026'}
              </p>
              <p className="text-[8px] font-bold text-slate-400 tracking-wider uppercase mt-0.5">
                Team Join Date
              </p>
            </div>
          </div>

          {/* Biography */}
          <div className="mb-4">
            <h4 className="text-[10px] font-black text-slate-400 tracking-wider uppercase mb-1">
              Creative Biography
            </h4>
            <p className="text-slate-600 text-xs leading-relaxed">
              {user.bio || 'This hub designer specialist has not written a custom description profile yet.'}
            </p>
          </div>

          {/* Skills deck */}
          {user.skills && (
            <div className="mb-4">
              <h4 className="text-[10px] font-black text-slate-400 tracking-wider uppercase mb-1.5">
                Modular Skills Deck
              </h4>
              <div className="flex flex-wrap gap-1.5">
                {user.skills.split(',').map((s, idx) => (
                  <span
                    key={idx}
                    className="px-2.5 py-1 rounded-full text-[10px] font-extrabold bg-emerald-50 text-emerald-700 border border-emerald-100"
                  >
                    {s.trim()}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Badges credentials */}
          <div className="mb-4 border-t border-slate-100 pt-3">
            <h4 className="text-[10px] font-black text-slate-400 tracking-wider uppercase mb-2">
              Earned Brand Credentials ({earnedList.length})
            </h4>
            {earnedList.length === 0 ? (
              <p className="text-slate-400 text-xs italic">
                No design badges earned yet. Complete claimed operations with pristine graphics to earn specialized credentials!
              </p>
            ) : (
              <div className="flex flex-wrap gap-2">
                {earnedList.map((badge, idx) => (
                  <BadgeDisplay key={idx} badgeName={badge} />
                ))}
              </div>
            )}
          </div>

          {/* Featured projects (for founders) */}
          {user.role === 'Super Admin' && user.featuredProjects && (
            <div className="mb-4 border-t border-slate-100 pt-3">
              <h4 className="text-[10px] font-black text-slate-400 tracking-wider uppercase mb-2">
                Featured Platform Segments
              </h4>
              <div className="flex gap-2 overflow-x-auto pb-1 scrollbar-thin">
                {user.featuredProjects.split(',').map((fp, idx) => (
                  <div key={idx} className="bg-slate-50 border border-slate-200 rounded-xl p-2.5 min-w-[120px] shrink-0">
                    <p className="font-extrabold text-[10px] text-slate-800 line-clamp-1">{fp.trim()}</p>
                    <p className="text-[8px] font-semibold text-slate-400">Sleek Repository</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Social / Contact links */}
          <div className="mb-4 border-t border-slate-100 pt-3">
            <h4 className="text-[10px] font-black text-slate-400 tracking-wider uppercase mb-2">
              Contact & Auditing Channels
            </h4>
            <div className="flex gap-2">
              {user.whatsapp && (
                <a
                  href={`https://api.whatsapp.com/send?phone=${user.whatsapp.replace(/\D/g, '')}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="w-9 h-9 rounded-full bg-green-500 hover:bg-green-600 text-white flex items-center justify-center shadow transition-colors"
                >
                  <Phone size={15} />
                </a>
              )}
              {user.contactInfo && (
                <a
                  href={`mailto:${user.contactInfo}`}
                  className="w-9 h-9 rounded-full bg-emerald-600 hover:bg-emerald-700 text-white flex items-center justify-center shadow transition-colors"
                >
                  <Mail size={15} />
                </a>
              )}
              {user.website && (
                <a
                  href={user.website}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="w-9 h-9 rounded-full bg-slate-800 hover:bg-slate-900 text-white flex items-center justify-center shadow transition-colors"
                >
                  <Globe size={15} />
                </a>
              )}
            </div>
          </div>

          {/* Badge gifting console box */}
          {isManagement && isNotSelf && (
            <div className="border-t border-slate-200 pt-4 mt-4 bg-slate-50/50 p-4 rounded-xl border">
              <div className="flex items-center justify-between mb-2">
                <div>
                  <h4 className="text-xs font-black text-slate-800 tracking-wide uppercase">
                    Executive Brand Badge Gift Office
                  </h4>
                  <p className="text-[10px] text-slate-400 font-medium">
                    Award or revoke professional visual credentials instantly.
                  </p>
                </div>
                <button
                  onClick={() => setAssignBadgeExpanded(!assignBadgeExpanded)}
                  className="flex items-center gap-1 text-[11px] font-bold bg-slate-800 text-white px-2.5 py-1 rounded-md hover:bg-slate-900 transition-colors"
                >
                  <Plus size={11} /> Gift Badge
                </button>
              </div>

              {/* Award Dropdown/Selector */}
              {assignBadgeExpanded && (
                <div className="mb-3 max-h-36 overflow-y-auto bg-white border border-slate-200 rounded-lg p-2 grid grid-cols-2 gap-1.5 shadow-inner">
                  {unearnedList.length === 0 ? (
                    <span className="text-[11px] text-slate-400 p-2 col-span-2">All badges are already earned.</span>
                  ) : (
                    unearnedList.map((badge, idx) => (
                      <button
                        key={idx}
                        onClick={() => {
                          onAwardBadge(user.username, badge);
                          setAssignBadgeExpanded(false);
                        }}
                        className="text-left text-[10px] font-bold text-slate-700 py-1.5 px-2.5 rounded hover:bg-emerald-50 hover:text-emerald-700 transition-colors truncate"
                      >
                        {badge}
                      </button>
                    ))
                  )}
                </div>
              )}

              {/* Revoke visual credentials */}
              {earnedList.length > 0 && (
                <div className="mt-3">
                  <p className="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1.5">
                    Tap to revoke visual badge:
                  </p>
                  <div className="flex flex-wrap gap-1.5">
                    {earnedList.map((badge, idx) => (
                      <div
                        key={idx}
                        onClick={() => onRemoveBadge(user.username, badge)}
                        className="group flex items-center gap-1 px-2 py-1 bg-white border border-red-200 rounded-md cursor-pointer hover:bg-red-50 hover:border-red-300 transition-colors text-[9px] font-bold text-red-700"
                        title="Click to revoke badge"
                      >
                        <span>{badge}</span>
                        <X size={10} className="text-red-400 group-hover:text-red-700" />
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

        </div>
      </div>
    </div>
  );
};
