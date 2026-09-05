import React from 'react';
import Navbar from '../components/layout/Navbar';
import HeroSection from '../components/landing/HeroSection';
import HowItWorksSection from '../components/landing/HowItWorksSection';
import StatisticsSection from '../components/landing/StatisticsSection';
import GamificationSection from '../components/landing/GamificationSection';
import TrustSection from '../components/landing/TrustSection';
import FinalCtaSection from '../components/landing/FinalCtaSection';
import Footer from '../components/landing/Footer';

export const LandingPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-[#FBFBF9] text-[#141A14] flex flex-col">
      <Navbar />
      <main className="flex-1">
        <HeroSection />
        <HowItWorksSection />
        <StatisticsSection />
        <GamificationSection />
        <TrustSection />
        <FinalCtaSection />
      </main>
      <Footer />
    </div>
  );
};

export default LandingPage;
