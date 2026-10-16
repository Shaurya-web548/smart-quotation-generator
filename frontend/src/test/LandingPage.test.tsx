import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { LandingPage } from '../pages/LandingPage';
import React from 'react';

describe('LandingPage Component', () => {
  it('renders branding and main CTA buttons', () => {
    render(
      <BrowserRouter>
        <LandingPage />
      </BrowserRouter>
    );

    expect(screen.getByText('SmartQuote')).toBeInTheDocument();
    expect(screen.getByText('Create Your First Quotation')).toBeInTheDocument();
    expect(screen.getByText('Sign In to Dashboard')).toBeInTheDocument();
  });

  it('renders key business feature highlights', () => {
    render(
      <BrowserRouter>
        <LandingPage />
      </BrowserRouter>
    );

    expect(screen.getByText('Exact Financial Calculations')).toBeInTheDocument();
    expect(screen.getByText('Printable PDF Export')).toBeInTheDocument();
    expect(screen.getByText('Multi-Tenant Isolation')).toBeInTheDocument();
  });
});

