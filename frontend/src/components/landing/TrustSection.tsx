import React from 'react';

export const TrustSection: React.FC = () => {
  const institutions = [
    {
      name: 'United International University',
      abbr: 'UIU',
      logo: '/images/universities/uiu.svg',
    },
    {
      name: 'BRAC University',
      abbr: 'BRAC',
      logo: '/images/universities/brac.svg',
    },
    {
      name: 'Daffodil International University',
      abbr: 'DIU',
      logo: '/images/universities/diu.svg',
    },
    {
      name: 'North South University',
      abbr: 'NSU',
      logo: '/images/universities/nsu.svg',
    },
    {
      name: 'Independent University, Bangladesh',
      abbr: 'IUB',
      logo: '/images/universities/iub.svg',
    },
  ];

  return (
    <section id="trust" className="py-16 bg-[#FBFBF9] border-b border-black/[0.04]">
      <div className="max-w-7xl mx-auto px-6 text-center">
        <p className="text-xs sm:text-sm font-semibold text-memora-text-muted uppercase tracking-wider mb-8">
          Trusted by learners from top universities and schools
        </p>

        <div className="flex flex-wrap items-center justify-center gap-8 md:gap-14 opacity-80">
          {institutions.map((inst) => (
            <div
              key={inst.abbr}
              className="flex items-center gap-2.5 text-stone-600 hover:text-memora-dark hover:opacity-100 transition duration-200 cursor-default select-none group"
            >
              <div className="w-8 h-8 rounded-lg flex items-center justify-center shrink-0 overflow-hidden bg-transparent">
                <img
                  src={inst.logo}
                  alt={`${inst.name} logo`}
                  className="w-full h-full object-contain filter grayscale contrast-125 opacity-75 group-hover:grayscale-0 group-hover:opacity-100 group-hover:scale-110 transition-all duration-300"
                  loading="lazy"
                />
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
