import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Menu, X, ArrowRight } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { Button } from '../ui/Button';

interface NavItem {
  id: string;
  label: string;
  href?: string;
  to?: string;
  sectionId?: string;
}

const NAV_ITEMS: NavItem[] = [
  { id: 'home', label: 'Home', href: '#home', sectionId: 'home' },
  { id: 'features', label: 'Features', href: '#features', sectionId: 'features' },
  { id: 'how-it-works', label: 'How it works', href: '#how-it-works', sectionId: 'how-it-works' },
  { id: 'leaderboard', label: 'Leaderboard', to: '/leaderboard' },
  { id: 'trust', label: 'About us', href: '#trust', sectionId: 'trust' },
];

export const Navbar: React.FC = () => {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const [activeIndex, setActiveIndex] = useState(0);
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null);
  const [dotStyle, setDotStyle] = useState({ left: 0, opacity: 0 });
  const [isReady, setIsReady] = useState(false);

  const navRef = useRef<HTMLElement>(null);
  const itemRefs = useRef<(HTMLElement | null)[]>([]);

  // Sync active section based on current route and scroll position on landing page
  useEffect(() => {
    if (location.pathname === '/leaderboard') {
      setActiveIndex(3);
      return;
    }

    if (location.pathname !== '/') {
      setActiveIndex(-1);
      return;
    }

    const handleScroll = () => {
      const scrollPos = window.scrollY + 140;

      // Check sections from bottom to top
      const sections = [
        { id: 'trust', index: 4 },
        { id: 'features', index: 1 },
        { id: 'how-it-works', index: 2 },
      ];

      for (const sec of sections) {
        const el = document.getElementById(sec.id);
        if (el) {
          const top = el.offsetTop;
          const height = el.offsetHeight;
          if (scrollPos >= top && scrollPos < top + height) {
            setActiveIndex(sec.index);
            return;
          }
        }
      }

      if (window.scrollY < 300) {
        setActiveIndex(0); // Home
      }
    };

    window.addEventListener('scroll', handleScroll, { passive: true });
    handleScroll();

    return () => window.removeEventListener('scroll', handleScroll);
  }, [location.pathname]);

  const targetIndex = hoveredIndex !== null ? hoveredIndex : (activeIndex >= 0 ? activeIndex : 0);

  // Recalculate dot position based on target item's bounding rect
  const updateDotPosition = useCallback(() => {
    const navEl = navRef.current;
    const targetEl = itemRefs.current[targetIndex];

    if (navEl && targetEl) {
      const navRect = navEl.getBoundingClientRect();
      const targetRect = targetEl.getBoundingClientRect();
      const center = targetRect.left - navRect.left + targetRect.width / 2;

      setDotStyle({
        left: center,
        opacity: activeIndex >= 0 || hoveredIndex !== null ? 1 : 0,
      });
      setIsReady(true);
    }
  }, [targetIndex, activeIndex, hoveredIndex]);

  useEffect(() => {
    updateDotPosition();

    // Recompute on window resize and font load
    const timer = setTimeout(updateDotPosition, 60);
    window.addEventListener('resize', updateDotPosition);

    return () => {
      clearTimeout(timer);
      window.removeEventListener('resize', updateDotPosition);
    };
  }, [updateDotPosition]);

  const scrollToSection = (e: React.MouseEvent, sectionId: string) => {
    e.preventDefault();
    setMobileMenuOpen(false);

    const performScroll = () => {
      const el = document.getElementById(sectionId);
      if (el) {
        const navHeight = 80;
        const elementPosition = el.getBoundingClientRect().top;
        const offsetPosition = elementPosition + window.pageYOffset - navHeight;
        window.scrollTo({
          top: Math.max(0, offsetPosition),
          behavior: 'smooth',
        });
      } else {
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    };

    if (window.location.pathname !== '/') {
      navigate('/');
      setTimeout(performScroll, 150);
    } else {
      performScroll();
    }
  };

  const handleLogoClick = (e: React.MouseEvent) => {
    e.preventDefault();
    setMobileMenuOpen(false);
    setActiveIndex(0);

    if (window.location.pathname !== '/') {
      navigate('/');
      setTimeout(() => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }, 150);
    } else {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  return (
    <header className="sticky top-0 z-40 bg-[#FBFBF9]/90 backdrop-blur-md border-b border-black/[0.04]">
      <div className="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between">
        {/* Brand Logo - Smoothly scrolls to hero/top */}
        <a href="#home" onClick={handleLogoClick} className="flex items-center gap-2.5 group cursor-pointer">
          <div className="w-10 h-10 rounded-2xl bg-memora-green-light flex items-center justify-center text-memora-green group-hover:scale-105 transition-transform duration-200">
            <svg
              className="w-6 h-6"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2.2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M9.5 2A2.5 2.5 0 0 1 12 4.5v15a2.5 2.5 0 0 1-4.96.44 2.5 2.5 0 0 1-2.96-3.08 3 3 0 0 1-.34-5.58 2.5 2.5 0 0 1 1.32-4.24 2.5 2.5 0 0 1 4.44-2.04Z" />
              <path d="M14.5 2A2.5 2.5 0 0 0 12 4.5v15a2.5 2.5 0 0 0 4.96.44 2.5 2.5 0 0 0 2.96-3.08 3 3 0 0 0 .34-5.58 2.5 2.5 0 0 0-1.32-4.24 2.5 2.5 0 0 0-4.44-2.04Z" />
            </svg>
          </div>
          <span className="text-xl font-extrabold tracking-tight text-memora-dark">Memora</span>
        </a>

        {/* Desktop Nav Links with Dynamic Smooth Gliding Indicator */}
        <nav
          ref={navRef}
          onMouseLeave={() => setHoveredIndex(null)}
          className="hidden md:flex items-center gap-8 relative py-2"
        >
          {NAV_ITEMS.map((item, idx) => {
            const isHighlighted = targetIndex === idx;
            const commonClasses = `text-sm tracking-tight transition-colors duration-200 cursor-pointer flex flex-col items-center pb-1.5 ${
              isHighlighted
                ? 'font-semibold text-memora-dark'
                : 'font-medium text-memora-text-muted hover:text-memora-dark'
            }`;

            if (item.to) {
              return (
                <Link
                  key={item.id}
                  ref={(el) => {
                    itemRefs.current[idx] = el;
                  }}
                  to={item.to}
                  onMouseEnter={() => setHoveredIndex(idx)}
                  className={commonClasses}
                >
                  {item.label}
                </Link>
              );
            }

            return (
              <a
                key={item.id}
                ref={(el) => {
                  itemRefs.current[idx] = el;
                }}
                href={item.href}
                onClick={(e) => {
                  if (item.sectionId) {
                    scrollToSection(e, item.sectionId);
                    setActiveIndex(idx);
                  }
                }}
                onMouseEnter={() => setHoveredIndex(idx)}
                className={commonClasses}
              >
                {item.label}
              </a>
            );
          })}

          {/* Dynamic Moving Dot Indicator */}
          <span
            aria-hidden="true"
            className={`absolute bottom-0 left-0 w-1.5 h-1.5 rounded-full bg-memora-green pointer-events-none transition-all duration-300 ease-out ${
              isReady && dotStyle.opacity > 0 ? 'opacity-100 scale-100' : 'opacity-0 scale-50'
            }`}
            style={{
              transform: `translateX(${dotStyle.left}px) translateX(-50%)`,
            }}
          />
        </nav>

        {/* Right CTA / Auth buttons */}
        <div className="hidden md:flex items-center gap-4">
          {isAuthenticated ? (
            <Button
              variant="primary"
              size="md"
              onClick={() => navigate('/dashboard')}
              rightIcon={<ArrowRight className="w-4 h-4" />}
            >
              Dashboard ({user?.name.split(' ')[0]})
            </Button>
          ) : (
            <>
              <Link
                to="/login"
                className="text-sm font-semibold text-memora-dark hover:text-memora-green transition px-3 py-2"
              >
                Log in
              </Link>
              <Button
                variant="primary"
                size="md"
                onClick={() => navigate('/register')}
                className="rounded-full shadow-sm"
              >
                Get Started
              </Button>
            </>
          )}
        </div>

        {/* Mobile Hamburger Toggle */}
        <button
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          className="md:hidden p-2 rounded-xl text-memora-dark hover:bg-stone-100 transition"
          aria-label="Toggle navigation"
        >
          {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
        </button>
      </div>

      {/* Mobile Drawer */}
      {mobileMenuOpen && (
        <div className="md:hidden bg-white border-b border-black/[0.08] px-6 py-6 space-y-4 animate-fade-in shadow-xl">
          <nav className="flex flex-col gap-3">
            <a
              href="#home"
              onClick={(e) => scrollToSection(e, 'home')}
              className="text-base font-semibold text-memora-dark py-2 cursor-pointer"
            >
              Home
            </a>
            <a
              href="#features"
              onClick={(e) => scrollToSection(e, 'features')}
              className="text-base font-medium text-memora-text-muted py-2 cursor-pointer"
            >
              Features
            </a>
            <a
              href="#how-it-works"
              onClick={(e) => scrollToSection(e, 'how-it-works')}
              className="text-base font-medium text-memora-text-muted py-2 cursor-pointer"
            >
              How it works
            </a>
            <Link
              to="/leaderboard"
              onClick={() => setMobileMenuOpen(false)}
              className="text-base font-medium text-memora-text-muted py-2"
            >
              Leaderboard
            </Link>
            <a
              href="#trust"
              onClick={(e) => scrollToSection(e, 'trust')}
              className="text-base font-medium text-memora-text-muted py-2 cursor-pointer"
            >
              About us
            </a>
          </nav>
          <div className="pt-4 border-t border-stone-100 flex flex-col gap-3">
            {isAuthenticated ? (
              <Button
                variant="primary"
                size="md"
                onClick={() => {
                  setMobileMenuOpen(false);
                  navigate('/dashboard');
                }}
              >
                Go to Dashboard
              </Button>
            ) : (
              <>
                <Button
                  variant="outline"
                  size="md"
                  onClick={() => {
                    setMobileMenuOpen(false);
                    navigate('/login');
                  }}
                >
                  Log in
                </Button>
                <Button
                  variant="primary"
                  size="md"
                  onClick={() => {
                    setMobileMenuOpen(false);
                    navigate('/register');
                  }}
                >
                  Get Started
                </Button>
              </>
            )}
          </div>
        </div>
      )}
    </header>
  );
};

export default Navbar;
