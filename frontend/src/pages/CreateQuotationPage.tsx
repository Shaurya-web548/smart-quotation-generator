import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { api } from '../services/api';
import { Customer, QuotationItem, QuotationStatus } from '../types';
import { Plus, Trash2, Save, Send, AlertCircle, ArrowLeft, Loader2 } from 'lucide-react';

export const CreateQuotationPage: React.FC = () => {
  const { company } = useAuth();
  const navigate = useNavigate();

  const [customerName, setCustomerName] = useState('');
  const [customerEmail, setCustomerEmail] = useState('');
  const [customerPhone, setCustomerPhone] = useState('');
  const [customerAddress, setCustomerAddress] = useState('');
  const [existingCustomers, setExistingCustomers] = useState<Customer[]>([]);
  const [selectedCustomerId, setSelectedCustomerId] = useState<number | undefined>();

  const today = new Date().toISOString().split('T')[0];
  const defaultExpiry = new Date(Date.now() + (company?.defaultValidityDays || 30) * 86400000)
    .toISOString()
    .split('T')[0];

  const [issueDate, setIssueDate] = useState(today);
  const [expiryDate, setExpiryDate] = useState(defaultExpiry);
  const [discountPercentage, setDiscountPercentage] = useState<number>(0);
  const [taxPercentage, setTaxPercentage] = useState<number>(company?.defaultTaxRate || 18);
  const [notes, setNotes] = useState('');
  const [terms, setTerms] = useState(company?.defaultTerms || '');

  const [items, setItems] = useState<QuotationItem[]>([
    { description: 'Widget', quantity: 2, unitPrice: 150.0 },
    { description: 'Gadget', quantity: 1, unitPrice: 99.5 },
  ]);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadCustomers() {
      try {
        const data = await api.customers.list();
        setExistingCustomers(data);
      } catch {
        // Silently handle customer listing error
      }
    }
    loadCustomers();
  }, []);

  const handleSelectCustomer = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const id = e.target.value ? Number(e.target.value) : undefined;
    setSelectedCustomerId(id);
    if (id) {
      const found = existingCustomers.find((c) => c.id === id);
      if (found) {
        setCustomerName(found.name);
        setCustomerEmail(found.email || '');
        setCustomerPhone(found.phone || '');
        setCustomerAddress(found.address || '');
      }
    }
  };

  const handleAddItem = () => {
    setItems([...items, { description: '', quantity: 1, unitPrice: 0 }]);
  };

  const handleRemoveItem = (index: number) => {
    if (items.length <= 1) {
      setError('Quotation must contain at least one line item');
      return;
    }
    setItems(items.filter((_, i) => i !== index));
  };

  const handleItemChange = (index: number, field: keyof QuotationItem, value: any) => {
    const updated = [...items];
    updated[index] = { ...updated[index], [field]: value };
    setItems(updated);
  };

  // Preview calculations for instantaneous visual feedback
  const subtotal = items.reduce((acc, it) => acc + (Number(it.quantity) || 0) * (Number(it.unitPrice) || 0), 0);
  const discountAmount = (subtotal * (Number(discountPercentage) || 0)) / 100;
  const taxableAmount = subtotal - discountAmount;
  const taxAmount = (taxableAmount * (Number(taxPercentage) || 0)) / 100;
  const totalAmount = taxableAmount + taxAmount;

  const currencySymbol = company?.currencySymbol || '₹';

  const handleSubmit = async (status: QuotationStatus) => {
    try {
      setError(null);
      if (!customerName.trim()) {
        setError('Please enter a customer name');
        return;
      }

      for (let i = 0; i < items.length; i++) {
        if (!items[i].description.trim()) {
          setError(`Item #${i + 1} description cannot be empty`);
          return;
        }
        if (items[i].quantity <= 0) {
          setError(`Item #${i + 1} quantity must be greater than zero`);
          return;
        }
        if (items[i].unitPrice < 0) {
          setError(`Item #${i + 1} unit price cannot be negative`);
          return;
        }
      }

      setIsSubmitting(true);
      const res = await api.quotations.create({
        customerId: selectedCustomerId,
        customerName: customerName.trim(),
        customerEmail: customerEmail.trim() || undefined,
        customerPhone: customerPhone.trim() || undefined,
        customerAddress: customerAddress.trim() || undefined,
        issueDate,
        expiryDate,
        discountPercentage: Number(discountPercentage) || 0,
        taxPercentage: Number(taxPercentage) || 0,
        notes: notes.trim() || undefined,
        terms: terms.trim() || undefined,
        status,
        items: items.map((it, idx) => ({
          description: it.description.trim(),
          quantity: Number(it.quantity),
          unitPrice: Number(it.unitPrice),
          sortOrder: idx,
        })),
      });

      navigate(`/quotations/${res.id}`);
    } catch (err: any) {
      setError(err.message || 'Failed to create quotation');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6 pb-12 font-sans">
      {/* Header */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => navigate(-1)}
          className="inline-flex items-center space-x-1 text-sm font-semibold text-slate-500 hover:text-slate-800 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back</span>
        </button>
        <div className="flex items-center space-x-3">
          <button
            onClick={() => handleSubmit('DRAFT')}
            disabled={isSubmitting}
            className="inline-flex items-center space-x-2 px-4 py-2 rounded-xl border border-slate-300 bg-white hover:bg-slate-50 text-slate-700 font-semibold text-sm shadow-xs transition-colors disabled:opacity-50"
          >
            <Save className="w-4 h-4" />
            <span>Save Draft</span>
          </button>
          <button
            onClick={() => handleSubmit('SENT')}
            disabled={isSubmitting}
            className="inline-flex items-center space-x-2 px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-sm shadow-xs transition-colors disabled:opacity-50"
          >
            {isSubmitting ? <Loader2 className="w-4 h-4 animate-spin" /> : <Send className="w-4 h-4" />}
            <span>Generate & Finalize</span>
          </button>
        </div>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-xs p-6 sm:p-8 space-y-8">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Create New Quotation</h1>
          <p className="text-sm text-slate-500 mt-1">
            Complete the form below. Financial figures will be accurately computed and verified on the server.
          </p>
        </div>

        {error && (
          <div className="p-4 rounded-xl bg-red-50 border border-red-200 flex items-start space-x-3 text-red-700 text-sm">
            <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}

        {/* Customer Information */}
        <div className="space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-2">
            <h2 className="text-base font-bold text-slate-800">Customer Details</h2>
            {existingCustomers.length > 0 && (
              <select
                onChange={handleSelectCustomer}
                value={selectedCustomerId || ''}
                className="text-xs border border-slate-300 rounded-lg px-2.5 py-1 bg-white text-slate-700 focus:outline-hidden focus:ring-1 focus:ring-blue-600"
              >
                <option value="">Select Existing Customer (Optional)</option>
                {existingCustomers.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            )}
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Customer / Company Name *</label>
              <input
                type="text"
                placeholder="e.g. Acme Traders"
                value={customerName}
                onChange={(e) => setCustomerName(e.target.value)}
                className="w-full px-3.5 py-2 rounded-lg border border-slate-300 text-sm focus:outline-hidden focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Customer Email</label>
              <input
                type="email"
                placeholder="customer@domain.com"
                value={customerEmail}
                onChange={(e) => setCustomerEmail(e.target.value)}
                className="w-full px-3.5 py-2 rounded-lg border border-slate-300 text-sm focus:outline-hidden focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Phone Number</label>
              <input
                type="text"
                placeholder="+91 98765 43210"
                value={customerPhone}
                onChange={(e) => setCustomerPhone(e.target.value)}
                className="w-full px-3.5 py-2 rounded-lg border border-slate-300 text-sm focus:outline-hidden focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Billing Address</label>
              <input
                type="text"
                placeholder="Street address, City, Pincode"
                value={customerAddress}
                onChange={(e) => setCustomerAddress(e.target.value)}
                className="w-full px-3.5 py-2 rounded-lg border border-slate-300 text-sm focus:outline-hidden focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600"
              />
            </div>
          </div>
        </div>

        {/* Dates */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 border-b border-slate-100 pb-6">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Issue Date</label>
            <input
              type="date"
              value={issueDate}
              onChange={(e) => setIssueDate(e.target.value)}
              className="w-full px-3.5 py-2 rounded-lg border border-slate-300 text-sm focus:outline-hidden focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600"
            />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Expiry Date</label>
            <input
              type="date"
              value={expiryDate}
              onChange={(e) => setExpiryDate(e.target.value)}
              className="w-full px-3.5 py-2 rounded-lg border border-slate-300 text-sm focus:outline-hidden focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600"
            />
          </div>
        </div>

        {/* Line Items Table */}
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-bold text-slate-800">Items & Services</h2>
            <button
              type="button"
              onClick={handleAddItem}
              className="inline-flex items-center space-x-1 px-3 py-1.5 rounded-lg bg-blue-50 text-blue-700 hover:bg-blue-100 text-xs font-semibold transition-colors"
            >
              <Plus className="w-4 h-4" />
              <span>Add Item</span>
            </button>
          </div>

          <div className="space-y-3">
            {items.map((item, index) => {
              const lineTotal = (Number(item.quantity) || 0) * (Number(item.unitPrice) || 0);
              return (
                <div
                  key={index}
                  className="flex flex-col sm:flex-row items-start sm:items-center gap-3 p-3 bg-slate-50/60 rounded-xl border border-slate-200/60"
                >
                  <div className="flex-1 w-full sm:w-auto">
                    <input
                      type="text"
                      placeholder="Item name / description"
                      value={item.description}
                      onChange={(e) => handleItemChange(index, 'description', e.target.value)}
                      className="w-full px-3 py-2 bg-white rounded-lg border border-slate-300 text-sm focus:outline-hidden focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600"
                    />
                  </div>

                  <div className="w-24 shrink-0">
                    <input
                      type="number"
                      step="any"
                      min="0.01"
                      placeholder="Qty"
                      value={item.quantity}
                      onChange={(e) => handleItemChange(index, 'quantity', parseFloat(e.target.value) || 0)}
                      className="w-full px-3 py-2 bg-white rounded-lg border border-slate-300 text-sm focus:outline-hidden focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600"
                    />
                  </div>

                  <div className="w-32 shrink-0">
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      placeholder="Unit Price"
                      value={item.unitPrice}
                      onChange={(e) => handleItemChange(index, 'unitPrice', parseFloat(e.target.value) || 0)}
                      className="w-full px-3 py-2 bg-white rounded-lg border border-slate-300 text-sm focus:outline-hidden focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600"
                    />
                  </div>

                  <div className="w-28 text-right font-bold text-sm text-slate-800 shrink-0 self-center">
                    {currencySymbol} {lineTotal.toFixed(2)}
                  </div>

                  <button
                    type="button"
                    onClick={() => handleRemoveItem(index)}
                    className="p-2 text-slate-400 hover:text-red-600 transition-colors shrink-0 self-center"
                    title="Remove Item"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              );
            })}
          </div>
        </div>

        {/* Calculation & Summary Section */}
        <div className="border-t border-slate-100 pt-6 flex flex-col md:flex-row justify-between gap-8">
          {/* Notes & Terms */}
          <div className="flex-1 space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Notes (Optional)</label>
              <textarea
                rows={2}
                placeholder="Special notes or payment instructions..."
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                className="w-full px-3 py-2 rounded-lg border border-slate-300 text-sm focus:outline-hidden focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Terms and Conditions</label>
              <textarea
                rows={3}
                value={terms}
                onChange={(e) => setTerms(e.target.value)}
                className="w-full px-3 py-2 rounded-lg border border-slate-300 text-sm focus:outline-hidden focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600"
              />
            </div>
          </div>

          {/* Totals Breakdown */}
          <div className="w-full md:w-80 bg-slate-50 p-5 rounded-2xl border border-slate-200/80 space-y-3">
            <div className="flex justify-between text-sm text-slate-600">
              <span>Subtotal:</span>
              <span className="font-semibold text-slate-900">
                {currencySymbol} {subtotal.toFixed(2)}
              </span>
            </div>

            <div className="flex items-center justify-between text-sm text-slate-600">
              <span className="flex items-center space-x-1">
                <span>Discount %:</span>
              </span>
              <div className="flex items-center space-x-2">
                <input
                  type="number"
                  min="0"
                  max="100"
                  step="0.5"
                  value={discountPercentage}
                  onChange={(e) => setDiscountPercentage(parseFloat(e.target.value) || 0)}
                  className="w-16 px-2 py-1 bg-white border border-slate-300 rounded text-right text-xs"
                />
                <span className="font-semibold text-slate-700">-{currencySymbol} {discountAmount.toFixed(2)}</span>
              </div>
            </div>

            <div className="flex justify-between text-sm text-slate-600">
              <span>Taxable Amount:</span>
              <span className="font-semibold text-slate-900">
                {currencySymbol} {taxableAmount.toFixed(2)}
              </span>
            </div>

            <div className="flex items-center justify-between text-sm text-slate-600">
              <span className="flex items-center space-x-1">
                <span>GST / Tax %:</span>
              </span>
              <div className="flex items-center space-x-2">
                <input
                  type="number"
                  min="0"
                  max="100"
                  step="0.5"
                  value={taxPercentage}
                  onChange={(e) => setTaxPercentage(parseFloat(e.target.value) || 0)}
                  className="w-16 px-2 py-1 bg-white border border-slate-300 rounded text-right text-xs"
                />
                <span className="font-semibold text-slate-700">+{currencySymbol} {taxAmount.toFixed(2)}</span>
              </div>
            </div>

            <div className="border-t border-slate-200 pt-3 flex justify-between items-baseline">
              <span className="text-base font-bold text-slate-900">Final Total:</span>
              <span className="text-xl font-extrabold text-blue-600">
                {currencySymbol} {totalAmount.toFixed(2)}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
