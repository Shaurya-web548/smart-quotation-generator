import { describe, it, expect } from 'vitest';

describe('Quotation Math Consistency', () => {
  it('computes exact subtotal, discount, tax and total matching legacy values', () => {
    const items = [
      { quantity: 2, unitPrice: 150.0 },
      { quantity: 1, unitPrice: 99.5 },
    ];

    const discountPct = 10;
    const gstPct = 18;

    const subtotal = items.reduce((acc, it) => acc + it.quantity * it.unitPrice, 0);
    expect(subtotal).toBe(399.5);

    const discountAmount = (subtotal * discountPct) / 100;
    expect(discountAmount).toBe(39.95);

    const taxable = subtotal - discountAmount;
    expect(taxable).toBe(359.55);

    const taxAmount = Number(((taxable * gstPct) / 100).toFixed(2));
    expect(taxAmount).toBe(64.72);

    const total = Number((taxable + taxAmount).toFixed(2));
    expect(total).toBe(424.27);
  });
});
