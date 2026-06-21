import React from 'react';
import { Award, Shield, Image, Brush, Radio, Palette, Zap, Users, Star, Calendar, TrendingUp, Lightbulb } from 'lucide-react';

interface BadgeDisplayProps {
  badgeName: string;
  isLarge?: boolean;
  className?: string;
}

export const BadgeDisplay: React.FC<BadgeDisplayProps> = ({
  badgeName,
  isLarge = false,
  className = ''
}) => {
  const trimmedName = badgeName.trim();
  if (!trimmedName) return null;

  let rarity = 'Common';
  let colors = 'from-slate-400 to-slate-600';
  let icon = <Award size={isLarge ? 28 : 12} className="text-white" />;
  let label = trimmedName;
  let desc = 'Awarded contribution and design service credential.';
  let bgRarityColor = 'bg-slate-100 text-slate-700';

  switch (trimmedName) {
    case 'Founder Crown Badge':
      rarity = 'Legendary';
      colors = 'from-yellow-400 via-amber-300 to-yellow-600';
      icon = <Award size={isLarge ? 28 : 12} className="text-white" />;
      label = "Founder & Super Admin";
      desc = "Premium exclusive crown badge of ultimate creative agency authority.";
      bgRarityColor = 'bg-yellow-100 text-yellow-800 border border-yellow-300';
      break;
    case 'Elite Admin Badge':
      rarity = 'Epic';
      colors = 'from-blue-600 to-slate-400';
      icon = <Shield size={isLarge ? 28 : 12} className="text-white" />;
      label = "Administrator";
      desc = "Elite administrator shield badge for operational systems.";
      bgRarityColor = 'bg-blue-100 text-blue-800 border border-blue-200';
      break;
    case 'Thumbnail Master':
      rarity = 'Legendary';
      colors = 'from-red-600 to-orange-500';
      icon = <Image size={isLarge ? 28 : 12} className="text-white" />;
      label = "Thumbnail Master";
      desc = "High engagement CTR packaging and strategic YouTube banner layouts.";
      bgRarityColor = 'bg-red-100 text-red-800 border border-red-200';
      break;
    case 'Logo Specialist':
      rarity = 'Epic';
      colors = 'from-purple-600 to-indigo-700';
      icon = <Brush size={isLarge ? 28 : 12} className="text-white" />;
      label = "Logo Specialist";
      desc = "Mastery of visual identity typography and vector geometric symbols.";
      bgRarityColor = 'bg-purple-100 text-purple-800 border border-purple-200';
      break;
    case 'Social Media Expert':
      rarity = 'Epic';
      colors = 'from-cyan-400 to-blue-600';
      icon = <Radio size={isLarge ? 28 : 12} className="text-white" />;
      label = "Social Media Expert";
      desc = "Bespoke social framework templates tailored for target digital campaigns.";
      bgRarityColor = 'bg-cyan-100 text-cyan-800 border border-cyan-200';
      break;
    case 'Creative Expert':
      rarity = 'Legendary';
      colors = 'from-emerald-500 to-green-600';
      icon = <Palette size={isLarge ? 28 : 12} className="text-white" />;
      label = "Creative Expert";
      desc = "High-fidelity brand manuals, art assets, and guidelines curation.";
      bgRarityColor = 'bg-emerald-100 text-emerald-800 border border-emerald-200';
      break;
    case 'Fast Delivery':
      rarity = 'Common';
      colors = 'from-slate-500 to-slate-700';
      icon = <Zap size={isLarge ? 28 : 12} className="text-white" />;
      label = "Fast Delivery";
      desc = "Phenomenal turnaround speeds respecting aggressive product schedules.";
      bgRarityColor = 'bg-slate-200 text-slate-800';
      break;
    case 'Team Leader':
      rarity = 'Rare';
      colors = 'from-blue-700 to-indigo-800';
      icon = <Users size={isLarge ? 28 : 12} className="text-white" />;
      label = "Team Leader";
      desc = "Exceptional project mentorship guiding junior designers on accuracy formats.";
      bgRarityColor = 'bg-indigo-100 text-indigo-800 border border-indigo-200';
      break;
    case 'Top Designer':
      rarity = 'Legendary';
      colors = 'from-red-500 via-orange-400 to-yellow-500';
      icon = <Star size={isLarge ? 28 : 12} className="text-white" />;
      label = "Top Designer";
      desc = "Flawless designer execution matching all creative specifications perfectly.";
      bgRarityColor = 'bg-amber-100 text-amber-800 border border-amber-300';
      break;
    case 'Employee of the Month':
      rarity = 'Epic';
      colors = 'from-fuchsia-500 to-purple-800';
      icon = <Calendar size={isLarge ? 28 : 12} className="text-white" />;
      label = "Employee of the Month";
      desc = "High-tier coworker recognition for exceeding peer contributions.";
      bgRarityColor = 'bg-fuchsia-100 text-fuchsia-800 border border-fuchsia-200';
      break;
    case 'Rising Talent':
      rarity = 'Common';
      colors = 'from-blue-400 to-slate-500';
      icon = <TrendingUp size={isLarge ? 28 : 12} className="text-white" />;
      label = "Rising Talent";
      desc = "Fast tracking development with pristine potential inside team boards.";
      bgRarityColor = 'bg-blue-50 text-blue-700';
      break;
    case 'Problem Solver':
      rarity = 'Common';
      colors = 'from-gray-600 to-gray-800';
      icon = <Lightbulb size={isLarge ? 28 : 12} className="text-white" />;
      label = "Problem Solver";
      desc = "High speed revision workflows and diagnostic feedback mastery.";
      bgRarityColor = 'bg-gray-100 text-gray-800';
      break;
  }

  if (isLarge) {
    return (
      <div className={`p-5 rounded-2xl bg-white border border-slate-200 shadow-md flex flex-col items-center text-center ${className}`}>
        <div className={`w-14 h-14 rounded-full flex items-center justify-center bg-gradient-to-tr ${colors} shadow-lg mb-3`}>
          {icon}
        </div>
        <h4 className="font-extrabold text-slate-800 text-base">{label}</h4>
        <div className={`my-1.5 px-3 py-0.5 rounded-full text-[10px] font-black tracking-wider uppercase ${bgRarityColor}`}>
          {rarity} GRADE
        </div>
        <p className="text-xs text-slate-500 leading-relaxed mt-1">{desc}</p>
      </div>
    );
  }

  return (
    <div className={`px-3 py-1.5 rounded-lg bg-gradient-to-r ${colors} shadow-sm inline-flex items-center gap-2 ${className}`}>
      {icon}
      <span className="text-[10px] font-extrabold text-white tracking-wide uppercase select-none">{label}</span>
    </div>
  );
};
