import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useAuth } from '../context/AuthContext';
import { FileText, AlertCircle, ArrowRight, Loader2 } from 'lucide-react';

const registerSchema = z.object({
  fullName: z.string().min(2, 'Full name must be at least 2 characters'),
  companyName: z.string().min(2, 'Business name must be at least 2 characters'),
  email: z.string().email('Please enter a valid work email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
});

type RegisterFormData = z.infer<typeof registerSchema>;

export const RegisterPage: React.FC = () => {
  const { register: registerAuth } = useAuth();
  const navigate = useNavigate();
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = async (data: RegisterFormData) => {
    try {
      setServerError(null);
      await registerAuth(data);
      navigate('/dashboard');
    } catch (err: any) {
      setServerError(err.message || 'Registration failed. Please check your details.');
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8 font-sans">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <Link to="/" className="inline-flex items-center space-x-2">
          <div className="w-10 h-10 rounded-xl bg-blue-600 flex items-center justify-center text-white shadow-sm">
            <FileText className="w-6 h-6" />
          </div>
          <span className="font-extrabold text-2xl text-slate-900 tracking-tight">SmartQuote</span>
        </Link>
        <h2 className="mt-6 text-3xl font-bold tracking-tight text-slate-900">Start creating quotes</h2>
        <p className="mt-2 text-sm text-slate-600">
          Already have an account?{' '}
          <Link to="/login" className="font-semibold text-blue-600 hover:text-blue-500">
            Sign in
          </Link>
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-6 shadow-sm border border-slate-200/80 rounded-2xl sm:px-10">
          {serverError && (
            <div className="mb-6 p-4 rounded-xl bg-red-50 border border-red-200 flex items-start space-x-3 text-red-700 text-sm">
              <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
              <span>{serverError}</span>
            </div>
          )}

          <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-1">Your Full Name</label>
              <input
                type="text"
                placeholder="John Doe"
                {...register('fullName')}
                className={`w-full px-3.5 py-2.5 rounded-lg border text-sm transition-colors focus:outline-hidden focus:ring-2 ${
                  errors.fullName
                    ? 'border-red-300 focus:ring-red-400'
                    : 'border-slate-300 focus:border-blue-600 focus:ring-blue-600/20'
                }`}
              />
              {errors.fullName && <p className="mt-1 text-xs text-red-600 font-medium">{errors.fullName.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-1">Business / Company Name</label>
              <input
                type="text"
                placeholder="Acme Enterprises Pvt Ltd"
                {...register('companyName')}
                className={`w-full px-3.5 py-2.5 rounded-lg border text-sm transition-colors focus:outline-hidden focus:ring-2 ${
                  errors.companyName
                    ? 'border-red-300 focus:ring-red-400'
                    : 'border-slate-300 focus:border-blue-600 focus:ring-blue-600/20'
                }`}
              />
              {errors.companyName && <p className="mt-1 text-xs text-red-600 font-medium">{errors.companyName.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-1">Work Email</label>
              <input
                type="email"
                placeholder="john@acme.com"
                {...register('email')}
                className={`w-full px-3.5 py-2.5 rounded-lg border text-sm transition-colors focus:outline-hidden focus:ring-2 ${
                  errors.email
                    ? 'border-red-300 focus:ring-red-400'
                    : 'border-slate-300 focus:border-blue-600 focus:ring-blue-600/20'
                }`}
              />
              {errors.email && <p className="mt-1 text-xs text-red-600 font-medium">{errors.email.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-1">Password</label>
              <input
                type="password"
                placeholder="At least 6 characters"
                {...register('password')}
                className={`w-full px-3.5 py-2.5 rounded-lg border text-sm transition-colors focus:outline-hidden focus:ring-2 ${
                  errors.password
                    ? 'border-red-300 focus:ring-red-400'
                    : 'border-slate-300 focus:border-blue-600 focus:ring-blue-600/20'
                }`}
              />
              {errors.password && <p className="mt-1 text-xs text-red-600 font-medium">{errors.password.message}</p>}
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full mt-4 flex items-center justify-center space-x-2 py-3 px-4 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-sm shadow-xs transition-colors disabled:opacity-60"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Setting up account...</span>
                </>
              ) : (
                <>
                  <span>Create Account</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

