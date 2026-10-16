export type Role = 'USER' | 'ADMIN';

export type QuotationStatus = 'DRAFT' | 'SENT' | 'ACCEPTED' | 'REJECTED' | 'EXPIRED';

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: Role;
}

export interface Company {
  id: number;
  name: string;
  logoUrl?: string;
  address?: string;
  phone?: string;
  email?: string;
  taxId?: string;
  currency: string;
  currencySymbol: string;
  defaultTaxRate: number;
  defaultValidityDays: number;
  defaultTerms?: string;
}

export interface Customer {
  id: number;
  name: string;
  email?: string;
  phone?: string;
  address?: string;
  taxId?: string;
}

export interface QuotationItem {
  id?: number;
  description: string;
  quantity: number;
  unitPrice: number;
  amount?: number;
  sortOrder?: number;
}

export interface Quotation {
  id: number;
  quotationNumber: string;
  companyId: number;
  customerId?: number;
  customerName: string;
  customerEmail?: string;
  customerPhone?: string;
  customerAddress?: string;
  status: QuotationStatus;
  issueDate: string;
  expiryDate: string;
  subtotal: number;
  discountPercentage: number;
  discountAmount: number;
  taxableAmount: number;
  taxPercentage: number;
  taxAmount: number;
  totalAmount: number;
  notes?: string;
  terms?: string;
  items: QuotationItem[];
  createdAt: string;
  updatedAt: string;
}

export interface DashboardSummary {
  totalQuotations: number;
  draftQuotations: number;
  sentQuotations: number;
  acceptedQuotations: number;
  rejectedQuotations: number;
  expiredQuotations: number;
  totalValue: number;
  acceptedValue: number;
  recentQuotations: Quotation[];
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: User;
  company: Company;
}

