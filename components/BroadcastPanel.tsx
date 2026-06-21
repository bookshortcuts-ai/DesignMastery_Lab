import React, { useState } from 'react';
import { User, Notification } from '../types';
import { ChevronDown, ChevronUp, Send, CheckCircle2, Megaphone, HelpCircle } from 'lucide-react';

interface BroadcastPanelProps {
  currentUser: User | null;
  users: User[];
  onAddBroadcast: (notification: Omit<Notification, 'id'>) => void;
}

export const BroadcastPanel: React.FC<BroadcastPanelProps> = ({
  currentUser,
  users,
  onAddBroadcast
}) => {
  const [isExpanded, setIsExpanded] = useState(false);
  const [title, setTitle] = useState('');
  const [msg, setMsg] = useState('');
  const [category, setCategory] = useState('Announcement');
  const [priority, setPriority] = useState<'Low' | 'Normal' | 'High' | 'Urgent'>('Normal');
  const [bannerPreset, setBannerPreset] = useState('Sunset Neon');
  const [iconSelected, setIconSelected] = useState('Comment');
  const [targetUsername, setTargetUsername] = useState('All');
  const [notifySuccess, setNotifySuccess] = useState(false);

  const categories = [
    "Announcement", "Project Assigned", "Project Deadline", 
    "Project Approved", "Revision Required", "Badge Awarded", 
    "Team Update", "Important Alert", "System Update", "Custom"
  ];

  const priorities = ["Low", "Normal", "High", "Urgent"] as const;

  const bannerPresets = [
    { name: "Sunset Neon", style: "from-pink-500 to-rose-500" },
    { name: "Classic Gold", style: "from-amber-500 to-yellow-500" },
    { name: "Sleek Charcoal", style: "from-slate-700 to-slate-900" },
    { name: "Deep Sapphire", style: "from-blue-600 to-indigo-800" },
    { name: "Emerald Forest", style: "from-emerald-500 to-teal-700" }
  ];

  const iconsList = ["Comment", "Shield", "Crown", "Star", "Alert", "Check"];

  if (currentUser?.role !== 'Super Admin' && currentUser?.role !== 'Admin') {
    return null;
  }

  const handleDispatch = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !msg.trim()) return;

    onAddBroadcast({
      title: title.trim(),
      message: msg.trim(),
      targetUsername,
      read: false,
      timestamp: Date.now(),
      priority,
      type: category,
      bannerPreset,
      badgeIcon: iconSelected,
      isScheduled: false,
      scheduledTime: 0,
      readCountSimulated: 0,
      showAsPushOverlay: true
    });

    // Reset Form
    setTitle('');
    setMsg('');
    setNotifySuccess(true);
    setTimeout(() => setNotifySuccess(false), 3000);
  };

  return (
    <div className="bg-white border-2 border-emerald-500/30 rounded-2xl shadow-md overflow-hidden transition-all duration-300">
      {/* Header section */}
      <div 
        onClick={() => setIsExpanded(!isExpanded)}
        className="flex items-center justify-between p-4 bg-emerald-50/50 cursor-pointer select-none"
      >
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-emerald-500 text-white flex items-center justify-center shadow-md">
            <Megaphone size={18} />
          </div>
          <div>
            <h3 className="font-extrabold text-slate-800 text-sm leading-tight">
              Executive Broadcast Desk
            </h3>
            <p className="text-[10px] text-slate-500">
              Design, style, and broadcast custom hub notifications.
            </p>
          </div>
        </div>
        <button className="text-emerald-600 hover:text-emerald-700 focus:outline-none">
          {isExpanded ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
        </button>
      </div>

      {/* Main expanded panel wrapper */}
      {isExpanded && (
        <form onSubmit={handleDispatch} className="p-5 border-t border-slate-100 flex flex-col gap-4">
          
          {/* Inputs grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="flex flex-col gap-1">
              <label className="text-[10px] font-black tracking-wider text-slate-400 uppercase">
                Broadcast Label Title
              </label>
              <input
                type="text"
                required
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Brand brief updated..."
                className="w-full text-xs font-bold border border-slate-200 bg-slate-50 rounded-xl p-3 focus:border-emerald-500 focus:bg-white outline-none transition-colors"
              />
            </div>

            <div className="flex flex-col gap-1">
              <label className="text-[10px] font-black tracking-wider text-slate-400 uppercase">
                Audience Filter Segment
              </label>
              <select
                value={targetUsername}
                onChange={(e) => setTargetUsername(e.target.value)}
                className="w-full text-xs font-bold border border-slate-200 bg-slate-50 rounded-xl p-3 focus:border-emerald-500 focus:bg-white outline-none transition-colors"
              >
                <option value="All">All Users (Broadcast)</option>
                <option value="Admin">All Admins</option>
                <option value="Team Member">All Team Members</option>
                {users.map((u, idx) => (
                  <option key={idx} value={u.username}>{u.fullName} (@{u.username})</option>
                ))}
              </select>
            </div>
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-[10px] font-black tracking-wider text-slate-400 uppercase">
              Visual Brief Details Message
            </label>
            <textarea
              required
              rows={2}
              value={msg}
              onChange={(e) => setMsg(e.target.value)}
              placeholder="Deliver final vector revisions within 4 hours..."
              className="w-full text-xs font-semibold border border-slate-200 bg-slate-50 rounded-xl p-3 focus:border-emerald-500 focus:bg-white outline-none transition-colors"
            />
          </div>

          {/* Configuration selections */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Category selection */}
            <div className="flex flex-col gap-1">
              <label className="text-[10px] font-black tracking-wider text-slate-400 uppercase">
                Broadcast Category
              </label>
              <div className="flex flex-wrap gap-1 max-h-24 overflow-y-auto p-1 bg-slate-50 border border-slate-200 rounded-xl">
                {categories.map((c, idx) => (
                  <button
                    key={idx}
                    type="button"
                    onClick={() => setCategory(c)}
                    className={`px-2 py-1 text-[9px] font-bold rounded-md border transition-all ${
                      category === c 
                        ? 'bg-emerald-600 text-white border-emerald-600'
                        : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'
                    }`}
                  >
                    {c}
                  </button>
                ))}
              </div>
            </div>

            {/* Priority and Icon selection */}
            <div className="flex flex-col gap-3">
              <div className="flex flex-col gap-1">
                <span className="text-[10px] font-black tracking-wider text-slate-400 uppercase">
                  Urgency Priority Levels
                </span>
                <div className="flex gap-1.5">
                  {priorities.map((p, idx) => (
                    <button
                      key={idx}
                      type="button"
                      onClick={() => setPriority(p)}
                      className={`text-[9px] font-black px-3 py-1.5 rounded-lg border transition-all uppercase tracking-wide flex-1 text-center ${
                        priority === p
                          ? p === 'Urgent' ? 'bg-red-500 text-white border-red-500' 
                            : p === 'High' ? 'bg-orange-500 text-white border-orange-500'
                            : 'bg-emerald-600 text-white border-emerald-600'
                          : 'bg-slate-50 text-slate-600 border-slate-200 hover:bg-slate-100'
                      }`}
                    >
                      {p}
                    </button>
                  ))}
                </div>
              </div>

              {/* Icon preset choosing */}
              <div className="grid grid-cols-2 gap-2">
                <div className="flex flex-col gap-1">
                  <span className="text-[9px] font-black text-slate-400 uppercase tracking-wider">
                    Badge Vector Icon
                  </span>
                  <select
                    value={iconSelected}
                    onChange={(e) => setIconSelected(e.target.value)}
                    className="text-[11px] font-bold border border-slate-200 p-1.5 rounded-lg focus:outline-emerald-500 outline-none"
                  >
                    {iconsList.map((ico, idx) => (
                      <option key={idx} value={ico}>{ico}</option>
                    ))}
                  </select>
                </div>

                <div className="flex flex-col gap-1">
                  <span className="text-[9px] font-black text-slate-400 uppercase tracking-wider">
                    Simulated Banner Type
                  </span>
                  <select
                    value={bannerPreset}
                    onChange={(e) => setBannerPreset(e.target.value)}
                    className="text-[11px] font-bold border border-slate-200 p-1.5 rounded-lg focus:outline-emerald-500 outline-none"
                  >
                    {bannerPresets.map((bp, idx) => (
                      <option key={idx} value={bp.name}>{bp.name}</option>
                    ))}
                  </select>
                </div>
              </div>
            </div>
          </div>

          {/* Banner cover presets display previews style design info */}
          <div className="flex flex-col gap-1.5">
            <span className="text-[10px] font-black tracking-wider text-slate-400 uppercase">
              Simulated Cover Presets
            </span>
            <div className="flex gap-2 overflow-x-auto pb-1 scrollbar-thin">
              {bannerPresets.map((bp, idx) => {
                const isSelected = bannerPreset === bp.name;
                return (
                  <div
                    key={idx}
                    onClick={() => setBannerPreset(bp.name)}
                    className={`px-3 py-2 rounded-xl text-center cursor-pointer select-none text-[9px] font-bold text-white shadow bg-gradient-to-r ${bp.style} ${
                      isSelected ? 'ring-2 ring-emerald-500 scale-102 ring-offset-2' : 'opacity-70 hover:opacity-100'
                    } min-w-[90px] uppercase tracking-wider`}
                  >
                    {bp.name}
                  </div>
                );
              })}
            </div>
          </div>

          {/* Submit broadcast bar actions */}
          <div className="flex items-center justify-between border-t border-slate-100 pt-4 mt-1">
            {notifySuccess ? (
              <div className="flex items-center gap-1.5 text-xs text-emerald-600 font-extrabold">
                <CheckCircle2 size={16} /> Broadcast Dispatched successfully.
              </div>
            ) : (
              <div className="text-[10px] text-slate-400 font-medium">
                Pushes visual alert system banners real-time inside clients.
              </div>
            )}
            <button
              type="submit"
              className="flex items-center gap-1.5 text-xs font-black bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2.5 rounded-xl shadow-md transition-all active:scale-95"
            >
              <Send size={13} /> Dispatch Campaign Pulse
            </button>
          </div>

        </form>
      )}
    </div>
  );
};
