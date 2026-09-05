import React from 'react';

export const TrustSection: React.FC = () => {
  const institutions = [
    { name: 'United International University', abbr: 'UIU' },
    { name: 'BRAC University', abbr: 'BRAC' },
    { name: 'Daffodil International University', abbr: 'DIU' },
    { name: 'North South University', abbr: 'NSU' },
    { name: 'Independent University, Bangladesh', abbr: 'IUB' },
  ];

  return (
    <section id="trust" className="py-16 bg-[#FBFBF9] border-b border-black/[0.04]">
      <div className="max-w-7xl mx-auto px-6 text-center">
        <p className="text-xs sm:text-sm font-semibold text-memora-text-muted uppercase tracking-wider mb-8">
          Trusted by learners from top universities and schools
        </p>

        <div className="flex flex-wrap items-center justify-center gap-8 md:gap-14 opacity-75">
          {institutions.map((inst) => (
            <div
              key={inst.abbr}
              className="flex items-center gap-2 text-stone-500 hover:text-memora-dark hover:opacity-100 transition duration-200 cursor-default select-none"
            >
              {/* Minimal crest emblem */}
              <div className="w-8 h-8 rounded-full border border-stone-300 flex items-center justify-center text-[10px] font-black tracking-tighter">
                {inst.abbr}
              </div>
              <span className="text-xs sm:text-sm font-bold tracking-tight">
                {inst.name}
              </span>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default TrustSection;
