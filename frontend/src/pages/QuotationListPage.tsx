import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { api } from '../services/api';
import { Quotation, QuotationStatus } from '../types';
import {
  FileText,
  Search,
  Plus,
  Eye,
  Copy,
  Download,
  AlertCircle,
  Loader2,
  ChevronLeft,
  ChevronRight,
  Filter
} from 'lucide-react';

export const QuotationListPage: React.FC = () => {
  const { company } = useAuth();
  const [quotations, setQuotations] = useState<Quotation[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState<QuotationStatus | ''>('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionMessage, setActionMessage] = useState<string | null>(null);

  const fetchQuotations = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const res = await api.quotations.list({
        search: search.trim() || undefined,
        status: status || undefined,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
        page,
        size: 10,
      });
      setQuotations(res.content);
      setTotalElements(res.totalElements);
      setTotalPages(res.totalPages || 1);
    } catch (err: any) {
      setError(err.message || 'Failed to load quotation history');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchQuotations();
  }, [page, status, startDate, endDate]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    fetchQuotations();
  };

  const handleDuplicate = async (id: number) => {
    try {
      setActionMessage('Duplicating quotation...');
      const duplicate = await api.quotations.duplicate(id);
      setActionMessage(`Created new draft ${duplicate.quotationNumber}`);
      fetchQuotations();
      setTimeout(() => setActionMessage(null), 4000);
    } catch (err: any) {
      setError(err.message || 'Failed to duplicate quotation');
    }
  };

  const getStatusBadge = (s: string) => {
    switch (s) {
      case 'DRAFT':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-slate-100 text-slate-700">Draft</span>;
      case 'SENT':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-100 text-blue-700">Sent</span>;
      case 'ACCEPTED':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-700">Accepted</span>;
      case 'REJECTED':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-rose-100 text-rose-700">Rejected</span>;
      case 'EXPIRED':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-700">Expired</span>;
      default:
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-slate-100 text-slate-700">{s}</span>;
    }
  };

  const currencySymbol = company?.currencySymbol || '₹';

  return (
    <div className="space-y-6 font-sans">
      {/* Header & New Action */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Quotation History</h1>
          <p className="text-sm text-slate-500 mt-1">
            Browse, search, duplicate, and download all generated business quotations.
          </p>
        </div>
        <Link
          to="/quotations/new"
          className="inline-flex items-center space-x-2 px-4 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-xl font-semibold text-sm shadow-xs transition-colors shrink-0"
        >
          <Plus className="w-4 h-4" />
          <span>New Quotation</span>
        </Link>
      </div>

      {actionMessage && (
        <div className="p-3 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs font-semibold">
          {actionMessage}
        </div>
      )}

      {error && (
        <div className="p-4 rounded-xl bg-red-50 border border-red-200 flex items-start space-x-3 text-red-700 text-sm">
          <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      {/* Filter / Search Bar */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs flex flex-col md:flex-row gap-3 items-stretch md:items-center justify-between">
        <form onSubmit={handleSearchSubmit} className="flex-1 relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search by quote # or customer name..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2 bg-slate-50 border border-slate-300 rounded-xl text-sm focus:outline-hidden focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600"
          />
        </form>

        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center space-x-1">
            <Filter className="w-4 h-4 text-slate-400" />
            <select
              value={status}
              onChange={(e) => {
                setStatus(e.target.value as QuotationStatus | '');
                setPage(0);
              }}
              className="border border-slate-300 rounded-xl px-3 py-2 text-xs bg-white text-slate-700 focus:outline-hidden focus:ring-2 focus:ring-blue-600/20"
            >
              <option value="">All Statuses</option>
              <option value="DRAFT">Draft</option>
              <option value="SENT">Sent</option>
              <option value="ACCEPTED">Accepted</option>
              <option value="REJECTED">Rejected</option>
              <option value="EXPIRED">Expired</option>
            </select>
          </div>

          <div className="flex items-center space-x-2">
            <input
              type="date"
              value={startDate}
              onChange={(e) => {
                setStartDate(e.target.value);
                setPage(0);
              }}
              className="border border-slate-300 rounded-xl px-2.5 py-1.5 text-xs bg-white text-slate-700 focus:outline-hidden"
              title="From Date"
            />
            <span className="text-slate-400 text-xs">to</span>
            <input
              type="date"
              value={endDate}
              onChange={(e) => {
                setEndDate(e.target.value);
                setPage(0);
              }}
              className="border border-slate-300 rounded-xl px-2.5 py-1.5 text-xs bg-white text-slate-700 focus:outline-hidden"
              title="To Date"
            />
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-xs overflow-hidden">
        {isLoading ? (
          <div className="py-24 flex flex-col items-center justify-center space-y-3">
            <Loader2 className="w-7 h-7 text-blue-600 animate-spin" />
            <span className="text-xs font-medium text-slate-500">Retrieving quotations...</span>
          </div>
        ) : quotations.length > 0 ? (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm text-slate-600">
                <thead className="bg-slate-50 text-xs font-bold text-slate-500 uppercase tracking-wider border-b border-slate-100">
                  <tr>
                    <th className="px-6 py-3.5">Quote #</th>
                    <th className="px-6 py-3.5">Customer</th>
                    <th className="px-6 py-3.5">Issue Date</th>
                    <th className="px-6 py-3.5">Valid Until</th>
                    <th className="px-6 py-3.5">Status</th>
                    <th className="px-6 py-3.5 text-right">Total Amount</th>
                    <th className="px-6 py-3.5 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {quotations.map((q) => (
                    <tr key={q.id} className="hover:bg-slate-50/70 transition-colors">
                      <td className="px-6 py-4 font-bold text-slate-900">{q.quotationNumber}</td>
                      <td className="px-6 py-4">
                        <div className="font-medium text-slate-800">{q.customerName}</div>
                        {q.customerEmail && <div className="text-xs text-slate-400">{q.customerEmail}</div>}
                      </td>
                      <td className="px-6 py-4 text-xs text-slate-600">{q.issueDate}</td>
                      <td className="px-6 py-4 text-xs text-slate-600">{q.expiryDate}</td>
                      <td className="px-6 py-4">{getStatusBadge(q.status)}</td>
                      <td className="px-6 py-4 text-right font-bold text-slate-900">
                        {currencySymbol} {Number(q.totalAmount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </td>
                      <td className="px-6 py-4 text-right">
                        <div className="inline-flex items-center space-x-1.5">
                          <Link
                            to={`/quotations/${q.id}`}
                            className="p-1.5 text-slate-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                            title="View Document"
                          >
                            <Eye className="w-4 h-4" />
                          </Link>
                          <button
                            onClick={() => handleDuplicate(q.id)}
                            className="p-1.5 text-slate-500 hover:text-amber-600 hover:bg-amber-50 rounded-lg transition-colors"
                            title="Duplicate Quote"
                          >
                            <Copy className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => api.quotations.downloadPdf(q.id, `${q.quotationNumber}.pdf`)}
                            className="p-1.5 text-slate-500 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"
                            title="Download PDF"
                          >
                            <Download className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination Controls */}
            <div className="px-6 py-4 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
              <span>
                Showing {quotations.length} of {totalElements} quotations
              </span>
              <div className="flex items-center space-x-2">
                <button
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={page === 0}
                  className="p-1.5 rounded-lg border border-slate-200 hover:bg-slate-50 disabled:opacity-40 transition-colors"
                >
                  <ChevronLeft className="w-4 h-4" />
                </button>
                <span className="font-semibold text-slate-700">
                  Page {page + 1} of {totalPages}
                </span>
                <button
                  onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                  disabled={page >= totalPages - 1}
                  className="p-1.5 rounded-lg border border-slate-200 hover:bg-slate-50 disabled:opacity-40 transition-colors"
                >
                  <ChevronRight className="w-4 h-4" />
                </button>
              </div>
            </div>
          </>
        ) : (
          <div className="py-20 text-center">
            <FileText className="w-10 h-10 text-slate-300 mx-auto mb-3" />
            <h3 className="text-sm font-bold text-slate-700">No matching quotations found</h3>
            <p className="text-xs text-slate-500 mt-1">Try adjusting your filters or search keywords.</p>
          </div>
        )}
      </div>
    </div>
  );
};
