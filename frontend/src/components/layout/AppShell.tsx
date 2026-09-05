import React from 'react';
import { Sidebar } from './Sidebar';
import { TopHeader } from './TopHeader';
import { MobileNav } from './MobileNav';

interface AppShellProps {
  title: string;
  subtitle?: string;
  titleClassName?: string;
  children: React.ReactNode;
}

export const AppShell: React.FC<AppShellProps> = ({ title, subtitle, titleClassName, children }) => {
  return (
    <div className="min-h-screen bg-[#FBFBF9] flex flex-col md:flex-row">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0 pb-20 md:pb-6">
        <TopHeader title={title} subtitle={subtitle} titleClassName={titleClassName} />
        <main className="flex-1 p-6 md:p-8 max-w-7xl w-full mx-auto animate-fade-in">
          {children}
        </main>
      </div>
      <MobileNav />
    </div>
  );
};

export default AppShell;
