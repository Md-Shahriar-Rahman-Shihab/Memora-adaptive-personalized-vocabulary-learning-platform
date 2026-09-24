import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Sparkles, ArrowRight, Lock, Mail, User, AlertCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';

export const RegisterPage: React.FC = () => {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !email || !password) {
      setError('Please fill in all fields.');
      return;
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters.');
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      await register(name, email, password);
      // Immediately navigate to /dashboard where locked onboarding experience is presented
      navigate('/dashboard', { replace: true });
    } catch (err: any) {
      const msg =
        err?.response?.data?.message || 'Registration failed. This email may already be registered.';
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#FBFBF9] flex flex-col justify-center py-12 px-6 lg:px-8">
      {/* Brand Header */}
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center mb-8">
        <Link to="/" className="inline-flex items-center gap-2.5 mb-4 group">
          <div className="w-10 h-10 rounded-2xl bg-memora-green-light flex items-center justify-center text-memora-green group-hover:scale-105 transition-transform">
            <Sparkles className="w-6 h-6 fill-current" />
          </div>
          <span className="text-2xl font-black tracking-tight text-memora-dark">Memora</span>
        </Link>
        <h2 className="text-3xl font-extrabold text-memora-dark tracking-tight">
          Start learning smarter
        </h2>
        <p className="text-sm text-memora-text-muted mt-1.5">
          Join Memora for personalized vocabulary retention
        </p>
      </div>

      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <Card variant="default" className="p-8 shadow-card border border-black/[0.08]">
          {error && (
            <div className="mb-5 p-3.5 rounded-2xl bg-red-50 border border-red-100 flex items-center gap-3 text-xs text-red-700 font-medium">
              <AlertCircle className="w-4 h-4 shrink-0 text-red-500" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Full Name */}
            <div>
              <label className="block text-xs font-bold text-memora-dark uppercase tracking-wider mb-2">
                Full Name
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-stone-400">
                  <User className="w-4 h-4" />
                </div>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. Alex Morgan"
                  className="w-full pl-10 pr-4 py-3 bg-stone-50/70 border border-black/[0.08] rounded-2xl text-sm text-memora-dark placeholder:text-stone-400 focus:outline-none focus:ring-2 focus:ring-memora-green focus:bg-white transition"
                />
              </div>
            </div>

            {/* Email Field */}
            <div>
              <label className="block text-xs font-bold text-memora-dark uppercase tracking-wider mb-2">
                Email address
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-stone-400">
                  <Mail className="w-4 h-4" />
                </div>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="e.g. alex@example.com"
                  className="w-full pl-10 pr-4 py-3 bg-stone-50/70 border border-black/[0.08] rounded-2xl text-sm text-memora-dark placeholder:text-stone-400 focus:outline-none focus:ring-2 focus:ring-memora-green focus:bg-white transition"
                />
              </div>
            </div>

            {/* Password Field */}
            <div>
              <label className="block text-xs font-bold text-memora-dark uppercase tracking-wider mb-2">
                Password
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-stone-400">
                  <Lock className="w-4 h-4" />
                </div>
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="At least 6 characters"
                  className="w-full pl-10 pr-4 py-3 bg-stone-50/70 border border-black/[0.08] rounded-2xl text-sm text-memora-dark placeholder:text-stone-400 focus:outline-none focus:ring-2 focus:ring-memora-green focus:bg-white transition"
                />
              </div>
            </div>

            <div className="pt-3">
              <Button
                type="submit"
                variant="primary"
                size="lg"
                isLoading={isLoading}
                className="w-full justify-center shadow-md"
                rightIcon={<ArrowRight className="w-4 h-4" />}
              >
                Create Account & Discover Level
              </Button>
            </div>
          </form>

          {/* Bottom link */}
          <div className="mt-6 pt-6 border-t border-black/[0.06] text-center">
            <p className="text-xs text-memora-text-muted">
              Already have an account?{' '}
              <Link to="/login" className="font-bold text-memora-green hover:underline ml-1">
                Sign in
              </Link>
            </p>
          </div>
        </Card>
      </div>
    </div>
  );
};

export default RegisterPage;
