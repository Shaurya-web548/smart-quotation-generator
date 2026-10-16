import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { api } from '../services/api';
import { Quotation, QuotationStatus } from '../types';
import {
  ArrowLeft,
  Download,
  Printer,
  Copy,
  CheckCircle,
  XCircle,
  Clock,
  Send,
  AlertCircle,
  Loader2,
  Building2,
  Calendar
} from 'lucide-react';

export const QuotationDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { company } = useAuth();

  const [quotation, setQuotation] = useState<Quotation | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isUpdatingStatus, setIsUpdatingStatus] = useState(false);

  const fetchQuotation = async () => {
    if (!id) return;
    try {
      setIsLoading(true);
      setError(null);
      const data = await api.quotations.getById(Number(id));
      setQuotation(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load quotation');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchQuotation();
  }, [id]);

  const handleStatusChange = async (newStatus: QuotationStatus) => {
    if (!id) return;
    try {
      setIsUpdatingStatus(true);
      const updated = await api.quotations.updateStatus(Number(id), newStatus);
      setQuotation(updated);
    } catch (err: any) {
      setError(err.message || 'Status transition rejected');
    } finally {
      setIsUpdatingStatus(false);
    }
  };

  const handleDuplicate = async () => {
    if (!id) return;
    try {
      const duplicate = await api.quotations.duplicate(Number(id));
      navigate(`/quotations/${duplicate.id}`);
    } catch (err: any) {
      setError(err.message || 'Failed to duplicate quotation');
    }
  };

  const handlePrint = () => {
    window.print();
  };

  if (isLoading) {
    return (
      <div className="py-24 flex flex-col items-center justify-center space-y-3">
        <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
        <span className="text-sm font-medium text-slate-500">Rendering quotation preview...</span>
      </div>
    );
  }

  if (error || !quotation) {
    return (
      <div className="p-6 rounded-2xl bg-red-50 border border-red-200 text-red-700">
        <h3 className="font-bold">Error</h3>
        <p className="text-sm mt-1">{error || 'Quotation not found'}</p>
        <Link to="/quotations" className="mt-3 inline-block text-xs font-bold text-red-800 underline">
          Return to list
        </Link>
      </div>
    );
  }

  const currencySymbol = company?.currencySymbol || '₹';

  return (
    <div className="max-w-4xl mx-auto space-y-6 pb-16 font-sans">
      {/* Top Action Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 print:hidden">
        <button
          onClick={() => navigate('/quotations')}
          className="inline-flex items-center space-x-1 text-sm font-semibold text-slate-500 hover:text-slate-800 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>All Quotations</span>
        </button>

        <div className="flex flex-wrap items-center gap-2.5">
          {quotation.status === 'DRAFT' && (
            <button
              onClick={() => handleStatusChange('SENT')}
              disabled={isUpdatingStatus}
              className="inline-flex items-center space-x-1.5 px-3.5 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs shadow-xs transition-colors"
            >
              <Send className="w-3.5 h-3.5" />
              <span>Mark as Sent</span>
            </button>
          )}

          {quotation.status === 'SENT' && (
            <>
              <button
                onClick={() => handleStatusChange('ACCEPTED')}
                disabled={isUpdatingStatus}
                className="inline-flex items-center space-x-1.5 px-3.5 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-xs shadow-xs transition-colors"
              >
                <CheckCircle className="w-3.5 h-3.5" />
                <span>Mark Accepted</span>
              </button>
              <button
                onClick={() => handleStatusChange('REJECTED')}
                disabled={isUpdatingStatus}
                className="inline-flex items-center space-x-1.5 px-3.5 py-2 rounded-xl bg-rose-600 hover:bg-rose-700 text-white font-semibold text-xs shadow-xs transition-colors"
              >
                <XCircle className="w-3.5 h-3.5" />
                <span>Mark Rejected</span>
              </button>
            </>
          )}

          <button
            onClick={handleDuplicate}
            className="inline-flex items-center space-x-1.5 px-3.5 py-2 rounded-xl border border-slate-300 bg-white hover:bg-slate-50 text-slate-700 font-semibold text-xs shadow-xs transition-colors"
          >
            <Copy className="w-3.5 h-3.5" />
            <span>Duplicate</span>
          </button>

          <button
            onClick={handlePrint}
            className="inline-flex items-center space-x-1.5 px-3.5 py-2 rounded-xl border border-slate-300 bg-white hover:bg-slate-50 text-slate-700 font-semibold text-xs shadow-xs transition-colors"
          >
            <Printer className="w-3.5 h-3.5" />
            <span>Print</span>
          </button>

          <button
            onClick={() => api.quotations.downloadPdf(quotation.id, `${quotation.quotationNumber}.pdf`)}
            className="inline-flex items-center space-x-1.5 px-3.5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-xs shadow-xs transition-colors"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Download PDF</span>
          </button>
        </div>
      </div>

      {/* Printable Invoice / Quotation Sheet */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-xs p-8 sm:p-12 space-y-8 print:border-none print:shadow-none print:p-0">
        {/* Document Header */}
        <div className="flex flex-col sm:flex-row justify-between items-start gap-6 border-b border-slate-100 pb-8">
          <div>
            <div className="flex items-center space-x-3 mb-2">
              <div className="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center text-white">
                <Building2 className="w-5 h-5" />
              </div>
              <h2 className="text-2xl font-black text-slate-900">{company?.name}</h2>
            </div>
            {company?.address && <p className="text-xs text-slate-500 whitespace-pre-line">{company.address}</p>}
            <div className="text-xs text-slate-500 mt-1 space-x-2">
              {company?.email && <span>{company.email}</span>}
              {company?.phone && <span>• {company.phone}</span>}
              {company?.taxId && <span>• Tax ID: {company.taxId}</span>}
            </div>
          </div>

          <div className="text-left sm:text-right">
            <span className="text-3xl font-black text-slate-900 tracking-tight block">QUOTATION</span>
            <span className="text-sm font-bold text-blue-600 mt-1 block">{quotation.quotationNumber}</span>
            <div className="mt-2 text-xs text-slate-500 space-y-1">
              <div>
                <span className="text-slate-400">Date:</span> {quotation.issueDate}
              </div>
              <div>
                <span className="text-slate-400">Valid Until:</span> {quotation.expiryDate}
              </div>
              <div>
                <span className="text-slate-400">Status:</span>{' '}
                <span className="font-bold text-slate-800 uppercase">{quotation.status}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Client info */}
        <div className="bg-slate-50/60 p-5 rounded-xl border border-slate-200/60">
          <span className="text-xs font-bold text-slate-400 uppercase tracking-wider block mb-1">
            Quotation Prepared For
          </span>
          <h3 className="text-base font-bold text-slate-900">{quotation.customerName}</h3>
          <div className="text-xs text-slate-600 mt-1 space-y-0.5">
            {quotation.customerEmail && <div>{quotation.customerEmail}</div>}
            {quotation.customerPhone && <div>{quotation.customerPhone}</div>}
            {quotation.customerAddress && <div className="text-slate-500">{quotation.customerAddress}</div>}
          </div>
        </div>

        {/* Items Table */}
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-700">
            <thead className="bg-slate-50 text-xs font-bold text-slate-500 uppercase tracking-wider border-y border-slate-200">
              <tr>
                <th className="py-3 px-4">#</th>
                <th className="py-3 px-4">Item Description</th>
                <th className="py-3 px-4 text-center">Qty</th>
                <th className="py-3 px-4 text-right">Unit Price</th>
                <th className="py-3 px-4 text-right">Amount</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {quotation.items.map((item, idx) => (
                <tr key={idx}>
                  <td className="py-3.5 px-4 text-xs text-slate-400">{idx + 1}</td>
                  <td className="py-3.5 px-4 font-medium text-slate-900">{item.description}</td>
                  <td className="py-3.5 px-4 text-center">{item.quantity}</td>
                  <td className="py-3.5 px-4 text-right">
                    {currencySymbol} {Number(item.unitPrice).toFixed(2)}
                  </td>
                  <td className="py-3.5 px-4 text-right font-semibold text-slate-900">
                    {currencySymbol} {Number(item.amount).toFixed(2)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Totals Summary */}
        <div className="flex justify-end pt-4 border-t border-slate-100">
          <div className="w-full sm:w-72 space-y-2.5 text-sm">
            <div className="flex justify-between text-slate-600">
              <span>Subtotal:</span>
              <span className="font-semibold text-slate-900">
                {currencySymbol} {Number(quotation.subtotal).toFixed(2)}
              </span>
            </div>

            {Number(quotation.discountPercentage) > 0 && (
              <>
                <div className="flex justify-between text-slate-600">
                  <span>Discount ({quotation.discountPercentage}%):</span>
                  <span className="font-semibold text-slate-700">
                    -{currencySymbol} {Number(quotation.discountAmount).toFixed(2)}
                  </span>
                </div>
                <div className="flex justify-between text-slate-600">
                  <span>Taxable Amount:</span>
                  <span className="font-semibold text-slate-900">
                    {currencySymbol} {Number(quotation.taxableAmount).toFixed(2)}
                  </span>
                </div>
              </>
            )}

            {Number(quotation.taxPercentage) > 0 && (
              <div className="flex justify-between text-slate-600">
                <span>GST / Tax ({quotation.taxPercentage}%):</span>
                <span className="font-semibold text-slate-700">
                  +{currencySymbol} {Number(quotation.taxAmount).toFixed(2)}
                </span>
              </div>
            )}

            <div className="border-t-2 border-slate-900 pt-3 flex justify-between items-baseline">
              <span className="font-bold text-slate-900 text-base">Total Amount:</span>
              <span className="font-extrabold text-blue-600 text-xl">
                {currencySymbol} {Number(quotation.totalAmount).toFixed(2)}
              </span>
            </div>
          </div>
        </div>

        {/* Terms & Notes */}
        {(quotation.notes || quotation.terms) && (
          <div className="pt-8 border-t border-slate-100 grid grid-cols-1 md:grid-cols-2 gap-6 text-xs text-slate-500">
            {quotation.notes && (
              <div>
                <span className="font-bold text-slate-700 block mb-1 uppercase tracking-wider">Notes</span>
                <p className="whitespace-pre-line">{quotation.notes}</p>
              </div>
            )}
            {quotation.terms && (
              <div>
                <span className="font-bold text-slate-700 block mb-1 uppercase tracking-wider">
                  Terms & Conditions
                </span>
                <p className="whitespace-pre-line">{quotation.terms}</p>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
