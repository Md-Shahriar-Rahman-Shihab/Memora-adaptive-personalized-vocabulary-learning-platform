import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Compass, RotateCw, HelpCircle, User } from 'lucide-react';

export const MobileNav: React.FC = () => {
  const items = [
    { label: 'Dashboard', path: '/dashboard', icon: <LayoutDashboard className="w-5 h-5" /> },
    { label: 'Learn', path: '/learn-path', icon: <Compass className="w-5 h-5" /> },
    { label: 'Review', path: '/review', icon: <RotateCw className="w-5 h-5" /> },
    { label: 'Quiz', path: '/quiz', icon: <HelpCircle className="w-5 h-5" /> },
    { label: 'Profile', path: '/profile', icon: <User className="w-5 h-5" /> },
  ];

  return (
    <nav className="md:hidden fixed bottom-0 left-0 right-0 z-40 bg-white/95 backdrop-blur-md border-t border-black/[0.08] px-4 py-2 flex items-center justify-around">
      {items.map((item) => (
        <NavLink
          key={item.path}
          to={item.path}
          className={({ isActive }) =>
            `flex flex-col items-center gap-1 p-2 rounded-xl text-[10px] font-semibold transition ${
              isActive ? 'text-memora-green font-bold' : 'text-memora-text-muted hover:text-memora-dark'
            }`
          }
        >
          {item.icon}
          <span>{item.label}</span>
        </NavLink>
      ))}
    </nav>
  );
};

export default MobileNav;
