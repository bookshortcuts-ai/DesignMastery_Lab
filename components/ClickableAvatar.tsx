import React from 'react';

interface ClickableAvatarProps {
  avatarId: number;
  fullName: string;
  size?: number;
  onClick?: () => void;
  className?: string;
}

export const ClickableAvatar: React.FC<ClickableAvatarProps> = ({
  avatarId,
  fullName,
  size = 54,
  onClick,
  className = ''
}) => {
  const colors = [
    'from-pink-500 to-rose-600',
    'from-purple-500 to-indigo-600',
    'from-indigo-500 to-blue-600',
    'from-teal-500 to-emerald-600',
    'from-orange-500 to-amber-600',
    'from-fuchsia-500 to-purple-600'
  ];
  
  const bgGradient = colors[avatarId % colors.size || avatarId % colors.length || 0];
  
  const initials = React.useMemo(() => {
    const trimmed = fullName.trim();
    if (!trimmed) return 'DM';
    const parts = trimmed.split(/\s+/);
    if (parts.length > 1) {
      return (parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }
    return trimmed.substring(0, 2).toUpperCase();
  }, [fullName]);

  return (
    <div
      onClick={onClick}
      style={{ width: size, height: size }}
      className={`rounded-full flex items-center justify-center text-white font-bold select-none shadow-md bg-gradient-to-tr ${bgGradient} ${
        onClick ? 'cursor-pointer hover:scale-105 active:scale-95 transition-transform' : ''
      } ${className}`}
    >
      <span style={{ fontSize: size * 0.38 }}>{initials}</span>
    </div>
  );
};
