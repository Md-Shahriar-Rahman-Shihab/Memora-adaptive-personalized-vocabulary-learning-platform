import React from 'react';
import { Link } from 'react-router-dom';
import { Sparkles } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-[#FBFBF9] py-14 border-t border-black/[0.06]">
      <div className="max-w-7xl mx-auto px-6">
        <div className="grid grid-cols-2 md:grid-cols-5 gap-10 pb-12 border-b border-black/[0.06]">
          {/* Brand Col */}
          <div className="col-span-2 space-y-4">
            <Link to="/" className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-2xl bg-memora-green-light flex items-center justify-center text-memora-green">
                <Sparkles className="w-5 h-5 fill-current" />
              </div>
              <span className="text-xl font-extrabold tracking-tight text-memora-dark">Memora</span>
            </Link>
            <p className="text-xs sm:text-sm text-memora-text-muted max-w-sm leading-relaxed">
              AI memory-based adaptive vocabulary learning platform. Designed to reinforce retention,
              diagnose proficiency, and personalizing everyday revision.
            </p>
          </div>

          {/* Col 1 */}
          <div>
            <h4 className="text-xs font-bold text-memora-dark uppercase tracking-wider mb-4">
              Product
            </h4>
            <ul className="space-y-2.5 text-xs text-memora-text-muted">
              <li>
                <a href="#how-it-works" className="hover:text-memora-dark transition">
                  Adaptive Engine
                </a>
              </li>
              <li>
                <Link to="/assessment" className="hover:text-memora-dark transition">
                  CEFR Placement
                </Link>
              </li>
              <li>
                <Link to="/leaderboard" className="hover:text-memora-dark transition">
                  Global Leaderboard
                </Link>
              </li>
            </ul>
          </div>

          {/* Col 2 */}
          <div>
            <h4 className="text-xs font-bold text-memora-dark uppercase tracking-wider mb-4">
              Resources
            </h4>
            <ul className="space-y-2.5 text-xs text-memora-text-muted">
              <li>
                <span className="text-stone-400">SM-2 Spaced Repetition</span>
              </li>
              <li>
                <span className="text-stone-400">CEFR Framework</span>
              </li>
              <li>
                <span className="text-stone-400">Documentation</span>
              </li>
            </ul>
          </div>

          {/* Col 3 */}
          <div>
            <h4 className="text-xs font-bold text-memora-dark uppercase tracking-wider mb-4">Legal</h4>
            <ul className="space-y-2.5 text-xs text-memora-text-muted">
              <li>
                <span className="text-stone-400">Privacy Policy</span>
              </li>
              <li>
                <span className="text-stone-400">Terms of Service</span>
              </li>
              <li>
                <span className="text-stone-400">Academic Integrity</span>
              </li>
            </ul>
          </div>
        </div>

        {/* Bottom copyright */}
        <div className="pt-8 flex flex-col sm:flex-row items-center justify-between text-xs text-memora-text-muted gap-4">
          <p>© {new Date().getFullYear()} Memora Platform. All rights reserved.</p>
          <p className="font-medium text-stone-500">
            Engineered with Spring Boot 3 & React TypeScript.
          </p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
