import {
  ApiResponse,
  AuthResponse,
  Company,
  Customer,
  DashboardSummary,
  PageResponse,
  Quotation,
  QuotationItem,
  QuotationStatus
} from '../types';

const API_BASE = import.meta.env.VITE_API_BASE_URL || '';

function getAuthHeader(): Record<string, string> {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const headers = {
    'Content-Type': 'application/json',
    ...getAuthHeader(),
    ...options.headers,
  };

  const url = `${API_BASE}${endpoint}`;
  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('company');
    if (!window.location.pathname.startsWith('/login') && !window.location.pathname.startsWith('/register') && window.location.pathname !== '/') {
      window.location.href = '/login';
    }
  }

  if (response.status === 204) {
    return {} as T;
  }

  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/pdf')) {
    const blob = await response.blob();
    return blob as unknown as T;
  }

  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.message || `Request failed with status ${response.status}`);
  }

  return data.data !== undefined ? data.data : data;
}

export const api = {
  // Auth
  auth: {
    register: (body: { fullName: string; email: string; password: string; companyName: string }) =>
      request<AuthResponse>('/api/v1/auth/register', {
        method: 'POST',
        body: JSON.stringify(body),
      }),

    login: (body: { email: string; password: string }) =>
      request<AuthResponse>('/api/v1/auth/login', {
        method: 'POST',
        body: JSON.stringify(body),
      }),

    me: () => request<AuthResponse>('/api/v1/auth/me'),

    logout: () =>
      request<void>('/api/v1/auth/logout', {
        method: 'POST',
      }),
  },

  // Dashboard
  dashboard: {
    getSummary: () => request<DashboardSummary>('/api/v1/dashboard'),
  },

  // Customers
  customers: {
    list: () => request<Customer[]>('/api/v1/customers'),
    create: (body: { name: string; email?: string; phone?: string; address?: string; taxId?: string }) =>
      request<Customer>('/api/v1/customers', {
        method: 'POST',
        body: JSON.stringify(body),
      }),
  },

  // Quotations
  quotations: {
    list: (params: {
      search?: string;
      status?: QuotationStatus | '';
      startDate?: string;
      endDate?: string;
      page?: number;
      size?: number;
      sortBy?: string;
      sortDir?: string;
    }) => {
      const query = new URLSearchParams();
      if (params.search) query.append('search', params.search);
      if (params.status) query.append('status', params.status);
      if (params.startDate) query.append('startDate', params.startDate);
      if (params.endDate) query.append('endDate', params.endDate);
      if (params.page !== undefined) query.append('page', params.page.toString());
      if (params.size !== undefined) query.append('size', params.size.toString());
      if (params.sortBy) query.append('sortBy', params.sortBy);
      if (params.sortDir) query.append('sortDir', params.sortDir);

      const qs = query.toString();
      return request<PageResponse<Quotation>>(`/api/v1/quotations${qs ? `?${qs}` : ''}`);
    },

    getById: (id: number) => request<Quotation>(`/api/v1/quotations/${id}`),

    create: (body: {
      customerId?: number;
      customerName: string;
      customerEmail?: string;
      customerPhone?: string;
      customerAddress?: string;
      issueDate: string;
      expiryDate: string;
      discountPercentage: number;
      taxPercentage: number;
      notes?: string;
      terms?: string;
      status?: QuotationStatus;
      items: QuotationItem[];
    }) =>
      request<Quotation>('/api/v1/quotations', {
        method: 'POST',
        body: JSON.stringify(body),
      }),

    update: (id: number, body: any) =>
      request<Quotation>(`/api/v1/quotations/${id}`, {
        method: 'PUT',
        body: JSON.stringify(body),
      }),

    duplicate: (id: number) =>
      request<Quotation>(`/api/v1/quotations/${id}/duplicate`, {
        method: 'POST',
      }),

    updateStatus: (id: number, status: QuotationStatus) =>
      request<Quotation>(`/api/v1/quotations/${id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status }),
      }),

    downloadPdf: async (id: number, filename: string) => {
      const headers = { ...getAuthHeader() };
      const response = await fetch(`${API_BASE}/api/v1/quotations/${id}/pdf`, {
        headers,
      });

      if (!response.ok) {
        throw new Error('Failed to generate or download quotation PDF');
      }

      const blob = await response.blob();
      const downloadUrl = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = downloadUrl;
      link.download = filename || `Quotation-${id}.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(downloadUrl);
    },
  },

  // Settings
  settings: {
    getCompany: () => request<Company>('/api/v1/settings/company'),
    updateCompany: (body: Partial<Company>) =>
      request<Company>('/api/v1/settings/company', {
        method: 'PUT',
        body: JSON.stringify(body),
      }),
    uploadLogo: async (file: File) => {
      const formData = new FormData();
      formData.append('file', file);

      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}/api/v1/settings/company/logo`, {
        method: 'POST',
        headers: token ? { Authorization: `Bearer ${token}` } : {},
        body: formData,
      });

      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.message || 'Failed to upload logo');
      }
      return data.data as Company;
    },
  },
};
